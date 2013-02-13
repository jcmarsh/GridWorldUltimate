package gridworld.sensors;

import java.util.ArrayList;

import simulator.Channel;
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

public class StateSensor extends Sensor {
	CarModel carModel; // The car model which this sensor is attached to.

	public StateSensor (Channel<String> requestsToSensor, Channel<ArrayList<String>> responsesFromSensor)
	{
		super(requestsToSensor, responsesFromSensor);
	}
	
	public void setCarModel(CarModel cm) {
		carModel = cm;
	}

	@Override
	public void nextStep(double delT) {
		if (requestsToSensor.hasMessage()) {
			ArrayList<String> message = new ArrayList<String> ();
			String actionStr = requestsToSensor.getMessage();

			if (actionStr.equalsIgnoreCase("velocity")) {
				// ** Return current velocity. Later, add noise.
				message.add ("v1=" + carModel.getV1());
				message.add ("v2=" + carModel.getV2());
			} else if (actionStr.equalsIgnoreCase("orientation")) {
				// ** Return current angle. Later, add noise.
				message.add ("orientation=" + orientation.theta);
			} else {
				message.add ("sensor=error");
			}
			responsesFromSensor.setMessage(message);
		}
	}
}
