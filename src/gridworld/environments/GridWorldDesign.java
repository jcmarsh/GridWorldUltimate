package gridworld.environments;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import simulator.*;

/**
 * GridWorldDesign.java
 * 
 * A pseudo environment for making GridWorld maps.
 * 
 * Large chucks of code pulled from Rahul Simha's original GridWorld.java
 * 
 * May 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 */

public class GridWorldDesign implements simulator.Environment {

	GridWorldPanel gwPanel = new GridWorldPanel();
	//GridObject[][] grid;

	////////////////////////////////////////////////////////////////////////
	// Design variables
	JTextField fileField;
	boolean isDesign = false;
	int javaCellWidth, javaCellHeight;
	boolean firstPaint = false;

    public void setTime(double time) { }
	void designClear ()
	{
		gwPanel.computeGridLines();
		gwPanel.initGridAndBorder();
		gwPanel.repaint ();
	}

	void designBasic ()
	{
		designClear();
		gwPanel.basicGrid();
		gwPanel.repaint ();
	}
	
	@Override
	public void doDraw() {
		gwPanel.repaint();
	}

	@Override
	public JPanel getSettingsPanel() {
		JPanel panel = new JPanel ();

		panel.setLayout (new GridLayout(5,1));

		JPanel inPanel = new JPanel ();

		inPanel = new JPanel ();
		JButton resetB = new JButton ("Clear");
		resetB.addActionListener (
				new ActionListener () {
					public void actionPerformed (ActionEvent a)
					{
						designClear ();
					}
				}
				);
		inPanel.add (resetB);
		panel.add (inPanel);

		inPanel = new JPanel ();
		JButton basicB = new JButton ("Basic");
		basicB.addActionListener (
				new ActionListener () {
					public void actionPerformed (ActionEvent a)
					{
						designBasic ();
					}
				}
				);
		inPanel.add (basicB);
		panel.add (inPanel);

		inPanel = new JPanel ();
		inPanel.add (new JLabel("filename:"));
		fileField = new JTextField (10);
		fileField.setText ("scene.txt");
		inPanel.add (fileField);
		panel.add (inPanel);

		inPanel = new JPanel ();
		JButton saveB = new JButton ("Save");
		saveB.addActionListener (
				new ActionListener () {
					public void actionPerformed (ActionEvent a)
					{
						save ();
					}
				}
				);
		inPanel.add (saveB);
		panel.add (inPanel);


		inPanel = new JPanel ();
		JButton quitB = new JButton ("Quit ");
		quitB.addActionListener (
				new ActionListener () {
					public void actionPerformed (ActionEvent a)
					{
						System.exit(0);
					}
				}
				);
		inPanel.add (quitB);
		panel.add (inPanel);

		return panel;
	}
	
	void save ()
	{
		try {
			PrintWriter pw = new PrintWriter (new File ("scene.txt"));
			for (int i=0; i<GridWorldPanel.M; i++) {
				for (int j=0; j<GridWorldPanel.N; j++) {
					if (gwPanel.grid[i][j] != null) {
						pw.println (i + " " + j);
					}
				}
			}
			pw.close ();
		}
		catch (IOException e) {
			System.out.println (e);
		}
	}

	@Override
	public JPanel getViewPanel() {
		return gwPanel;
	}

	@Override
	public PlannableObject initNewPlannable(int index) {
		// Design is special: it has no plannable objects
		return null;
	}

	@Override
	public void reset() {
		designClear();
	}

	@Override
	public void init(Physics physics) {
		// Design has no need for physics!
	}
	@Override
	public boolean isDone() {
		// Our work is never done!
		return false;
	}
}