package net.xprova.verilogparser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

import net.xprova.netlist.GateLibrary;
import net.xprova.netlist.Module;
import net.xprova.netlist.Net;
import net.xprova.netlist.Netlist;
import net.xprova.netlist.PinConnection;
import net.xprova.netlist.PinDirection;
import net.xprova.netlist.Port;
import net.xprova.verilogparser.Verilog2001Parser.Continuous_assignContext;
import net.xprova.verilogparser.Verilog2001Parser.DescriptionContext;
import net.xprova.verilogparser.Verilog2001Parser.ExpressionContext;
import net.xprova.verilogparser.Verilog2001Parser.Inout_declarationContext;
import net.xprova.verilogparser.Verilog2001Parser.Input_declarationContext;
import net.xprova.verilogparser.Verilog2001Parser.List_of_port_connectionsContext;
import net.xprova.verilogparser.Verilog2001Parser.List_of_portsContext;
import net.xprova.verilogparser.Verilog2001Parser.Module_declarationContext;
import net.xprova.verilogparser.Verilog2001Parser.Module_instanceContext;
import net.xprova.verilogparser.Verilog2001Parser.Module_instantiationContext;
import net.xprova.verilogparser.Verilog2001Parser.Module_itemContext;
import net.xprova.verilogparser.Verilog2001Parser.Module_or_generate_itemContext;
import net.xprova.verilogparser.Verilog2001Parser.Module_or_generate_item_declarationContext;
import net.xprova.verilogparser.Verilog2001Parser.Named_port_connectionContext;
import net.xprova.verilogparser.Verilog2001Parser.Net_assignmentContext;
import net.xprova.verilogparser.Verilog2001Parser.Net_declarationContext;
import net.xprova.verilogparser.Verilog2001Parser.Net_identifierContext;
import net.xprova.verilogparser.Verilog2001Parser.Net_typeContext;
import net.xprova.verilogparser.Verilog2001Parser.Ordered_port_connectionContext;
import net.xprova.verilogparser.Verilog2001Parser.Output_declarationContext;
import net.xprova.verilogparser.Verilog2001Parser.PortContext;
import net.xprova.verilogparser.Verilog2001Parser.Port_declarationContext;
import net.xprova.verilogparser.Verilog2001Parser.Port_identifierContext;
import net.xprova.verilogparser.Verilog2001Parser.PrimaryContext;
import net.xprova.verilogparser.Verilog2001Parser.RangeContext;
import net.xprova.verilogparser.Verilog2001Parser.Source_textContext;
import net.xprova.verilogparser.Verilog2001Parser.TermContext;

/**
 *
 * Module
 *
 */
public class VerilogParser {

	// constants

	// CASSIGN_MOD : name of internal virtual module used to represent
	// continuous assignment statements (i.e. wires)

	public final static String CASSIGN_MOD = "*CASSIGN";

	// class members

	private static CommonTokenStream tokenStream;

	private static GateLibrary library1;

	private static Verilog2001Parser parser1;

	// error messages

	private final static String ERR_MSG_1 = "port <%s> not declared in module header";
	private final static String ERR_MSG_2 = "could not parse port list";
	private final static String ERR_MSG_3 = "direction of port <%s> unspecified";
	private final static String ERR_MSG_4 = "unsupported grammar";
	private final static String ERR_MSG_5 = "port <%s> declared more than once in module header";
	private final static String ERR_MSG_6 = "unsupported net declaration grammar";
	private final static String ERR_MSG_7 = "Unsupported grammar for bit range";
	private final static String ERR_MSG_8 = "net redefinition";
	private final static String ERR_MSG_9 = "declaration mismatch between port <%s> [%d:%d] and wire <%s> [%d:%d]";

	private final static String ERR_MSG_10 = "implicit declaratin of array net";
	private final static String ERR_MSG_12 = "gate instantiation is not supported";
	private final static String ERR_MSG_13 = "unsupported module connection";

	private final static String ERR_MSG_15 = "redefinition of module instance <%s>";
	private final static String ERR_MSG_16 = "module %s does not exist in library";
	private final static String ERR_MSG_17 = "non-scalar net <%s> must be explicitly declared";

	// exception generation and handling

	private static void fail(String filename, String errMsg, Module_itemContext itemCon)
			throws UnsupportedGrammerException {

		Interval int1 = itemCon.getSourceInterval(); // get token interval

		Token firstToken = tokenStream.get(int1.a);

		int lineNum = firstToken.getLine(); // get line of first token

		// Determining j, first token in int1 which occurs at a different line

		int j;

		for (j = int1.a; j < int1.b; j++) {

			if (tokenStream.get(j).getLine() != lineNum)
				break;
		}

		// form a string from tokens 1 through j-1

		String tokenStr = tokenStream.getText(new Interval(int1.a, j));

		System.err.printf("Parser error (%s:%d): %s\n", filename, lineNum, tokenStr);

		fail(errMsg);

	}

	private static void fail(String errMsg) throws UnsupportedGrammerException {

		throw new UnsupportedGrammerException(errMsg);

	}

	// parsing API

	public static ArrayList<Netlist> parseFile(String verilogFile, GateLibrary library) throws Exception {

		// pass null as `library` to parse without library

		FileInputStream stream1 = new FileInputStream(verilogFile);

		return getNetlists(verilogFile, new ANTLRInputStream(stream1), library == null ? new GateLibrary() : library);

	}

	public static ArrayList<Netlist> parseString(String str, GateLibrary library) throws Exception {

		// pass null as `library` to parse without library

		return getNetlists("STRING", new ANTLRInputStream(str), library == null ? new GateLibrary() : library);

	}

	// parsing methods

	private static void createParser(String filename, ANTLRInputStream antlr, GateLibrary library1) {

		// ANTLR parsing

		Verilog2001Lexer lexer1 = new Verilog2001Lexer(antlr);

		CommonTokenStream tokenStream = new CommonTokenStream(lexer1);

		parser1 = new Verilog2001Parser(tokenStream);

		VerilogParser.tokenStream = tokenStream;

		VerilogParser.library1 = library1;

	}

	private static ArrayList<Netlist> getNetlists(String filename, ANTLRInputStream antlr, GateLibrary library1)
			throws Exception {

		// ANTLR parsing

		createParser(filename, antlr, library1);

		Source_textContext source = parser1.source_text();

		List<DescriptionContext> modules = source.description();

		int nModules = modules.size();

		ArrayList<Netlist> netlistArr = new ArrayList<Netlist>();

		for (int j = 0; j < nModules; j++) {

			Module_declarationContext module = modules.get(j).module_declaration();

			Netlist netlist = new Netlist();

			netlist.name = module.module_identifier().getText();

			List_of_portsContext portList = module.list_of_ports();

			if (portList == null) {

				fail(ERR_MSG_4);
			}

			ArrayList<String> ports = new ArrayList<String>();

			for (int i = 0; i < portList.port().size(); i++) {

				ports.add(portList.port(i).getText());

			}

			// Initialize member variables

			netlist.ports = new HashMap<String, Port>();

			netlist.nets = new HashMap<String, Net>();

			netlist.modules = new HashMap<String, Module>();

			netlist.orderedPorts = ports;

			List<Module_itemContext> x = module.module_item();

			// parse ports

			parsePortList(module, netlist);

			// main parsing loop:

			// initially goes through the module items adding them to either
			// netDefs, modDefs, assignDefs or throwing a parsing exception

			ArrayList<Module_itemContext> netDefs = new ArrayList<Module_itemContext>();

			ArrayList<Module_itemContext> modDefs = new ArrayList<Module_itemContext>();

			ArrayList<Module_itemContext> assignDefs = new ArrayList<Module_itemContext>();

			for (int i = 0; i < x.size(); i++) {

				Module_itemContext itemCon = x.get(i);

				if (itemCon.port_declaration() != null) {

					// port declaration

					parsePortDeclaration(filename, itemCon, netlist);

					continue;
				}

				Module_or_generate_itemContext modItem = itemCon.module_or_generate_item();

				if (modItem != null) {

					if (modItem.gate_instantiation() != null) {

						fail(filename, ERR_MSG_12, itemCon);

						continue;
					}

					if (modItem.module_instantiation() != null) {

						modDefs.add(itemCon);

						continue;
					}

					Module_or_generate_item_declarationContext modItem2 = modItem.module_or_generate_item_declaration();

					if (modItem2 != null) {

						Net_declarationContext netDec = modItem2.net_declaration();

						if (netDec != null) {

							// net declaration

							Net_typeContext netType = netDec.net_type();

							if (netType.getText().equals("wire")) {

								// net definition

								netDefs.add(itemCon);

								continue;
							}

						} else {

							fail(filename, ERR_MSG_6, itemCon);
						}
					}

					Continuous_assignContext conAssignItem = modItem.continuous_assign();

					if (conAssignItem != null) {

						List<Net_assignmentContext> assignList = conAssignItem.list_of_net_assignments()
								.net_assignment();

						for (Net_assignmentContext conAssign : assignList) {

							String lval = conAssign.net_lvalue().getText();
							String rval = conAssign.expression().getText();

							// boolean isEscapedL = lval.startsWith("\\");
							boolean isEscapedR = rval.startsWith("\\");

							boolean looksArrL = lval.contains(":");
							boolean looksArrR = rval.contains(":");

							boolean looksConcatL = lval.contains("{");
							boolean nestedConcatR = (rval.indexOf("{") != rval.lastIndexOf("{"));

							// check lval

							if (looksArrL || looksConcatL)
								fail(filename, ERR_MSG_4, itemCon);

							// check rval

							if ((looksArrR || nestedConcatR) && !isEscapedR)
								fail(filename, ERR_MSG_4, itemCon);

							// item can be parsed:

							assignDefs.add(itemCon);

						}

						continue;

					}

				}

				// Unsupported grammar

				fail(filename, ERR_MSG_4, itemCon);

			}

			// now process netDefs then modDefs

			// order is important: nets must be populated before processing
			// modules

			for (Module_itemContext entry : netDefs) {

				parseNetDeclaration(filename, entry, netlist);
			}

			for (Module_itemContext entry : modDefs) {

				parseModuleInstantiation(filename, entry, netlist);
			}

			for (Module_itemContext entry : assignDefs) {

				parseAssignStatement(filename, entry, netlist);
			}

			checkAll(netlist);

			netlistArr.add(netlist);

		}

		return netlistArr;

	}

	private static void parsePortList(Module_declarationContext module, Netlist netlist)
			throws UnsupportedGrammerException {

		if (module.list_of_ports() == null) {

			fail(ERR_MSG_2);

		}

		List<PortContext> portList = module.list_of_ports().port();

		if (portList.size() == 1 && "".equals(portList.get(0).getText())) {
			System.out.println("warning: empty port list");

			return;
		}

		for (int i = 0; i < portList.size(); i++) {

			String name = portList.get(i).getText();

			Port p = new Port(name, PinDirection.UNKONWN);

			if (netlist.ports.containsKey(name)) {

				String emsg = String.format(ERR_MSG_5, name);

				fail(emsg);
			}

			netlist.ports.put(name, p);

			netlist.nets.put(name, p);

		}

	}

	private static void parseAssignStatement(String filename, Module_itemContext itemCon, Netlist netlist)
			throws Exception {

		List<Net_assignmentContext> assignList = itemCon.module_or_generate_item().continuous_assign()
				.list_of_net_assignments().net_assignment();

		for (Net_assignmentContext conAssign : assignList) {

			String lval = conAssign.net_lvalue().getText();
			String rval = conAssign.expression().getText();

			boolean isConcatR = rval.contains("{");

			if (isConcatR) {

				int k1 = rval.indexOf("{");
				int k2 = rval.indexOf("}");

				String netNamesStr = rval.substring(k1 + 1, k2);

				String[] netNames = netNamesStr.split(",");

				int bitIndex = 0;

				for (String netName : netNames) {

					String wireModID = CASSIGN_MOD + "_u" + netlist.modules.size();

					Module m = new Module(wireModID, CASSIGN_MOD);

					netlist.modules.put(wireModID, m);

					m.connections.put("IN", new PinConnection(netName, 0, PinDirection.IN));

					m.connections.put("OUT", new PinConnection(lval, bitIndex, PinDirection.OUT));

					Net net = netlist.nets.get(netName);

					if (net == null) {

						// implicit net declaration

						netlist.nets.put(netName, new Net(netName));

					} else {

						// check if bit 0 is in net range

						if (!netlist.nets.get(netName).inRange(0)) {

							String msg1_a = "net <%s> does not have bit <%d>";

							String msg2 = String.format(msg1_a, netName, 0);

							fail(filename, msg2, itemCon);
						}

					}

					bitIndex += 1;

				}

			} else if (rval.contains("[")) {

				Net rNet = parseArrayNet(conAssign.expression());

				if (rNet == null)
					fail(filename, ERR_MSG_13, itemCon);

				String wireModID = CASSIGN_MOD + "_u" + netlist.modules.size();

				Module m = new Module(wireModID, CASSIGN_MOD);

				netlist.modules.put(wireModID, m);

				m.connections.put("IN", new PinConnection(rNet.id, rNet.start, PinDirection.IN));

				m.connections.put("OUT", new PinConnection(lval, rNet.start, PinDirection.OUT));

			} else {

				int bitsL = netlist.nets.get(lval).getCount();
				int bitsR = netlist.nets.get(rval).getCount();

				if (bitsL != bitsR)
					fail(filename, "bit number mismatch in continuous assignment statement", itemCon);

				for (int bit = 0; bit < bitsL; bit++) {

					String wireModID = CASSIGN_MOD + "_u" + netlist.modules.size();

					Module m = new Module(wireModID, CASSIGN_MOD);

					netlist.modules.put(wireModID, m);

					m.connections.put("IN", new PinConnection(rval, bit, PinDirection.IN));

					m.connections.put("OUT", new PinConnection(lval, bit, PinDirection.OUT));

				}

			}

		}

	}

	private static void parseModuleInstantiation(String filename, Module_itemContext itemCon, Netlist netlist)
			throws Exception {

		Module_or_generate_itemContext modItem = itemCon.module_or_generate_item();

		Module_instantiationContext modIns = modItem.module_instantiation();

		String moduleType = modIns.module_identifier().getText();

		List<Module_instanceContext> instances = modIns.module_instance();

		for (int j = 0; j < instances.size(); j++) {

			Module_instanceContext i = instances.get(j);

			Module m = new Module(i.name_of_instance().getText(), moduleType);

			ArrayList<Port> modulePorts = library1.get(moduleType);

			if (modulePorts == null) {

				String msg2 = String.format(ERR_MSG_16, moduleType);

				fail(filename, msg2, itemCon);

			}

			List_of_port_connectionsContext conList = i.list_of_port_connections();

			List<Ordered_port_connectionContext> orderedCons = conList.ordered_port_connection();

			List<Named_port_connectionContext> namedCons = conList.named_port_connection();

			boolean ordered = orderedCons.size() > 0;

			int conCount = ordered ? orderedCons.size() : namedCons.size();

			if (conCount > modulePorts.size()) {

				String strE = String.format(
						"module instantiation contains %d ports while only %d are defined in library", conCount,
						modulePorts.size());

				fail(filename, strE, itemCon);

			}

			// loop through ports

			for (int k = 0; k < conCount; k++) {

				ExpressionContext expr = ordered ? orderedCons.get(k).expression() : namedCons.get(k).expression();

				List<TermContext> termList = expr.term();

				if (termList.size() != 1)
					fail(filename, ERR_MSG_4, itemCon);

				PrimaryContext zp = termList.get(0).primary();

				String portNet = ""; // net connected though this port

				if (zp.hierarchical_identifier() != null) {

					portNet = zp.hierarchical_identifier().getText();

				} else if (zp.number() != null) {

					portNet = zp.number().getText();

				} else {

					fail(ERR_MSG_2);
				}

				String portID = ordered ? modulePorts.get(k).id : namedCons.get(k).port_identifier().getText();

				Port port = library1.getPort(moduleType, portID);

				// start processing module pin connections

				// this section here does the following:

				// - adds a PinConnection to the `connections` set of Module `m`
				// - create any single-bit nets that the port connection
				// references, and adds them to netlist.nets

				if (portNet.contains("[")) {

					// indexed net identifier, (e.g. "x[1]")

					Net netR = parseArrayNet(zp);

					if (netR == null)
						fail(filename, ERR_MSG_13, itemCon);

					PinConnection pcon = new PinConnection(netR.id, netR.start, port.direction);

					m.connections.put(port.id, pcon);

					// check if net port_con is explicitly declared

					if (!netlist.nets.containsKey(netR.id)) {

						String msg2 = String.format(ERR_MSG_17, portNet);

						fail(filename, msg2, itemCon);
					}

					// check if bit is in net range

					if (!netlist.nets.get(netR.id).inRange(netR.start)) {

						String msg1_a = "net <%s> does not have bit <%d>";

						String msg2 = String.format(msg1_a, portNet, netR.start);

						fail(filename, msg2, itemCon);
					}

				} else {

					// non-indexed net identifier (e.g. "x")

					Net net = netlist.nets.get(portNet);

					if (net == null) {

						// implicit net declaration

						if (port.getCount() > 1) {

							// implicit net declaration with array ports is not
							// supported atm

							fail(filename, ERR_MSG_10, itemCon);

						} else {

							netlist.nets.put(portNet, new Net(portNet));

						}
					}

					for (int bit : port.getBits()) {

						PinConnection pcon = new PinConnection(portNet, bit, port.direction);

						String pID = port.getCount() > 1 ? (port.id + "[" + bit + "]") : port.id;

						m.connections.put(pID, pcon);

					}

				}

				// finish processing module pin connections

			}

			if (netlist.modules.containsKey(m.id)) {

				String msg = String.format(ERR_MSG_15, m.id);

				fail(filename, msg, itemCon);
			}

			netlist.modules.put(m.id, m);

		}

	}

	private static void parseNetDeclaration(String filename, Module_itemContext itemCon, Netlist netlist)
			throws UnsupportedGrammerException {

		Module_or_generate_itemContext modItem = itemCon.module_or_generate_item();

		Module_or_generate_item_declarationContext modItem2 = modItem.module_or_generate_item_declaration();

		Net_declarationContext netDec = modItem2.net_declaration();

		RangeContext r = netDec.range();

		int start = 0, end = 0;

		if (r != null) {

			ExpressionContext expMSB = r.msb_constant_expression().constant_expression().expression();

			ExpressionContext expLSB = r.lsb_constant_expression().constant_expression().expression();

			try {

				start = Integer.parseInt(expMSB.getText());

				end = Integer.parseInt(expLSB.getText());

			} catch (Exception e) {

				fail(filename, ERR_MSG_7, itemCon);

			}

		}

		List<Net_identifierContext> list = netDec.list_of_net_identifiers().net_identifier();

		for (int j = 0; j < list.size(); j++) {

			String netName = list.get(j).getText();

			if (netlist.nets.get(netName) != null) {

				fail(filename, ERR_MSG_8, itemCon);

			} else {

				Net net = new Net(netName);

				net.start = start;

				net.end = end;

				netlist.nets.put(netName, net);

			}

		}

	}

	private static void parsePortDeclaration(String filename, Module_itemContext itemCon, Netlist netlist)
			throws UnsupportedGrammerException {

		Port_declarationContext portDec = itemCon.port_declaration();

		Input_declarationContext inp = portDec.input_declaration();

		Output_declarationContext outp = portDec.output_declaration();

		Inout_declarationContext inout = portDec.inout_declaration();

		List<Port_identifierContext> portList = null;

		RangeContext r = null;

		PinDirection direction = PinDirection.UNKONWN;

		if (inp != null) {

			portList = inp.list_of_port_identifiers().port_identifier();

			direction = PinDirection.IN;

			r = inp.range();

		} else if (outp != null) {

			portList = outp.list_of_port_identifiers().port_identifier();

			direction = PinDirection.OUT;

			r = outp.range();

		} else if (inout != null) {

			portList = inout.list_of_port_identifiers().port_identifier();

			direction = PinDirection.INOUT;

			r = inout.range();

			// fail(ERR_MSG_14, itemCon);

		}

		for (int j = 0; j < portList.size(); j++) {

			Port_identifierContext portIdent = portList.get(j);

			String portName = portIdent.getText();

			// now need to lookup this port in ArrayList ports

			Port p = netlist.ports.get(portName);

			if (p == null) {

				// port wasn't declared in module header

				String str = String.format(ERR_MSG_1, portName);

				fail(filename, str, itemCon);

			}

			p.direction = direction;

			if (r != null) {

				ExpressionContext expMSB = r.msb_constant_expression().constant_expression().expression();

				ExpressionContext expLSB = r.lsb_constant_expression().constant_expression().expression();

				try {

					p.start = Integer.parseInt(expMSB.getText());

					p.end = Integer.parseInt(expLSB.getText());

				} catch (Exception e) {

					fail(filename, ERR_MSG_7, itemCon);

				}

			}

		}
	}

	private static Net parseArrayNet(ParserRuleContext con) throws Exception {

		// note: returns null on failure, parent method must throw parsing error
		// if this method returns null

		int tokensR = con.getSourceInterval().length();

		if (tokensR == 4 || tokensR == 5) {

			// indexed identifier, e.g. x[1]

			int spaceBuffer = tokensR == 5 ? 1 : 0;

			int firstToken = con.start.getTokenIndex();

			Token token1 = tokenStream.get(firstToken);

			Token token2 = tokenStream.get(firstToken + 1 + spaceBuffer);

			Token token3 = tokenStream.get(firstToken + 2 + spaceBuffer);

			Token token4 = tokenStream.get(firstToken + 3 + spaceBuffer);

			if (token2.getText().equals("[") && token4.getText().equals("]")) {

				Net result = new Net(token1.getText());

				int bit = Integer.parseInt(token3.getText());

				result.start = bit;

				result.end = bit;

				return result;

			}

		}

		return null;

	}

	// checks

	private static void checkAll(Netlist netlist) throws Exception {

		checkPortDirections(netlist);

		checkPortsAndNets(netlist);

	}

	private static void checkPortDirections(Netlist netlist) throws UnsupportedGrammerException {

		// verify no ports have UNKNOWN direction

		for (Map.Entry<String, Port> entry : netlist.ports.entrySet()) {

			if (entry.getValue().direction == PinDirection.UNKONWN) {

				String msg = String.format(ERR_MSG_3, entry.getValue().id);

				fail(msg);

			}

		}
	}

	private static void checkPortsAndNets(Netlist netlist) throws UnsupportedGrammerException {

		// verify that port definitions match any existing net definitions

		for (Map.Entry<String, Port> entry : netlist.ports.entrySet()) {

			Port port = entry.getValue();

			Net net = netlist.nets.get(port.id);

			if (net != null) {

				if (net.start != port.start || net.end != port.end) {

					String emsg = String.format(ERR_MSG_9, port.id, port.start, port.end, net.id, net.start, net.end);

					fail(emsg);

				}
			}

		}

	}

	// extract module dependencies

	public static HashMap<String, HashSet<String>> getDependencies(String verilogFile) throws IOException {

		HashMap<String, HashSet<String>> dependencies = new HashMap<String, HashSet<String>>();

		FileInputStream stream1 = new FileInputStream(verilogFile);

		ANTLRInputStream antlr = new ANTLRInputStream(stream1);

		// ANTLR parsing

		Verilog2001Lexer lexer1 = new Verilog2001Lexer(antlr);

		CommonTokenStream tokenStream = new CommonTokenStream(lexer1);

		Verilog2001Parser parser1 = new Verilog2001Parser(tokenStream);

		Source_textContext source = parser1.source_text();

		List<DescriptionContext> modules = source.description();

		for (DescriptionContext module : modules) {

			String design = module.module_declaration().module_identifier().getText();

			HashSet<String> designDepends = new HashSet<String>();

			List<Module_itemContext> modItems = module.module_declaration().module_item();

			for (Module_itemContext itemCon : modItems) {

				Module_or_generate_itemContext modItem = itemCon.module_or_generate_item();

				if (modItem != null) {

					Module_instantiationContext mi = modItem.module_instantiation();

					if (mi != null) {

						String id = mi.module_identifier().getText();

						designDepends.add(id);

					}

				}

			}

			dependencies.put(design, designDepends);

		}

		return dependencies;

	}

}
