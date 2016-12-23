package net.xprova.netlistgraph;

import net.xprova.dot.GraphDotFormatter;

public class NetlistGraphDotFormatter extends GraphDotFormatter<Vertex> {

	private final NetlistGraph parent;

	private final boolean useModuleTypes;

	@Override
	public String getEdgeLabel(Vertex s, Vertex d) {

		return parent.getPinName(s, d);

	}

	public NetlistGraphDotFormatter(NetlistGraph parent, boolean useModuleTypes) {

		super();

		this.parent = parent;

		this.useModuleTypes = useModuleTypes;

	}

	@Override
	public String getVertexLabel(Vertex s) {

		boolean useSubType = s.type == VertexType.MODULE && useModuleTypes;

		return (useSubType ? s.subtype : s).toString();

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

			// TODO: replace this heuristic with better way of determining if
			// vertex is a flip-flop

			boolean isFlop = (parent.getNet(v, "CK") != null) || (v.subtype.contains("DFF")) ;

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
