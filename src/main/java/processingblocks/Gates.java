package processingblocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

import midiblocks.Observer;
import scales.Note;

/**
 * This class represents the Gates Processing Block. Functionality
 * is described in the constructor below.
 * @author Lisa Liu-Thorrold
 *
 */
public class Gates implements ProcessingBlock {
	
	private final ArrayList<Observer> observers = new ArrayList<>();
	
	// parameter specified by the user
	private double notesPerTick;
	
	// the mode of the gates
	private String mode;
	
	// notes to released each time the gate is opened
	private int notesReleasedPerTick;
	
	// to store the note messages that arrive into this processing block
	private final LinkedBlockingDeque<Note> gateQueue;
	
	// note on messages that have been outputted
	private final LinkedBlockingDeque<Note> noteOnOutputted;
	
	// note off messages to be send next time gate opens, so no notes are left
	// on indefinitely
	private final LinkedBlockingDeque<Note> sendWhenGateOpens;
	
	private Note[] availableNotes;
	
	// Constants for the gates mode
	private final static String QUEUE = "Queue";
	private final static String FIRST_HOLD = "First Hold";
	private final static String LAST_HOLD = "Last Hold";
	
	/**
	 * The gate block is responsible for ensuring the MIDI stream maintains a 
	 * constant tempo. The block can be set into one of three modes which 
	 * determines how new input is handled while the gate is closed. In every
	 * cycle of the global clock the gate should open to allow a configurable
	 * number of notes to pass through the gate. The three gate modes are:
	 * 1. Queue - Notes are queued within the block and passed in a first-in
	 * 			 - first out fashion
	 * 2. First Hold - Only the first note should be passed on the next tick,
	 * 			       all additional notes are ignored
	 * 3. Last Hold - Only the last (most recent) note should be passed on the
	 * 				  next tick, earlier notes are discarded
	 * @param mode - One of the modes defined above
	 * @param notesPerTick - The number of notes to pass through on each clock
	 * 						 tick. If number < 1, the gate should release a note
	 * 						 every (1/notes per tick) ticks, rounded up to the
	 * 						 next integer.
	 */
	public Gates(String mode, double notesPerTick) {		
  	    // constrain notes per tick to be a double with 2dp
  	    this.notesPerTick = Math.round(notesPerTick * 100.0) / 100.0;
		this.mode = mode;
		setNotesReleasedPerTick();
		gateQueue = new LinkedBlockingDeque<>();
		sendWhenGateOpens = new LinkedBlockingDeque<>();
		noteOnOutputted = new LinkedBlockingDeque<>();
	}
	

	/**
	 * Method is invoked when we receive a new note on or note off message. 
	 * If the message is a note of message, we add it to the queue.
	 */
	@Override
	public void update(Note note, Boolean noteOn, Note[] availableNotes) {
		
		if (this.availableNotes == null) {
			this.availableNotes = availableNotes;
		}
		
		if (noteOn) {
			gateQueue.add(note);
		} else {
			handleNoteOff(note);
		}
		
	}
	
	/**
	 * Handles a note off message. 
	 * - If the note is in the gate queue, it cancels and drops it from the queue
	 * - If the note is a previously outputted note, add it to the list to send
	 *   as an off message next time the gate opens. This ensures that the note
	 *   is not left on indefinitely
	 * @param note - The note to turn off
	 */
	private void handleNoteOff(Note note) {
		
		// If the note is in the gate queue, cancel and drop
		if (gateQueue.contains(note)) {
			gateQueue.remove(note);
		}
		
		// If the note has been sent by a previous gate
		if(noteOnOutputted.contains(note)) {
			
			// Add it to the send when gate opens list
			sendWhenGateOpens.add(note);
		}
	}
	
	/**
	 * This is the method that handles releasing the notes from the gate. It 
	 * gets the note or notes to release from the gate depending on the input 
	 * (via notifyObservers)
	 */
	public void release() {
		System.out.println("Release!");

		//if we have nothing to send to the output controller, do nothing.
		if (gateQueue.size() > 0 || sendWhenGateOpens.size() > 0) {
			
			//release any off messages currently in the sendWhenGateOpensQueue
			// these are all off notes that match with the on notes from the
			// previously released lot.
			for (Note n : sendWhenGateOpens) {
				notifyObservers(n, false, availableNotes);
				sendWhenGateOpens.remove(n);
			}
			
			switch(mode) {
			case QUEUE:
				releaseQueue();
				break;
			case FIRST_HOLD:
				releaseFirstHold();
				break;
			case LAST_HOLD:
				releaseLastHold();
				break;
			} 
			
		}
	}
	
	/**
	 * Releases the notes according the queue mode. (Notes are released in a
	 * first-in-first-out fashion
	 */
	private void releaseQueue() {
		
		// If the size of the queue is less the parameter supplied the user,
		// then clear the queue.
		int numIterations = Math.min(gateQueue.size(), notesReleasedPerTick);
		
		for (int i = 0; i < numIterations; i++) {
			
			// Get the note at the front of the queue
			Note toRelease = gateQueue.getFirst();
			
			// Send to output
			notifyObservers(toRelease, true, availableNotes);
			
			// Add it to the list of outputted notes
			noteOnOutputted.add(toRelease);
			
			// Remove it from the front of the queue
			gateQueue.removeFirst();
		}
	}
	
	/**
	 * Only the first note should be released (at the front of the queue)
	 */
	private void releaseFirstHold() {
		
		if (gateQueue.size() > 0) {
			Note toRelease = gateQueue.getFirst();
			
			// Send to output
			notifyObservers(toRelease, true, availableNotes);
			
			// Add it to the output list
			noteOnOutputted.add(toRelease);
			
			// Clear the queue (discard all other notes)
			gateQueue.clear();
		}
	}
	
	/**
	 * Only the last (most recent) note should be passed on the next tick.
	 */
	private void releaseLastHold() {
		if (gateQueue.size() > 0) {
			Note toRelease = gateQueue.getLast();

			// Send to output
			notifyObservers(toRelease, true, availableNotes);

			// Add it to the output list
			noteOnOutputted.add(toRelease);

			// Clear the queue (discard all the other notes)
			gateQueue.clear();	
		}
	}

	
	/**
	 * Clears the gate so that input from old midi source is handled.
	 */
	public void clearGateQueue() {
		// delete everything that is due to be sent next time the gate opens
		gateQueue.clear();
		
		// send everything that was supposed to be sent next time the gate opens
		for (Note n : sendWhenGateOpens) {
			notifyObservers(n, false, availableNotes);
			sendWhenGateOpens.remove(n);
		}
		
		// send the corresponding 'off notes' for the ones that have already 
		// been released.
		for (Note n : noteOnOutputted) {
			notifyObservers(n, false, availableNotes);
			noteOnOutputted.remove(n);
		}
		
		
	}
	
	
	
	/*************************************************
	 *  Private helper methods
	 *************************************************/
	
	/**
	 * Sets the notes to be released each time the gate is opened.
	 */
	private void setNotesReleasedPerTick() {
		// if notesPerTick < 1, notes released per tick = 1/notesPerTick, 
		// rounded up to the next integer
		if (notesPerTick < 1) {
			notesReleasedPerTick = (int) Math.ceil(1/notesPerTick);
		} else {
			// greater than one, round to the nearest integer	
			if (notesPerTick % 1 >= 0.5) {
				notesReleasedPerTick = (int) Math.ceil(notesPerTick);
			} else {
				notesReleasedPerTick = (int) Math.floor(notesPerTick);
			}
		}
	}
	
	/*************************************************
	 *  Getter/setter methods
	 *************************************************/

	@Override
	public String getName() {
		return "Gates";
	}

	@Override
	public String getParameters() {
		return mode + "," + Double.toString(notesPerTick); 
	}
	
	@Override
	public String toString() {
		return "Gates: " + mode + ", Notes released per tick: " + 
				Double.toString(notesPerTick);
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setGatesMode(String mode) {
		this.mode = mode;
	}
	
	public void setGatesNotesPerTick(double notesPerTick) {
  	    // constrain notes per tick to be a double with 2dp
  	    this.notesPerTick = Math.round(notesPerTick * 100.0) / 100.0;
  	    setNotesReleasedPerTick();
	}
	
	public double getNotesPerTick() {
		return notesPerTick;
	}
	
	@Override
	public void setAvailableNotes(Note[] availableNotes) {
		this.availableNotes = availableNotes;	
	}
	
	/*************************************************
	 *  Getter methods used for unit testing
	 *************************************************/
	
	public ArrayList<Note> getGateQueue() {
		return new ArrayList<>(gateQueue);
	}
	
	public ArrayList<Note> getNoteOnOutputted() {
		return new ArrayList<>(noteOnOutputted);
	}
	
	public ArrayList<Note> getSendWhenGateOpens() {
		return new ArrayList<>(sendWhenGateOpens);
	}
	
	
	/*************************************************
	 *  Observer methods
	 *************************************************/
	
	@Override
	public void update(Note note, Boolean noteOn) { }
	
	private void notifyObservers(Note note, Boolean noteOn, Note[] availableNotes) {
		for (Observer observer : observers) {
			observer.update(note, noteOn, availableNotes);
		}
	}
	

	@Override
	public void registerObserver(Observer observer) {
		observers.add(observer);	
	}

	@Override
	public void removeObserver(Observer observer) {
		observers.remove(observer);	
	}

	@Override
	public void notifyObservers(Note note, Boolean noteOn) {
		for (Observer ob : observers) {
			ob.update(note, noteOn);
		}
	}
	
	/**
	 * Stop the output processor from listening to events from this class
	 * (so that the output processor does not receive duplicate events)
	 */
	public void removeAllObservers() {
		Iterator<Observer> iterator = observers.iterator();

		while (iterator.hasNext()){
			iterator.next();
			iterator.remove();
		}
	}

}
