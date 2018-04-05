package engg2800g07;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import processingblocks.Monophonic;
import scales.Note;

public class MonophonicTest {

	@Test
	public void testBasicMonophonic() {
		Monophonic monophonic = new Monophonic();
		
    	Note n1 = new Note(22,	'F',	true, 	2);
		Note n2 = new Note(23,	'G',	false, 	2);
		Note n3 = new Note(24,	'G',	true,	2);
		Note n4 = new Note(25,	'A',	false,	2);
		
		Note[] availableNotes = { n1, n2, n3, n4 };
		
		// should be no current note on
		assertEquals(null, monophonic.getCurrentNoteOn());
		
		monophonic.update(n1, true, availableNotes); 
		assertEquals(n1, monophonic.getCurrentNoteOn());
		
		monophonic.update(n2, true, availableNotes); 
		assertEquals(n2, monophonic.getCurrentNoteOn());
		
		monophonic.update(n3, true, availableNotes); 
		assertEquals(n3, monophonic.getCurrentNoteOn());
		
		monophonic.update(n4, true, availableNotes); 
		assertEquals(n4, monophonic.getCurrentNoteOn());
	}
	
	@Test
	public void testMonophonicLastNote() {
		Monophonic monophonic = new Monophonic();
		
    	Note n1 = new Note(22,	'F',	true, 	2);
		Note n2 = new Note(23,	'G',	false, 	2);
		Note n3 = new Note(24,	'G',	true,	2);
		
		Note[] availableNotes = { n1, n2, n3};
		
		// should be no current note on
		assertEquals(null, monophonic.getCurrentNoteOn());
		
		monophonic.update(n1, true, availableNotes); 
		assertEquals(n1, monophonic.getCurrentNoteOn());
		
		monophonic.update(n2, true, availableNotes); 
		assertEquals(n2, monophonic.getCurrentNoteOn());
		
		monophonic.update(n3, true, availableNotes); 
		assertEquals(n3, monophonic.getCurrentNoteOn());
		
		// turn the last note off for monophonic
		monophonic.update(n3, false, availableNotes); 
		assertEquals(null, monophonic.getCurrentNoteOn());
	}
	
}
