package elevatorenv;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

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
import eis.PerceptUpdate;
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
 * @author W.Pasman 30oct2014 updated to EIS0.4
 * @author W.Pasman 17dec2014 updated to EIS0.5-SNAPSHOT
 */
@SuppressWarnings("serial")
public class EnvironmentInterface extends EIDefaultImpl implements SimulationApplication {
	private Image iconImage;
	private ApplicationUI theUI;

	public static void main(final String[] args) {
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
	 * Override state transition logic, Elevator can switch mode any time because
	 * user can change the controller at any time via the GUI. #1591.
	 */
	@Override
	public boolean isStateTransitionValid(final EnvironmentState oldState, final EnvironmentState newState) {
		return true;
	}

	/****************************************************/
	/************** Support functions ********** ********/
	/****************************************************/

	/**
	 * Opens the elevator environment and sets the size as it was set last time.
	 */
	private void openUI() {
		this.theUI = new ApplicationUI(this);
		this.theUI.setSize(ElevatorSettings.getWidth(), ElevatorSettings.getHeight());
		this.theUI.setLocation(ElevatorSettings.getX(), ElevatorSettings.getY());
	}

	/**
	 * Returns the controller for controlling the elevators.
	 *
	 * @return the GOAL controller that enables control of the elevator.
	 *
	 * @throws eis.exceptions.NoEnvironmentException if there is no GOALController.
	 */
	private GOALController getController() throws NoEnvironmentException {
		final Simulator sim = ((SimulationArea) (this.theUI.simulationArea)).getSimulator();
		if (sim != null) {
			final Controller controller = sim.getCurrentController();
			if (controller != null && controller instanceof GOALController) {
				return (GOALController) controller;
			}
		}
		throw new NoEnvironmentException("To control the simulator you must select the GOAL controller.");
	}

	/**
	 * @return the simulator that is selected. The simulator may be in running or
	 *         paused state. returns null if no simulator available
	 */
	private Simulator getSimulator() {
		try {
			return ((SimulationArea) this.theUI.simulationArea).getSimulator();
		} catch (final NullPointerException e) {
			return null; // no simulator available.
		}
	}

	/**
	 * Returns the simulation clock, or null if no clock available. This may be
	 * because theUI is null, simulation area is null, or the clock was not yet
	 * initialized. Note, we catch away all exceptions. This may be hard to debug
	 * but there seems no alternative to the SimulationArea.getClock() call.
	 *
	 * @author 8nov2010 removed throwing to make getClock more generally usable
	 *
	 */
	private Clock getClock() {
		try {
			return ((SimulationArea) this.theUI.simulationArea).getClock();
		} catch (final Exception e) {
			/**
			 * Note, we catch away all exceptions. This may be hard to debug but there seems
			 * no alternative to the SimulationArea.getClock() call.
			 */
			return null;
		}
	}

	/**
	 * make addEntity public. we have to because the GOALController creates the
	 * entities and has to notify EIS about that. Seems to be HACK because the idea
	 * of having this private is that only the environment itself can call this.
	 * However the GOALController is not a derived class of environment so that does
	 * not work.
	 *
	 * @param entity the new entity
	 * @param type   the type of the new entity
	 * @throws EntityException if entity already exists.
	 **/
	public void newEntity(final String entity, final String type) throws EntityException {
		final String name = "car" + entity;
		try {
			addEntity(name, type);
		} catch (final EntityException e) {
			e.printStackTrace();
			if (!getEntities().contains(name)) { // entity does not exist... try
													// again...
				System.out.println("Continuing with adding entity without type.");
				addEntity(name);
			} else { // entity does exist, assume type exception due to bug
				notifyNewEntity(name);
			}
		}
	}

	/**
	 * Returns whether the environment is ready to run. The elevator environment is
	 * set up when the following is the case: 1. a simulation has been selected (and
	 * the start/pause button is present) 2. the GOAL controller has been selected
	 * by the user when starting up the simulation.
	 *
	 * @return true if the environment is set up and ready to run, false otherwise.
	 */
	public boolean readyToRun() {
		if (this.theUI == null || this.theUI.simulationArea == null
				|| ((SimulationArea) this.theUI.simulationArea).startButton == null) {
			return false;
		}
		try {
			final GOALController controller = getController();
			if (controller == null) {
				return false;
			}
		} catch (final Exception e) {
			/*
			 * if there is an exception the controller is not goal controller. User has a
			 * weird environment setting. But we can not give a warning at this point,
			 * because that would flush the user with warnings as this function is called
			 * very frequently from the IDE.
			 */
			return false;
		}
		return (((SimulationArea) this.theUI.simulationArea).startButton.isEnabled());
	}

	/**
	 * <p>
	 * Executes the action goto(floornr, dir) where parameter dir should be either
	 * up or down. This lets the elevator go to the given floornr. After arrival,
	 * the &lt;dir&gt; light (up or down) will be turned on, indicating to the
	 * people on that floor that this elevator will be going in &lt;dir&gt;
	 * direction and thus suggesting that people who want in the other direction
	 * should not enter. Note, there MAY be people entering the elevator that travel
	 * in the other direction.
	 * </p>
	 * <p>
	 * If the elevator is currently traveling, that travel will be cancelled and
	 * replaced with the new target floor. If the elevator has its doors open, it
	 * will first wait till the doors have been closed before starting the travel.
	 * </p>
	 *
	 * @throws ActException           if action fails.
	 * @throws NoEnvironmentException if the environment is currently unavailable.
	 * @param entity is the entity, typically "car"+number
	 * @param target is the target for the car.
	 */
	public void actiongoto(final String entity, final CarTarget target) throws ActException, NoEnvironmentException {
		final GOALController controller = getController();
		try {
			/*
			 * EIS returns a DOUBLE even though an INT was inserted. Uses substring to get
			 * rid of the "car" prefix.
			 */
			controller.executeGoto(entity.substring(3), target.getFloor(), target.getDirection());
		} catch (final IllegalArgumentException e) {
			throw new ActException(ActException.FAILURE, "Action goto failed.", e);
		}
	}

	/**
	 * Check that this is a goto action and get the floor number argument from the
	 * goto action. We do some type checking but do not do all checks here (e.g., is
	 * floor number legal?)
	 *
	 * @param action action
	 * @return CarTarget object containing car target.
	 * @throws IllegalArgumentException if action or arguments are wrong
	 */
	private CarTarget getTarget(final Action action) throws IllegalArgumentException {
		if (!action.getName().equals("goto")) {
			throw new IllegalArgumentException("Unknown action " + action.getName());
		}
		final List<Parameter> params = action.getParameters();
		if (params.size() != 2) {
			throw new IllegalArgumentException("goto takes 2 arguments but got " + params);
		}

		if (!(params.get(0) instanceof Numeral)) {
			throw new IllegalArgumentException("goto requires number as first argument but got " + params.get(0));
		}
		final Numeral floor = (Numeral) params.get(0);

		if (!(params.get(1) instanceof Identifier)) {
			throw new IllegalArgumentException("goto requires String as second argument but got " + params.get(1));
		}
		final String dir = ((Identifier) params.get(1)).getValue();
		if (!(dir.equals("up") || dir.equals("down"))) {
			throw new IllegalArgumentException("goto requires 'up' or 'down' as second argument but got " + dir);
		}

		return new CarTarget(floor.getValue().intValue(), dir);
	}

	/**
	 * Initialize the environment, given a list of key-value pairs as parameters.
	 * The key is a {@link Simulator.Keys} object, the value is a
	 * {@link eis.iilang.Parameter} object. Note that we did not yet try to convert
	 * the Parameter yet.
	 *
	 * There are two special cases triggering automatic initialization:
	 * <ol>
	 * <li>If the parameter list contains the {link
	 * ElevatorSettings.InitKey#SIMULATION}, then we will not ask the user to give
	 * the simulation. This is called when you call the EIS ManageEnvironment with
	 * INIT parameter.
	 * <li>If (1) holds AND all required parameters were provided, then the settings
	 * are automatically applied, making the environment ready to run right away.
	 * What the required parameters are depends on the selected simulation
	 * </ol>
	 *
	 * @param parameters list [Key,Value] pairs (both key and value are String) used
	 *                   for the init
	 * @throws ManagementException
	 *
	 *                             TODO Link not working
	 */
	protected void initializeEnvironment(final Map<Simulator.Keys, Parameter> parameters) throws ManagementException {
		boolean simulation_was_selected = false;
		if (parameters.containsKey(Simulator.Keys.SIMULATION)) {
			simulation_was_selected = true;
		}

		if (!simulation_was_selected) {
			final Simulator sim = this.theUI.showSimulatorSelectionGUI(this.theUI, this);
			ElevatorSettings.setSimulator(sim.getDescription());
		}

		setPreferences(parameters);
		// ElevatorSettings.initializePreferredSettings(parameters);

		// multiple is disabled for now.
		this.theUI.handleSimulationSelected(getSimulator(ElevatorSettings.getSimulation()), this, false);

		// all parameters available then apply the parameters and start the env.
		final List<Simulator.Keys> required = getSimulator().getParameterKeys();
		if (simulation_was_selected && parameters.keySet().containsAll(required)) {
			((SimulationArea) (this.theUI.simulationArea)).applyParameters(this);
		}
	}

	/**
	 * Copy all preferences to the Simulator. Un-wraps the EIS parameters and passes
	 * them one by one.
	 *
	 * @param parameters the settings for the simulator
	 * @throws IllegalArgumentException if argument not Numeral or Identifier.
	 */
	protected void setPreferences(final Map<Simulator.Keys, Parameter> parameters) throws IllegalArgumentException {
		for (final Simulator.Keys key : parameters.keySet()) {
			final Parameter p = parameters.get(key);
			Object value;
			if (p instanceof Numeral) {
				value = ((Numeral) p).getValue();
			} else if (p instanceof Identifier) {
				value = ((Identifier) p).getValue();
			} else {
				throw new IllegalArgumentException(
						"Expected Numeral or Identifier but got " + p + " of type " + p.getClass());
			}
			key.setPreference(value);
		}
	}

	/****************************************************/
	/************** EIS interface implementation ********/
	/****************************************************/

	@Override
	protected boolean isSupportedByEnvironment(final Action action) {
		/**
		 * Tristan explained: You cannot have a goto-action if there is no map. You
		 * cannot shout if there is no air.
		 */
		return true;
	}

	@Override
	protected boolean isSupportedByType(final Action action, final String type) {
		/**
		 * Tristan wrote about this: to check whether the action is supported by the
		 * entity-type. Note that if one of these methods returns false the
		 * action-mechanism automatically throws an Exception. "there are types of
		 * entities with different capabilities" "Entities without legs cannot go"
		 */
		return true;
	}

	@Override
	protected boolean isSupportedByEntity(final Action action, final String entity) {
		/**
		 * Tristan wrote about this: to check whether an entity can execute a specific
		 * action.
		 */
		return action.getName().equals("goto");
	}

	@Override
	protected void performEntityAction(final Action action, final String entity) throws ActException {
		// there is only 1 action: goto. So we proceed to execute that
		CarTarget target;
		try {
			target = getTarget(action);
		} catch (final IllegalArgumentException e) {
			throw new ActException(ActException.FAILURE, "Action could not be performed.", e);
		}
		try {
			actiongoto(entity, target);
		} catch (final NoEnvironmentException e) {
			throw new ActException(ActException.FAILURE,
					"BUG: There is no environment so this call should not have been made.", e);
		}
	}

	/**
	 * Returns a list of percepts derived from the simulator environment. Defines
	 * the perceptual capabilities of an entity.
	 *
	 * @param entity
	 * @version 3 now throws error if problem occors. returns empty Percept array if
	 *          no controller available yet
	 * @version 4 now uses entity (=== car name) instead of agent name to comply
	 *          with EIS.
	 */
	@Override
	public PerceptUpdate getPerceptsForEntity(final String entity) throws PerceiveException {
		try {
			// use substring to get rid of the "car" prefix
			return getController().sendPercepts(entity.substring(3), entity, getClock().getTimeConversion());
		} catch (final Exception e) {
			throw new PerceiveException("Exception occured during getPerceptsFrom Entity:" + e, e);
		}
	}

	/**
	 * this allows our children (particularly, {@link SimulationArea}) to change the
	 * state.
	 *
	 * @param state is new state.
	 */
	public void notifyEvent(final EnvironmentState state) {
		try {
			setState(state);
		} catch (final ManagementException e) {
			System.out.println("ElevatorEnv bug: failed state transition");
			e.printStackTrace();
		}
	}

	/**
	 * Closes the environment. Dispose all threads. Stop clocks. CHECK why is this
	 * code not inside the kill() function?
	 */
	private void close() throws ManagementException {
		final Clock clock = getClock();
		if (clock != null && clock instanceof RealTimeClock && clock.isRunning()) {
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
		for (final String entity : getEntities()) {
			try {
				deleteEntity(entity);
			} catch (final EntityException e) {
				System.out.println("BUG: " + e);
				e.printStackTrace();
			} catch (final RelationException e) {
				System.out.println("BUG: " + e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Closes the GUI and remembers the width, height, etc of the GUI to be able to
	 * restart in the same configuration.
	 */
	private void closeUI() {
		if (this.theUI != null) {
			// TRAC 710: save old position and size of the main window
			ElevatorSettings.setWindowParams(this.theUI.getX(), this.theUI.getY(), this.theUI.getWidth(),
					this.theUI.getHeight());

			this.theUI.dispose();
			this.theUI.setVisible(false);
			this.theUI = null;
		}
	}

	/**
	 * Pauses the simulation.
	 *
	 * @throws NullPointerException if theUI is null.
	 * @throws ManagementException  if simulation not running under GOAL controller
	 *                              or not running at all.
	 */
	private void pauseRun() throws ManagementException {
		final Clock theclock = getClock();
		if (theclock == null) {
			throw new ManagementException("Pausing failed: the environment was not initialized.");
		}
		if (theclock.isRunning()) {
			theclock.pause();
		}
		setState(EnvironmentState.PAUSED);
	}

	/**
	 * Continues running of the simulation.
	 *
	 * @throws NullPointerException if theUI is null.
	 * @throws ManagementException  CHECK DOC if simulation not running under GOAL
	 *                              controller or not running at all.
	 */
	private void continueRun() throws ManagementException {
		final Clock theclock = getClock();
		if (theclock == null) {
			throw new ManagementException("continue failed: environment was not initialized.");
		}
		if (!theclock.isRunning()) {
			theclock.start();
		}
		setState(EnvironmentState.RUNNING);
	}

	@Override
	public void start() throws ManagementException {
		// 3 cases:
		// case 1. environment was PAUSEd. -> start means Continue
		// case 2. environment is not even launched.
		// case 3. if the UI is open but not set up properly yet ->
		// exception.
		if (this.theUI == null) {
			openUI();
		} else {
			continueRun();
		}
	}

	@Override
	public void pause() throws ManagementException {
		pauseRun();
	}

	@Override
	public void kill() throws ManagementException {
		close();
	}

	@Override
	public void init(final Map<String, Parameter> params) throws ManagementException {
		super.init(params);
		Map<Simulator.Keys, Parameter> parameters = new HashMap<>(0);
		try {
			parameters = EISConverter.EIS2KeyValue(params);
		} catch (final IllegalArgumentException e) {
			System.out.println("Received unknown INIT argument" + e);
		}
		// try to initialize as far as possible with given params.
		initializeEnvironment(parameters);

		// environment ready to run. Now we can announce the entities.
		setState(EnvironmentState.PAUSED);
		final GOALController controller = getController();
		for (final Car car : controller.getCars()) {
			try {
				newEntity(car.getName(), "car");
			} catch (final EntityException e) {
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
	public List<Simulator> getSimulations() {
		final List<Controller> controllers = new ArrayList<>(4);
		controllers.add(new MetaController());
		controllers.add(new SimpleController());
		controllers.add(new ManualController());
		controllers.add(new GOALController(this));

		final List<Simulator> simulations = new ArrayList<>(9);
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
	 * @param simulatorName name of the simulator to get
	 * @return the Simulator having this name as description. Throws if no such name
	 * @throws IllegalArgumentException if there is no simulator with given name.
	 */
	public Simulator getSimulator(final String simulatorName) throws IllegalArgumentException {
		for (final Simulator sim : getSimulations()) {
			if (sim.getDescription().equals(ElevatorSettings.getSimulation())) {
				return sim;
			}
		}
		throw new IllegalArgumentException("No Simulator exists with the name " + simulatorName);
	}

	/**
	 * Version as set by Chris Dailey and Neil McKellar.
	 */
	@Override
	public String getVersion() {
		return "0.4";
	}

	@Override
	public Image getImageIcon() {
		if (this.iconImage == null) {
			final URL iconImageURL = EnvironmentInterface.class.getResource("icon.gif");
			this.iconImage = Toolkit.getDefaultToolkit().createImage(iconImageURL);
		}
		return this.iconImage;
	}

	@Override
	public JComponent createView(final Model m) {
		return new BuildingView((Building) m);
	}

	@Override
	public String getApplicationName() {
		return "Elevator Simulator";
	}

	@Override
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
	private final int floor;
	private final String direction;

	public CarTarget(final int f, final String d) {
		this.floor = f;
		this.direction = d;
	}

	int getFloor() {
		return this.floor;
	}

	String getDirection() {
		return this.direction;
	}
}
