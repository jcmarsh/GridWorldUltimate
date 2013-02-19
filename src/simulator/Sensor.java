package simulator;

import simulator.channel.*;

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
	protected Channel<T> responsesFromSensor;
	protected ChannelM<F> dataResponsesFromSensor = null;

	public Sensor(Channel<T> requestsToSensor, Channel<T> responsesFromSensor) {
		this.requestsToSensor = requestsToSensor;
		this.responsesFromSensor = responsesFromSensor;
	}
	
	public Sensor(Channel<T> requestsToSensor, Channel<T> responsesFromSensor, ChannelM<F> dataResponsesFromSensor) {
		this(requestsToSensor, responsesFromSensor);
		this.dataResponsesFromSensor = dataResponsesFromSensor;
	}
	
	public Channel<T> getToChannel() {
		return requestsToSensor;
	}
	
	public Channel<T> getFromChannel() {
		return responsesFromSensor;
	}
	
	public ChannelM<F> getDataFromChannel() {
		return dataResponsesFromSensor;
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
