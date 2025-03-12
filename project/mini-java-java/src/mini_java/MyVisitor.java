package mini_java;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyVisitor implements Visitor {

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
    protected static String dotAux="";
    protected static Boolean needsReturnStatement, hasReturnStatement;
    protected static Set<String> keywords;
    protected static HashMap<String, Variable> variables;
    protected static HashMap<Variable, PType> getVarPtype;
    protected static boolean stringsInvolved, startingMethodBlock;

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
            }else if(ei instanceof TEvar){
                Variable v = ((TEvar)ei).x;
                return v.ty.getClass() == tt.getClass();
            }else if(ei instanceof TEattr){
                Attribute a = ((TEattr)ei).a;
                return a.ty.getClass() == tt.getClass();
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
            }else if(ei instanceof TEattr){
                Attribute a = ((TEattr)ei).a;
                return a.ty.getClass() == tt.getClass();
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
            }else if(ei instanceof TEattr){
                Attribute a = ((TEattr)ei).a;
                TType tt2 = a.ty;
                if(tt2 instanceof TTclass){
                    Class_ c2 = ((TTclass)tt2).c;
                    while(c2.name.equals(c.name) == false && c2.extends_ != null){
                        c2 = c2.extends_;
                    }
                    boolean isValidDescendant = c2.name.equals(c.name);
                    return isValidDescendant;
                }else{
                    return false;
                }
            }else if(ei instanceof TEvar){
                Variable v = ((TEvar)ei).x;
                TType tt2 = v.ty;
                if(tt2 instanceof TTclass){
                    Class_ c2 = ((TTclass)tt2).c;
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
            }else if(ei instanceof TEcall){
                Method m = ((TEcall)ei).m;
                return m.type.getClass() == tt.getClass();
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
        TType ttypeBinop1 = ttypeBinop;
        TType ttypeUnop1 = ttypeUnop;

        e2.accept(this);
        TExpr te2 = currentExpr;
        TType ttypeBinop2 = ttypeBinop;
        TType ttypeUnop2 = ttypeUnop;

        if(te1 instanceof TEassignAttr){//in case of an assignment, we only care about the resulting expression
            te1 = ((TEassignAttr)te1).e2;
        }else if(te1 instanceof TEassignVar){
            te1 = ((TEassignVar)te1).e;
        }
        if(te2 instanceof TEassignAttr){
            te2 = ((TEassignAttr)te1).e2;
        }else if(te2 instanceof TEassignVar){
            te2 = ((TEassignVar)te2).e;
        }

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
                            || (te2 instanceof TEnull && (te1 instanceof TEvar || te1 instanceof TEattr))
                            || (te1 instanceof TEvar && te2 instanceof TEthis && ((TTclass)((TEvar)te1).x.ty).c.name.equals(tdclass.c.name))
                            || (te2 instanceof TEvar && te1 instanceof TEthis && ((TTclass)((TEvar)te2).x.ty).c.name.equals(tdclass.c.name))
                            || (te1 instanceof TEbinop && compatibilityTest(te2, ttypeBinop1) && ttypeBinop1.getClass()==auxTint.getClass())
                            || (te2 instanceof TEbinop && compatibilityTest(te1, ttypeBinop2) && ttypeBinop2.getClass()==auxTint.getClass())
                            || (te1 instanceof TEunop && compatibilityTest(te2, ttypeUnop1) && ttypeUnop1.getClass()==auxTint.getClass())
                            || (te2 instanceof TEunop && compatibilityTest(te1, ttypeUnop2) && ttypeUnop2.getClass()==auxTint.getClass());
        boolean atLeastOne2 = (compatibilityTest(te1, auxTbool) && compatibilityTest(te2, auxTbool))
                            || ( te1 instanceof TEvar && te2 instanceof TEcst && ((TEvar)te1).x.ty.getClass() == auxTbool.getClass() && ((TEcst)te1).c instanceof Cbool)
                            || ( te2 instanceof TEvar && te1 instanceof TEcst && ((TEvar)te2).x.ty.getClass() == auxTbool.getClass() && ((TEcst)te1).c instanceof Cbool)
                            || (te1 instanceof TEvar && te2 instanceof TEvar && ((TEvar)te1).x.getClass() == ((TEvar)te2).x.getClass() && ((TEvar)te2).x.ty.getClass() == auxTbool.getClass())
                            || (te1 instanceof TEattr && te2 instanceof TEattr && ((TEattr)te1).a.ty.getClass() == ((TEattr)te2).a.ty.getClass())
                            || (te1 instanceof TEattr && te2 instanceof TEvar && ((TEattr)te1).a.ty.getClass() == ((TEvar)te2).x.ty.getClass())
                            || (te2 instanceof TEattr && te1 instanceof TEvar && ((TEattr)te2).a.ty.getClass() == ((TEvar)te1).x.ty.getClass())
                            || (te1 instanceof TEbinop && compatibilityTest(te2, ttypeBinop1) && ttypeBinop1.getClass()==auxTbool.getClass())
                            || (te2 instanceof TEbinop && compatibilityTest(te1, ttypeBinop2) && ttypeBinop2.getClass()==auxTbool.getClass())
                            || (te1 instanceof TEunop && compatibilityTest(te2, ttypeUnop1) && ttypeUnop1.getClass()==auxTbool.getClass())
                            || (te2 instanceof TEunop && compatibilityTest(te1, ttypeUnop2) && ttypeUnop2.getClass()==auxTbool.getClass());
        boolean atLeastOne3 = (compatibilityTest(te1, auxTint) && compatibilityTest(te2, auxTint))
                            || ( te1 instanceof TEvar && te2 instanceof TEcst && ((TEvar)te1).x.ty.getClass() == auxTint.getClass() && ((TEcst)te2).c instanceof Cint)
                            || ( te2 instanceof TEvar && te1 instanceof TEcst && ((TEvar)te2).x.ty.getClass() == auxTint.getClass() && ((TEcst)te1).c instanceof Cint)
                            || (te1 instanceof TEvar && te2 instanceof TEvar && ((TEvar)te1).x.getClass() == ((TEvar)te2).x.getClass() && ((TEvar)te2).x.ty.getClass() == auxTint.getClass())
                            || (te1 instanceof TEattr && te2 instanceof TEattr && ((TEattr)te1).a.ty.getClass() == ((TEattr)te2).a.ty.getClass())
                            || (te1 instanceof TEattr && te2 instanceof TEvar && ((TEattr)te1).a.ty.getClass() == ((TEvar)te2).x.ty.getClass())
                            || (te2 instanceof TEattr && te1 instanceof TEvar && ((TEattr)te2).a.ty.getClass() == ((TEvar)te1).x.ty.getClass())
                            || (te1 instanceof TEbinop && compatibilityTest(te2, ttypeBinop1) && ttypeBinop1.getClass()==auxTint.getClass())
                            || (te2 instanceof TEbinop && compatibilityTest(te1, ttypeBinop2) && ttypeBinop2.getClass()==auxTint.getClass())
                            || (te1 instanceof TEunop && compatibilityTest(te2, ttypeUnop1) && ttypeUnop1.getClass()==auxTint.getClass())
                            || (te2 instanceof TEunop && compatibilityTest(te1, ttypeUnop2) && ttypeUnop2.getClass()==auxTint.getClass());
        stringsInvolved = ((te1 instanceof TEcst && ((TEcst)te1).c instanceof Cstring) || (te2 instanceof TEcst && ((TEcst)te2).c instanceof Cstring) && !(te1 instanceof TEcst && ((TEcst)te1).c instanceof Cbool) && !(te2 instanceof TEcst && ((TEcst)te2).c instanceof Cbool))
                                || (te1 instanceof TEbinop && compatibilityTest(te1, stringType))
                                || (te2 instanceof TEbinop && compatibilityTest(te2, stringType))
                                || (te1 instanceof TEvar && ((TEvar)te1).x.ty.getClass() == stringType.getClass())
                                || (te2 instanceof TEvar && ((TEvar)te2).x.ty.getClass() == stringType.getClass())
                                || (te1 instanceof TEattr && ((TEattr)te1).a.ty.getClass() == stringType.getClass())
                                || (te2 instanceof TEattr && ((TEattr)te2).a.ty.getClass() == stringType.getClass())
                                || (te1 instanceof TEcall && ((TEcall)te1).m.type.getClass() == stringType.getClass())
                                || (te2 instanceof TEcall && ((TEcall)te2).m.type.getClass() == stringType.getClass());
        switch(op){ //let's check if the expression types are ok!
            case Badd:
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
                ttypeBinop = new TTint();
                if(!atLeastOne3){
                    Typing.error(null, "SUB operations need two integer operands");
                    return;
                }
                break;
            case Bmul:
                ttypeBinop = new TTint();
                if(!atLeastOne3){
                    Typing.error(null, "MUL operations need two integer operands");
                    return;
                }
                break;
            case Bdiv:
                ttypeBinop = new TTint();
                if(!atLeastOne3){
                    Typing.error(null, "DIV operations need two integer operands");
                    return;
                }
                break;
            case Bmod:
                ttypeBinop = new TTint();
                if(!atLeastOne3){
                    Typing.error(null, "MOD operations need two integer operands");
                    return;
                }
                break;
            case Beq:
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "EQ TEST needs two compatible operands");
                    return;
                }
                break;
            case Bneq:
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "NEQ TEST needs two compatible operands");
                    return;
                }
                break;
            case Blt:
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "LT operations need two integer operands");
                    return;
                }
                break;
            case Ble:
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "LE operations need two integer operands");
                    return;
                }
                break;
            case Bgt:
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "GT operations need two integer operands");
                    return;
                }
                break;
            case Bge:
                ttypeBinop = new TTboolean();
                if(!atLeastOne){
                    Typing.error(null, "GE operations need two integer operands");
                    return;
                }
                break;
            case Band:
                ttypeBinop = new TTboolean();
                if(!atLeastOne2){
                    Typing.error(null, "AND operations need two boolean operands");
                    return;
                }
                break;
            case Bor:
                ttypeBinop = new TTboolean();
                if(!atLeastOne2){
                    Typing.error(null, "OR operations need two boolean operands");
                    return;
                }
                break;
            case Badd_s:
                ttypeBinop = new TTclass(Typing.StringClass);
                break;
        }
        currentExpr = new TEbinop(typedOp, te1, te2);
    }

    @Override
    public void visit(PEunop e) {
        Unop op = e.op; Unop typedOp = op;
        PExpr pe = e.e;

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

        pe.accept(this);
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

        if(!isIdentifierOk(id.id)){
            Typing.error(id.loc, "Identifier " + id.id + " violates identifier syntax");
            return;
        }
        if(keywords.contains(id.id)){// check if name is a keyword
            Typing.error(id.loc, "Identifier " + id.id + " is a reserved keyword");
            return;
        }

        if(dotAux == ""){
            boolean found = findParam(currentTDecl, id);

            if(!found){

                if(variables.containsKey(id.id)){
                    currentExpr = new TEvar(variables.get(id.id));
                }else if(tdclass.c.attributes.containsKey(id.id)){
                    currentExpr = new TEattr(new TEthis(), tdclass.c.attributes.get(id.id));
                }else{
                    Typing.error(id.loc, "Variable/attribute " + id.id + " does not exist");
                    return;
                }

            }//if found is true, new currentExpr is created inside findParam function

        }else{

            if(id.id.equals("System") && (dotAux.equals("print") || dotAux.equals("out"))){//Expression here is the caller for print function
                dotAux = "System_out";
                currentExpr = new TEthis();//let's ignore this later and treat print as a special case
                return;
            }

            String callerClass = id.id;
            if(ClassesTable.classesTable.containsKey(callerClass) == false){
                //ok, so it is a variable attribute in this case
                if(variables.containsKey(id.id)){//dotAux is an attribute of this Variable
                    Variable v = variables.get(id.id);
                    PType pt = getVarPtype.get(v);
                    Class_ class_;
                    if(pt instanceof PTident){
                        class_ = ClassesTable.lookup(((PTident)pt).x.id);
                        Attribute a = class_.attributes.get(dotAux);
                        currentExpr = new TEattr(new TEvar(v), a);
                    }else{
                        Typing.error(id.loc, "call to " + id.id + " not valid");
                        return;
                    }
                }else{
                    Typing.error(id.loc, "Variable " + id.id + " does not exist");
                }
                dotAux = "";
                return;
            }
            Class_ c = ClassesTable.lookup(id.id);
            if(c.attributes.containsKey(dotAux)){
                Attribute res = c.attributes.get(dotAux);
                currentExpr = new TEvar(new Variable(res.name, res.ty));
                dotAux = "";
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

        if(variables.containsKey(id.id)){// we check local variable first!
            Variable v = variables.get(id.id);
            TEassignVar aux = new TEassignVar(v, currentExpr);
            currentExpr = aux;
        }else if (tdclass.c.attributes.containsKey(id.id)) {//we are making assign to attribute
            Attribute a = tdclass.c.attributes.get(id.id);

            if(compatibilityTest(currentExpr, a.ty) == false){
                Typing.error(id.loc, "Invalid type for assignment of attribute " + id.id);
                return;
            }

            TEassignAttr aux = new TEassignAttr(new TEthis(), a, currentExpr);
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
        if(dotAux.equals("")){
            dotAux = id.id;
        }else{
            dotAux = id.id + "_" + dotAux;
        }
        pe.accept(this);
    }

    @Override
    public void visit(PEassignDot e) {//unline PEassign, here we are sure it is an attribute!
        PExpr e1 = e.e1;
        Ident id = e.id;
        PExpr e2 = e.e2;

        e1.accept(this);
        TExpr aux = currentExpr; //this must be an object of some class!!
        Attribute a = null;//and we need to find it!
        TType tv = null;
        if(aux instanceof TEvar){
            tv = ((TEvar)aux).x.ty;
        }else if(aux instanceof TEattr){//an attribute of an attribute of ... of some class
            tv = ((TEattr)aux).a.ty;
        }else{
            Typing.error(id.loc, "Expression does not identify to an Object!");
            return;
        }
        if(tv instanceof TTclass){
            Class_ c = ((TTclass)tv).c;
            if(c.attributes.containsKey(id.id)){
                a = c.attributes.get(id.id);
            }else{
                Typing.error(id.loc, "Attribute " + id.id + " does not exist in class " + c.name);
                return;
            }
        }
        
        e2.accept(this);
        TExpr aux2 = currentExpr;//can be anything...
        if(!compatibilityTest(aux2, a.ty)){//as long as it is compatible with the attribute type!
            Typing.error(id.loc, "Invalide type for assignment of attribute " + id.id);
            return;
        }
        
        currentExpr = new TEassignAttr(aux, a, aux2);
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
        stringsInvolved = false;
        LinkedList<TExpr> methodParams = new LinkedList<TExpr>();

        dotAux = "";
        pe.accept(this);

        Method m; boolean isPrint = false;
        if(dotAux.equals("System_out")){//let's make an exception for print...
            LinkedList<Variable> mv = new LinkedList<Variable>();
            mv.add(new Variable("printArg", new TTclass(Typing.StringClass)));
            m = new Method("System_out_print", new TTvoid(), mv);
            isPrint = true;
            dotAux = "";
        }else{//all other methods, we have to find in some of our other classes!!
            Class_ class_ = ClassesTable.lookup(dotAux);
            if(class_ == null){ // x.insert() -> it's not directly the class, we need to get it from variable type
                if(currentExpr instanceof TEattr){
                    Attribute a  = ((TEattr)currentExpr).a;
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
                }else if(currentExpr instanceof TEthis){
                    if(tdclass.c.methods.containsKey(id.id)){
                        class_ = tdclass.c;
                    }else{
                        Typing.error(id.loc, "class " + tdclass.c.name + " does not have method " + id.id);
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

        if(l.size() != m.params.size()){
            Typing.error(id.loc, "Number of arguments in function " + dotAux + "_" + id.id + " is incorrect!");
            return;
        }
        ListIterator<PExpr> it = l.listIterator();
        ListIterator<Variable> it2 = m.params.listIterator();
        while(it.hasNext()){
            PExpr ei = it.next();
            ei.accept(this);
            Variable ej = it2.next();

            boolean printErrorCriteria = !(currentExpr instanceof TEcst || ttypeBinop instanceof TTboolean || ttypeBinop instanceof TTint);
            if(isPrint && printErrorCriteria && !stringsInvolved){
                Typing.error(id.loc, "Wrong argument type for call of Print function ");
                return;
            }else if(!compatibilityTest(currentExpr, ej.ty)){
                Typing.error(id.loc, "Wrong argument type for call of function " + dotAux + "_" + id.id);
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

        ptype.accept(this);

        if(pexpr == null){//variable is not initialized!!
            currentExpr = new TEnull();
        }else{
            pexpr.accept(this);
            if(!compatibilityTest(currentExpr, ttype)){
                Typing.error(id.loc, "Variable " + id.id + " not compatible with expression type");
                return;
            }
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
        TExpr ifCondition = currentExpr;

        HashMap<String, Variable> variablesIf = new HashMap<String, Variable>(variables);//create deep copies of HashMap to enter each scope of if statement!!
        HashMap<String, Variable> variablesElse = new HashMap<String, Variable>(variables);
        HashMap<String, Variable> temp = variables;

        boolean hasReturnStatementBackup = hasReturnStatement;
        boolean ifHasReturnStatement, elseHasReturnStatement;

        variables = variablesIf; //we swap before entering each scope
        hasReturnStatement = false;
        s1.accept(this);
        ifHasReturnStatement = hasReturnStatement;
        TStmt ts1 = currentStmt;

        variables = variablesElse;
        hasReturnStatement = false;
        s2.accept(this);
        elseHasReturnStatement = hasReturnStatement;
        TStmt ts2 = currentStmt;

        variables = temp;//and unswap to move on at the end!
        hasReturnStatement = hasReturnStatementBackup || (ifHasReturnStatement && elseHasReturnStatement); //both paths must have returns!!
        currentStmt = new TSif(ifCondition, ts1, ts2);
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
        if(!compatibilityTest(currentExpr, tdmethod.m.type)){
            Typing.error(null, "Invalid type for return of " + tdmethod.m.name);
            return;
        }
        currentStmt = new TSreturn(currentExpr);
    }

    @Override
    public void visit(PSblock s) {
        LinkedList<PStmt> l = s.l;
        ListIterator<PStmt> it = l.listIterator();

        TSblock subBlock = new TSblock();
        hasReturnStatement = false;

        HashMap<String, Variable> variablesBackup = new HashMap<String, Variable>(variables); //deep copy of scope variable so we can modify it inside block
        while(it.hasNext()){
            PStmt st = it.next();
            st.accept(this); //we call visitor on each statement in the block
            subBlock.l.add(currentStmt);
        }
        currentStmt = subBlock;
        variables = variablesBackup; //we reset variables to state before the block!!
    }

    @Override
    public void visit(PSfor s) {
        PStmt initStmt = s.s1;
        PExpr loopCondition = s.e;
        PStmt endOfIterStmt = s.s2;
        PStmt loopBody = s.s3;

        initStmt.accept(this);
        TStmt iniStmt_Typed = currentStmt;
        loopCondition.accept(this);
        TExpr loopCondition_Typed = currentExpr;
        if(!compatibilityTest(loopCondition_Typed, new TTboolean())){
            Typing.error(null, "Condition in for loop must be of type boolean!");
            return;
        }
        endOfIterStmt.accept(this);
        TStmt endOfIterStmt_Typed = currentStmt;

        if(loopCondition_Typed instanceof TEcst && ((TEcst)loopCondition_Typed).c instanceof Cbool && !((Cbool)((TEcst)loopCondition_Typed).c).b){//corner case: unreachable code!!
            currentStmt = new TSfor(loopCondition_Typed, iniStmt_Typed, endOfIterStmt_Typed, new TSblock());
        }else{
            HashMap<String, Variable> variablesBackup = new HashMap<String, Variable>(variables);
            loopBody.accept(this);
            variables = variablesBackup;
            TStmt loopBodyStmt_Typed = currentStmt;
            currentStmt = new TSfor(loopCondition_Typed, iniStmt_Typed, endOfIterStmt_Typed, loopBodyStmt_Typed);
        }
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
        if(hasConstructor){
            Typing.error(null, "Class " + tdclass.c.name + " constructor must be unique!");
            return;
        }
        hasConstructor = true;
        if(goIntoBody){//let's analyse parameters and block of statements!!

            HashMap<String, Variable> variablesBackup = new HashMap<String, Variable>(variables);

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
                    variables.put(pparam.x.id, var);
                    getVarPtype.put(var, ptype);
                }
            }

            //visitor will go into statement block
            currentTDecl = Typing.getTDconstructor.get(tdclass.c);
            needsReturnStatement = false;
            s.s.accept(this);

            currentBlock = getTSblock.get(currentTDecl);
            currentBlock.l.add(currentStmt);

            variables = variablesBackup;

        }else{//let's add constructor
            
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
            Typing.getTDconstructor.put(ClassesTable.lookup(s.x.id), tdconstructor);
        }
    }

    @Override
    public void visit(PDmethod s) {
        if(goIntoBody){//let's analyse parameters, block of statements

            HashMap<String, Variable> variablesBackup = new HashMap<String, Variable>(variables);

            LinkedList<PParam> l = s.l;
            Method method = tdclass.c.methods.get(s.x.id);

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
            needsReturnStatement = !(method.type instanceof TTvoid);
            currentTDecl = Typing.getTDecl.get(method);
            s.s.accept(this);
            currentBlock = getTSblock.get(currentTDecl);
            currentBlock.l.add(currentStmt);
            //check if return statement was found here!!
            if(needsReturnStatement && !hasReturnStatement){
                Typing.error(null, "Method is missing a return statement");
                return;
            }
            variables = variablesBackup;

        }else{//let's add method
            if(tdclass.c.methods.get(s.x.id) != null){
                Typing.error(null, "Class " + tdclass.c.name + " has duplicate methods " + s.x.id);
                return;
            }
            if(s.ty != null){
                s.ty.accept(this);
            }else{
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
