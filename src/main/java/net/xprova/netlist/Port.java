package net.xprova.netlist;


public class Port extends Net {

	public PinDirection direction = PinDirection.UNKONWN;

	public Port(String id, PinDirection direction) {

		super(id);

		this.direction = direction;
	}

	@Override
	public void print() {

		int bits = Math.abs(end - start) + 1;

		System.out.println(id + ", \t" + bits + " bit(s), \t" + direction);
	}
}