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

	// TODO: What do you think?
	private ArrayList<Sensor<String, ?>> Sensors;
	private RangeSensor rangeSensor;
	private simulator.channel.Channel<String> toRangeSensor;
	private simulator.channel.Channel<String> fromRangeSensor;
	private simulator.channel.ChannelM<Double> dataFromRangeSensor;
	private GPSSensor gpsSensor;
	private simulator.channel.Channel<String> toGPSSensor;
	private simulator.channel.Channel<String> fromGPSSensor;
	private simulator.channel.ChannelM<Double> dataFromGPSSensor;
	private StateSensor stateSensor;
	private simulator.channel.Channel<String> toStateSensor;
	private simulator.channel.Channel<String> fromStateSensor;
	private simulator.channel.ChannelM<Double> dataFromStateSensor;
	private GroundSensor groundSensor;
	private simulator.channel.Channel<String> toGroundSensor;
	private simulator.channel.Channel<String> fromGroundSensor;
	private simulator.channel.ChannelM<Integer> dataFromGroundSensor;
	
	private boolean isAccelModel;

	public Car (int carNum, GridWorldPanel gwPanel, boolean isAccelModel) {
		this.gwPanel = gwPanel;
		this.isAccelModel = isAccelModel;
		this.carNum = carNum;
		Sensors = new ArrayList<Sensor<String, ?>>();
		// Range
		toRangeSensor = new simulator.channel.Channel<String>(carNum);
		fromRangeSensor = new simulator.channel.Channel<String>(carNum);
		dataFromRangeSensor = new simulator.channel.ChannelM<Double>(carNum, "range");
		rangeSensor = new RangeSensor(gwPanel, toRangeSensor, fromRangeSensor, dataFromRangeSensor);
		Sensors.add(rangeSensor);
		// GPS
		toGPSSensor = new simulator.channel.Channel<String>(carNum);
		fromGPSSensor = new simulator.channel.Channel<String>(carNum);
		dataFromGPSSensor = new simulator.channel.ChannelM<Double>(carNum, "gps");
		gpsSensor = new GPSSensor(toGPSSensor, fromGPSSensor, dataFromGPSSensor);
		Sensors.add(gpsSensor);
		// State
		toStateSensor = new simulator.channel.Channel<String>(carNum);
		fromStateSensor = new simulator.channel.Channel<String>(carNum);
		dataFromStateSensor = new simulator.channel.ChannelM<Double>(carNum, "state");
		stateSensor = new StateSensor(toStateSensor, fromStateSensor, dataFromStateSensor);
		Sensors.add(stateSensor);
		// Ground
		toGroundSensor = new simulator.channel.Channel<String>(carNum);
		fromGroundSensor = new simulator.channel.Channel<String>(carNum);
		dataFromGroundSensor = new simulator.channel.ChannelM<Integer>(carNum, "ground");		
		groundSensor = new GroundSensor(gwPanel, toGroundSensor, fromGroundSensor, dataFromGroundSensor);
		Sensors.add(groundSensor);
	}

	public void init (Orientation orien) {
		precieved_orien = orien;
	}

	public int getCarNum() {
		return carNum;
	}

	private String[] discreteActions = {"go", "left", "right", "pick", "drop"};
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
		return fromRangeSensor.hasMessage() || dataFromRangeSensor.hasMessage()
				|| fromGPSSensor.hasMessage() || dataFromGPSSensor.hasMessage()
				|| fromStateSensor.hasMessage() || dataFromStateSensor.hasMessage()
				|| fromGroundSensor.hasMessage() || dataFromGroundSensor.hasMessage();
	}

	public ArrayList<String> getMessage() {
		ArrayList<String> msg = new ArrayList<String>();
		if (fromRangeSensor.hasMessage()) {
			msg.add(fromRangeSensor.getMessage());
		} else if (dataFromRangeSensor.hasMessage()) {
			msg = dataFromRangeSensor.getMessageStr();
		} else if (fromGPSSensor.hasMessage()) {
			msg.add(fromGPSSensor.getMessage());
		} else if (dataFromGPSSensor.hasMessage()) {
			msg =  dataFromGPSSensor.getMessageStr();
		} else if (fromGroundSensor.hasMessage()) {
			msg.add(fromGroundSensor.getMessage());
		} else if (dataFromGroundSensor.hasMessage()) {
			msg =  dataFromGroundSensor.getMessageStr();
		} else if (fromStateSensor.hasMessage()) {
			msg.add(fromStateSensor.getMessage());
		} else if (dataFromStateSensor.hasMessage()) {
			msg =  dataFromStateSensor.getMessageStr();
		} else {
			msg.add("Car error: No message for the controller (but one was requested).");
		}
		return msg;
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