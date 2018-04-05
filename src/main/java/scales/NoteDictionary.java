package scales;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a dictionary of numbers to notes, representing the 88 keys on a real
 * piano.
 * @author Lisa Liu-Thorrold
 *
 */
public class NoteDictionary {

	private final HashMap<Integer, Note> keyMap;
	
	public NoteDictionary() {
		keyMap = new HashMap<>();
		initiateDictionary();
	}

	/*************************************************
	 *  Getter/setter methods
	 *************************************************/
	public Note getNote(int noteNumber) { return keyMap.get(noteNumber); }


	/*************************************************
	 *  Helper methods
	 *************************************************/

	/**
	 * This method returns a list of available notes from the 88 key virtual
	 * piano, given an array of notes in a scale.
	 * @param filter The notes in the selected scale
	 * @return An array of Notes available from the 88 key virtual keyboard,
	 * 		   according to the selected scale.
	 */
	public Note[] filterAvailableNotes(String [] filter) {

		// An arraylist is used as we are unsure of the size required.
		ArrayList<Note> availableNotes = new ArrayList<>();

		for (int i = 1; i <= keyMap.size(); i++) {
			for (String aFilter : filter) {
				Note temp = keyMap.get(i);

				// check if the note letter matches
				char noteLetter = aFilter.charAt(0);

				// if the note from the method input is not a sharp, and the 
				// current key in the dictionary it note not a sharp
				if (!(aFilter.length() > 1) && !temp.isSharp()) {

					// check to see if the notes match
					if (temp.isNoteLetter(noteLetter) && temp.isPlayable()) {
						// add it to the available notes
						availableNotes.add(temp);
					}

					// check if the note from the input is a sharp, and the
					// current key in the dictionary is also a sharp
				} else if ((aFilter.length() > 1) && temp.isSharp()) {

					// if they have the same matching letter (ie. same note)	
					if (temp.isNoteLetter(noteLetter) && temp.isPlayable()) {
						// add it to the available notes
						availableNotes.add(temp);
					}
				}

			}
		}

		// Convert the arraylist into an array to pass back to the calling
		// method

		return availableNotes.toArray(new
				Note[availableNotes.size()]);
	}

	/**
	 * This method sets up the mapping for keys to reflect the 88 key piano
	 */
	private void initiateDictionary() {
		keyMap.put(1, new Note(1,	'A',	false,	0));
		keyMap.put(2, new Note(2,	'A',	true, 	0));
		keyMap.put(3, new Note(3,	'B',	false, 	0));
		keyMap.put(4, new Note(4,	'C',	false,	1));
		keyMap.put(5, new Note(5,	'C',	true,	1));
		keyMap.put(6, new Note(6,	'D', 	false,	1));
		keyMap.put(7, new Note(7,	'D', 	true,	1));
		keyMap.put(8, new Note(8,	'E', 	false,	1));
		keyMap.put(9, new Note(9,	'F', 	false,	1));
		keyMap.put(10, new Note(10,	'F', 	true,	1));
		
		keyMap.put(11, new Note(11,	'G',	false,	1));
		keyMap.put(12, new Note(12,	'G',	true, 	1));
		keyMap.put(13, new Note(13,	'A',	false, 	1));
		keyMap.put(14, new Note(14,	'A',	true,	1));
		keyMap.put(15, new Note(15,	'B',	false,	1));
		keyMap.put(16, new Note(16,	'C', 	false,	2));
		keyMap.put(17, new Note(17,	'C', 	true,	2));
		keyMap.put(18, new Note(18,	'D', 	false,	2));
		keyMap.put(19, new Note(19,	'D', 	true,	2));
		keyMap.put(20, new Note(20,	'E', 	false,	2));
		
		keyMap.put(21, new Note(21,	'F',	false,	2));
		keyMap.put(22, new Note(22,	'F',	true, 	2));
		keyMap.put(23, new Note(23,	'G',	false, 	2));
		keyMap.put(24, new Note(24,	'G',	true,	2));
		keyMap.put(25, new Note(25,	'A',	false,	2));
		keyMap.put(26, new Note(26,	'A', 	true,	2));
		keyMap.put(27, new Note(27,	'B', 	false,	2));
		keyMap.put(28, new Note(28,	'C', 	false,	3));
		keyMap.put(29, new Note(29,	'C', 	true,	3));
		keyMap.put(30, new Note(30,	'D', 	false,	3));
		
		keyMap.put(31, new Note(31,	'D',	true,	3));
		keyMap.put(32, new Note(32,	'E',	false, 	3));
		keyMap.put(33, new Note(33,	'F',	false, 	3));
		keyMap.put(34, new Note(34,	'F',	true,	3));
		keyMap.put(35, new Note(35,	'G',	false,	3));
		keyMap.put(36, new Note(36,	'G', 	true,	3));
		keyMap.put(37, new Note(37,	'A', 	false,	3));
		keyMap.put(38, new Note(38,	'A', 	true,	3));
		keyMap.put(39, new Note(39,	'B', 	false,	3));
		keyMap.put(40, new Note(40,	'C', 	false,	4));
		
		keyMap.put(41, new Note(41,	'C',	true,	4));
		keyMap.put(42, new Note(42,	'D',	false, 	4));
		keyMap.put(43, new Note(43,	'D',	true, 	4));
		keyMap.put(44, new Note(44,	'E',	false,	4));
		keyMap.put(45, new Note(45,	'F',	false,	4));
		keyMap.put(46, new Note(46,	'F', 	true,	4));
		keyMap.put(47, new Note(47,	'G', 	false,	4));
		keyMap.put(48, new Note(48,	'G', 	true,	4));
		keyMap.put(49, new Note(49,	'A', 	false,	4));
		keyMap.put(50, new Note(50,	'A', 	true,	4));
		
		keyMap.put(51, new Note(51,	'B',	false,	4));
		keyMap.put(52, new Note(52,	'C',	false, 	5));
		keyMap.put(53, new Note(53,	'C',	true, 	5));
		keyMap.put(54, new Note(54,	'D',	false,	5));
		keyMap.put(55, new Note(55,	'D',	true,	5));
		keyMap.put(56, new Note(56,	'E', 	false,	5));
		keyMap.put(57, new Note(57,	'F', 	false,	5));
		keyMap.put(58, new Note(58,	'F', 	true,	5));
		keyMap.put(59, new Note(59,	'G', 	false,	5));
		keyMap.put(60, new Note(60,	'G', 	true,	5));
		
		keyMap.put(61, new Note(61,	'A',	false,	5));
		keyMap.put(62, new Note(62,	'A',	true, 	5));
		keyMap.put(63, new Note(63,	'B',	false, 	5));
		keyMap.put(64, new Note(64,	'C',	false,	6));
		keyMap.put(65, new Note(65,	'C',	true,	6));
		keyMap.put(66, new Note(66,	'D', 	false,	6));
		keyMap.put(67, new Note(67,	'D', 	true,	6));
		keyMap.put(68, new Note(68,	'E', 	false,	6));
		keyMap.put(69, new Note(69,	'F', 	false,	6));
		keyMap.put(70, new Note(70,	'F', 	true,	6));
		
		keyMap.put(71, new Note(71,	'G',	false,	6));
		keyMap.put(72, new Note(72,	'G',	true, 	6));
		keyMap.put(73, new Note(73,	'A',	false, 	6));
		keyMap.put(74, new Note(74,	'A',	true,	6));
		keyMap.put(75, new Note(75,	'B',	false,	6));
		keyMap.put(76, new Note(76,	'C', 	false,	7));
		keyMap.put(77, new Note(77,	'C', 	true,	7));
		keyMap.put(78, new Note(78,	'D', 	false,	7));
		keyMap.put(79, new Note(79,	'D', 	true,	7));
		keyMap.put(80, new Note(80,	'E', 	false,	7));
		
		keyMap.put(81, new Note(81,	'F',	false,	7));
		keyMap.put(82, new Note(82,	'F',	true, 	7));
		keyMap.put(83, new Note(83,	'G',	false, 	7));
		keyMap.put(84, new Note(84,	'G',	true,	7));
		keyMap.put(85, new Note(85,	'A',	false,	7));
		keyMap.put(86, new Note(86,	'A', 	true,	7));
		keyMap.put(87, new Note(87,	'B', 	false,	7));
		keyMap.put(88, new Note(88,	'C', 	false,	8));
	}
}
