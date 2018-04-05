package scales;

/**
 * This class is a wrapper for a 'scale' - defined by a root note and mode.
 * @author Lisa Liu-Thorrold
 *
 */
public class Scale {
	private final String rootNote;
	private final String mode;
	
	public Scale (String mode, String rootNote) {
		this.mode = mode;
		this.rootNote = rootNote;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result * mode.hashCode();
		result = prime * result * rootNote.hashCode();
		return result;
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Scale)) {
			return false;
		}
		
		Scale s = (Scale) obj;
		return (rootNote.equals(s.rootNote) && mode.equals(s.mode));
	}
	
	@Override
	public String toString() {return mode + " " + rootNote; }
	
	/*************************************************
	 * Getter methods
	 *************************************************/
	
	public String getRootNote() {
		return rootNote;
	}
	
}
