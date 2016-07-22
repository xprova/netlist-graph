package net.xprova.netlist;

import java.util.ArrayList;

public class Net {

	public String id;

	public int start = 0, end = 0;

	public Net(Net other) {

		this.id = other.id;
		this.start = other.start;
		this.end = other.end;

	}

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

	@Override
	public String toString() {

		return id + ", \t" + getCount() + " bit(s)";
	}

	public ArrayList<Integer> getBits() {

		ArrayList<Integer> result = new ArrayList<Integer>();

		if (start < end) {

			// wire [0:3] x;

			for (int i = start; i <= end; i++)
				result.add(i);

		} else {

			// wire [3:0] x;

			for (int i = start; i >= end; i--)
				result.add(i);

		}

		return result;

	}

}
