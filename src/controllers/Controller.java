package controllers;

// Discrete-case commands: 
//   For type=move,
//     move=go|left|right|pick|drop
//   For type=sensor
//     sensor=gps|range|targetclose|targetvisible
// Continuous-case commands:
//   For type=move,
//     move=go|stop|pick|drop
//   Note: to be able to apply stop, velocity must be near zero in the accel-Model.
//   For type=sensor
//     sensor=gps|range|targetclose|targetvisible|velocity|orientation


import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.geom.*;
import java.awt.*;
import gridworld.*;

public class Controller {
	private final int SUCCESS = 1;
	private final int FAILURE = -1;

	protected int BASE_PORT = 9000;  // Must be same as in GridWorld.java

	protected LineNumberReader reader;
	protected PrintWriter writer;
	protected Thread readThread;
	protected ArrayList<String> message = new ArrayList<String>();
	protected Socket soc;
	protected boolean isCommandLine = false;

	// Useful variables - both discrete/continous
	protected int carNum = 0;  // This controller is for car# 0.
	protected int taskNum, sceneNum;
	protected int numTargets, numObstacles;

	// Useful variables - discrete:
	protected int numColumns, numRows;
	protected ArrayList<Point> discreteTargets, discreteObstacles;


	// Useful variables - continuous:
	protected double minX, maxX, minY, maxY;
	protected boolean isAccModel = false;
	protected ArrayList<Rectangle2D.Double> continuousObstacles, continuousTargets;

	//Added: Scotty and Pablo
	protected boolean knownTargets;
	protected boolean knownObstacles;

	public static void main (String[] argv)
	{
		boolean isCommandLine = false;
		if ((argv.length == 0) || (argv[0].equalsIgnoreCase("command"))) {
			isCommandLine = true;
		}

		int carNum = 0;
		if((argv.length > 1)){
			carNum = Integer.parseInt(argv[1]);
		}

		Controller c = new Controller(carNum, isCommandLine);
		c.start ();
	}

	public Controller(int carNum, boolean isCommandLine)
	{
		this.carNum = carNum;
		this.isCommandLine = isCommandLine;
	}

	public void start ()
	{
		try {
			// The controller will run as server, and needs
			// to be launched first.
			ServerSocket srv = new ServerSocket (BASE_PORT + carNum);

			// Block until we hear from GridWorld.
			System.out.println ("Controller: waiting for a connection from GridWorld");
			soc = srv.accept ();

			// At this stage, the connection will have been made.
			InetAddress remoteMachine = soc.getInetAddress();
			System.out.println ("Server: accepted a connection"
					+ " from " + remoteMachine);

			// Get the input and output streams.
			InputStream inStream = soc.getInputStream ();
			reader = new LineNumberReader (new InputStreamReader(inStream));
			OutputStream outStream = soc.getOutputStream ();
			writer = new PrintWriter (outStream);

			// Step 1. Receive the test message from GridWorld.
			String s = reader.readLine ();   // begin
			System.out.println (s);
			s = reader.readLine ();          // type=test
			System.out.println (s);
			s = reader.readLine ();          // car=<carNum>
			System.out.println (s);
			s = reader.readLine ();          // end
			System.out.println (s);
			System.out.println ("Received test message from GridWorld");

			// Step 2. Now write back to GridWorld
			writer.println ("begin");
			writer.println ("type=test");
			writer.println ("controller=" + carNum);
			writer.println ("end");
			writer.flush ();
			System.out.println ("Completed sending test message to GridWorld");

			// Now two-way communication has been established.
			// GridWorld has a thread waiting to hear from us, 
			// and one to send to us.

			// Step 3. Receive init message from GridWorld.
			ArrayList<String> initMsg = new ArrayList<String>();
			s = reader.readLine ();
			while (!s.equalsIgnoreCase("end")) {
				initMsg.add (s);
				s = reader.readLine ();
			}
			initMsg.add (s);

			if(processInit (initMsg) == SUCCESS){
				System.out.println ("Processed init-message from GridWorld");
			}else{
				System.out.println("Error: Incorrect init-message.  Halting.");
				srv.close();
				return;
			}

			// Fire off a thread to deal with input.
			readThread = new Thread () {
				public void run () 
				{
					readFromGridWorld ();
				}
			};
			readThread.start ();

			// Now we're ready for control.
			if (isCommandLine) {
				commandLineControl ();
			}
			else {
				programmaticControl ();
			}

			srv.close();
		}
		catch (IOException e) { System.out.println (e); }
	}


	private void commandLineControl ()
	{
		try {
			Scanner console = new Scanner (System.in);
			boolean done = false;
			while (! done) {
				System.out.print ("> ");
				System.out.flush ();
				String s = console.nextLine ();
				if (s.equalsIgnoreCase("Quit")) {
					done = true;
				}
				writer.println (s);
				writer.flush ();
				System.out.println ("wrote " + s + " to socket");
			}
			soc.close ();
		}
		catch (Exception e) { System.out.println (e); }
	}


	private void readFromGridWorld ()
	{
		while (true) {
			// See if there's a message to read.
			String s = null;

			try {
				s = reader.readLine ();
			} catch (IOException e) { 
				System.out.println("IO Error reading from GridWorld " + e); 
			}
			if (s != null) {
				System.out.println ("Controller: received s=" + s);
				if (s.equalsIgnoreCase("begin")) {
					// It's the start of a new message.
					message = new ArrayList<String>();
					message.add (s);
				}
				else if (s.equalsIgnoreCase("end")) {
					// It's the end of a message, so parse.
					System.out.println ("Controller: Processing message");
					message.add (s);
					processMessage (message);
					message = null;
				}
				else {
					// Accumulate.
					if (message == null) {
						System.out.println ("Controller: Improper message in socket handler");
					}
					message.add (s);
				}
			}
		}  //endwhile
	}

	private int processInit (ArrayList<String> initMsg)
	{
		int return_value = SUCCESS;

		// Read init data, target locations etc.
		System.out.println ("INIT message received from GridWorld:");
		for (String s: initMsg) {
			System.out.println (s);
		}

		String s = initMsg.get (0);
		if (!s.equals ("begin")) {
			handleError (s);
			return FAILURE;
		}

		// 2nd line is type=init
		s = initMsg.get (1);
		if (!s.equals ("type=init")) {
			handleError ("Expected type=init, but received: " + s);

			return FAILURE;
		}

		s = initMsg.get (2);
		if (s.equals ("case=discrete")) {
			try {
				return_value = discreteInit (initMsg);
			}
			catch (IOException e) {
				System.out.println (e);
			}
		}
		else if (s.equals ("case=continuous")) {
			try {
				return_value = continuousInit (initMsg);
			}
			catch (IOException e) {
				System.out.println (e);
			}
		}
		else {
			handleError (s);
			return FAILURE;
		}
		return return_value;
	}


	private int discreteInit (ArrayList<String> initMsg)
			throws IOException
			{
		// Line#.
		int n = 3;

		// Get nCols, nRows.
		String colStr = initMsg.get (n++);
		numColumns = Util.getIntProperty (colStr);
		String rowStr = initMsg.get (n++);
		numRows = Util.getIntProperty (rowStr);

		// Get task, scene.
		String taskStr = initMsg.get (n++);
		taskNum = Util.getIntProperty (taskStr);
		String sceneStr = initMsg.get (n++);
		sceneNum = Util.getIntProperty (sceneStr);

		// # targets.
		String targetStr = initMsg.get (n++);
		numTargets = Util.getIntProperty (targetStr);

		System.out.println ("Controller init msg: numRows=" + numRows + " numColumns=" + numColumns + " task#=" + taskNum + " scene#=" + sceneNum + " numTargets=" + numTargets);

		String knownTargetStr = initMsg.get (n++);
		boolean knownTargets = Util.getBooleanProperty (knownTargetStr);
		if (knownTargets) {
			// Read them.
			discreteTargets = new ArrayList<Point>();
			for (int i=0; i<numTargets; i++) {
				String tStr = initMsg.get (n++);
				Scanner strScan = new Scanner (tStr);
				Point c = new Point ();
				c.setLocation(strScan.nextInt(), strScan.nextInt());
				discreteTargets.add (c);
				System.out.println (" >> target: " + c);
			}
		}

		// if obstacles are given: read the #, then the obstacles.
		String knownObsStr = initMsg.get (n++);
		boolean knownObstacles = Util.getBooleanProperty (knownObsStr);
		if (knownObstacles) {
			String numObsStr = initMsg.get (n++);
			numObstacles = Util.getIntProperty (numObsStr);
			// Now read them.
			discreteObstacles = new ArrayList<Point>();
			for (int i=0; i<numObstacles; i++) {
				String obStr = initMsg.get (n++);
				Scanner strScan = new Scanner (obStr);
				Point c = new Point ();
				c.setLocation(strScan.nextInt(), strScan.nextInt());
				discreteObstacles.add (c);
				System.out.println (" >> obstacle: " + c);
			}
		}

		String endStr = initMsg.get (n++);
		if (!endStr.equals ("end")) {
			// Trouble.
			System.out.println ("ERROR: no end of message found");
			System.exit (0);
		}
		else {
			System.out.println ("Controller: discrete init message processed");
		}

		return SUCCESS;
			}


	private int continuousInit (ArrayList<String> initMsg)
			throws IOException
			{
		// Line#.
		int n = 3;

		// Get minX, maxX, minY, maxY
		String xStr = initMsg.get (n++);
		minX = Util.getDoubleProperty (xStr);
		xStr = initMsg.get (n++);
		maxX = Util.getIntProperty (xStr);
		String yStr = initMsg.get (n++);
		minY = Util.getDoubleProperty (yStr);
		yStr = initMsg.get (n++);
		maxY = Util.getIntProperty (yStr);

		// Get task, scene.
		String taskStr = initMsg.get (n++);
		taskNum = Util.getIntProperty (taskStr);
		String sceneStr = initMsg.get (n++);
		sceneNum = Util.getIntProperty (sceneStr);
		String accStr = initMsg.get (n++);
		isAccModel = Util.getBooleanProperty (accStr);

		// # targets.
		String targetStr = initMsg.get (n++);
		numTargets = Util.getIntProperty (targetStr);

		System.out.println ("Controller init msg continuous: minX=" + minX + " maxX=" + maxX + " minY=" + minY + " maxY=" + maxY + " task#=" + taskNum + " scene#=" + sceneNum + " numTargets=" + numTargets);

		String knownTargetStr = initMsg.get (n++);
		knownTargets = Util.getBooleanProperty (knownTargetStr);
		if (knownTargets) {
			// Read them.
			continuousTargets = new ArrayList<Rectangle2D.Double>();
			for (int i=0; i<numTargets; i++) {
				String tStr = initMsg.get (n++);
				Scanner strScan = new Scanner (tStr);
				Rectangle2D.Double R = new Rectangle2D.Double ();
				R.x = strScan.nextDouble();
				R.y = strScan.nextDouble();
				R.width = strScan.nextDouble();
				R.height = strScan.nextDouble();
				continuousTargets.add (R);
				System.out.println (" >> target: " + R);
			}
		}

		// if obstacles are given: read the #, then the obstacles.
		String knownObsStr = initMsg.get (n++);
		knownObstacles = Util.getBooleanProperty (knownObsStr);
		if (knownObstacles) {
			String numObsStr = initMsg.get (n++);
			numObstacles = Util.getIntProperty (numObsStr);
			// Now read them.
			continuousObstacles = new ArrayList<Rectangle2D.Double>();
			for (int i=0; i<numObstacles; i++) {
				String obStr = initMsg.get (n++);
				Scanner strScan = new Scanner (obStr);
				Rectangle2D.Double R = new Rectangle2D.Double ();
				R.x = strScan.nextDouble();
				R.y = strScan.nextDouble();
				R.width = strScan.nextDouble();
				R.height = strScan.nextDouble();
				continuousObstacles.add (R);
				System.out.println (" >> obstacle: " + R);
			}
		}

		String endStr = initMsg.get (n++);
		if (!endStr.equals ("end")) {
			// Trouble.
			System.out.println ("ERROR: no end of message found");
			System.exit (0);
		}
		else {
			System.out.println ("Controller: continuous init message processed");
		}

		return SUCCESS;
			}

	private void handleError (String s)
	{
		System.out.println ("ERROR in msg: " + s);
	}

	/////////////////////////////////////////////////////////////////////
	// INSERT YOUR CODE HERE

	protected void programmaticControl ()
	{
		// This is where you write a program to send control
		// messages. For example, for the case that everything
		// is known, this is where you would: (1) running a
		// planning algorithm to plan a path/actions; (2) execute
		// the plan by issuing messages to GridWorld.
	}

	protected void processMessage (ArrayList<String> message)
	{
		// message.get(0) should be "begin" etc.
		// Ideally, this processing should not take too much time
		// because other messages could be sitting in the socket.
		// I don't know what happens if a socket's buffer is 
		// unattended for long.
	}



}

