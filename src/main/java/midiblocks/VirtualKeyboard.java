package midiblocks;

import processing.core.PApplet;
import processing.core.PFont;
import scales.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * This class represents the Virtual Keyboard that the user selects as a
 * MIDI input source. The keyboard has 19 keys.
 * @author Lisa Liu-Thorrold
 *
 */

public class VirtualKeyboard extends PApplet
							implements Subject {
	
	private static final long serialVersionUID = 8315362040023408935L;

	private KeyboardInputProcessor inputProcessor;
	
	private final ArrayList<Observer> observers = new ArrayList<>();

	private final MidiModel model;
	
	private final int WIDTH = 1311;
	
	private final int HEIGHT = 294;
	
	private final int NUM_KEYS = 19;
	
	private final int KEY_WIDTH = WIDTH / NUM_KEYS;
	
	private final HashMap<Character, Integer> keyboardMap;
	
	// Private array of available notes that the keyboard has
	private Note[] availableKeyboardNotes;
	
	private boolean running;
	
	// Font used for writing the names of notes onto the keys
	private final PFont TEXT_FONT = createFont("Arial", 14, true);
	
	// A map of keys on the keyboard, used to map keys on the virtual keyboard
	private static final char[] KEYS = {'q','w','e','r','t','y','u','i','o',
			'p','a','s','d', 'f','g','h', 'j', 'k', 'l'};


	public VirtualKeyboard(MidiModel model) {
		keyboardMap = new HashMap<>();
		initiateKeyboardMap();
		availableKeyboardNotes = new Note[19];
		this.model = model;
		running = false;
	}

	/**
	 * This method sets the dimensions of the keyboard
	 */
	public void setup() {
		size(WIDTH, HEIGHT);
	}

	/**
	 * This method is called continuously
	 */
	public void draw() {
		if (running) {
			drawKeys();
		}
	}

	/**
	 * This method draws the keys according to the available keyboard notes
	 * If the key is a regular key, then it's color is white. If the key
	 * is a sharp or flat, then it's color is black. If the key is non
	 * playable, then it is grey.
	 */
	private void drawKeys() {

		int xCord = 0;
		int yCord = 0;
		int textNoteX;
		int textNoteY;
		int textLetterX;
		int textLetterY;
		int rootY = (HEIGHT/10 * 8);

		for (int i = 0; i < NUM_KEYS; i++) {

			Note note = availableKeyboardNotes[i];

			int keyColor = note.getColor();
			int textColor = 0;
			fill(keyColor);
			stroke(153);
			rect(xCord, yCord, KEY_WIDTH, HEIGHT);

			/* set the text color according to the font */
			switch (keyColor) {
				case 160:
					textColor = 0;
					break;
				case 255:
					textColor = 0;
					break;
				case 0:
					textColor = 255;
					break;
			}

			textFont(TEXT_FONT);
			fill(textColor);

			// for the name of the note 
			textNoteY = (HEIGHT/10);
			textNoteX = (xCord)  + (KEY_WIDTH/ 3);
			
			if (note.isSharp()) {
				String[] result = note.toString().split(" ");
				// The sharp portion of the note is on top
				text(result[0], textNoteX, textNoteY); 
				// The flat portion of the note is on the bottom
				text(result[1], textNoteX, textNoteY + 15);
			} else {
				// regular note
				text(note.toString(), textNoteX, textNoteY);
			}
			

			// for the corresponding keyboard letter 
			textLetterX = (xCord) + (KEY_WIDTH/2);
			textLetterY = (HEIGHT/10)*9;
			text(KEYS[i], textLetterX, textLetterY);

			// if the key is a root note, draw a dash to indicate so.
			if (model.isRootNote(note.getNoteLetter())) {
				text("-", textLetterX, rootY);
			}

			xCord += KEY_WIDTH;
		}
	}

	/**
	 * This method is invoked when a key is pressed. If it is pressed, the visual
	 * feedback is given by changing the color of the corresponding note. It
	 * also emits a MIDI Note on event.
	 */
	public void mousePressed() {
		//      int keyPressed = 0;
		for (int i = 1; i <= 19; i++) {
			if ((mouseX > (KEY_WIDTH * (i-1))  && mouseX < (KEY_WIDTH * i)) && 
					(mouseY > 0 && mouseY < HEIGHT)){

				Note note = availableKeyboardNotes[i-1];
				
				MIDINoteOn(i, note);

				break;
			}
		}
	}

	/**
	 * This method is invoked when a key is pressed. If it corresponds to a key 
	 * on the virtual keyboard, then visual feedback is given by changing
	 * the color of the corresponding note, and a MIDI Note On message is 
	 * emitted.
	 * The following behaviour occurs when the following keys are pressed:
	 * 1. 'Z' or 'z' shifts the keyboard left by a full octave size. If it 
	 * 	  cannot shift a full octave size because there are not enough keys
	 *    to shift by, then it shifts to the very beginning of the keyboard.
	 * 2. 'X' or 'x' shifts the keyboard left by a key size of 1.
	 * 3. 'N' or 'n' shifts the keyboard right by a key size of 1.
	 * 4. 'M' or 'm' shifts the keyboard right by a full octave size. If it
	 *    cannot shift a full octave size because there are not enough keys
	 *    to shift by, then it shifts to the very end of the keyboard.
	 */
	public void keyPressed() {

		int octaveSize = model.getOctaveSize();

		// Check whether the key pressed on the keyboard corresponds to a key
		// on the virtual keyboard.
		if (keyboardMap.get(key) != null ) {
			int keyPressed = keyboardMap.get(key);

			Note note = availableKeyboardNotes[keyPressed-1];
			
			MIDINoteOn(keyPressed, note);

		} else {
			// check if the user wanted to invoke a keyboard shift operation
			switch(key) {
				case 'z':
					model.shiftKeyboard(-octaveSize);
					setKeyboardNotes();
					break;
				case 'Z':
					model.shiftKeyboard(-octaveSize);
					setKeyboardNotes();
					break;
				case 'X':
					model.shiftKeyboard(-1);
					setKeyboardNotes();
					break;
				case 'x':
					model.shiftKeyboard(-1);
					setKeyboardNotes();
					break;
				case 'N':
					model.shiftKeyboard(1);
					setKeyboardNotes();
					break;
				case 'n':
					model.shiftKeyboard(1);
					setKeyboardNotes();
					break;
				case 'm':
					model.shiftKeyboard(octaveSize);
					setKeyboardNotes();
					break;
				case 'M' :
					model.shiftKeyboard(octaveSize);
					setKeyboardNotes();
					break;
				default:
					break;
			}
		}
	}

	/**
	 * This method represents a MIDI Note On event
	 * Observers are notified when a MIDI Note On event is received
	 * @param note The note whose event is Note On
	 */
	private void MIDINoteOn(int i, Note note) {
		//change the color of the key to indicate that it's been pressed.
		if (note.isPlayable()) {
			note.setNoteColor(227);
			notifyObservers(note, true);
		}
	}

	/**
	 * This method restores the original colour of the key when it is released,
	 * and emits a MIDI note off message.
	 */
	public void mouseReleased() {
		//should send the corresponding event out.
		for (int i = 1; i <= 19; i++) {
			if ((mouseX > (KEY_WIDTH * (i-1))  && mouseX < (KEY_WIDTH * i)) &&
					(mouseY > 0 && mouseY < HEIGHT)){

				Note note = availableKeyboardNotes[i-1];
				MIDINoteOff(note);
				break;
			}
		}
	}

	/**
	 * This method restores the original colour of the key when it is released,
	 * and emits a MIDI note off message.
	 */
	public void keyReleased() {
		//should send the corresponding midi out
		if (keyboardMap.get(key) != null ) {
			int keyPressed = keyboardMap.get(key);

			Note note = availableKeyboardNotes[keyPressed-1];
			MIDINoteOff(note);
		}

	}

	/**
	 * This method represents a MIDI Note Off event
	 * Observers are notified when a MIDI Note off event is received
	 * @param note The note whose event is Note Off
	 */
	private void MIDINoteOff(Note note) {
		// Restore the note's original color and notifies the observer
		if (note.isPlayable()) {
			note.restoreNoteColor();
			notifyObservers(note, false);
		}
	}
	
	/*************************************************
	 * Getter/ Setter methods
	 *************************************************/
	
	/**
	 * This method sets the available notes for the keyboard.
	 * @param availableKeyboardNotes - The notes for the virtual keyboard
	 */
	public void setAvailableNotes(Note[] availableKeyboardNotes) {
		this.availableKeyboardNotes = availableKeyboardNotes;
	}
	
	public void setInputProcessor(InputProcessor inputProcessor) {
		this.inputProcessor = (KeyboardInputProcessor)inputProcessor;
		registerObserver(this.inputProcessor);
	}


	/*************************************************
	 * Helper methods
	 *************************************************/

	/**
	 * Set the keyboard notes according to the notes that are available to it
	 * from the model.
	 */
	public void setKeyboardNotes() {
		this.availableKeyboardNotes = model.getKeyboardNotes();
		running = true;
	}

	/**
	 * A map of the keys on the keyboard to keys on the virtual keyboard.
	 */
	private void initiateKeyboardMap() {
		keyboardMap.put('Q', 1);
		keyboardMap.put('q', 1);
		keyboardMap.put('W', 2);
		keyboardMap.put('w', 2);
		keyboardMap.put('E', 3);
		keyboardMap.put('e', 3);
		keyboardMap.put('R', 4);
		keyboardMap.put('r', 4);
		keyboardMap.put('T', 5);
		keyboardMap.put('t', 5);
		keyboardMap.put('Y', 6);
		keyboardMap.put('y', 6);
		keyboardMap.put('U', 7);
		keyboardMap.put('u', 7);
		keyboardMap.put('I', 8);
		keyboardMap.put('i', 8);
		keyboardMap.put('O', 9);
		keyboardMap.put('o', 9);
		keyboardMap.put('P', 10);
		keyboardMap.put('p', 10);
		keyboardMap.put('A', 11);
		keyboardMap.put('a', 11);
		keyboardMap.put('S', 12);
		keyboardMap.put('s', 12);
		keyboardMap.put('D', 13);
		keyboardMap.put('d', 13);
		keyboardMap.put('F', 14);
		keyboardMap.put('f', 14);
		keyboardMap.put('G', 15);
		keyboardMap.put('g', 15);
		keyboardMap.put('H', 16);
		keyboardMap.put('h', 16);	
		keyboardMap.put('J', 17);
		keyboardMap.put('j', 17);
		keyboardMap.put('K', 18);
		keyboardMap.put('k', 18);
		keyboardMap.put('L', 19);
		keyboardMap.put('l', 19);
	}
	
	/*************************************************
	 *  Observer methods
	 *************************************************/

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

	@Override
	public void removeAllObservers() {
		Iterator<Observer> iterator = observers.iterator();

		while (iterator.hasNext()){
			iterator.next();
			iterator.remove();
		}
	}
}
