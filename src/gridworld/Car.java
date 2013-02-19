package gridworld;

import gridworld.environments.GridWorldPanel;
import gridworld.sensors.*;

import java.util.*;

import simulator.Orientation;
import simulator.Sensor;
import simulator.channel.Channel;

/**
 * Car.java
 * 
 * Modified from Simha's original Car.java. Represents a Car in the simulation
 * for the purposes of communicating with the planner. {@link CarModel} handles
 * the actual drawing and movement of the Car.
 * 
 * Jul 3, 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 *
 */
public class Car implements simulator.PlannableObject {
	// We'll use some max-screen height for conversion to Java coordinates.
	double maxHeight = 1000;

	double c1, c2;                  // Left, right controls

	double distMoved;                          

	// Angular velocities for accelerative model.
	double v1, v2;
	Orientation precieved_orien; // May not be actual orientation.

	int carNum;
	GridWorldPanel gwPanel;
	private gridworld.SocketHandler controller;       // To write sensor readings.

	GridObject target = null;       // The target.

	// TODO: split discrete and continuous cars?
	simulator.channel.Channel<CarControl> controlToPhysics;
	simulator.channel.Channel<String> discActionStrToPhysics;
	simulator.channel.Channel<String> contActionStrToPhysics;

	ArrayList<Sensor> Sensors;
	RangeSensor rangeSensor;
	simulator.channel.Channel<String> toRangeSensor;
	simulator.channel.Channel<ArrayList<String>> fromRangeSensor;
	GPSSensor gpsSensor;
	simulator.channel.Channel<String> toGPSSensor;
	simulator.channel.Channel<ArrayList<String>> fromGPSSensor;
	StateSensor stateSensor;
	simulator.channel.Channel<String> toStateSensor;
	simulator.channel.Channel<ArrayList<String>> fromStateSensor;
	GroundSensor groundSensor;
	simulator.channel.Channel<String> toGroundSensor;
	simulator.channel.Channel<ArrayList<String>> fromGroundSensor;
	
	private boolean isAccelModel;

	public Car (int carNum, GridWorldPanel gwPanel, boolean isAccelModel) {
		this.gwPanel = gwPanel;
		this.isAccelModel = isAccelModel;
		this.carNum = carNum;
		Sensors = new ArrayList<Sensor>();
		// Range
		toRangeSensor = new simulator.channel.Channel<String>(carNum);
		fromRangeSensor = new simulator.channel.Channel<ArrayList<String>>(carNum);
		rangeSensor = new RangeSensor(gwPanel, toRangeSensor, fromRangeSensor);
		Sensors.add(rangeSensor);
		// GPS
		toGPSSensor = new simulator.channel.Channel<String>(carNum);
		fromGPSSensor = new simulator.channel.Channel<ArrayList<String>>(carNum);
		gpsSensor = new GPSSensor(toGPSSensor, fromGPSSensor);
		Sensors.add(gpsSensor);
		// State
		toStateSensor = new simulator.channel.Channel<String>(carNum);
		fromStateSensor = new simulator.channel.Channel<ArrayList<String>>(carNum);
		stateSensor = new StateSensor(toStateSensor, fromStateSensor);
		Sensors.add(stateSensor);
		// Ground
		toGroundSensor = new simulator.channel.Channel<String>(carNum);
		fromGroundSensor = new simulator.channel.Channel<ArrayList<String>>(carNum);
		groundSensor = new GroundSensor(gwPanel, toGroundSensor, fromGroundSensor);
		Sensors.add(groundSensor);
	}

	public void init (Orientation orien) {
		precieved_orien = orien;
	}

	public int getCarNum() {
		return carNum;
	}

	String[] discreteActions = {"go", "left", "right", "pick", "drop"};
	private boolean checkValidDiscrete(String action) {
		for (String s : discreteActions) {
			if (action.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	public void discreteMove (String actionStr)
	{
		if (discActionStrToPhysics.hasMessage()) {
			simulator.Master.error("Channel already has an unprocessed message");
		} else {
			if (checkValidDiscrete(actionStr)) {
				// System.out.println ("Car.discreteMove: action=" + actionStr);
				discActionStrToPhysics.setMessage(actionStr);
			} else {
				simulator.Master.error("Invalid action str: " + actionStr);
			}
		}
	}

	String[] continuousActions = {"pick", "drop"};
	private boolean checkValidContinuous(String action) {
		for (String s : continuousActions) {
			if (action.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	public void continuousAction(String actionStr) {
	    if (contActionStrToPhysics.hasMessage()) {
			simulator.Master.error("Channel already has an unprocessed message");
		} else {
			if (checkValidContinuous(actionStr)) {
			    contActionStrToPhysics.setMessage(actionStr);
			} else {
				simulator.Master.error("Invalid action str: " + actionStr);
			}
		}
	}

	public void continuousMove(double left, double right) {
		if (controlToPhysics.hasMessage()) {
			simulator.Master.error("Channel already has an unprocessed message");
		} else {
			controlToPhysics.setMessage(new CarControl(left, right));
		}
	}

    private String[] sensorCommands = {"ground", "gps", "range", "orientation", "velocity", "targetvisible", "targetclose"};
    private boolean checkValidSensorCommand(String action) {
	for (String s : sensorCommands) {
	    if (action.equalsIgnoreCase(s)) {
		return true;
	    }			
	}
	return false;
    }

	public void discOrContSensor(String actionStr) {
		if (checkValidSensorCommand(actionStr)) {
			if (actionStr.equalsIgnoreCase("gps")) {
				toGPSSensor.setMessage(actionStr);
			} else if (actionStr.equalsIgnoreCase("orientation") || 
						actionStr.equalsIgnoreCase("velocity")) {
				toStateSensor.setMessage(actionStr);
			} else if (actionStr.equalsIgnoreCase("range") ||
						actionStr.equalsIgnoreCase("targetclose") || 
						actionStr.equalsIgnoreCase("targetvisible")){
				toRangeSensor.setMessage(actionStr);
			} else if (actionStr.equalsIgnoreCase("ground")) {
				toGroundSensor.setMessage(actionStr);
			} else {
				simulator.Master.error("No valid command, but passed valid command check!!!");
			}
		} else {
			simulator.Master.error("Invalid sensor command: " + actionStr);
		}
	}

	public boolean hasMessage() {
		return fromRangeSensor.hasMessage() || fromGPSSensor.hasMessage() || fromStateSensor.hasMessage() || fromGroundSensor.hasMessage();
	}

	public ArrayList<String> getMessage() {
		if (fromRangeSensor.hasMessage()) {
			return fromRangeSensor.getMessage();
		} else if (fromGPSSensor.hasMessage()) {
			return fromGPSSensor.getMessage();
		} else if (fromGroundSensor.hasMessage()) {
			return fromGroundSensor.getMessage();
		} else {
			return fromStateSensor.getMessage();
		}
	}

	public double getDistanceMoved () {
		return distMoved;
	}

	@Override
	public SocketHandler getSocket() {
		return controller;
	}

	@Override
	public void setSocket(SocketHandler s) {
		controller = s;
	}

	@Override
	public void initPhysicsChannel(simulator.Physics physics, Orientation orientation) {
		CarModel cm = new CarModel(gwPanel, Sensors, orientation, isAccelModel);
		stateSensor.setCarModel(cm);
		gwPanel.addCarModel(cm);
		
		controlToPhysics = new Channel<CarControl> (carNum);
		discActionStrToPhysics = new Channel<String> (carNum);

		//Well....this probably shouldn't be the solution, but it's what I'm doing
		contActionStrToPhysics = new Channel<String> (carNum);//discActionStrToPhysics;
		
		
		physics.addPOM(carNum, cm, controlToPhysics, contActionStrToPhysics);//discActionStrToPhysics);
	}
}