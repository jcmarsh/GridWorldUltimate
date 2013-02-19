package simulator;

import java.util.Random;

public class NoiseGaussian extends NoiseGen<Double> {
	private int level = 1;
	
	@Override
	public Double addNoise(Double t) {
		Random rand = new Random(); // Note: no seed!
		double retVal = 0.0;
		double sum = 0.0;
		double scale = 0;
		if (level == 1) {
			scale = t / 15.0;
		} else if (level == 2) {
			scale = t / 5.0;
		}
		// Approximate a normal distribution by adding 3 regular random numbers between -1 and 1
		for (int i = 0; i < 3; i++) {
			sum = sum + rand.nextFloat();
		}
			
		retVal = scale * sum + t;
		return retVal;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}
}
