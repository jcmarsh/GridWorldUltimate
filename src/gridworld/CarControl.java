/**
 * 
 */
package gridworld;

/**
 * CarControl.java
 * 
 * A message that describes control inputs to a {@link CarModel}.
 * 
 * Jun 20, 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 *
 */
public class CarControl {
	double left_wheel;
	double right_wheel;
	
	public CarControl(double left_wheel, double right_wheel) {
		this.left_wheel = left_wheel;
		this.right_wheel = right_wheel;
	}
}
