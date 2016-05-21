package net.xprova.dot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.xprova.graph.Graph;

public class GraphDotPrinter {

	private static final int maxLabelLength = 60;

	public static <V> void printGraph(String file, Graph<V> graph, GraphDotFormatter<V> formatter,
			HashSet<V> subVertices, String[] ignoreEdgesArr)
			throws FileNotFoundException, UnsupportedEncodingException {

		List<String> ignoreEdges = Arrays.asList(ignoreEdgesArr);

		PrintWriter out = new PrintWriter(file, "UTF-8");

		// print header

		out.println("digraph graph1{");

		// out.println("\tsplines=ortho;");

		out.println("\trankdir=LR;");

		// out.println("\tnodesep=1;");

		HashMap<V, Integer> uniqueIDs = new HashMap<V, Integer>();

		int idCounter = 1;

		// print graph nodes

		for (V v : subVertices) {

			String vid = "n" + idCounter;

			String label = v.toString();

			if (label.length() > maxLabelLength) {

				label = label.substring(0, maxLabelLength - 1);

			}

			out.printf("\t %s \t [label=\"%s\"] [%s];\n", vid, label, formatter.getShape(v));

			uniqueIDs.put(v, idCounter);

			idCounter++;

		}

		// print graph connections

		for (V v : graph.getVertices()) {

			String vid = "n" + uniqueIDs.get(v);

			for (V d : graph.getDestinations(v)) {

				if (graph.getVertices().contains(d)) {

					String did = "n" + uniqueIDs.get(d);

					String edge = formatter.getEdgeLabel(v, d);

					if (edge == null) {

						out.printf("\t %s \t -> \t %s [fontName=Arial]\n", vid, did);

					} else if (!ignoreEdges.contains(edge)) {

						out.printf("\t %s \t -> \t %s [label=%s, fontName=Arial]\n", vid, did, edge);

					}

				}

			}
		}

		// out.println("subgraph x1 {color=blue; ln1 ln2 ln3}");

		out.println("}");

		out.close();

	}

}
