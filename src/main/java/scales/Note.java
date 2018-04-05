package scales;

/**
 * This class represents a 'Note' in the MIDIBlocks program
 * @author Lisa Liu-Thorrold
 *
 */
public class Note {
	private final char noteLetter;
	private final int keyNumber;
	private final boolean isSharpOrFlat;
	private final boolean isPlayable;
	private int color;
	private final int octave;
	
	private final int COLOR_GREY = 160;
	private final int COLOR_BLACK = 0;
	private final int COLOR_WHITE = 255;
	private final int LOWEST_PLAYABLE_KEY = 4;
	private final int HIGHEST_PLAYABLE_KEY = 87;
	

	/**
	 * This constructor determine the color of it's note by the given 
	 * parameters
	 * @param number Between 1 and 88 to presents the keys in a full keyboard
	 * @param noteLetter A-G
	 * @param isSharpOrFlat Whether the note is sharp of flat
	 * @param octave What octave the note is in
	 */
	public Note(int number, char noteLetter, boolean isSharpOrFlat, int octave) {
		this.keyNumber =  number;
		this.octave = octave;
		this.isSharpOrFlat = isSharpOrFlat;
		this.noteLetter = noteLetter;

		if (number < LOWEST_PLAYABLE_KEY || number  > HIGHEST_PLAYABLE_KEY) {
			this.isPlayable = false;
			this.color = COLOR_GREY; 
		} else {
			this.isPlayable = true;

			if (isSharpOrFlat) {
				this.color = COLOR_BLACK; 
			} else {
				this.color = COLOR_WHITE; 
			}
		}

	}

	/**
	 * This method is invoked when the mouse or keyboard letter is released,
	 * and restores the color of the note back to it's original color.
	 */
	public void restoreNoteColor() {
		if (keyNumber < LOWEST_PLAYABLE_KEY || keyNumber > 
				HIGHEST_PLAYABLE_KEY) {
			this.color = COLOR_GREY; 
		} else {
			if (isSharpOrFlat) {
				this.color = COLOR_BLACK;
			} else {
				this.color = COLOR_WHITE; 
			}
		}
	}

	@Override
	public String toString() {
		String isSharp;
		
		char nextNote = (char) (noteLetter + 1);
		
		if (noteLetter == 'G') {
			isSharp = String.valueOf(noteLetter) + "#" + octave + " " + 
					String.valueOf('A') + octave +  "b";
		} else {
			isSharp = String.valueOf(noteLetter) + "#" + octave + " " +
			String.valueOf(nextNote) + octave + "b";
		}
		
		String notSharp = String.valueOf(noteLetter) + octave;

		return isSharpOrFlat ? isSharp : notSharp;
	}
	
	/**
	 * Two notes if they have the same note number
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Note)) {
			return false;
		}
		Note s = (Note) obj; // the note to compare
		return keyNumber == s.getKeyNumber();
	}

	@Override
	public int hashCode() {
		final int prime = 31; // a prime for combining hash codes of fields
		int result = 1;
		result = prime * result + toString().hashCode();
		result = prime * result + keyNumber;
		return result;
	}
	
	
	/*************************************************
	 * Getter/ setter methods
	 *************************************************/
	
	public int getKeyNumber() { return keyNumber; }
	public int getColor() { return color; }
	public boolean isPlayable() { return isPlayable; }
	public void setNoteColor(int color) { this.color = color; }
	public boolean isNoteLetter (char character) { return character == noteLetter; }	
	public boolean isSharp() { return isSharpOrFlat; }
	
	/**
	 * Get the string representation of the note for constructing the virtual
	 * keyboard
	 * @return Alternative string representation of the note
	 */
	public String getNoteLetter() {
		char nextNote = (char) (noteLetter + 1);
		
		String isSharp;
		
		if (noteLetter == 'G') {
			isSharp = String.valueOf(noteLetter) +  "#/" +
					String.valueOf('A') + "b";
		} else {
			isSharp = String.valueOf(noteLetter) +  "#/" +  
			String.valueOf(nextNote) + "b";
		}
		
		String notSharp = String.valueOf(noteLetter);
		return isSharpOrFlat ? isSharp : notSharp;
	}
}
