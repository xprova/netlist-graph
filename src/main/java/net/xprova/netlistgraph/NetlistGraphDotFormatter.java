package net.xprova.netlistgraph;

import net.xprova.dot.GraphDotFormatter;

public class NetlistGraphDotFormatter extends GraphDotFormatter<Vertex> {

	private final NetlistGraph parent;

	@Override
	public String getEdgeLabel(Vertex s, Vertex d) {

		return parent.getPinName(s, d);

	}

	public NetlistGraphDotFormatter(NetlistGraph parent) {

		super();

		this.parent = parent;

	}

	@Override
	public String getShape(Vertex v) {

		if (v.type == VertexType.NET) {

			if ("input".equals(v.subtype) || "output".equals(v.subtype)) {

				return "shape=circle, fixedsize=false, style=\"filled, solid\", fillcolor=plum, color=black, fontname=Arial";

			} else {

				return "shape=circle, fixedsize=false, fontname=Arial";

			}

		} else {

			boolean isFlop = parent.getNet(v, "CK") != null;

			boolean isBlock = v.subtype.equals("BLOCK");

			if (isBlock) {

				return "shape=square, fixedsize=false, style=\"filled, solid\", fillcolor=white, color=black, fontname=Arial";

			} else if (isFlop) {

				boolean isVulnerable = parent.getNet(v, "V") != null;

				if (isVulnerable) {

					return "shape=box, fixedsize=false, style=\"filled, solid\", fillcolor=indianred1, color=black, fontname=Arial";

				} else {

					return "shape=box, fixedsize=false, style=\"filled, solid\", fillcolor=limegreen, color=black, fontname=Arial";

				}

			} else {

				return "shape=cds, fixedsize=false, style=\"filled, solid\", fillcolor=khaki1, color=black, fontname=Arial, width=1, height=0.75";

				// return "shape=box, fixedsize=false";
			}

		}
	}

}
