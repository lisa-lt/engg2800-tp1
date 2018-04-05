package midiblocks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import org.jfugue.pattern.Pattern;

import processingblocks.ProcessingBlock;
import scales.Note;
import scales.NoteDictionary;
import scales.Scale;

/**
 * This contains the state of the program (the application's model in the
 * MVC architecture). This program is responsible for setting up the connection
 * logic from midi source to the processing blocks to output source.
 * @author Lisa Liu-Thorrold
 *
 */
public class MidiModel extends EventEmitter {

	private boolean metronomeMuted;
	private boolean playBackStarted;
	private boolean processingStarted;
	private boolean midiFileProcessingStarted;
	
	private int tempo;
	// The first key available to the keyboard
	private int firstKey;
	// The last key in the keyboard
	private int lastKey;
	
	// range of available notes for the keyboard.
	private Note[] availableKeyboardNotes;
	
	// full scale of available notes for the keyboard
	private Note[] availableNotes;
	
	// The current root note
	private Note currentRootNote;
	
	// The index of the current root note in the global available arrays.
	private int currentRootNoteIndex;

	private String[] selectedMidiOutput;
	
	private File midiInputFile;
	
	// processing blocks in the processing block chain
	public LinkedList<ProcessingBlock> processingBlocks;

	private Map<Scale, String[]> scalesMap;
	
	// The currently selected scale by the user
	private Scale selectedScale;
	
	private final NoteDictionary noteDictionary;
	
	// To allow users to undo processing block configurations
	private final ProcessingBlockStack history;
	
	// Source of midi input for the users
	private VirtualKeyboard virtualKeyboard;
	
	// The current processor that is listening for midi events
	private InputProcessor inputProcessor;
	
	private final ProcessingBlockController pblockController;
	
	// The current processor that is writing midi events to output file/devices
	private OutputProcessor outputProcessor;


	public MidiModel () {
		processingBlocks = new LinkedList<>();
		processingStarted = false;
		noteDictionary = new NoteDictionary();
		firstKey = 0;
		lastKey = 0;
		availableKeyboardNotes = new Note[19];
		metronomeMuted = true;
		history = new ProcessingBlockStack(this);
		playBackStarted = false;
		pblockController = new ProcessingBlockController(this);
		midiFileProcessingStarted = false;
	}

	/*************************************************
	 *  Getter/setter methods
	 *************************************************/

	public void changeMetronome(boolean isMute) { metronomeMuted = isMute; }
	public boolean isMetronomeMuted() {  return metronomeMuted; }
	public int getTempo() { return tempo; }
	public boolean playBackStarted() { return playBackStarted; }
	public Note[] getKeyboardNotes() { return availableKeyboardNotes; }
	public void setMidiSourceFile(File input) { midiInputFile = input; }
	public File getMidiSourceFile() { return midiInputFile; }
	public Pattern getPatternOutput() { return outputProcessor.getPattern(); }
	public InputProcessor getInputProcessor() { return inputProcessor; }
	public Note[] getAvailableNotes() { return availableNotes; }
	
	/**
	 * This method returns the previous state of the processing block chain
	 * as requested by the user
	 */
	public void undo() {
		this.processingBlocks = history.pop();
	}
	
	/**
	 * Adds to the history the state of the processing block chain, before the
	 * user makes a configuration that alters the state of the chain.
	 * @param processingBlocks - The current state of the processing blocks chain
	 */
	public void addToHistory(LinkedList<ProcessingBlock> processingBlocks) {
		history.push(processingBlocks);
	}

	/**
	 * Sets up the MIDI source with the relevant input processor to listen for
	 * MIDI events on
	 * @param selectedMidiSource - the selected midi source (as a string)
	 */
	public void setMidiSource(String selectedMidiSource) {
		
		midiFileProcessingStarted = false;

		if (inputProcessor != null) {
			// remove all current observers so that duplicate messages 
			// not received.
			inputProcessor.removeAllObservers();
			inputProcessor.setRunning(false);
			
		}

		InputProcessorFactory factory = new InputProcessorFactory(this);
		inputProcessor = factory.getProcessor(selectedMidiSource);
		inputProcessor.registerObserver(pblockController);
		
		if (inputProcessor instanceof KeyboardInputProcessor) {
			virtualKeyboard.setInputProcessor(inputProcessor);
		}
		
		if (inputProcessor instanceof FileInputProcessor) {
			// if playback has already started, start processing midi file
			if (playBackStarted) {
				((FileInputProcessor)inputProcessor).process();
			}
		}

	}
	
	/**
	 *  Sets the tempo an alerts the processing block controller of a tempo
	 *  change event
	 *  @param tempo - The new tempo set by the user
	 */
	public void setTempo(int tempo) { 
		this.tempo = tempo; 
		this.emit("newTempo");
	}

	/**
	 * Sets up the MIDI output with the relevant output processor to send
	 * MIDI events to after processing.
	 * @param midiOutput - The outputs as selected by the user. Could be one of:
	 *                   1. USB device
	 *                   2. MIDI file
	 *                   3. Both USB device and MIDI file
	 */
	public void setMidiOutput(String[] midiOutput) {
		this.selectedMidiOutput = midiOutput;
		
		if (pblockController != null) {
			// remove all current observers so that duplicate messages are not
			// received. (ie. Memory leak )
			pblockController.removeAllObservers();
		}
		
		if (outputProcessor != null) {
			// close any existing serial connections before starting a new one.
			outputProcessor.closeConnection();
		}

		switch (midiOutput.length) {
		case 1:
			// only one thing is selected as midi output
			// determine whether midi source or usb output
			if (midiOutput[0].contains(".mid")) {  //midi output selected
				outputProcessor = new OutputProcessor(this, true, false);
			} else {							//usb device selected
				outputProcessor = new OutputProcessor(this, false, true);
			}
			break;
		default:
			//both midi file and usb output selected
			outputProcessor = new OutputProcessor(this, true, true);
		}
		
		// set the output process to listen to the pblock controller.
		assert pblockController != null;
		pblockController.registerObserver(outputProcessor);
	}

	public LinkedList<ProcessingBlock> getProcessingBlocks() {
		return processingBlocks;
	}
	
	public void clearProcessingBlocks() {
		processingBlocks.clear();
	}
	
	public void setPlayBackStarted(boolean playBackStarted) {
		// starting new playback should refresh/renew current midi pattern
		if (playBackStarted) {
			outputProcessor.refreshPattern();
			
			// Midi file processing is stopped by default. If it hasn't
			if (!midiFileProcessingStarted) {
				if (inputProcessor instanceof FileInputProcessor) {				
					((FileInputProcessor) inputProcessor).process();
				}
				
				midiFileProcessingStarted = true;
			} else {
				if (inputProcessor instanceof FileInputProcessor) {		
					((FileInputProcessor) inputProcessor).newThreadStartProcessing();
				}
			}
		} else {
			// pause the midi file from processing
			if (inputProcessor instanceof FileInputProcessor) {				
				((FileInputProcessor) inputProcessor).pauseMidiProcessing();
			}
		}
		
		this.playBackStarted = playBackStarted;
	}

	public void setScales(Map<Scale, String[]> scalesMap) {
		this.scalesMap = scalesMap;
	}

	public void setScale(String mode, String rootNote) {
		this.selectedScale = new Scale (mode, rootNote);
		setAvailableNotes();
		initialiseKeyboardNotes();
	}

	public boolean isRootNote(String s) {
		return s.equals(selectedScale.getRootNote());
	}
	

	public int getOctaveSize() {
		return scalesMap.get(selectedScale).length;
	}
	
	public void setVirtualKeyboard(VirtualKeyboard keyboard) {
		this.virtualKeyboard = keyboard;
	}
	
	public VirtualKeyboard getVirtualKeyboard() {
		return virtualKeyboard;
	}

	public void setProcessingStarted(boolean proccesingStarted) {
		this.processingStarted = proccesingStarted;
	}
	
	public String[] getSelectedMidiOutput() {
		return selectedMidiOutput;
	}


	/*************************************************
	 *  Helper methods
	 *************************************************/

	/**
	 * This method sets the available notes according to the globally selected
	 * scale
	 */
	private void setAvailableNotes() {
		String[] notes = scalesMap.get(selectedScale);
		availableNotes = noteDictionary.filterAvailableNotes(notes);
		pblockController.setAvailableNotes();
	}

	/**
	 * This method initialises notes on the keyboard. Sends configurations on
	 * keyboard start up.
	 */
	private void initialiseKeyboardNotes() {
		int middle = availableNotes.length/2;
		firstKey = middle - 10;
		lastKey = middle + 9;

		availableKeyboardNotes = Arrays.copyOfRange(availableNotes, firstKey, 
				lastKey);

		//Set the lowest visible root note
		currentRootNote = getCurrentRootNote(availableKeyboardNotes);
		int indexOfRootNote = getIndexOfNote(currentRootNote);
		sendConfigurations(indexOfRootNote);
	}

	/**
	 * This method returns the lowest visible rootNote
	 * @param availableKeyboardNotes - Notes currently on the virtual keyboard
	 * @return The lowest root note - The lowest visible root note on the
	 * 		   virtual keyboard
	 */
	private Note getCurrentRootNote(Note[] availableKeyboardNotes) {
		Note temp = availableKeyboardNotes[0];
		for (Note availableKeyboardNote : availableKeyboardNotes) {
			temp = availableKeyboardNote;

			String noteLetter = temp.getNoteLetter();

			if (isRootNote(noteLetter)) {
				break;
			}
		}
		
		return temp;
	}
	
	/**
	 * This method gets the index of the given note in the available notes
	 * @param note - The note at which to retrieve the index of
	 * @return The index of the given note in the availableNotes array.
	 */
	private int getIndexOfNote(Note note) {

		for(int i = 0; i < availableNotes.length; i++) {
			if (availableNotes[i].equals(note)) {
				return i;
			}
		}
		
		//should never reach here.
		return 0;
	}
	

	/**
	 * This method is invoked when the user wishes to shift the view of the
	 * keyboard up or down. The shift size can be size 1, or the size of
	 * an octave. A negative number indicates a shift to the left.
	 * @param shiftSize - The size of the pitchshift
	 */
	public void shiftKeyboard(int shiftSize){
		if (firstKey + shiftSize <=0 ) {
			firstKey = 0;
			lastKey = firstKey + 19;
		} else if (lastKey + shiftSize > availableNotes.length) {
			lastKey = availableNotes.length;
			firstKey = lastKey - 19;
		} else {
			firstKey = firstKey + shiftSize;
			lastKey = lastKey + shiftSize;
		}

		availableKeyboardNotes = Arrays.copyOfRange(availableNotes, firstKey, 
				lastKey);
		
		// Set the lowest available root note
		Note currentRootNote = getCurrentRootNote(availableKeyboardNotes);
		int indexOfRootNote = getIndexOfNote(currentRootNote);
		// check to see whether it's changed
		checkRootNoteChange(indexOfRootNote, currentRootNote);
		
	}
	
	/**
	 * This method checks to see whether the lowest visible root note has 
	 * changed.
	 * @param newRootNoteIndex - The index of the rootNoteIndex to check
	 * @param newRootNote - The new root note to check
	 */
	private void checkRootNoteChange(int newRootNoteIndex, Note newRootNote) {

		if ((newRootNoteIndex != currentRootNoteIndex) || (currentRootNote == null) ||
				(currentRootNote.getKeyNumber() != newRootNote.getKeyNumber())) {
			// THE LOWEST VISIBLE ROOT NOTE HAS CHANGED!
			System.out.println("New root note: " + availableNotes[newRootNoteIndex]);
		
			// send configurations to hardware
			sendConfigurations(newRootNoteIndex);
			
			// set the new index
			currentRootNoteIndex = newRootNoteIndex;
			currentRootNote = newRootNote;
		} 
	}
	
	/**
	 * This method alerts the hardware of new configurations when either the
	 * root note has changed, or a new scale has been selected by the user
	 * @param index - The index of the lowest visible root note on the keyboard
	 */
	private void sendConfigurations(int index) {
		ArrayList<Integer> notesForConfiguredScale = new ArrayList<>();

		for(int i = 0; i < 8; i++) {
			if (index + i > availableNotes.length-1 ) {
				break;
			}
			
			int noteNumber = availableNotes[index+i].getKeyNumber();
			notesForConfiguredScale.add(noteNumber);
			//notesForConfiguredScale[i] = noteNumber;
		}

		System.out.println("First 8 note numbers of configured scale: " +
				notesForConfiguredScale);
		
		// Alert the hardware
		try {
			outputProcessor.sendHardwareConfigurations(notesForConfiguredScale);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	/**
	 * This method is called when there has been a change in the configuration
	 * of processing blocks.
	 */
	public void connectProcessingBlocks() {
		pblockController.connectProcessingBlocks();
	}

}

