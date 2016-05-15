package net.xprova.netlist;

import java.util.ArrayList;

import junit.framework.TestCase;
import net.xprova.verilogparser.VerilogParser;

public class GateLibraryTest extends TestCase {

	public void testGateLibrary() throws Exception {

		// this tests Generator by attempting to re-parse generated Verilog

		ClassLoader classLoader = getClass().getClassLoader();

		// prepare test GateLibrary

		String fullPath1 = classLoader.getResource("simple.lib").getPath();

		ArrayList<Netlist> libModules = VerilogParser.parseFile(fullPath1, new GateLibrary());

		GateLibrary simpleLib = new GateLibrary(libModules);

		ArrayList<Port> pl = simpleLib.get("AND");

		assert(pl.get(0).id.equals("a"));
		assert(pl.get(1).id.equals("b"));
		assert(pl.get(2).id.equals("y"));

		assert(pl.get(0).direction == PinDirection.IN);
		assert(pl.get(1).direction == PinDirection.IN);
		assert(pl.get(2).direction == PinDirection.OUT);

	}

}
