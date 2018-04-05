package midiblocks;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import scales.Note;
import scales.NoteDictionary;

/**
 * This class represents the input processor that is responsible for
 * receiving inputs from a MIDI driver source. When a MIDI event is received,
 * it is checked to see whether it is within range C1-B7 before being passed
 * on the processing block controller (for processing through the processing
 * blocks).
 * @author Lisa Liu-Thorrold
 *
 */
public class DriverInputProcessor implements InputProcessor,
											 Subject {

	// Dictionary of notes for reference
	private final NoteDictionary noteDictionary;
	
	// Observers that are listening to events from this class/object 
	private final ArrayList<Observer> observers = new ArrayList<>();

	// To convert 127 keys to 88 keys
	private static final int KEY_OFFSET = 8;
	
	// Constants that represent MIDI messages that are received by our MIDI
	// input receiver
	private static final int NOTE_ON = 0x90;
	private static final int NOTE_OFF = 0x80;

	/**
	 * This class connects the specified MIDI driver level input device
	 * by the user and listens for MIDI events and sends them on to the
	 * Processing Block Controller for further processing.
	 * @param deviceName - The name of the MIDI driver level input device.
	 */
	public DriverInputProcessor(String deviceName) {
		noteDictionary = new NoteDictionary();
		//try and establish a connection with the device
		connect(deviceName);	
	}

	/**
	 * This method attempts to establish a connection with the
	 * MIDI Device that is connected to the input driver
	 * @param deviceName - The name of the MIDI Device/ port the device is 
	 * 					   connected to.
	 */
	private void connect(String deviceName) {

		MidiDevice device;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

		for (MidiDevice.Info info : infos) {
			try {
				device = MidiSystem.getMidiDevice(info);

				// Test to see if the midi device can be a midi source
				// The value is be -1 if device can have unlimited transmitters
				// or >  1, if there are a fixed number of transmitters.
				if (device.getMaxTransmitters() != 0) {

					String name = device.getDeviceInfo().toString();

					if (name.equals(deviceName)) {
						// See if we can open and close the device without error
						device.open();

						Transmitter transmitter = device.getTransmitter();
						transmitter.setReceiver(new MidiInputReceiver());

						break;
					}

				}

			} catch (MidiUnavailableException e) {
				// do nothing
				return;
			}

		}
	}

	/**
	 * This class is an object that receives MIDI messages, processes the MIDI
	 * Note On and Note Off Messages and sends them to the controller.
	 * @author Lisa
	 *
	 */
	private class MidiInputReceiver implements Receiver {

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
		 * @param shortMessage - The MIDI message to process
		 */
		private void processMessage(ShortMessage shortMessage) {
			int key = shortMessage.getData1();
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
	
	/*************************************************
	 *  Observer methods
	 *************************************************/

	/** 
	 * This method sends MIDI events received to the Processor Controller
	 * @param note - The note to send
	 * @param noteOn - Whether the note is on or off
	 */
	@Override
	public void sendToProcessorController(Note note, Boolean noteOn) {
		notifyObservers(note, noteOn);	
	}

	@Override
	public void setRunning(Boolean running) {}

	/**
	 * This method registers observers (the Processor controller) so 
	 * that it may listen to events.
	 * @param observer - Observer to add to our list to listen for events
	 */
	@Override
	public void registerObserver(Observer observer) {
		observers.add(observer);
		
	}

	/**
	 * This method notifies the Processor controller, when 
	 * MIDI events have been received.
	 * @param note - The note to send
	 * @param noteOn - Whether note is on or off
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
	 * input from another MIDI source (MIDI File, or
	 * virtual keyboard)
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
	 * re-register as an observer.
	 */
	@Override
	public void removeAllObservers() {
		Iterator<Observer> iterator = observers.iterator();

		while (iterator.hasNext()) {
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
