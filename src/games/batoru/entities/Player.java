/*
 * Created on Sep 30, 2003
 */
package games.batoru.entities;

/**
 * @author Tako
 */
public interface Player {
	public static final int LOC_IDLE = 0;
	public static final int LOC_WALKING = 1;
	public static final int LOC_RUNNING = 2;
	public static final int LOC_JUMPING = 3;
	public static final int LOC_LANDING = 4;
	
	public static final int ACT_IDLE = 0;
	public static final int ACT_SHOOTING = 1;
	
	public float getEyeHeight();
	public void setLocomotionState(int _nState);
	public void setActionState(int _nState);
}
