package elevatorenv;

import java.util.HashMap;
import java.util.Map;

import org.intranet.sim.Simulator;

import eis.iilang.Parameter;

public class EISConverter {
	/**
	 * Convert the KeySet of Parameter objects into a {@link HashMap}
	 * &lt;Key,Parameter&gt; with Key the {@link Simulator.Keys} and value the value
	 * which should be a Identifier (String) or Numeral (Integer or Float).
	 *
	 * @param eisparams is a LinkedList of eis.iilang.Function objects, each
	 *                  Function having just 1 parameter.
	 * @return simulator settings
	 */
	public static Map<Simulator.Keys, Parameter> EIS2KeyValue(final Map<String, Parameter> eisparams) {
		final Map<Simulator.Keys, Parameter> parameters = new HashMap<>();
		for (final String key : eisparams.keySet()) {
			parameters.put(Simulator.Keys.valueOf(key.toUpperCase()), eisparams.get(key));
		}
		return parameters;
	}
}