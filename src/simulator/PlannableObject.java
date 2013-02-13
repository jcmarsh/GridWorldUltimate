package simulator;

import gridworld.SocketHandler;

/**
 * PlannableObject.java
 * Represents an actor in GridWorld which must be drawn and 
 * accounted for by the physics of the world, which is 
 * controlled by a planner.
 * 
 * May 2012
 * @author James Marshall
 */

public interface PlannableObject {
	public SocketHandler getSocket();
	public void setSocket(SocketHandler s);

    // Sensors
    //   Channels to Sensors (two way?)

    // Movement
    //   Channel to Physics
	public void initPhysicsChannel(Physics physics, Orientation oreintation);
    //   Physical properties (set by Physics)

}