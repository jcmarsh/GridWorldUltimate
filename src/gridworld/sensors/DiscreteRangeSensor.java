package gridworld.sensors;

import gridworld.CarModel;
import gridworld.Cardinal;
import gridworld.Obstacle;
import gridworld.Target;
import gridworld.environments.GridWorldPanel;

import java.awt.Point;
import java.util.ArrayList;

import simulator.Sensor;
import simulator.channel.Channel;
import simulator.channel.ChannelM;

/**
 * DiscreteRangeSensor.java
 * 
 * This file is modified from Rahul Simha's original RangeSensor.java in GridWorld
 * 
 * Feb 19, 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 *
 */

public class DiscreteRangeSensor extends Sensor<String, Integer> {
	private GridWorldPanel gwPanel;

	private int gridx, gridy;
	private Cardinal discreteDirection;
	
	public DiscreteRangeSensor (GridWorldPanel gwPanel, Channel<String> requestsToSensor,
			Channel<String> responsesFromSensor, ChannelM<Integer> dataResponsesFromSensor)
	{
		super(requestsToSensor, responsesFromSensor, dataResponsesFromSensor);
		this.gwPanel= gwPanel;
	}
	
	public void nextStep(double deltT) {
		if (requestsToSensor.hasMessage()) {
			// action is one of: range, targetclose, targetvisible.
			String actionStr = requestsToSensor.getMessage();

			if (actionStr.equals("targetclose")) {
				targetClose();
			}
			else if (actionStr.equals("range")) {
				distance();
			}
			else if (actionStr.equals("targetvisible")) {
				scanTarget();
			}
			else {
				responsesFromSensor.setMessage("sensor=error");
			}
		}
	}
	
	private void targetClose() {
		String message;
		// See if a target is in a neighboring cell.
		boolean found = false;
		for (Cardinal c : Cardinal.values()) {
			Point location = GridWorldPanel.identifyCell(orientation.location);
			Point next = CarModel.computeNextCell(c, location);
			if (CarModel.validCell(next, gwPanel) &&
					(Target.class.isInstance(gwPanel.grid[next.x][next.y]))) {
				found = true;
			}
		}
		if (found) {
			message = "targetclose=true";
		}
		else {
			message = "targetclose=false";
		}
		responsesFromSensor.setMessage(message);
	}
	
	private void distance ()
	{
		ArrayList<Integer> msg = new ArrayList<Integer>();
		switch(discreteDirection) {
		case NORTH:
			msg.add(scanYObstacle(1));
		case SOUTH:
			msg.add(scanYObstacle(-1));
		case EAST:
			msg.add(scanXObstacle(1));
		case WEST:
			msg.add(scanXObstacle(-1));
		default:
			responsesFromSensor.setMessage("range=error");
		}
		dataResponsesFromSensor.setMessage(msg);
	}

	private void scanTarget()
	{
		String msg;
		switch(discreteDirection) {
		case NORTH:
			msg = "targetvisible=" + scanYTarget (1);
		case SOUTH:
			msg = "targetvisible=" + scanYTarget (-1);
		case EAST:
			msg = "targetvisible=" + scanXTarget (1);
		case WEST:
			msg = "targetvisible=" + scanXTarget (-1);
		default:
			msg = "targetvisible=error";
		}
		responsesFromSensor.setMessage(msg);
	}

	private int scanYObstacle(int increment)
	{
		int nexty = gridy + increment;
		while (true) {
			if ((nexty < 0) || (nexty >= GridWorldPanel.N)) {
				return nexty;
			}
			else if (gwPanel.grid[gridx][nexty] == null) {
				nexty += increment;
			}
			else if (Obstacle.class.isInstance(gwPanel.grid[gridx][nexty])) {
				return nexty;
			}
			else if (CarModel.class.isInstance(gwPanel.grid[gridx][nexty])) {
				return nexty;
			}
			else {
				nexty += increment;
			}
		} // endwhile
	}


	private int scanXObstacle(int increment)
	{
		int nextx = gridx + increment;
		while (true) {
			if ((nextx < 0) || (nextx >= GridWorldPanel.M)) {
				return nextx;
			}
			else if (gwPanel.grid[nextx][gridy] == null) {
				nextx += increment;
			}
			else if (Obstacle.class.isInstance(gwPanel.grid[nextx][gridy])) {
				return nextx;
			}
			else if (CarModel.class.isInstance(gwPanel.grid[nextx][gridy])) {
				return nextx;
			}
			else {
				nextx += increment;
			}
		} // endwhile
	}


	private boolean scanYTarget(int increment)
	{
		int nexty = gridy + increment;
		while (true) {
			if ((nexty < 0) || (nexty >= GridWorldPanel.N)) {
				return false;
			}
			else if (gwPanel.grid[gridx][nexty] == null) {
				nexty += increment;
			}
			else if (Obstacle.class.isInstance(gwPanel.grid[gridx][nexty])) {
				return false;
			}
			else if (CarModel.class.isInstance(gwPanel.grid[gridx][nexty])) {
				return false;
			}
			else if (Target.class.isInstance(gwPanel.grid[gridx][nexty])) {
				return true;
			}
			else {
				return false;
			}
		} // endwhile
	}


	private boolean scanXTarget(int increment)
	{
		int nextx = gridx + increment;
		while (true) {
			if ((nextx < 0) || (nextx >= GridWorldPanel.M)) {
				return false;
			}
			else if (gwPanel.grid[nextx][gridy] == null) {
				nextx += increment;
			}
			else if (Obstacle.class.isInstance(gwPanel.grid[nextx][gridy])) {
				return false;
			}
			else if (CarModel.class.isInstance(gwPanel.grid[nextx][gridy])) {
				return false;
			}
			else if (Target.class.isInstance(gwPanel.grid[nextx][gridy])) {
				return true;
			}
			else {
				return false;
			}
		} // endwhile
	}
}
