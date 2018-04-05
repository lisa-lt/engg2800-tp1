package engg2800g07;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import midiblocks.MidiModel;
import midiblocks.OutputProcessor;
import scales.Note;

public class OutputProcessorTest {

	MidiModel midiModel;
	OutputProcessor outputProcessor;
	Note n1, n2, n3, n4;

	@Before
	public void beforeEach() throws Exception {
		
		outputProcessor = new OutputProcessor(midiModel, true, false);
		
    	n1 = new Note(22,	'F',	true, 	2);
		n2 = new Note(23,	'G',	false, 	2);
		n3 = new Note(24,	'G',	true,	2);
		n4 = new Note(25,	'A',	false,	2);
	}
	
	@Test
	public void basicTest() {	
		// should be empty
		assertEquals(0, outputProcessor.getNoteOnMessagesSent().size());
		
		// send a note on to output
		outputProcessor.update(n1, true);
		assertEquals(1, outputProcessor.getNoteOnMessagesSent().size());
		
		// send the corresponding not off to output
		outputProcessor.update(n1, false);
		assertEquals(0, outputProcessor.getNoteOnMessagesSent().size());	
	}
	
	
	@Test
	public void testChangeProcessingBlockParameters() {
		// send a note on to output
		outputProcessor.update(n1, true);
		assertEquals(1, outputProcessor.getNoteOnMessagesSent().size());
		
		// send the corresponding not off to output
		outputProcessor.update(n2, true);
		assertEquals(2, outputProcessor.getNoteOnMessagesSent().size());
		
		// processing blocks changed, flush all messages (send note off's!)
		outputProcessor.flushMessages();
		
		assertEquals(0, outputProcessor.getNoteOnMessagesSent().size());
	}
	
	@Test
	public void testDuplicateNoteOn() {
		
		// send a note on to output
		outputProcessor.update(n1, true);
		assertEquals(1, outputProcessor.getNoteOnMessagesSent().size());
		
		// send the same note again (it should be sent twice)
		outputProcessor.update(n1, true);
		assertEquals(1, outputProcessor.getNoteOnMessagesSent().size());
	}
	
	

	
}
