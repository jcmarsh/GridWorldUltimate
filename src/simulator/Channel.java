package simulator;

/**
 * Channel.java
 * The means of communication within the simulator.
 * Allows the addition of noise
 * One way
 * 
 * May 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 */

public class Channel<T> {
	private T message;
	private int plannableObjectID = -1;

	boolean newMessage = false;

	public Channel(int id) {
	    this.plannableObjectID = id;
	}

	public int getPOID() {
		return plannableObjectID;		
	}
	
	public T getMessage() {
	    newMessage = false;
	    return message;
	}

	public void setMessage(T msg) {
	    message = msg;
	    newMessage = true;
	}
	
	public boolean hasMessage() {
		return newMessage;
	}
}
