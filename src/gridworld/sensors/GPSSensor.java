package gridworld.sensors;

import java.util.ArrayList;

import simulator.Channel;
import simulator.Sensor;

public class GPSSensor extends Sensor {
	
	public GPSSensor (Channel<String> requestsToSensor, Channel<ArrayList<String>> responsesFromSensor)
	{
		super(requestsToSensor, responsesFromSensor);
	}
	@Override
	public void nextStep(double delT) {
		if (requestsToSensor.hasMessage()) {
			ArrayList<String> message = new ArrayList<String> ();
			
			// ** Return current location. Later, add noise.
			message.add ("x=" + orientation.location.x);
			message.add ("y=" + orientation.location.y);
			
			responsesFromSensor.setMessage(message);
		}
	}

}
