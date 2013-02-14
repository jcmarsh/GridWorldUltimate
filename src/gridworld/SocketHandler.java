package gridworld;
import gridworld.environments.GridWorld;
import gridworld.environments.GridWorldPanel;

import java.util.*;
import java.io.*;
import java.net.*;

import simulator.Master;
/**
 * SocketHandler.java
 * 
 * The Socket Handler from the original GridWorld. Handles communication between
 * the planner and the {@link PlannableObject}.
 * 
 * @author Rahul Simha
 * modified: James Marshall <jcmarsh (at) gwmail.gwu.edu>
 *
 */

// TODO: should there be a more general version that is extend by the environment?
public class SocketHandler {

	public final static String FINISHED_STRING = "type=finished";
	GridWorldPanel gwPanel;
	Car car;
	LineNumberReader reader;
	PrintWriter writer;
	Socket soc;
	Thread readThread;
	ArrayList<String> message = new ArrayList<String>();


	public boolean init (simulator.Environment gWorld, simulator.PlannableObject car, String ipAddr, int port)
	{
		gwPanel = gWorld.getGridWorldPanel();

		if (Car.class.isInstance(car)) {
			this.car = (Car) car;
		} else {
			simulator.Master.error("SocketHandler can not be used for non-car");
			return false;
		}

		// Open a socket to the server at the address.
		try {
			soc = new Socket (ipAddr, port);
			InetAddress remoteMachine = soc.getInetAddress();
			String msg = "Attempting connection to " + remoteMachine + ":" 
					+ port;
			System.out.println (msg);
			simulator.Master.status (msg);

			// Now create the streams.
			OutputStream outStream = soc.getOutputStream ();
			writer = new PrintWriter (outStream);
			InputStream inStream = soc.getInputStream ();
			reader = new LineNumberReader (new InputStreamReader(inStream));

			// Send and receive test message.
			// Send.

			writer.println ("begin");
			writer.println ("type=test");
			writer.println ("car=" + this.car.carNum);
			writer.println ("end");
			writer.flush ();

			System.out.println ("Finished sending test message");

			// Receive.
			// Should be "begin"
			String s = reader.readLine ();
			System.out.println (s);
			if (! s.equalsIgnoreCase("begin")) {
				return false;
			}
			// Should be "type=test"
			s = reader.readLine ();
			System.out.println (s);
			if (! s.equalsIgnoreCase("type=test")) {
				return false;
			}
			// Should be "controller=<name>"
			s = reader.readLine ();
			System.out.println (s);
			// Should be "end"
			s = reader.readLine ();
			System.out.println (s);
			if (! s.equalsIgnoreCase("end")) {
				return false;
			}

			// car.setController(this);

			// Wrap a thread around reading.
			readThread = new Thread () {
				public void run () 
				{
					readFromController ();
				}
			};
			readThread.start ();

			return true;
		}
		catch (Exception e) { 
			System.out.println (e); 
			return false;
		}
	}

	public boolean isActive ()
	{
		return soc.isConnected();
		// ** Check that socket is active.
		//	return true;
	}

	public void sendFinished(){
		if(writer != null){
			writer.println("begin");
			writer.println(FINISHED_STRING);
			writer.println("end");
			writer.flush();
		}
	}

	public void sendInit ()
	{
		writer.println ("begin");
		writer.println ("type=init");

		if (GridWorld.isDiscrete) {
			writer.println ("case=discrete");
			writer.println ("numColumns=" + GridWorldPanel.M);
			writer.println ("numRows=" + GridWorldPanel.N);
		}
		else {
			writer.println ("case=continuous");
			writer.println ("minX=" + GridWorldPanel.MIN_Y);
			writer.println ("maxX=" + GridWorldPanel.MAX_X);
			writer.println ("minY=" + GridWorldPanel.MIN_Y);
			writer.println ("maxY=" + GridWorldPanel.MAX_Y);
		}

		writer.println ("task=" + GridWorld.taskNum);
		writer.println ("scene=" + GridWorld.sceneNum);

		if (!GridWorld.isDiscrete) {
			if (GridWorld.accModel) {
				writer.println ("accModel=true");
			}
			else {
				writer.println ("accModel=false");
			}
		}

		writer.println ("numTargets=" + GridWorld.numTargets);
		if (GridWorld.knownTargets) {
			writer.println ("knownTargets=true");
			// Write the target locations one by one.
			writeTargets ();
		}
		else {
			writer.println ("knownTargets=false");
		}

		if (GridWorld.knownObstacles) {
			writer.println ("knownObstacles=true");
			writer.println ("numObstacles=" + gwPanel.getObstacles().size());
			// Write the obstacles one by one.
			writeObstacles ();
		}
		else {
			writer.println ("knownObstacles=false");
		}

		writer.println ("end");
		writer.flush ();
		System.out.println ("Sent init message to controller for car " + car.carNum);
	}


	void writeTargets ()
	{
		System.out.println("Hello from SocketHandler!");
		System.out.println("gwPanel: " + gwPanel);
		System.out.println("gwPanel.getTargets: " + gwPanel.getTargets());
		System.out.println("Empty? " + gwPanel.getTargets().isEmpty());
		//if (!gwPanel.getTargets().isEmpty()) {
			if (GridWorld.isDiscrete) {
				// Write the grid coordinates of the target cells.
				for (GridObject d: gwPanel.getTargets()) {
					System.out.println(d.getGridLoc().x + " " + d.getGridLoc().y);
					writer.println (d.getGridLoc().x + " " + d.getGridLoc().y);
				}	
				return;
			}

			// Otherwise, write the rectangle info for continuous case.
			for (GridObject d: gwPanel.getTargets()) {
				writer.println (d.x + " " + d.y + " " + d.width + " " + d.height);
			}	
		//}
	}


	void writeObstacles ()
	{
		if (GridWorld.isDiscrete) {
			// Write the grid coordinates of the obstacle cells.
			for (GridObject d: gwPanel.getObstacles()) {
				writer.println (d.getGridLoc().x + " " + d.getGridLoc().y);
			}	
			return;
		}

		// Otherwise, write the rectangle info for continuous case.
		for (GridObject d: gwPanel.getObstacles()) {
			writer.println (d.x + " " + d.y + " " + d.width + " " + d.height);
		}	
	}


	void readFromController ()
	{
		while (true) {
			// See if there's a message to read. (Controller to Car)
			String s = null;
			try {
				s = reader.readLine ();
			} catch (IOException e) {
				System.out.println("IO error reading from controller: " + e);
			}
			if (s != null) {
				System.out.println ("SocketH: received " + s);
				if (s.equalsIgnoreCase("begin")) {
					// It's the start of a new message.
					message = new ArrayList<String>();
					message.add (s);
				}
				else if (s.equalsIgnoreCase("end")) {
					// It's the end of a message, so parse.
					//System.out.println ("Processing message");
					message.add (s);
					processMessage ();
					message = null;
				}
				else {
					// Accumulate.
					if (message == null) {
						Master.error ("Improper message in socket handler");
					}
					message.add (s);
				}
			} 
			
			// See if there's a message to write (Car to Controller)
			if (car.hasMessage()) {
				ArrayList<String> result = car.getMessage();
				writer.println("begin");
				System.out.println("Printing: " + result.size());
				for (int i = 0; i < result.size(); i++) {
					writer.println(result.get(i));
				}
				writer.println("end");
				writer.flush();
			}
		}  // end while
	}

	void processMessage ()
	{
		if (GridWorld.isDiscrete) {
			processDiscrete ();
		}
		else {
			processContinuous ();
		}
	}


	void processDiscrete ()
	{
		// Expecting 2nd line to be type=move or type=sensor
		String s = message.get (1);
		//System.out.println ("Line 2: " + s);
		if (s == null) {
			return;
		}

		if (s.equals ("type=move")) {
			s = message.get (2);
			if(car != null){
				System.out.println("Car Number: " + car.carNum);
			}
			//System.out.println ("Line 3: " + s);
			// This is one of move= [left, right, go, pick, drop]
			String actionStr = Util.getStringProperty (s);
			car.discreteMove (actionStr);
		}
		else if (s.equals ("type=sensor")) {
			s = message.get (2);
			System.out.println ("Line 3: " + s);
			// This is one of: gps, range, targetvisible, targetclose
			String actionStr = Util.getStringProperty (s);
			car.discOrContSensor(actionStr);
		} else {
			simulator.Master.error("Illegal message: s=" + s);
			return;
		}

	}


	void processContinuous ()
	{
		// Expecting 2nd line to be type=move or type=sensor
		String s = message.get (1);
		System.out.println ("Line 2: " + s);
		if (s == null) {
			return;
		}

		if (s.equals ("type=move")) {
			s = message.get (2);
			System.out.println ("Line 3: " + s);
			// This is one of move= [go, pick, drop]
			String actionStr = Util.getStringProperty (s);
			if (actionStr.equals("go")) {
				// Get the left and right quantities.
				String leftStr = message.get (3);
				String rightStr = message.get (4);
				double leftSignal = Util.getDoubleProperty (leftStr);
				double rightSignal = Util.getDoubleProperty (rightStr);

				car.continuousMove (leftSignal, rightSignal);
			} else {

			    System.out.println("Calling ContinuousAction");
				car.continuousAction(actionStr);
			}
		}
		else if (s.equals ("type=sensor")) {
			s = message.get (2);
			System.out.println ("Line 3: " + s);
			// This is one of: gps, range, targetvisible, targetclose
			String actionStr = Util.getStringProperty (s);
			car.discOrContSensor(actionStr);
		}
		else {
			simulator.Master.error ("Illegal message: s=" + s);
			return;
		}

	}

	public void finalize ()
	{
		try {
			// Overrides the one inherited from Object.
			soc.close ();
			readThread.interrupt();
			readThread = null;
		}
		catch (Exception e) {
		}
	}

	public boolean isConnected() {
		if (soc == null) {
			return false;
		} else {
			return soc.isConnected();
		}
	}
}
