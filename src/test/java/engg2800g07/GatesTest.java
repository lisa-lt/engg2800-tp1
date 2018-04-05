package engg2800g07;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import processingblocks.Gates;
import scales.Note;

public class GatesTest {

	private Note n1, n2, n3, n4;
	private final Note[] availableNotes = { n1, n2, n3, n4 };
	
	private final static String QUEUE = "Queue";
	private final static String FIRST_HOLD = "First Hold";
	private final static String LAST_HOLD = "Last Hold";
	
	// notes per tick >= 1
	private final static double NOTES_PER_TICK_NORMAL = 2.0;
	
	// 0 < notes per tick < 1
	private final static double NOTES_PER_TICK = 0.45;
	
	@Before
	public void beforeEach() throws Exception {
    	n1 = new Note(22,	'F',	true, 	2);
		n2 = new Note(23,	'G',	false, 	2);
		n3 = new Note(24,	'G',	true,	2);
		n4 = new Note(25,	'A',	false,	2);
	}
	
	@Test 
	public void testQueueGateRelease() {
		
		Gates gates = new Gates(QUEUE, NOTES_PER_TICK);
		
		gates.update(n1, true, availableNotes); 
		gates.update(n4, true, availableNotes); 
		gates.update(n3, true, availableNotes); 
		gates.update(n2, true, availableNotes); 

		// Tick received, release notes from the gate
		gates.release();

		// Should be 0, as no note off messages received so far
		assertEquals(0, gates.getSendWhenGateOpens().size());

		// verify the queue released three notes sent, so one remaining
		assertEquals(n2, gates.getGateQueue().get(0));
		
		// verify 3 notes sent
		assertEquals(n1, gates.getNoteOnOutputted().get(0));
		assertEquals(n4, gates.getNoteOnOutputted().get(1));
		assertEquals(n3, gates.getNoteOnOutputted().get(2));

	}
	
	@Test
	public void testSimpleQueueGateRelease() throws Exception {
		
    	LinkedList<Note> correctList = new LinkedList<>();
    	
    	correctList.add(n1);
		correctList.add(n4);
		correctList.add(n3);
		correctList.add(n2);
		
    	Gates gates = new Gates(QUEUE, NOTES_PER_TICK_NORMAL);
		
		gates.update(n1, true, availableNotes); 
		gates.update(n4, true, availableNotes); 
		gates.update(n3, true, availableNotes); 
		gates.update(n2, true, availableNotes); 
		
		ArrayList<Note> listToTest = gates.getGateQueue();
		
		// check that the gate queue is correct
		for (int i = 0; i< listToTest.size(); i++) {
			assertEquals(correctList.get(i), listToTest.get(i));
		}
		
		// check that no notes has been sent
		assertEquals(0, gates.getNoteOnOutputted().size());
		
		// check that there are no pending notes to send when gate is open
		assertEquals(0, gates.getSendWhenGateOpens().size());
		
		// Tick received, release notes from the gate
		gates.release();
		
		// Should be 0, as no note off messages received so far
		assertEquals(0, gates.getSendWhenGateOpens().size());
		
		// verify the queue released two notes, and thus two remaining
		assertEquals(n3, gates.getGateQueue().get(0));
		assertEquals(n2, gates.getGateQueue().get(1));
		
		assertEquals(n1, gates.getNoteOnOutputted().get(0));
		assertEquals(n4, gates.getNoteOnOutputted().get(1));
		
    }
	
	@Test
	public void testSimpleQueueNoteCancel() {

    	Gates gates = new Gates(QUEUE, NOTES_PER_TICK_NORMAL);
		
		gates.update(n1, true, availableNotes); 
		gates.update(n4, true, availableNotes); 
		gates.update(n3, true, availableNotes); 
		gates.update(n2, true, availableNotes); 
		
		// release 2 notes
		gates.release();
		
		// should be 2 notes left in the gate
		assertEquals(2, gates.getGateQueue().size());
		
		// send corresponding off note to a note currently on the gate
		gates.update(n3, false, availableNotes);
		
		// the note should be cancelled and dropped from the queue
		assertEquals(1, gates.getGateQueue().size());
		assertEquals(n2, gates.getGateQueue().get(0));
		
		// 2 notes are sent from the gate
		assertEquals(2, gates.getNoteOnOutputted().size());
		assertEquals(n1, gates.getNoteOnOutputted().get(0));
		assertEquals(n4, gates.getNoteOnOutputted().get(1));
	}
	
	@Test
	public void testNoteOffAfterRelease() {

    	Gates gates = new Gates(QUEUE, NOTES_PER_TICK_NORMAL);
		
		gates.update(n1, true, availableNotes); 
		gates.update(n4, true, availableNotes); 
		gates.update(n3, true, availableNotes); 
		gates.update(n2, true, availableNotes); 
		
		// release 2 notes
		gates.release();
		
		// should be 2 notes left in the gate
		assertEquals(2, gates.getGateQueue().size());
		
		// send corresponding off note to a note currently on the gate
		gates.update(n1, false, availableNotes);
		
		// the note should be cancelled and dropped from the queue
		assertEquals(1, gates.getSendWhenGateOpens().size());
		assertEquals(n1, gates.getSendWhenGateOpens().get(0));
	}
	
	@Test
	public void testFirstHoldRelease() {
		Gates gates = new Gates(FIRST_HOLD, NOTES_PER_TICK_NORMAL);
		
		gates.update(n1, true, availableNotes); 
		gates.update(n4, true, availableNotes); 
		gates.update(n3, true, availableNotes); 
		gates.update(n2, true, availableNotes);
		
		// should release the first note in the queue, and clear the queue
		gates.release();
		
		// check the queue is empty
		assertEquals(0, gates.getGateQueue().size());
		
		// check that one note was sent
		assertEquals(1, gates.getNoteOnOutputted().size());
		assertEquals(n1, gates.getNoteOnOutputted().get(0));
		
		// check stuff to send next time gate opens is empty
		assertEquals(0, gates.getSendWhenGateOpens().size());
	}
	
	@Test
	public void testSimpleFirstHoldNoteOff() {
		Gates gates = new Gates(FIRST_HOLD, NOTES_PER_TICK_NORMAL);
		
		gates.update(n1, true, availableNotes); 
		gates.update(n4, true, availableNotes); 
		gates.update(n3, true, availableNotes); 
		gates.update(n2, true, availableNotes);
		
		// should release the first note in the queue, and clear the queue
		gates.release();
		
		// check that this should just pass through as it's not in the queue
		gates.update(n2,  false, availableNotes);
		
		// check that this does not affect our queues
		assertEquals(0, gates.getGateQueue().size());
		assertEquals(1, gates.getNoteOnOutputted().size());
		assertEquals(n1, gates.getNoteOnOutputted().get(0));
		assertEquals(0, gates.getSendWhenGateOpens().size());
		
	}
	
	@Test
	public void testFirstHoldNoteOff() {
		Gates gates = new Gates(FIRST_HOLD, NOTES_PER_TICK_NORMAL);
		
		gates.update(n1, true, availableNotes); 
		gates.update(n4, true, availableNotes); 
		gates.update(n3, true, availableNotes); 
		gates.update(n2, true, availableNotes);
		
		// should release the first note in the queue, and clear the queue
		gates.release();
		
		// note off received for a note sent by the gate
		gates.update(n1,  false, availableNotes);
		
		// check gate queue is still empty
		assertEquals(0, gates.getGateQueue().size());
		
		// check that n1 was sent from the release
		assertEquals(1, gates.getNoteOnOutputted().size());
		assertEquals(n1, gates.getNoteOnOutputted().get(0));
		
		// check that n1's note off will be sent next time the gate opens
		assertEquals(1, gates.getSendWhenGateOpens().size());
		assertEquals(n1, gates.getSendWhenGateOpens().get(0));
	}
	
	@Test
	public void testLastHold() {
		Gates gates = new Gates(LAST_HOLD, NOTES_PER_TICK_NORMAL);
		
		gates.update(n1, true, availableNotes); 
		gates.update(n4, true, availableNotes); 
		gates.update(n3, true, availableNotes); 
		gates.update(n2, true, availableNotes);
		
		// should release the last note in the queue, and clear the queue
		gates.release();
		
		// check the queue is empty
		assertEquals(0, gates.getGateQueue().size());
		
		// check that one note was sent
		assertEquals(1, gates.getNoteOnOutputted().size());
		assertEquals(n2, gates.getNoteOnOutputted().get(0));
		
		// check stuff to send next time gate opens is empty
		assertEquals(0, gates.getSendWhenGateOpens().size());
		
	}
	
	@Test
	public void testSimpleLastHoldNoteOff() {
		Gates gates = new Gates(LAST_HOLD, NOTES_PER_TICK_NORMAL);
		
		gates.update(n1, true, availableNotes); 
		gates.update(n4, true, availableNotes); 
		gates.update(n3, true, availableNotes); 
		gates.update(n2, true, availableNotes);
		
		// should release the last note in the queue, and clear the queue
		gates.release();
		
		// check that this should just pass through as it's not in the queue
		gates.update(n1,  false, availableNotes);
		
		// check that this does not affect our queues
		assertEquals(0, gates.getGateQueue().size());
		assertEquals(0, gates.getSendWhenGateOpens().size());
		// check that the one last note was sent
		assertEquals(1, gates.getNoteOnOutputted().size());	
		assertEquals(n2, gates.getNoteOnOutputted().get(0));
	}
	
	@Test
	public void testLastHoldNoteOff() {
		Gates gates = new Gates(LAST_HOLD, NOTES_PER_TICK_NORMAL);
		
		gates.update(n1, true, availableNotes); 
		gates.update(n4, true, availableNotes); 
		gates.update(n3, true, availableNotes); 
		gates.update(n2, true, availableNotes);
		
		// should release the last note in the queue, and clear the queue
		gates.release();
		
		// note off received for a note sent by the gate
		gates.update(n2,  false, availableNotes);
		
		// check gate queue is still empty
		assertEquals(0, gates.getGateQueue().size());
		
		// check that n2 was sent from the release
		assertEquals(1, gates.getNoteOnOutputted().size());
		assertEquals(n2, gates.getNoteOnOutputted().get(0));
		
		// check that n2's note off will be sent next time the gate opens
		assertEquals(1, gates.getSendWhenGateOpens().size());
		assertEquals(n2, gates.getSendWhenGateOpens().get(0));
	}
	
}
