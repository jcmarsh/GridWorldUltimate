package gridworld;

import gridworld.environments.GridWorldPanel;

import java.awt.Color;
import java.awt.Graphics;

/**
 * A Line drawn across GridWorld.
 * 
 * Code pulled from Rahul Simha's original DrawObject.java
 *  
 * Jun 13, 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 *
 */
@SuppressWarnings("serial")
public class Line extends GridObject {

	public Line(GridWorldPanel gwPanel) {
		super(gwPanel);
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(Color.lightGray);
		GridWorldPanel.drawLine(g, this);
	}

}
