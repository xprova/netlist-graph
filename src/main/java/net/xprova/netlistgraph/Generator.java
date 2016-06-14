package net.xprova.netlistgraph;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Generator {

	private static class Range {

		public String name, type;

		public int min, max;

		public Range(String name, String type, int bit) {

			this.name = name;
			this.type = type;
			this.min = this.max = bit;

		}

	}

	public static void generateFile(NetlistGraph graph, String file) throws Exception {

		PrintWriter out = new PrintWriter(file, "UTF-8");

		out.print(generateString(graph));

		out.close();

	}

	public static String generateString(NetlistGraph graph) throws Exception {

		ArrayList<String> wires = new ArrayList<String>();

		ArrayList<String> mods = new ArrayList<String>();

		ArrayList<String> ports = new ArrayList<String>();

		HashMap<String, Range> nets = new HashMap<String, Range>();

		for (Vertex v : graph.getNets()) {

			// parse net name and bit

			int k1 = v.name.indexOf("[");
			int k2 = v.name.indexOf("]");

			String name = v.name;

			int bit = 0;

			if (k1 != -1 && k2 != -1) {

				name = v.name.substring(0, k1);

				bit = Integer.valueOf(v.name.substring(k1 + 1, k2));
			}

			if (nets.containsKey(name)) {

				Range r = nets.get(name);

				r.min = Math.min(r.min, bit);
				r.max = Math.max(r.max, bit);

			} else {

				nets.put(name, new Range(name, v.subtype, bit));

				if ("input".equals(v.subtype) || "output".equals(v.subtype)) {

					ports.add(name);
				}

			}

		}

		for (Vertex v : graph.getModules()) {

			if ("WIRE_NG_INTERNAL".equals(v.subtype)) {

				// virtual module created by an assignment statement

				Vertex in = graph.getNet(v, "IN");
				Vertex out = graph.getNet(v, "OUT");

				String str = String.format("assign %s = %s;", out, in);

				mods.add(str);

			} else {

				// actual (non-virtual) module

				ArrayList<String> portList = new ArrayList<String>();

				String form = ".%s(%s)";

				for (Vertex s : graph.getSources(v)) {

					portList.add(String.format(form, graph.getPinName(s, v), s.name));
				}

				for (Vertex d : graph.getDestinations(v)) {

					portList.add(String.format(form, graph.getPinName(v, d), d.name));
				}

				String str = String.format("%s %s (%s);", v.subtype, v.name, sortjoin(portList, ", "));

				mods.add(str);

			}
		}

		String header = String.format("module %s (%s);", graph.getName(), graph.getPortList());

		for (Range r : nets.values()) {

			String str;

			if (r.min == 0 && r.max == 0) {

				str = String.format("%s %s;", r.type, r.name);

			} else {

				str = String.format("%s [%d:%d] %s;", r.type, r.max, r.min, r.name);
			}

			wires.add(str);

		}

		Collections.sort(wires);

		Collections.sort(mods);

		StringBuilder strb = new StringBuilder(header);

		strb.append("\n");

		for (String str : wires)
			strb.append("\t").append(str).append("\n");

		for (String str : mods)
			strb.append("\t").append(str).append("\n");

		strb.append("endmodule");

		return strb.toString();

	}

	private static String sortjoin(ArrayList<String> list, String delim) {

		if (list.isEmpty()) {

			return "";

		} else if (list.size() == 1) {

			return list.get(0);

		} else {

			Collections.sort(list);

			StringBuilder sb = new StringBuilder(1024);

			sb.append(list.get(0));

			for (int i = 1; i < list.size(); i++) {

				sb.append(delim);

				sb.append(list.get(i));

			}

			return sb.toString();

		}

	}

}
