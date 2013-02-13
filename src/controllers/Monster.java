package controllers;

/**
 * Implementing a Monster for testing temporal properties of planning.
 *
 * This is a modification of James' square dancing robot.
 */
import java.util.*;
import java.awt.Point;
import gridworld.*;

public class Monster extends Controller implements Runnable{
    enum Orientation{
	NORTH (0, 1), 
	EAST  (1, 0), 
	SOUTH (0, -1), 
	WEST (-1, 0);
	
	public final int i, j;
	Orientation(int i, int j){
	    this.i = i; this.j = j;
	}

	static String getTurn(Orientation from, Orientation to){
	    String retVal = "left";
	    switch(from){
	         case NORTH:
		     if(to == EAST){
			 retVal = "right";
		     }
		     break;
	         case EAST:
		     if(to == SOUTH){
			 retVal = "right";
		     }
		     break;
	         case SOUTH:
		     if(to == WEST){
			 retVal = "right";
		     }
		     break;
	         case WEST:
		     if(to == NORTH){
			 retVal = "right";
		     }
		     break;
	    }
	    return retVal;
	}
    }

    public static int CAR_NUM = 0;
    public static final double FORWARD = 0.8;
    public static final double LEFT = 0.1;
    public static final double RIGHT = 0.1; 
    
    public static final int ILEFT = 0;
    public static final int IRIGHT = 1;
    static Orientation robotOrientation = Orientation.EAST;
    public static boolean done = false;

    public static void main (String[] argv) {
	Monster controller = new Monster(CAR_NUM, false);
	controller.start();
    }

    public Monster(int carNum, boolean isCommandLine) {
	super(carNum, isCommandLine);
    }

    protected void programmaticControl() {
	Point currLoc = new Point(numColumns - carNum, numRows - 1);
	right();
	robotOrientation = getNewOrientation(IRIGHT);
	try{Thread.sleep(1000);}catch(Exception e){System.out.println(e);}
	while(!done){
	    double dir = (double) UniformRandom.uniform();
	    Point next = nextLoc(currLoc, robotOrientation);
	    if(dir <= LEFT){
		left();
		robotOrientation = getNewOrientation(ILEFT);
	    }else if(dir <= (LEFT + FORWARD)){
		if(discreteObstacles != null && 
		   discreteObstacles.contains(next) ||
		   discreteTargets != null &&
		   discreteTargets.contains(next)){
		    //TODO: Still need to make sure the Monster won't actively
		    //      ram controlled cars?
		    continue;
		}

		if(inBounds(next)){
		    forward(1);
		    currLoc = next;
		}else{
		    //Turn Around!
		    left();
		    robotOrientation = getNewOrientation(ILEFT);
		    try{Thread.sleep(1000);}catch(Exception e){}
		    left();
		    robotOrientation = getNewOrientation(ILEFT);
		}
	    }else{
		right();
		robotOrientation = getNewOrientation(IRIGHT);
	    }
		

	    try{
		Thread.sleep(500);
		processMessage(message);
		Thread.sleep(500);
	    }catch(java.lang.InterruptedException e){System.out.println(e);}
	}
	return;
    }

    private Point nextLoc(Point currLoc, Orientation o){
	return new Point((int)currLoc.getX() + o.i, (int)currLoc.getY() + o.j);
    }

    private Orientation getNewOrientation(int dir){
	Orientation retVal = Orientation.NORTH;
	switch(robotOrientation){
	case NORTH:
	    if(dir == ILEFT){retVal = Orientation.WEST;}
	    else{retVal = Orientation.EAST;}
	    break;
	case WEST:
	    if(dir == ILEFT){retVal = Orientation.SOUTH;}
	    else{retVal = Orientation.NORTH;}
	    break;
	case SOUTH:
	    if(dir == ILEFT){retVal = Orientation.EAST;}
	    else{retVal = Orientation.WEST;}
	    break;
	case EAST:
	    if(dir == ILEFT){retVal = Orientation.NORTH;}
	    else{retVal = Orientation.SOUTH;}
	    break;
	}
	return retVal;

    }

    protected void processMessage(ArrayList<String> message) {
	if(message != null){
	    for(String s : message){
		if(!done){
		    System.out.println("Scotty: parsing messages! - " + s);
		    done = s.equals(SocketHandler.FINISHED_STRING);
		    if(done){
			writer.flush();
			writer.close();

			readThread.interrupt();
			readThread = null;

			try{soc.close();}catch(Exception e){}
			System.out.println("Scotty: done!");
		    }
		}
	    }
	}
    }

    private void forward(int times) {
	for (int i = 0; i < times; i++) {
	    writer.println("begin");
	    writer.println("type=move");
	    writer.println("move=go");
	    writer.println("end");
	}
	writer.flush();
    }

    private void left() {
	writer.println("begin");
	writer.println("type=move");
	writer.println("move=left");
	writer.println("end");
	writer.flush();
    }

    private void right() {
	writer.println("begin");
	writer.println("type=move");
	writer.println("move=right");
	writer.println("end");
	writer.flush();
    }

    private boolean inBounds(Point c){
	return (c.getX() >= 0 && c.getX() < numColumns) &&
	    (c.getY() >= 0 && c.getY() < numRows);
    }

    public void run(){
	done = false;
	start();
    }
}
