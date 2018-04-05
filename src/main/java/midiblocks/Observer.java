package midiblocks;
import scales.Note;

/**
 * The observer interface to allow of events to be notified by the subject that
 * the observer is observing.
 * @author Lisa Liu-Thorrold
 *
 */
public interface Observer {
    void update(Note note, Boolean noteOn);
    void update(Note note, Boolean noteOn, Note[] availableNotes);
}