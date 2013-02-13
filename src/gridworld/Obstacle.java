/**
 * 
 */
package gridworld;

import gridworld.environments.GridWorldPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * A rectangular block that should not be touched. May be invisible; this
 * allows us to encircle the grid with obstacles.
 * 
 * Code pulled from Rahul Simha's original DrawObject.java
 * 
 * Jun 13, 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 *
 */
@SuppressWarnings("serial")
public class Obstacle extends GridObject {

	public Obstacle(GridWorldPanel gwPanel) {
		super(gwPanel);
	}

	@Override
	public void setGridLocation(Point gridLoc) {
		this.gridLoc = gridLoc;  
		
		x = GridWorldPanel.cellWidth * gridLoc.x;
		y = GridWorldPanel.cellHeight * (gridLoc.y + 1);
		width = GridWorldPanel.cellWidth;
		height = GridWorldPanel.cellHeight;		
	}

	@Override
	public void draw(Graphics g) {
		if (show) {
			g.setColor(Color.red);
			GridWorldPanel.drawRectangleOrOval(g, this, true);
		} 
	}

}
