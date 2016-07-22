package net.xprova.netlist;

public class Port extends Net {

	public PinDirection direction = PinDirection.UNKONWN;

	public Port(Port other) {

		super(other);

		this.direction = other.direction;

	}

	public Port(String id, PinDirection direction) {

		super(id);

		this.direction = direction;
	}

	@Override
	public String toString() {

		int c = this.getCount();

		return String.format("%s (%d %s - %s)", id, c, c == 1 ? "bit" : "bits", direction);
	}

}