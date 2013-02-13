package gridworld;

import gridworld.environments.GridWorldPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import simulator.Master;
import simulator.Orientation;
import simulator.Sensor;

/**
 * CarModel.java
 * 
 * Implements the physical properties of a {@link Car},
 * including how to draw it. Keeps track of {@link Orientation} for
 * itself and its related {@link Sensor}s.
 * 
 * Large portions ripped from Simha's original Car.java.
 * 
 * Jun 20, 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 *
 */
@SuppressWarnings("serial")
public class CarModel extends GridObject implements simulator.PhysicsObject {
	// The limits for either velocity or acceleration control values.
	public static final double C_LOW = -10;
	public static final double C_HIGH = 10;

	static double ZERO_VELOCITY_ERROR = 0.2;

	static double r = 2.0;                 // Wheel radius.
	static double d = 1.6;                 // Wheel thickness.
	static double S = 3.6;                 // Wheel spacing.
	static double L = 6.0;                 // Car length.

	Orientation orientation;
	double distMoved = 0.0;
	boolean isAccelModel;
	double wheel_1_velocity = 0.0;
	double wheel_2_velocity = 0.0;
	CarControl controls = new CarControl(0.0, 0.0);

	double targetRadius = 1.2;
	Color bodyColor = Color.gray;

	// Needs a reference to all sensors so that it can update their location as
	// needed (ie. when the model's orientation is changed.
	ArrayList<Sensor> Sensors;

	public CarModel(GridWorldPanel gwPanel, ArrayList<Sensor> Sensors, Orientation orientation, boolean isAccelModel) {
		super(gwPanel);
		this.Sensors = Sensors;
		setOrientation(orientation);
		this.isAccelModel = isAccelModel;
	}
	
	@Override
	public Point getGridLoc() {
		gridLoc = GridWorldPanel.identifyCell(orientation.location);
		return gridLoc;
	}

	public static double fixRange (double c) {
		if (c < C_LOW) {
			c = C_LOW;
		}
		else if (c > C_HIGH) {
			c = C_HIGH;
		}
		return c;
	}

	public void newControlInput(CarControl newControl) {
		controls.left_wheel = fixRange(newControl.left_wheel);
		controls.right_wheel = fixRange(newControl.right_wheel);
	}

	public void nextStep (double delT) {
		double c1, c2;
		if (! isAccelModel) {
			c1 = controls.left_wheel;
			c2 = controls.right_wheel;
		}
		else {
			c1 = wheel_1_velocity + delT * controls.left_wheel;
			c2 = wheel_2_velocity + delT * controls.right_wheel;
		}

		double[] ret = calcStep(c1, c2, delT, orientation.theta, r, S);

		updateOrientation(ret[0], ret[1], ret[2], ret[3]);
		distMoved += Math.sqrt (ret[0]*ret[0] + ret[1]*ret[1]);
		gridLoc = GridWorldPanel.identifyCell(orientation.location); 

		// Update sensors
		for (Sensor s : Sensors) {
			s.nextStep(delT);
		}
	}

	/*
	 * Calculates how far the robot will move give the wheel velocities,
	 * time, theta, wheel radius, and wheel spacing. 
	 * Returns the change in x, y, angle, and velocity.
	 */
	private static double[] calcStep(double v1, double v2, double delT, double theta, double r, double S) {
		double[] res = new double[4];
		res[0] = delT * r * 0.5 * (v1 + v2) * Math.cos(theta); //delX
		res[1] = delT * r * 0.5 * (v1 + v2) * Math.sin(theta); //delY
		res[2] = delT * r * (v2 - v1) / S; // delTheta
		res[3] = delT * r * 0.5 * (v1 + v2); // delV

		return res;
	}

	private void continuousStop() {
		if (! velNearZero()) {
			// Can't apply.
			return;
		}

		if (isAccelModel) {
			wheel_1_velocity = 0;  wheel_2_velocity = 0;
			controls.left_wheel = 0;  controls.right_wheel = 0;
		}
		else {
			controls.left_wheel = 0;  controls.right_wheel = 0;
		}
	}

	private void continuousPick ()
	{
		if (! checkPickDrop() ) {
			return;
		}

		double min_distance = 100000;
		Target near_target = null;
		for (Target t : gwPanel.getTargets()) { // Find closest target
			double distance = orientation.location.distance(t.x, t.y);
			if (distance < min_distance) {
				min_distance = distance;
				near_target = t;
			}						    
		}

		if (near_target != null) {
			double new_t_x = near_target.x - orientation.location.x;
			double new_t_y = near_target.y - orientation.location.y;
			System.out.println("Nearest Target: " + new_t_x + "," + new_t_y + "!");
			System.out.println("min_distance: " + min_distance);
			new_t_x = new_t_x * Math.cos(orientation.theta) - y * Math.sin(orientation.theta);
			new_t_y = new_t_x * Math.sin(orientation.theta) + y * Math.cos(orientation.theta);

			double angle_between = Math.atan2(new_t_x, new_t_y);
			System.out.println("angle_between: " + angle_between);
			if (min_distance <= 5 && Math.abs(angle_between) < Math.PI) {
				heldTarget = near_target;
				near_target.setHeld(true);
				gwPanel.grid[near_target.getGridLoc().x][near_target.getGridLoc().x] = null;
			}
		}
	}

	public static boolean validCell(Point cell, GridWorldPanel gwp) {
		boolean retVal = (cell.x >= 0) && (cell.x < GridWorldPanel.M);
		retVal = retVal && (cell.y >= 0) && (cell.y < GridWorldPanel.N);
		retVal = retVal && (gwp.grid[cell.x][cell.y] == null);
		return retVal;
	}
	
	private void continuousDrop() {
		if (heldTarget == null) {
			simulator.Master.error("Drop: not carrying target");
			return;
		}

		// Check if velocity near zero and grid-parallel orientation.
		if (! checkPickDrop() ) {
			return;
		}
		
		// Get approx orientation.
		Cardinal heading = contHeadingToCardinal(orientation.theta);
		// Get the neighboring cell in that direction.
		Point nextCell = computeNextCell(heading, gridLoc);
		System.out.println("gridLoc: " + gridLoc.x + ", " + gridLoc.y);
		System.out.println("nextCel: " + nextCell.x + ", " + nextCell.y);
		
		if (validCell(nextCell, gwPanel)) {
			gwPanel.grid[nextCell.x][nextCell.y] = heldTarget;
			heldTarget.setHeld(false);
			heldTarget.setGridLocation(nextCell);
			heldTarget = null;
		}
	}

	private boolean checkPickDrop() {
		// Check if velocity is near zero.
		if ( (wheel_1_velocity != 0) || (wheel_2_velocity != 0) ) {
			simulator.Master.error("Can't pick/drop: vel not zero");
			return false;
		}
		return true;
	}

	/*
	 * (Read in a the "Dramatic Movie Trailer Guy's Voice")
	 * In a world where values are continuous, tough decisions must be made.
	 * Decisions that discretize domains. And in this function, that is exactly
	 * what must be done. 
	 */
	private static Cardinal contHeadingToCardinal(double theta) {
		double min = Math.abs(theta);
		Cardinal heading = Cardinal.EAST;
		double e = Math.abs(theta - Math.PI/2);
		if (e < min) {
			min = e;
			heading = Cardinal.NORTH;
		}
		e = Math.abs (theta - Math.PI);
		if (e < min) {
			min = e;
			heading = Cardinal.WEST;
		}
		e = Math.abs (theta - 3*Math.PI/2);
		if (e < min) {
			min = e;
			heading = Cardinal.SOUTH;
		}
		return heading;
	}

	private boolean velNearZero () {
		if ((Math.abs(wheel_1_velocity) > ZERO_VELOCITY_ERROR) ||
				(Math.abs(wheel_2_velocity) > ZERO_VELOCITY_ERROR)) {
			return false;
		}
		return true;
	}

	// TODO: Make sure it is being updated in the discrete case
	Cardinal discreteHeading = Cardinal.EAST;
	Target heldTarget = null;

	public static Point computeNextCell(Cardinal heading, Point current) {
		Point next = new Point(-1, -1);
		if (heading== Cardinal.NORTH) {
			next.x = current.x;  next.y = current.y + 1;
		}
		else if (heading == Cardinal.WEST) {
			next.x = current.x - 1;  next.y = current.y;
		}
		else if (heading == Cardinal.SOUTH) {
			next.x = current.x;  next.y = current.y - 1;
		}
		else {
			next.x = current.x + 1;  next.y = current.y;
		}

		return next;
	}

	public void continuousMove(String actionStr) {
		if (actionStr.equalsIgnoreCase("stop")) {
			this.continuousStop();
		} else if (actionStr.equalsIgnoreCase("pick")) {
			this.continuousPick();
		} else if (actionStr.equalsIgnoreCase("drop")) {
			this.continuousDrop();
		} else {
			Master.error("Incorrect message to CarModel: " + actionStr);
		}
	}

	public void discreteMove(String actionStr) {
		// TODO: Make sure discreteHeading and gridLoc are set correctly
		Point nextCell = computeNextCell(discreteHeading, gridLoc);
		// Now handle actions.
		if (actionStr.equals ("go")) {
			discreteMove (gwPanel, this, nextCell);
		}
		else if (actionStr.equals ("left")) {
			updateOrientation(0.0, 0.0, Math.PI/2, 0.0);
			discreteHeading = Cardinal.turnLeft(discreteHeading);
		}
		else if (actionStr.equals ("right")) {
			updateOrientation(0.0, 0.0, -Math.PI/2, 0.0);
			discreteHeading= Cardinal.turnRight(discreteHeading);
		}
		else if (actionStr.equals ("pick")) {
			// Check if target is in next cell.
			if ( gwPanel.grid[nextCell.x][nextCell.y] instanceof Target) {
				heldTarget = (Target) gwPanel.grid[nextCell.x][nextCell.y];
				gwPanel.grid[nextCell.x][nextCell.y] = null;
				heldTarget.setHeld(true);
				// simulator.Master.status("Car# " + carNum + " picked target at [" + nextX + "," + nextY + "]");
			}
		}
		else if (actionStr.equals ("drop")) {
			if (heldTarget == null) {
				simulator.Master.error("Not carrying target");
				return;
			}
			if (validCell(nextCell, gwPanel)) {
				gwPanel.grid[nextCell.x][nextCell.y] = heldTarget;
				heldTarget.setHeld(false);
				heldTarget.setGridLocation(new Point(nextCell.x, nextCell.y));
				heldTarget = null;
			}
			else {
				simulator.Master.error ("Cannot drop target in occupied cell");
				return;
			}
		}
		else {
			// No such action.
			simulator.Master.error("No such action " + actionStr); 
			return;
		}
		//}
		// TODO: Shouldn't painting be handled by the master loop?
		gwPanel.repaint ();
	}

	private static void discreteMove (GridWorldPanel gwPanel, CarModel c, Point next)
	{
		gwPanel.grid[c.gridLoc.x][c.gridLoc.y] = null; // James - must vacate old cell
		c.gridLoc.x = next.x;  c.gridLoc.y = next.y;
		Point2D.Double location = GridWorldPanel.identifyLoc(c.gridLoc);
		double dx = location.x - (c.orientation.location.x);
		double dy = location.y - (c.orientation.location.y);
		c.updateOrientation(dx, dy, 0.0, 0.0);
		gwPanel.grid[next.x][next.y] = c;
	}

	// TODO: should check for other models too
	public boolean hitObstacle() {
		if ( gwPanel.getObstacles() == null || (gwPanel.getObstacles().size() == 0) ) {
			return false;
		}

		Graphics2D g2 = (Graphics2D) gwPanel.getGraphics();
		AffineTransform oldTransform = g2.getTransform ();

		int affX = GridWorldPanel.realToJavaX(orientation.location.x);
		int affY = GridWorldPanel.realToJavaY(orientation.location.y);
		AffineTransform rotatorTrans = AffineTransform.getRotateInstance(-orientation.theta, affX,affY);
		g2.setTransform (rotatorTrans);

		// Make the three rectangles for the body and two wheels, then
		// check whether these rectangles intersect the obstacles.
		Rectangle2D.Double[] rects = new Rectangle2D.Double [3];

		// A = top left of main part.
		int Ax = (int) (orientation.location.x + CarModel.r - CarModel.L);
		int Ay = (int) (orientation.location.y + CarModel.S/2.0);
		int Axjava = GridWorldPanel.realToJavaX (Ax);
		int Ayjava = GridWorldPanel.realToJavaY (Ay);
		int w = GridWorldPanel.realToJava (CarModel.L);
		int h = GridWorldPanel.realToJava (CarModel.S);
		rects[0] = new Rectangle2D.Double (Axjava, Ayjava, w, h);

		// Left wheel
		int Bxjava = Axjava + GridWorldPanel.realToJava(CarModel.L - (2 * CarModel.r));
		int Byjava = Ayjava - GridWorldPanel.realToJava(CarModel.d);
		w = GridWorldPanel.realToJava (2*CarModel.r);
		h = GridWorldPanel.realToJava (CarModel.d);
		rects[1] = new Rectangle2D.Double (Bxjava, Byjava, w, h);

		// Right wheel
		int Cxjava = Bxjava;
		int Cyjava = Ayjava + GridWorldPanel.realToJava(CarModel.S);
		rects[2] = new Rectangle2D.Double(Cxjava, Cyjava, w, h);

		boolean intersectFound = false;
		for (int i=0; i<3; i++) {
			Shape rotatedRect = rotatorTrans.createTransformedShape (rects[i]);
			for (Obstacle O: gwPanel.getObstacles()) {
				// See if rotatedRect intersects O.
				int Rxjava = GridWorldPanel.realToJavaX (O.x);
				int Ryjava = GridWorldPanel.realToJavaY (O.y);
				w = GridWorldPanel.realToJava (O.width);
				h = GridWorldPanel.realToJava (O.height);
				Rectangle2D.Double Rflip = new Rectangle2D.Double (Rxjava, Ryjava, w, h);
				if (rotatedRect.intersects(Rflip)) {
					intersectFound = true;
				}
			}
		}
		g2.setTransform (oldTransform);
		return intersectFound;
	}

	private static double angleFix (double a)
	{
		// Make each angle an angle between 0 and 2*PI.
		//** Note: this code can be optimized.
		if (a < 0) {
			while (a < 0) {
				a = a + 2*Math.PI;
			}
		}
		else if (a > 2*Math.PI) {
			while (a > 2*Math.PI) {
				a = a - 2*Math.PI;
			}
		}
		return a;
	}

	public double getV1() {
		return this.wheel_1_velocity;
	}
	
	public double getV2() {
		return this.wheel_2_velocity;
	}

	@Override
	public void draw (Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform oldTransform = g2.getTransform ();

		int affX = GridWorldPanel.realToJavaX(orientation.location.x);
		int affY = GridWorldPanel.realToJavaY(orientation.location.y);
		g2.setTransform (AffineTransform.getRotateInstance(-orientation.theta, affX, affY));

		// A = top left of main part.
		// B = top left of left wheel.
		// C = top left of right wheel.
		double Ax = (orientation.location.x + CarModel.r - CarModel.L);
		double Ay = (orientation.location.y + CarModel.S/2.0);

		// Main.
		g2.setColor (bodyColor);
		int Axjava = GridWorldPanel.realToJavaX(Ax);
		int Ayjava = GridWorldPanel.realToJavaY(Ay);
		int w = GridWorldPanel.realToJava(CarModel.L);
		int h = GridWorldPanel.realToJava(CarModel.S);
		g2.fillRect (Axjava, Ayjava, w, h);

		// Left wheel.
		// Now drawn relative to the main body (right wheel and target as well) - James
		g2.setColor (Color.gray);
		int Bxjava = Axjava + GridWorldPanel.realToJava(CarModel.L - (2 * CarModel.r));
		int Byjava = Ayjava - GridWorldPanel.realToJava(CarModel.d);
		w = GridWorldPanel.realToJava (2*CarModel.r);
		h = GridWorldPanel.realToJava (CarModel.d);
		g2.fillRect (Bxjava,Byjava, w, h);

		// Right wheel.
		int Cxjava = Bxjava;
		int Cyjava = Ayjava + GridWorldPanel.realToJava(CarModel.S);
		g2.fillRect (Cxjava,Cyjava, w, h);

		// If carrying target.
		if (heldTarget != null) {
			int tRadius = GridWorldPanel.realToJava (targetRadius);
			int Txjava = Axjava + GridWorldPanel.realToJava(CarModel.L - CarModel.r) - tRadius;
			int Tyjava = Ayjava + GridWorldPanel.realToJava(CarModel.S/2.0) - tRadius;
			g2.setColor (Color.green);
			g2.fillRect (Txjava,Tyjava, 2*tRadius, 2*tRadius);
		}

		g2.setTransform (oldTransform);
	}

	@Override
	public void setOrientation(Orientation o) {
		orientation = o;
		
		for (Sensor s : Sensors) {
			s.setOrientation(o);
		}
	}

	@Override
	public void updateOrientation(double dx, double dy, double dtheta, double dvelocity) {
		orientation.location.x += dx;
		orientation.location.y += dy;
		orientation.theta += dtheta;
		orientation.theta = angleFix(orientation.theta);
		orientation.velocity += dvelocity;
		
		for (Sensor s : Sensors) {
			s.updateOrientation(dx, dy, dtheta, dvelocity);
		}
	}

	@Override
	public Orientation getOrientation() {
		return orientation;
	}
}
