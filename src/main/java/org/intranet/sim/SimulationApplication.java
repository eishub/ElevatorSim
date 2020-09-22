/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim;

import java.awt.Image;
import java.util.List;

import javax.swing.JComponent;

/**
 * @author Neil McKellar and Chris Dailey
 */
public interface SimulationApplication {
	JComponent createView(Model m);

	List<Simulator> getSimulations();

	String getApplicationName();

	String getCopyright();

	String getVersion();

	Image getImageIcon();
}
