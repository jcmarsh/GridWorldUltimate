package gridworld;

import gridworld.environments.GridWorldPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * A green dot that the Car needs to pick up and move for some unknown reason.
 * 
 * Code pulled from Rahul Simha's original DrawObject.java
 * 
 * Jun 13, 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 *
 */
@SuppressWarnings("serial")
public class Target extends GridObject {
	private boolean isHeld = false;

	public Target(GridWorldPanel gwPanel) {
		super(gwPanel);
	}

	public boolean isHeld(){
		return isHeld;
	}

	public void setHeld(boolean held) {
		isHeld = held;
	}

	@Override
	public void setGridLocation(Point gridLoc) {
		this.gridLoc = gridLoc;  

		x = gridLoc.x * GridWorldPanel.cellWidth + GridWorldPanel.cellWidth / 4;  
		y = (gridLoc.y + 1) * GridWorldPanel.cellHeight - GridWorldPanel.cellHeight / 4;
		width = GridWorldPanel.cellWidth / 2;
		height = GridWorldPanel.cellHeight / 2;		
	}

	@Override
	public void draw(Graphics g) {

		if( !isHeld ){
			g.setColor(Color.green);
			GridWorldPanel.drawRectangleOrOval(g, this, false);
		}
	}

}
