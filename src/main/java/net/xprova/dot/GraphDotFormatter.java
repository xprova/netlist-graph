package net.xprova.dot;

public interface GraphDotFormatter<V> {

	public String getEdgeLabel(V s, V d);

	public String getShape(V v);

}
