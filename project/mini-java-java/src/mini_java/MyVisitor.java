package mini_java;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyVisitor implements Visitor {
    /* We use this visitor to insert symbols into the Symbol Table during the static type check!
     * This implies that when we visit a parsed declaration, what we want to do here is
     * create the right symbol to insert into the Symbol Table
     */

    protected static boolean goIntoBody, hasConstructor;
    protected static TType ttype;
    protected static TDClass tdclass;
    protected static HashMap<TDecl, TSblock> getTSblock;
    protected static TDecl currentTDecl;
    protected static TSblock currentBlock;
    protected static TStmt currentStmt;
    protected static TExpr currentExpr;
    protected static String callerName;
    protected static Boolean needsReturnStatement, hasReturnStatement;
    protected static Set<String> keywords;
    protected static HashMap<String, TEvar> variables;

    public MyVisitor(){
        goIntoBody = false;
        ttype = null;
        tdclass = null;
        keywords = Set.of("boolean", "class", "else", "extends", "false",
        "for", "if", "instanceof",  "int",  "new", "null", "public", "return", "static",
        "this", "true", "void");
        variables = new HashMap<String, TEvar>();
        getTSblock = new HashMap<TDecl, TSblock>();
    }

    static String identifierRegex = "[a-zA-Z_][a-zA-Z_0-9]*";
    static Pattern identifierPattern = Pattern.compile(identifierRegex);
    static boolean isIdentifierOk(String id){
        Matcher mat = identifierPattern.matcher(id);
        return mat.matches();
    }

    public static void goIntoBodyFALSE(){
        goIntoBody = false;
        hasConstructor = false;
        return;
    }

    public static void goIntoBodyTRUE(){
        goIntoBody = true;
        return;
    }

    public static boolean hasConstructor(){
        return hasConstructor;
    }

    public static void setClass_(TDClass currenTDclass){
        tdclass = currenTDclass;
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
        String name = t.x.id;

        if(ClassesTable.lookup(name) == null){
            Typing.error(t.x.loc, "Type identifier " + name + " does not denote a valid type");
            return;
        }

        ttype = new TTclass(ClassesTable.lookup(name));
    }

    @Override
    public void visit(Cbool c) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' BOOL");
    }

    @Override
    public void visit(Cstring c) {
        System.out.println("Cstring = " + c.s);
        currentExpr = new TEcst(c);
    }

    @Override
    public void visit(Cint c) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' INT");
    }

    @Override
    public void visit(PEcst e) {
        Constant c = e.c;
        c.accept(this);
    }

    @Override
    public void visit(PEbinop e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' EBINOP");
    }

    @Override
    public void visit(PEunop e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' EUNOP");
    }

    @Override
    public void visit(PEthis e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' ETHIS");
    }

    @Override
    public void visit(PEnull e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' ENULL");
    }

    @Override
    public void visit(PEident e) {
        Ident id = e.id;
        System.out.println("PEident id = " + id.id);

        if(!isIdentifierOk(id.id)){
            Typing.error(id.loc, "Identifier " + id.id + " violates identifier syntax");
            return;
        }

        if(keywords.contains(id.id)){// check if name is a keyword
            Typing.error(id.loc, "Identifier " + id.id + " is a reserved keyword");
            return;
        }
        callerName = id.id + callerName;
    }

    @Override
    public void visit(PEassignIdent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' EASSIDENT");
    }

    @Override
    public void visit(PEdot e) {
        PExpr pe = e.e;
        Ident id = e.id;
        System.out.println("PEdot id = " + id.id);
        callerName = "_" + id.id + callerName;
        pe.accept(this);
    }

    @Override
    public void visit(PEassignDot e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' EASSDOT");
    }

    @Override
    public void visit(PEnew e) {
        Ident id = e.c;
        LinkedList<PExpr> pl = e.l;

        if(ClassesTable.lookup(id.id) == null){
            Typing.error(id.loc, "Class " + id.id + " does not exist!");
            return;
        }
        Class_ class_ = ClassesTable.lookup(id.id);
        LinkedList<TExpr> tl = new LinkedList<TExpr>();

        ListIterator<PExpr> it = pl.listIterator();
        while(it.hasNext()){
            PExpr pe = it.next();
            pe.accept(this);
            tl.add(currentExpr);
        }

        currentExpr = new TEnew(class_, tl);
    }

    @Override
    public void visit(PEcall e) {
        PExpr pe = e.e; //who is calling the method
        Ident id = e.id; // name of the method
        LinkedList<PExpr> l = e.l; //method parameters

        System.out.println("CALL of " + id.id);

        LinkedList<TExpr> methodParams = new LinkedList<TExpr>();

        callerName = "";
        pe.accept(this);
        System.out.println("THE CALLER IS " + callerName);

        Method m;
        if((id.id.equals("print") || id.id.equals("println")) && callerName.equals("System_out")){//let's make an exception for print...
            m = new Method(callerName + "_" + id.id, new TTnull(), new LinkedList<Variable>());
        }else{//all other methods, we have to find in some of our other classes!!
            Class_ class_ = ClassesTable.lookup(callerName);
            m = class_.methods.get(id.id);
        }
        TEcall tecall = new TEcall(new TEnull(), m, methodParams);

        ListIterator<PExpr> it = l.listIterator();
        while(it.hasNext()){
            System.out.println("Let's visit arguments for the call!");
            PExpr ei = it.next();
            ei.accept(this);
            methodParams.add(currentExpr);
        }
        currentExpr = tecall;
    }

    @Override
    public void visit(PEcast e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' ECAST");
    }

    @Override
    public void visit(PEinstanceof e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' EINST");
    }

    @Override
    public void visit(PSexpr s) {
        s.e.accept(this);
        currentStmt = new TSexpr(currentExpr);
    }

    @Override
    public void visit(PSvar s) {
        PType ptype = s.ty;
        Ident id = s.x;
        PExpr pexpr = s.e;

        if(variables.containsKey(id.id)){
            Typing.error(id.loc, "Variable " + id.id + " is a duplicate");
            return;
        }

        System.out.println("Lets visit variable type");
        ptype.accept(this);
        System.out.println("Lets visit variable expression");
        pexpr.accept(this);
        Variable var = new Variable(id.id, ttype);
        currentStmt = new TSvar(var, currentExpr);
    }

    @Override
    public void visit(PSif s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' SIF");
    }

    @Override
    public void visit(PSreturn s) {
        hasReturnStatement = true;
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' SRET");
    }

    @Override
    public void visit(PSblock s) {
        System.out.println("VISITING STATEMENT BLOCK");
        LinkedList<PStmt> l = s.l;
        ListIterator<PStmt> it = l.listIterator();
        currentBlock = getTSblock.get(currentTDecl);
        hasReturnStatement = false;
        while(it.hasNext()){
            PStmt st = it.next();
            if(st instanceof PSexpr){
                System.out.println("Lets visit PSexpr");
            }else if(st instanceof PSvar){
                System.out.println("Lets visit PSvar");
            }else if(st instanceof PSif){
                System.out.println("Lets visit PSif");
            }else if(st instanceof PSreturn){
                System.out.println("Lets visit PSreturn");
            }else if(st instanceof PSblock){
                System.out.println("Lets visit PSblock");
            }else if(st instanceof PSfor){
                System.out.println("Lets visit PSfor");
            }
            st.accept(this); //we call visitor on each statement in the block
            currentBlock.l.add(currentStmt);
        }
        if(needsReturnStatement && !hasReturnStatement){
            Typing.error(null, "Method is missing a return statement");
            return;
        }
    }

    @Override
    public void visit(PSfor s) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit' SFOR");
    }

    @Override
    public void visit(PDattribute s) {
        if(goIntoBody){//we're interested in the body of methods and constructors ONLY!
            return;
        }else{//let's add this attribute to the symbol table
            if (tdclass.c.attributes.get(s.x.id) != null) {//attributes must be distinct!!
                Typing.error(null, "Class " + tdclass.c.name + " has duplicate attribute " + s.x.id);
                return;
            }
            s.ty.accept(this);
            Attribute attribute = new Attribute(s.x.id, ttype);
            tdclass.c.attributes.put(s.x.id, attribute);
        }
    }

    @Override
    public void visit(PDconstructor s) {
        hasConstructor = true;
        if(goIntoBody){//let's analyse parameters and block of statements!!

            System.out.println("SECOND PASS IN VISITOR FOR CONSTRUCTOR " + s.x.id);
            LinkedList<PParam> l = s.l;
            TDconstructor constructor = Typing.getTDconstructor.get(tdclass.c);

            if(l != null){
                ListIterator<PParam> it = l.listIterator();
                Set<String> parameterNames = new HashSet<>();
                while(it.hasNext()){
                    PParam pparam = it.next();

                    PType ptype = pparam.ty;
                    ptype.accept(this);

                    Variable var = new Variable(pparam.x.id, ttype);

                    if(parameterNames.contains(pparam.x.id)){
                        Typing.error(null, "Parameter " + pparam.x.id + " is a duplicate");
                        return;
                    }
                    if(constructor.params.contains(var)){
                        Typing.error(null, "Class " + tdclass.c.name + " has constructor " + tdclass.c.name + " with duplicate parameter " +  s.x.id);
                        return;
                    }
                    
                    constructor.params.add(var);
                    parameterNames.add(pparam.x.id);
                }
            }

            //visitor will go into statement block
            currentTDecl = Typing.getTDecl.get((TDecl)constructor);
            s.s.accept(this);

        }else{//let's add constructor
            System.out.println("FIRST PASS IN VISITOR FOR CONSTRUCTOR " + s.x.id);
            if(!tdclass.c.name.equals(s.x.id)){
                Typing.error(null, "Constructor " + s.x.id + " does not match class name " + tdclass.c.name);
                return;
            }
            if(tdclass.c.methods.get(s.x.id) != null){
                Typing.error(null, "Class " + tdclass.c.name + " has duplicate constructor " + s.x.id);
                return;
            }
            Method constructor = new Method(s.x.id, new TTvoid(), new LinkedList<Variable>());
            tdclass.c.methods.put(s.x.id, constructor);

            LinkedList<PParam> l = s.l;
            ListIterator<PParam> it = l.listIterator();
            LinkedList<Variable> tparams = new LinkedList<Variable>();
            while(it.hasNext()){
                PParam pparam = it.next();
                pparam.ty.accept(this);
                Variable v = new Variable(pparam.x.id, ttype);
                tparams.add(v);
            }

            TSblock tsblock = new TSblock();
            TDconstructor tdconstructor = new TDconstructor(tparams, new TSblock());
            getTSblock.put(tdconstructor, tsblock);
            tdclass.l.add(tdconstructor);
            Typing.getTDecl.put(constructor, tdconstructor);
        }
    }

    @Override
    public void visit(PDmethod s) {
        if(goIntoBody){//let's analyse parameters, block of statements

            System.out.println("SECOND PASS IN VISITOR FOR METHOD " + s.x.id);
            LinkedList<PParam> l = s.l;
            Method method = tdclass.c.methods.get(s.x.id);

            if(l != null){
                ListIterator<PParam> it = l.listIterator();
                Set<String> parameterNames = new HashSet<>();
                while(it.hasNext()){
                    PParam pparam = it.next();

                    System.out.println("CHECK PPARAM" + pparam.x.id);

                    PType ptype = pparam.ty;
                    ptype.accept(this);

                    Variable var = new Variable(pparam.x.id, ttype);

                    if(parameterNames.contains(pparam.x.id)){
                        Typing.error(null, "Parameter " + pparam.x.id + " is a duplicate");
                        return;
                    }
                    if(method.params.contains(var)){
                        Typing.error(null, "Class " + tdclass.c.name + " has method " + method.name + " with duplicate parameter " +  s.x.id);
                        return;
                    }
                    
                    method.params.add(var);
                    parameterNames.add(pparam.x.id);
                }
            }

            //Visitor will go into statement block
            if(s.s instanceof PSexpr){
                System.out.println("Lets visit PSexpr");
            }else if(s.s instanceof PSvar){
                System.out.println("Lets visit PSvar");
            }else if(s.s instanceof PSif){
                System.out.println("Lets visit PSif");
            }else if(s.s instanceof PSreturn){
                System.out.println("Lets visit PSreturn");
            }else if(s.s instanceof PSblock){
                System.out.println("Lets visit PSblock");
            }else if(s.s instanceof PSfor){
                System.out.println("Lets visit PSfor");
            }
            currentTDecl = Typing.getTDecl.get(method);
            if(!(method.type instanceof TTvoid)){
                System.out.println("ok, we will need a return statement " + method.type);
                needsReturnStatement = true;
            }else{
                System.out.println("ok, no return statement :)");
                needsReturnStatement = false;
            }
            s.s.accept(this);

        }else{//let's add method
            System.out.println("FIRST PASS IN VISITOR FOR METHOD " + s.x.id);
            if(tdclass.c.methods.get(s.x.id) != null){
                Typing.error(null, "Class " + tdclass.c.name + " has duplicate methods " + s.x.id);
                return;
            }
            if(s.ty != null){
                System.out.println("METHOD HAS TYPE " + s.ty);
                s.ty.accept(this);
            }else{
                System.out.println("METHOD HAS NO RETURN TYPE => VOID");
                ttype = new TTvoid();
            }
            Method method = new Method(s.x.id, ttype, new LinkedList<Variable>());
            tdclass.c.methods.put(s.x.id, method);

            TSblock tsblock = new TSblock();
            TDmethod tdmethod = new TDmethod(method, tsblock); // add method to typed AST
            getTSblock.put(tdmethod, tsblock);
            tdclass.l.add(tdmethod);
            Typing.getTDecl.put(method, tdmethod);
        }
    }
    
}
