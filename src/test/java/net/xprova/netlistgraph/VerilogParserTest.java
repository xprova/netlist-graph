package net.xprova.netlistgraph;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;
import net.xprova.netlist.GateLibrary;
import net.xprova.netlist.Netlist;
import net.xprova.netlist.PinConnection;
import net.xprova.netlist.PinDirection;
import net.xprova.verilogparser.VerilogParser;

public class VerilogParserTest extends TestCase {

	public void testMinimal() throws Exception {

		ClassLoader classLoader = getClass().getClassLoader();

		// prepare test GateLibrary
		String fullPath1 = classLoader.getResource("simple.lib").getPath();
		ArrayList<Netlist> libModules = VerilogParser.parseFile(fullPath1, new GateLibrary());
		GateLibrary simpleLib = new GateLibrary(libModules);

		// build Netlist from test resource file minimal.v
		String fullPath2 = classLoader.getResource("minimal.v").getPath();
		ArrayList<Netlist> netListArr = VerilogParser.parseFile(fullPath2, simpleLib);

		assert (netListArr.size() == 1);

		Netlist nl = netListArr.get(0);

		// test ports
		assert (nl.ports.size() == 4);
		assert (nl.ports.get("x").direction == PinDirection.IN);
		assert (nl.ports.get("clk").direction == PinDirection.IN);
		assert (nl.ports.get("rst").direction == PinDirection.IN);
		assert (nl.ports.get("y").direction == PinDirection.OUT);

		// test modules
		assert (nl.modules.size() == 1);
		assert (nl.modules.get("inv1").type.equals("NOT"));
		assert (nl.modules.get("inv1").id.equals("inv1"));

		// test nets
		assert (nl.nets.size() == 4);
		assert (nl.nets.get("clk").id.equals("clk"));
		assert (nl.nets.get("rst").id.equals("rst"));
		assert (nl.nets.get("x").id.equals("x"));
		assert (nl.nets.get("y").id.equals("y"));

		// test module connections
		HashMap<String, PinConnection> inv1Cons = nl.modules.get("inv1").connections;
		assert (inv1Cons.size() == 2);
		assert (inv1Cons.get("a").dir == PinDirection.IN);
		assert (inv1Cons.get("y").dir == PinDirection.OUT);
		assert (inv1Cons.get("a").net.equals("x"));
		assert (inv1Cons.get("y").net.equals("y"));
		assert (inv1Cons.get("a").bit == 0);
		assert (inv1Cons.get("y").bit == 0);

		// test netlist name
		assert (nl.name.equals("main"));

	}

	public void testMultibit() throws Exception {

		ClassLoader classLoader = getClass().getClassLoader();

		// prepare test GateLibrary
		String fullPath1 = classLoader.getResource("simple.lib").getPath();
		ArrayList<Netlist> libModules = VerilogParser.parseFile(fullPath1, new GateLibrary());
		GateLibrary simpleLib = new GateLibrary(libModules);

		// build Netlist from test resource file minimal.v
		String fullPath2 = classLoader.getResource("multibit.v").getPath();
		ArrayList<Netlist> netListArr = VerilogParser.parseFile(fullPath2, simpleLib);

		assert (netListArr.size() == 1);

		Netlist nl = netListArr.get(0);

		// test multi-bit port
		assert (nl.nets.size() == 4);
		assert (nl.nets.get("count").start == 3);
		assert (nl.nets.get("count").end == 0);
		assert (nl.nets.get("count").getCount() == 4);
		assert (nl.nets.get("count").getHigher() == 3);
		assert (nl.nets.get("count").getLower() == 0);

	}

	public void testPortOrder() throws Exception {

		// library:
		String LIB_STR = "module NOT (y, a); input a; output y; endmodule";

		// should parse correctly:
		ArrayList<String> LIB_OK = new ArrayList<String>();
		LIB_OK.add("module top (a, y); input a; output y; NOT u1 (y, a); endmodule");
		LIB_OK.add("module top (a, y); input a; output y; NOT u1 (.y(y), .a(a)); endmodule");
		LIB_OK.add("module top (a, y); input a; output y; NOT u1 (.a(a), .y(y)); endmodule");

		// should raise a ConnectivityException
		ArrayList<String> LIB_PROB = new ArrayList<String>();
		LIB_PROB.add("module top (a, y); input a; output y; NOT u1 (a, y); endmodule");
		LIB_PROB.add("module top (a, y); input a; output y; NOT u1 (.y(a), .a(y)); endmodule");
		LIB_PROB.add("module top (a, y); input a; output y; NOT u1 (.a(y), .y(a)); endmodule");

		// test code

		GateLibrary lib = new GateLibrary(VerilogParser.parseString(LIB_STR));

		for (String str : LIB_OK) {

			// this should execute without throwing any exceptions

			new NetlistGraph(VerilogParser.parseString(str, lib).get(0));

		}

		for (String str : LIB_PROB) {

			// this should throw a ConnectivityException

			try {

				new NetlistGraph(VerilogParser.parseString(str, lib).get(0));

				fail("parser did not throw an expected ConnectivityException");

			} catch (ConnectivityException e) {

				// exception caught; test passed

			}

		}

	}

}
