package gridworld.sensors;

import java.util.ArrayList;	

import simulator.channel.*;
import simulator.Sensor;

public class GPSSensor extends Sensor<String, Double> {
	
	public GPSSensor (Channel<String> requestsToSensor, Channel<String> responsesFromSensor,
			ChannelM<Double> dataResponsesFromSensor)
	{
		super(requestsToSensor, responsesFromSensor, dataResponsesFromSensor);
	}
	@Override
	public void nextStep(double delT) {
		if (requestsToSensor.hasMessage()) {
			ArrayList<Double> message = new ArrayList<Double> ();
			
			// ** Return current location. Later, add noise.
			message.add(orientation.location.x);
			message.add(orientation.location.y);
			
			dataResponsesFromSensor.setMessage(message);
		}
	}

}
