package elevatorenv;

import org.intranet.sim.Simulator;

/**
 * Static object to store elevator settings TODO this seems deprecated since we
 * store all in {@link Simulator}
 * 
 * @author W.Pasman
 * 
 */

public class ElevatorSettings {
	/**
	 * get the currently selected Simulation.
	 * 
	 * @return preferred simulation as set by user, or "Random Rider Insertion"
	 *         as default.
	 */
	public static String getSimulation() {
		return Simulator.simulatorprefs.get(
				Simulator.Keys.SIMULATION.toString(), "Random Rider Insertion");
	}

	/**
	 * get preferred width of the window.
	 * 
	 * @return preferred width set by user, or 800 by default
	 */
	public static int getWidth() {
		return Simulator.simulatorprefs.getInt("width", 800);
	}

	/**
	 * get preferred height of the window.
	 * 
	 * @return preferred height set by user, or 600 by default
	 */

	public static int getHeight() {
		return Simulator.simulatorprefs.getInt("height", 600);
	}

	/**
	 * get preferred x position of top left corner of the window.
	 * 
	 * @return preferred x pos of top left corner set by user, or 0 by default
	 */
	public static int getX() {
		return Simulator.simulatorprefs.getInt("x", 0);
	}

	/**
	 * get preferred y position of top left corner of the window.
	 * 
	 * @return preferred y pos of top left corner set by user, or 0 by default
	 */
	public static int getY() {
		return Simulator.simulatorprefs.getInt("y", 0);
	}

	/**
	 * save the window settings
	 * 
	 * @param x
	 *            :x pos of top left corner
	 * @param y
	 *            :y pos of top left corner
	 * @param width
	 *            :width of the window
	 * @param height
	 *            :height of the window
	 */
	public static void setWindowParams(int x, int y, int width, int height) {
		Simulator.simulatorprefs.putInt("width", width);
		Simulator.simulatorprefs.putInt("height", height);
		Simulator.simulatorprefs.putInt("x", x);
		Simulator.simulatorprefs.putInt("y", y);

	}

	/**
	 * save given simulator as preferred simulator
	 * 
	 * @param sim
	 *            String name of the simulator. Should exactly match
	 *            {@link Simulator#getDescription()}.
	 */
	public static void setSimulator(String sim) {
		Simulator.simulatorprefs.put(Simulator.Keys.SIMULATION.toString(), sim);
	}

}