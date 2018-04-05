package midiblocks;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;

import com.fazecast.jSerialComm.SerialPort;

import processingblocks.Arpeggiator;
import processingblocks.Gates;
import processingblocks.PitchShift;
import processingblocks.ProcessingBlock;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the entire application in a Graphical User Interface
 * @author Lisa Liu-Thorrold
 *
 */
public class MidiBlocksContainer extends EventEmitter{

	/* Constants */
	private static final int WINDOW_WIDTH = 1440;
	private static final int WINDOW_HEIGHT = 807;
	
	private static final String APPLICATION_NAME = "MIDIBlocks";


	/* GUI Combo boxes */
	private JComboBox<String> modeComboBox;
	private JComboBox<String> midiOutputComboBox;
	private JComboBox<String> processingBlocksComboBox;
	private JComboBox<String> MIDISourceComboBox;
	private JComboBox<String> arpeggiatorComboBox;
	private JComboBox<String> gatesComboBox;
	private JComboBox <String>rootNoteComboBox;

	/* GUI JButtons */
	private JButton saveMidiFileButton;
	private JButton startMidiPlaybackButton;
	private JButton stopMidiPlaybackButton;
	private JButton previewPlaybackButton;

	/* GUI JLabels */
	private JLabel pitchShiftLabel;
	private JLabel virtualKeyboardLabel;
	private JLabel gatesModeLabel;
	private JLabel gatesNotesToReleaseLabel;

	/* GUI Textfields */
	private JTextField tempoTextField;
	private JTextField pitchShiftTextField;
	private JTextField gatesTextField;

	/* Combobox model */
	private DefaultComboBoxModel<String> rootNoteComboBoxModel;
	private DefaultComboBoxModel<String> modeComboBoxModel;
	private DefaultComboBoxModel<String> midiSourceComboBoxModel;
	private DefaultComboBoxModel<String> midiOutputComboBoxModel;

	/* Mute checkbox for metronome */
	private JCheckBox muteCheckBox;

	/* Visual metronome is a square canvas */
	private Canvas metronome;

	/* Container for the virtual keyboard */
	private JPanel keyboardPanel;

	/* Model for the list of processing blocks */
	private DefaultListModel<ProcessingBlock> processingBlockListModel;

	/* GUI List component representing the processing blocks */
	private JList<ProcessingBlock> processingBlockList;

	/* The J frame for the entire application */
	private JFrame frame;
	
	/* Pop up menu that is displayed when the user wants to change the
	 * parameter of a processing block */
	private JPopupMenu processingBlockPopupMenu = new JPopupMenu();  

    
    /**
     * Creates and initialises the graphical user interface.
     */
    public MidiBlocksContainer() {
    	initComponents();
    }
    

    /**
     * This method initialises, and places all the components required on the
     * graphical user face. Action listeners are placed on components of 
     * interest
     */
    private void initComponents() {   	
    	frame = new JFrame(APPLICATION_NAME);

    	frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    	
    	/* Set default operation to exit on close */
    	frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    	frame.getContentPane().setLayout(null);
    	
    	/* Add the menu items */
    	JMenuBar menuBar = new JMenuBar();    	
    	frame.setJMenuBar(menuBar);

    	/* Add file menu */
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        
        /* Add a load configuration item in the File menu */
        JMenuItem loadConfigMenuItem = new JMenuItem("Load Configuration");
        loadConfigMenuItem.addActionListener(event -> 
        		this.emit("loadConfiguration"));
        fileMenu.add(loadConfigMenuItem);
    	
        /* Add a save configuration item in the File menu */
        JMenuItem saveConfigMenuItem = new JMenuItem("Save Configuration");
        saveConfigMenuItem.addActionListener(event -> 
        		this.emit("saveConfiguration"));
        fileMenu.add(saveConfigMenuItem);
        
        /*************************************************
    	 * Add all the JLabels onto the GUI
    	 *************************************************/
    	JLabel MIDIBlocksLabel = new JLabel("MIDIBlocks");
    	MIDIBlocksLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 40));
    	MIDIBlocksLabel.setBounds(497, 17, 204, 71);
    	frame.getContentPane().add(MIDIBlocksLabel);
    	
    	JLabel processingBlocksLabel = new JLabel("Processing Blocks");
    	processingBlocksLabel.setFont(new Font("Helvetica Neue", 
    	        Font.PLAIN, 20));
    	processingBlocksLabel.setBounds(520, 77, 170, 32);
    	frame.getContentPane().add(processingBlocksLabel);
    	
    	JLabel metronomeLabel = new JLabel("Metronome:");
    	metronomeLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
    	metronomeLabel.setBounds(1167, 27, 81, 21);
    	frame.getContentPane().add(metronomeLabel);
    	
    	JLabel MIDISourceLabel = new JLabel("MIDI Source:");
    	MIDISourceLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
    	MIDISourceLabel.setBounds(1167, 85, 96, 21);
    	frame.getContentPane().add(MIDISourceLabel);
    	
    	JLabel bpmLabel = new JLabel("bpm");
    	bpmLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 12));
    	bpmLabel.setBounds(1266, 166, 31, 21);
    	frame.getContentPane().add(bpmLabel);
    	
    	JLabel setScaleLabel = new JLabel("Set Scale");
    	setScaleLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
    	setScaleLabel.setBounds(1225, 251, 64, 21);
    	frame.getContentPane().add(setScaleLabel);
    	
    	JLabel rootNoteLabel = new JLabel("Root Note:");
    	rootNoteLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 12));
    	rootNoteLabel.setBounds(1167, 270, 64, 21);
    	frame.getContentPane().add(rootNoteLabel);
    	
    	JLabel modeLabel = new JLabel("Mode:");
    	modeLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 12));
    	modeLabel.setBounds(1275, 270, 64, 21);
    	frame.getContentPane().add(modeLabel);
    	
    	JLabel tempoLabel = new JLabel("Tempo: ");
    	tempoLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
    	tempoLabel.setBounds(1167, 140, 57, 21);
    	frame.getContentPane().add(tempoLabel);
    	
    	JLabel MIDIOutputLabel = new JLabel("MIDI Output:");
    	MIDIOutputLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
    	MIDIOutputLabel.setBounds(1167, 331, 96, 21);
    	frame.getContentPane().add(MIDIOutputLabel);
    	
    	JLabel processingBlocksLabel2 = new JLabel("Processing Blocks");
    	processingBlocksLabel2.setFont(new Font("Helvetica Neue", 
    			Font.PLAIN, 20));
    	processingBlocksLabel2.setBounds(60, 77, 170, 32);
    	frame.getContentPane().add(processingBlocksLabel2);
    	
    	/*************************************************
    	 * Add all the ComboBoxes onto the GUI
    	 *************************************************/
    	
		midiSourceComboBoxModel = new DefaultComboBoxModel<>();
    	MIDISourceComboBox = new JComboBox<>(midiSourceComboBoxModel);
    	MIDISourceComboBox.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
    	MIDISourceComboBox.setBounds(1161, 108, 252, 27);
    	MIDISourceComboBox.addItem("Virtual Keyboard");
    	MIDISourceComboBox.addItem("MIDI file");
		loadMidiDriverSources();
    	MIDISourceComboBox.setSelectedIndex(-1);
    	MIDISourceComboBox.addActionListener(event -> 
    			this.emit("MIDISourceSelected"));
		Component[] components = MIDISourceComboBox.getComponents(); {
			for (Component component : components) {
				component.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent event) {
						(new Thread(MidiBlocksContainer.
								this::loadMidiDriverSources)).start();
					}
				});
			}
		}
    	frame.getContentPane().add(MIDISourceComboBox);

		midiOutputComboBoxModel = new DefaultComboBoxModel<>();
		midiOutputComboBox = new JComboBox<>(midiOutputComboBoxModel);
		midiOutputComboBox.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
		midiOutputComboBox.setBounds(1161, 350, 252, 27);
		loadMidiOutputs();
		midiOutputComboBox.setSelectedIndex(-1);
		Component[] comp = midiOutputComboBox.getComponents(); {
			for (Component aComp : comp) {
				aComp.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent event) {
						(new Thread(MidiBlocksContainer.
								this::loadMidiOutputs)).start();
					}
				});
			}
		}
		midiOutputComboBox.addActionListener(event -> 
		this.emit("MIDIOutputSelected"));
		frame.getContentPane().add(midiOutputComboBox);

    	processingBlocksComboBox = new JComboBox<>();
    	processingBlocksComboBox.addActionListener(event -> 
    	this.emit("changeProcessingBlock"));
    	processingBlocksComboBox.setFont(new Font("Helvetica Neue", 
    			Font.PLAIN, 13));
        processingBlocksComboBox.addItem("Arpeggiator");
        processingBlocksComboBox.addItem("Chordify");
        processingBlocksComboBox.addItem("Gates");
        processingBlocksComboBox.addItem("Monophonic");
        processingBlocksComboBox.addItem("Pitch Shift");
    	processingBlocksComboBox.setBounds(42, 121, 201, 27);
        processingBlocksComboBox.setSelectedIndex(-1);
    	frame.getContentPane().add(processingBlocksComboBox);
    	
    	rootNoteComboBoxModel = new DefaultComboBoxModel<>();
    	rootNoteComboBox = new JComboBox<>(rootNoteComboBoxModel);
    	rootNoteComboBox.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
    	rootNoteComboBox.setBounds(1161, 292, 96, 27);
    	frame.getContentPane().add(rootNoteComboBox);
    	
    	modeComboBoxModel = new DefaultComboBoxModel<>();
    	modeComboBox = new JComboBox<>(modeComboBoxModel);
    	modeComboBox.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
    	modeComboBox.setBounds(1266, 292, 147, 27);
    	frame.getContentPane().add(modeComboBox);
    	
    	/*************************************************
    	 * Add all the TextFields onto the GUI
    	 *************************************************/
    	tempoTextField = new JTextField();
    	tempoTextField.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
    	tempoTextField.setBounds(1167, 162, 87, 28);
    	frame.getContentPane().add(tempoTextField);
    	tempoTextField.setColumns(10);
    	
    	/*************************************************
    	 * Add all the Buttons onto the GUI
    	 *************************************************/
    	JButton loadScalesButton = new JButton("Load Scales");
    	loadScalesButton.setBounds(1167, 213, 117, 29);
    	loadScalesButton.addActionListener(event -> this.emit("loadScales"));
    	frame.getContentPane().add(loadScalesButton);
    	
    	JButton clearConfigButton = new JButton("Clear");
    	clearConfigButton.setBounds(1161, 383, 87, 29);
    	clearConfigButton.addActionListener(event -> 
    			this.emit("clearConfigurations"));
    	frame.getContentPane().add(clearConfigButton);
    	
    	JButton setConfigButton = new JButton("Set");
    	setConfigButton.setBounds(1275, 383, 87, 29);
    	setConfigButton.addActionListener(event -> 
    			this.emit("setConfigurations"));
    	frame.getContentPane().add(setConfigButton);
    	
    	JButton clearProcessingBlockButton = new JButton("Clear");
    	clearProcessingBlockButton.setBounds(44, 267, 87, 29);
    	clearProcessingBlockButton.addActionListener(event -> 
    			this.emit("clearProcessingBlock"));
    	frame.getContentPane().add(clearProcessingBlockButton);
    	
    	JButton addProcessingBlockButton = new JButton("Add");
    	addProcessingBlockButton.setBounds(143, 267, 87, 29);
    	addProcessingBlockButton.addActionListener(event -> 
    			this.emit("addProcessingBlock"));
    	frame.getContentPane().add(addProcessingBlockButton);
    	
    	JButton shiftUpButton = new JButton("Shift Up");
    	shiftUpButton.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
    	shiftUpButton.setBounds(976, 120, 113, 29);
    	shiftUpButton.addActionListener(event -> 
    			this.emit("shiftProcessingBlockUp"));
    	frame.getContentPane().add(shiftUpButton);
    	
    	JButton shiftDownButton = new JButton("Shift Down");
    	shiftDownButton.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
    	shiftDownButton.setBounds(976, 163, 113, 29);
    	shiftDownButton.addActionListener(event -> 
    			this.emit("shiftProcessingBlockDown"));
    	frame.getContentPane().add(shiftDownButton);
    	
    	JButton deleteProcessingBlockButton = new JButton("Delete");
    	deleteProcessingBlockButton.setFont(new Font("Helvetica Neue", 
    			Font.PLAIN, 13));
    	deleteProcessingBlockButton.setBounds(976, 204, 113, 29);
    	deleteProcessingBlockButton.addActionListener(event -> 
    			this.emit("deleteProcessingBlock"));
    	frame.getContentPane().add(deleteProcessingBlockButton);
    	
    	JButton clearProcessingBlockListButton = new JButton("Clear");
    	clearProcessingBlockListButton.setFont(new Font("Helvetica Neue", 
    			Font.PLAIN, 13));
    	clearProcessingBlockListButton.setBounds(976, 248, 113, 29);
    	clearProcessingBlockListButton.addActionListener(event -> 
    			this.emit("clearAllProcessingBlocks"));
    	frame.getContentPane().add(clearProcessingBlockListButton);
    	
    	JButton undoButton = new JButton("Undo");
    	undoButton.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
    	undoButton.setBounds(976, 291, 113, 29);
    	undoButton.addActionListener(event -> this.emit("undo"));
    	frame.getContentPane().add(undoButton);
   
    	
      	JButton setTempoButton = new JButton("Set Tempo");
    	setTempoButton.setBounds(1297, 163, 108, 29);
    	setTempoButton.addActionListener(event -> this.emit("setTempo"));
    	frame.getContentPane().add(setTempoButton);
    	
    	/*************************************************
    	 * Add other miscellaneous components on the GUI
    	 *************************************************/
    	
    	/* Mute check box for metronome */
    	muteCheckBox = new JCheckBox("Mute");
    	muteCheckBox.addActionListener(event -> this.emit("mutePressed"));
    	muteCheckBox.setFont(new Font("Helvetica Neue", Font.PLAIN, 12));
    	muteCheckBox.setBounds(1177, 50, 64, 23);
		muteCheckBox.setSelected(true);
		frame.getContentPane().add(muteCheckBox);
    	
		/* A visual separator for the GUI components */
    	JSeparator separator = new JSeparator();
    	separator.setOrientation(SwingConstants.VERTICAL);
    	separator.setBounds(1113, 124, 12, 212);
    	frame.getContentPane().add(separator);
    	
    	/* A popup menu to change the parameter configurations */
    	processingBlockPopupMenu = new JPopupMenu();
    	JMenuItem changeParameter = new JMenuItem(
    			"Change Processing Block parameter");
    	changeParameter.addActionListener(event -> 
    			this.emit("changeProcessingBlockParameter"));
    	processingBlockPopupMenu.add(changeParameter);
    	
    	/* The list of processing blocks */
    	processingBlockListModel = new DefaultListModel<>();
    	processingBlockList = new JList<>(processingBlockListModel);
    	JScrollPane scrollPane = new JScrollPane(processingBlockList);
    	scrollPane.setBounds(306, 124, 672, 209);
    	// Add a right click listener to enable the user to change the 
    	// parameter of the processing block if it is possible.
    	processingBlockList.addMouseListener(new MouseAdapter() {
    		 public void mousePressed(MouseEvent e) {
    			 	// check if the mouse event was a right click
    		        if ( SwingUtilities.isRightMouseButton(e) ) {       	
    		        	checkValidProcessingBlock(e);            
    		        }
    		    }
    	});  		       
    	frame.getContentPane().add(scrollPane);
    	
    	/* JPanel for the virtual keyboard */
     	keyboardPanel = new JPanel();
    	keyboardPanel.setBounds(60, 442, 1325, 294);
    	frame.getContentPane().add(keyboardPanel);
    	
    	/* Visual Metronome */
    	metronome = new Canvas();
    	metronome.setBackground(Color.BLACK);
    	metronome.setBounds(1254, 50, 23, 21);
    	frame.getContentPane().add(metronome);
    	
    	
    	/*************************************************
    	 * The following components are not initially 
    	 * visible, they will be visible when certain
    	 * conditions are met.
    	 *************************************************/
    	
    	/* The pitch shift text field is only visible when the user is adding
    	 *  a pitch shift processing block.
    	 */
        pitchShiftTextField = new JTextField();
    	pitchShiftTextField.setBounds(75, 229, 134, 28);
    	frame.getContentPane().add(pitchShiftTextField);
        pitchShiftTextField.setColumns(10);
        pitchShiftTextField.setVisible(false);

        /* The pitch shift label is only visible when the user is adding
    	 *  a pitch shift processing block.
    	 */
        pitchShiftLabel = new JLabel("Amount to shift pitch by:");
    	pitchShiftLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 12));
    	pitchShiftLabel.setBounds(75, 207, 145, 21);
        frame.getContentPane().add(pitchShiftLabel);
    	pitchShiftLabel.setVisible(false);
    	
    	/* The Gates text field is only visible when the user is adding a gates
    	 * processing block.
    	 */
    	gatesTextField = new JTextField();
    	gatesTextField.setBounds(75, 229, 134, 28);
    	frame.getContentPane().add(gatesTextField);
    	gatesTextField.setColumns(10);
    	gatesTextField.setVisible(false);
    	
    	/*
    	 * The gates label is only visible when the user is adding a gates 
    	 * processing block
    	 */
    	gatesModeLabel = new JLabel("Gates Mode:");
    	gatesModeLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 12));
    	gatesModeLabel.setBounds(95, 160, 81, 21);
        frame.getContentPane().add(gatesModeLabel);
        gatesModeLabel.setVisible(false);
        
    	gatesNotesToReleaseLabel = new JLabel("Notes to release per tick:");
    	gatesNotesToReleaseLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 12));
    	gatesNotesToReleaseLabel.setBounds(78, 213, 131, 21);
        frame.getContentPane().add(gatesNotesToReleaseLabel);
        gatesNotesToReleaseLabel.setVisible(false);
    	
    	
    	/* The virtual keyboard label is only visible when the user has 
    	 * selected the virtual keyboard as MIDI input into this program
    	 */
    	virtualKeyboardLabel = new JLabel("Virtual Keyboard");
		virtualKeyboardLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 20));
		virtualKeyboardLabel.setBounds(482, 399, 170, 32);
		virtualKeyboardLabel.setVisible(false);
		frame.getContentPane().add(virtualKeyboardLabel);

		/* The arpeggiator combo box is only visible when the user is adding
    	 *  an arpeggiator processing block.
    	 */
        arpeggiatorComboBox = new JComboBox<>();
    	arpeggiatorComboBox.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
    	arpeggiatorComboBox.setBounds(42, 164, 201, 27);
    	arpeggiatorComboBox.addItem("Ascending Scale");
    	arpeggiatorComboBox.addItem("Descending Scale");
    	arpeggiatorComboBox.addItem("Ping pong");
    	arpeggiatorComboBox.addItem("Random");
    	frame.getContentPane().add(arpeggiatorComboBox);
    	arpeggiatorComboBox.setVisible(false);
    	
    	/* The gates combo box is only visible when the user is adding
    	 *  an gates processing block.
    	 */
        gatesComboBox = new JComboBox<>();
        gatesComboBox.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
        gatesComboBox.setBounds(42, 180, 201, 27);
        gatesComboBox.addItem("Queue");
        gatesComboBox.addItem("First Hold");
        gatesComboBox.addItem("Last Hold");
    	frame.getContentPane().add(gatesComboBox);
    	gatesComboBox.setVisible(false);
    	
    	/**
    	 * The save midi file button is only visible when the user has stopped
    	 * playback, and processing has started.
    	 */
    	saveMidiFileButton = new JButton("Save MIDI File");
    	saveMidiFileButton.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
    	saveMidiFileButton.setBounds(861, 349, 117, 29);
    	saveMidiFileButton.addActionListener(event -> 
    			this.emit("saveMidiFile"));
    	frame.getContentPane().add(saveMidiFileButton); 
    	saveMidiFileButton.setVisible(false);
		
    	/**
    	 * The start midi playback button is only visible when a valid 
    	 * MIDI input and valid MIDI output has been selected.
    	 */
    	startMidiPlaybackButton = new JButton("Start MIDI Playback");
    	startMidiPlaybackButton.setFont(new Font("Helvetica Neue", 
    			Font.PLAIN, 13));
    	startMidiPlaybackButton.setBounds(304, 349, 159, 29);
    	startMidiPlaybackButton.addActionListener(event -> 
    			this.emit("startMidiPlayBack"));
    	frame.getContentPane().add(startMidiPlaybackButton); 
    	startMidiPlaybackButton.setVisible(false);
    	
    	/**
    	 * The stop midi playback button is only visible when MIDI playback
    	 * has started.
    	 */
    	stopMidiPlaybackButton = new JButton("Stop MIDI Playback");
    	stopMidiPlaybackButton.setFont(new Font("Helvetica Neue", 
    			Font.PLAIN, 13));
    	stopMidiPlaybackButton.setBounds(482, 349, 155, 29);
    	stopMidiPlaybackButton.addActionListener(event -> 
    			this.emit("stopMidiPlayBack"));
    	frame.getContentPane().add(stopMidiPlaybackButton); 
    	stopMidiPlaybackButton.setVisible(false);
    	
    	previewPlaybackButton = new JButton("Preview MIDI Playback");
    	previewPlaybackButton.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
    	previewPlaybackButton.setBounds(664, 349, 170, 29);
    	previewPlaybackButton.addActionListener( event -> 
    			this.emit("previewPlayback"));
    	frame.getContentPane().add(previewPlaybackButton);
    	previewPlaybackButton.setVisible(false);
    	
    }
    

    /*************************************************
	 *  Getter/setter methods
	 *************************************************/
    
    public JComboBox<String> getMidiSources() { return MIDISourceComboBox; } 
    public JTextField getTempo() { return tempoTextField; }
	public JLabel getPitchShiftLabel() { return pitchShiftLabel ; }
	public JLabel getGatesModeLabel() { return gatesModeLabel; }
	public JLabel getGatesReleaseLabel() { return gatesNotesToReleaseLabel; }
	public JButton getSaveMidiFileButton() { return saveMidiFileButton; }
	public Canvas getMetronomeCanvas() { return metronome; }
	public JLabel getVirtualKeyboardLabel() { return virtualKeyboardLabel; }
	public JPanel getKeyboardPanel() { return keyboardPanel; }
	public JCheckBox getMuteCheckBox() { return muteCheckBox; }
	public JTextField getPitchShiftField() { return pitchShiftTextField; }
	public JTextField getGatesTextField() { return gatesTextField; }
    public JComboBox<String> getMidiOutputs() { return midiOutputComboBox; }
    public JComboBox<String> getRootNoteComboBox() { return rootNoteComboBox; }
    public JComboBox<String> getModeComboBox() { return modeComboBox; }
    public JComboBox<String> getGatesComboBox() { return gatesComboBox; }
  
    public JList<ProcessingBlock> getProcessingBlockListBox() { 
    	return processingBlockList; 
    }
    
    public DefaultListModel<ProcessingBlock> getProcessingBlockList() {
		return processingBlockListModel;
	}
    
    public DefaultComboBoxModel<String> getRootNoteModel() { 
    	return rootNoteComboBoxModel; 
    }
    
    public DefaultComboBoxModel<String> getModeModel() { 
    	return modeComboBoxModel; 
    }
  
    public JComboBox<String> getProcessingBlockToAdd() { 
    	return processingBlocksComboBox; 
    }

	public JComboBox<String> getArpeggiatorType() { 
		return arpeggiatorComboBox; 
	}

	public JButton getStartMidiPlaybackButton() { 
		return startMidiPlaybackButton; 
	}
	
	public JButton getStopMidiPlaybackButton() { 
		return stopMidiPlaybackButton; 
	}
	
	public JButton getPreviewPlaybackButton() {
		return previewPlaybackButton;
	}
	
    
	/*************************************************
	 *  Helper/ auxillary methods.
	 *************************************************/
	/**
	 * This method checks to see whether a valid proccessing block has
	 * been clicked during a mouse event
	 * @param e - The right click mouse event
	 */
	private void checkValidProcessingBlock(MouseEvent e) {
    	// Get the processing block that it was clicked out
    	JList<?> list = (JList<?>)e.getSource();
        int row = list.locationToIndex(e.getPoint());
        if (row > -1 && list.getCellBounds(row, row).
                contains(e.getPoint())) {
    	
        	list.setSelectedIndex(row);
        	
        	// Check if the processing block's parameters can
        	// be changed
        	if((processingBlockListModel.get(row) instanceof PitchShift)
        			|| (processingBlockListModel.get(row) instanceof Arpeggiator
        			|| processingBlockListModel.get(row) instanceof Gates)) {
        	 	processingBlockPopupMenu.show(e.getComponent(), 
        	 			e.getX(), e.getY());
        	}
        		
        }
	}
     
	/**
	 * This method allows the controller to display a message dialog
	 * to the user
	 * @param message - The message to display
	 */
	public void showMessageDialog(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
	
	/**
	 * This method displays an input dialog to allow the user to change the
	 * pitch on the pitch processing block
	 * @return The new pitch as selected by the user. 
	 * 		   
	 */
	public Integer showPitchInputDialog() {
		int pitch;
		Integer pitchInteger = null;
		
		String result = JOptionPane.showInputDialog("Enter a new pitch:");
		
		// Nothing was entered by the user
		if( result == null ) {
			return null;
		}
		
		try {
			pitch = Integer.parseInt(result);
			pitchInteger = pitch;
		} catch (Exception e) {
			showMessageDialog("Pitch must be a valid integer");	
		}
		
		return pitchInteger;
	}
	
	/**
	 * This method displays an input dialog to allow the user to change their
	 * current arpeggiator pattern.
	 * @return New arpeggiator pattern as selected by the user. If nothing was
	 * 		   selected, or cancelled, then return null
	 */
	public String showArpeggiatorInputDialog() {
		Object[] possibilities = {"Ascending Scale", "Descending Scale", 
				"Ping pong", "Random"};

		return (String) JOptionPane.showInputDialog(frame,
			        "Select a new type of arpeggiator pattern: ",
			        "Change Arpeggiator Pattern",
			        JOptionPane.QUESTION_MESSAGE,
			        null,
			        possibilities,
			        -1);
	}
	
	/**
	 * This method displays an input dialog to allow the user to select the
	 * usb device that they would like to connect to.
	 * @return the usb device that was selected by the user.
	 */
	public String showUsbOutputDialog() {
		
		SerialPort[] availablePorts = getAvailablePorts();
		
		List<String> optionList = new ArrayList<>();
		
		for (SerialPort serialPort: availablePorts) {
			optionList.add(serialPort.getSystemPortName());
		}
		
		Object[] options = optionList.toArray();

		return (String) JOptionPane.showInputDialog(frame,
			        "Select the USB device to connect to: ",
			        "Select USB Output device",
			        JOptionPane.QUESTION_MESSAGE,
			        null,
			        options,
			        -1);
	}
    
	/**
	 * This method shows the GUI 
	 */
    public void run() {
		frame.setVisible(true);
    }

    /**
     * This method add the available MIDI outputs to the GUI combo
     * box for user selection (includes .mid file, and usb devices that
     * are connected).
     */
	private void loadMidiOutputs() {

		midiOutputComboBoxModel.removeAllElements();

		midiOutputComboBoxModel.addElement(".mid");
		
		SerialPort[] listOfPorts = getAvailablePorts();

		for (SerialPort sp : listOfPorts) {
			String portName = sp.getSystemPortName();
			if (midiOutputComboBoxModel.getIndexOf(portName) == -1) {
				midiOutputComboBoxModel.addElement(portName);
			}
		}
		
		if (getAvailablePorts().length > 0) {
			String both = ".mid and USB";
			if(midiOutputComboBoxModel.getIndexOf(both) == -1) {
				midiOutputComboBoxModel.addElement(both);
			}
		}
	}

	/**
	 * This method gets the available devices that are available for serial
	 * communication (detects AVR)
	 * @return Available ports for serial communication
	 */
	private SerialPort[] getAvailablePorts() {
		ArrayList<SerialPort> availablePorts = new ArrayList<>();

		SerialPort[] listOfPorts = SerialPort.getCommPorts();

		for (SerialPort serialPort: listOfPorts) {
			// port is not open
			if (!serialPort.isOpen()) {
				// open the port
				if (serialPort.openPort()) {
					// add to list of ports
					availablePorts.add(serialPort);
					serialPort.closePort();
				}
			}
		}
		return availablePorts.toArray(new SerialPort[availablePorts.size()]);
	}


	/**
	 * This method loads all the MIDI devices that are connected to the system
	 * level MIDI driver.
	 */
	private void loadMidiDriverSources() {
		MidiDevice device;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

		for (MidiDevice.Info info : infos) {
			try {
				device = MidiSystem.getMidiDevice(info);

				// Test to see if the midi device can be a midi source
				// The value is -1 if the it can have unlimited transmitters
				// or >  1, if there are a fixed number of transmitters.
				if (device.getMaxTransmitters() != 0) {

					// See if we can open and close the device without error.
					device.open();
					device.close();
					String deviceName = device.getDeviceInfo().toString();

					// If the Midi source combo box doesn't have the
					// midi device, then add it.
					if (midiSourceComboBoxModel.getIndexOf(deviceName) == -1) {
						midiSourceComboBoxModel.addElement(deviceName);
					}
				}

			} catch (MidiUnavailableException e) {
				// do nothing
				return;
			}

		}
	}
}
