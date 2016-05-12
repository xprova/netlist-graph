package net.xprova.netlistgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.xprova.graph.Graph;
import net.xprova.graph.MultiMap;
import net.xprova.netlist.Module;
import net.xprova.netlist.Net;
import net.xprova.netlist.Netlist;
import net.xprova.netlist.PinConnection;
import net.xprova.netlist.PinDirection;
import net.xprova.netlist.Port;

public class NetlistGraph extends Graph<Vertex> {

	private HashSet<Vertex> modules, nets, inputs, outputs;

	private MultiMap<Vertex, Vertex, String> pins; // pins are graph edges

	// modConnections: keys are module and port, value is connected net

	private MultiMap<Vertex, String, Vertex> modConnections;

	private String port_list;

	private static final String clkPort = "CK";

	public void addPort(String newPort) {

		// TODO: new to find a better way to manage port list
		// ideally it should be extracted automatically

		port_list = port_list.concat(", ").concat(newPort);
	}

	public void printStats(String title) {

		// TODO: this function needs to be moved elsewhere

		int modCount = modules.size();

		int flopCount = getFlops(this).size();

		int gateCount = modCount - flopCount;

		System.out.printf("%-20s : %s\n", "Design", title);

		System.out.printf("%-20s : %d\n", "Modules", modCount);

		System.out.printf("%-20s : %d\n", "Flip-flops", flopCount);

		System.out.printf("%-20s : %d\n", "Gates", gateCount);

		System.out.println("");

	}

	private static HashSet<Vertex> getFlops(NetlistGraph graph) {

		HashSet<Vertex> flops = new HashSet<Vertex>();

		for (Vertex v : graph.getModules()) {

			if (graph.getNet(v, clkPort) != null) {

				flops.add(v);

			}
		}

		return flops;

	}

	public NetlistGraph() {

		super();

		modules = new HashSet<Vertex>();

		nets = new HashSet<Vertex>();

		inputs = new HashSet<Vertex>();

		outputs = new HashSet<Vertex>();

		pins = new MultiMap<Vertex, Vertex, String>();

		modConnections = new MultiMap<Vertex, String, Vertex>();

	}

	public NetlistGraph(Netlist netlist) throws Exception {

		this();

		// netMap: used to access Net vertices by their String name

		HashMap<String, Vertex> netMap = new HashMap<String, Vertex>();

		port_list = netlist.port_list;

		// first, add nets

		for (Net net : netlist.nets.values()) {

			for (int i = net.getLower(); i <= net.getHigher(); i++) {

				String vName = getNetVertexName(net.id, i, net.getCount());

				String type = "wire";

				Port p = netlist.ports.get(net.id);

				if (p != null) {

					if (p.direction == PinDirection.IN) {

						type = "input";

					} else if (p.direction == PinDirection.OUT) {

						type = "output";

					} else {

						throw new ConnectivityException(
								"cannot determine direction of port " + net.id);

					}
				}

				Vertex v = new Vertex(vName, VertexType.NET, type);

				addVertex(v);

				netMap.put(vName, v);
			}

		}

		// now add modules

		// map1 is used to map each Vertex to its corresponding Module object

		HashMap<Vertex, Module> map1 = new HashMap<Vertex, Module>();

		for (Module module : netlist.modules.values()) {

			Vertex v = new Vertex(module.id, VertexType.MODULE, module.type);

			addVertex(v);

			map1.put(v, module);
		}

		// connections

		for (Vertex module : modules) {

			Module mod = map1.get(module);

			Set<String> ports = mod.connections.keySet();

			for (String port : ports) {

				PinConnection con = mod.connections.get(port);

				int netBits = netlist.nets.get(con.net).getCount();

				String netName = getNetVertexName(con.net, con.bit, netBits);

				Vertex netV = netMap.get(netName);

				if (con.dir == PinDirection.IN) {

					addConnection(netV, module);

					pins.put(netV, module, port);

				} else if (con.dir == PinDirection.OUT) {

					addConnection(module, netV);

					pins.put(module, netV, port);

				}

				modConnections.put(module, port, netV);
			}
		}

		// inputs and outputs

		for (Port port : netlist.ports.values()) {

			for (int i = port.getLower(); i <= port.getHigher(); i++) {

				String netName = getNetVertexName(port.id, i, port.getCount());

				Vertex v = netMap.get(netName);

				if (port.direction == PinDirection.IN) {

					inputs.add(v);

				} else {

					outputs.add(v);
				}
			}

		}

		checkDrivers();

	}

	private void checkDrivers() throws ConnectivityException {

		// checks graph for nets with no or multiple drivers

		final String msg_1 = "net <%s> has no drivers";
		final String msg_2 = "net <%s> has multiple drivers";

		// add special nets

		for (Vertex net : nets) {

			int drivers = sources.get(net).size();

			if (drivers == 0 && !inputs.contains(net)) {

				String msg = String.format(msg_1, net.name);

				throw new ConnectivityException(msg);

			} else if (drivers > 1) {

				String msg = String.format(msg_2, net.name);

				throw new ConnectivityException(msg);
			}
		}

	}

	private String getNetVertexName(String id, int bit, int totalBits) {

		return totalBits > 1 ? id + "[" + bit + "]" : id;
	}

	public HashSet<Vertex> getNets() {

		// returns a shallow copy of nets

		return new HashSet<Vertex>(nets);
	}

	public HashSet<Vertex> getIONets() {

		// returns input and output net vertices

		HashSet<Vertex> ios = new HashSet<Vertex>();

		for (Vertex f : nets) {

			if ("input".equals(f.subtype) || "output".equals(f.subtype)) {

				ios.add(f);

			}
		}

		return ios;
	}

	public HashSet<Vertex> getModules() {

		// returns a shallow copy of modules

		return new HashSet<Vertex>(modules);
	}

	public HashSet<Vertex> getModulesByType(String type) {

		HashSet<Vertex> result = new HashSet<Vertex>();

		for (Vertex v : modules) {

			if (v.subtype.equals(type))
				result.add(v);

		}

		return result;

	}

	public String getPinName(Vertex v1, Vertex v2) {

		return pins.get(v1, v2);
	}

	public Vertex getNet(Vertex module, String port) {

		return modConnections.get(module, port);

	}

	@Override
	public boolean removeVertex(Vertex v) {

		if (super.contains(v)) {

			modules.remove(v);

			nets.remove(v);

			inputs.remove(v);

			outputs.remove(v);

			for (Vertex source : sources.get(v)) {

				String port = pins.get(source, v);

				modConnections.remove(v, port);

				pins.remove(source, v);

			}

			for (Vertex destination : destinations.get(v)) {

				String port = pins.get(v, destination);

				modConnections.remove(v, port);

				pins.remove(v, destination);

			}

			super.removeVertex(v);

			return true;

		} else {

			return false;

		}

	}

	@Override
	public boolean addVertex(Vertex v) {

		if (super.addVertex(v)) {

			if (v.type == VertexType.MODULE) {

				modules.add(v);

			} else if (v.type == VertexType.NET) {

				nets.add(v);

			}

			return true;

		} else {

			return false;

		}

	}

	public boolean removeConnection(Vertex source, Vertex destination) {

		if (super.removeConnection(source, destination)) {

			String port = pins.get(source, destination);

			pins.remove(source, destination);

			if (source.type == VertexType.MODULE) {

				modConnections.remove(source, port);
			}

			if (destination.type == VertexType.MODULE) {

				modConnections.remove(destination, port);
			}

			return true;

		} else {

			return false;
		}

	}

	public boolean addConnection(Vertex source, Vertex destination, String port)
			throws Exception {

		if (destination.type == VertexType.NET
				&& !getSources(destination).isEmpty()) {

			throw new Exception("net <" + destination
					+ "> already has a driver " + getSources(destination)
					+ ", cannot add source " + source);

		}

		super.addConnection(source, destination);

		pins.put(source, destination, port);

		if (source.type == VertexType.MODULE) {

			modConnections.put(source, port, destination);
		}

		if (destination.type == VertexType.MODULE) {

			modConnections.put(destination, port, source);
		}

		return true;

	}

	public Vertex getVertex(String name) {

		for (Vertex v : vertices) {

			if (v.name.equals(name))
				return v;
		}

		return null;

	}

	@Override
	protected String getEdgeLabel(Vertex s, Vertex d) {

		return getPinName(s, d);

	}



	@Override
	protected String getShape(Vertex v) {

		if (v.type == VertexType.NET) {

			if ("input".equals(v.subtype) || "output".equals(v.subtype)) {

				return "shape=circle, fixedsize=false, style=filled, color=plum, fontname=Arial";

			} else {

				return "shape=circle, fixedsize=false, fontname=Arial";

			}

		} else {

			boolean isFlop = getNet(v, "CK") != null;

			if (isFlop) {

				boolean isVulnerable = getNet(v, "V") != null;

				if (isVulnerable) {

					return "shape=box, fixedsize=false, style=filled, color=indianred1, fontname=Arial";

				} else {

					return "shape=box, fixedsize=false, style=filled, color=limegreen, fontname=Arial";

				}

			} else {

				return "shape=cds, fixedsize=false, style=filled, color=khaki1, fontname=Arial";

				// return "shape=box, fixedsize=false";
			}

		}
	}

	public String getPortList() {

		return port_list;
	}

	public Graph<Vertex> getModuleGraph() {

		return reduce(modules);
	}

	public Graph<Vertex> getNetGraph() {

		return reduce(nets);
	}

	public Graph<Vertex> getModuleTypeGraph(String type) {

		HashSet<Vertex> cells = new HashSet<Vertex>();

		for (Vertex v : modules) {

			if (type.equals(v.subtype))
				cells.add(v);

		}

		return reduce(cells);
	}

	public Vertex getSourceModule(Vertex net) throws Exception {

		if (net.type != VertexType.NET) {

			throw new Exception("not a net");
		}

		return getSources(net).iterator().next();

	}

	@Override
	public NetlistGraph getSubGraph(HashSet<Vertex> subVertices)
			throws Exception {

		NetlistGraph subgraph = new NetlistGraph();

		for (Vertex vertex : subVertices) {

			subgraph.addVertex(vertex);
		}

		for (Vertex source : subVertices) {

			HashSet<Vertex> destinations = getDestinations(source);

			destinations.retainAll(subVertices);

			for (Vertex d : destinations) {

				subgraph.addVertex(d);

				if (isConnected(source, d)) {

					String port = this.getEdgeLabel(source, d);

					subgraph.addConnection(source, d, port);

				}

			}

		}

		return subgraph;

	}

}
