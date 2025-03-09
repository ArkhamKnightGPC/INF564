package mini_java;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.type.NullType;

import org.w3c.dom.Attr;

public class MyVisitor implements Visitor {
    /* We use this visitor to insert symbols into the Symbol Table during the static type check!
     * This implies that when we visit a parsed declaration, what we want to do here is
     * create the right symbol to insert into the Symbol Table
     */

    protected static boolean goIntoBody, hasConstructor;
    protected static TType ttype;
    protected static TType ttypeBinop;
    protected static TType ttypeUnop;
    protected static TDClass tdclass;
    protected static HashMap<TDecl, TSblock> getTSblock;
    protected static TDecl currentTDecl;
    protected static TSblock currentBlock;
    protected static TStmt currentStmt;
    protected static TExpr currentExpr;
    protected static String callerName="";
    protected static Boolean needsReturnStatement, hasReturnStatement;
    protected static Set<String> keywords;
    protected static HashMap<String, Variable> variables;
    protected static HashMap<Variable, PType> getVarPtype;
    protected static boolean isElseBlock = false;
    protected static boolean stringsInvolved;

    public MyVisitor(){
        goIntoBody = false;
        ttype = null;
        tdclass = null;
        keywords = Set.of("boolean", "class", "else", "extends", "false",
        "for", "if", "instanceof",  "int",  "new", "null", "public", "return", "static",
        "this", "true", "void");
        variables = new HashMap<String, Variable>();
        getTSblock = new HashMap<TDecl, TSblock>();
        getVarPtype = new HashMap<Variable, PType>();
    }

    static String identifierRegex = "[a-zA-Z_][a-zA-Z_0-9]*";
    static Pattern identifierPattern = Pattern.compile(identifierRegex);
    static boolean isIdentifierOk(String id){
        Matcher mat = identifierPattern.matcher(id);
        return mat.matches();
    }

    public static void goIntoBodyFALSE(){
        goIntoBody = false;
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
        hasConstructor = false;
        return;
    }

    public static boolean compatibilityTest(TExpr ei, TType tt){
        System.out.println("COMPATIBILITY TEST " + ei + " " + tt);
        if(tt instanceof TTvoid){

        }else if(tt instanceof TTnull){

        }else if(tt instanceof TTboolean){
            if(ei instanceof TEcst){
                Constant c = ((TEcst)ei).c;
                return (c instanceof Cbool);
            }else if(ei instanceof TEbinop){
                return ttypeBinop.getClass() == tt.getClass();
            }else if(ei instanceof TEunop){
                return compatibilityTest(((TEunop)ei).e, tt);
            }else if(ei instanceof TEcall){
                return ((TEcall)ei).m.type.getClass() == tt.getClass();
            }else{
                return false;
            }
        }else if(tt instanceof TTint){
            if(ei instanceof TEcst){
                Constant c = ((TEcst)ei).c;
                return (c instanceof Cint);
            }else if(ei instanceof TEbinop){
                return ttypeBinop.getClass() == tt.getClass();
            }else if(ei instanceof TEunop){
                return compatibilityTest(((TEunop)ei).e, tt);
            }else if(ei instanceof TEcall){
                return ((TEcall)ei).m.type.getClass() == tt.getClass();
            }else if(ei instanceof TEvar){
                Variable v = ((TEvar)ei).x;
                return v.ty.getClass() == tt.getClass();
            }else{
                return false;
            }
        }else if(tt instanceof TTclass){//ok, I will not be coming here to validate print...
            Class_ c = ((TTclass)tt).c;
            if(ei instanceof TEcst){
                Constant cte = ((TEcst)ei).c;
                if(c.equals(Typing.StringClass)){
                    return (cte instanceof Cstring);
                }
                return true;
            }else if(ei instanceof TEbinop){
                return ttypeBinop.getClass() == tt.getClass();
            }else if(ei instanceof TEunop){
                return compatibilityTest(((TEunop)ei).e, tt);
            }else if(ei instanceof TEvar){
                Variable v = ((TEvar)ei).x;
                TType tt2 = v.ty;
                Class_ c2 = ((TTclass)tt2).c;
                if(tt2 instanceof TTclass){
                    while(c2.name.equals(c.name) == false && c2.extends_ != null){
                        c2 = c2.extends_;
                    }
                    boolean isValidDescendant = c2.name.equals(c.name);
                    return isValidDescendant;
                }else{
                    return false;
                }
            }else if(ei instanceof TEnew){
                return true;
            }else if(ei instanceof TEnull){
                return true;
            }else if(ei instanceof TEthis){
                return tdclass.c.name.equals(c.name);
            }else if(ei instanceof TEassignAttr){
                return ((TEassignAttr)ei).a.ty.getClass() == tt.getClass();
            }else{
                return false;
            }
        }
        return true;
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
        currentExpr = new TEcst(c);
    }

    @Override
    public void visit(Cstring c) {
        currentExpr = new TEcst(c);
    }

    @Override
    public void visit(Cint c) {
        currentExpr = new TEcst(c);
    }

    @Override
    public void visit(PEcst e) {
        Constant c = e.c;
        c.accept(this);
    }

    @Override
    public void visit(PEbinop e) {
        Binop op = e.op; Binop typedOp = op;
        PExpr e1 = e.e1;
        PExpr e2 = e.e2;
        e1.accept(this);
        TExpr te1 = currentExpr;
        e2.accept(this);
        TExpr te2 = currentExpr;

        System.out.println("Show me expressions in Binop " + te1 + " " + te2 + " op=" + op);
        TTint auxTint = new TTint();
        TTboolean auxTbool = new TTboolean();
        TTclass stringType = new TTclass(Typing.StringClass);
        boolean atLeastOne = (compatibilityTest(te1, auxTint) && compatibilityTest(te2, auxTint))
                            || (compatibilityTest(te1, auxTbool) && compatibilityTest(te2, auxTbool))
                            || ( te1 instanceof TEvar && !(te2 instanceof TEvar) && ( ((TEvar)te1).x.ty.getClass() == auxTbool.getClass() || ((TEvar)te1).x.ty.getClass() == auxTint.getClass()))
                            || ( te2 instanceof TEvar && !(te1 instanceof TEvar) && ( ((TEvar)te2).x.ty.getClass() == auxTbool.getClass() || ((TEvar)te2).x.ty.getClass() == auxTint.getClass()))
                            || (te1 instanceof TEvar && te2 instanceof TEvar && ((TEvar)te1).x.getClass() == ((TEvar)te2).x.getClass())
                            || ( te1 instanceof TEattr && !(te2 instanceof TEattr) && ( ((TEattr)te1).a.ty.getClass() == auxTbool.getClass() || ((TEattr)te1).a.ty.getClass() == auxTint.getClass()))
                            || ( te2 instanceof TEattr && !(te1 instanceof TEvar) && ( ((TEattr)te2).a.ty.getClass() == auxTbool.getClass() || ((TEattr)te2).a.ty.getClass() == auxTint.getClass()))
                            || (te1 instanceof TEattr && te2 instanceof TEattr && ((TEattr)te1).a.ty.getClass() == ((TEattr)te2).a.ty.getClass())
                            || (te1 instanceof TEattr && te2 instanceof TEvar && ((TEattr)te1).a.ty.getClass() == ((TEvar)te2).x.ty.getClass())
                            || (te2 instanceof TEattr && te1 instanceof TEvar && ((TEattr)te2).a.ty.getClass() == ((TEvar)te1).x.ty.getClass())
                            || (te1 instanceof TEnull && (te2 instanceof TEvar || te2 instanceof TEattr))
                            || (te2 instanceof TEnull && (te1 instanceof TEvar || te1 instanceof TEattr));
        boolean atLeastOne2 = (compatibilityTest(te1, auxTbool) && compatibilityTest(te2, auxTbool))
                            || ( te1 instanceof TEvar && te2 instanceof TEcst && ((TEvar)te1).x.ty.getClass() == auxTbool.getClass() && ((TEcst)te1).c instanceof Cbool)
                            || ( te2 instanceof TEvar && te1 instanceof TEcst && ((TEvar)te2).x.ty.getClass() == auxTbool.getClass() && ((TEcst)te1).c instanceof Cbool)
                            || (te1 instanceof TEvar && te2 instanceof TEvar && ((TEvar)te1).x.getClass() == ((TEvar)te2).x.getClass() && ((TEvar)te2).x.ty.getClass() == auxTbool.getClass())
                            || (te1 instanceof TEattr && te2 instanceof TEattr && ((TEattr)te1).a.ty.getClass() == ((TEattr)te2).a.ty.getClass())
                            || (te1 instanceof TEattr && te2 instanceof TEvar && ((TEattr)te1).a.ty.getClass() == ((TEvar)te2).x.ty.getClass())
                            || (te2 instanceof TEattr && te1 instanceof TEvar && ((TEattr)te2).a.ty.getClass() == ((TEvar)te1).x.ty.getClass());
        boolean atLeastOne3 = (compatibilityTest(te1, auxTint) && compatibilityTest(te2, auxTint))
                            || ( te1 instanceof TEvar && te2 instanceof TEcst && ((TEvar)te1).x.ty.getClass() == auxTint.getClass() && ((TEcst)te1).c instanceof Cint)
                            || ( te2 instanceof TEvar && te1 instanceof TEcst && ((TEvar)te2).x.ty.getClass() == auxTint.getClass() && ((TEcst)te1).c instanceof Cint)
                            || (te1 instanceof TEvar && te2 instanceof TEvar && ((TEvar)te1).x.getClass() == ((TEvar)te2).x.getClass() && ((TEvar)te2).x.ty.getClass() == auxTint.getClass())
                            || (te1 instanceof TEattr && te2 instanceof TEattr && ((TEattr)te1).a.ty.getClass() == ((TEattr)te2).a.ty.getClass())
                            || (te1 instanceof TEattr && te2 instanceof TEvar && ((TEattr)te1).a.ty.getClass() == ((TEvar)te2).x.ty.getClass())
                            || (te2 instanceof TEattr && te1 instanceof TEvar && ((TEattr)te2).a.ty.getClass() == ((TEvar)te1).x.ty.getClass());
        stringsInvolved = ((te1 instanceof TEcst && ((TEcst)te1).c instanceof Cstring) || (te2 instanceof TEcst && ((TEcst)te2).c instanceof Cstring) && !(te1 instanceof TEcst && ((TEcst)te1).c instanceof Cbool) && !(te2 instanceof TEcst && ((TEcst)te2).c instanceof Cbool))
                                || (te1 instanceof TEbinop && compatibilityTest(te1, stringType))
                                || (te2 instanceof TEbinop && compatibilityTest(te2, stringType))
                                || (te1 instanceof TEvar && ((TEvar)te1).x.ty.getClass() == stringType.getClass())
                                || (te2 instanceof TEvar && ((TEvar)te1).x.ty.getClass() == stringType.getClass())
                                || (te1 instanceof TEattr && ((TEattr)te1).a.ty.getClass() == stringType.getClass())
                                || (te2 instanceof TEattr && ((TEattr)te2).a.ty.getClass() == stringType.getClass())
                                || (te1 instanceof TEcall && ((TEcall)te1).m.type.getClass() == stringType.getClass())
                                || (te2 instanceof TEcall && ((TEcall)te2).m.type.getClass() == stringType.getClass());
        switch(op){ //let's check if the expression types are ok!
            case Badd:
                System.out.println("Add");
                if(stringsInvolved){
                    ttypeBinop = new TTclass(Typing.StringClass);
                    typedOp = Binop.Badd_s;
                }else{
                    ttypeBinop = new TTint();
                    if(!atLeastOne3){
                        Typing.error(null, "ADD operations need two integer operands or be a string/int concatenation");
                        return;
                    }
                }
                break;
            case Bsub:
                System.out.println("Sub");
                ttypeBinop = new TTint();
                if(!atLeastOne3){
                    Typing.error(null, "SUB operations need two integer operands");
                    return;
                }
                break;
            case Bmul:
                System.out.println("Mul");
                ttypeBinop = new TTint();
                if(!atLeastOne3){
                    Typing.error(null, "MUL operations need two integer operands");
                    return;
                }
                break;
            case Bdiv:
                System.out.println("Div");
                ttypeBinop = new TTint();
                if(!atLeastOne3){
                    Typing.error(null, "DIV operations need two integer operands");
                    return;
                }
                break;
            case Bmod:
                System.out.println("Mod");
                ttypeBinop = new TTint();
                if(!atLeastOne3){
                    Typing.error(null, "MOD operations need two integer operands");
                    return;
                }
                break;
            case Beq:
                System.out.println("Eq");
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "EQ TEST needs two compatible operands");
                    return;
                }
                break;
            case Bneq:
                System.out.println("Neq");
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "NEQ TEST needs two compatible operands");
                    return;
                }
                break;
            case Blt:
                System.out.println("Lt");
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "LT operations need two integer operands");
                    return;
                }
                break;
            case Ble:
                System.out.println("Le");
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "LE operations need two integer operands");
                    return;
                }
                break;
            case Bgt:
                System.out.println("Gt");
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "GT operations need two integer operands");
                    return;
                }
                break;
            case Bge:
                System.out.println("Ge");
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "GE operations need two integer operands");
                    return;
                }
                break;
            case Band:
                System.out.println("Binop And");
                ttypeBinop = new TTboolean();
                if(!atLeastOne2){
                    Typing.error(null, "AND operations need two boolean operands");
                    return;
                }
                break;
            case Bor:
                System.out.println("Binop Or");
                ttypeBinop = new TTboolean();
                if(!atLeastOne2){
                    Typing.error(null, "OR operations need two boolean operands");
                    return;
                }
                break;
            case Badd_s:
                System.out.println("Add_s");
                ttypeBinop = new TTclass(Typing.StringClass);
                break;
        }
        currentExpr = new TEbinop(typedOp, te1, te2);
    }

    @Override
    public void visit(PEunop e) {
        Unop op = e.op; Unop typedOp = op;
        PExpr pe = e.e;
        pe.accept(this);

        switch (op) {
            case Uneg:
                ttypeUnop = new TTboolean();
                break;
            case Unot:
                ttypeUnop = new TTboolean();
                break;
            case Upreinc:
                ttypeUnop = new TTint();
                break;
            case Upostinc:
                ttypeUnop = new TTint();
                break;
            case Upredec:
                ttypeUnop = new TTint();
                break;
            case Upostdec:
                ttypeUnop = new TTint();
                break;
            case Ustring_of_int:
                ttypeUnop = new TTclass(Typing.StringClass);
                break;
        }

        TEunop te = new TEunop(typedOp, currentExpr);
        currentExpr = te;
    }

    @Override
    public void visit(PEthis e) {
        currentExpr = new TEthis();
    }

    @Override
    public void visit(PEnull e) {
        currentExpr = new TEnull();
    }

    public boolean findParam(TDecl tdecl, Ident id){
        ListIterator<Variable> it;

        if(tdecl instanceof TDmethod){
            TDmethod currentMethod = (TDmethod)tdecl;
            LinkedList<Variable> l = currentMethod.m.params;
            it = l.listIterator();
        }else{ //currentTDecl instanceof TDconstructor
            TDconstructor currentConstructor = (TDconstructor)tdecl;
            LinkedList<Variable> l = currentConstructor.params;
            it = l.listIterator();
        }

        boolean found = false;
        while(it.hasNext()){
            Variable v = it.next();
            if(v.name.equals(id.id)){
                found = true;
                currentExpr = new TEvar(v);
            }
        }
        return found;
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

        if(callerName == ""){
            boolean found = findParam(currentTDecl, id);

            if(!found){
                if(variables.containsKey(id.id)){
                    currentExpr = new TEvar(variables.get(id.id));
                }else if(tdclass.c.attributes.containsKey(id.id)){
                    currentExpr = new TEattr(new TEthis(), tdclass.c.attributes.get(id.id));
                }else{
                    Typing.error(id.loc, "Variable " + id.id + " does not exist");
                    return;
                }
            }

        }else{
            System.out.println("CHECK " + id.id + " AND " + callerName);
            if(id.id.equals("System") && (callerName.equals("print") || callerName.equals("out"))){
                callerName = "System_out";
                currentExpr = new TEprint(currentExpr);
                return;
            }
            String callerClass = id.id;
            if(ClassesTable.classesTable.containsKey(callerClass) == false){
                Typing.error(id.loc, "Class does not exist " + callerClass);
                return;
            }
            Class_ c = ClassesTable.lookup(id.id);
            if(c.attributes.containsKey(callerName)){
                Attribute res = c.attributes.get(callerName);
                currentExpr = new TEvar(new Variable(res.name, res.ty));
            }else{
                Typing.error(id.loc, "invalid attribute name for class " + id.id);
                return;
            }
        }
    }

    @Override
    public void visit(PEassignIdent e) {
        Ident id = e.id;
        PExpr pe = e.e;
        pe.accept(this);

        if (tdclass.c.attributes.containsKey(id.id)) {//we are making assign to attribute

            Attribute a = tdclass.c.attributes.get(id.id);
            System.out.println(a.ty + " " + currentExpr);

            if(compatibilityTest(currentExpr, a.ty) == false){
                Typing.error(id.loc, "Invalid type for assignment of attribute " + id.id);
                return;
            }

            TEassignAttr aux = new TEassignAttr(new TEthis(), a, currentExpr);
            currentExpr = aux;
        }else if(variables.containsKey(id.id)){
            Variable v = variables.get(id.id);
            TEassignVar aux = new TEassignVar(v, currentExpr);
            currentExpr = aux;
        }else{
            Typing.error(id.loc, "Attribution not possible for identifier " + id.id);
            return;
        }
    }

    @Override
    public void visit(PEdot e) {
        PExpr pe = e.e;
        Ident id = e.id;
        System.out.println("PEdot id = " + id.id);
        if(callerName.equals("")){
            callerName = id.id;
        }else{
            callerName = id.id + "_" + callerName;
        }
        pe.accept(this);
    }

    @Override
    public void visit(PEassignDot e) { // e.next = next (attribution on field of a TEvar)
        PExpr e1 = e.e1;
        Ident id = e.id;
        PExpr e2 = e.e2;
        System.out.println(e1 + " " + id.id + " " + e2);
        e1.accept(this);
        TExpr aux = currentExpr;
        System.out.println(currentExpr);
        e2.accept(this);
        TExpr aux2 = currentExpr;
        System.out.println(currentExpr);
        Attribute a;
        if(aux instanceof TEvar){
            Variable v = ((TEvar)aux).x;
            PType vt = getVarPtype.get(v);
            System.out.println("SHIT " + vt);
        }
        currentExpr = new TEassignAttr(aux, null, aux2);
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

        TDconstructor tdconstructor = Typing.getTDconstructor.get(class_);
        LinkedList<Variable> l = tdconstructor.params;

        if(pl.size() != l.size()){
            Typing.error(null, "Wrong number of arguments for constructor of class " + class_.name);
            return;
        }

        ListIterator<PExpr> it = pl.listIterator();
        ListIterator<Variable> it2 = l.listIterator();
        while(it.hasNext()){
            PExpr pe = it.next();
            Variable expectedParam = it2.next();
            pe.accept(this);
            if(!compatibilityTest(currentExpr, expectedParam.ty)){
                Typing.error(id.loc, "Wrong type for an argument in constructor of class " + class_.name);
                return;
            }
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
        stringsInvolved = false;

        LinkedList<TExpr> methodParams = new LinkedList<TExpr>();

        callerName = "";
        pe.accept(this);
        System.out.println("THE CALLER IS " + callerName);

        Method m; boolean isPrint = false;
        if(id.id.equals("print") || id.id.equals("println") || callerName.equals("_print") || callerName.equals("System_out")){//let's make an exception for print...
            LinkedList<Variable> mv = new LinkedList<Variable>();
            mv.add(new Variable("printArg", new TTclass(Typing.StringClass)));
            m = new Method(callerName + "_" + id.id, new TTvoid(), mv);
            isPrint = true;
        }else{//all other methods, we have to find in some of our other classes!!
            Class_ class_ = ClassesTable.lookup(callerName);
            if(class_ == null){ // x.insert() -> it's not directly the class, we need to get it from variable type
                System.out.println("HELP ME FIND YOU " + currentExpr + " callerName= "  + callerName + " id.id= " + id.id);
                if(currentExpr instanceof TEattr){
                    Attribute a  = ((TEattr)currentExpr).a;
                    if(Typing.getAtrrClass.containsKey(a)){
                        System.out.println("VERY NICE");
                    }else{
                        System.out.println("SAD :(");
                    }
                    class_ = Typing.getAtrrClass.get(a);
                }else if(currentExpr instanceof TEvar){
                    Variable v = ((TEvar)currentExpr).x;
                    PType pt = getVarPtype.get(v);
                    if(pt instanceof PTident){
                        class_ = ClassesTable.lookup(((PTident)pt).x.id);
                    }else{
                        Typing.error(id.loc, "call to " + id.id + " not valid");
                        return;
                    }
                }else if(currentExpr instanceof TEcall){
                    if (id.id.equals("equals")){
                        class_ = Typing.StringClass;
                    }
                }
            }
            m = class_.methods.get(id.id);
        }
        TEcall tecall = new TEcall(currentExpr, m, methodParams);

        ListIterator<PExpr> it = l.listIterator();
        ListIterator<Variable> it2 = m.params.listIterator();
        while(it.hasNext()){
            System.out.println("Let's visit arguments for the call!");
            PExpr ei = it.next();
            ei.accept(this);
            if(it2.hasNext() == false){
                Typing.error(id.loc, "Too many arguments in function " + callerName + "_" + id.id + "call");
                return;
            }
            Variable ej = it2.next();
            System.out.println("Check parameter compatibility " + ei + " " + ej.ty);
            boolean printErrorCriteria = !(currentExpr instanceof TEcst || ttypeBinop instanceof TTboolean || ttypeBinop instanceof TTint);
            if(isPrint && printErrorCriteria && !stringsInvolved){
                Typing.error(id.loc, "Wrong argument type for call of Print function ");
                return;
            }else if(!compatibilityTest(currentExpr, ej.ty)){
                Typing.error(id.loc, "Wrong argument type for call of function " + callerName + "_" + id.id);
                return;
            }
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
        if(!compatibilityTest(currentExpr, ttype)){
            Typing.error(id.loc, "Variable " + id.id + " not compatible with expression type");
            return;
        }
        Variable var = new Variable(id.id, ttype);
        variables.put(id.id, var);
        getVarPtype.put(var, ptype);
        currentStmt = new TSvar(var, currentExpr);
    }

    @Override
    public void visit(PSif s) {
        PExpr e = s.e;
        PStmt s1 = s.s1;
        PStmt s2 = s.s2;

        e.accept(this);

        HashMap<String, Variable> variablesIf = new HashMap<String, Variable>(variables);//create deep copies of HashMap to enter each scope of if statement!!
        HashMap<String, Variable> variablesElse = new HashMap<String, Variable>(variables);

        HashMap<String, Variable> temp = variables;
        variables = variablesIf; //we swap before entering each scope
        s1.accept(this);
        TStmt ts1 = currentStmt;

        variables = variablesElse;
        isElseBlock = true;
        s2.accept(this);
        isElseBlock = false;
        TStmt ts2 = currentStmt;

        variables = temp;//and unswap to move on at the end!
        currentStmt = new TSif(currentExpr, ts1, ts2);
    }

    @Override
    public void visit(PSreturn s) {
        hasReturnStatement = true;
        PExpr pe = s.e;

        if((needsReturnStatement == false && pe != null) || (needsReturnStatement && pe == null)){
            Typing.error(null, "Method in class " + tdclass.c.name + " has wrong type in return statement");
            return;
        }
        if(pe == null){
            currentStmt = new TSreturn(new TEnull());
            return;
        }

        pe.accept(this);
        TDmethod tdmethod = (TDmethod)currentTDecl;
        if(compatibilityTest(currentExpr, tdmethod.m.type) == false){
            System.out.println(currentExpr + " " +  tdmethod.m.type);
            Typing.error(null, "Invalid type for return of " + tdmethod.m.name);
            return;
        }
        currentStmt = new TSreturn(currentExpr);
    }

    @Override
    public void visit(PSblock s) {
        LinkedList<PStmt> l = s.l;
        ListIterator<PStmt> it = l.listIterator();
        currentBlock = getTSblock.get(currentTDecl);
        hasReturnStatement = false;

        System.out.println("VISITING STATEMENT BLOCK " + s + " linked list size = " + l.size() + " needsRet = "  + needsReturnStatement);
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
        System.out.println("END OF STATEMENT BLOCK " + s + " hasRet = " + hasReturnStatement);
        variables.clear();
        if(needsReturnStatement && !hasReturnStatement && !isElseBlock){
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
            Typing.getAtrrClass.put(attribute, tdclass.c);
            tdclass.c.attributes.put(s.x.id, attribute);
        }
    }

    @Override
    public void visit(PDconstructor s) {
        System.out.println("current class = " + tdclass.c.name + "  constructor name = " + s.x.id);
        if(hasConstructor){
            Typing.error(null, "Class " + tdclass.c.name + " constructor must be unique!");
            return;
        }
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
                    
                    parameterNames.add(pparam.x.id);
                }
            }

            //visitor will go into statement block
            currentTDecl = Typing.getTDconstructor.get(tdclass.c);
            needsReturnStatement = false;
            s.s.accept(this);

        }else{//let's add constructor
            System.out.println("FIRST PASS IN VISITOR FOR CONSTRUCTOR " + s.x.id + "  number of params = " + s.l.size());
            if(!tdclass.c.name.equals(s.x.id)){
                Typing.error(null, "Constructor " + s.x.id + " does not match class name " + tdclass.c.name);
                return;
            }
            if(tdclass.c.methods.get(s.x.id) != null){
                Typing.error(null, "Class " + tdclass.c.name + " has duplicate constructor " + s.x.id);
                return;
            }

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
            //Method m = new Method(s.x.id, new TTvoid(), tparams);
            Typing.getTDconstructor.put(ClassesTable.lookup(s.x.id), tdconstructor);
            //Typing.getTDecl.put(m, tdconstructor);
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
                    variables.put(pparam.x.id, var);
                    getVarPtype.put(var, ptype);
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
