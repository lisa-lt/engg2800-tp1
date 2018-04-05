package midiblocks;
import java.util.ArrayList;
import java.util.Iterator;

import scales.Note;

/**
 * This class represents an observer of events produced by the virtual keyboard.
 * It is observed by the Processing Block Controller, which processes all 
 * events through the processing blocks.
 * @author Lisa Liu-Thorrold
 *
 */
public class KeyboardInputProcessor implements InputProcessor, Observer, Subject {
	
	/*  The virtual keyboard whose events this class observes */
	private VirtualKeyboard virtualKeyboard;
	
	/* Observers that are listening to events from this class/object */
	private final ArrayList<Observer> observers = new ArrayList<>();
	
	public KeyboardInputProcessor(VirtualKeyboard virtualKeyboard) {
		this.setKeyboard(virtualKeyboard);
	}
	
	/*************************************************
	 *  Observer / subject methods
	 *************************************************/

	/** 
	 * This method sends MIDI events received to the Processor Controller
	 * @param note - The note to send 
	 * @param noteOn - Whether the note is on or off
	 */
	public void sendToProcessorController(Note note, Boolean noteOn) {
		notifyObservers(note, noteOn);	
	}

	/** 
	 * This method receives messages from the keyboard,
	 * and passes it on to the observer (Processor Controller)
	 * @param note - The note received
	 * @param noteOn - Whether the note is an on or off message
	 */
	@Override
	public void update(Note note, Boolean noteOn) {
		sendToProcessorController(note, noteOn);	
	}

	/**
	 * This method removes any observers that may be
	 * listening to events originating from this object.
	 * This is because the observer may want to observe
	 * input from another MIDI source (MIDI Driver source
	 * or virtual keyboard)
	 */
	@Override ()
	public void removeObserver(Observer observer) {
		observers.remove(observer);	
	}

	/**
	 * This method registers observers (the Processor controller) so 
	 * that it may listen to events.
	 */
	@Override
	public void registerObserver(Observer observer) {
		observers.add(observer);	
	}

	/**
	 * This method notifies the Processor controller, when 
	 * MIDI events have been received.
	 */
	@Override
	public void notifyObservers(Note note, Boolean noteOn) {
		for (Observer observer : observers) {
            observer.update(note, noteOn);
		}	
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

		while (iterator.hasNext()){
			Observer observer = iterator.next();
			// stop processing block controller from continuing
			// to process previous input (eg. gates/ arpeggiator)
			if (observer instanceof ProcessingBlockController) {
				((ProcessingBlockController) observer).changeMidiSource();
			}
	         iterator.remove();
		}		
	}
	
	@Override
	public void update(Note note, Boolean noteOn, Note[] availableNotes) {	}
	
	/*************************************************
	 *  Getter/ setter methods
	 *************************************************/
	
	@Override
	public void setRunning(Boolean running) {}

	/**
	 * Get the instance of virtual keyboard
	 * @return virtualKeyboard - the instance of our virtual keyboard
	 */
	public VirtualKeyboard getVirtualKeyboard() {
		return virtualKeyboard;
	}

	/**
	 * Set the virtual keyboard instance
	 * @param virtualKeyboard - The virtual keyboard to set
	 */
	public void setKeyboard(VirtualKeyboard virtualKeyboard) {
		this.virtualKeyboard = virtualKeyboard;
	}

}
