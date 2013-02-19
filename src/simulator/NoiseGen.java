package simulator;

public abstract class NoiseGen<T> {
	public abstract T addNoise(T t);
	public abstract void setLevel(int level);
}
