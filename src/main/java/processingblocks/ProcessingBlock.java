package processingblocks;

import midiblocks.Observer;
import midiblocks.Subject;
import scales.Note;


/**
 * This is the interface which all Processing Blocks implement.
 * @author Lisa Liu-Thorrold
 *
 */
public interface ProcessingBlock extends Observer, Subject {
	String toString();
	String getName();
	String getParameters();
	void setAvailableNotes(Note[] availableNotes);
	void update(Note note, Boolean noteOn, Note[] availableNotes);
}
