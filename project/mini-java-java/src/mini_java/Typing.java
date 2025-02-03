package mini_java;

import java.util.LinkedList;
import java.util.ListIterator;


class Typing {

  static boolean debug = false;

  // use this method to signal typing errors
  static void error(Location loc, String msg) {
    String l = loc == null ? " <no location>" : " " + loc;
    throw new Error(l + "\nerror: " + msg);
  }

  static Ident getPDeclName(PDecl pdecl){
    if(pdecl instanceof PDattribute){
      return ((PDattribute)pdecl).x;
    }else if(pdecl instanceof PDconstructor){
      return ((PDconstructor)pdecl).x;
    }else if(pdecl instanceof PDmethod){
      return ((PDmethod)pdecl).x;
    }
    return null;
  }
  

  static TFile file(PFile f) {

    LinkedList<TDClass> typed_classes = new LinkedList<TDClass>();
    SymbolTable symbolTable = new SymbolTable();
    SymbolTableVisitor myVisitor = new SymbolTableVisitor();
    
    // 1. declare all classes and check for uniqueness of classes
    ListIterator<PClass> it = f.l.listIterator();
    while(it.hasNext()){
      PClass pclass = it.next();
      Ident className = pclass.name;
      if(symbolTable.scope_look_current(className) != null){
        //class has been declared twice => error!
        error(className.loc, "Class name " + className.id + " is used for more than one class");
        return null;
      }
      Class_ cclass = new Class_(className.id);
      TTclass tclass = new TTclass(cclass);

      Symbol symbol = new Symbol(Symbol_type.SYMBOL_GLOBAL_SCOPE, tclass, className, symbolTable.scope_level());

      symbolTable.scope_bind(className, symbol);
    }

    // 2. declare inheritance relations (extends) attributes, constructors, and methods
    it = f.l.listIterator();
    while(it.hasNext()){
      PClass pclass = it.next();
      Ident className = pclass.name;
      Ident fatherName = pclass.ext;
      Symbol fatherSymbol = null;
      LinkedList<PDecl> classDecl = pclass.l;

      Symbol pclass_symbol = symbolTable.scope_look_current(className); //after first step, we know we have something here!
      if(fatherName != null){//this class inherits from someone!
        fatherSymbol = symbolTable.scope_look_current(fatherName);
        if(fatherSymbol == null){
          //class is inheriting from a non-existing class!
          error(className.loc, "Class " + className.id + " inherits from non-existing class");
          return null;
        }
      }

      //let's add attributes, constructor and methods to hash table
      symbolTable.scope_enter();//we're going into the Class scope
      myVisitor.go_into_body_FALSE(); //visitor will NOT enter body of constructors and methos
      ListIterator<PDecl> it2 = classDecl.listIterator();
      while(it2.hasNext()){
        PDecl pdecl = it2.next();
        pdecl.accept(myVisitor);
        Symbol symbol = myVisitor.get_result();
        symbolTable.scope_bind(getPDeclName(pdecl), symbol);
      }

      // 3. type check the body of constructors and methods.
      it2 = classDecl.listIterator();
      myVisitor.go_into_body_TRUE(); //visitor will enter body of constructors and methods
      while(it2.hasNext()){
        // we must make this step inside step 2, because when we leave this scope, hash table will be popped!
        PDecl pdecl = it2.next();
        pdecl.accept(myVisitor);
        // ok... maybe we'll have problems using vistors here :( we will see!
      }
      symbolTable.scope_exit();
    }

    return new TFile(typed_classes);
  }

}
