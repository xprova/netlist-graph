package net.xprova.netlist;

import junit.framework.TestCase;

public class NetTest extends TestCase {

	public void testNet() throws Exception {

		Net x = new Net("x");
		x.start = 3;
		x.end = 0;

		assert (x.getCount() == 4);
		assert (x.getHigher() == 3);
		assert (x.getLower() == 0);

		for (int i = -10; i < 10; i++) {
			boolean inRange = (i >= 0) && (i <= 3);
			assert (x.inRange(i) == inRange);
		}

		Net y = new Net("y");
		y.start = 0;
		y.end = 3;

		assert (y.getCount() == 4);
		assert (y.getHigher() == 3);
		assert (y.getLower() == 0);

		for (int i = -10; i < 10; i++) {
			boolean inRange = (i >= 0) && (i <= 3);
			assert (y.inRange(i) == inRange);
		}

	}

}
