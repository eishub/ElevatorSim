/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.realtime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class DurationTest {

	@Test
	public final void testMsFormat() {
		assertEquals("00.123", Duration.format(123));
	}

	@Test
	public final void testSecFormat() {
		assertEquals("25.123", Duration.format(25123));
	}

	@Test
	public final void testMinFormat() {
		assertEquals("03:45.123", Duration.format(225123));
	}

	@Test
	public final void testHourFormat() {
		assertEquals("04:03:45.123", Duration.format(14625123));
	}

	@Test
	public final void testDayFormat() {
		assertEquals("5 day 04:03:45.123", Duration.format(446625123));
	}

	@Test
	public final void testMonthFormat() {
		assertEquals("3 month 5 day 04:03:45.123", Duration.format(8222625123L));
	}

	@Test
	public final void testYearFormat() {
		assertEquals("4 year 3 month 5 day 04:03:45.123",
				Duration.format(134366625123L));
	}
}
