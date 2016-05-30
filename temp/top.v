module top (clk, rst, a, z);
    input clk, rst, a;
    output z;
    NOT u1 (z, a);
endmodule