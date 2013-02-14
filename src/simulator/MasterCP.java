package simulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
* MasterCP.java
* 
* Draws the control panel for the {@link Master}, the bit in the upper left that
* sets controllers and resets/go/pause/quits.
* 
* Jun 19, 2012
* @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
*/

public class MasterCP {
	private String GWDisc = "GridWorldDiscrete";
	private String GWCont = "GridWorldContinuous";
	private String GWDesi = "GridWorldDesign";
	private String GWGrad = "Gradient";
	private String[] environments = {GWDisc, GWCont, GWDesi, GWGrad};
	private JComboBox environmentBox;
	private static JTextArea consoleText = null;
	private JTextField pObjectsField;
	JTextField ipaddrField;
	JTextField pObjectNumField;
	
	private Master master;
	
	public MasterCP(Master master) {
		this.master = master;
	}

	public static void error (String msg)
	{
		if (consoleText != null) {
			consoleText.append("ERR: " + msg + "\n");
		}
	}

	public static void status (String msg)
	{
		if (consoleText != null) {
			consoleText.append("STA: " + msg + "\n");
		}
	}
	
	// Master Text Console - for now not editable, just for displaying messages
	public JPanel makeTextConsole (int width) {
		consoleText = new JTextArea();
		consoleText.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(consoleText);
		scrollPane.setPreferredSize(new Dimension(width - 20, 80));
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);				    

		JPanel panel = new JPanel();
		panel.add(scrollPane);
		return panel;
	}
	
	public Environment getEnvironment() {
		Environment env;
		String selected = (String) environmentBox.getSelectedItem();
		
		if (selected.equalsIgnoreCase(GWDisc)) {
			env = new gridworld.environments.GridWorld(true);
		} else if (selected.equalsIgnoreCase(GWCont)) {
			env = new gridworld.environments.GridWorld(false);
		} else if (selected.equalsIgnoreCase(GWDesi)) {
			env = new gridworld.environments.GridWorldDesign();
		} else if (selected.equalsIgnoreCase(GWGrad)) {
			env = new gridworld.environments.Gradient();
		} else {
			error("Failed to create environment");
			return null;
		}
		
		return env;
	}
	
	public int readNumPObjects() {
		String numString = pObjectsField.getText().trim();
		int num = 0;
		// Should switch pObjectsField from a JTextField to a JFormattedTextField
		// but this takes a bit of work. Catching the exception is easier.
		try {
			num = Integer.parseInt(numString);
		} catch (NumberFormatException e) {
			System.out.println("Number? " + numString);
			error("Number of Plannable Objects must be an integer.");
		}
		return num;
	}
	
	JPanel controlPanel;
	private void initControlPanel(int numPObjects) {
		controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
		controlPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		// Select Environment
		JPanel inPanel = new JPanel();
		JLabel label = new JLabel("Env:");
		inPanel.add(label);
		environmentBox = new JComboBox(environments);
		inPanel.add(environmentBox);
		inPanel.setMaximumSize(inPanel.getPreferredSize());
		controlPanel.add(inPanel);

		// TODO: Fix!
		inPanel = new JPanel ();
		inPanel.add (new JLabel("# Plannable Objects:"));
		pObjectsField = new JTextField (2);
		pObjectsField.setText ("" + numPObjects);
		inPanel.add (pObjectsField);
		controlPanel.add (inPanel);
		
		// Make Controller Socket Connections
		inPanel = new JPanel();
		inPanel.setEnabled(false);
		inPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		inPanel.setLayout(new GridLayout(2,1));
		JPanel subPanel = new JPanel();
		subPanel.add(new JLabel("IP-Addr:"));
		ipaddrField = new JTextField (15);
		ipaddrField.setText ("localhost");
		subPanel.add (ipaddrField);
		inPanel.add (subPanel);
		subPanel = new JPanel ();
		subPanel.add (new JLabel ("Car#:"));
		pObjectNumField = new JTextField (2);
		pObjectNumField.setText ("0");
		subPanel.add (pObjectNumField);
		JButton controlB = new JButton ("Set controller");
		controlB.addActionListener (new ActionListener () {
			public void actionPerformed (ActionEvent a) { master.addController(
						ipaddrField, pObjectNumField); }
		} );
		subPanel.add (controlB);
		inPanel.add (subPanel);
		inPanel.setMaximumSize(inPanel.getPreferredSize());
		controlPanel.add (inPanel);

		// Go, Reset, Pause and Quit the Simulator
		inPanel = new JPanel();
		// Go
		JButton goButton = new JButton("Go");
		goButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) { master.go(); }
		} );
		inPanel.add(goButton);
		// Reset 
		// TODO: Check if reset is needed... probably
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) { master.reset(); }
		} );
		inPanel.add(resetButton);
		// Pause
		JButton pauseButton = new JButton("Pause");
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) { master.pause(); }
		} );
		inPanel.add(pauseButton);
		// Quit
		JButton quitButton = new JButton("Quit");
		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) { master.quit(); }
		} );
		inPanel.add(quitButton);
		inPanel.setMaximumSize(inPanel.getPreferredSize());
		controlPanel.add(inPanel);
	}
	
	JPanel mainPanel;
	public JPanel makeControlPanel(JPanel envSettings, int numPObjects) {
		if (controlPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
			this.initControlPanel(numPObjects);
		} else {
			mainPanel.removeAll();
		}
		mainPanel.add(controlPanel);
		mainPanel.add(envSettings);
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.validate();
		mainPanel.repaint();

		return mainPanel;
	}
}
