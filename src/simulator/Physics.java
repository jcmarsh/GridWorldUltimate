package simulator;

import gridworld.*;
import java.util.ArrayList;

import simulator.channel.Channel;

/**
 * Physics.java
 * 
 * Handles the state of the physical objects in the world.
 * 
 * May 2012
 * @author James Marshall
 */

public class Physics {
	ArrayList<gridworld.CarModel> models; // Index will be the car number?
	ArrayList<Channel<CarControl>> controlFromPO; // same as above
	//ArrayList<Channel<String>> sensorRequestFromPO;
	ArrayList<Channel<String>> actionStrFromPO;

	int numObstacleHits = 0;
	// Handle on the static environment (for now GridWorld)
	// Scene graph of all objects in the environment
	//   Each needs to have a channel where Physics receives updates

	public Physics() {
		models = new ArrayList<CarModel> ();
		controlFromPO = new ArrayList<Channel<CarControl>> ();
		//sensorRequestFromPO = new ArrayList<Channel<String>> ();
		actionStrFromPO = new ArrayList<Channel<String>> ();
	}

	public void update(int delta_time) {
		// Process any new messages
		// TODO: Separate continuous and discrete?
		// For continuous:
		double delt = delta_time / 1000.0; // millis to seconds
		for (int carIndex = 0; carIndex < controlFromPO.size(); carIndex++) {
			Channel<CarControl> cc = controlFromPO.get(carIndex);
			gridworld.CarModel currentModel = models.get(carIndex); 
			if (cc.hasMessage()) {
				CarControl controls = cc.getMessage();
				currentModel.newControlInput(controls);
			}

			Channel<String> ca = actionStrFromPO.get(carIndex);

			if(ca.hasMessage()){
				String m = ca.getMessage();
				System.out.println("Physics has a message: *" + m + "*");
				currentModel.continuousMove(m);
				System.out.flush();
			}

			currentModel.nextStep(delt);
			if (currentModel.hitObstacle()) {
				numObstacleHits++;
				simulator.Master.status("Hit detected: " + numObstacleHits);
			}
		}
		/*
		// For discrete
		for (int carIndex = 0; carIndex < actionStrFromPO.size(); carIndex++) {
			Channel<String> asc = actionStrFromPO.get(carIndex);
			if (asc.hasMessage()) {
				gridworld.CarModel currentModel = models.get(carIndex);
				currentModel.discreteMove(asc.getMessage());
			}
		}
		 */
		//   Resolve any collisions
		for (int carIndex = 0; carIndex < models.size(); carIndex++) {
			// TODO: Figure out collisions!
		}


		/*
		//Sensors! First read any new sensor commands from POs
		for (int sensorIndex = 0; sensorIndex < sensorRequestFromPO.size(); sensorIndex++) {
			Channel<String> request = sensorRequestFromPO.get(sensorIndex);
			if (request.hasMessage()) {
				gridworld.RangeSensor sensor = models.get(request.getPOID()).getSensor();
				sensor.process(request.getMessage());
			}
		}

		/*
		// Now check for responses from the sensors (sent to the POs)
		for (int sensorIndex = 0; sensorIndex < sensorResponseToPO.size(); sensorIndex++) {
			Channel<String> response = sensorResponseToPO.get(sensorIndex);
			if (response.hasMessage()) {
				// TODO: Shit... how do we signal for Car.java to check this?
				// or will Car.java periodically check itself?
				// Socket Handler will check this in its loop.
			}
		}
		 */

	}

	public void addPOM(int carNum, CarModel cm, Channel<CarControl> cFPO, Channel<String> aSFPO) {
		System.out.println("Is this getting called many times for: " + carNum + "?");
		models.add(cm);
		controlFromPO.add(cFPO);
//		sensorRequestFromPO.add(sRFPO);
		actionStrFromPO.add(aSFPO);
	}

	// TODO: Either remove or fully implement (right now Master just makes a new one).
	public void reset() {
		numObstacleHits = 0; 
	}
}