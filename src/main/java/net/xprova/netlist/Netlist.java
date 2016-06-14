package net.xprova.netlist;

import java.util.ArrayList;
import java.util.HashMap;

public class Netlist {

	public String name;

	public HashMap<String, Port> ports;

	public HashMap<String, Net> nets;

	public HashMap<String, Module> modules;

	public ArrayList<String> orderedPorts; // as in header

	public ArrayList<Port> getOrderedPorts() {

		ArrayList<Port> result = new ArrayList<Port>();

		for (String portName : orderedPorts) {

			result.add(ports.get(portName));

		}

		return result;

	}

}
