package gridworld;

import java.util.Scanner;
import java.util.ArrayList;
import java.awt.Point;

/**
 * Util.java
 * 
 * Utilities used throughout GridWorld.
 *
 */
public class Util {

	public static String getPropertyName (String propStr)
	{
		try {
			int i = propStr.indexOf('=');
			String left = propStr.substring(0, i);
			left = left.trim();
			return left;
		} 
		catch (Exception e) {
			return null;
		}
	}

	public static String getStringProperty (String propStr)
	{
		try {
			int i = propStr.indexOf('=');
			String right = propStr.substring (i+1);
			right = right.trim();
			return right;
		}
		catch (Exception e) {
			return null;
		}
	}

	public static int getIntProperty (String propStr)
	{
		try {
			int i = propStr.indexOf('=');
			String right = propStr.substring (i+1);
			right = right.trim();
			int n = Integer.parseInt (right);
			return n;
		}
		catch (Exception e) {
			return -1;
		}
	}

	public static double getDoubleProperty (String propStr)
	{
		try {
			int i = propStr.indexOf('=');
			String right = propStr.substring (i+1);
			right = right.trim();
			double d = Double.parseDouble (right);
			return d;
		}
		catch (Exception e) {
			return -1.0;
		}
	}

	public static boolean getBooleanProperty (String propStr)
	{
		try {
			int i = propStr.indexOf('=');
			String right = propStr.substring (i+1);
			right = right.trim();
			if (right.equals("true")) {
				return true;
			}
			else {
				return false;
			}
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public static double angleFix (double a)
	{
		// TODO: This code exists elsewhere!
		// Make each angle an angle between 0 and 2*PI.
		//** Note: this code can be optimized.
		if (a < 0) {
			while (a < 0) {
				a = a + 2*Math.PI;
			}
		}
		else if (a > 2*Math.PI) {
			while (a > 2*Math.PI) {
				a = a - 2*Math.PI;
			}
		}
		return a;
	}

//	// TODO: Re-implement as noise on a channel.
//	public static double addContinuousNoise(GridWorld gWorld, double orig){
//		double noise = 0.0;
//		if(gWorld.noiseLevel == 1){
//			noise = (double) UniformRandom.uniform(-1.0, 1.0); 
//		}else if(gWorld.noiseLevel == 2){
//			noise = (double) UniformRandom.uniform(-3.0, 3.0);
//		}
//
//		System.out.println("NOISE!: " + noise);
//		return orig + noise;
//	}

	public static String formatDiscreteMap(ArrayList<Point> map){
		StringBuilder retVal = new StringBuilder("{");
		boolean addComma = false;
		for(Point c : map){
			if(addComma){
				retVal.append(",");
			}
			retVal.append("(");
			retVal.append(c.getX());
			retVal.append(",");
			retVal.append(c.getY());
			retVal.append(")");
		}
		retVal.append("}");

		return retVal.toString();
	}
	
	public static ArrayList<Point> parseDiscreteMap(String data){
		//A data string for DiscreteMaps looks like: "{(1,1),(1,2), ...}"
		ArrayList<Point> retVal = new ArrayList<Point>();

		//Remove the leading "{(" and trailing ")}", just so the scanner/tokenizer
		//doesn't have to deal with special cases.
		String data_copy = new String(data.getBytes());
		data_copy = data_copy.substring(2, data_copy.length() - 2);

		Scanner scan = new Scanner(data_copy);
		//yes, the "\\"'s are necessary
		scan.useDelimiter("\\),\\(");

		while(scan.hasNext()){
			String tok = scan.next();
			int comma = tok.indexOf(',');
			int x = Integer.parseInt(tok.substring(0,comma));
			int y = Integer.parseInt(tok.substring(comma+1, tok.length()));
			retVal.add(new Point(x,y));
		}

		return retVal;
	}
}