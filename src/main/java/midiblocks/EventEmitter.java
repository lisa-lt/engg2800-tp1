package midiblocks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


/**
 * This class was adapted from CSEE3002 team project (Semester 1, 2015)
 * Written on: May 23 2015
 * Co-authors: Kerrin English, Lisa Liu-Thorrold
 */

/**
 * An EventEmitter is responsible for managing a set of listeners and publishing
 * events to those listeners when it is informed that the particular event
 * has occurred. The publication works in a simple multicast fashion.
 */
public class EventEmitter {
	private Map<String, List<Consumer<Payload>>> _listeners = new HashMap<>();

	/**
	 * Adds a new listener to be invoked whenever an event of the specified
	 * is emitted.
	 * @param eventType - The name of the event to listen to
	 * @param listener - Function to invoke when the eventType is emitted.
	 */
	public ListenerSubscription addListener(String eventType, 
			Consumer<Payload> listener) {
		if (_listeners.get(eventType) == null) {
			_listeners.put(eventType, new ArrayList<>());
		}

		int key = _listeners.get(eventType).size();
		_listeners.get(eventType).add(listener);

		return new ListenerSubscription(this, eventType, key);
	}

	/**
	 * Emits an event of the given type. All registered handlers for that type
	 * will be invoked.
	 * @param eventType - The name of the event to emit
	 */
	public void emit(String eventType) {

		List<Consumer<Payload>> listeners = _listeners.get(eventType);
		if (listeners == null) {
			return;
		}

		listeners.stream().filter(listener -> listener != null).forEach(
				listener -> listener.accept(new Payload()));
	}

}

/**
 * Event payload
 */
class Payload {}

/**
 * A subscription token for identifying a particular listener subscription.
 */
class ListenerSubscription {
	private EventEmitter emitter;
	private String eventType;
	private int key;

	public ListenerSubscription(EventEmitter emitter, String eventType, 
			int key) {
		this.emitter = emitter;
		this.eventType = eventType;
		this.key = key;
	}

}
