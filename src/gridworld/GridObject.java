/**
 * GridObject.java
 * Represents an object that is in the grid.
 *
 * Large chucks of code pulled from Rahul Simha's original DrawObject.java
 * 
 * May 2012
 * @author James Marshall <jcmarsh@gwmail.gwu.edu>
 */
package gridworld;

import gridworld.environments.GridWorldPanel;

import java.awt.*;
import java.awt.geom.*;

@SuppressWarnings("serial")
public abstract class GridObject extends Rectangle2D.Double {

	static int IDCount = 1;

	int diameter = 6;        // For points.
	Color color;             // Desired color.

	Point gridLoc = new Point(-1, -1);  // Target/obstacle cells for discrete case.

	boolean show = true;

	GridWorldPanel gwPanel;        // We need this for cellWidth etc.

	int ID;

	public GridObject (GridWorldPanel gwPanel)
	{
		this.gwPanel = gwPanel;
		ID = IDCount ++;
	}

	public boolean equals (Object obj)
	{
		if (! (obj instanceof GridObject) ) {
			return false;
		}

		return this.ID == ((GridObject)obj).ID;
	}
	
	public void show(boolean s) {
		show = s;
	}
	
	public Point getGridLoc() {
		return gridLoc;
	}

	public void setGridLocation(Point gridLoc) {
		this.gridLoc = gridLoc;
	}
	
	public abstract void draw(Graphics g);
}
