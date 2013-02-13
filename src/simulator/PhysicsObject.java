/**
 * 
 */
package simulator;

/**
 * PhysicalObject.java
 * 
 * An object that can be manipulated by {@link Physics}
 * Can be expanded based on how complicated of physics we implement.
 * For now, just a location, heading and velocity should be enough.
 * 
 * Jun 14, 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 *
 */
public interface PhysicsObject {
    public void setOrientation(Orientation o);
    public void updateOrientation(double dx, double dy, double dtheta, double dvelocity);
    public Orientation getOrientation();
    public void nextStep(double delT);
    
    // TODO: What about collisions? Hit count?
}
