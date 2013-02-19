package simulator;

import simulator.channel.Channel;

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
public abstract class Sensor<T, F> implements simulator.PhysicsObject {
	// Current location, orientation.
	protected Orientation orientation;
	
	protected Channel<T> requestsToSensor;
	protected Channel<F> responsesFromSensor;

	public Sensor(Channel<T> requestsToSensor, Channel<F> responsesFromSensor) {
		this.requestsToSensor = requestsToSensor;
		this.responsesFromSensor = responsesFromSensor;
	}
	
	public Channel<T> getToChannel() {
		return requestsToSensor;
	}
	
	public Channel<F> getFromChannel() {
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
