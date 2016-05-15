package net.xprova.netlistgraph;

import java.util.ArrayList;

import junit.framework.TestCase;
import net.xprova.netlist.GateLibrary;
import net.xprova.netlist.Netlist;
import net.xprova.verilogparser.VerilogParser;

public class GeneratorTest extends TestCase {

	public void testGenerator() throws Exception {

		// this tests Generator by attempting to re-parse generated Verilog
		
		GateLibrary lib = new GateLibrary("");
		
		ClassLoader classLoader = getClass().getClassLoader();
		
		String fullPath = classLoader.getResource("minimal.v").getPath();
		
		ArrayList<Netlist> netListArr = VerilogParser.parseFile(fullPath, lib);
		
		NetlistGraph g = new NetlistGraph(netListArr.get(0));
		
		String verilogString = Generator.generateString(g);
		
		VerilogParser.parseString(verilogString, lib);

	}

}
