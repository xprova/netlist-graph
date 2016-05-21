package net.xprova.dot;

import java.util.Arrays;
import java.util.HashSet;

public class GraphDotFormatter<V> {

	private HashSet<String> ignoredVertices, ignoredEdges;

	public GraphDotFormatter() {

		ignoredVertices = new HashSet<String>();

		ignoredEdges = new HashSet<String>();

	}

	public boolean getIgnoredVertex(String vName) {

		return ignoredVertices.contains(vName);

	}

	public boolean getIgnoredEdge(String eName) {

		return ignoredEdges.contains(eName);

	}

	public void setIgnoredEdges(String[] ignoredEdgesArr) {

		ignoredEdges.clear();

		ignoredEdges.addAll(Arrays.asList(ignoredEdgesArr));

	}

	public void setIgnoredVertices(String[] ignoredVerticesArr) {

		ignoredVertices.clear();

		ignoredVertices.addAll(Arrays.asList(ignoredVerticesArr));

	}



	public String getEdgeLabel(V s, V d) {

		return "";

	}

	public String getShape(V v) {

		return "shape=box, fixedsize=false, fontname=Arial";

	}

}
