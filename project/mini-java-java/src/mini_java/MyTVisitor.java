package mini_java;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

public class MyTVisitor implements TVisitor {

    static TDClass tdClass;

    protected static int msgCnt = 0, ifCnt = 0, binopCnt = 0;
    protected static String toPrint;
    protected static LinkedList<String> argumentRegisters;
    protected static HashMap<TSif, Integer> localIfCnt = new HashMap<TSif, Integer>();
    protected static HashMap<TEbinop, Integer> localBinopCnt = new HashMap<TEbinop, Integer>();
    static boolean startingMethodBlock;

    public MyTVisitor(){
        argumentRegisters = new LinkedList<String>();
        argumentRegisters.push("%rdi");
        argumentRegisters.push("%rsi");
        argumentRegisters.push("%rdx");
        argumentRegisters.push("%rcx");
        argumentRegisters.push("%r8");
        argumentRegisters.push("%r9");
    }

    public void setTDClass(TDClass tdClass) {
        MyTVisitor.tdClass = tdClass;
    }

    @Override
    public void visit(TTvoid t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' TTVOID");
    }

    @Override
    public void visit(TTnull t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' TTNULL");
    }

    @Override
    public void visit(TTboolean t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' TTBOOL");
    }

    @Override
    public void visit(TTint t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' TTINT");
    }

    @Override
    public void visit(TTclass t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' TTCLASS");
    }

    @Override
    public void visit(Cbool c) {
        boolean b = c.b;
        if(b){
            Compile.ret.movq(1, "%rax");
        }else{
            Compile.ret.movq(0, "%rax");
        }
    }

    @Override
    public void visit(Cstring c) {//let's place string in data section and place pointer value in register %rax
        msgCnt += 1;
        Compile.ret.dlabel("message" + msgCnt);
        toPrint = c.s;
        Compile.ret.string(toPrint);
    }

    @Override
    public void visit(Cint c) {
        long x = c.i;
        Compile.ret.movq(x, "%rax");
    }

    @Override
    public void visit(TEcst e) { // here let's put constant value (or pointer to it) in register %rax
        Constant c = e.c;
        c.accept(this);
    }

    @Override
    public void visit(TEbinop e) {
        binopCnt += 1;
        localBinopCnt.put(e, binopCnt);
        
        Binop op = e.op;
        TExpr e1 = e.e1;
        e1.accept(this); //after call, result of expression is on %rax
        Compile.ret.pushq("%rax");//let's put it on the stack and move on
        Compile.ret.jmp("skipE2_" + localBinopCnt.get(e));
        
        Compile.ret.label("E2_" + localBinopCnt.get(e));
        TExpr e2 = e.e2;
        e2.accept(this);
        Compile.ret.pushq("%rax");//let's put it on the stack and move on
        Compile.ret.jmp("afterE2_" + localBinopCnt.get(e));
        Compile.ret.label("skipE2_" + localBinopCnt.get(e));

        switch(op){
            case Badd:
                System.out.println("Add");
                break;
            case Bsub:
                System.out.println("Sub");
                break;
            case Bmul:
                System.out.println("Mul");
                break;
            case Bdiv:
                System.out.println("Div");//say we want to do a/b
                Compile.ret.jmp("E2_" + localBinopCnt.get(e)); //in this case we will need e2 all the time!!
                Compile.ret.label("afterE2_" + localBinopCnt.get(e));
                Compile.ret.popq("%r8"); //pop b from the stack to register %r8
                Compile.ret.popq("%rax"); //pop a from the stack to register %rax (implicit argument!!)
                Compile.ret.idivq("%r8"); //quotient is stored in %rax, remainder is stored in %rdx (result is in %rax, ok!!)
                break;
            case Bmod:
                System.out.println("Mod");
                break;
            case Beq:
                System.out.println("Eq"); // a==b
                Compile.ret.jmp("E2_" + localBinopCnt.get(e)); //in this case we will need e2 all the time!!
                Compile.ret.label("afterE2_" + localBinopCnt.get(e));
                Compile.ret.popq("%r8"); //pop b from the stack to register %r8
                Compile.ret.popq("%r9"); //pop a from the stack to register %r9
                Compile.ret.cmpq("%r8", "%r9");
                Compile.ret.sete("%al"); // set if equal
                Compile.ret.movzbq("%al", "%rax"); //put result in %rax!
                break;
            case Bneq:
                System.out.println("Neq");
                break;
            case Blt:
                System.out.println("Lt");
                break;
            case Ble:
                System.out.println("Le");
                break;
            case Bgt:
                System.out.println("Gt");
                break;
            case Bge:
                System.out.println("Ge");
                break;
            case Band:
                System.out.println("And");
                break;
            case Bor: // a||b (we will only jump to e2 to get b if a is false!!)
                Compile.ret.popq("%r9"); //pop a from the stack to register %r9
                Compile.ret.pushq("%r9"); //just trying to stay out of trouble, what if b erases content of r9 ?
                Compile.ret.testq("%r9", "%r9");
                Compile.ret.je("E2_" + localBinopCnt.get(e)); //if it's zero(false), let's get b!!
                Compile.ret.movq(0, "%r8");//if not, let's just put a dummy value in the stack (0 for Or)
                Compile.ret.pushq("%r8");
                Compile.ret.label("afterE2_" + localBinopCnt.get(e));
                Compile.ret.popq("%r9"); //pop a again
                Compile.ret.popq("%r8"); //pop b from the stack to register %r8
                Compile.ret.orq("%r8", "%r9");
                Compile.ret.movq("%r9", "%rax"); //put result in %rax!
                break;
            case Badd_s:
                System.out.println("Add_s");
                break;
        }
    }

    @Override
    public void visit(TEunop e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' UNOP");
    }

    @Override
    public void visit(TEthis e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' THIS");
    }

    @Override
    public void visit(TEnull e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' TENULL");
    }

    @Override
    public void visit(TEvar e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' VAR");
    }

    @Override
    public void visit(TEassignVar e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' TEASSIGNVAR");
    }

    @Override
    public void visit(TEattr e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' ATTR");
    }

    @Override
    public void visit(TEassignAttr e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' ASS_ATTR");
    }

    @Override
    public void visit(TEnew e) {
        Class_ cl = e.cl;
        LinkedList<TExpr> l = e.l;

        ListIterator<TExpr> it = l.listIterator();
        while(it.hasNext()){
            TExpr te = it.next();
            te.accept(this);
        }
        Compile.ret.call(cl.name + "_" + cl.name); //we call constructor
    }

    @Override
    public void visit(TEcall e) {
        TExpr te = e.e; //who called
        Method m = e.m; //what method
        LinkedList<TExpr> l = e.l; //what are the parameters
        ListIterator<TExpr> it = l.listIterator();

        if(m.name.equals("System_out_print") || m.name.equals("System_out_println")){
            TExpr aux = it.next(); // for print let's just assume a single argument
            aux.accept(this);

            Compile.ret.movq("$message"+msgCnt, "%rdi");
            Compile.ret.call("puts");
            Compile.ret.movq("$0", "%rax");
        }else{
            te.accept(this);
            ListIterator<String> itarg = argumentRegisters.listIterator();
            while(it.hasNext()){
                TExpr argument = it.next();
                argument.accept(this); //result of expression is in %rax!!
                if(!itarg.hasNext()){
                    throw new UnsupportedOperationException("Too many arguments in function call!! I can handle at most 6!");
                }
                String register = itarg.next();
                Compile.ret.movq("%rax", register);
            }
            Compile.ret.call(m.name); //assume return value is placed in %rax
            Compile.ret.popq("%rbp");//get return address
            Compile.ret.ret();
        }
    }

    @Override
    public void visit(TEcast e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' CAST");
    }

    @Override
    public void visit(TEinstanceof e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' INSTANCEOF");
    }

    @Override
    public void visit(TEprint e) {
        throw new UnsupportedOperationException("Unimplemented method 'visit' PRINT");
    }

    @Override
    public void visit(TSexpr s) {
        System.out.println("TSEXPR " + s);
        s.e.accept(this);
    }

    @Override
    public void visit(TSvar s) {
        Variable v = s.v;
        TExpr e = s.e;
        e.accept(this);
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("TSVAR");
    }

    @Override
    public void visit(TSif s) {
        ifCnt += 1; //we will use this counter in labels for s1 and s2 blocks!
        localIfCnt.put(s, ifCnt); //we use a local copy to keep present value of ifCnt
        TExpr e = s.e;
        TStmt s1 = s.s1;
        TStmt s2 = s.s2;
        
        e.accept(this); //expression result is in %rax!
        Compile.ret.testq("%rax", "%rax");
        Compile.ret.je("elseBlock" + localIfCnt.get(s)); //if rax is zero (false) jump to else block!

        s1.accept(this);// if rax is one (true) continue to if block
        Compile.ret.jmp("endIf" + localIfCnt.get(s));//after ifBlock, jump to end to avoid else block
        
        Compile.ret.label("elseBlock" + localIfCnt.get(s));
        s2.accept(this);
        Compile.ret.label("endIf" + localIfCnt.get(s));
        Compile.ret.movq("%rax", "%rax");//dummy statement just to mark the end of the block
    }

    @Override
    public void visit(TSreturn s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' RETURN");
    }

    @Override
    public void visit(TSblock s) {
        LinkedList<TStmt> l = s.l;
        ListIterator<TStmt> it = l.listIterator();
        System.out.println("TSBLOCK " + s);

        while(it.hasNext()){
            TStmt st = it.next();
            st.accept(this); //we call visitor on each statement in the block
        }
        Compile.ret.movq("%rax", "%rax");//dummy statement just to mark the end of the block
    }

    @Override
    public void visit(TSfor s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' FOR");
    }

    @Override
    public void visit(TDconstructor d) {
        Compile.ret.label(tdClass.c.name + "_" + tdClass.c.name); //use the same label we referenced in class descriptor!!
        TStmt tstmt = d.s;
        System.out.println("COMPILE lets visit constructor " + tdClass.c.name);
        tstmt.accept(this);
        Compile.ret.ret();
    }

    @Override
    public void visit(TDmethod d) {
        TStmt tstmt = d.s;
        System.out.println("COMPILE lets visit method " + d.m.name);
        if(tdClass.c.name.equals("Main") && d.m.name.equals("main")){
            Compile.ret.label("main");
        }else{
            Compile.ret.label(tdClass.c.name + "_" + d.m.name); //use the same label we referenced in class descriptor!!
        }
        tstmt.accept(this);
        Compile.ret.ret();
    }
    
}
