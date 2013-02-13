package simulator;

import java.util.ArrayList;

import gridworld.Util;

/**
 * Sensor.java
 * 
 * Represents a sensor that has a command channel and a response channel.
 * Also maintains an orientation so that it may move throughout the world.
 * 
 * Feb 7, 2013
 * @author James Marshall <jcmarsh (at) gmail.gwu.edu>
 *
 */
public abstract class Sensor implements simulator.PhysicsObject {
	// Current location, orientation.
	protected Orientation orientation;
	
	protected Channel<String> requestsToSensor;
	protected Channel<ArrayList<String>> responsesFromSensor;

	public Sensor(Channel<String> requestsToSensor, Channel<ArrayList<String>> responsesFromSensor) {
		this.requestsToSensor = requestsToSensor;
		this.responsesFromSensor = responsesFromSensor;
	}
	
	public Channel<String> getToChannel() {
		return requestsToSensor;
	}
	
	public Channel<ArrayList<String>> getFromChannel() {
		return responsesFromSensor;
	}
	
	@Override
	public void setOrientation(Orientation o) {
		orientation = o;
	}

	@Override
	public void updateOrientation(double dx, double dy, double dtheta,
			double dvelocity) {
		orientation.location.x += dx;
		orientation.location.y += dy;
		orientation.theta = Util.angleFix(orientation.theta + dtheta);
		orientation.velocity += dvelocity;
	}
	
	@Override
	public Orientation getOrientation() {
		return orientation;
	}
}
