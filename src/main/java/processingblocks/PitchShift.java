package processingblocks;

import java.util.ArrayList;
import java.util.Iterator;

import midiblocks.Observer;
import scales.Note;

/**
 * This class represents the PitchShift Processing Block. The pitch shift 
 * block shifts a MIDI note up or down by a certain number of notes. The block
 * also constrains all notes to the globally set scale. Note that in the case
 * of a 0 shift block (and the incoming note is not in the globally set scale),
 * then incoming notes are shifted to the closest note in the set scale. If 
 * there two equidistant notes, then the note is shifted to the lowest of the
 * two notes. If the note is inside the globally set scale, nothing happens.
 * If the shifted note is outside the range of playable notes, it should return
 * the highest/lowest playable note limited to the notes in the current scale.
 * @author Lisa Liu-Thorrold
 *
 */
public class PitchShift implements ProcessingBlock {

	// The pitch to shift by as defined by the user
	private int pitch;
	
	// Range of playable keys
	private final int LOWEST_PLAYABLE_KEY = 4;
	private final int HIGHEST_PLAYABLE_KEY = 87;
	
	private Note lastNoteSent;
	
	private final ArrayList<Observer> observers = new ArrayList<>();
	
	public PitchShift(int pitch) {
		this.pitch = pitch;
	}

	/**
	 * This method is invoked when we receive a note on message coming through
	 * this processing block. It shifts the notes by the specified pitch. If 
	 * the note, after shifting the pitch is not in the globally selected notes, 
	 * then we get the closest note. 
	 */
	@Override
	public void update(Note note, Boolean noteOn, Note[] availableNotes) {
			
		// Get the current note number
		int noteNumber = note.getKeyNumber();

		// Check if the note is the highest or lowest
		if (noteNumber < LOWEST_PLAYABLE_KEY) {
			noteNumber = availableNotes[0].getKeyNumber();
		} else if (noteNumber > HIGHEST_PLAYABLE_KEY) {
			noteNumber = 
					availableNotes[availableNotes.length - 1].getKeyNumber();
		} else {
			// Check if the current note is in the selected scale
			boolean inSelectedScale = 
					isInSelectedScale(note, availableNotes);

			if (!inSelectedScale) {
				// Get the closest note number
				noteNumber = getClosestNote(note, availableNotes).
						getKeyNumber();
			}

		}

		Note newNote;

		// Then shift the note. Get the position of the note number in the 
		// index.
		int positionInAvailableNotes = 
				getPositionOfNote(noteNumber, availableNotes);

		int newPositionInAvailableNotes = positionInAvailableNotes + pitch;

		// Shift the note according to the pitch
		if (newPositionInAvailableNotes < 0 ) {
			newNote = availableNotes[0];
		} else if (newPositionInAvailableNotes > availableNotes.length - 1) {
			newNote = availableNotes[availableNotes.length-1];
		} else {
			newNote = availableNotes[newPositionInAvailableNotes];
		}
		
		lastNoteSent = newNote;
		notifyObservers(newNote, noteOn, availableNotes);
		
	}

	
	/**
	 * This method returns the closest note. If the input note is higher than 
	 * the highest available note, it will return the highest available note. 
	 * If the input note is lower than the lowest available note, it will return
	 * the lowest available note. Otherwise, it will get the closest note to 
	 * the input note in the currently selected scale. If the current note is
	 * equidistant from two notes, it will return the lower of the two notes.
	 * @param note - The note to constrain to the selected scale
	 * @param availableNotes - The notes available to the user.
	 * @return the closest note to the given note.
	 */
	private Note getClosestNote(Note note, Note[] availableNotes) {
		
		Note closestNote = null;
		
		// Note is below the lowest available note
		if (note.getKeyNumber() < availableNotes[0].getKeyNumber()) {
			closestNote = availableNotes[0];
			return closestNote;
		}
		
		// Note is higher than the highest available note
		int lastNote= availableNotes.length-1;
		if (note.getKeyNumber() > 
				availableNotes[lastNote].getKeyNumber()) {
			closestNote = availableNotes[lastNote];
			return closestNote;
		}
		
		// If the note is not higher than the highest note, or smaller than the
		// smallest note, then find the closest note. If there are two closest 
		// notes, the note will be shifted to the lower of the two.
		int smallestDifference = 88; //some sentinel value
		for (Note current : availableNotes) {
			int tempDifference = Math.abs(note.getKeyNumber() -
					current.getKeyNumber());

			if (tempDifference < smallestDifference) {
				smallestDifference = tempDifference;
				closestNote = current;
			}
		}
		
		return closestNote;
	}
	
	/**
	 * This method checks to see whether the note is in the current selected
	 * scale
	 * @param note - The new shifted note
	 * @param availableNotes - The notes available in currently selected scale
	 * @return true if the new shifted note is in the currently selected scale
	 */
	private boolean isInSelectedScale(Note note, Note[] availableNotes) {

		for (Note availableNote : availableNotes) {
			if (availableNote.getKeyNumber() == note.getKeyNumber()) {
				//is in the selected scale
				return true;
			}
		}
		
		return false;	
	}
	
	
	/**
	 * This method returns the position of the specified note in the list of
	 * available notes to allow the calling method to perform pitch shift
	 * accordingly.
	 * @param noteNumber - The note number to get the position of
	 * @param availableNotes - The available notes to get the note from
	 * @return int - The Position of the specified note in the list of available
	 * 				note
	 */
	private int getPositionOfNote(int noteNumber, Note[] availableNotes) {
		
		int position = 0;
		
		for (int i = 0; i < availableNotes.length; i++) {
			if (availableNotes[i].getKeyNumber() == noteNumber) {
				position = i;
				break;
			}
		}
		
		return position;
	}

	/*************************************************
	 *  Getter/setter methods
	 *************************************************/

	@Override
	public String toString() {
		return "Pitch shift: " + pitch;
	}

	@Override
	public String getName() {
		return "Pitchshift";
	}

	@Override
	public String getParameters() {
		return Integer.toString(pitch) + ",NoParams";
	}

	public int getPitch() {
		return pitch;
	}

	public void setPitch(int pitch) {
		this.pitch = pitch;
	}
	
	@Override
	public void setAvailableNotes(Note[] availableNotes) {	}
	
	// For testing
	public Note getLastNoteSent() {
		return lastNoteSent;
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
