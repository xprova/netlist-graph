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

		assertEquals(netListArr.size(), 1);

		Netlist nl = netListArr.get(0);

		// test ports
		assertEquals(nl.ports.size(), 4);
		assertEquals(nl.ports.get("x").direction, PinDirection.IN);
		assertEquals(nl.ports.get("clk").direction, PinDirection.IN);
		assertEquals(nl.ports.get("rst").direction, PinDirection.IN);
		assertEquals(nl.ports.get("y").direction, PinDirection.OUT);

		// test modules
		assertEquals(nl.modules.size(), 1);
		assertEquals(nl.modules.get("inv1").type, "NOT");
		assertEquals(nl.modules.get("inv1").id, "inv1");

		// test nets
		assertEquals(nl.nets.size(), 4);
		assertEquals(nl.nets.get("clk").id, "clk");
		assertEquals(nl.nets.get("rst").id, "rst");
		assertEquals(nl.nets.get("x").id, "x");
		assertEquals(nl.nets.get("y").id, "y");

		// test module connections
		HashMap<String, PinConnection> inv1Cons = nl.modules.get("inv1").connections;
		assertEquals(inv1Cons.size(), 2);
		assertEquals(inv1Cons.get("a").dir, PinDirection.IN);
		assertEquals(inv1Cons.get("y").dir, PinDirection.OUT);
		assertEquals(inv1Cons.get("a").net, "x");
		assertEquals(inv1Cons.get("y").net, "y");
		assertEquals(inv1Cons.get("a").bit, 0);
		assertEquals(inv1Cons.get("y").bit, 0);

		// test netlist name
		assertEquals(nl.name, "main");

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

		assertEquals(netListArr.size(), 1);

		Netlist nl = netListArr.get(0);

		// test multi-bit port
		assertEquals(nl.nets.size(), 4);
		assertEquals(nl.nets.get("count").start, 3);
		assertEquals(nl.nets.get("count").end, 0);
		assertEquals(nl.nets.get("count").getCount(), 4);
		assertEquals(nl.nets.get("count").getHigher(), 3);
		assertEquals(nl.nets.get("count").getLower(), 0);

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

		GateLibrary lib = new GateLibrary(VerilogParser.parseString(LIB_STR, null));

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

	public void testAssignStatement() throws Exception {

		// library:
		String LIB_STR = ""; //module NOT (y, a); input a; output y; endmodule";

		String str1 = "module top(count, a); input [3:0] count; output a; assign a = count[0]; endmodule";
		String str2 = "module top(b, a); input b; output a; assign a = b; endmodule";

		// test code

		GateLibrary lib = new GateLibrary(VerilogParser.parseString(LIB_STR, null));

		NetlistGraph ng = new NetlistGraph(VerilogParser.parseString(str1, lib).get(0));

		assertEquals(ng.getModules().size(), 1);

		assertEquals(ng.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 1);

		new NetlistGraph(VerilogParser.parseString(str2, lib).get(0));

	}

	public void testEscapedIdentifiers() throws Exception {

		String str = "module \\top():: (\\a) ); input \\a) ; wire \\hello;; ; NOT u1 (\\hello;; , \\a) ); endmodule ";

		ClassLoader classLoader = getClass().getClassLoader();

		// prepare test GateLibrary
		String fullPath1 = classLoader.getResource("simple.lib").getPath();
		ArrayList<Netlist> libModules = VerilogParser.parseFile(fullPath1, new GateLibrary());
		GateLibrary simpleLib = new GateLibrary(libModules);

		Netlist nl = VerilogParser.parseString(str, simpleLib).get(0);

		assertEquals(nl.name, "\\top()::");

	}

}
