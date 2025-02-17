package mini_java;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PropertyPermission;
import java.util.Set;

public class MyVisitor implements Visitor {
    /* We use this visitor to insert symbols into the Symbol Table during the static type check!
     * This implies that when we visit a parsed declaration, what we want to do here is
     * create the right symbol to insert into the Symbol Table
     */

    protected static boolean go_into_body;
    protected static TType ttype;
    protected static Class_ class_;
    protected static Set<String> keywords;
    protected static HashMap<String, TType> variables;

    public MyVisitor(){
        go_into_body = false;
        ttype = null;
        class_ = null;
        keywords = Set.of("boolean", "class", "else", "extends", "false",
        "for", "if", "instanceof",  "int",  "new", "null", "public", "return", "static",
        "this", "true", "void");
        variables = new HashMap<String, TType>();
    }

    public void go_into_body_FALSE(){
        go_into_body = false;
        return;
    }

    public void go_into_body_TRUE(){
        go_into_body = true;
        return;
    }

    public void setClass_(Class_ visitedClass_){
        class_ = visitedClass_;
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

        if(keywords.contains(name)){// check if name is a keyword
            Typing.error(null, "Identifier " + name + " is a reserved keyword");
            return;
        }

        if(variables.containsKey(name)){
            ttype = variables.get(name);
        }else{
            Typing.error(null, "Identifier " + name + " is not defined");
            return;
        }
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
        LinkedList<PStmt> l = s.l;
        ListIterator<PStmt> it = l.listIterator();
        while(it.hasNext()){
            PStmt st = it.next();
            st.accept(this); //we call visitor on each statement in the block
        }
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
            if (class_.attributes.get(s.x.id) != null) {//attributes must be distinct!!
                Typing.error(null, "Class " + class_.name + " has duplicate attribute " + s.x.id);
                return;
            }
            s.ty.accept(this);
            Attribute attribute = new Attribute(s.x.id, ttype);
            class_.attributes.put(s.x.id, attribute);
        }
    }

    @Override
    public void visit(PDconstructor s) {
        if(go_into_body){//let's analyse parameters and block of statements!!

            LinkedList<PParam> l = s.l;
            if(l == null){
                return;
            }
            ListIterator<PParam> it = l.listIterator();

            Method constructor = class_.methods.get(s.x.id);

            while(it.hasNext()){
                PParam pparam = it.next();

                PType ptype = pparam.ty;
                ptype.accept(this);

                Variable var = new Variable(pparam.x.id, ttype);

                if(constructor.params.contains(var)){
                    Typing.error(null, "Class " + class_.name + " has constructor " + constructor.name + " with duplicate parameter " +  s.x.id);
                    return;
                }
                
                constructor.params.add(var);
            }

            //visitor will go into statement block
            s.s.accept(this);

        }else{//let's add constructor
            if(class_.methods.get(s.x.id) != null){
                Typing.error(null, "Class " + class_.name + " has duplicate constructor " + s.x.id);
                return;
            }
            Method constructor = new Method(s.x.id, new TTvoid(), new LinkedList<Variable>());
            class_.methods.put(s.x.id, constructor);
        }
    }

    @Override
    public void visit(PDmethod s) {
        if(go_into_body){//let's analyse parameters, block of statements

            LinkedList<PParam> l = s.l;
            if(l == null){
                return;
            }
            ListIterator<PParam> it = l.listIterator();


            Method method = class_.methods.get(s.x.id);

            while(it.hasNext()){
                PParam pparam = it.next();

                System.out.println(pparam.x.id);

                PType ptype = pparam.ty;
                ptype.accept(this);

                Variable var = new Variable(pparam.x.id, ttype);

                if(method.params.contains(var)){
                    Typing.error(null, "Class " + class_.name + " has method " + method.name + " with duplicate parameter " +  s.x.id);
                    return;
                }
                
                method.params.add(var);
            }

            //Visitor will go into statement block
            s.s.accept(this);

        }else{//let's add method
            if(class_.methods.get(s.x.id) != null){
                Typing.error(null, "Class " + class_.name + " has duplicate methods " + s.x.id);
                return;
            }
            if(s.ty != null){
                s.ty.accept(this);
            }else{
                ttype = new TTnull();
            }
            Method method = new Method(s.x.id, ttype, new LinkedList<Variable>());
            class_.methods.put(s.x.id, method);
        }
    }
    
}
