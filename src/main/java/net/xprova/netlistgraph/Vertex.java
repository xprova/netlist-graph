package net.xprova.netlistgraph;

public class Vertex implements Comparable<Vertex>{

	public String name, subtype;

	public VertexType type;

	public Vertex(String name, VertexType type, String subtype) {

		this.name = name;

		this.type = type;

		this.subtype = subtype;

	}

	public Vertex(Vertex v) {

		this.name = v.name;

		this.type = v.type;

		this.subtype = v.subtype;

	}

	public Vertex() {

		this.name  = "n/a";

		this.type  = VertexType.NET;

		this.subtype  = "n/a";

	}

	@Override
	public String toString() {

		return name;

	}

	@Override
	public int compareTo(Vertex other) {

		return name.compareTo(other.name);


	}

}
