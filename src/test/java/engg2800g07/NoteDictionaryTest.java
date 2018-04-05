package engg2800g07;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import scales.Note;
import scales.NoteDictionary;

public class NoteDictionaryTest {
	
	@Test
	public void testFilterAvailableNotes() {
		
		String[] notes = { "C#/Db", "D#/Eb", "E", "F#/Gb", "G#/Ab", 
				"A", "C" };
		
		NoteDictionary noteDictionary = new NoteDictionary();
		
		ArrayList<Note> availableNotes = new ArrayList<>();
		
		availableNotes.add(new Note(4,	'C',	false,	1));
		availableNotes.add(new Note(5,	'C',	true,	1));
		availableNotes.add(new Note(7,	'D', 	true,	1));
		availableNotes.add(new Note(8,	'E', 	false,	1));
		availableNotes.add(new Note(10,	'F', 	true,	1));
		availableNotes.add(new Note(12,	'G',	true, 	1));
		availableNotes.add(new Note(13,	'A',	false, 	1));
		availableNotes.add(new Note(16,	'C', 	false,	2));
		
		availableNotes.add(new Note(17,	'C', 	true,	2));
		availableNotes.add(new Note(19,	'D', 	true,	2));
		availableNotes.add(new Note(20,	'E', 	false,	2));
		availableNotes.add(new Note(22,	'F',	true, 	2));
		availableNotes.add(new Note(24,	'G',	true,	2));
		availableNotes.add(new Note(25,	'A',	false,	2));
		availableNotes.add(new Note(28,	'C', 	false,	3));
		
		availableNotes.add(new Note(29,	'C', 	true,	3));
		availableNotes.add(new Note(31,	'D',	true,	3));
		availableNotes.add(new Note(32,	'E',	false, 	3));
		availableNotes.add(new Note(34,	'F',	true,	3));
		availableNotes.add(new Note(36,	'G', 	true,	3));
		availableNotes.add(new Note(37,	'A', 	false,	3));
		availableNotes.add(new Note(40,	'C', 	false,	4));
		
		availableNotes.add(new Note(41,	'C',	true,	4));
		availableNotes.add(new Note(43,	'D',	true, 	4));
		availableNotes.add(new Note(44,	'E',	false,	4));
		availableNotes.add(new Note(46,	'F', 	true,	4));
		availableNotes.add(new Note(48,	'G', 	true,	4));
		availableNotes.add(new Note(49,	'A', 	false,	4));
		availableNotes.add(new Note(52,	'C',	false, 	5));
		
		availableNotes.add(new Note(53,	'C',	true, 	5));
		availableNotes.add(new Note(55,	'D',	true,	5));
		availableNotes.add(new Note(56,	'E', 	false,	5));
		availableNotes.add(new Note(58,	'F', 	true,	5));
		availableNotes.add(new Note(60,	'G', 	true,	5));
		availableNotes.add(new Note(61,	'A',	false,	5));
		availableNotes.add(new Note(64,	'C',	false,	6));
		
		availableNotes.add(new Note(65,	'C',	true,	6));
		availableNotes.add(new Note(67,	'D', 	true,	6));
		availableNotes.add(new Note(68,	'E', 	false,	6));
		availableNotes.add(new Note(70,	'F', 	true,	6));
		availableNotes.add(new Note(72,	'G',	true, 	6));
		availableNotes.add(new Note(73,	'A',	false, 	6));
		availableNotes.add(new Note(76,	'C', 	false,	7));
		
		availableNotes.add(new Note(77,	'C', 	true,	7));
		availableNotes.add(new Note(79,	'D', 	true,	7));
		availableNotes.add(new Note(80,	'E', 	false,	7));
		availableNotes.add(new Note(82,	'F',	true, 	7));
		availableNotes.add(new Note(84,	'G',	true,	7));
		availableNotes.add(new Note(85,	'A',	false,	7));
		
		Note[] correctFilteredNotes = new Note[availableNotes.size()];
		correctFilteredNotes = availableNotes.toArray(correctFilteredNotes);
		
		Note[] toTestFilteredNotes = noteDictionary.filterAvailableNotes(notes);
		
		for (int i = 0; i< correctFilteredNotes.length; i++) {
			
			Note correctNote = correctFilteredNotes[i];
			Note noteToCheck = toTestFilteredNotes[i];
			
			assertEquals(correctNote, noteToCheck);
		}
	}
}
