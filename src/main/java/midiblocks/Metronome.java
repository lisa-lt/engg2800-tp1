package midiblocks;
import java.awt.Canvas;
import java.awt.Color;

import org.jfugue.player.Player;

/**
 * This class represents a Metronome which has a visual and audio 
 * component.
 * @author Lisa Liu-Thorrold
 *
 */
public class Metronome implements Runnable {
	
	private boolean metronomeMuted;
	private int count; //To keep count of the number of ticks. 
	private final int tempo; // BPM
	private final Canvas visualMetronome;
	private final Player player;
	
	private static final String REGULAR_LONG_NOTE = "A";
	private static final String TICK_LONG_NOTE = "D";
	private static final String REGULAR_MEDIUM_NOTE = "Ai";
	private static final String TICK_MEDIUM_NOTE = "Di";
	private static final String REGULAR_SHORT_NOTE = "At";
	private static final String TICK_SHORT_NOTE = "Dt";
	
	/**
	 * @param metronomeMuted - Whether the metronome is muted
	 * @param visualMetronome - The visual component of the metronome
	 * @param tempo - The beats per minute specified by the user
	 */
	public Metronome(boolean metronomeMuted, Canvas visualMetronome, int tempo) {
		this.metronomeMuted = metronomeMuted;
		this.visualMetronome = visualMetronome;
		count = 1;
		this.tempo = tempo;
		player = new Player();
	}
	
	/**
	 * This method is called upon with each beat of the metronome according
	 * to the bpm set by the user.
	 */
	@Override
    public void run() {
       if (metronomeMuted) {
    	   runMuteMetronome();
       } else {
    	   runMetronome();
       }
    }
    
    /** 
     * This method just runs the visual metronome. This method is called when
     * the user has set the metronome to mute.
     */
    private void runMuteMetronome() {
    	if (count > 3) {
    		visualMetronome.setBackground(Color.PINK);
    		count = 1;
    	} else {
    		visualMetronome.setBackground(Color.BLACK);
    		count++;
    	}	
    }
    
    /** 
     * This method run a visual an audio metronome.
	 * and call appropriately so timing is good.
     */
    private void runMetronome() {
    	
    	// slow tempo can play a long note
		if (tempo < 60) {
			if (count > 3) {
				visualMetronome.setBackground(Color.BLUE);
				player.play(REGULAR_LONG_NOTE);
				count = 1;
			} else {
				visualMetronome.setBackground(Color.BLACK);
				player.play(TICK_LONG_NOTE);
				count++;
			}	
			// play a quicker note
		} else if (tempo >= 60 && tempo <= 100) {
			if (count > 3) {
				visualMetronome.setBackground(Color.BLUE);
				player.play(REGULAR_MEDIUM_NOTE);
				count = 1;
			} else {
				visualMetronome.setBackground(Color.BLACK);
				player.play(TICK_MEDIUM_NOTE);
				count++;
			}
		} else {
			// play a very fast note
			if (count > 3) {
				visualMetronome.setBackground(Color.BLUE);
				player.play(REGULAR_SHORT_NOTE);
				count = 1;
			} else {
				visualMetronome.setBackground(Color.BLACK);
				player.play(TICK_SHORT_NOTE);
				count++;
			}
		}

    }
    
    /**
     * Sets the metronome to mute (ie. Only runs the visual metronome if
     * the parameter is true)
     * @param muteMetronome - Whether the metronome is set to mute by the user.
     */
    public void setMute(boolean muteMetronome) {
    	this.metronomeMuted = muteMetronome;
    }

 }