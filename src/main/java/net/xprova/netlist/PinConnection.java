package net.xprova.netlist;


public class PinConnection {
	
	public String net;
	
	public int bit;
	
	public PinDirection dir;
	
	public PinConnection(String net, int bit, PinDirection dir) {
		
		this.net = net;
		this.bit = bit;
		this.dir = dir;
	}

	public void print() {

		System.out.print(net + "[" + bit + "]");
		
	}
}