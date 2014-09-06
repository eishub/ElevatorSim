package elevatorenv;

import java.util.Hashtable;
import java.util.Map;

import org.intranet.sim.Simulator;

import eis.iilang.Parameter;

public class EISConverter {

	/**
	 * Convert the Keysset of Parameter objects into a Hashmap<Key,Parameter>
	 * with Key the {@link Simulator.Keys} and value the value which should be a
	 * Identifier (String) or Numeral (Integer or Float).
	 * 
	 * @param eisparams
	 *            is a LinkedList of eis.iilang.Function objects, each Function
	 *            having just 1 parameter.
	 * 
	 */
	public static Hashtable<Simulator.Keys, Parameter> EIS2KeyValue(
			Map<String, Parameter> eisparams) {
		Hashtable<Simulator.Keys, Parameter> parameters = new Hashtable<Simulator.Keys, Parameter>();
		for (String key : eisparams.keySet()) {
			parameters.put(Simulator.Keys.valueOf(key.toUpperCase()),
					eisparams.get(key));
		}
		return parameters;
	}
}