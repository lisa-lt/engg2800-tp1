package engg2800g07;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import processingblocks.Chordify;
import scales.Note;
import scales.NoteDictionary;

public class ChordifyTest {
	
	private final NoteDictionary noteDictionary = new NoteDictionary();
	
	private final String[] notes = { "C#/Db", "D#/Eb", "E", "F#/Gb", "G#/Ab",
			"A", "C" };
	
	private final Note[] availableNotes = noteDictionary.filterAvailableNotes(notes);
	
	@Test
	public void testSimpleChordify() {
		Chordify chordify = new Chordify();
		
		// check no notes sent
		assertEquals(0, chordify.getNotesSent().size());	
		
		// The correct notes for chordify
		Note n1 = new Note(8, 'E', false, 1);
		Note n2 = new Note(12, 'G',	true, 1);
		Note n3 = new Note(16, 'C', false, 2);
		
		// Random note for testing
		Note n4 = new Note(24, 'G',	true, 2);
		
		// send one 'on' note to chordify
		chordify.update(n1, true, availableNotes); 
		
		// check that there were 3 notes sent to the next pblock
		assertEquals(3, chordify.getNotesSent().size());
		
		// check that the correct notes & corresponding messages were sent
		assertEquals(true, chordify.getNotesSent().get(n1));
		assertEquals(true, chordify.getNotesSent().get(n2));
		assertEquals(true, chordify.getNotesSent().get(n3));
		assertNotEquals(true, chordify.getNotesSent().get(n4));
	}
	
	@Test
	public void testChordifyNoteOff() {
		Chordify chordify = new Chordify();
		
		// check no notes sent
		assertEquals(0, chordify.getNotesSent().size());	
		
		// The correct notes for chordify
		Note n1 = new Note(8, 'E', false, 1);
		Note n2 = new Note(12, 'G',	true, 1);
		Note n3 = new Note(16, 'C', false, 2);
		
		Note n4 = new Note(24, 'G',	true, 2);
		
		// send one 'on' note to chordify
		chordify.update(n1, false, availableNotes); 
		
		// check that there were 3 notes sent to the next pblock
		assertEquals(3, chordify.getNotesSent().size());
		
		// check that the correct notes & corresponding messages were sent
		assertEquals(false, chordify.getNotesSent().get(n1));
		assertEquals(false, chordify.getNotesSent().get(n2));
		assertEquals(false, chordify.getNotesSent().get(n3));
		assertNotEquals(true, chordify.getNotesSent().get(n4));
	}
	
	@Test
	public void testChordifyFirstEdgeCase() {
		Chordify chordify = new Chordify();
		
		// check no notes sent
		assertEquals(0, chordify.getNotesSent().size());	
		
		
		// The correct notes for chordify
		Note n1 = new Note(82, 'F',	true, 7);
		Note n2 = new Note(85, 'A',	false, 7);
		
		// send one 'on' note to chordify
		chordify.update(n1, true, availableNotes); 
		
		// check that there were 2 notes sent (last note is out of bounds)
		assertEquals(2, chordify.getNotesSent().size());
		
		// check that the correct notes & corresponding messages were sent
		assertEquals(true, chordify.getNotesSent().get(n1));
		assertEquals(true, chordify.getNotesSent().get(n2));
	}
	
	@Test
	public void testChordifySecondEdgeCase() {
		Chordify chordify = new Chordify();
		
		// check no notes sent
		assertEquals(0, chordify.getNotesSent().size());	
		
		// The correct notes for chordify
		Note n1 = new Note(84, 'G',	true, 7);
		
		// send one 'on' note to chordify
		chordify.update(n1, true, availableNotes); 
		
		// check only 1 note sent (last two notes are out of bounds)
		assertEquals(1, chordify.getNotesSent().size());
		
		// check that the correct notes & corresponding messages were sent
		assertEquals(true, chordify.getNotesSent().get(n1));
	}

}
