package gridworld.environments;

/**
 * GridWorldPanel.java
 * 
 * Maintains the "Grid" for the GridWorld environment, including all of the
 * {@link GridObject}s within.
 * 
 * Large chucks of code pulled from Rahul Simha's original GridWorld.java
 * 
 * May 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 */

import gridworld.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.text.*;
import java.util.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class GridWorldPanel extends JPanel {
	public static double MIN_X=0, MAX_X=320, MIN_Y=0, MAX_Y=270;    // Bounds.
	private static int INSET = 60;
	public static int M = 32, N = 27; // # of columns, # of rows, both starting at bottom left

	int numIntervalsX = 16;                        // # tick marks.
	int numIntervalsY = 27;                        // # tick marks.
	static Dimension D;             // Size of drawing area.
	int pointDiameter = 6;                        // Size of dot.

	DecimalFormat df = new DecimalFormat ("##.##");

	public static double cellWidth, cellHeight;

	// Not required, but makes finding GridObjects faster
	public GridObject[][] grid;

	private ArrayList<Obstacle> obstacles;
	private ArrayList<Target> targets;
	private ArrayList<Line> gridLines;
	private ArrayList<CarModel> cars;

    String topMessage = "Hello!";
        
    // To store the gradient
    BufferedImage gradient = null;

	public GridWorldPanel() {
		obstacles = new ArrayList<Obstacle>();
		targets = new ArrayList<Target>();
		gridLines = new ArrayList<Line>();
		cars = new ArrayList<CarModel>();
	}
	
	public void initGradient() {
		int w = D.width - 2 * INSET;
		int h = D.height - 2 * INSET;
		gradient = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				gradient.setRGB(i, j, i * j);
			}
		}
	}
	
	// From continuous location to a discrete cell
	public static Point identifyCell(Point2D.Double loc) {
		return new Point((int) (loc.x / cellWidth), (int) (loc.y / cellHeight));
	}
	
	// From a discrete cell to a continuous location
	public static Point2D.Double identifyLoc(Point cell) {
		Point2D.Double retVal = new Point2D.Double(0.0, 0.0);
		retVal.x = cellWidth * cell.x + cellWidth / 2;
		retVal.y = cellHeight * cell.y + cellHeight / 2;
		return retVal;
	}
	
	public static void setDimension(Dimension Dim) {
		D = Dim;
	}

	public static int realToJava (double dist)
	{
		// Here's where we assume that it's a square.
		int scaledDist = (int) ((dist-MIN_X) / (MAX_X-MIN_X) * (D.width-2*INSET));
		return scaledDist;
	}

	public static int realToJavaX (double x)
	{
		int scaledX = (int) ((x-MIN_X) / (MAX_X-MIN_X) * (D.width-2*INSET));
		return (INSET + scaledX);
	}

	public static int realToJavaY (double y)
	{
		int scaledY = (int) ((y-MIN_Y) / (MAX_Y-MIN_Y) * (D.height-2.0*INSET) );
		return (D.height - INSET - scaledY);
	}

	public static double javaToRealX (int jX)
	{
		int scaledX = jX - INSET;
		double x = MIN_X + scaledX * (MAX_X-MIN_X)/ (D.width-2.0*INSET);
		return x;
	}

	public static double javaToRealY (int jY)
	{
		int scaledY = D.height - INSET - jY;
		double y = MIN_Y + scaledY * (MAX_Y-MIN_Y)/ (D.height-2.0*INSET);
		return y;
	}

	public static void drawLine (Graphics g, GridObject L)
	{
		if (L == null) {
			return;
		}
		int x1 = realToJavaX (L.x);
		int y1 = realToJavaY (L.y);
		int x2 = realToJavaX (L.x + L.width);
		int y2 = realToJavaY (L.y + L.height);
		g.drawLine (x1, y1, x2, y2);
	}


	public static void drawRectangleOrOval (Graphics g, GridObject R, boolean isRect)
	{
		if (R == null) {
			return;
		}
		int x1 = realToJavaX (R.x);
		int y1 = realToJavaY (R.y);
		double x = R.x + R.width;
		double y = R.y - R.height;
		int x2 = realToJavaX (x);
		int y2 = realToJavaY (y);
		if (isRect) {
			g.fillRect (x1, y1, x2-x1, y2-y1);
		}
		else {
			g.fillOval (x1, y1, x2-x1, y2-y1);
		}
	}

	public static void drawBoundingBox(Graphics g) {
		g.setColor (Color.gray);
		g.drawLine (INSET, D.height -INSET, D.width - INSET, D.height - INSET);
		g.drawLine (INSET, INSET, INSET, D.height - INSET);
		g.drawLine (D.width - INSET, INSET, D.width - INSET, D.height - INSET);
		g.drawLine (INSET, INSET, D.width - INSET, INSET);
	}

	public void paintComponent (Graphics g) {
		super.paintComponent (g);

		Graphics2D g2 = (Graphics2D)g;
		RenderingHints rh = g2.getRenderingHints();
		rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHints(rh);

		// Clear.
		D = this.getSize();
		//System.out.println ("D=" + D);
		g.setColor (Color.white);
		g.fillRect (0,0, D.width,D.height);

		if (gradient != null) {
			g.drawImage(gradient, INSET, INSET, null);
		}
		
		// Axes, bounding box.
		drawBoundingBox(g);

		double xDelta = (MAX_X - MIN_X) / numIntervalsX;

		// X-ticks and labels.
		for (int i=1; i<=numIntervalsX; i++) {
			double xTickd = i * xDelta;
			int xTick = realToJavaX(xTickd);
			g.drawLine (xTick, D.height - INSET - 5, xTick, D.height - INSET + 5);
			double x = MIN_X + i * xDelta;
			g.drawString(df.format(x), xTick - 5, D.height - INSET + 20);
		}

		// Y-ticks
		double yDelta = (MAX_Y - MIN_Y) / numIntervalsY;
		for (int i=0; i<numIntervalsY; i++) {
			int yTick = (i+1) * (int) ((D.height - 2 * INSET) / (double)numIntervalsY);
			g.drawLine(INSET - 5, D.height - yTick - INSET, INSET + 5, D.height - yTick - INSET);
			double y = MIN_Y + (i+1)  * yDelta;
			g.drawString (df.format(y), 1, D.height - yTick - INSET);
		}

		// Save current orientation, because the car will change it.
		AffineTransform savedTransform = g2.getTransform ();

		for (Target t : targets) {
			t.draw(g2);
		}
		for (Obstacle o : obstacles) {
			o.draw(g2);
		}
		for (Line l : gridLines) {
			l.draw(g2);
		}
		for (CarModel cm : cars) {
			cm.draw(g2);
		}

		// Restore.
		g2.setTransform (savedTransform);

		g2.setColor(Color.black);
		g2.drawString(topMessage, 20, 30);
	}

	void computeGridLines () 
	{
		gridLines = new ArrayList<Line> ();
		cellWidth = (MAX_X - MIN_X) / M;
		cellHeight = (MAX_Y - MIN_Y) / N;

		double x = cellWidth;
		while (x < MAX_X) {
			Line L = new Line(this);
			L.x = x;    L.y = 0;
			L.width = 0;   L.height = MAX_Y;
			gridLines.add (L);
			x += cellWidth;
		}

		double y = cellHeight;
		while (y < MAX_Y) {
			Line L = new Line(this);
			L.x = 0;    L.y = y;
			L.width = MAX_X;   L.height = 0;
			gridLines.add(L);
			y += cellHeight;
		}
	}

	void initGridAndBorder()
	{
		obstacles = new ArrayList<Obstacle>();

		// Put in the four walls as hidden obstacles.
		double e = 10;
		Obstacle R = new Obstacle(this);

		// Left side:
		R.x = MIN_X - e;
		R.y = MAX_Y + e;
		R.width = e;
		R.height = (MAX_Y - MIN_Y) + 2*e;
		R.show(false);
		obstacles.add(R);

		// Right side:
		R = new Obstacle(this);
		R.x = MAX_X;
		R.y = MAX_Y + e;
		R.width = e;
		R.height = (MAX_Y - MIN_Y) + 2*e;
		R.show(false);
		obstacles.add (R);

		// Top side:
		R = new Obstacle(this);
		R.x = MIN_X - e;
		R.y = MAX_Y + e;
		R.width = (MAX_X - MIN_X) + 2 * e;
		R.height = e;
		R.show(false);
		obstacles.add(R);

		// Bottom side:
		R = new Obstacle(this);
		R.x = MIN_X - e;
		R.y = MIN_Y;
		R.width = (MAX_X - MIN_X) + 2 * e;
		R.height = e;
		R.show(false);
		obstacles.add (R);

		grid = new GridObject[M][N];
	}

	void basicGrid ()
	{
		// Scene 2: basic grid.
		int x = 2;
		while (x < M) {
			// Build obstacles from bottom to top.
			int y = 2;
			while (y < N) {
				// Mark grid cells x,x+1,x+2, y,y+1,y+2
				for (int i=0; i<=2; i++) {
					for (int j=0; j<=2; j++) {
						Obstacle R = new Obstacle(this);
						R.setGridLocation(new Point(x + i, y + j));
						obstacles.add(R);
						grid[x+i][y+j] = R;
					}
				}
				y = y + 5;
			}
			x = x + 5;
		}
	}

	void setTargets(int numTargets)
	{
		targets = new ArrayList<Target> ();

		// Count non-occupied spaces in grid
		int numAvailable = 0;
		for (int x=0; x<M; x++) {
			for (int y=0; y<N; y++) {
				if (grid[x][y] == null) {
					numAvailable ++;
				}
			}
		}	
		if (numAvailable < numTargets) {
			simulator.Master.error("Request more targets than open spaces.");
			return;
		}

		int targetsLeft = numTargets;
		while (targetsLeft > 0) {
			int x = (int) UniformRandom.uniform(0, M - 1);
			int y = (int) UniformRandom.uniform(0, N - 1);
			if (grid[x][y] == null) {
				Target t = new Target(this);
				t.setGridLocation(new Point(x, y));
				targets.add(t);
				grid[x][y] = t;
				targetsLeft--;
			}
		}
	}
	
	public void addCarModel(CarModel cm) {
		grid[cm.getGridLoc().x][cm.getGridLoc().y] = cm; 
		cars.add(cm);
	}

	public ArrayList<Target> getTargets() {
		return targets;
	}
	
	public void addTarget(Target t) {
		targets.add(t);
		grid[t.getGridLoc().x][t.getGridLoc().y] = t;
	}
	
	public ArrayList<Obstacle> getObstacles() {
		return obstacles;
	}
	
	public void addObstacle(Obstacle o) {
		obstacles.add(o);
		grid[o.getGridLoc().x][o.getGridLoc().y] = o;
	}
}