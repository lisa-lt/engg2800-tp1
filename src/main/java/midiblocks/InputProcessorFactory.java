package midiblocks;
import java.io.File;

/**
 * This class uses the GoF Factory design pattern
 * to determine what type of Input processor is 
 * made, depending on the type of MIDI input source
 * the user has selected.
 * @author Lisa Liu-Thorrold
 *
 */
public class InputProcessorFactory {
	
	private MidiModel midiModel;
	
	private final static String VIRTUAL_KEYBOARD = "Virtual Keyboard";
	private final static String MIDI_FILE = "MIDI File";
	
	public InputProcessorFactory(MidiModel midiModel) {
		this.midiModel = midiModel;
	}
	
	/**
	 * Returns the relevant input processor depending on midi source selected
	 * @param inputName - The name of the midi source
	 * @return The input processor constructed from GoF design pattern
	 */
	public InputProcessor getProcessor(String inputName) {
		if(inputName.equalsIgnoreCase(VIRTUAL_KEYBOARD)){
			return new KeyboardInputProcessor(midiModel.getVirtualKeyboard());
		} else if(inputName.equalsIgnoreCase(MIDI_FILE)){
			File midiFile = midiModel.getMidiSourceFile();
			return new FileInputProcessor(midiFile);
		} else {
			return new DriverInputProcessor(inputName);
		}
	}
}
