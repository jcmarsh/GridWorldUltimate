package gridworld.environments;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import gridworld.*;
import simulator.*;


/**
 * Gradient.java
 * An environment with a 2D gradient to be used in the boundary estimation problem.
 * 
 * February 2013
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 */
public class Gradient implements simulator.Environment {

	GridWorldPanel gwPanel = new GridWorldPanel();
	Physics physics;
	
	private String[] noiseLevels = {"zero-noise", "low-noise","high-noise"};
	public static int noiseLevel = 0;
	private String[] driveTypes = {"velocity-model ", "accel-model"};
	public static boolean accModel = false;
	
	JComboBox noiseBox, driveBox;
	
	boolean hasStarted = false;
	
	public Gradient() {
		GridWorld.isDiscrete = false;
		gwPanel.initGradient();
	}
	
	@Override
	public JPanel getSettingsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(10,1));
		
		JPanel inPanel = new JPanel();
		noiseBox = new JComboBox (noiseLevels);
		inPanel.add (noiseBox);
		panel.add (inPanel);
		
		inPanel = new JPanel ();
		driveBox = new JComboBox (driveTypes);
		inPanel.add (driveBox);
		panel.add (inPanel);
		
		inPanel = new JPanel ();
		JButton resetB = new JButton ("Reset");
		resetB.addActionListener (
				new ActionListener () {
					public void actionPerformed (ActionEvent a)
					{
						reset ();
					}
				}
				);
		inPanel.add (resetB);
		panel.add (inPanel);
		
		return panel;
	}

	@Override
	public JPanel getViewPanel() {
		return gwPanel;
	}

	@Override
	public void doDraw() {
		gwPanel.repaint();
	}

	@Override
	public void setTime(double time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PlannableObject initNewPlannable(int index) {
		Car c = new Car(index, gwPanel, accModel);
		
		Orientation newOrien = new Orientation();
		newOrien.location.x = index * 20 + 10;
		newOrien.location.y = 10;
		newOrien.theta = 0;
		newOrien.velocity = 0;
		
		c.initPhysicsChannel(physics, newOrien);
		return c;
	}

	@Override
	public void reset() {
		simulator.Master.status("Reseting GridWorld");
		
		hasStarted = false;
		
		gwPanel.computeGridLines();
		gwPanel.initGridAndBorder();
		gwPanel.repaint();
	}

	@Override
	public void init(Physics physics) {
		this.physics = physics;
		
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GridWorldPanel getGridWorldPanel() {
		return gwPanel;
	}

}
