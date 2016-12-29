package net.xprova.netlistgraph;

public class Vertex implements Comparable<Vertex> {

	public String name, subtype;

	public VertexType type;

	public String arrayName = ""; // if vertex belongs to array

	public int arrayIndex = 0; // if vertex belongs to array

	public int arraySize = 0; // if vertex belongs to array

	public String tag = ""; // additional tag attribute (optional)

	public Vertex(String name, VertexType type, String subtype) {

		this.name = name;

		this.type = type;

		this.subtype = subtype;

	}

	public Vertex(Vertex other) {

		this.name = other.name;

		this.type = other.type;

		this.subtype = other.subtype;

		this.arrayName = other.arrayName;

		this.arrayIndex = other.arrayIndex;

		this.arraySize = other.arraySize;

		this.tag = other.tag;

	}

	public Vertex() {

		this.name = "n/a";

		this.type = VertexType.NET;

		this.subtype = "n/a";

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
