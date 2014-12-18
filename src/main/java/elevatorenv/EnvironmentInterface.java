package elevatorenv;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JComponent;

import org.intranet.elevator.ElevatorSimulationApplication;
import org.intranet.elevator.EveningTrafficElevatorSimulator;
import org.intranet.elevator.MorningTrafficElevatorSimulator;
import org.intranet.elevator.NoIdleElevatorCarSimulator;
import org.intranet.elevator.RandomElevatorSimulator;
import org.intranet.elevator.ThreePersonBugSimulator;
import org.intranet.elevator.ThreePersonElevatorSimulator;
import org.intranet.elevator.ThreePersonTwoElevatorSimulator;
import org.intranet.elevator.UpToFourThenDownSimulator;
import org.intranet.elevator.ticket492simulator;
import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.elevator.model.operate.controller.ManualController;
import org.intranet.elevator.model.operate.controller.MetaController;
import org.intranet.elevator.model.operate.controller.SimpleController;
import org.intranet.elevator.view.BuildingView;
import org.intranet.sim.Model;
import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;
import org.intranet.sim.clock.Clock;
import org.intranet.sim.clock.RealTimeClock;
import org.intranet.sim.ui.ApplicationUI;
import org.intranet.sim.ui.realtime.SimulationArea;

import eis.EIDefaultImpl;
import eis.exceptions.ActException;
import eis.exceptions.EntityException;
import eis.exceptions.ManagementException;
import eis.exceptions.NoEnvironmentException;
import eis.exceptions.PerceiveException;
import eis.exceptions.RelationException;
import eis.iilang.Action;
import eis.iilang.EnvironmentState;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;

/**
 * see also {@link GOALController} for details. See also the Elevator
 * Environment documentation for details on the actions and percepts.
 * 
 * All percepts are provided via the {@link #getAllPerceptsFromEntity(String)}.
 * 
 * <h1>Stand-alone</h1> To run the elevator simulator stand-alone, call
 * org.intranet.elevator.ElevatorSimulationApplication.
 * 
 * <h1>GOAL plug-in</h1> To run as a GOAL plug-in, copy the jar file in the GOAL
 * environments directory.
 * 
 * @author W.Pasman 12jan2009
 * @author W.Pasman 6oct09: Modified to conform EIS standard
 * @author KH January 2010
 * @author W.Pasman 1dec2010 updated to EIS0.3
 * @author K.Hindriks 11 March 2011
 * @author W.Pasman 17dec2014 updated to EIS0.5-SNAPSHOT
 */
@SuppressWarnings("serial")
public class EnvironmentInterface extends EIDefaultImpl implements
		SimulationApplication {

	private Image iconImage;
	ApplicationUI theUI = null;

	/**
	 * Main method facilitates running the elevator simulator stand-alone.
	 */
	public static void main(String[] args) {
		new EnvironmentInterface();
	}

	/**
	 * Alternatively, use simulator as EIS environment and connect via EIS
	 * environment interface.
	 */
	public EnvironmentInterface() {
		super();
		openUI();
	}

	/**
	 * Override state transition logic, Elevator can switch mode any time
	 * because user can change the controller at any time via the GUI. #1591.
	 */
	@Override
	public boolean isStateTransitionValid(EnvironmentState oldState,
			EnvironmentState newState) {
		return true;
	}

	/****************************************************/
	/************** Support functions ********** ********/
	/****************************************************/

	/**
	 * Opens the elevator environment and sets the size as it was set last time.
	 */
	private void openUI() {
		theUI = new ApplicationUI(this);
		theUI.setSize(ElevatorSettings.getWidth(), ElevatorSettings.getHeight());
		theUI.setLocation(ElevatorSettings.getX(), ElevatorSettings.getY());
	}

	/**
	 * Returns the controller for controlling the elevators.
	 * 
	 * @return the GOAL controller that enables control of the elevator.
	 * 
	 * @throws eis.exceptions.NoEnvironmentException
	 *             if there is no GOALController.
	 */
	private GOALController getController() throws NoEnvironmentException {
		Simulator sim = ((SimulationArea) (theUI.simulationArea))
				.getSimulator();
		if (sim != null) {
			Controller controller = sim.getCurrentController();
			if (controller != null && controller instanceof GOALController) {
				return (GOALController) controller;
			}
		}
		throw new NoEnvironmentException(
				"To control the simulator you must select the GOAL controller.");
	}

	/**
	 * @return the simulator that is selected. The simulator may be in running
	 *         or paused state. returns null if no simulator available
	 */
	private Simulator getSimulator() {
		try {
			return ((SimulationArea) theUI.simulationArea).getSimulator();
		} catch (NullPointerException e) {
			return null; // no simulator available.
		}
	}

	/**
	 * Returns the simulation clock, or null if no clock available. This may be
	 * because theUI is null, simulation area is null, or the clock was not yet
	 * initialized. Note, we catch away all exceptions. This may be hard to
	 * debug but there seems no alternative to the SimulationArea.getClock()
	 * call.
	 * 
	 * @author 8nov2010 removed throwing to make getClock more generally usable
	 * 
	 */
	private Clock getClock() {
		try {
			return ((SimulationArea) theUI.simulationArea).getClock();
		} catch (Exception e) {
			/**
			 * Note, we catch away all exceptions. This may be hard to debug but
			 * there seems no alternative to the SimulationArea.getClock() call.
			 */
			return null;
		}
	}

	/**
	 * make addEntity public. we have to because the GOALController creates the
	 * entities and has to notify EIS about that. Seems to be HACK because the
	 * idea of having this private is that only the environment itself can call
	 * this. However the GOALController is not a derived class of environment so
	 * that does not work.
	 **/
	public void newEntity(String entity, String type) throws EntityException {
		String name = "car" + entity;

		try {
			addEntity(name, type);
		} catch (EntityException e) {
			e.printStackTrace();
			if (!getEntities().contains(name)) { // entity does not exist... try
													// again...
				System.out
						.println("Continuing with adding entity without type.");
				addEntity(name);
			} else { // entity does exist, assume type exception due to bug
				notifyNewEntity(name);
			}
		}
	}

	/**
	 * Returns whether the environment is ready to run. The elevator environment
	 * is set up when the following is the case: 1. a simulation has been
	 * selected (and the start/pause button is present) 2. the GOAL controller
	 * has been selected by the user when starting up the simulation.
	 * 
	 * @return true if the environment is set up and ready to run, false
	 *         otherwise.
	 */
	public boolean readyToRun() {
		if (theUI == null || theUI.simulationArea == null
				|| ((SimulationArea) theUI.simulationArea).startButton == null) {
			return false;
		}
		try {
			GOALController controller = getController();
			if (controller == null) {
				return false;
			}
		} catch (Exception e) {
			/*
			 * if there is an exception the controller is not goal controller.
			 * User has a weird environment setting. But we can not give a
			 * warning at this point, because that would flush the user with
			 * warnings as this function is called very frequently from the IDE.
			 */
			return false;
		}
		return (((SimulationArea) theUI.simulationArea).startButton.isEnabled());
	}

	/**
	 * <p>
	 * Executes the action goto(floornr, dir) where parameter dir should be
	 * either up or down. This lets the elevator go to the given floornr. After
	 * arrival, the &lt;dir&gt; light (up or down) will be turned on, indicating
	 * to the people on that floor that this elevator will be going in
	 * &lt;dir&gt; direction and thus suggesting that people who want in the
	 * other direction should not enter. Note, there MAY be people entering the
	 * elevator that travel in the other direction.
	 * </p>
	 * <p>
	 * If the elevator is currently traveling, that travel will be cancelled and
	 * replaced with the new target floor. If the elevator has its doors open,
	 * it will first wait till the doors have been closed before starting the
	 * travel.
	 * </p>
	 * 
	 * @throws ActException
	 *             if action fails.
	 * @throws NoEnvironmentException
	 *             if the environment is currently unavailable.
	 * @param entity
	 *            is the entity, typically "car"+number
	 * @param target
	 *            is the target for the car.
	 */
	public void actiongoto(String entity, CarTarget target)
			throws ActException, NoEnvironmentException {
		GOALController controller;

		controller = getController();

		try {
			/*
			 * EIS returns a DOUBLE even though an INT was inserted. Uses
			 * substring to get rid of the "car" prefix.
			 */
			controller.executeGoto(entity.substring(3), target.getFloor(),
					target.getDirection());
		} catch (IllegalArgumentException e) {
			throw new ActException(ActException.FAILURE, "Action goto failed.",
					e);
		}
	}

	/**
	 * Check that this is a goto action and get the floor number argument from
	 * the goto action. We do some type checking but do not do all checks here
	 * (e.g., is floor number legal?)
	 * 
	 * @param action
	 *            action
	 * @return CarTarget object containing car target.
	 * @throws IllegalArgumentException
	 *             if action or arguments are wrong
	 */
	CarTarget getTarget(Action action) throws IllegalArgumentException {
		if (!action.getName().equals("goto")) {
			throw new IllegalArgumentException("Unknown action "
					+ action.getName());
		}
		LinkedList<Parameter> params = action.getParameters();
		if (params.size() != 2) {
			throw new IllegalArgumentException(
					"goto takes 2 arguments but got " + params);
		}

		if (!(params.get(0) instanceof Numeral)) {
			throw new IllegalArgumentException(
					"goto requires number as first argument but got "
							+ params.get(0));
		}
		Numeral floor = (Numeral) params.get(0);

		if (!(params.get(1) instanceof Identifier)) {
			throw new IllegalArgumentException(
					"goto requires String as second argument but got "
							+ params.get(1));
		}
		String dir = ((Identifier) params.get(1)).getValue();
		if (!(dir.equals("up") || dir.equals("down"))) {
			throw new IllegalArgumentException(
					"goto requires 'up' or 'down' as second argument but got "
							+ dir);
		}

		return new CarTarget(floor.getValue().intValue(), dir);
	}

	/**
	 * Initialize the environment, given a list of key-value pairs as
	 * parameters. The key is a {@link Simulator.Keys} object, the value is a
	 * {@link eis.iilang.Parameter} object. Note that we did not yet try to
	 * convert the Parameter yet.
	 * 
	 * There are two special cases triggering automatic initialization:
	 * <ol>
	 * <li>
	 * If the parameter list contains the {link
	 * ElevatorSettings.InitKey#SIMULATION}, then we will not ask the user to
	 * give the simulation. This is called when you call the EIS
	 * ManageEnvironment with INIT parameter.
	 * <li>
	 * If (1) holds AND all required parameters were provided, then the settings
	 * are automatically applied, making the environment ready to run right
	 * away. What the required parameters are depends on the selected simulation
	 * </ol>
	 * 
	 * @param parameters
	 *            list [Key,Value] pairs (both key and value are String) used
	 *            for the init
	 * @throws ManagementException
	 *
	 *             TODO Link not working
	 */
	protected void initializeEnvironment(
			Hashtable<Simulator.Keys, Parameter> parameters)
			throws ManagementException {

		boolean simulation_was_selected = false;
		if (parameters.containsKey(Simulator.Keys.SIMULATION)) {
			simulation_was_selected = true;
		}

		if (!simulation_was_selected) {
			Simulator sim = theUI.showSimulatorSelectionGUI(theUI, this);
			ElevatorSettings.setSimulator(sim.getDescription());
		}

		setPreferences(parameters);
		// ElevatorSettings.initializePreferredSettings(parameters);

		// multiple is disabled for now.
		theUI.handleSimulationSelected(
				getSimulator(ElevatorSettings.getSimulation()), this, false);

		// all parameters available then apply the parameters and start the env.
		ArrayList<Simulator.Keys> required = getSimulator().getParameterKeys();
		if (simulation_was_selected
				&& parameters.keySet().containsAll(required)) {
			((SimulationArea) (theUI.simulationArea)).applyParameters(this);
		}
	}

	/**
	 * Copy all preferences to the Simulator. Un-wraps the EIS parameters and
	 * passes them one by one.
	 * 
	 * @param parameters
	 * @throws IllegalArgumentException
	 *             if argument not Numeral or Identifier.
	 */
	protected void setPreferences(
			Hashtable<Simulator.Keys, Parameter> parameters)
			throws IllegalArgumentException {
		for (Simulator.Keys key : parameters.keySet()) {
			Parameter p = parameters.get(key);
			Object value;
			if (p instanceof Numeral) {
				value = ((Numeral) p).getValue();
			} else if (p instanceof Identifier) {
				value = ((Identifier) p).getValue();
			} else {
				throw new IllegalArgumentException(
						"Expected Numeral or Identifier but got " + p
								+ " of type " + p.getClass());
			}
			key.setPreference(value);
		}

	}

	/****************************************************/
	/************** EIS interface implementation ********/
	/****************************************************/
	/**
	 * {@inheritDoc}
	 */
	// @Override
	// public EnvironmentState getState() {
	// /**
	// * Once the user configured the environment, we have to switch the state
	// * to PAUSED. That is why we override getState.
	// */
	// EnvironmentState state = super.getState();
	// if (state == EnvironmentState.INITIALIZING) {
	// if (readyToRun()) {
	// try {
	// setState(EnvironmentState.PAUSED);
	// } catch (ManagementException e) {
	// System.out
	// .println("ElevatorEnv bug: failed state transition");
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// return super.getState();
	// }

	@Override
	protected boolean isSupportedByEnvironment(Action action) {
		/**
		 * Tristan explained: You cannot have a goto-action if there is no map.
		 * You cannot shout if there is no air.
		 */
		return true;
	}

	@Override
	protected boolean isSupportedByType(Action action, String type) {
		/**
		 * Tristan wrote about this: to check whether the action is supported by
		 * the entity-type. Note that if one of these methods returns false the
		 * action-mechanism automatically throws an Exception.
		 * "there are types of entities with different capabilities"
		 * "Entities without legs cannot go"
		 */
		return true;
	}

	@Override
	protected boolean isSupportedByEntity(Action action, String entity) {
		/**
		 * Tristan wrote about this: to check whether an entity can execute a
		 * specific action.
		 */
		return action.getName().equals("goto");
	}

	@Override
	protected Percept performEntityAction(String entity, Action action)
			throws ActException {
		// there is only 1 action: goto. So we proceed to execute that
		CarTarget target;
		try {
			target = getTarget(action);
		} catch (IllegalArgumentException e) {
			throw new ActException(ActException.FAILURE,
					"Action could not be performed.", e);
		}
		try {
			actiongoto(entity, target);
		} catch (NoEnvironmentException e) {
			throw new ActException(
					ActException.FAILURE,
					"BUG: There is no environment so this call should not have been made.",
					e);
		}
		return null;
	}

	/**
	 * Returns a list of percepts derived from the simulator environment.
	 * Defines the perceptual capabilities of an entity.
	 * 
	 * @param entity
	 *            DOC
	 * @version 3 now throws error if problem occors. returns empty Percept
	 *          array if no controller available yet
	 * @version 4 now uses entity (=== car name) instead of agent name to comply
	 *          with EIS.
	 */
	public LinkedList<Percept> getAllPerceptsFromEntity(String entity)
			throws PerceiveException {
		try {
			// use substring to get rid of the "car" prefix
			return getController().sendPercepts(entity.substring(3), entity,
					getClock().getTimeConversion());
		} catch (Exception e) {
			throw new PerceiveException(
					"Exception occured during getPerceptsFrom Entity:" + e, e);
		}
	}

	/**
	 * this allows our children (particularly, {@link SimulationArea}) to change
	 * the state.
	 * 
	 * @param state
	 *            is new state.
	 */
	public void notifyEvent(EnvironmentState state) {
		try {
			setState(state);
		} catch (ManagementException e) {
			System.out.println("ElevatorEnv bug: failed state transition");
			e.printStackTrace();
		}
	}

	/**
	 * Closes the environment. Dispose all threads. Stop clocks. CHECK why is
	 * this code not inside the kill() function?
	 */
	private void close() throws ManagementException {
		Clock clock = getClock();
		if (clock != null && clock instanceof RealTimeClock
				&& clock.isRunning()) {
			((RealTimeClock) clock).pause();
		}
		deleteCars();
		closeUI();
		setState(EnvironmentState.KILLED);
	}

	/**
	 * Informs EIS that all entities are gone now.
	 */
	public void deleteCars() {
		for (String entity : getEntities()) {
			try {
				deleteEntity(entity);
			} catch (EntityException e) {
				System.out.println("BUG: " + e);
				e.printStackTrace();
			} catch (RelationException e) {
				System.out.println("BUG: " + e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Closes the GUI and remembers the width, height, etc of the GUI to be able
	 * to restart in the same configuration.
	 */
	private void closeUI() {
		if (theUI != null) {
			// TRAC 710: save old position and size of the main window
			ElevatorSettings.setWindowParams(theUI.getX(), theUI.getY(),
					theUI.getWidth(), theUI.getHeight());

			theUI.dispose();
			theUI.setVisible(false);
			theUI = null;
		}
	}

	/**
	 * Pauses the simulation.
	 * 
	 * @throws NullPointerException
	 *             if theUI is null.
	 * @throws ManagementException
	 *             if simulation not running under GOAL controller or not
	 *             running at all.
	 */
	void pauseRun() throws ManagementException {
		Clock theclock = getClock();
		if (theclock == null) {
			throw new ManagementException(
					"Pausing failed: the environment was not initialized.");
		}
		if (theclock.isRunning()) {
			theclock.pause();
		}
		setState(EnvironmentState.PAUSED);
	}

	/**
	 * Continues running of the simulation.
	 * 
	 * @throws NullPointerException
	 *             if theUI is null.
	 * @throws ManagementException
	 *             CHECK DOC if simulation not running under GOAL controller or
	 *             not running at all.
	 */
	private void continueRun() throws ManagementException {
		Clock theclock = getClock();
		if (theclock == null) {
			throw new ManagementException(
					"continue failed: environment was not initialized.");
		}
		if (!theclock.isRunning()) {
			theclock.start();
		}
		setState(EnvironmentState.RUNNING);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws ManagementException {
		// 3 cases:
		// case 1. environment was PAUSEd. -> start means Continue
		// case 2. environment is not even launched.
		// case 3. if the UI is open but not set up properly yet ->
		// exception.
		if (theUI == null) {
			openUI();
		} else {
			continueRun();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pause() throws ManagementException {
		pauseRun();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void kill() throws ManagementException {
		close();
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Parameter> params) throws ManagementException {
		super.init(params);
		Hashtable<Simulator.Keys, Parameter> parameters = new Hashtable<Simulator.Keys, Parameter>();
		try {
			parameters = EISConverter.EIS2KeyValue(params);
		} catch (IllegalArgumentException e) {
			System.out.println("Received unknown INIT argument" + e);
		}
		// try to initialize as far as possible with given params.
		initializeEnvironment(parameters);

		// environment ready to run. Now we can announce the entities.
		setState(EnvironmentState.PAUSED);
		GOALController controller = getController();
		for (Car car : controller.getCars()) {
			try {
				newEntity(car.getName(), "car");
			} catch (EntityException e) {
				throw new ManagementException("can't create entity " + car, e);
			}
		}

	}

	/**
	 * Provides name of the environment.
	 * 
	 * @return name of the environment.
	 */
	@Override
	public String toString() {
		return getApplicationName();
	}

	/************************************************************/
	/************** implements SimulationApplication ************/
	/************************************************************/
	@Override
	public ArrayList<Simulator> getSimulations() {

		ArrayList<Controller> controllers = new ArrayList<Controller>();
		controllers.add(new MetaController());
		controllers.add(new SimpleController());
		controllers.add(new ManualController());
		controllers.add(new GOALController(this));

		ArrayList<Simulator> simulations = new ArrayList<Simulator>();
		simulations.add(new RandomElevatorSimulator(controllers));
		simulations.add(new MorningTrafficElevatorSimulator(controllers));
		simulations.add(new EveningTrafficElevatorSimulator(controllers));
		simulations.add(new ThreePersonBugSimulator(controllers));
		simulations.add(new ThreePersonElevatorSimulator(controllers));
		simulations.add(new UpToFourThenDownSimulator(controllers));
		simulations.add(new NoIdleElevatorCarSimulator(controllers));
		simulations.add(new ThreePersonTwoElevatorSimulator(controllers));
		simulations.add(new ticket492simulator(controllers));
		return simulations;
	}

	/**
	 * get the Simulator given its {@link Simulator#getDescription()}
	 * 
	 * @param simulatorName
	 * @return the Simulator having this name as description. Throws if no such
	 *         name
	 * @throws IllegalArgumentException
	 *             if there is no simulator with given name.
	 */
	public Simulator getSimulator(String simulatorName)
			throws IllegalArgumentException {
		for (Simulator sim : getSimulations()) {
			if (sim.getDescription().equals(ElevatorSettings.getSimulation())) {
				return sim;
			}
		}
		throw new IllegalArgumentException("No Simulator exists with the name "
				+ simulatorName);
	}

	/**
	 * Version as set by Chris Dailey and Neil McKellar.
	 */
	public String getVersion() {
		return "0.4";
	}

	public Image getImageIcon() {
		if (iconImage == null) {
			URL iconImageURL = ElevatorSimulationApplication.class
					.getResource("icon.gif");
			iconImage = Toolkit.getDefaultToolkit().createImage(iconImageURL);
		}
		return iconImage;
	}

	public JComponent createView(Model m) {
		return new BuildingView((Building) m);
	}

	public String getApplicationName() {
		return "Elevator Simulator";
	}

	public String getCopyright() {
		return "Copyright 2004-2005 Chris Dailey & Neil McKellar";
	}

}

/**
 * support class to store the target for a car.
 * 
 * @author wouter
 * 
 */
class CarTarget {
	int floor;
	String direction;

	public CarTarget(int f, String d) {
		floor = f;
		direction = d;
	}

	int getFloor() {
		return floor;
	}

	String getDirection() {
		return direction;
	}
}
