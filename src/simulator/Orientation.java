/**
 * 
 */
package simulator;

import java.awt.geom.Point2D;

/**
 * Orientation.java
 * 
 * Represents the state of a {@link PhysicalObject}.
 * 
 * Jun 14, 2012
 * @author James Marshall <jcmarsh@gwmail.gwu.edu>
 *
 */
public class Orientation {
    public Point2D.Double location;
    public double theta;
    public double velocity;
    
    public Orientation() {
    	location = new Point2D.Double();
    }
}
