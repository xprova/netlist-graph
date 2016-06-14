package net.xprova.netlist;

import java.util.HashMap;

public class Module {

	public String id; // e.g. myFlop1

	public String type; // type (e.g. DFF1)

	// connections is : port (String) -> PinConnection
	public HashMap<String, PinConnection> connections;

	public Module() {

		connections = new HashMap<String, PinConnection>();
	}

	public Module (String id, String type) {

		connections = new HashMap<String, PinConnection>();

		this.id = id;
		this.type = type;

	}

	@Override
	public String toString() {

		return id + " (" + type + ")";

	}

}
