package midiblocks;
import java.awt.Canvas;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfugue.player.Player;

import processingblocks.Arpeggiator;
import processingblocks.Gates;
import processingblocks.PitchShift;
import processingblocks.ProcessingBlock;
import processingblocks.ProcessingBlockFactory;
import scales.Scale;


/**
 * The application's controller, using MVC architecture.
 *
 * @author Lisa Liu-Thorrold
 */
public class MidiController {
	
	// The model from the MVC architecture
	private final MidiModel model;
	
	// The view from the MVC architecture
	private final MidiBlocksContainer view;
	
	private VirtualKeyboard keyboard;
	
	private Metronome metronome;
	
	// Used to flag the metronome's starting state
	private boolean hasStarted;
	
	// Used to schedule the metronome's timing
	private ScheduledFuture<?> sf;

	/**
	 * This is the controller of the application in the MVC architecture
	 * @param model - The application's model
	 * @param container - The view of the application
	 */
	public MidiController(MidiModel model, MidiBlocksContainer container) {

		this.model = model;
		this.view = container;
		hasStarted = false;
		initKeyboard();

		// Add listeners for the GUI components 
		view.addListener("saveConfiguration", event -> saveConfiguration());
		view.addListener("loadConfiguration", event -> loadConfiguration());
		view.addListener("loadScales", event -> loadScales());
		view.addListener("clearConfigurations", event -> clearConfigurations());
		view.addListener("setConfigurations", event -> setConfigurations());
		view.addListener("addProcessingBlock", event -> addProcessingBlock());
		view.addListener("mutePressed", event -> mutePressed());
		view.addListener("MIDISourceSelected", event -> MIDISourceSelected());
		view.addListener("MIDIOutputSelected", event -> MIDIOutputSelected());
		view.addListener("saveMidiFile", event -> saveMidiFile());
		view.addListener("undo", event -> undoProcessingBlockChange());
		view.addListener("startMidiPlayBack", event -> startMidiPlayBack());
		view.addListener("stopMidiPlayBack", event -> stopMidiPlayBack());
		view.addListener("previewPlayback", event -> previewMidiPlayBack());
		view.addListener("setTempo", event -> setTempo());
		view.addListener("clearAllProcessingBlocks", event -> 
				clearAllProcessingBlocks());
		view.addListener("changeProcessingBlock", event -> 
				changeProcessingBlock());
		view.addListener("shiftProcessingBlockUp", event -> 
				shiftProcessingBlockUp());
		view.addListener("shiftProcessingBlockDown", event -> 
				shiftProcessingBlockDown());
		view.addListener("deleteProcessingBlock", event -> 
				deleteProcessingBlock());
		view.addListener("clearProcessingBlock", event -> 
				clearProcessingBlock());
		view.addListener("changeProcessingBlockParameter", event -> 
		changeProcessingBlockParameter());
		
	}

	/*************************************************
	 * Control event listeners
	 *************************************************/

	/**
	 * The format of a configuration file is:
	 * 1. A CSV file
	 * 2. The first line containing the name of the MIDI source
	 * 3. Additional lines containing: Processing Block Name, Parameters
	 */
	private void loadConfiguration() {
		
		// if midi playback has started, stop it
		stopMidiPlayBack();
		
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				".csv files", "csv");
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {
			
			File selectedFile = fileChooser.getSelectedFile();
			BufferedReader bufferedReader = null;
			String line;
			String cvsSplitBy = ",";

			try {
				bufferedReader = new BufferedReader(new FileReader(selectedFile));
				
				// Used to indicate whether the MIDI input matches anything
				// currently in the MIDI input combo box in the GUI 
				boolean flag = false;

				// Get the MIDI source
				String configuration = bufferedReader.readLine();
			
				// Clear all the processing blocks current in the JList
				clearAllProcessingBlocks();
				
				int size = view.getMidiSources().getModel().getSize();

				// Set the MIDI source to be the one selected, if it is
				// currently available as a MIDI source, otherwise set the
				// selected MIDI input to nothing.
				for (int i = 0; i < size; i++) {
					if (view.getMidiSources().getModel().getElementAt(i).
							equals(configuration)) {
						view.getMidiSources().setSelectedIndex(i);
						flag = true;
						break;
					}
				}

				// no match was for for midi source to currently available
				// midi sources, so set the selected MIDI source at empty
				if (!flag) {
					view.getMidiSources().setSelectedIndex(-1);
				}

				// Process the remaining lines which contain the processing
				// blocks
				while ((line = bufferedReader.readLine()) != null) {
					// use comma as separator
					String[] processingBlocks = line.split(cvsSplitBy);		
					ProcessingBlockFactory factory = new ProcessingBlockFactory();

					System.out.println(Arrays.toString(processingBlocks));

					ProcessingBlock block = factory.makeProcessingBlock(
							processingBlocks[0], processingBlocks[1],
							processingBlocks[2]);
					
					// Factory returns null, so invalid processing block
					if(block == null) {
						JOptionPane.showMessageDialog(null, "Configuration file "
								+ "format not correct");
						model.clearProcessingBlocks();
						view.getMidiSources().setSelectedIndex(-1);
						return;
					}

					// valid block with valid parameters
					model.getProcessingBlocks().add(block);
				}
				
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Configuration file "
						+ "format not correct");
				model.clearProcessingBlocks();
				view.getMidiSources().setSelectedIndex(-1);
			} finally {
				try {
					assert bufferedReader != null;
					bufferedReader.close();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Configuration file"
							+ " format not correct");
				}
			}
		}

		// Update the processing list block in the GUI to represent the 
		// internal state.
		updateProcessingListView();
	}

	/**
	 * This method saves the configurations currently set by the user into
	 * a CSV file. The CSV File format is:
	 * Line 1: Midi Source
	 * Line 2 onwards: Processing block, parameter1, parameter2
	 */
	private void saveConfiguration() {
		final String NEW_LINE_SEPARATOR = "\n";
		final String COMMA_DELIMITER = ",";

		//need to check that input source is not null.
		if (view.getMidiSources().getSelectedIndex() == -1) {
			view.showMessageDialog("A MIDI Source must be selected");
			return;
		}

		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				".csv files", "csv");
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int result = fileChooser.showSaveDialog(fileChooser);
		FileWriter writer = null;
		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				File file = fileChooser.getSelectedFile();
				
				// Append suffix if user forgot to do so.
				if(!fileChooser.getSelectedFile().getAbsolutePath().endsWith(".csv")) {
					// Add csv suffix
					file = new File(fileChooser.getSelectedFile() + ".csv");
				}
				
				writer = new FileWriter(file);

				/* Append the MIDI source */
				String source = view.getMidiSources().getSelectedItem().toString();
				writer.append(source);
				writer.append(NEW_LINE_SEPARATOR);

				/* Append all the processing blocks */
				for (ProcessingBlock block : model.getProcessingBlocks()) {
					writer.append(block.getName());
					writer.append(COMMA_DELIMITER);
					writer.append(block.getParameters());
					writer.append(NEW_LINE_SEPARATOR);
				}
			} catch (Exception e) {
				view.showMessageDialog("Error saving configurations");
			} finally {
				try {
					assert writer != null;
					writer.flush();
					writer.close();
				} catch (IOException e) {
					view.showMessageDialog("Error saving configurations");
				}
			}
		}
	}

	/**
	 * This method loads a CSV file containing the scales available to the
	 * user. The CSV file is in the following format:
	 * The first column contains the mode of the key, followed by all
	 * the notes in that scale starting with the root note of the key. 
	 * The file will contain one full octave of notes as well as a second root 
	 * note of the next octave as the final entry.
	 */
	private void loadScales() {

		// Map of the scale name, root note, to array of notes 
		Map<Scale, String[]> scalesMap =
				new HashMap<>();

		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
						".csv files", "csv");
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			BufferedReader bufferedReader = null;
			String line;
			String cvsSplitBy = ",";

			try {
				bufferedReader = new BufferedReader(new FileReader(selectedFile));

				// Process the file
				while ((line = bufferedReader.readLine()) != null) {
					// use comma as separator
					String[] scalesLine = line.split(cvsSplitBy);

					String scaleName = scalesLine[0];
					String rootNote = scalesLine[1];

					Scale scale = new Scale(scaleName, rootNote);

					// Copy the range of notes
					String[] notes = Arrays.copyOfRange(scalesLine, 1, 
							scalesLine.length - 1);

					// Parse the notes to check for correct format
					String[] parsedNotes = parseArray(notes);

					// Put the scale, and it's notes into the map
					scalesMap.put(scale, parsedNotes);

					// Add the scale mode into the JCombo box if not already 
					// there 
					if (view.getModeModel().getIndexOf(scaleName) == -1) {
						view.getModeModel().addElement(scaleName);
					}

					// Add the root note into the JCombo box if not already 
					// there 
					if (view.getRootNoteModel().getIndexOf(rootNote) == -1) {
						view.getRootNoteModel().addElement(rootNote);
					}

				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error loading scales file");
			} finally {
				try {
					assert bufferedReader != null;
					bufferedReader.close();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Error loading scales file");
				}
			}
		}

		// Once all the scales have been loaded and processed, set the model's 
		// allowable scales to this map.
		model.setScales(scalesMap);

		// Make sure mode/note boxes have not selected anything automatically
		view.getRootNoteComboBox().setSelectedIndex(-1);
		view.getModeComboBox().setSelectedIndex(-1);
	}

	/**
	 * This method clears any configurations that have been made
	 */
	private void clearConfigurations() {
		view.getMidiSources().setSelectedIndex(-1);
		view.getMidiOutputs().setSelectedIndex(-1);
		view.getTempo().setText("");
		view.getRootNoteComboBox().setSelectedIndex(-1);
		view.getModeComboBox().setSelectedIndex(-1);
		view.getSaveMidiFileButton().setVisible(false);
	}

	/**
	 * Depending on which Input is selected
	 * 1. Virtual keyboard --> instantiates a PApplet
	 * with the keys determined by the selected root note
	 * and mode
	 * 2. Actions to be done 
	 * (i).   Set the model's tempo
	 * (ii).  Set model's midi output source
	 * (iii). Set the model's midi input source
	 * (iv).  Establish connection with input
	 * 3. Need to make sure all the fields are not null
	 */
	private void setConfigurations() {
		
		// Set the model's MIDI source. Currently the model's MIDI source is
        // represented by a string . Checks for valid midi file
		if (!checkValidMidiFile()) {
			return;
		}
		
		// Stop the model from processing, if it is currently doing so 
		model.setProcessingStarted(false);

		// Check to see whether things have been entered in all the fields 
		if (validateInput()) {
			return;
		}

		// Set the model's tempo
		setTempo();

		//Set the globally selected scale in the model
		String rootNote = view.getRootNoteComboBox().getSelectedItem().toString();
		String mode = view.getModeComboBox().getSelectedItem().toString();
		model.setScale(mode, rootNote);
		
		setUpMidiSource();

		// Display save midi file button if the output is a MIDI file
		if (view.getMidiOutputs().getSelectedItem().toString().contains("mid")) {
			view.getSaveMidiFileButton().setVisible(true);
		}

		// Display and configure the MIDI playback buttons
		view.getStartMidiPlaybackButton().setVisible(true);
		view.getStopMidiPlaybackButton().setVisible(true);
		view.getPreviewPlaybackButton().setVisible(true);
		setPlayBackButtons();
		
		//get the model ready to receive input
		model.setProcessingStarted(true);
	}

	/**
	 * This method starts the metronome at the tempo specified by the user
	 */
	private void startMetronome() {

		int tempo = model.getTempo();
		int period = 60000/tempo; // the beats per minute specified by user
		boolean metronomeMuted = model.isMetronomeMuted();
		Canvas visualMetronome =  view.getMetronomeCanvas();
		this.metronome = new Metronome(metronomeMuted, visualMetronome, tempo);

		ScheduledExecutorService metronomeTimer = 
				Executors.newSingleThreadScheduledExecutor();

		// if the controller does not have a metronome started, then start it
		// This calls the metronome at an interval of period which is the tempo
		// set by the user.
		if (!hasStarted) {
			sf = metronomeTimer.scheduleAtFixedRate(metronome, 0, period, 
					TimeUnit.MILLISECONDS);
		} else {
			// otherwise cancel the running metronome, and run a new one. This
			// is for when the user sets a new tempo
			sf.cancel(true);
			sf = metronomeTimer.scheduleAtFixedRate(metronome, 0, period, 
					TimeUnit.MILLISECONDS);
		}
		
	}
	
	/**
	 * This method clears the configurations for the processing blocks
	 */
	private void clearProcessingBlock() {
		view.getProcessingBlockToAdd().setSelectedIndex(-1);
		view.getArpeggiatorType().setSelectedIndex(-1);
		view.getGatesComboBox().setSelectedIndex(-1);
		view.getPitchShiftField().setText("");
		view.getGatesTextField().setText("");
		view.getArpeggiatorType().setVisible(false);
		view.getPitchShiftLabel().setVisible(false);
		view.getPitchShiftField().setVisible(false);
		view.getGatesComboBox().setVisible(false);
		view.getGatesTextField().setVisible(false);
		view.getGatesModeLabel().setVisible(false);
		view.getGatesReleaseLabel().setVisible(false);
	}

	/**
	 * This method adds a processing block to the model's processing block 
	 * chain
	 */
	private void addProcessingBlock() {
		// Add state of processing blocks to the history, as this operation  
		// affects the processing block chain 
		addToHistory(model.getProcessingBlocks());

		ProcessingBlockFactory factory = new ProcessingBlockFactory();
		ProcessingBlock block;
		String parameter1 = "";
		String parameter2 = "";
		try {
			String blockType = view.getProcessingBlockToAdd().
					getSelectedItem().toString();
			switch (blockType) {
			case "Arpeggiator":
				parameter1 = view.getArpeggiatorType().getSelectedItem().toString();
				block = factory.makeProcessingBlock("Arpeggiator", parameter1, parameter2);
				model.getProcessingBlocks().add(block);
				break;
			case "Chordify":
				block = factory.makeProcessingBlock("Chordify", parameter1, parameter2);
				model.getProcessingBlocks().add(block);
				break;
			case "Gates":
				parameter1 = view.getGatesComboBox().getSelectedItem().toString();
				parameter2 = view.getGatesTextField().getText();
				block = factory.makeProcessingBlock("Gates", parameter1, parameter2);
				if (block == null) {
					view.showMessageDialog("Notes per tick cannot be 0");
					return;
				}
				model.getProcessingBlocks().add(block);
				break;
			case "Monophonic":
				block = factory.makeProcessingBlock("Monophonic", parameter1, parameter2);
				model.getProcessingBlocks().add(block);
				break;
			case "Pitch Shift":
				parameter1 = view.getPitchShiftField().getText();
				block = factory.makeProcessingBlock("PitchShift", parameter1, parameter2);
				model.getProcessingBlocks().add(block);
				break;
			}
			
			model.connectProcessingBlocks();

		} catch (Exception e) {
			view.showMessageDialog("Need to select type of processing block, "
					+ " and enter correct parameters (if applicable).");
			return;
		}

		updateProcessingListView();

	}

	/** 
	 * This method shifts the processing block up in the model's processing
	 * block chain
	 */
	private void shiftProcessingBlockUp() {
		// Add state of processing blocks to the history, as this operation  
		// affects the processing block chain 
		addToHistory(model.getProcessingBlocks());
		
		int i = view.getProcessingBlockListBox().getSelectedIndex();
		
		// The top most block in the processing chain is selected, so do nothing
		if (i == 0 || i == -1) {
			return;
		}
		
		// Do the shift
		ProcessingBlock temp = model.getProcessingBlocks().get(i - 1);
		ProcessingBlock toShift = model.getProcessingBlocks().get(i);
		model.getProcessingBlocks().set(i - 1, toShift);
		model.getProcessingBlocks().set(i, temp);
		
		
		// Reconnect the processing blocks
		model.connectProcessingBlocks();
		
		// Update the GUI
		updateProcessingListView();
	}

	/** 
	 * This method shifts the processing block down in the model's processing
	 * block chain
	 */
	private void shiftProcessingBlockDown() {
		// Add state of processing blocks to the history, as this operation  
	    // affects the processing block chain 
		addToHistory(model.getProcessingBlocks());
		
		int i = view.getProcessingBlockListBox().getSelectedIndex();
		
		// The bottom most block in the processing chain, so do nothing
		if (i == model.getProcessingBlocks().size() - 1 || i == -1) {
			return;
		}
		
		// Do the shift
		ProcessingBlock temp = model.getProcessingBlocks().get(i + 1);
		ProcessingBlock toShift = model.getProcessingBlocks().get(i);
		model.getProcessingBlocks().set(i + 1, toShift);
		model.getProcessingBlocks().set(i, temp);
		
		// Reconnect the processing blocks
		model.connectProcessingBlocks();
		
		// Update the GUI 
		updateProcessingListView();
	}

	/** 
	 * This method delete the processing block in the model's processing
	 * block chain
	 */
	private void deleteProcessingBlock() {
		// Add state of processing blocks to the history, as this operation  
		// affects the processing block chain 
		addToHistory(model.getProcessingBlocks());
		
		int i = view.getProcessingBlockListBox().getSelectedIndex();
		
		// nothing was selected to delete by the user
		if (i == -1) {
			return;
		}
		
		model.getProcessingBlocks().remove(i);
		
		// Reconnect the processing blocks
		model.connectProcessingBlocks();
		
		// Update the GUI
		updateProcessingListView();
	}

	/** 
	 * This method deletes all the processing blocks currently in the model's 
	 * processing block chain.
	 */
	private void clearAllProcessingBlocks() {
		// Add state of processing blocks to the history, as this operation  
		// affects the processing block chain 
		addToHistory(model.getProcessingBlocks());
		model.getProcessingBlocks().clear();
		model.connectProcessingBlocks();
		updateProcessingListView();
	}
	
	/**
	 * This method undoes changes to the model's processing block chain, up to
	 * 15 changes. Processing block changes that can be undone include
	 * 1. Adding a processing block
	 * 2. Delete a processing block
	 * 3. Clearing all the processing blocks
	 * 4. Shifting a processing block up
	 * 5. Shifting a processing block down
	 */
	private void undoProcessingBlockChange() {
		model.undo();
		model.connectProcessingBlocks();
		updateProcessingListView();
	}

	/**
	 * This method updates the model's internal state, when an event on the
	 * mute check box is detected
	 */
	private void mutePressed() {
		boolean mutePressed = view.getMuteCheckBox().isSelected();
		model.changeMetronome(mutePressed);
		if (hasStarted) {
			metronome.setMute(mutePressed);
		}
	}

	/**
	 * This method is invoked when a midi source is selected.
	 * Depending on which MIDI Source is selected:
	 * 1. MIDI file --> opens a choose file dialog for the user to load a
	 * midi file
	 * 2. Virtual Keyboard --> should display Virtual Label
	 */
	private void MIDISourceSelected() {
		if (view.getMidiSources().getSelectedItem() == null) {
			return;
		}

		String source = view.getMidiSources().getSelectedItem().toString();

		if (source.contains("MIDI file")) {

			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"MIDI file", "mid");
			fileChooser.setFileFilter(filter);
			int result = fileChooser.showOpenDialog(fileChooser);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				model.setMidiSourceFile(selectedFile);
				try {
					Sequencer sequencer = MidiSystem.getSequencer();
					sequencer.setSequence(MidiSystem.getSequence(selectedFile));
					// Set the tempo text to the sequencer.
					int tempo;
					double midiTempo = (double)sequencer.getTempoInBPM();
					
					// round up the the tempo is not an integer, and greater
					// than 0.5 in the remainder.
					if (midiTempo % 1 >= 0.5) {
						tempo = (int) Math.ceil(midiTempo);
					} else {
						tempo = (int) Math.floor(midiTempo);
					}
					
					// Set the tempo in the text field after rounding.
					view.getTempo().setText(Integer.toString(tempo));
					sequencer.close();
					
				} catch (IOException e) {
					view.showMessageDialog("Problem accessing MIDI file, "
							+ "select a new MIDI file");
					return;
				} catch (Exception e) {
					view.showMessageDialog("Invalid MIDI file, select a new "
							+ "MIDI file");
					return;
				}
			}
		}

		if (source.contains("Virtual Keyboard")) {
			view.getVirtualKeyboardLabel().setVisible(true);
		} else {
			view.getVirtualKeyboardLabel().setVisible(false);
		}

	}
	
	/**
	 * This method is invoked when a midi output is selected
	 * and set's the model's MIDI output. The outputs may be:
     * 1. .mid file
     * 2. USB device (if connected)
     * 3. USB Device and .mid file 
	 */
	private void MIDIOutputSelected() {
		if (view.getMidiOutputs().getSelectedItem() == null) {
			return;
		}

		String output = view.getMidiOutputs().getSelectedItem().toString();

		try {
			// Contains both
			if (output.contains(".mid and USB")) {
				// show a message dialog to show which usb device the user wants 
				// to connect to.
				String selectedDevice = view.showUsbOutputDialog();
				
				if (selectedDevice == null) {
					view.showMessageDialog("No USB device selected,"
							+ " output will be .mid only");
					view.getMidiOutputs().setSelectedItem(".mid");
					MIDIOutputSelected();
				} else {
					String[] outputs = {".mid", selectedDevice};
					model.setMidiOutput(outputs);
				}
			} else {
				String[] outputs = { output };
				model.setMidiOutput(outputs);
			} 
		} catch (Exception e) {
			view.showMessageDialog("Unable to connect to USB device");
		}
	}

	/**
	 * This method saves the current state of the output into a MIDI file
	 */
	private void saveMidiFile() {
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				".mid", "mid");
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int result = fileChooser.showSaveDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				File file = fileChooser.getSelectedFile();
				
				// Add midi suffix if the user forgot to do that.
				if(!fileChooser.getSelectedFile().getAbsolutePath().endsWith(".mid")) {
					// Add csv suffix
					file = new File(fileChooser.getSelectedFile() + ".mid");
				}
				
				Sequence sequence = (new Player()).getSequence(
						model.getPatternOutput());
				OutputStream out = (new FileOutputStream(file));
				int[] writers = MidiSystem.getMidiFileTypes(sequence);
		        if(writers.length != 0) {
		            MidiSystem.write(sequence, writers[0], out);
		        }
		        out.close();
			} catch (Exception e) {
				view.showMessageDialog("Trouble saving MIDI file, check if file"
						+ " is currently used by another program.");
			}
		}
	}

	/**
	 * This method is invoked when a change in the processing block has been 
	 * detected in the JComboBox, and sets visible relevant GUI components.
	 */
	private void changeProcessingBlock() {
		if (view.getProcessingBlockToAdd().getSelectedItem() != null) {

			String processingBlockName = (String) 
					view.getProcessingBlockToAdd().getSelectedItem();
			switch (processingBlockName) {
			case "Arpeggiator":
				view.getArpeggiatorType().setSelectedIndex(-1);
				view.getArpeggiatorType().setVisible(true);
				view.getPitchShiftLabel().setVisible(false);
				view.getPitchShiftField().setVisible(false);
				view.getGatesReleaseLabel().setVisible(false);
				view.getGatesComboBox().setVisible(false);
				view.getGatesTextField().setVisible(false);
				view.getGatesModeLabel().setVisible(false);
				break;
			case "Chordify":
				view.getArpeggiatorType().setVisible(false);
				view.getPitchShiftLabel().setVisible(false);
				view.getPitchShiftField().setVisible(false);
				view.getGatesComboBox().setVisible(false);
				view.getGatesTextField().setVisible(false);
				view.getGatesModeLabel().setVisible(false);
				view.getGatesReleaseLabel().setVisible(false);
				break;
			case "Gates":
				view.getGatesComboBox().setVisible(true);
				view.getGatesComboBox().setSelectedIndex(-1);
				view.getGatesTextField().setVisible(true);
				view.getGatesModeLabel().setVisible(true);	
				view.getGatesReleaseLabel().setVisible(true);
				view.getArpeggiatorType().setVisible(false);
				view.getPitchShiftLabel().setVisible(false);
				view.getPitchShiftField().setVisible(false);
				break;
			case "Monophonic":
				view.getArpeggiatorType().setVisible(false);
				view.getPitchShiftLabel().setVisible(false);
				view.getPitchShiftField().setVisible(false);
				view.getGatesReleaseLabel().setVisible(false);
				view.getGatesComboBox().setVisible(false);
				view.getGatesTextField().setVisible(false);
				view.getGatesModeLabel().setVisible(false);
				break;
			case "Pitch Shift":
				view.getArpeggiatorType().setVisible(false);
				view.getPitchShiftLabel().setVisible(true);
				view.getPitchShiftField().setText("");
				view.getPitchShiftField().setVisible(true);
				view.getGatesReleaseLabel().setVisible(false);
				view.getGatesComboBox().setVisible(false);
				view.getGatesTextField().setVisible(false);
				view.getGatesModeLabel().setVisible(false);
				break;
			default:
				view.getArpeggiatorType().setVisible(false);
				view.getPitchShiftLabel().setVisible(false);
				view.getPitchShiftField().setVisible(false);
				view.getGatesComboBox().setVisible(false);
				view.getGatesReleaseLabel().setVisible(false);
				view.getGatesTextField().setVisible(false);
				view.getGatesModeLabel().setVisible(false);
			}
		}
	}
	
	/**
	 * This method sets the tempo and starts a metronome based on this tempo
	 * value. We limit tempo to be <= 250, so that the metronome doesn't go
	 * too crazy.
	 */
	private void setTempo() {
		try {
			String tempoText = view.getTempo().getText();
			int tempoValue = Integer.parseInt(tempoText);
			
			if (tempoValue <= 0 || tempoValue > 250) {
				view.showMessageDialog("Tempo must a positive "
						+ "integer that is 250 or under");
				return;
			}
			
			model.setTempo(tempoValue);
			startMetronome();
			hasStarted = true;
		} catch (NumberFormatException e) {
			view.showMessageDialog("Tempo must be an integer");
		}
	}
	
	/**
	 * This method allows the user to change the parameter of the processing
	 * block if they wish to, by right clicking on the processing block on
	 * the GUI, and entering a parameter. Only valid for arpeggiator and pitch
	 * shift processing blocks (as the other 2 do not take parameters).
	 */
	private void changeProcessingBlockParameter() {
		
		boolean changeMade = false;
		
		// Get the type of processing block parameter
		int i = view.getProcessingBlockListBox().getSelectedIndex();
		ProcessingBlock toChange = model.getProcessingBlocks().get(i);
		
		// Processing block to change is arpeggiator
		if (toChange instanceof Arpeggiator) {
			String newType = view.showArpeggiatorInputDialog();
			if (newType == null) {
				// Do nothing 
				return;
			} else {
				//Change the parameter of the processing block
				if (!((Arpeggiator) toChange).getType().equals(newType)) {					
					addToHistory(model.getProcessingBlocks());
					((Arpeggiator) toChange).setArpeggiatorType(newType);
					changeMade = true;
				}
			}
		} 
		
		// Processing block to change is Pitchshift
		if(toChange instanceof PitchShift) {
			Integer pitch = view.showPitchInputDialog();
			if (pitch == null) { 
				// Do nothing
				return;
			} else {
				if(((PitchShift) toChange).getPitch() != pitch) {
					addToHistory(model.getProcessingBlocks());
					//Change the parameter of the processing block
					((PitchShift) toChange).setPitch(pitch);
					changeMade = true;
				}
			}
		}
		
		// Processing block to change is Gates
		if (toChange instanceof Gates) {
			JTextField notesPerTickTextField = new JTextField(5);
			JComboBox<String> gatesComboBox = new JComboBox<>();
			gatesComboBox.addItem("Queue");
			gatesComboBox.addItem("First Hold");
			gatesComboBox.addItem("Last Hold");

			JPanel gatesPanel = new JPanel();
			gatesPanel.add(new JLabel("Gates mode:"));
			gatesPanel.add(gatesComboBox);
			gatesPanel.add(new JLabel("Notes per tick:"));
			gatesPanel.add(notesPerTickTextField);

			int result = JOptionPane.showConfirmDialog(null, gatesPanel, 
					"Enter new gate parameter values", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				
				Double notesPerTick;
				
				//check for valid input for notes per tick
				try {
					notesPerTick = 
							Double.parseDouble(notesPerTickTextField.getText());
					if (notesPerTick == 0) {
						view.showMessageDialog("Notes per tick cannot be 0");
						return;
					}
				} catch (Exception e) {
					view.showMessageDialog("Notes per tick must be a valid number");
					return;
				}
				
				String newMode = gatesComboBox.getSelectedItem().toString();
				
				// Check that a change has actually been made
				Double notesPerTickRounded = 
						Math.round(notesPerTick * 100.0) / 100.0;
				
				if (!notesPerTickRounded.equals(((Gates) toChange).getNotesPerTick())) {
						((Gates) toChange).setGatesNotesPerTick(notesPerTickRounded);
					changeMade = true;
				}
				
				if (!newMode.equals(((Gates) toChange).getMode())) {
					((Gates) toChange).setGatesMode(newMode);
					changeMade = true;
				}
				
			}
		}

		if (changeMade) {
			// Alert processing blocks of the change.
			model.connectProcessingBlocks();
		}
	}
	
	/**
	 * Previews the playback made by the user.
	 */
	private void previewMidiPlayBack() {
		(new Thread(() -> { 
			Player player = new Player();
			player.play(model.getPatternOutput());} )).start();
	}
	



	/*************************************************
	 * Helper methods
	 *************************************************/

	/**
	 * This method updates the Processing Block list in the GUI, to reflect
	 * the model's internal state of the processing block chain
	 */
	private void updateProcessingListView() {
		view.getProcessingBlockList().removeAllElements();
		for (int i = 0; i < model.getProcessingBlocks().size(); i++) {
			ProcessingBlock block = model.getProcessingBlocks().get(i);
			view.getProcessingBlockList().addElement(block);
		}
	}

	/**
	 * Thus method validates user input
	 */
	private boolean validateInput() {

		if (view.getMidiSources().getSelectedItem() == null) {
			view.showMessageDialog("A MIDI Source must be selected");
			return true;
		}

		if (view.getMidiOutputs().getSelectedItem() == null) {
			view.showMessageDialog("A MIDI Output must be selected");
			return true;
		}

		if (! (view.getTempo().getText().length() > 0) ) {
			view.showMessageDialog("Tempo must be entered");
			return true;
		}

		try {
			String tempoText = view.getTempo().getText();
			Integer.parseInt(tempoText);
		} catch (NumberFormatException e) {
			view.showMessageDialog("Tempo must be an integer");
			return true;
		}


		if (view.getRootNoteComboBox().getSelectedItem() == null) {
			view.showMessageDialog("A root note must be selected");
			return true;
		}

		if (view.getModeComboBox().getSelectedItem() == null) {
			view.showMessageDialog("A mode note must be selected");
			return true;
		}

		return false;
	}

	/**
	 * This method parses the scales, and makes sure that the file is in
	 * the correct format
	 * @param notes The list of notes
	 * @return a correct list of notes, with incorrectly formatted 
	 * 		   notes removed
	 */
	private String[] parseArray(String[] notes) {

		// To hold valid notes
		List<String> parsedNotes = new ArrayList<>();
		
		// A regular note
		Pattern sampleRegex = Pattern.compile("^([A-G])");
		
		// A sharp/flat note
		Pattern sampleRegex1 = Pattern.compile(
				"^([A-G])" + "(#/{1})" + "([A-G]{1})" + "(b){1}");

		// See if the note matches either a regular note or sharp/flat note
		for (String line : notes) {
			Matcher m = sampleRegex.matcher(line);
			Matcher m1 = sampleRegex1.matcher(line);

			if (!m.matches() && !m1.matches()) {
				view.showMessageDialog("There is an invalid line in the file");
				continue; //skip invalid lines.
			}

			// Add valid lines to the list of valid notes
			parsedNotes.add(line);
		}

		// Convert this back into an array to pass back to the caller

		return parsedNotes.toArray(new
				String[parsedNotes.size()]);
	}

	/**
	 * This creates a new keyboard, which is a PApplet, and starts it.
	 */
	private void initKeyboard() {
		keyboard = new VirtualKeyboard(model);
		keyboard.init();
	}

	/**
	 * This method sets the enables and disables the playback buttons, to
	 * ensure these buttons behave as expected.
	 */
	private void setPlayBackButtons() {
		boolean playBackStarted = model.playBackStarted();

		if (playBackStarted) {
			view.getStartMidiPlaybackButton().setEnabled(false);
			view.getStopMidiPlaybackButton().setEnabled(true);
			view.getSaveMidiFileButton().setEnabled(false);
			view.getPreviewPlaybackButton().setVisible(false);
		} else {
			view.getStartMidiPlaybackButton().setEnabled(true);
			view.getStopMidiPlaybackButton().setEnabled(false);
			view.getSaveMidiFileButton().setEnabled(true);
			view.getPreviewPlaybackButton().setVisible(true);
		}
	}

	/**
	 * This updates the model's internal state of starting playback.
	 * In turn, this enables the Stop Playback button, and disables
	 * the 'Save to Midi File' button
	 */
	private void startMidiPlayBack() {
		model.setPlayBackStarted(true);
		setPlayBackButtons();
	}

	/**
	 * This updates the model's internal state of starting playback.
	 * In turn, this disables the Stop Playback button, and enables
	 * the 'Save to Midi File' button, if the midi file is not null.
	 */
	private void stopMidiPlayBack() {
		model.setPlayBackStarted(false);
		setPlayBackButtons();
	}

	/**
	 * This method adds a Processing Block State to a data structure which
	 * stores this state, for future 'undo' operations
	 * @param processingBlockListState The state of the processing block
	 *                                 chain at an instant in time
	 */
	private void addToHistory(LinkedList<ProcessingBlock> 
			processingBlockListState) {
		
		LinkedList<ProcessingBlock> deepCopy =
				new LinkedList<>(processingBlockListState);
		
		model.addToHistory(deepCopy);
	}
	
	private boolean checkValidMidiFile() {
		
		String source = view.getMidiSources().getSelectedItem().toString();

		// Check if no midi file (or a valid midi file) was selected
		if (source.contains("MIDI file")) {
			try {
				Sequencer sequencer = MidiSystem.getSequencer();
				sequencer.setSequence(MidiSystem.getSequence(
						model.getMidiSourceFile()));
			} catch (Exception e) {
				view.showMessageDialog("Select a valid MIDI File or change"
						+ " MIDI Source");
				return false;
			}
		}

		return true;
	}

	/**
	 * This method sets up the midi source.
	 */
	private void setUpMidiSource() {	
		String source = view.getMidiSources().getSelectedItem().toString();
		try {
			// See if the source is a virtual keyboard. Set it up on GUI if so.
			if (source.contains("Virtual Keyboard")) {
				view.getKeyboardPanel().add(keyboard);
				keyboard.setKeyboardNotes();
				model.setVirtualKeyboard(keyboard);
			} else {
				view.getKeyboardPanel().remove(keyboard);
			}
			model.setMidiSource(source);
		} catch (Exception e) {
			view.showMessageDialog("Error connecting to MIDI Input");
			e.printStackTrace();
		}

	}

}
