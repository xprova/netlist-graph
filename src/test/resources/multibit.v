module main(clk, rst, x, y);

	input clk, rst, x;

	output y;

	NOT inv1(x, y);

endmodule