package mini_java;

import java.util.ListIterator;

public class SymbolTableVisitor implements Visitor {
    /* We use this visitor to insert symbols into the Symbol Table during the static type check!
     * This implies that when we visit a parsed declaration, what we want to do here is
     * create the right symbol to insert into the Symbol Table
     */

    protected static boolean go_into_body;
    protected static SymbolTable symbolTable;
    protected static TType ttype;
    public boolean errorFlag = false;
    public String errorMsg;

    public void go_into_body_FALSE(){
        go_into_body = false;
        return;
    }

    public void go_into_body_TRUE(){
        go_into_body = true;
        return;
    }

    @Override
    public void visit(PTboolean t) {
        ttype = new TTboolean();
    }

    @Override
    public void visit(PTint t) {
        ttype = new TTint();
    }

    @Override
    public void visit(PTident t) {
        ttype = null;
    }

    @Override
    public void visit(Cbool c) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Cstring c) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(Cint c) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEcst e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEbinop e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEunop e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEthis e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEnull e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEident e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEassignIdent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEdot e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEassignDot e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEnew e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEcall e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEcast e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PEinstanceof e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PSexpr s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PSvar s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PSif s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PSreturn s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PSblock s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PSfor s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public void visit(PDattribute s) {
        if(go_into_body){//we're interested in the body of methods and constructors ONLY!
            return;
        }else{//let's add this attribute to the symbol table
            if (symbolTable.scope_look_current(s.x) != null) {//attributes must be distinct!!
                errorFlag = true;
                errorMsg = "class attribute " + s.x.id + " must have unique identifier";
                return;
            }
            s.ty.accept(this);
            Symbol attributeSymbol = new Symbol(Symbol_type.SYMBOL_CLASS_SCOPE, ttype, s.x, symbolTable.scope_level());
            symbolTable.scope_bind(s.x, attributeSymbol);
        }
    }

    @Override
    public void visit(PDconstructor s) {
        if(go_into_body){//let's analyse parameters and block of statements!!
            symbolTable.scope_enter();
            ListIterator<PParam> it = s.l.listIterator();

            while(it.hasNext()){
                PParam pparam = it.next();
                if(symbolTable.scope_look_current(s.x) != null){
                    errorFlag = true;
                    errorMsg = "constructor " + s.x.id + " parameter " + pparam.x.id + " must have unique identifier";
                    return;
                }
                pparam.ty.accept(this);
                Symbol paramSymbol = new Symbol(Symbol_type.SYMBOL_INNER_SCOPE, ttype, pparam.x, symbolTable.scope_level());
                symbolTable.scope_bind(pparam.x, paramSymbol);
            }
            symbolTable.scope_exit();

        }else{//let's add constructor
            if(symbolTable.scope_lookup(s.x) != null){
                errorFlag = true;
                errorMsg = "class constructor " + s.x.id + " must have unique identifier";
                return;
            }
            //if constructor name generates no conflict => add symbol with type TTvoid to Symbol Table!
            Symbol constructorSymbol = new Symbol(Symbol_type.SYMBOL_CLASS_SCOPE, new TTvoid(), s.x, symbolTable.scope_level());
            symbolTable.scope_bind(s.x, constructorSymbol);
        }
    }

    @Override
    public void visit(PDmethod s) {
        if(go_into_body){//let's analyse parameters block of statements

            Symbol methodSymbol = symbolTable.scope_look_current(s.x);
            TDClass tdclass = Typing.typedClasses.getLast();
            TDmethod tdmethod = new TDmethod(null, null, null);
            tdclass.l.add(tdmethod);
            // Is this really the best way to add stuff into our class ?

            symbolTable.scope_enter();
            ListIterator<PParam> it = s.l.listIterator();

            while(it.hasNext()){
                PParam pparam = it.next();
                if(symbolTable.scope_look_current(s.x) != null){
                    errorFlag = true;
                    errorMsg = "method " + s.x.id + " parameter " + pparam.x.id + " must have unique identifier";
                    return;
                }
                pparam.ty.accept(this);
                Symbol paramSymbol = new Symbol(Symbol_type.SYMBOL_INNER_SCOPE, ttype, pparam.x, symbolTable.scope_level());
                symbolTable.scope_bind(pparam.x, paramSymbol);
            }
            symbolTable.scope_exit();
        }else{//let's add method
            if(symbolTable.scope_lookup(s.x) != null){
                errorFlag = true;
                errorMsg = "class method " + s.x.id + " must have unique identifier";
                return;
            }
            s.ty.accept(this);
            Symbol methodSymbol = new Symbol(Symbol_type.SYMBOL_CLASS_SCOPE, ttype, s.x, symbolTable.scope_level());
            symbolTable.scope_bind(s.x, methodSymbol);
        }
    }
    
}
