package simulator;

/**
 * Master.java
 * The main control loop for the program, also handles the GUI
 * 
 * Large chucks of code pulled from Rahul Simha's original GridWorld.java
 * 
 * May 2012
 * @author James Marshall <jcmarsh@gwmail.gwu.edu>
 */

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

// Should get this base version working quickly

public class Master {
	// environment (GridWorld)
	static Environment environment;
	ArrayList<PlannableObject> pObjects = new ArrayList<PlannableObject>();
	private int numPObjects = 1;
	
	// physics
	private static Physics physics;
	private static long current_time = -1;
    private static long elapsed_time = 0;
	private static int delta_time = 0;	

	public static final int BASE_PORT = 9000;
	public static final int FRAME_WIDTH = 1100;
	public static final int FRAME_HEIGHT = 700;

	// Animation stuff.
    private static enum State {BOOT, GO, PAUSE, DONE}
	private static State state = State.BOOT;

	private static MasterCP masterCP;
	
	JFrame mainFrame;
	JPanel controlPanel;
	JPanel viewPanel;
	JPanel textConsole;

	private void makeFrame() {
		if (mainFrame == null) {
			mainFrame = new JFrame();
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
			mainFrame.setTitle("Simulator");
			
			masterCP = new MasterCP(this);
			controlPanel = masterCP.makeControlPanel(environment.getSettingsPanel(), numPObjects);
			viewPanel = environment.getViewPanel();
			textConsole = masterCP.makeTextConsole(FRAME_WIDTH);
		} else {
			//mainFrame.removeAll();
			controlPanel = masterCP.makeControlPanel(environment.getSettingsPanel(), numPObjects);
			viewPanel = environment.getViewPanel();
		}

		Container cPane = mainFrame.getContentPane();
		cPane.removeAll();
		cPane.add(controlPanel, BorderLayout.LINE_START);
		cPane.add(viewPanel, BorderLayout.CENTER);
		cPane.add(textConsole, BorderLayout.PAGE_END);
		cPane.validate();
		cPane.repaint();
		//mainFrame.validate();
		//mainFrame.repaint();
		mainFrame.setVisible(true);
	}

	public static JPanel getViewPanel() {
		return environment.getViewPanel();
	}
	
	public Master () { 
		physics = new Physics();
	}

	public static void main(String[] args) {
		// initialize
		Master m = new Master();
		// Default environment
		environment = new gridworld.environments.GridWorld(true);
		m.makeFrame();
		status("Initialized Master");

		while (true) {
			switch (state) {
			case BOOT:
				m.reset();
				break;
			case GO:
				long new_time = System.currentTimeMillis();
				if (current_time >= 0) {
					delta_time = (int) (new_time - current_time);					
				}
				elapsed_time += delta_time;
				environment.setTime(elapsed_time);
				current_time = new_time;
				
				physics.update(delta_time);
				
				environment.doDraw();

				// push communication

				//Check to see if done
				if(environment.isDone()){
				    System.out.println("DONE!");
				    state = State.DONE;
				}
				break;
			case PAUSE:
			case DONE:
			    break;
			}
		}
	}
	///	// TODO: Look up what Rendering Hints are!
	//	RenderingHints rh = g2.getRenderingHints();
	//	rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	//	rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	//	g2.setRenderingHints(rh);
	//}

	void go() {
		if (state == State.BOOT){
			reset();
		}
		
		numPObjects = masterCP.readNumPObjects();
		// check if ready!
		boolean ready = true;
		int count = 0;
		if (pObjects != null) {
		    for (PlannableObject po : pObjects) {
			if (!po.getSocket().isActive()) {
			    Master.error("Plannable Object has no controller");
			    ready = false;
			}
			count++;
		    }
		} else {
			ready = false;
			error("No Plannable Objects initialized.");
		}
		
		if (count != numPObjects) {
			error("Not enough (or too many?) controllers!");
			ready = false;
		}
		
		if (ready) {
			for (PlannableObject po : pObjects) {
				po.getSocket().sendInit();
			}
			state = State.GO;
		}
	}
	
	void reset() {
		physics = new Physics();
		current_time = -1;
		delta_time = 0;
		
		if (pObjects != null) {
			for (PlannableObject po : pObjects) {
				if (po.getSocket() != null) {
					po.getSocket().sendFinished();
				}
				po = null;
			}
		}
		
		pObjects = null;
		
		environment = masterCP.getEnvironment();
		makeFrame();
		environment.reset();
		environment.init(physics);
		numPObjects = masterCP.readNumPObjects();
		state = State.PAUSE;
	}

	void pause() {
		state = State.PAUSE;
	}

	void quit() {
		System.exit(0);
	}

	void addController(JTextField ipaddrField, JTextField pObjectNumField)
	{
		// Retrieve car#
		int pObjectNum = 0;
		try {
			int n = Integer.parseInt (pObjectNumField.getText().trim());
			if ((n >= 0) && (n < numPObjects)) {
				pObjectNum = n;
			}
		}
		catch (Exception e) {
			error("Improper car#: cannot connect");
			return;
		}

		String ipAddr = ipaddrField.getText().trim();

		// TODO: This code is surely problematic.
		if (pObjects == null) {
			pObjects = new ArrayList<PlannableObject>();
		}
		if (pObjectNum <= pObjects.size()) {
			for (int i = 0; i <= pObjectNum; i++) {
				if (i <= pObjects.size()) {
					PlannableObject po = environment.initNewPlannable(i);
					pObjects.add(po);
				}
			}
		}
		
		// Make a socket
		PlannableObject po = pObjects.get(pObjectNum);
		gridworld.SocketHandler handler = new gridworld.SocketHandler ();
		po.setSocket(handler);
		boolean ok = handler.init(environment, po, ipAddr, BASE_PORT + pObjectNum);
		if (!ok) {
			error("Could not initialize socket");
		}
		else {
			status("Socket initialized to controller");
		}
	}

	public static void error (String msg)
	{
		MasterCP.error(msg);
	}

	public static void status (String msg)
	{
		MasterCP.status(msg);
	}
}