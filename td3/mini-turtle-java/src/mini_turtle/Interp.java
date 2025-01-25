package mini_turtle;

import java.util.HashMap;
import java.util.Iterator;

public class Interp implements Interpreter {

	static Turtle turtle = null;
	
	// les définitions de fonctions sont locales
	HashMap<String, Integer> vars = new HashMap<String, Integer>();

	// les définitions de fonctions sont globales
	static HashMap<String, Def> functions = new HashMap<String, Def>();

	@Override
	public int interp(Ecst e) {
		return e.c;
	}

	@Override
	public int interp(Ebinop e) {
		int v1 = e.e1.accept(this), v2 = e.e2.accept(this);
		switch (e.op) {
		case Badd: return v1 + v2;
		case Bsub: return v1 - v2;
		case Bmul: return v1 * v2;
		case Bdiv: return v1 / v2;
		}
		assert false;
		return 0; // inatteignable
	}

	@Override
	public int interp(Evar e) {
		if (!this.vars.containsKey(e.s))
			throw new Error("unbound variable " + e.s);
		return this.vars.get(e.s);
	}

	@Override
	public int interp(Eneg e) {
		return - e.e1.accept(this);
	}

	@Override
	public void interp(Sif s) {
		if (s.e.accept(this) != 0)
			s.s1.accept(this);
		else
			s.s2.accept(this);
	}

	@Override
	public void interp(Srepeat s) {
		int n = s.e.accept(this);
		for (int i = 1; i <= n; i++)
			s.s1.accept(this);
	}

	@Override
	public void interp(Sforward s) {
		turtle.forward(s.e.accept(this));
	}

	@Override
	public void interp(Sturnleft s) {
		turtle.turnleft(s.e.accept(this));
	}

	@Override
	public void interp(Sturnright s) {
		turtle.turnright(s.e.accept(this));
	}

	@Override
	public void interp(Spenup s) {
		turtle.penup();
	}

	@Override
	public void interp(Spendown s) {
		turtle.pendown();
	}

	@Override
	public void interp(Scolor s) {
		turtle.setColor(s.c);
	}

	@Override
	public void interp(Sblock s) {
		for (Stmt st: s.l)
			st.accept(this);
	}

	@Override
	public void interp(Scall s) {
		Def d = functions.get(s.f);
		if (d == null)
			throw new Error("unbound function " + s.f);
		if (s.l.size() != d.l.size())
			throw new Error("bad arity");
		Interp ctxf = new Interp();
		Iterator<String> it = d.l.iterator();
		for (Expr e1 : s.l)
			ctxf.vars.put(it.next(), e1.accept(this));
		d.s.accept(ctxf);
	}

}
