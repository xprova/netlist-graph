package net.xprova.netlistgraph;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

			}

		}

		for (Vertex v : graph.getModules()) {

			if ("WIRE_NG_INTERNAL".equals(v.subtype)) {

				// virtual module created by an assignment statement

				Vertex in = graph.getNet(v, "IN");
				Vertex out = graph.getNet(v, "OUT");

				String str = String.format("assign %s = %s;", out.name, getEscaped(in.name));

				mods.add(str);

			} else {

				// actual (non-virtual) module

				ArrayList<String> portList = new ArrayList<String>();

				String form = ".%s(%s)";

				// Generating port connections is nearly straightforward.
				// The only thing to pay a bit of attention to is the generation
				// of array connections, for example:
				//
				// mod1 u1 (.a(x), .b(y)
				// where x and y are say 2-bit arrays
				//
				// in this case this u1 has four connections:
				// - input from x[0] through pin a[0]
				// - input from x[1] through pin a[1]
				// - output to y[0] through pin b[0]
				// - output to y[1] through pin b[1]

				// as a first step, we build a TreeMap of <pinName, Vertex>
				// containing individual array pins (e.g. x[0] and a[0])

				TreeMap<String, Vertex> portMap = new TreeMap<String, Vertex>();

				for (Vertex s : graph.getSources(v)) {

					portMap.put(graph.getPinName(s, v), s);

				}

				for (Vertex d : graph.getDestinations(v)) {

					portMap.put(graph.getPinName(v, d), d);

				}

				// next we "group" array bits by building another TreeMap
				// similar to portMap but where each entry is <pinName,
				// VertexName> and pinName/VertexName are NOT indexed, for
				// example: <x, a>

				TreeMap<String, String> groupPortMap = new TreeMap<String, String>();

				HashSet<String> groupedPorts = new HashSet<String>();

				for (Entry<String, Vertex> entry : portMap.entrySet()) {

					String portName = entry.getKey();

					Vertex net = entry.getValue();

					if (!groupedPorts.contains(net.arrayName)) {

						groupPortMap.put(getStripped(portName), net.arrayName);

						groupedPorts.add(net.arrayName);

					}

					// as an additional check, verify that net and port
					// index are the same, i.e. x[n] connected through a[n]

					if (net.arraySize > 1) {

						String strIndex = "[" + net.arrayIndex + "]";

						if (!portName.contains(strIndex))
							throw new Exception("net and pin indices are not equal");

					}

				}

				for (Entry<String, String> entry : groupPortMap.entrySet()) {

					String portName = getEscaped(entry.getKey());

					String netName = getEscaped(entry.getValue());

					portList.add(String.format(form, portName, netName));

				}

				String str = String.format("%s %s (%s);", v.subtype, getEscaped(v.name), sortjoin(portList, ", "));

				mods.add(str);

			}
		}

		String header = String.format("module %s (%s);", graph.getName(), graph.getPortList());

		for (Range r : nets.values()) {

			String str;

			if (r.min == 0 && r.max == 0) {

				str = String.format("%s %s;", r.type, getEscaped(r.name));

			} else {

				str = String.format("%s [%d:%d] %s;", r.type, r.max, r.min, getEscaped(r.name));
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

	private static String getEscaped(String id) {

		return id.startsWith("\\") ? id + " " : id;

	}

	private static String getStripped(String id) {

		// returns identifier given an indexed identifier
		// e.g. when passed `x[1]` returns `x`

		// also handles the special case of escaped identifiers
		// e.g. `\x [1]` -> `\x`

		String regex = "(\\S+)\\s*\\[\\d+\\]";

		Matcher m1 = Pattern.compile(regex).matcher(id);

		return m1.find() ? m1.group(1) : id;

	}

}
