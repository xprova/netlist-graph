### NetlistGraph

This is a Java library for parsing and returning graph representations of
gate-level Verilog netlists. It's being developed primarily as a backbone for
[Xprova](https://github.com/xprova/xprova) but can also be used to jump start
the development of any EDA tool that involves gate-level processing of Verilog
netlists.

## Cookbook Examples

To load a Verilog netlist, first prepare and load a _component library_. This
is just a Verilog file that contains skeleton module definitions with module
names, ports and port directions such as:

```
module DFF(clk, reset, d, q);
	input clk, reset, d;
	output q;
endmodule

module AND(a, b, y);
	input a, b;
	output y;
endmodule

module NOT(a, y);
	input a;
	output y;
endmodule
```

The following call loads the content of the file above (call it `simple.lib`):

```
ArrayList<Netlist> libModules = VerilogParser.parseFile('./simple.lib', new GateLibrary());
```

Each of `DFF`, `AND` and `NOT` are loaded as individual `Netlist` objects and
can now be used to prepare a `GateLibrary` object using:

```
GateLibrary simpleLib = new GateLibrary(libModules);
```

After initializing `simpleLib` you can load any gate-level Verilog netlist
consisting of the libray modules such as:

```
module main(clk, rst, x, y);

	input clk, rst, x;

	output y;

	NOT inv1(x, y);

endmodule
```

which can be loaded using:

```
ArrayList<Netlist> netListArr = VerilogParser.parseFile('minimal.v', simpleLib);
```
