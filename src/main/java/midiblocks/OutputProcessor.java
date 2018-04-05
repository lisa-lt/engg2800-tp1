package midiblocks;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jfugue.pattern.Pattern;

import scales.Note;

/**
 * @author Lisa Liu-Thorrold
 */
public class OutputProcessor implements Observer {

	private final MidiModel model;

	// Whether output is to a MIDI File
	private final boolean midiFileSelected;
	// Whether output is to a USB device.
	private final boolean usbDeviceSelected;

	private String portName;

	// Wrapper to abstract serial communication with the usb device
	private Serial serial;

	// The data rate at which communication occurs with serial device
	private static final int DATA_RATE = 250000;

	// For threads to receive messages and send them to their output device
	private LinkedBlockingQueue<Message> fileMessageQueue;
	private LinkedBlockingQueue<Message> usbMessageQueue;

	// The current MIDI file/pattern that the user is outputting midi events to
	private Pattern midiOutputPattern;

	// To convert from 88-key to 127-key MIDI event notes.
	private static final int KEY_OFFSET = 8;

	// List of note on messages that have been sent to hardware (important to
	// keep track of these so they can be flushed when the user changes the
	// the configurations of the processing blocks
	//	private final ArrayList<Message> noteOnMessagesSent;

	private final ConcurrentLinkedQueue<Message> noteOnMessagesSent;

	/**
	 * This class observes the processing block processor, and outputs to
	 * the selected devices as chosen by the user.
	 * @param model - MIDIModel to retrieve information from
	 * @param MIDIFileSelected - Whether the user wants to output a midi file
	 * @param USBDeviceSelected - Whether user wants to output to a usb device
	 */
	public OutputProcessor(MidiModel model, 
			Boolean MIDIFileSelected, Boolean USBDeviceSelected) {
		this.model = model;
		this.midiFileSelected = MIDIFileSelected;
		this.usbDeviceSelected = USBDeviceSelected;
		midiOutputPattern = new Pattern();
		noteOnMessagesSent = new ConcurrentLinkedQueue<>();

		// If only the usb output device is selected, then the port name for 
		// communication with this device is the first element in the array
		if ((this.usbDeviceSelected) && (!this.midiFileSelected)) {
			portName = model.getSelectedMidiOutput()[0];
		}

		// If both midi file & usb output device is selected, then the port name 
		// for communication with this device is the second element in the array
		if ((this.usbDeviceSelected) && (this.midiFileSelected)) {
			portName = model.getSelectedMidiOutput()[1];
		}

		// If usb output is selected, then create a new thread to listen 
		// for messages received by the output processor, and send them 
		// serially to the avr microcontroller
		if (this.usbDeviceSelected) {
			// For the thread to listen to, and extract messages, when they are
			// placed in the queue by the output processor
			usbMessageQueue = new LinkedBlockingQueue<>();

			Thread usbOutputThread = new Thread(new USBOutput(usbMessageQueue));
			usbOutputThread.start();
		}

		// If the MIDIfile output is selected, then create a new thread to 
		// listen for messages received by the output processor, and write
		// them to the midi file
		if (this.midiFileSelected) {
			// For the thread to listen to, and extract messages, when they are
			// placed in the queue by the output processor
			fileMessageQueue = new LinkedBlockingQueue<>();

			Thread midiFileOutputThread = new Thread(new 
					MIDIFileOutput(fileMessageQueue));
			midiFileOutputThread.start();
		}
	}

	/**
	 *	This method adds the messages to the queues
	 *	that the threads listen to and process.
	 */
	@Override
	public void update(Note note, Boolean noteOn) {
		checkNoteOnMessage(new Message(note, noteOn));
	}

	/**
	 * This method checks to see whether the message received is note on.
	 * If so, we check whether we have already previous sent a note on message.
	 * If so, we do nothing, otherwise we send the message on to the output
	 * devices.
	 * 
	 * If the message received is note off, then we send to the output, and 
	 * drop it from the note on messages sent list.
	 * 
	 * @param newMessage - The message to check.
	 */
	private void checkNoteOnMessage(Message newMessage) {
		// note has been sent from a note on message
		if (containsNoteOnMessage(newMessage)) {

			// message is a note off message
			if (!newMessage.getNoteOn()) {
				sendMessage(newMessage);
				removeMessage(newMessage);
			}
			//  don't send if newMessage noteOn == true (otherwise sending 2
			// note on messages)
		} else {
			sendMessage(newMessage);
			noteOnMessagesSent.add(newMessage);
		}

	}

	/**
	 * This method sends the messages to the hardware
	 * @param message - The message to send
	 */
	private void sendMessage(Message message) {
		
		System.out.println("Message sending: " + message.getNote() + " " + 
				message.getNoteOn());	
		
		try {
			if (usbMessageQueue != null) {
				usbMessageQueue.put(message);
			}

			if (fileMessageQueue != null) {
				fileMessageQueue.put(message);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * This method removes messages from noteOnMessagesSent. This occurs when
	 * a note off message is received. (To ensure note on messages are not
	 * sent twice to output)
	 * @param newMessage - The message to remove
	 */
	private void removeMessage(Message newMessage) {			
		Iterator<Message> iterator = noteOnMessagesSent.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	/**
	 * This method returns whether a message has already been sent to the output
	 * devices, but not yet cancelled (ie. note receiving corresponding note 
	 * off messages.
	 * @param newMessage - The message to check against noteOnMessagesSent
	 * @return Whether the new message's note is in noteOnMessagesSent
	 */
	private boolean containsNoteOnMessage(Message newMessage) {

		for (Message message : noteOnMessagesSent) {
			if (newMessage.getNote().getKeyNumber() ==
					message.getNote().getKeyNumber()) {
				return true;
			}
		}

		return false;
	}

	/*************************************************
	 *  Getter/setter methods
	 *************************************************/

	public Pattern getPattern() {
		return midiOutputPattern;
	}

	public void refreshPattern() {
		midiOutputPattern = new Pattern();
		midiOutputPattern.setTempo(model.getTempo());

	}


	/*************************************************
	 *  Helper methods
	 *************************************************/

	/**
	 * Establishes a new serial connection using, and stores the new Serial
	 * connection object on the instance.
	 *
	 * @return true if connection is successful, and false otherwise
	 */
	private boolean connect() {
		this.serial = new Serial();
		this.serial.connect(this.portName, DATA_RATE);

		return this.serial.isConnected();
	}

	/**
	 * This method sends the first 8 notes of the configured
	 * scale to the hardware, when either the lowest root note
	 * has changed by shifting the virtual keyboard, or a new
	 * scale has been selected by the user. Only sends when the
	 * serial is connected.
	 * 
	 * @param notesForConfiguredScale - The first 8 notes of the configured scale
	 */
	public void sendHardwareConfigurations(ArrayList<Integer> 
	notesForConfiguredScale) throws IOException {

		if (serial == null) return;

		if (serial.isConnected()) {
			// send the token for configuration
			serial.sendMessage('^');

			for (int noteNumber : notesForConfiguredScale) {
				serial.sendMessage(noteNumber);
			}

		}
	}

	/**
	 * Closes the serial connection.
	 */
	public void closeConnection() {
		if (serial == null) return;
		serial.close();
	}

	/**
	 * This method flushes out note off messages to all the note's that are
	 * currently note on. This is to prevent indefinite note on if there
	 * is a shift in the processing chain. 
	 */
	public void flushMessages() {

		System.out.println("Flush");

		Iterator<Message> iterator = noteOnMessagesSent.iterator();

		while (iterator.hasNext()) {
			Message message= iterator.next();
			update(message.getNote(), false);
		}

		noteOnMessagesSent.clear();
	}


	/**
	 * Wrapper class to send a message to the relevant output device.
	 * @author Lisa
	 *
	 */
	public class Message {
		private final Note note;
		private final Boolean noteOn;

		public Message(Note note, boolean noteOn) {
			this.note = note;
			this.noteOn = noteOn;
		}

		public String toString() {
			return note.toString() + " " + noteOn;
		}

		public Note getNote() {
			return note;
		}

		public Boolean getNoteOn() {
			return noteOn;
		}
	}

	/**
	 * This inner class is a new thread that is made to handle
	 * MIDI messages received by the Output processor
	 * @author Lisa
	 *
	 */
	private class USBOutput implements Runnable {
		private final LinkedBlockingQueue<Message> usbMessageQueue;

		USBOutput(LinkedBlockingQueue<Message> USBMessageQueue) {
			this.usbMessageQueue = USBMessageQueue;
		}

		/**
		 * This method continually takes messages placed into the queue
		 * from the classes it is observering, and processes the message 
		 * to send to the usb device
		 */
		public void run() {
			try {
				connect();
				while(usbDeviceSelected) {
					// the thread waits if the queue is empty
					Message message = usbMessageQueue.take();
					processMessage(message);

					// for debugging with luca.
					//serial.getMessage();

				}
			} catch(Exception e) {
				serial.close();
				// finished processing, terminate the thread.
			}
		}

		/**
		 * Sends the message to the serial device
		 * For a Note on message:
		 * 		@\n
		 * 		Note Number\n
		 * For a Note off message:
		 * 		#\n
		 * 		Note Number\n
		 * @param message - The encapsulated message to send to the output
		 * @throws IOException 
		 */
		private void processMessage(Message message) throws IOException {

			if(message.getNoteOn()) {
				serial.sendMessage('[');
			} else {
				serial.sendMessage(']');
			}

			Note note = message.getNote();
			int noteNumber = note.getKeyNumber();

			serial.sendMessage(noteNumber);

		}
	}


	/**
	 * Inner class which is a thread than runs to handle 
	 * MIDI File output when midi messages are received
	 * by the OutputProcessor, and constructs a MIDI file
	 * @author Lisa
	 *
	 */
	private class MIDIFileOutput implements Runnable {

		private final LinkedBlockingQueue<Message> fileMessageQueue;

		MIDIFileOutput(LinkedBlockingQueue<Message> fileMessageQueue) {
			this.fileMessageQueue= fileMessageQueue;
		}

		/**
		 * This method continually takes messages placed into the queue
		 * from the classes it is observering, and constructs a midi file
		 * from the messages received.
		 */
		public void run() {
			try {
				while(true) {
					// the thread waits, if the queue is empty
					Message message = fileMessageQueue.take();
					process(message);
				}
			} catch(Exception e) {
				// finished processing, terminate the thread.
			}
		}

		/**
		 * This method builds the midi file for user output
		 * @param message - the message received (containing note, noteOn)
		 */
		private void process(Message message) {
			if (model.playBackStarted()) {
				if (message.getNoteOn()) {
					// then write to midi file.
					Note note = message.getNote();
					// convert back to the note number
					// as the note's toString format is not understood
					// by jfugue (eg. understands f#5, not f5#).
					int noteNumber = note.getKeyNumber() + KEY_OFFSET;
					Pattern toAdd = new Pattern(Integer.toString(noteNumber));
					midiOutputPattern.add(toAdd);
				}
			}
		}
	}
	
	/**
	 * For testing purposes
	 * @return List of note on messages that have been sent by the software
	 * 		   to midi output (without corresponding note off messages sent yet)
	 */
	public ConcurrentLinkedQueue<Message> getNoteOnMessagesSent() {
		return new ConcurrentLinkedQueue<Message>(noteOnMessagesSent);
	}


	@Override
	public void update(Note note, Boolean noteOn, Note[] availableNotes) { }

}
