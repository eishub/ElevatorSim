/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.intranet.sim.Simulator;

/**
 * @author Neil McKellar and Chris Dailey
 */
public abstract class SingleValueParameter extends Parameter {
	private static final long serialVersionUID = 1L;

	/**
	 * @param desc the simulator settings
	 */
	public SingleValueParameter(final Simulator.Keys desc) {
		super(desc);
	}

	@Override
	public Object clone() {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
			final byte[] bytes = baos.toByteArray();
			final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			final ObjectInputStream ois = new ObjectInputStream(bais);
			final Object obj = ois.readObject();
			ois.close();
			return obj;
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public abstract List<Object> getValues(String min, String max, String inc);

	// SOON: setValueFromUI() should always receive a String
	public abstract void setValueFromUI(Object param);

	// SOON: getUIValue() should always return a String
	public abstract Object getUIValue();
}
