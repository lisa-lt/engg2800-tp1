package midiblocks;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import scales.Note;
import scales.NoteDictionary;

/**
 * This class represents the input processor that processes inputs from
 * MIDI files.
 * @author Lisa Liu-Thorrold
 *
 */
public class FileInputProcessor extends EventEmitter implements InputProcessor,
		Subject {

	// To reference our midi notes
	private final NoteDictionary noteDictionary;
	
	// The MIDI file to process
	private final File midiFile;

	// To convert 127 keys to 88 keys
	private static final int KEY_OFFSET = 8;
	
	private static final int NOTE_ON = 0x90;
	private static final int NOTE_OFF = 0x80;
	
	private Sequencer sequencer;

	/** Running indicates whether the currently selected
	 * source is the MIDI file. The user may change the MIDI
	 * source while this class is in the middle of listening
	 * to events on the midi file. */
	private volatile Boolean running;
	
	/* Observers that are listening to events from this class/object */
	private final ArrayList<Observer> observers = new ArrayList<>();

	/**
	 * This class processes the MIDI file input specified by the user by the
	 * users and listens for MIDI events and sends them on to the Processing
	 * Block Controller for further processing
	 * @param midiFile - The file to process and listen to for MIDI event
	 */
	public FileInputProcessor(File midiFile) {
		noteDictionary = new NoteDictionary();
		this.midiFile = midiFile;
	}

	/**
	 * Set the MidiInputReceiver to the MIDI File in order to process
	 * MIDI event in real time. Sequencer must be used in order to process the
	 * MIDI events from the file in real time.
	 */
	public void process() {
		try {
			// use this if we want to listen to the sound while 
			// processing midi events from the midi file
			// sequencer =  MidiSystem.getSequencer();
			sequencer = MidiSystem.getSequencer(false);
			Sequence sequence = MidiSystem.getSequence(midiFile);
			sequencer.setSequence(sequence);
			sequencer.open();
			sequencer.getTransmitter().setReceiver(new MidiInputReceiver());
			running = true;
			newThreadStartProcessing();
		} catch(Exception e) {
			// do nothing
		}
	}
	
	/**
	 * Starts a new thread for processing the midi file
	 */
	public void newThreadStartProcessing() {
		(new Thread(this::startProcessing)).start();
	}

	/**
	 * This method processes events from the MIDI File.
	 * Constantly checks to see whether the sequencer
	 * has finished processing the midi file, and then
	 * stops when it has detected that.
	 */
	private void startProcessing() {
		sequencer.start();
		while(running) {
			if(sequencer.isRunning()) {
				try {
					Thread.sleep(200);
				} catch(InterruptedException ignore) {
					break;
				}
			} else {
				break;
			}
		}

		sequencer.stop();
	}


	/**
	 * This class is an object that receives MIDI messages, processes the MIDI
	 * Note On and Note Off Messages and sends them to the controller.
	 * @author Lisa
	 *
	 */
	public class MidiInputReceiver implements Receiver {

		/**
		 * This method is invoked when a MIDI message is received.
		 * This method only listens for Note On and Note Off messages.
		 */
		@Override
		public void send(MidiMessage midiMessage, long timeStamp) {

			if (midiMessage instanceof ShortMessage) {
				ShortMessage shortMessage = (ShortMessage) midiMessage;

				if((shortMessage.getCommand() == NOTE_ON) ||
						(shortMessage.getCommand() == NOTE_OFF)) {
					processMessage(shortMessage);
				}
			}
		}

		/**
		 * This method processes the midi message received, and sends the
		 * message on to the processor controller.
		 * @param shortMessage - The MIDI message received
		 */
		private void processMessage(ShortMessage shortMessage) {
			// get the midi key
			int key = shortMessage.getData1();
			
			// convert the key number (from 127 to 88)
			int realKey = key - KEY_OFFSET;
			
			Note note;
			Boolean isPlayable;

			if ((realKey >= 1) && (realKey <= 88)) {
				note = noteDictionary.getNote(realKey);
				isPlayable = note.isPlayable();

				if (isPlayable && shortMessage.getCommand() == NOTE_ON) {
					sendToProcessorController(note, true);
				} else if (isPlayable && shortMessage.getCommand() == NOTE_OFF) {
					sendToProcessorController(note, false);
				}
			}
		}

		public void close() {}

	}

	/** 
	 * This method sends MIDI events received to the Processor Controller
	 * @param note - The note to send
	 * @param noteOn - Whether the midi message is a note on message
	 */
	public void sendToProcessorController(Note note, Boolean noteOn) {
		if (running) {
			notifyObservers(note, noteOn);
		}
	}

	/**
	 * Set whether this class continues to process
	 * the MIDI file. Release system resources if not running
	 * @param running - Whether to continue processing midi file
	 */
	@Override
	public void setRunning(Boolean running) { 
		this.running = running;
	}
	
	/**
	 * This method pauses the Midi file processing when the user has clicked 
	 * stop midi playback.
	 */
	public void pauseMidiProcessing() {
		try {
			sequencer.stop();
		} catch (IllegalStateException i) {
			// do nothing
		}
	}

	/**
	 * This method registers observers (the Processor controller) so 
	 * that it may listen to events.
	 * @param observer - The observer to register so that it can listen
	 * 					 for events from this class
	 */
	@Override
	public void registerObserver(Observer observer) {
		observers.add(observer);
	}

	/**
	 * This method notifies the Processor controller, when 
	 * MIDI events have been received.
	 * @param note - The note received
	 * @param noteOn - Whether the note is on or off
	 */
	@Override
	public void notifyObservers(Note note, Boolean noteOn) {
		for (Observer observer : observers) {
			observer.update(note, noteOn);
		}
	}

	/**
	 * This method removes any observers that may be
	 * listening to events originating from this object.
	 * This is because the observer may want to observe
	 * input from another MIDI source (MIDI Driver source
	 * or virtual keyboard)
	 * @param observer - The observer to remove from listening to 
	 * 					 events from this class.
	 */
	@Override
	public void removeObserver(Observer observer) {
		observers.remove(observer);	
	}

	/**
	 * This method removes all current observers that may
	 * be listening to events originating from this object/
	 * class. This is because we don't want observers 
	 * receiving duplicate messages, if they decide to 
	 * re-register as an observer. Furthermore, we want to 
	 * stop our listeners from continously processing previous
	 * input (processing block controller)
	 */
	@Override
	public void removeAllObservers() {
		
		Iterator<Observer> iterator = observers.iterator();

		while (iterator.hasNext()){
			Observer observer = iterator.next();
			
			// stop processing block controller from continuing
			// to process previous input (eg. gates/ arpeggiator)
			if (observer instanceof ProcessingBlockController) {
				((ProcessingBlockController) observer).changeMidiSource();
			}
			iterator.remove();
		}
	}
	
}
