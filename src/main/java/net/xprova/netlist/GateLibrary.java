package net.xprova.netlist;

import java.util.ArrayList;
import java.util.HashMap;

public class GateLibrary {

	// key: module id
	// value: ArrayList of ports

	private HashMap<String, ArrayList<Port>> list; // key is module type

	private HashMap<String, Port> portLUT; // key is module type + space + port
											// id (e.g. "DFF clk")

	public GateLibrary(String name) {

		list = new HashMap<String, ArrayList<Port>>();

		portLUT = new HashMap<String, Port>();

		if ("faraday90nm".equals(name)) {

			loadMiniFaraday90nm();

		} else {

			loadTestLibrary();
		}

		buildPortLUT();

	}

	private void buildPortLUT() {
		for (String str : list.keySet()) {

			ArrayList<Port> portList = list.get(str);

			for (Port p : portList) {

				portLUT.put(str + " " + p.id, p);
			}

		}
	}

	private void loadMiniFaraday90nm() {

		// this is a minimal set of Faraday90nm that should be sufficient for
		// most (all?) designs

		list.clear();

		// QDFFRSBX1

		ArrayList<Port> ports = new ArrayList<Port>();

		ports.add(new Port("D", PinDirection.IN));
		ports.add(new Port("CK", PinDirection.IN));
		ports.add(new Port("RB", PinDirection.IN));
		ports.add(new Port("SB", PinDirection.IN));
		ports.add(new Port("Q", PinDirection.OUT));

		list.put("QDFFRSBX1", ports);

		// INVX1, BUFX1

		ports = new ArrayList<Port>();

		ports.add(new Port("I", PinDirection.IN));
		ports.add(new Port("O", PinDirection.OUT));

		list.put("INVX1", ports);
		list.put("BUFX1", ports);
		
		// AN2X1, OR2X1, NR2X1, ND2X1

		ports = new ArrayList<Port>();

		ports.add(new Port("I1", PinDirection.IN));
		ports.add(new Port("I2", PinDirection.IN));
		ports.add(new Port("O", PinDirection.OUT));

		list.put("AN2X1", ports);
		list.put("OR2X1", ports);
		list.put("NR2X1", ports);
		list.put("ND2X1", ports);

		// HA1X1

		ports = new ArrayList<Port>();

		ports.add(new Port("A", PinDirection.IN));
		ports.add(new Port("B", PinDirection.IN));
		ports.add(new Port("C", PinDirection.OUT));
		ports.add(new Port("S", PinDirection.OUT));

		list.put("HA1X1", ports);

		// TIE0X1, TIE1X1

		ports = new ArrayList<Port>();

		ports.add(new Port("O", PinDirection.OUT));

		list.put("TIE0X1", ports);
		list.put("TIE1X1", ports);
		

	}

	private void loadTestLibrary() {

		list.clear();

		// DFF

		ArrayList<Port> ports = new ArrayList<Port>();

		ports.add(new Port("clk", PinDirection.IN));
		ports.add(new Port("reset", PinDirection.IN));
		ports.add(new Port("d", PinDirection.IN));
		ports.add(new Port("q", PinDirection.OUT));

		list.put("DFF", ports);

		// AND

		ports = new ArrayList<Port>();

		ports.add(new Port("a", PinDirection.IN));
		ports.add(new Port("b", PinDirection.IN));
		ports.add(new Port("y", PinDirection.OUT));

		list.put("AND", ports);

		// OR

		ports = new ArrayList<Port>();

		ports.add(new Port("a", PinDirection.IN));
		ports.add(new Port("b", PinDirection.IN));
		ports.add(new Port("y", PinDirection.OUT));

		list.put("OR", ports);

		// NOT

		ports = new ArrayList<Port>();

		ports.add(new Port("a", PinDirection.IN));
		ports.add(new Port("y", PinDirection.OUT));

		list.put("NOT", ports);

		// MUX2

		ports = new ArrayList<Port>();

		ports.add(new Port("i0", PinDirection.IN));
		ports.add(new Port("i1", PinDirection.IN));
		ports.add(new Port("s", PinDirection.IN));
		ports.add(new Port("y", PinDirection.OUT));

		list.put("MUX2", ports);

	}

	public ArrayList<Port> get(String id) {

		return list.get(id);
	}

	public Port getPort(String module, String port) throws Exception {

		String key = module + " " + port;

		if (!portLUT.containsKey(key)) {

			throw new Exception("port <" + port + "> undefined for module <"
					+ module + ">");

		}

		return portLUT.get(key);

	}

}
