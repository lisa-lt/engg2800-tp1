package engg2800g07;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import processingblocks.Arpeggiator;
import scales.Note;

/**
 * Tests the behaviour of all the Arpeggiator Processing Block
 * @author Lisa
 *
 */
public class ArpeggiatorTest {
	
	private Note n1, n2, n3, n4;
	private final Note[] availableNotes = { n1, n2, n3, n4 };
	
	private final static String DESCENDING = "Descending Scale";
	private final static String ASCENDING = "Ascending Scale";
	private final static String PING_PONG = "Ping pong";
	private final static String RANDOM = "Random";
	
	@Before
	public void beforeEach() throws Exception {
    	
    	n1 = new Note(22,	'F',	true, 	2);
		n2 = new Note(23,	'G',	false, 	2);
		n3 = new Note(24,	'G',	true,	2);
		n4 = new Note(25,	'A',	false,	2);
	}

	@Test
	public void testArpeggiatorInsertAscendingOrder() throws Exception {
		
    	Arpeggiator arpeggiator = new Arpeggiator(ASCENDING);
    	
    	LinkedList<Note> correctList = new LinkedList<>();
    	
    	correctList.add(n1);
		correctList.add(n2);
		correctList.add(n3);
		correctList.add(n4);
		
		arpeggiator.update(n1, true, availableNotes); 
		arpeggiator.update(n4, true, availableNotes); 
		arpeggiator.update(n3, true, availableNotes); 
		arpeggiator.update(n2, true, availableNotes); 
		
		LinkedList<Note> listToTest = arpeggiator.getNotesCurrentlyOn();
		
		// check that the notes were inserted in ascending order
		for (int i = 0; i< listToTest.size(); i++) {
			assertEquals(correctList.get(i), listToTest.get(i));
		}
    }
	
	@Test
	public void testArpeggiatorAscending() throws Exception {
		
    	Arpeggiator arpeggiator = new Arpeggiator(ASCENDING);
		
		arpeggiator.update(n1, true, availableNotes); 
		arpeggiator.update(n4, true, availableNotes); 
		arpeggiator.update(n3, true, availableNotes); 
		arpeggiator.update(n2, true, availableNotes); 
		
		// Test the note for correct arpeggiation in the ascending order
		// The first one should be note, and it should successively ascending
		// with each arpeggiation. Once the top has been reached, it should
		// go back to the bottom
		assertEquals(null, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n1, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n2, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		// should wrap back to the lowest note
		arpeggiator.arpeggiate();
		assertEquals(n1, arpeggiator.getLastArpeggiatedNote());
		
		// should continue arpeggiating ascenidng sequence
		arpeggiator.arpeggiate();
		assertEquals(n2, arpeggiator.getLastArpeggiatedNote());
    }
	
	@Test
	public void testArpeggiatorDescending() throws Exception {
		
		Arpeggiator arpeggiator = new Arpeggiator(DESCENDING);
		
		arpeggiator.update(n1, true, availableNotes); 
		arpeggiator.update(n4, true, availableNotes); 
		arpeggiator.update(n3, true, availableNotes); 
		arpeggiator.update(n2, true, availableNotes); 
		
		// Test the note for correct arpeggiation in the ascending order
		// The first one should be note, and it should successively ascending
		// with each arpeggiation. Once the top has been reached, it should
		// go back to the bottom
		assertEquals(arpeggiator.getLastArpeggiatedNote(), null);
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n2, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n1, arpeggiator.getLastArpeggiatedNote());
		
		// wraps back up to the highest note
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		// and continues arpeggiating sequence
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
    }
	
	@Test
	public void testArpeggiatorPingPong() throws Exception {
		Arpeggiator arpeggiator = new Arpeggiator(PING_PONG);
		
		arpeggiator.update(n1, true, availableNotes); 
		arpeggiator.update(n4, true, availableNotes); 
		arpeggiator.update(n3, true, availableNotes); 
		arpeggiator.update(n2, true, availableNotes); 
		
		// Test the note for correct arpeggiation in the ping pong order
		// Should start ascending, then when it hits the highest note, should
		// start descending
		assertEquals(null, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n1, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n2, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		// hits the highest note, start descending
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		// continues descending sequence
		arpeggiator.arpeggiate();
		assertEquals(arpeggiator.getLastArpeggiatedNote(), n2);

    }
	
	@Test
	public void testArpeggiatorRandom() throws Exception {
		Arpeggiator arpeggiator = new Arpeggiator(RANDOM);
		
		arpeggiator.update(n1, true, availableNotes); 
		arpeggiator.update(n4, true, availableNotes); 
		arpeggiator.update(n3, true, availableNotes); 
		arpeggiator.update(n2, true, availableNotes); 
		
		// Test next output note should not be the same as the currently on 
		// note (ie. last arpeggiated note) - unless there is only one note on
		assertEquals(null, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		Note lastArpeggiatedNote = arpeggiator.getLastArpeggiatedNote();
		
		arpeggiator.arpeggiate();
		assertNotEquals(lastArpeggiatedNote, arpeggiator.getLastArpeggiatedNote());
		lastArpeggiatedNote = arpeggiator.getLastArpeggiatedNote();
		
		arpeggiator.arpeggiate();
		assertNotEquals(lastArpeggiatedNote, arpeggiator.getLastArpeggiatedNote());
		lastArpeggiatedNote = arpeggiator.getLastArpeggiatedNote();
		
		arpeggiator.arpeggiate();
		assertNotEquals(lastArpeggiatedNote, arpeggiator.getLastArpeggiatedNote());
		lastArpeggiatedNote = arpeggiator.getLastArpeggiatedNote();
		
		arpeggiator.arpeggiate();
		assertNotEquals(lastArpeggiatedNote, arpeggiator.getLastArpeggiatedNote());
		lastArpeggiatedNote = arpeggiator.getLastArpeggiatedNote();
		
		arpeggiator.arpeggiate();
		assertNotEquals(lastArpeggiatedNote, arpeggiator.getLastArpeggiatedNote());
    }
	
	@Test
	public void testArpeggiatorRandomOneNote() throws Exception {
		Arpeggiator arpeggiator = new Arpeggiator("Random");
		
		arpeggiator.update(n1, true, availableNotes);  
	 
		// Test next output note should be the same as the currently on 
		// if there is only one note on
		assertEquals(null, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		Note lastArpeggiatedNote = arpeggiator.getLastArpeggiatedNote();
		
		arpeggiator.arpeggiate();
		assertEquals(lastArpeggiatedNote, arpeggiator.getLastArpeggiatedNote());
		lastArpeggiatedNote = arpeggiator.getLastArpeggiatedNote();
		
		arpeggiator.arpeggiate();
		assertEquals(lastArpeggiatedNote, arpeggiator.getLastArpeggiatedNote());
		lastArpeggiatedNote = arpeggiator.getLastArpeggiatedNote();
		
		arpeggiator.arpeggiate();
		assertEquals(lastArpeggiatedNote, arpeggiator.getLastArpeggiatedNote());
    }
	
	@Test 
	public void testAddingNotesAscending() {
		Arpeggiator arpeggiator = new Arpeggiator(ASCENDING);
		
		arpeggiator.update(n1, true, availableNotes); 
		
		arpeggiator.arpeggiate();
		arpeggiator.arpeggiate();
		
		arpeggiator.update(n4, true, availableNotes); 
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.update(n3, true, availableNotes); 
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.update(n2, true, availableNotes); 
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n1, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n2, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
	}

	@Test
	public void testRemovingNotesAscending() {
		Arpeggiator arpeggiator = new Arpeggiator(ASCENDING);
		
		arpeggiator.update(n1, true, availableNotes); 
		arpeggiator.update(n4, true, availableNotes); 
		arpeggiator.update(n3, true, availableNotes); 
		arpeggiator.update(n2, true, availableNotes);
		
		arpeggiator.arpeggiate();
		assertEquals(n1, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.update(n2, false, availableNotes);
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		
		arpeggiator.update(n1, false, availableNotes);
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
	}
	
	@Test 
	public void testAddingNotesDescending() {
		Arpeggiator arpeggiator = new Arpeggiator(DESCENDING);
		
		arpeggiator.update(n1, true, availableNotes); 
		
		arpeggiator.arpeggiate();
		arpeggiator.arpeggiate();
		
		arpeggiator.update(n4, true, availableNotes); 
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.update(n3, true, availableNotes); 
		arpeggiator.arpeggiate();
		assertEquals(n1, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.update(n2, true, availableNotes); 
		arpeggiator.arpeggiate();
		assertEquals(n1, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n2, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n1, arpeggiator.getLastArpeggiatedNote());
		
	}

	@Test
	public void testRemovingNotesDescending() {
		Arpeggiator arpeggiator = new Arpeggiator(DESCENDING);
		
		arpeggiator.update(n1, true, availableNotes); 
		arpeggiator.update(n4, true, availableNotes); 
		arpeggiator.update(n3, true, availableNotes); 
		arpeggiator.update(n2, true, availableNotes);
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.update(n2, false, availableNotes);
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		
		arpeggiator.update(n1, false, availableNotes);
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
	}
	
	@Test 
	public void testAddingNotesPingPong() {
		Arpeggiator arpeggiator = new Arpeggiator(PING_PONG);
		
		arpeggiator.update(n1, true, availableNotes); 
		
		arpeggiator.arpeggiate();
		arpeggiator.arpeggiate();
		
		arpeggiator.update(n4, true, availableNotes); 
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.update(n3, true, availableNotes); 
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		arpeggiator.arpeggiate();
		assertEquals(n1, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.update(n2, true, availableNotes); 
		arpeggiator.arpeggiate();
		assertEquals(n2, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n2, arpeggiator.getLastArpeggiatedNote());

	}

	@Test
	public void testRemovingNotesPingPong() {
		Arpeggiator arpeggiator = new Arpeggiator(PING_PONG);
		
		arpeggiator.update(n1, true, availableNotes); 
		arpeggiator.update(n4, true, availableNotes); 
		arpeggiator.update(n3, true, availableNotes); 
		arpeggiator.update(n2, true, availableNotes);
		
		arpeggiator.arpeggiate();
		assertEquals(n1, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.update(n2, false, availableNotes);
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		
		arpeggiator.update(n1, false, availableNotes);
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n3, arpeggiator.getLastArpeggiatedNote());
		
		arpeggiator.arpeggiate();
		assertEquals(n4, arpeggiator.getLastArpeggiatedNote());
		
	}
	
}
