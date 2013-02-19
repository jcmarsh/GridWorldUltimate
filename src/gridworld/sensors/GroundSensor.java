package gridworld.sensors;

import gridworld.environments.GridWorldPanel;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import simulator.Channel;
import simulator.Sensor;

/**
 * GroundSensor.java
 *
 * Is able to sense the "ground" beneath the robot. The ground is just the
 * gradient stored in GridWorldPanel as a bitmap. Has 4 sensors, at the corners of
 * the robot.
 * 
 * Feb 15, 2013
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 *
 */

public class GroundSensor extends Sensor {
	GridWorldPanel gwPanel;
	public static int NUM_SENSORS = 4;
	private static double dist = 5;
	Node[] nodes = new Node[NUM_SENSORS];
	
	public GroundSensor(GridWorldPanel gwPanel, Channel<String> requestsToSensor, Channel<ArrayList<String>> responsesFromSensor) {
		super(requestsToSensor, responsesFromSensor);
		this.gwPanel = gwPanel;
		
		for (int i = 0; i < NUM_SENSORS; i++) {
			Point2D.Double offset = new Point2D.Double();
			double angle = i * ((2 * Math.PI) / NUM_SENSORS);
			offset.x = dist * Math.cos(angle);
			offset.y = dist * Math.sin(angle);
			nodes[i] = new Node(offset, this);
		}
	}

	@Override
	public void nextStep(double delT) {
		if (requestsToSensor.hasMessage()) {
			String actionStr = requestsToSensor.getMessage();
			if (actionStr.equalsIgnoreCase("ground")) {
				ArrayList<String> message = new ArrayList<String> ();
				for (int i = 0; i < NUM_SENSORS; i++) {
					message.add(nodes[i].getReadString(i));
				}
				
				responsesFromSensor.setMessage(message);
			} else {
				simulator.Master.error("Bad GroundSensor command: " + actionStr);
			}
		}
	}
	  
	private class Node {
		Point2D.Double offset;
		GroundSensor parent;
		
		Node(Point2D.Double offset, GroundSensor parent) {
			this.offset = offset;
			this.parent = parent;
		}
		
		public String getReadString(int index) {
			Point2D.Double locInWorld = new Point2D.Double();
			double theta = parent.orientation.theta;
			locInWorld.x = offset.x * Math.cos(theta) - offset.y * Math.sin(theta);
			locInWorld.x += parent.orientation.location.x;
			locInWorld.y = offset.x * Math.sin(theta) + offset.y * Math.cos(theta);
			locInWorld.y += parent.orientation.location.y;
			
			int result = parent.gwPanel.readGradient((int)locInWorld.x, (int)locInWorld.y);
			
			return "ground" + index + "=" + formatRGB(result);
		}
		
		private String formatRGB(int in) {
			int R = in << 8;
			R = R >>> 24;
			int G = in << 16;
			G = G >>> 24;
			int B = in << 24;
			B = B >>> 24;
			
			return R + "," + G + "," + B;
		}
	}
}
