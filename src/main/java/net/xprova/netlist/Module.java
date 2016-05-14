package net.xprova.netlist;

import java.util.HashMap;

public class Module {

	public String id; // e.g. myFlop1

	public String type; // type (e.g. DFF1)

	public HashMap<String, PinConnection> connections; // port (String) ->
														// PinConnection

	public Module() {

		connections = new HashMap<String, PinConnection>();
	}

	public void print() {

		String str = String
				.format("id = %s, type = %s, connections:", id, type);

		System.out.println(str);

		for (String entry : connections.keySet()) {

			PinConnection con = connections.get(entry);

			str = String.format("%s -> %s[%d]", entry, con.net, con.bit);

			System.out.println(str);

		}

	}

}
