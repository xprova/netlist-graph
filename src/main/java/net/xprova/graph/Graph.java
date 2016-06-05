package net.xprova.graph;

import java.util.HashMap;
import java.util.HashSet;

public class Graph<V> {

	protected HashSet<V> vertices;

	protected HashMap<V, HashSet<V>> sources, destinations;

	public Graph() {

		vertices = new HashSet<V>();

		sources = new HashMap<V, HashSet<V>>();

		destinations = new HashMap<V, HashSet<V>>();

	}

	public boolean addVertex(V vertex) {

		if (vertices.contains(vertex)) {

			return false;

		} else {

			vertices.add(vertex);

			sources.put(vertex, new HashSet<V>());

			destinations.put(vertex, new HashSet<V>());

			return true;

		}

	}

	public boolean removeVertex(V v) {

		if (vertices.contains(v)) {

			vertices.remove(v);

			for (V source : sources.get(v)) {

				destinations.get(source).remove(v);
			}

			for (V destination : destinations.get(v)) {

				sources.get(destination).remove(v);
			}

			sources.remove(v);

			destinations.remove(v);

			return true;

		} else {

			return false;
		}

	}

	public boolean isConnected(V source, V destination) {

		return destinations.get(source).contains(destination);

	}

	public void addConnection(V source, V destination) throws Exception {

		if (!vertices.contains(source)) {

			throw new Exception("graph does not contain source vertex <" + source + ">");
		}

		if (!vertices.contains(destination)) {

			throw new Exception("graph does not contain destination vertex <" + destination + ">");
		}

		sources.get(destination).add(source);

		destinations.get(source).add(destination);

	}

	public boolean removeConnection(V source, V destination) {

		if (vertices.contains(source) && vertices.contains(destination)) {

			sources.get(destination).remove(source);

			destinations.get(source).remove(destination);

			return true;

		} else {

			return false;

		}

	}

	public V getVertex(String name) {

		for (V vertex : vertices) {

			if (vertex.toString().equals(name)) {

				return vertex;
			}

		}

		return null;

	}

	public HashSet<V> getVertices() {

		// returns a shallow copy of vertices

		return new HashSet<V>(vertices);
	}

	public HashSet<V> getDestinations(V v) {

		return new HashSet<V>(destinations.get(v));
	}

	public HashSet<V> getSources(V v) {

		return new HashSet<V>(sources.get(v));
	}

	public boolean contains(V vertex) {

		return vertices.contains(vertex);

	}

	public void combineVertices(V newV, HashSet<V> oldVs) throws Exception {

		// combines vertices in oldVs into a new vertex newV

		addVertex(newV);

		// duplicate connections

		for (V oldV : oldVs) {

			for (V src : getSources(oldV))
				addConnection(src, newV);

			for (V dst : getDestinations(oldV))
				addConnection(newV, dst);

		}

		// remove oldVs from graph

		for (V oldV : oldVs)
			removeVertex(oldV);

	}

	public HashSet<V> bfs(V start, int levels, boolean reverse) {

		HashSet<V> startSet = new HashSet<V>();

		startSet.add(start);

		return bfs(startSet, levels, reverse);

	}

	public HashSet<V> bfs(HashSet<V> start, int levels, boolean reverse) {

		// does a breadth-first search from start for a certain number of levels

		// if reverse is true then the search is performed assumed reverse edge
		// directions

		HashSet<V> visited = new HashSet<V>();

		HashSet<V> current = new HashSet<V>();

		current.addAll(start);

		for (int i = 0; i < levels && !current.isEmpty(); i++) {

			HashSet<V> toVisit = new HashSet<V>();

			for (V node : current) {

				HashSet<V> nodeDestinations = reverse ? getSources(node) : getDestinations(node);

				nodeDestinations.removeAll(visited);

				toVisit.addAll(nodeDestinations);

			}

			visited.addAll(toVisit);

			current = toVisit;

		}

		return visited;

	}

	public HashSet<V> bfs(HashSet<V> start, HashSet<V> terminals, boolean reverse) {

		// does a breadth-first search from start and ending at terminals
		// returns hashset of visited nodes excluding start and terminals

		// if reverse is true then the search is performed assumed reverse edge
		// directions

		HashSet<V> visited = new HashSet<V>();

		HashSet<V> current = new HashSet<V>();

		current.addAll(start);

		while (!current.isEmpty()) {

			HashSet<V> toVisit = new HashSet<V>();

			for (V node : current) {

				HashSet<V> nodeDestinations = reverse ? getSources(node) : getDestinations(node);

				nodeDestinations.removeAll(visited);

				toVisit.addAll(nodeDestinations);

			}

			toVisit.removeAll(terminals);

			visited.addAll(toVisit);

			current = toVisit;

		}

		return visited;

	}

	public HashSet<V> bfs(V start, HashSet<V> terminals, boolean reverse) {

		// does a breadth-first search from start and ending at terminals
		// returns hashset of visited nodes excluding start and terminals

		// if reverse is true then the search is performed assumed reverse edge
		// directions

		HashSet<V> visited = new HashSet<V>();

		HashSet<V> current = new HashSet<V>();

		current.add(start);

		while (!current.isEmpty()) {

			HashSet<V> toVisit = new HashSet<V>();

			for (V node : current) {

				HashSet<V> nodeDestinations = reverse ? getSources(node) : getDestinations(node);

				nodeDestinations.removeAll(visited);

				toVisit.addAll(nodeDestinations);

			}

			toVisit.removeAll(terminals);

			visited.addAll(toVisit);

			current = toVisit;

		}

		return visited;

	}

	public Graph<V> getSubGraph(HashSet<V> subVertices) throws Exception {

		Graph<V> subgraph = new Graph<V>();

		for (V vertex : subVertices) {

			subgraph.addVertex(vertex);
		}

		for (V source : subVertices) {

			for (V destination : getDestinations(source)) {

				if (isConnected(source, destination)) {

					subgraph.addConnection(source, destination);

				}

			}

		}

		return subgraph;

	}

	public Graph<V> reduce(HashSet<V> subVertices) {

		// returns a sub-graph composed of subVertices
		//
		// connections are made between any two reduced graph vertices v1 and v2
		// if the parent graph contains a path from v1 and v2 containing
		// vertices not in subVertices

		Graph<V> newGraph = new Graph<V>();

		for (V v : subVertices) {

			newGraph.addVertex(v);

		}

		// do a breadth first search for each node, terminating at any node in s

		HashSet<V> toExplore = new HashSet<V>();

		HashSet<V> visited = new HashSet<V>();

		for (V root : subVertices) {

			toExplore.clear();

			visited.clear();

			toExplore.add(root);

			while (!toExplore.isEmpty()) {

				HashSet<V> discovered = new HashSet<V>();

				for (V source : toExplore) {

					HashSet<V> new_destinations = getDestinations(source);

					new_destinations.removeAll(visited);

					HashSet<V> temp = new HashSet<V>(new_destinations);

					temp.retainAll(subVertices);

					for (V ffd : temp) {

						try {

							newGraph.addConnection(root, ffd);

						} catch (Exception e) {

							e.printStackTrace();
						}

					}

					new_destinations.removeAll(subVertices);

					discovered.addAll(new_destinations);

				}

				toExplore = discovered;

			}

		}

		return newGraph;

	}

}
