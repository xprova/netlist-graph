package net.xprova.verilogparser;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

	// class members

	private static CommonTokenStream tokenStream;

	private static GateLibrary library1;

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

	private final static String ERR_MSG_12 = "gate instantiation is not supported";
	private final static String ERR_MSG_13 = "unsupported module connection";

	private final static String ERR_MSG_15 = "redefinition of module instance <%s>";
	private final static String ERR_MSG_16 = "module %s does not exist in library";
	private final static String ERR_MSG_17 = "non-scalar net <%s> must be explicitly declared";

	// exception generation and handling

	private static void fail(String errMsg, Module_itemContext itemCon) throws UnsupportedGrammerException {

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

		System.err.printf("Parser error (line %d): %s\n", lineNum, tokenStr);

		throw new UnsupportedGrammerException("");

	}

	private static void fail(String errMsg) throws UnsupportedGrammerException {

		throw new UnsupportedGrammerException(errMsg);

	}

	// parsing

	public static ArrayList<Netlist> parseFile(String verilogFile) throws Exception {

		// This form of the method is used for parsing library files.
		// Modules in library files may not instantiate any modules within them;
		// then can only contain port declarations, for example:
		// module NOT (y, a); input a; output y; endmodule

		return parseFile(verilogFile, new GateLibrary());

	}

	public static ArrayList<Netlist> parseFile(String verilogFile, GateLibrary library) throws Exception {

		FileInputStream stream1 = new FileInputStream(verilogFile);

		return parse(new ANTLRInputStream(stream1), library);

	}

	public static ArrayList<Netlist> parseString(String str) throws Exception {

		// This form of the method is used for parsing library files.
		// Modules in library files may not instantiate any modules within them;
		// then can only contain port declarations, for example:
		// module NOT (y, a); input a; output y; endmodule

		return parseString(str, new GateLibrary());

	}

	public static ArrayList<Netlist> parseString(String str, GateLibrary library) throws Exception {

		return parse(new ANTLRInputStream(str), library);

	}

	private static ArrayList<Netlist> parse(ANTLRInputStream antlr, GateLibrary library1) throws Exception {

		// ANTLR parsing

		Verilog2001Lexer lexer1 = new Verilog2001Lexer(antlr);

		CommonTokenStream tokenStream = new CommonTokenStream(lexer1);

		Verilog2001Parser parser1 = new Verilog2001Parser(tokenStream);

		Source_textContext source = parser1.source_text();

		List<DescriptionContext> modules = source.description();

		int nModules = modules.size();

		ArrayList<Netlist> netlistArr = new ArrayList<Netlist>();

		for (int j = 0; j < nModules; j++) {

			Module_declarationContext module = modules.get(j).module_declaration();

			VerilogParser.tokenStream = tokenStream;

			VerilogParser.library1 = library1;

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

					parsePortDeclaration(itemCon, netlist);

					continue;
				}

				Module_or_generate_itemContext modItem = itemCon.module_or_generate_item();

				if (modItem != null) {

					if (modItem.gate_instantiation() != null) {

						fail(ERR_MSG_12, itemCon);

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

							fail(ERR_MSG_6, itemCon);
						}
					}

					Continuous_assignContext conAssignItem = modItem.continuous_assign();

					if (conAssignItem != null) {

						List<Net_assignmentContext> assignList = conAssignItem.list_of_net_assignments()
								.net_assignment();

						for (Net_assignmentContext conAssign : assignList) {

							// check lval

							String lval = conAssign.net_lvalue().getText();

							boolean isEscaped = lval.contains("\\");

							boolean looksArr = lval.contains("[");

							boolean looksConcat = lval.contains("{");

							if (isEscaped || looksArr || looksConcat)
								fail(ERR_MSG_4, itemCon);

							// check rval

							String rval = conAssign.expression().getText();

							isEscaped = rval.contains("\\");

							looksConcat = rval.contains("{");

							boolean nestedConcat = (rval.indexOf("{") != rval.lastIndexOf("{"));

							if (isEscaped || looksArr || nestedConcat)
								fail(ERR_MSG_4, itemCon);

							// item can be parsed:

							assignDefs.add(itemCon);

						}

						continue;

					}

				}

				// Unsupported grammar

				fail(ERR_MSG_4, itemCon);

			}

			// now process netDefs then modDefs

			// order is important: nets must be populated before processing
			// modules

			for (Module_itemContext entry : netDefs) {

				parseNetDeclaration(entry, netlist);
			}

			for (Module_itemContext entry : modDefs) {

				parseModuleInstantiation(entry, netlist);
			}

			for (Module_itemContext entry : assignDefs) {

				parseAssignStatement(entry, netlist);
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

	private static void parseAssignStatement(Module_itemContext itemCon, Netlist netlist) throws Exception {

		List<Net_assignmentContext> assignList = itemCon.module_or_generate_item().continuous_assign()
				.list_of_net_assignments().net_assignment();

		for (Net_assignmentContext conAssign : assignList) {

			String lval = conAssign.net_lvalue().getText();
			String rval = conAssign.expression().getText();

			boolean isConcatR = rval.contains("{");

			if (isConcatR) {

//				int k1 = rval.indexOf("}");
//				int k2 = rval.indexOf("}");
//
//				String netNameStr = "hello";

				throw new Exception("unsupported");


			} else {

				String wireModID = "WIRE_NG_INTERNAL_u" + netlist.modules.size();

				Module m = new Module(wireModID, "WIRE_NG_INTERNAL");

				netlist.modules.put(wireModID, m);

				ParserRuleContext zpL = conAssign.net_lvalue();
				ParserRuleContext zpR = conAssign.net_lvalue();

				processModulePinConnection(m, zpL, "OUT", lval, itemCon, PinDirection.OUT, netlist);
				processModulePinConnection(m, zpR, "IN", rval, itemCon, PinDirection.IN, netlist);

			}

		}

	}

	private static void parseModuleInstantiation(Module_itemContext itemCon, Netlist netlist) throws Exception {

		Module_or_generate_itemContext modItem = itemCon.module_or_generate_item();

		Module_instantiationContext modIns = modItem.module_instantiation();

		String id = modIns.module_identifier().getText();

		List<Module_instanceContext> instances = modIns.module_instance();

		for (int j = 0; j < instances.size(); j++) {

			Module_instanceContext i = instances.get(j);

			Module m = new Module();

			m.type = id;

			m.id = i.name_of_instance().getText();

			ArrayList<Port> modulePorts = library1.get(id);

			if (modulePorts == null) {

				String msg2 = String.format(ERR_MSG_16, id);

				fail(msg2, itemCon);

			}

			List_of_port_connectionsContext conList = i.list_of_port_connections();

			List<Ordered_port_connectionContext> orderedCons = conList.ordered_port_connection();

			List<Named_port_connectionContext> namedCons = conList.named_port_connection();

			boolean ordered = orderedCons.size() > 0;

			int conCount = ordered ? orderedCons.size() : namedCons.size();

			for (int k = 0; k < conCount; k++) {

				ExpressionContext expr;

				expr = ordered ? orderedCons.get(k).expression() : namedCons.get(k).expression();

				List<TermContext> termList = expr.term();

				if (termList.size() != 1) {

					fail(ERR_MSG_4, itemCon);
				}

				TermContext z = termList.get(0);

				PrimaryContext zp = z.primary();

				String port_con = "";

				if (zp.hierarchical_identifier() != null) {

					port_con = zp.hierarchical_identifier().getText();

				} else if (zp.number() != null) {

					port_con = zp.number().getText();

				} else {

					fail("ERR_MSG_2");
				}

				String port_id = ordered ? modulePorts.get(k).id : namedCons.get(k).port_identifier().getText();

				PinDirection dir = library1.getPort(id, port_id).direction;

				processModulePinConnection(m, zp, port_id, port_con, itemCon, dir, netlist);

			}

			if (netlist.modules.containsKey(m.id)) {

				String msg = String.format(ERR_MSG_15, m.id);

				fail(msg, itemCon);
			}

			netlist.modules.put(m.id, m);

		}

	}

	private static void parseNetDeclaration(Module_itemContext itemCon, Netlist netlist)
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

				fail(ERR_MSG_7, itemCon);

			}

		}

		List<Net_identifierContext> list = netDec.list_of_net_identifiers().net_identifier();

		for (int j = 0; j < list.size(); j++) {

			String netName = list.get(j).getText();

			if (netlist.nets.get(netName) != null) {

				fail(ERR_MSG_8, itemCon);

			} else {

				Net net = new Net(netName);

				net.start = start;

				net.end = end;

				netlist.nets.put(netName, net);

			}

		}

	}

	private static void parsePortDeclaration(Module_itemContext itemCon, Netlist netlist)
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

				fail(str, itemCon);

			}

			p.direction = direction;

			if (r != null) {

				ExpressionContext expMSB = r.msb_constant_expression().constant_expression().expression();

				ExpressionContext expLSB = r.lsb_constant_expression().constant_expression().expression();

				try {

					p.start = Integer.parseInt(expMSB.getText());

					p.end = Integer.parseInt(expLSB.getText());

				} catch (Exception e) {

					fail(ERR_MSG_7, itemCon);

				}

			}

		}
	}

	private static void processModulePinConnection(Module m, ParserRuleContext zp, String port_id, String port_con,
			Module_itemContext itemCon, PinDirection dir, Netlist netlist) throws UnsupportedGrammerException {

		// this function processes a single port connection of a given module
		// instantiation

		// inputs:

		// m : Module object
		// zp : holds the tokens for the net to which the port is connected

		// port_id : id of module port to be connected
		// port_con : net to be connected to module

		// itemCon : context of module instantiation (needed when throwing
		// Exceptions)

		// dir : direction of pin

		int tokens = zp.stop.getTokenIndex() - zp.start.getTokenIndex() + 1;

		if (tokens == 1) {

			PinConnection pcon = new PinConnection(port_con, 0, dir);

			m.connections.put(port_id, pcon);

			Net net = netlist.nets.get(port_con);

			if (net == null) {

				// implicit net declaration

				netlist.nets.put(port_con, new Net(port_con));

			} else {

				// check if bit 0 is in net range

				if (!netlist.nets.get(port_con).inRange(0)) {

					String msg1_a = "net <%s> does not have bit <%d>";

					String msg2 = String.format(msg1_a, port_con, 0);

					fail(msg2, itemCon);
				}

			}

		} else if (tokens == 4) {

			Token token2 = tokenStream.get(zp.start.getTokenIndex() + 1);

			Token token3 = tokenStream.get(zp.start.getTokenIndex() + 2);

			Token token4 = tokenStream.get(zp.start.getTokenIndex() + 3);

			if (token2.getText().equals("[") && token4.getText().equals("]")) {

				port_con = zp.start.getText();

				int pin = Integer.parseInt(token3.getText());

				PinConnection pcon = new PinConnection(port_con, pin, dir);

				m.connections.put(port_id, pcon);

				// check if net port_con is explicitly declared

				if (!netlist.nets.containsKey(port_con)) {

					String msg2 = String.format(ERR_MSG_17, port_con);

					fail(msg2, itemCon);
				}

				// check if bit is in net range

				if (!netlist.nets.get(port_con).inRange(pin)) {

					String msg1_a = "net <%s> does not have bit <%d>";

					String msg2 = String.format(msg1_a, port_con, pin);

					fail(msg2, itemCon);
				}
			}

		} else {

			fail(ERR_MSG_13, itemCon);
		}
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

	// print

	@SuppressWarnings("unused")
	private void print(ParserRuleContext context, CommonTokenStream tokenStream) {

		Interval int1 = context.getSourceInterval(); // get token interval

		System.out.println(tokenStream.getText(int1));
	}

}
