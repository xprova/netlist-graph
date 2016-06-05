package net.xprova.dot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

import net.xprova.graph.Graph;

public class GraphDotPrinter {

	private static final int maxLabelLength = 60;

	public static <V> void printGraph(String file, Graph <V> graph) throws FileNotFoundException, UnsupportedEncodingException {

		printGraph(file, graph, new GraphDotFormatter<V>(), graph.getVertices());

	}

	public static <V> void printGraph(String file, Graph<V> graph, GraphDotFormatter<V> formatter,
			HashSet<V> subVertices) throws FileNotFoundException, UnsupportedEncodingException {

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

			if (!formatter.getIgnoredVertex(v.toString())) {

				String vid = "n" + idCounter;

				String label = v.toString();

				if (label.length() > maxLabelLength)
					label = label.substring(0, maxLabelLength - 1);

				out.printf("\t %s \t [label=\"%s\"] [%s];\n", vid, label, formatter.getShape(v));

				uniqueIDs.put(v, idCounter);

				idCounter++;

			}

		}

		// print graph connections

		for (V v : graph.getVertices()) {

			String vid = "n" + uniqueIDs.get(v);

			for (V d : graph.getDestinations(v)) {

				if (graph.getVertices().contains(d)) {

					boolean ignoredSource = formatter.getIgnoredVertex(v.toString());

					boolean ignoredDestination = formatter.getIgnoredVertex(d.toString());

					if (!ignoredSource && !ignoredDestination) {

						String did = "n" + uniqueIDs.get(d);

						String edge = formatter.getEdgeLabel(v, d);

						if (edge == null) {

							out.printf("\t %s \t -> \t %s [fontName=Arial]\n", vid, did);

						} else if (!formatter.getIgnoredEdge(edge)) {

							out.printf("\t %s \t -> \t %s [label=%s, fontName=Arial]\n", vid, did, edge);

						}

					}

				}

			}
		}

		// out.println("subgraph x1 {color=blue; ln1 ln2 ln3}");

		out.println("}");

		out.close();

	}

}
