package net.xprova.netlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

	public String getPortList() {

		return join(orderedPorts, ", ");

	}

	private static String join(ArrayList<String> list, String delim) {

		if (list.isEmpty()) {

			return "";

		} else if (list.size() == 1) {

			return list.get(0);

		} else {

			StringBuilder sb = new StringBuilder(1024);

			sb.append(list.get(0));

			for (int i = 1; i < list.size(); i++) {

				sb.append(delim);

				sb.append(list.get(i));

			}

			return sb.toString();

		}

	}

	public void print() {

		print("Module name: " + name);

		print("");

		print("Ports: ");

		for (Map.Entry<String, Port> entry : ports.entrySet()) {

			entry.getValue().print();

		}

		print("");

		print("Nets: ");

		for (Map.Entry<String, Net> entry : nets.entrySet()) {

			entry.getValue().print();

		}

	}

	private void print(String s) {

		System.out.println(s);

	}

}
