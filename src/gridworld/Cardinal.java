package gridworld;

public enum Cardinal {
	NORTH, WEST, SOUTH, EAST;
	
	public static Cardinal turnLeft(Cardinal current) {
		Cardinal retVal = NORTH;
		switch(current) {
		case NORTH:
			retVal = WEST;
			break;
		case WEST:
			retVal = SOUTH;
			break;
		case SOUTH:
			retVal = EAST;
			break;
		case EAST:
			retVal = NORTH;
			break;
		}
		return retVal;
	}
	
	public static Cardinal turnRight(Cardinal current) {
		Cardinal retVal = NORTH;
		switch(current) {
		case NORTH:
			retVal = EAST;
			break;
		case EAST:
			retVal = SOUTH;
			break;
		case SOUTH:
			retVal = WEST;
			break;
		case WEST:
			retVal = NORTH;
			break;
		}
		return retVal;
	}
}
