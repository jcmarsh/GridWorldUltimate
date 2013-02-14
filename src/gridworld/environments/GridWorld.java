package gridworld.environments;
/**
 * GridWorld.java
 * Example implementation of both the discrete and continuous GridWorld environment
 *
 * Large chucks of code pulled from Rahul Simha's original GridWorld.java
 * 
 * May 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 */

import gridworld.Car;
import gridworld.Obstacle;
import gridworld.SocketHandler;
import gridworld.Target;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import simulator.*;


public class GridWorld  implements simulator.Environment {

	GridWorldPanel gwPanel = new GridWorldPanel();
	Physics physics;

	public static boolean isDiscrete = true;
	public static boolean knownTargets = false;
	public static boolean knownObstacles = false;

	public static int numTargets = 1;
	JTextField targetField;

	ArrayList<Car> enemies = new ArrayList<Car>();
	ArrayList<Thread> enemyThread = new ArrayList<Thread>();
	Color enemyColor = new Color(0, 155, 0);

	// GUI stuff.
	String[] blind = {"Known-targets", "Known-obstacles", "Both-unknown", "Both-known"};

	private String[] tasks = {"Task-1", "Task-2", "Task-3", "Task-4", "Task-5", "Task-6"};
	public static int taskNum = 1;
	private String[] scenes = {"Scene-1", "Scene-2", "Scene-3"};
	public static int sceneNum = 1;
	private String[] noiseLevels = {"zero-noise", "low-noise","high-noise"};
	public static int noiseLevel = 0;
	private String[] suddenTypes = {"no sudden obstacles", "fixed sudden", "moving sudden"};
	int suddenType = 0;
	private String[] driveTypes = {"velocity-model ", "accel-model"};
	public static boolean accModel = false;

	Color gridColor = Color.lightGray;
	boolean isAccurate = false;
	boolean enemy = false;

	JComboBox knownBox, taskBox, sceneBox, noiseBox, suddenBox, driveBox;
	JCheckBox enemyBox;
	//	String topMessage = "";
	//	Font msgFont = new Font ("Serif", Font.PLAIN, 15);

	JTextField xField, yField;
	double targetX=0, targetY=0;

	JTextField ipaddrField;
	JTextField carNumField;

	boolean hasStarted = false;

	public GridWorld(boolean isDiscrete) {
		GridWorld.isDiscrete = isDiscrete;
	}

	public void setTime(double time) {
		gwPanel.topMessage = "" + (time / 1000);
	}

	// TODO: separate class for settings panel?
	public JPanel getSettingsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(10, 1));

		JPanel inPanel = new JPanel ();
		taskBox = new JComboBox (tasks);
		inPanel.add (taskBox);

		sceneBox = new JComboBox (scenes);
		inPanel.add (sceneBox);
		panel.add (inPanel);

		inPanel = new JPanel ();
		knownBox = new JComboBox (blind);
		inPanel.add (knownBox);
		panel.add (inPanel);

		inPanel = new JPanel();
		inPanel.add (new JLabel("Godzilla? " ));
		enemyBox = new JCheckBox();
		enemyBox.setSelected(false);
		enemyBox.addItemListener (
				new ItemListener(){
					public void itemStateChanged(ItemEvent e){
						enemy = !enemy;
					}
				});
		inPanel.add(enemyBox);
		panel.add(inPanel);

		inPanel = new JPanel ();
		inPanel.add (new JLabel("# rand targets:"));
		targetField = new JTextField (3);
		targetField.setText ("" + numTargets);
		inPanel.add (targetField);
		panel.add (inPanel);


		inPanel = new JPanel ();
		noiseBox = new JComboBox (noiseLevels);
		inPanel.add (noiseBox);
		panel.add (inPanel);

		inPanel = new JPanel ();
		suddenBox = new JComboBox (suddenTypes);
		inPanel.add (suddenBox);
		panel.add (inPanel);

		inPanel = new JPanel ();
		driveBox = new JComboBox (driveTypes);
		inPanel.add (driveBox);
		panel.add (inPanel);

		inPanel = new JPanel ();
		JButton resetB = new JButton ("Reset");
		resetB.addActionListener (
				new ActionListener () {
					public void actionPerformed (ActionEvent a)
					{
						reset ();
					}
				}
				);
		inPanel.add (resetB);
		panel.add (inPanel);


		inPanel = new JPanel ();
		inPanel.add (new JLabel ("i:"));
		xField = new JTextField (3);
		inPanel.add (xField);
		inPanel.add (new JLabel ("j:"));
		yField = new JTextField (3);
		inPanel.add (yField);
		JButton addGoalB = new JButton ("Add target");
		addGoalB.addActionListener (
				new ActionListener () 
				{
					public void actionPerformed (ActionEvent a) 
					{
						addTarget ();
					}
				}
				);
		inPanel.add (addGoalB);
		panel.add (inPanel);

		return panel;
	}

	// TODO: These two methods seem silly together.
	public JPanel getViewPanel() {
		return gwPanel;
	}
	
	public GridWorldPanel getGridWorldPanel() {
		return gwPanel;
	}

	@Override
	public void doDraw() {
		gwPanel.repaint();
	}

	void readData ()
	{
		String knownStr = (String) knownBox.getSelectedItem ();
		if ((knownStr.equalsIgnoreCase("Known-targets"))) {
			knownTargets = true;
			knownObstacles = false;
		}
		else if ((knownStr.equalsIgnoreCase("Known-obstacles"))) {
			knownTargets = false;
			knownObstacles = true;
		}
		else if ((knownStr.equalsIgnoreCase("Both-unknown"))) {
			knownTargets = false;
			knownObstacles = false;
		}
		else {
			knownTargets = true;
			knownObstacles = true;
		}

		String taskStr = (String) taskBox.getSelectedItem ();
		taskNum = 1;
		if (taskStr.endsWith("1")) taskNum = 1;
		else if (taskStr.endsWith("2")) taskNum = 2;
		else if (taskStr.endsWith("3")) taskNum = 3;
		else if (taskStr.endsWith("4")) taskNum = 4;
		else if (taskStr.endsWith("5")) taskNum = 5;
		else if (taskStr.endsWith("6")) taskNum = 6;
		else {
			System.out.println ("Illegal task");
			System.exit (0);
		}

		String sceneStr = (String) sceneBox.getSelectedItem ();
		sceneNum = 1;
		if (sceneStr.endsWith("2")) {
			sceneNum = 2;
		}
		else if (sceneStr.endsWith("3")) {
			sceneNum = 3;
		}

		String noiseStr = (String) noiseBox.getSelectedItem ();
		noiseLevel = 0;
		if (noiseStr.startsWith ("low")) {
			noiseLevel = 1;
		}
		else if (noiseStr.startsWith ("high")) {
			noiseLevel = 2;
		}

		String suddenStr = (String) suddenBox.getSelectedItem ();
		suddenType = 0;
		if (suddenStr.startsWith ("fixed")) {
			suddenType = 1;
		}
		else if (noiseStr.startsWith ("moving")) {
			suddenType = 2;
		}

		String driveStr = (String) driveBox.getSelectedItem ();
		accModel = false;
		if (driveStr.startsWith("acc")) {
			accModel = true;
		}

		try {
			int n = Integer.parseInt (targetField.getText().trim());
			if (n >= 0) {
				numTargets = n;
			}
		}
		catch (Exception e) {
		}
	}

	public void reset()
	{
		simulator.Master.status("Reseting GridWorld");
		//Send complete messages
		for(Car c : enemies) {
			if(c.getSocket() != null){
				c.getSocket().sendFinished();
			}
		}

		hasStarted = false;

		readData ();
		gwPanel.computeGridLines ();

		setScene ();

		gwPanel.setTargets(numTargets);

		stopEnemies();

		//This is solely so that we can make sure the sockets aren't bound
		//When we try to reconnect them.
		if(enemies.size() > 0){
			Car firstEnemy = enemies.get(0);
			enemies = new ArrayList<Car>();
			enemyThread = new ArrayList<Thread>();

			//We need to spin while the previous thread ends.
			do{
				try{
					Thread.sleep(1000);
				}catch(Exception e){System.out.println(e);}
			}while(!firstEnemy.getSocket().isConnected());

			firstEnemy.getSocket().finalize();	    
		}

		setEnemy();

		gwPanel.repaint ();
	}

	void setScene ()
	{
		gwPanel.initGridAndBorder();

		// Scene 1: no obstacles
		if (sceneNum == 1) {
			return;
		}
		else if (sceneNum == 2) {
			gwPanel.basicGrid();
		}
		else if (sceneNum == 3) {
			// Read from file scene.txt
			readScene();
		}
	}	

	void readScene ()
	{
		try {
			LineNumberReader lnr = new LineNumberReader (new FileReader ("scene.txt"));
			String line = lnr.readLine ();
			while (line != null) {
				// line should have coords.
				Scanner s = new Scanner (line);
				Point p = new Point(s.nextInt(), s.nextInt());
				// Put an obstacle at this location.
				Obstacle R = new Obstacle(gwPanel);
				R.setGridLocation(p);
				gwPanel.addObstacle(R);
				// TODO: Do this in the add method!
				line = lnr.readLine ();
			}
		}
		catch (IOException e){
			System.out.println (e);
		}
	}

	boolean checkCars ()
	{
		for (Car c: enemies){
			c.getSocket().sendInit();
		}

		hasStarted = true;
		return true;
	}

	void stopEnemies(){
		for(Thread t : enemyThread){
			while(t.isAlive()){
				t.interrupt();
			}
		}
	}

	void setEnemy(){
		if(enemy){
			initEnemy(GridWorldPanel.M);
		}
	}

	void initEnemy(int cNum){
		setEnemy(cNum);

		//Setup enemy controller
		controllers.Monster m = new controllers.Monster(cNum, false);
		Thread t = new Thread(m);
		t.start();
		enemyThread.add(t);

		addEnemyController(cNum);
	}

	void addTarget ()
	{	
		try {
			// Get grid location.
			int tx = Integer.parseInt(xField.getText().trim());
			int ty = Integer.parseInt(yField.getText().trim());
			// See if target already exists.
			if (gwPanel.grid[tx][ty] != null) {
				simulator.Master.error("Could not add target");
				return;
			}
			// If not, add target.
			Target d = new Target(gwPanel);
			d.setGridLocation(new Point(tx, ty));
			gwPanel.addTarget(d);
			numTargets ++;
		}
		catch (Exception e) {
			simulator.Master.error("Could not add target");
		}
		gwPanel.repaint ();
	}

	void addEnemyController(int carNum){
		String ipAddr = "localhost";
		Car c = enemies.get(GridWorldPanel.M-carNum);
		SocketHandler h = new SocketHandler();
		c.setSocket(h);
		boolean ok = h.init(this, c, ipAddr, simulator.Master.BASE_PORT + c.getCarNum());

		if (!ok) {
			simulator.Master.error("Could not initialize socket");
		}
		else {
			simulator.Master.status("Socket initialized to enemy");
		}

	}

	void setEnemy(int carNum){
		Car c = new Car(carNum, gwPanel, accModel);
		int gridx = (carNum - GridWorldPanel.M);
		int gridy = GridWorldPanel.N - 1;

		Orientation newOrien = new Orientation();
		newOrien.location.x = gridx * GridWorldPanel.cellWidth + GridWorldPanel.cellWidth / 2;
		newOrien.location.y = gridy * GridWorldPanel.cellHeight + GridWorldPanel.cellHeight / 2;
		newOrien.theta = 0;
		newOrien.velocity = 0;

		c.initPhysicsChannel(physics, newOrien);
		enemies.add(c);
	}

	@Override
	public PlannableObject initNewPlannable(int index) {
		// The cars will start at the bottom row.
		Car c = new Car (index, gwPanel, accModel);

		Orientation newOrien = new Orientation();
		newOrien.location.x = index * GridWorldPanel.cellWidth + GridWorldPanel.cellWidth / 2;
		newOrien.location.y = GridWorldPanel.cellHeight / 2;
		newOrien.theta = 0;
		newOrien.velocity = 0;

		c.initPhysicsChannel(physics, newOrien);
		return c;
	}


	public boolean isDone(){
		//	System.out.println("IN DONE");
		boolean retVal = false;
		switch(taskNum){
		case 1:
			retVal = true;
			ArrayList<Target> tergets = gwPanel.getTargets();
			for(int i = 0; i < numTargets && retVal; i++){
				retVal = tergets.get(i).isHeld();
				//		if(!retVal){
				//		    System.out.println("Target " + i + " is not held");
				//		    System.out.flush();
				//		}
			}

			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		case 5:
			break;
		default:
			break;
		}

		return retVal;
	}
	@Override
	public void init(Physics physics) {
		this.physics = physics;
	}
}
