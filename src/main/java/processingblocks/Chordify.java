package processingblocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import midiblocks.Observer;
import scales.Note;

/**
 * This class represents the Chordify Processing Block. This processing block
 * takes every input note and outputs a chord based on that note. A chord is
 * defined as note (the note played) plus the notes 2, and 4 higher within the
 * globally set scale. If this note is above the highest note in the scale it 
 * should wrap to the next octave (or be ignored if this is outside the valid
 * range of notes). The chord should play as long as the note is on. The block
 * has no parameters. If a note is received by this block which is not in the 
 * currently selected scale, it should be silently ignored.
 * @author Lisa Liu-Thorrold
 *
 */
public class Chordify implements ProcessingBlock {
	
	private final ArrayList<Observer> observers = new ArrayList<>();
	private HashMap<Note, Boolean> notesSent = new HashMap<>();
	
	/**
	 * This method processes the input of notes, outputs a series
	 * of notes, defined at the input note, plus the notes 2, and
	 * 4 higher within the globally set scale
	 * @param noteOn - Whether the note is on or not
	 * @param firstNote - The note to chordify
	 * @param availableNotes - The available notes in the set scale
	 *
	 */
	@Override
	public void update(Note firstNote, Boolean noteOn, Note[] availableNotes) {
		
		notesSent.clear();

		// check if note is in the currently selected scale? if
		// so, get it's index.
		int inSelectedScale =
				isInSelectedScale(firstNote, availableNotes);

		// In the currently selected scale
		if (inSelectedScale != -1) {
			notifyObservers(firstNote, noteOn, availableNotes);
			notesSent.put(firstNote, noteOn);

			// get the second note higher, ignore if outside valid
			// range of notes
			if (inSelectedScale + 2 < availableNotes.length) {
				Note secondNote = availableNotes[inSelectedScale + 2];
				notifyObservers(secondNote, noteOn, availableNotes);
				notesSent.put(secondNote, noteOn);
			}

			// get the fourth note higher, ignore if outside valid
			// range of notes
			if (inSelectedScale + 4 < availableNotes.length) {
				Note fourthNote = availableNotes[inSelectedScale + 4];
				notifyObservers(fourthNote, noteOn, availableNotes);
				notesSent.put(fourthNote, noteOn);
			}
		}

	}

	/**
	 * This method checks to see whether the note is in the current selected
	 * scale
	 * @param note - The note to check
	 * @param availableNotes - The notes available in currently selected scale
	 * @return the position in the available notes is if the note is in the
	 * 		   currently selected scale, -1 other wise
	 */
	private int isInSelectedScale(Note note, Note[] availableNotes) {

		for (int i = 0; i < availableNotes.length; i++) {
			if (availableNotes[i].getKeyNumber() == note.getKeyNumber()) {
				//is in the selected scale
				return i;
			}
		}

		return -1;
	}

	/*************************************************
	 *  Getter/Setter methods
	 *************************************************/

	@Override
	public String toString() {
		return "Chordify";
	}

	@Override
	public String getName() {
		return "Chordify";
	}

	@Override
	public String getParameters() {
		return "NoParams,NoParams";
	}
	
	// For testing
	public HashMap<Note, Boolean> getNotesSent() {
		return new HashMap<>(notesSent);
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
	
	@Override
	public void setAvailableNotes(Note[] availableNotes) {	}
	
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




