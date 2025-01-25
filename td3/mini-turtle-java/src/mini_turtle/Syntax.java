package mini_turtle;

import java.util.LinkedList;

enum Binop {
	  Badd , Bsub , Bmul , Bdiv
	}

enum Color {
	Black, White, Red, Green, Blue
}

/* expressions */

abstract class Expr {
	abstract int accept(Interpreter v);

}

class Ecst extends Expr {
	final Integer c;
	Ecst(Integer c) {
		this.c = c;
	}
	@Override
	int accept(Interpreter v) { return v.interp(this); }
}

class Evar extends Expr {
	final String s;

	Evar(String s) {
		super();
		this.s = s;
	}

	@Override
	int accept(Interpreter v) { return v.interp(this); }
}

class Eneg extends Expr {
	final Expr e1;
	Eneg(Expr e1) {
		super();
		this.e1 = e1;
	}
	@Override
	int accept(Interpreter v) { return v.interp(this); }
}

class Ebinop extends Expr {
	final Binop op;
	final Expr e1, e2;
	Ebinop(Binop op, Expr e1, Expr e2) {
		super();
		this.op = op;
		this.e1 = e1;
		this.e2 = e2;
	}
	@Override
	int accept(Interpreter v) { return v.interp(this); }
}

/* instructions */

abstract class Stmt {
	abstract void accept(Interpreter v);
}

class Sif extends Stmt {
	final Expr e;
	final Stmt s1, s2;
	Sif(Expr e, Stmt s1, Stmt s2) {
		super();
		this.e = e;
		this.s1 = s1;
		this.s2 = s2;
	}
	@Override
	void accept(Interpreter v) { v.interp(this); }
}

class Sturnleft extends Stmt {
	final Expr e;
	Sturnleft(Expr e) {
		super();
		this.e = e;
	}
	@Override
	void accept(Interpreter v) { v.interp(this); }
}

class Sturnright extends Stmt {
	final Expr e;
	Sturnright(Expr e) {
		super();
		this.e = e;
	}
	@Override
	void accept(Interpreter v) { v.interp(this); }
}

class Sforward extends Stmt {
	final Expr e;
	Sforward(Expr e) {
		super();
		this.e = e;
	}
	@Override
	void accept(Interpreter v) { v.interp(this); }
}

class Srepeat extends Stmt {
	final Expr e;
	final Stmt s1;
	Srepeat(Expr e, Stmt s1) {
		super();
		this.e = e;
		this.s1 = s1;
	}
	@Override
	void accept(Interpreter v) { v.interp(this); }
}

class Spenup extends Stmt {
	@Override
	void accept(Interpreter v) { v.interp(this); }
}

class Spendown extends Stmt {
	@Override
	void accept(Interpreter v) { v.interp(this); }
}

class Sblock extends Stmt {
	final LinkedList<Stmt> l;
	Sblock() {
		this.l = new LinkedList<Stmt>();
	}
	Sblock(LinkedList<Stmt> l) {
		super();
		this.l = l;
	}
	@Override
	void accept(Interpreter v) { v.interp(this); }
}

class Scall extends Stmt {
	final String f;
	final LinkedList<Expr> l;
	Scall(String f, LinkedList<Expr> l) {
		super();
		this.f = f;
		this.l = l;
	}
	@Override
	void accept(Interpreter v) { v.interp(this); }
}

class Scolor extends Stmt {
	final Color c;
	Scolor(Color c) {
		this.c = c;
	}
	@Override
	void accept(Interpreter v) { v.interp(this); }
}

/* définition de fonction */

class Def {
	final String f;
	final LinkedList<String> l; // arguments formels
	final Stmt s;
	
	Def(String f, LinkedList<String> l, Stmt s) {
		super();
		this.f = f;
		this.l = l;
		this.s = s;
	}
}

class File {
	final LinkedList<Def> l;
	final Stmt s;
	File(LinkedList<Def> l, Stmt s) {
		super();
		this.l = l;
		this.s = s;
	}
}

/* un interprète (voir Interp.java) a l'interface suivante */

interface Interpreter {
	int interp(Ecst e);
	int interp(Ebinop e);
	int interp(Evar e);
	int interp(Eneg e);
	void interp(Sif s);
	void interp(Srepeat s);
	void interp(Sforward s);
	void interp(Sturnleft s);
	void interp(Sturnright s);
	void interp(Spenup s);
	void interp(Spendown s);
	void interp(Scolor s);
	void interp(Sblock s);
	void interp(Scall s);
}

