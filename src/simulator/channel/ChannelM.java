package simulator.channel;

import java.util.ArrayList;

import simulator.NoiseGen;

/**
 * ChannelM.java
 * Expands {@link Channel} to more gracefully deal with multiple
 * values being returned.
 * 
 * May 2012
 * @author James Marshall <jcmarsh (at) gwmail.gwu.edu>
 */

public class ChannelM<T> extends Channel<ArrayList<T>> {
	private ArrayList<T> message;
	private NoiseGen<T> generator = null;
	
	public ChannelM(int id, String frmtStr) {
	    super(id, frmtStr);
	}
	
	@Override
	public ArrayList<String> getMessageStr() {
		ArrayList<String> msg = new ArrayList<String>();
		ArrayList<T> mess = this.getMessage();
		for (int i = 0; i < mess.size(); i++) {
			msg.add(frmtStr + i + "=" + mess.get(i));
		}
		return msg;
	}

	@Override
	public void setMessage(ArrayList<T> msg) {
		message = new ArrayList<T>(); 
		for (T m : msg) {
			if (generator != null) {
	    		message.add(generator.addNoise(m));
	    	} else {
	    		message.add(m);
	    	}
	    }
	    newMessage = true;
	}
}