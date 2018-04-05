package engg2800g07;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import processingblocks.PitchShift;
import scales.Note;
import scales.NoteDictionary;

public class PitchShiftTest {

	private final NoteDictionary noteDictionary = new NoteDictionary();

	private final String[] notes = { "C#/Db", "D#/Eb", "E", "F#/Gb", "G#/Ab",
			"A", "C" };

	private final Note[] availableNotes = noteDictionary.filterAvailableNotes(notes);

	@Test
	public void simplePositivePitchShiftTest() {
		PitchShift pitchShift = new PitchShift(3);

		assertEquals(null, pitchShift.getLastNoteSent());

		Note n1 = new Note(8, 'E', false, 1);
		Note correctNote = new Note(13,	'A', false, 1);

		pitchShift.update(n1, true, availableNotes);

		assertEquals(correctNote, pitchShift.getLastNoteSent());

	}

	@Test
	public void simpleNegavePitchShiftTest() {
		PitchShift pitchShift = new PitchShift(-3);

		assertEquals(null, pitchShift.getLastNoteSent());

		Note n1 = new Note(52, 'C', false, 5);
		Note correctNote = new Note(46, 'F', true, 4);

		pitchShift.update(n1, true, availableNotes);

		assertEquals(correctNote, pitchShift.getLastNoteSent());
	}

	@Test
	public void positiveEdgeCaseTest() {
		PitchShift pitchShift = new PitchShift(300);
		
		Note n1 = new Note(52, 'C', false, 5);
		Note correctNote = new Note(85, 'A', false,	7);
		
		pitchShift.update(n1, true, availableNotes);

		assertEquals(correctNote, pitchShift.getLastNoteSent());
	}

	@Test
	public void negativeEdgeCaseTest() {
		PitchShift pitchShift = new PitchShift(-300);
		
		Note n1 = new Note(52, 'C', false, 5);
		Note correctNote = new Note(4, 'C', false, 1);
		
		pitchShift.update(n1, true, availableNotes);

		assertEquals(correctNote, pitchShift.getLastNoteSent());
	}

	@Test
	public void simpleOutsideAvailableNotesTest() {
		PitchShift pitchShift = new PitchShift(0);
		
		Note outOfRange = new Note(54, 'D', false, 5);
		Note correctNote = new Note(53,	'C', true, 5);
		
		// out of range, and in between notes 53 and 55 in the available notes
		pitchShift.update(outOfRange, true, availableNotes);

		// shift of 0 should round it down to 53.
		assertEquals(correctNote, pitchShift.getLastNoteSent());
	}

	@Test
	public void outsideAvailableNotesTest() {
		PitchShift pitchShift = new PitchShift(-2);
		
		Note outOfRange = new Note(81, 'F', false, 7);
		Note correctNote = new Note(77,	'C', true, 7);
		
		// out of range, and in between notes 80 and 82
		pitchShift.update(outOfRange, true, availableNotes);

		// shift of -2 should first round it down to 80, then to 77
		assertEquals(correctNote, pitchShift.getLastNoteSent());
		
	}

}
