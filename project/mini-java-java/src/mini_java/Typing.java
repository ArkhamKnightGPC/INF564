package mini_java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;


class Typing {

  static boolean debug = false;

  static LinkedList<TDClass> typedClasses;
  static HashMap<Class_, TDClass> getTDclass = new HashMap<Class_, TDClass>();
  static HashMap<Class_, TDconstructor> getTDconstructor = new HashMap<Class_, TDconstructor>();
  static HashMap<Method, TDecl> getTDecl = new HashMap<Method, TDecl>();
  static HashMap<Attribute, Class_> getAtrrClass = new HashMap<Attribute, Class_>();
  static Class_ IntegerClass = new Class_("String");
  static Class_ StringClass = new Class_("String");

  // use this method to signal typing errors
  static void error(Location loc, String msg) {
    String l = (loc == null) ? " <no location>" : " " + loc;
    throw new Error(l + "\nerror: " + msg);
  }

  static TFile file(PFile f) {

    typedClasses = new LinkedList<TDClass>();

    MyVisitor myVisitor = new MyVisitor();
    InheritanceDAG inheritanceDAG = new InheritanceDAG();
    ClassesTable.init();
    ClassesTable.add("Object", new Class_("Object"));
    ClassesTable.add("Integer", IntegerClass);
    ClassesTable.add("String", StringClass);

    Variable equalsParam = new Variable("equalsParam", new TTclass(StringClass));
    LinkedList<Variable> equalsParams = new LinkedList<Variable>();
    equalsParams.add(equalsParam);
    StringClass.methods.put("equals", new Method("equals", new TTboolean(), equalsParams));
    
    // 1. declare all classes and check for uniqueness of classes
    ListIterator<PClass> it = f.l.listIterator();
    while(it.hasNext()){
      PClass pclass = it.next(); //each parsed class will be converted into a typed class
      Ident className = pclass.name;
      if(ClassesTable.lookup(className.id) != null){
        //class has been declared twice => error!
        error(className.loc, "Class name " + className.id + " is used for more than one class");
        return null;
      }
      Class_ class_ = new Class_(className.id);
      ClassesTable.add(className.id, class_);

      TDClass tdclass = new TDClass(class_, new LinkedList<TDecl>());
      typedClasses.add(tdclass);//we declare this class, uniqueness was checked :)
      getTDclass.put(class_, tdclass);
      inheritanceDAG.addNode(class_);
    }

    // 2. declare inheritance relations (extends_) attributes, constructors, and methods
    HashMap<Class_, LinkedList<PDecl>> classDecls = new HashMap<Class_, LinkedList<PDecl>>();
    it = f.l.listIterator();
    while(it.hasNext()){
      PClass pclass = it.next();

      Ident className = pclass.name;
      Ident fatherClassName = pclass.ext;
      Class_ fatherClass_ = null;
      LinkedList<PDecl> classDecl = pclass.l;

      Class_ class_ = ClassesTable.lookup(className.id); //after step 1, we know class_ is not null!
      classDecls.put(class_, classDecl);

      if(fatherClassName != null){//this class inherits from another one!!
        fatherClass_ = ClassesTable.lookup(fatherClassName.id);
        if(fatherClass_ == null){
          //class is inheriting from a non-existing class!
          error(className.loc, "Class " + className.id + " inherits from non-existing class " + fatherClassName.id);
          return null;
        }
        class_.extends_ = fatherClass_; //declare inheritance relation (TDclass in linked list should point to this updated object)
        inheritanceDAG.addEdge(fatherClass_, class_);
      }
    }

    //we want to process parent classes before classes that inherit them!!
    // => we use toposort!
    ArrayList<Class_> sortedClasses = inheritanceDAG.topoSort();

    if(sortedClasses.size() < typedClasses.size()){
      //cycle was found during toposort!
      error(null, "Inheritance relations have a cycle!!");
      return null;
    }

    for(Class_ class_ : sortedClasses){
      System.out.println("CLASS " + class_.name);
      LinkedList<PDecl> classDecl = classDecls.get(class_);

      LinkedList<Class_> superClasses = new LinkedList<Class_>();
      Class_ c = class_.extends_;
      while(c != null){// we will use a stack to consider inherited classes first
        superClasses.add(c); // because of toposort we know they are already processed!!
        c = c.extends_;
      }

      while(superClasses.isEmpty() == false){
        c = superClasses.pollLast();
        for(Attribute attribute : c.attributes.values()){
          class_.attributes.put(attribute.name, attribute);
          getAtrrClass.put(attribute, class_);
        }
        for(Method method : c.methods.values()){
          class_.methods.put(method.name, method);
        }
      }

      TDClass currenTDclass = getTDclass.get(class_);
      //to handle overriding, add inherited methods to AST here!!
      for(Method method : class_.methods.values()){
        currenTDclass.l.add(getTDecl.get(method));
      }

      //let's add OUR OWN attributes, constructor and methods to hash table
      MyVisitor.setClass_(currenTDclass);
      MyVisitor.hasConstructor = false;

      MyVisitor.goIntoBodyFALSE(); //visitor will NOT enter body of constructors and methos
      ListIterator<PDecl> it2 = classDecl.listIterator();
      while(it2.hasNext()){
        PDecl pdecl = it2.next();
        pdecl.accept(myVisitor);
      }

      //we check if a constructor was declared, if not we give a default constructor to this class
      if(MyVisitor.hasConstructor() == false){
        TDconstructor tdconstructor = new TDconstructor(new LinkedList<Variable>(), new TSblock());
        getTDconstructor.put(class_, tdconstructor);
        currenTDclass.l.add(tdconstructor);
      }
    }

    // 3. type check the body of constructors and methods
    for(Class_ class_ : sortedClasses){
      System.out.println("CLASS " + class_.name);
      LinkedList<PDecl> classDecl = classDecls.get(class_);
      TDClass currenTDclass = getTDclass.get(class_);
      MyVisitor.setClass_(currenTDclass);

      ListIterator<PDecl> it2  = classDecl.listIterator();
      MyVisitor.goIntoBodyTRUE(); //visitor will enter body of constructors and methods
      while(it2.hasNext()){
        PDecl pdecl = it2.next();
        pdecl.accept(myVisitor);
      }
    }

    return new TFile(typedClasses);
  }

}
