package simulator.channel;

import java.util.ArrayList;

import simulator.NoiseGen;

/**
 * Channel.java
 * The means of communication within the simulator.
 * Allows the addition of noise
 * One way
 * 
 * NOTE: Call getMessage or getMessageStr both clear the current message.
 * 
 * May 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 */

public class Channel<T> {
	protected T message;
	protected int plannableObjectID = -1;
	protected NoiseGen<T> generator = null;
	protected String frmtStr = "";
	protected boolean newMessage = false;

	public void setNoiseGen(NoiseGen<T> noiseGen) {
		generator = noiseGen;
	}
		
	public Channel(int id, String frmtStr) {
	    this.plannableObjectID = id;
	    this.frmtStr = frmtStr;
	}

	public int getPOID() {
		return plannableObjectID;		
	}
	
	public T getMessage() {
	    newMessage = false;
	    return message;
	}
	
	public ArrayList<String> getMessageStr() {
		ArrayList<String> msg = new ArrayList<String>();
		msg.add(frmtStr + "=" + this.getMessage());
		return msg;
	}

	public void setMessage(T msg) { 
		if (generator != null) {
	   		message = generator.addNoise(msg);
	   	} else {
	   		message = msg;
	   	}
	    newMessage = true;
	}
		
	public boolean hasMessage() {
		return newMessage;
	}
}
