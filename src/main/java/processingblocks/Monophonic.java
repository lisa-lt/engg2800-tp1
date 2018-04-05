package processingblocks;

import java.util.ArrayList;
import java.util.Iterator;

import midiblocks.Observer;
import scales.Note;

/**
 * This class represents the Monophonic Processing Block. The monophonic block
 * allows only one note to be on at once. If a new "note on" signal passes
 * through this block, then any other notes should be turned off before the 
 * next note begins. As such this block needs to keep track of the last note
 * to pass through. When new input arrives one of several things may happen. If
 * the input is an off signal for that note, it simply passes through. If the 
 * note is an 'on' signal for a new note the block outputs an 'off' for the old
 * note and then an 'on' for the new note. If the input is an 'off signal' for
 * any other note, it is simply ignored.
 * @author Lisa Liu-Thorrold
 *
 */
public class Monophonic implements ProcessingBlock {
	
	private Note currentNoteOn;
	
	private final ArrayList<Observer> observers = new ArrayList<>();

	/**
	 * This method is invoked when we receive a new note through the processing
	 * block. This method turns the current note off (if it's on), and turns 
	 * the new note on.
	 */
	@Override
	public void update(Note note, Boolean noteOn, Note[] availableNotes) {
		// If the note is a new note on message, and there is a note that is
		// currently on
		if (currentNoteOn != null && noteOn) {
			// Turn the current note off				
			notifyObservers(currentNoteOn, false, availableNotes);

			// Assign the new note to be the current note
			currentNoteOn = note;

			// Turn on the new note
			notifyObservers(currentNoteOn, true, availableNotes);

			// The note is the first note on message
		} else if (currentNoteOn == null && noteOn) {
			currentNoteOn = note;
			notifyObservers(currentNoteOn, true, availableNotes);
			// note off message received
		} else if (currentNoteOn != null) {
			if (note.equals(currentNoteOn)) {
				//this is the last note, turn it off!
				notifyObservers(currentNoteOn, false, availableNotes);
				currentNoteOn = null;
			}
		}
	}
	
	/*************************************************
	 *  Getter/Setter methods
	 *************************************************/
	@Override
	public String toString() {
		return "Monophonic";
	}
	
	@Override
	public String getName() {
		return "Monophonic";
	}
	
	@Override
	public String getParameters() {
		return "NoParams,NoParams";
	}
	
	@Override
	public void setAvailableNotes(Note[] availableNotes) {
		//this.availableNotes = availableNotes;	
	}
	
	// Getter method for testing
	public Note getCurrentNoteOn() {
		return currentNoteOn;
	}
	
	/*************************************************
	 *  Observer methods
	 *************************************************/

	@Override
	public void update(Note note, Boolean noteOn) { }
	
	@Override
	public void notifyObservers(Note note, Boolean noteOn) { }

	@Override
	public void registerObserver(Observer observer) {
		observers.add(observer);	
	}

	@Override
	public void removeObserver(Observer observer) {
		observers.remove(observer);	
	}
	
	private void notifyObservers(Note note, Boolean noteOn, Note[] availableNotes) {
		for (Observer observer : observers) {
			observer.update(note, noteOn, availableNotes);
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
