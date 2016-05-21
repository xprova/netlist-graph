package net.xprova.netlistgraph;

import net.xprova.dot.GraphDotFormatter;

public class NetlistGraphDotFormatter implements GraphDotFormatter<Vertex> {

	private final NetlistGraph parent;

	public NetlistGraphDotFormatter(NetlistGraph parent) {

		this.parent = parent;

	}

	@Override
	public String getEdgeLabel(Vertex s, Vertex d) {

		return parent.getPinName(s, d);

	}

	@Override
	public String getShape(Vertex v) {

		if (v.type == VertexType.NET) {

			if ("input".equals(v.subtype) || "output".equals(v.subtype)) {

				return "shape=circle, fixedsize=false, style=filled, color=plum, fontname=Arial";

			} else {

				return "shape=circle, fixedsize=false, fontname=Arial";

			}

		} else {

			boolean isFlop = parent.getNet(v, "CK") != null;

			if (isFlop) {

				boolean isVulnerable = parent.getNet(v, "V") != null;

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

}
