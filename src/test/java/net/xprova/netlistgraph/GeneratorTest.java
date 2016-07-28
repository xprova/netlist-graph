package net.xprova.netlistgraph;

import java.util.ArrayList;

import junit.framework.TestCase;
import net.xprova.netlist.GateLibrary;
import net.xprova.netlist.Netlist;
import net.xprova.verilogparser.VerilogParser;

public class GeneratorTest extends TestCase {

	public void testGenerator() throws Exception {

		// this tests Generator by attempting to re-parse generated Verilog

		ClassLoader classLoader = getClass().getClassLoader();

		// prepare test GateLibrary

		String fullPath1 = classLoader.getResource("simple.lib").getPath();

		ArrayList<Netlist> libModules = VerilogParser.parseFile(fullPath1, new GateLibrary());

		GateLibrary simpleLib = new GateLibrary(libModules);

		// load minimal.v

		String fullPath = classLoader.getResource("minimal.v").getPath();

		ArrayList<Netlist> netListArr = VerilogParser.parseFile(fullPath, simpleLib);

		NetlistGraph g = new NetlistGraph(netListArr.get(0));

		String verilogString = Generator.generateString(g);

		VerilogParser.parseString(verilogString, simpleLib); // try to parse

	}

	public void testSimpleAssign() throws Exception {

		// simple assign statement (case 3 - see VerilogParser.parseAssignStatement)

		// parses, checks, generates, parses then checks again

		String strAssignSimple = "module top(a, b); input a; output b; assign b = a; endmodule";

		NetlistGraph g1 = new NetlistGraph(VerilogParser.parseString(strAssignSimple, null).get(0));

		assertEquals(g1.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 1);

		String verilogString = Generator.generateString(g1);

		NetlistGraph g2 = new NetlistGraph(VerilogParser.parseString(verilogString, null).get(0));

		assertEquals(g2.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 1);

	}

	public void testSimpleAssign2() throws Exception {

		// simple assign statement (case 3 - see VerilogParser.parseAssignStatement)

		// parses, checks, generates, parses then checks again

		String strAssignSimple = "module top(a, b); input [3:0] a; output [3:0] b; assign b = a; endmodule";

		NetlistGraph g1 = new NetlistGraph(VerilogParser.parseString(strAssignSimple, null).get(0));

		assertEquals(g1.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 4);

		String verilogString = Generator.generateString(g1);

		NetlistGraph g2 = new NetlistGraph(VerilogParser.parseString(verilogString, null).get(0));

		assertEquals(g2.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 4);

	}

	public void testBitAssign1() throws Exception {

		// simple assign statement (case 2 - see VerilogParser.parseAssignStatement)

		// parses, checks, generates, parses then checks again

		String strAssignSimple = "module top(a, b, c); input a; input b; output [1:0] c; assign c[0] = a; assign c[1] = b; endmodule";

		NetlistGraph g1 = new NetlistGraph(VerilogParser.parseString(strAssignSimple, null).get(0));

		assertEquals(g1.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 2);

		String verilogString = Generator.generateString(g1);

		NetlistGraph g2 = new NetlistGraph(VerilogParser.parseString(verilogString, null).get(0));

		assertEquals(g2.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 2);

	}

	public void testBitAssign2() throws Exception {

		// simple assign statement (case 2 - see VerilogParser.parseAssignStatement)

		// parses, checks, generates, parses then checks again

		String strAssignArray1 = "module top(a, b, c); input [1:0] a; output b; output c; assign b = a[0]; assign c = a[1]; endmodule";

		NetlistGraph g1 = new NetlistGraph(VerilogParser.parseString(strAssignArray1, null).get(0));

		assertEquals(g1.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 2);

		String verilogString = Generator.generateString(g1);

		NetlistGraph g2 = new NetlistGraph(VerilogParser.parseString(verilogString, null).get(0));

		assertEquals(g2.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 2);

	}

	public void testBitAssign3() throws Exception {

		// simple assign statement (case 2 - see VerilogParser.parseAssignStatement)

		// parses, checks, generates, parses then checks again

		String strAssignSimple = "module top(a, b); input [1:0] a; output [1:0] b; assign b[0] = a[0]; assign b[1] = a[1]; endmodule";

		NetlistGraph g1 = new NetlistGraph(VerilogParser.parseString(strAssignSimple, null).get(0));

		assertEquals(g1.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 2);

		String verilogString = Generator.generateString(g1);

		NetlistGraph g2 = new NetlistGraph(VerilogParser.parseString(verilogString, null).get(0));

		assertEquals(g2.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 2);

	}

	public void testConcatAssign() throws Exception {

		// concat assign statement (case 1 - see VerilogParser.parseAssignStatement)

		// parses, checks, generates, parses then checks again

		String strAssignArray1 = "module top(a, b, c); input a, b; output [1:0] c; assign c = {a, b}; endmodule";

		NetlistGraph g1 = new NetlistGraph(VerilogParser.parseString(strAssignArray1, null).get(0));

		assertEquals(g1.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 2);

		String verilogString = Generator.generateString(g1);

		NetlistGraph g2 = new NetlistGraph(VerilogParser.parseString(verilogString, null).get(0));

		assertEquals(g2.getModulesByType(VerilogParser.CASSIGN_MOD).size(), 2);

	}

}
