package mini_turtle;

import java_cup.runtime.*;

public class Main {

	public static void main(String[] args) throws Exception {
		String file = args.length > 0 ? args[0] : "test.logo";
		java.io.Reader reader = new java.io.FileReader(file);
		Lexer lexer = new Lexer(reader);
		parser parser = new MyParser(file, lexer);
		File f = (File) parser.parse().value;
		for (Def d: f.l) Interp.functions.put(d.f, d);
		Turtle turtle = new Turtle();
		Interp.turtle = turtle;
		try {
			f.s.accept(new Interp());
		} catch (Error e) {
			System.out.println("error: " + e.toString());
			System.exit(1);
		}
	}
	
}

class MyParser extends parser {
	final private String file;
	MyParser(String file, Scanner scanner) {
		super(scanner);
		this.file = file;
	}
	public void report_error(String message, Object info) {
	}
	public void report_fatal_error(String message, Object info) {
		int line = 0, column = 0;
		if (info instanceof Symbol) {
			Symbol symbol = (Symbol) info;
			line = symbol.left;
			column = symbol.right;
		}
		System.err.println(this.file + ":" + line + ":" + column + ": syntax error");
		System.exit(1);
	}
}

