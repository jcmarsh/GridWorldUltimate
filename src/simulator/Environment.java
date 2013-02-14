package simulator;

/**
 * Environment.java
 * Describes the world in which the {@link PlannableObject} is acting in.
 * 
 * May 2012
 * @author James Marshall <jcmarsh@gwmail.gwu.edu>
 */

import gridworld.environments.GridWorldPanel;

import javax.swing.*;

public interface Environment {
    // Responsible for defining a settings panel and view panel
    public JPanel getSettingsPanel();
    public JPanel getViewPanel();
    public void doDraw();
    public void setTime(double time);

    public PlannableObject initNewPlannable(int index);
    
    // Simulator State? Go Pause and Quit
    public void reset();
    public void init(Physics physics);
    // collisions?
    // location?
    // adjacency?

    public boolean isDone();
	public GridWorldPanel getGridWorldPanel();
}
