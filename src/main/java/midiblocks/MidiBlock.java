package midiblocks;
/*
  * @author Lisa Liu-Thorrold
  */
public class MidiBlock {
	
	public static void main(String[] args) {
		new MidiBlock();
	}
	
	/** 
	 * This is the entry point of the application
	 */
	private MidiBlock() {
		MidiBlocksContainer container = new MidiBlocksContainer();
		MidiModel model = new MidiModel();	
		new MidiController(model, container);
		container.run();
	}
}
