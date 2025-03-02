package mini_java;

import java.util.LinkedList;
import java.util.ListIterator;

public class MyTVisitor implements TVisitor {

    static TDClass tdClass;

    static int msgCnt = 0;
    String toPrint;

    public void setTDClass(TDClass tdClass) {
        MyTVisitor.tdClass = tdClass;
    }

    @Override
    public void visit(TTvoid t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(TTnull t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(TTboolean t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(TTint t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(TTclass t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Cbool c) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' BOOL");
    }

    @Override
    public void visit(Cstring c) {
        toPrint = c.s;
    }

    @Override
    public void visit(Cint c) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' INT");
    }

    @Override
    public void visit(TEcst e) {
        Constant c = e.c;
        c.accept(this);
    }

    @Override
    public void visit(TEbinop e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' BINOP");
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
        System.out.println("COMPILE visiting TTnull");
    }

    @Override
    public void visit(TEvar e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' VAR");
    }

    @Override
    public void visit(TEassignVar e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' ASS_VAR");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' NEW");
    }

    @Override
    public void visit(TEcall e) {
        TExpr te = e.e;
        Method m = e.m;
        LinkedList<TExpr> l = e.l;
        ListIterator<TExpr> it = l.listIterator();

        System.out.println("COMPILE visiting call " + m.name);

        te.accept(this);
        if(m.name.equals("System_out_print") || m.name.equals("System_out_println")){
            msgCnt += 1;
            Compile.ret.dlabel("message" + msgCnt);
            TExpr aux = it.next(); // for print let's just assume a single argument
            aux.accept(this);
            Compile.ret.string(toPrint);

            Compile.ret.movq("$message"+msgCnt, "%rdi");
            Compile.ret.call("puts");
            Compile.ret.movq("$0", "%rax");
        }else{
            while(it.hasNext()){//TODO: put arguments in registers before call
                TExpr argument = it.next();
            }
            Compile.ret.call(m.name); //assume return value is in %rax
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' PRINT");
    }

    @Override
    public void visit(TSexpr s) {
        s.e.accept(this);
    }

    @Override
    public void visit(TSvar s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' SVAR");
    }

    @Override
    public void visit(TSif s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' IF");
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
        System.out.println("COMPILE visiting statement block");
        while(it.hasNext()){
            TStmt st = it.next();
            if(st instanceof TSexpr){
                System.out.println("Lets visit TSexpr");
            }else if(st instanceof TSvar){
                System.out.println("Lets visit TSvar");
            }else if(st instanceof TSif){
                System.out.println("Lets visit TSif");
            }else if(st instanceof TSreturn){
                System.out.println("Lets visit TSreturn");
            }else if(st instanceof TSblock){
                System.out.println("Lets visit TSblock");
            }else if(st instanceof TSfor){
                System.out.println("Lets visit TSfor");
            }
            st.accept(this); //we call visitor on each statement in the block
        }
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
    }

    @Override
    public void visit(TDmethod d) {
        TStmt tstmt = d.s;
        System.out.println("COMPILE lets visit method " + d.m.name);
        if(tdClass.c.name.equals("Main") && d.m.name.equals("main")){
            Compile.ret.label("main");
            Compile.ret.pushq("%rbp");
            Compile.ret.movq("%rsp", "%rbp");
        }else{
            Compile.ret.label(tdClass.c.name + "_" + d.m.name); //use the same label we referenced in class descriptor!!
        }
        tstmt.accept(this);
        if(tdClass.c.name.equals("Main") && d.m.name.equals("main")){
            Compile.ret.ret();
        }
    }
    
}
