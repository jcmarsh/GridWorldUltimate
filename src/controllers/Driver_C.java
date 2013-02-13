package controllers;

/**
 * Allows the user to control the robot with the keyboard.
 * For the continuous case.
 * Left throttle: 'W' and 'S'
 * Right throttle: "up_arrow" and "down_arrow"
 * Q and "space" pick up the target, E and "enter" drop it.
 *
 * NEEDS A "STOP" COMMAND.
 * Fairly useless.
 *
 * James Marshall
 */
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// TODO Don't forget "stop"
public class Driver_C extends Controller implements KeyListener {
	private JPanel panel;
	private JFrame frame;

	private double velocity_left = 0.0;
	private double velocity_right = 0.0;
	private static final double STEP = 0.2;

	public static void main (String[] argv) {
		Driver_C controller = new Driver_C(0, false);
		controller.start();
	}

	public Driver_C(int carNum, boolean isCommandLine) {
		super(carNum, isCommandLine);
		frame = new JFrame();
		frame.setSize(300,200);
		frame.setTitle("You are the wheel man.");

		Button exit = new Button("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		panel = new JPanel();
		panel.add(exit);

		frame.getContentPane().add(panel);
		frame.setFocusable(true);
		frame.addKeyListener(this);
	}

	protected void programmaticControl() {
		System.out.println("You're the dog now boy.");
		frame.setVisible(true);
	}

	protected void processMessage(ArrayList<String> message) {

	}


	// Arrow keys are not "typed", just "pressed"
	public void keyPressed (KeyEvent e) {
		int typed = e.getKeyCode();

		if (typed == KeyEvent.VK_UP) {
			velocity_right += STEP;
		} else if (typed == KeyEvent.VK_DOWN) {
			velocity_right -= STEP;
		} 
		move();
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
		char typed = e.getKeyChar();

		if (typed == 'w' || typed == 'W') {
			velocity_left += STEP;
		} else if (typed == 's' || typed == 'S') {
			velocity_left -= STEP;
		}
		// Pickup
		else if (typed == 'q' || typed == 'Q' || typed == KeyEvent.VK_SPACE) {
			pickup();
		}
		// Drop
		else if (typed == 'e' || typed =='E' || typed == KeyEvent.VK_ENTER) {
			drop();
		}
	}

	private void pickup() {
		writer.println("begin");
		writer.println("type=move");
		writer.println("move=pick");
		writer.println("end");
		writer.flush();
	}

	private void drop()  {
		writer.println("begin");
		writer.println("type=move");
		writer.println("move=drop");
		writer.println("end");
		writer.flush();
	}

	private void move() {
		velocity_right = gridworld.CarModel.fixRange(velocity_right);
		velocity_left = gridworld.CarModel.fixRange(velocity_left);

		writer.println("begin");
		writer.println("type=move");
		writer.println("go");
		writer.println(velocity_left);
		writer.println(velocity_right);
		writer.println("end");

		writer.flush();
	}
}
