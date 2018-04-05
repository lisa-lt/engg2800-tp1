package midiblocks;
import scales.Note;

/**
 * This interface defines an InputProcessor that is responsible for receiving
 * MIDI events from MIDI input sources, and passes these events on to the
 * Processing Block Controller.
 * @author Lisa Liu-Thorrold
 *
 */
public interface InputProcessor {
	void sendToProcessorController(Note note, Boolean noteOn);
	void setRunning(Boolean running);
	void removeObserver(Observer observer);
	void registerObserver(Observer observer);
	void removeAllObservers();
}

