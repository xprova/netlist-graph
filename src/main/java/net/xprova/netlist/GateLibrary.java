package net.xprova.netlist;

import java.util.ArrayList;
import java.util.HashMap;

public class GateLibrary {

	// key: module id
	// value: ArrayList of ports

	private HashMap<String, ArrayList<Port>> list; // key is module type

	private HashMap<String, Port> portLUT; // key is module type + space + port
											// id (e.g. "DFF clk")

	public GateLibrary() {

		// creates empty library

		list = new HashMap<String, ArrayList<Port>>();

		portLUT = new HashMap<String, Port>();

	}

	public GateLibrary(ArrayList<Netlist> netlistArr) {

		// this constructor loads netlistArr objects created by parsing
		// a Verilog library file

		list = new HashMap<String, ArrayList<Port>>();

		portLUT = new HashMap<String, Port>();

		for (int i = 0; i < netlistArr.size(); i++) {

			Netlist nl = netlistArr.get(i);

			ArrayList<Port> ports = new ArrayList<Port>();

			for (Port p : nl.ports.values()) {

				ports.add(new Port(p.id, p.direction));

			}

			ports.add(new Port("clk", PinDirection.IN));
			ports.add(new Port("reset", PinDirection.IN));
			ports.add(new Port("d", PinDirection.IN));
			ports.add(new Port("q", PinDirection.OUT));

			list.put(nl.name, ports);

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

	public ArrayList<Port> get(String id) {

		return list.get(id);
	}

	public Port getPort(String module, String port) throws Exception {

		String key = module + " " + port;

		if (!portLUT.containsKey(key)) {

			throw new Exception("port <" + port + "> undefined for module <" + module + ">");

		}

		return portLUT.get(key);

	}

	public void printModules() {

		System.out.println("Module list:");

		for (String p : list.keySet()) {

			System.out.println(p);

		}

	}

}
