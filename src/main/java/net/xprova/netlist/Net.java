package net.xprova.netlist;

public class Net {

	public String id;

	public int start = 0, end = 0;

	public Net(String id) {

		this.id = id;

	}

	public boolean inRange(int bit) {

		return (bit >= getLower() && bit <= getHigher());
	}

	public int getLower() {
		
		return Math.min(start, end);
	}

	public int getHigher() {
		
		return Math.max(start, end);
	}
	
	public int getCount() {
		
		return getHigher() - getLower() + 1;
	}

	public void print() {

		System.out.println(id + ", \t" + getCount() + " bit(s)");
	}

}
