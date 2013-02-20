package gridworld.sensors;

public class RGB {
	private int R = 0;
	private int G = 0;
	private int B = 0;
	
	public RGB(int rgb) {
		R = rgb << 8;
		R = R >>> 24;
		G = rgb << 16;
		G = G >>> 24;
		B = rgb << 24;
		B = B >>> 24;
	}
	
	@Override
	public String toString() {
		return R + ":" + G + ":" + B;
	}
}
