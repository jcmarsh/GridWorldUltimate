package gridworld.sensors;

import java.util.ArrayList;

import simulator.channel.*;
import simulator.Sensor;
import gridworld.CarModel;

/**
 * StateSensor.java
 * 
 * Reports on the internal state of a Car. The car could report this itself, but
 * this way we have a uniform way to add noise and delay.
 * 
 * Feb 7, 2013
 * @author James Marshall <jcmarsh (at) gmail.gwu.edu>
 *
 */

public class StateSensor extends Sensor<String, Double> {
	CarModel carModel; // The car model which this sensor is attached to.

	public StateSensor (Channel<String> requestsToSensor, Channel<String> responsesFromSensor,
			ChannelM<Double> dataResponsesFromSensor)
	{
		super(requestsToSensor, responsesFromSensor, dataResponsesFromSensor);
	}
	
	public void setCarModel(CarModel cm) {
		carModel = cm;
	}

	@Override
	public void nextStep(double delT) {
		if (requestsToSensor.hasMessage()) {
			ArrayList<Double> message = new ArrayList<Double> ();
			String actionStr = requestsToSensor.getMessage();

			if (actionStr.equalsIgnoreCase("velocity")) {
				// ** Return current velocity. Later, add noise.
				message.add(carModel.getV1());
				message.add(carModel.getV2());
			} else if (actionStr.equalsIgnoreCase("orientation")) {
				// ** Return current angle. Later, add noise.
				// TODO: This should not be here... like GPS, it is a result of previous actions, not an internal state.
				message.add(orientation.theta);
			} else {
				responsesFromSensor.setMessage("StateSensor error: " + actionStr);
			}
			if (message.isEmpty()) {
				message = null;
			} else {
				dataResponsesFromSensor.setMessage(message);
			}
		}
	}
}
