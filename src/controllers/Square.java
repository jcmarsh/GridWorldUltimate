package controllers;

/**
 * Makes the robot square dance for a bit.
 *
 * James Marshall
 */
import java.util.*;

public class Square extends Controller {

    public static void main (String[] argv) {
	int carNum = 0;
	if (argv.length == 1) {
	    carNum = Integer.parseInt(argv[0]);
	}

	Square controller = new Square(carNum, false);
	controller.start();
    }

    public Square(int carNum, boolean isCommandLine) {
	super(carNum, isCommandLine);
    }

    protected void programmaticControl() {
	System.out.println("You're the dog now boy.");
	// make a box
	forward(1);
	left();
	forward(1);
	left();
	forward(1);
	left();
	forward(1);

	// turn around
	left();
	left();

	// make another box
	forward(1);
	right();
	forward(1);
	right();
	forward(1);
	right();
	forward(1);
	System.out.println("This concludes this evenings entertainment.");
    }

    protected void processMessage(ArrayList<String> message) {

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
}