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

		VerilogParser.parseString(verilogString, simpleLib); // try to parse g

	}

}
