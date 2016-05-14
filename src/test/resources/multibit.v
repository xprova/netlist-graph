module counter(clk, rst, ena, count);

	input clk, rst, ena;

	output [3:0] count;

	NOT inv0 (ena, count[0]);
	NOT inv1 (ena, count[1]);
	NOT inv2 (ena, count[2]);
	NOT inv3 (ena, count[3]);

endmodule