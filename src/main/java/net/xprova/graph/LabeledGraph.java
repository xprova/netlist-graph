package net.xprova.graph;

// TODO: this is an incomplete implementation, must be either revised or removed altogether

public class LabeledGraph<V, E> extends Graph<V> {

	protected MultiMap<V, V, E> edges;

	public LabeledGraph() {

		super();

		edges = new MultiMap<V, V, E>();

	}

	public void addConnection(V source, V destination, E edge) throws Exception {

		super.addConnection(source, destination);

		edges.put(source, destination, edge);

	}

	@Override
	public boolean removeConnection(V source, V destination) {

		edges.remove(source, destination);

		return super.removeConnection(source, destination);

	}

	public E getEdge(V source, V destination) {

		return edges.get(source, destination);

	}

}
