package processingblocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import midiblocks.Observer;
import scales.Note;

/**
 * This class represents the Arpeggiator Processing Block
 * @author Lisa Liu-Thorrold
 *
 */
public class Arpeggiator implements ProcessingBlock {

	// The mode of the arpeggiator
	private String type;
	
	// The observers of this processing block
	private final ArrayList<Observer> observers = new ArrayList<>();
	
	// Store the details of last arpeggiated note so we know which is the 
	// next note to arpeggiate
	private int indexOfLastArpeggiatedNote = -1;
	private Note lastArpeggiatedNote;
	
	// To store the notes that are currently on 
	private final LinkedList<Note> notesCurrentlyOn;

	// Whether we are currently ascending if the arpeggiator mode is ping pong
	private boolean pingPongAscending;
	
	// Available notes for the arpeggiator to use
	private Note[] availableNotes;
	
	// Constants for the arpeggiator mode
	private final static String DESCENDING = "Descending Scale";
	private final static String ASCENDING = "Ascending Scale";
	private final static String PING_PONG = "Ping pong";
	private final static String RANDOM = "Random";

	/**
	 * This class represents the Arpeggiator processing block. It takes all of 
	 * the currently 'on' notes and outputs a sequence of short notes made up
	 * of these notes when an 'on' signal is received by the processing block
	 * controller. The output is in one of 4 definable patterns. If an 
	 * additional note is turned on, or any of the notes turned off, the 
	 * sequence continues using the set of new notes. The definable patterns 
	 * include: 
	 * 1. Ascending - Output notes should be the currently pressed notes in
	 * 				  ascending order. When the highest note is reached the 
	 * 				  next note should wrap back to the lowest note. The order
	 * 			      in which the notes were pressed is irrelevant
	 * 2. Descending - Output notes should be the currently pressed notes in
	 * 				   descending order, when the lowest note in the scale is
	 * 				   reached the next output should wrap to the highest note 
	 * 				   in the scale. The order in which notes were pressed is
	 *   			   irrelevant.
	 * 3. Ping pong - In this mode notes ascend the set of notes in the same 
	 * 				  way as ascending or descending mode. This mode starts
	 * 				  ascending, when the highest note is reached, the output
	 * 				  switches to direction and descends to the lowest note.
	 * 				  This up and down pattern is then repeated. The order in 
	 * 				  which the notes were pressed is irrelevant.
	 * 4. Random -   Notes will be output in a random pattern. The next note to
	 * 				 be output is chosen at random from the set of currently 
	 *  			 'on' note - unless there is only one note on. The order in
	 *  			 which the notes were pressed is irrelevant.
	 * @param type - The type of arpeggiator from one of the types described
	 *               above
	 */
	public Arpeggiator(String type) {
		this.type = type;
		notesCurrentlyOn = new LinkedList<>();
		// ping pong mode starts with ascending
		pingPongAscending = true;
	}
	
	/**
	 * This method adds and removes notes that pass into this block
	 * If the note is a note on, it adds it to the linked list
	 * If the note is a note off, it removes it from the linked list
	 * @param note - The note
	 * @param noteOn - Whether the note is on or off
	 * @param availableNotes - Not used, just for passing onwards to the 
	 * 						   processing blocks that need this array.
	 */
	@Override
	public void update(Note note, Boolean noteOn, Note[] availableNotes) {
		if (this.availableNotes == null) {
			this.availableNotes = availableNotes;
		}
		
		// note on message received
		if (noteOn) {
			if(!notesCurrentlyOn.contains(note)) {
				insertAscendingOrder(note);
			}
			
		//note off message received
		} else {

			notesCurrentlyOn.remove(note);
			
			// Turn of the last arpeggiated note 
			if (notesCurrentlyOn.size() == 0) {
				indexOfLastArpeggiatedNote = -1;
				notifyObservers(note, false, availableNotes);
			}
		}
	}
	
	/**
	 * This method inserts the notes via insertion sort, the most logical way
	 * to store all the notes that are currently on.
	 * @param note - The new on note to add into the arpeggiating sequence.
	 */
	private void insertAscendingOrder(Note note) {
		 if (notesCurrentlyOn.size() == 0) {
			 notesCurrentlyOn.add(note);
			 
			 // the new note has the lowest key, add to the beginning
	        } else if (notesCurrentlyOn.get(0).getKeyNumber() > 
	        		note.getKeyNumber()) {
	        	notesCurrentlyOn.add(0, note);

	        	// the new note has the highest key
	        } else if (notesCurrentlyOn.get(notesCurrentlyOn.size() - 1).
	        		getKeyNumber() < note.getKeyNumber()) {
	        	notesCurrentlyOn.add(notesCurrentlyOn.size(), note);
	        	
	        	// search for the correct position to add the note so that the
	        	// ascending order of notes being stored is preserved.
	        } else {
	            int i = 0;
	            while (notesCurrentlyOn.get(i).getKeyNumber() < 
	            		note.getKeyNumber()) {
	                i++;
	            }
	            notesCurrentlyOn.add(i, note);
	        }
	}

	
	/**
	 * This is the method that handles arpeggiating. It gets the next note
	 * to arpeggiate, turns the current note off, the new note on, and sends 
	 * this to the next processing block (or output) via notifyObservers
	 */
	public void arpeggiate() {
		// if list is empty, do nothing.
		if (notesCurrentlyOn.size() > 0) {
			System.out.println("Arpeggiate!");

			// Turn on the old arpeggiated note off
			if (indexOfLastArpeggiatedNote != -1) {

				if(notesCurrentlyOn.contains(lastArpeggiatedNote)) {

					// turn off the last arpeggiated note
					notifyObservers(lastArpeggiatedNote, false, availableNotes);
				}
			}

			// Get the next note to arpeggiate
			int indexOfNextNote = getNextNote();
			Note toArpeggiate = notesCurrentlyOn.get(indexOfNextNote);
			
			// Arpeggiate the new note
			notifyObservers(toArpeggiate, true, availableNotes);
			
			// store the newly arpeggiated note
			indexOfLastArpeggiatedNote = indexOfNextNote;
			lastArpeggiatedNote = toArpeggiate;
			
		}
	}
	
	/**
	 * Gets the next note to arpeggiate depending on the type of arpeggiator
	 * selected
	 * @return The next note to arpeggiate
	 */
	private int getNextNote() {
		
		int indexOfNextNote = -1;

		switch(type) {
		case ASCENDING:
			indexOfNextNote = getNextAscending();
			break;
		case DESCENDING:
			indexOfNextNote = getNextDescending();
			break;
		case PING_PONG:
			indexOfNextNote = getNextPingPong();
			break;
		case RANDOM:
			indexOfNextNote = getNextRandom();
			break;
		}
		
		return indexOfNextNote;
	}
	
	/**
	 * Returns the index of the next ascending note
	 * @return The next ascending note to arpeggiate
	 */
	private int getNextAscending() {

		// special case, there is only one note currently on
		if (notesCurrentlyOn.size() == 1 ) {
			return 0;
		}

		// Return the starting (lowest note) if the index of the last 
		// arpeggiated note does not exist, or we hit the highest note
		if (indexOfLastArpeggiatedNote == -1 ||
				indexOfLastArpeggiatedNote >= notesCurrentlyOn.size()-1) {
			return 0;
			
			// return the next note in the sequence. use math.min to prevent
			// index out of bounds exceptions if notes have been removed since
			// the last time an arpeggiation was made
		} else {
			return Math.min(indexOfLastArpeggiatedNote + 1, 
					notesCurrentlyOn.size()-1);
		}
	}
	
	/**
	 * Returns the index of the next descending note
	 * @return The next descending note to arpeggiate
	 */
	private int getNextDescending() {

		// special case, there is only one note in the sequence
		if (notesCurrentlyOn.size() == 1 ) {
			return 0;
		}
		
		// Return the end (highest) note if the index of the large arpeggiated
		// note does not exist, or we hit the lowest note
		if (indexOfLastArpeggiatedNote == -1 ||
				indexOfLastArpeggiatedNote <= 0) {
			return notesCurrentlyOn.size()-1;
			
		} else {

			// prevent index out of bounds exception if user has turned off
			// notes after the last arpeggiation was made
			if (indexOfLastArpeggiatedNote >= notesCurrentlyOn.size()) {
				indexOfLastArpeggiatedNote = notesCurrentlyOn.size() - 1;
			}

			// return the next lower note in the sequence
			return Math.max(0, indexOfLastArpeggiatedNote - 1);
		}
		
	}
	
	/**
	 * Returns the index of the next note in the ping pong sequence
	 * @return The next note to arpeggiate
	 */
	private int getNextPingPong() {
		
		// special case, there is only one note
		if (notesCurrentlyOn.size() == 1 ) {
			return 0;
		}

		// a ping pong sequence has already started
		if (indexOfLastArpeggiatedNote != -1) {
			
			// in ascending mode
			if (pingPongAscending) {
				
				// we hit the top note
				if (indexOfLastArpeggiatedNote >= notesCurrentlyOn.size() -1) {
					
					//stop ascending
					pingPongAscending = false;
					
					// start descending
					return Math.min(indexOfLastArpeggiatedNote - 1, 
							notesCurrentlyOn.size() - 1 );
				} else {
					
					//ascend
					return indexOfLastArpeggiatedNote + 1 ;

				}	
				
			// in descending mode
			} else {
				
				// hit the bottom note
				if (indexOfLastArpeggiatedNote <= 0) {
					
					// stop descending
					pingPongAscending = true;
					
					// start ascending
					return Math.min(1, notesCurrentlyOn.size() - 1);
					
				} else {
					
					// descend
					return Math.max(0, indexOfLastArpeggiatedNote - 1);
					
				}
				
			}
		}
		
		// Reach here because index of last arpeggiated note is -1
		// so start ascending.
		pingPongAscending = true;
		return 0;
	}
	
	/**
	 * Returns the index of the next note to arpeggiated in a random sequence
	 * @return The next note to arpeggiate
	 */
	private int getNextRandom() {
		// special case where there is only one note
		if (notesCurrentlyOn.size() == 1 ) {
			return 0;
		}

		// generate a number in the range of the size of available notes
		int randomIndex = 
				ThreadLocalRandom.current().nextInt(0, notesCurrentlyOn.size());

		// it can't be the same as the currently on note (unless there is only
		// one note
		while (randomIndex == notesCurrentlyOn.indexOf(lastArpeggiatedNote)) {
			randomIndex = 
					ThreadLocalRandom.current().nextInt(0, notesCurrentlyOn.size());
		}
	
		return randomIndex;
	}
	
	
	/*************************************************
	 *  Getter/setter methods
	 *************************************************/
	
	@Override
	public String toString() {
		return "Arpeggiator: " + type;
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public String getName() {
		return "Arpeggiator";
	}
	
	@Override
	public String getParameters() {
		return type+",NoParams";
	}
	
	public void setArpeggiatorType(String type) {
		this.type = type;
	}
	
	public void clearNotesCurrentlyOn() {
		notesCurrentlyOn.clear();
	}
	
	@Override
	public void setAvailableNotes(Note[] availableNotes) {
		this.availableNotes = availableNotes;	
	}
	
	public LinkedList<Note> getNotesCurrentlyOn() {
		return new LinkedList<>(notesCurrentlyOn);
	}
	
	public Note getLastArpeggiatedNote() {
		return lastArpeggiatedNote;
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
