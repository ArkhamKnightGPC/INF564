package mini_java;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

class Compile {

  static boolean debug = false;

  static MyTVisitor myTVisitor;

  static X86_64 ret;

  static X86_64 file(TFile f) {
    myTVisitor = new MyTVisitor();

    ret = new X86_64();
    ret.emit(".globl main"); //define entrypoint for the linker!
    LinkedList<TDClass> l = f.l;

    // 1. build the class descriptors
    ListIterator<TDClass> it = l.listIterator();
    while(it.hasNext()){ // for each class
      TDClass tdClass = it.next();
      Class_ c = tdClass.c;

      if(c.name.equals("Main")){
        continue;
      }

      ret.dlabel("descriptor_" + c.name); // add a labeled block in the data section
      if(c.extends_ != null){// add ref to super class
        ret.quad("descriptor_" + c.extends_.name);
      }else{
        ret.quad(0);
      }
      // now add label refs to each method !!
      for(String methodName : c.methods.keySet()){
        ret.quad(c.name + "_" + methodName);
      }
    }

    // 2. set the offsets of attributes (within objects) and local variables (within the stack)
    it = l.listIterator();
    while(it.hasNext()){
      TDClass tdClass = it.next();
      Class_ c = tdClass.c;

      LinkedList<Class_> superClasses = new LinkedList<Class_>();
      while(c != null){// let's add inherited class attributes first!
        superClasses.add(c); // we will use a stack to do this!!
        c = c.extends_;
      }

      int cummulative_offset = 8;
      HashMap<String, Integer> offsets = new HashMap<String, Integer>();
      while(superClasses.isEmpty() == false){
        c = superClasses.pollLast();
        for(Attribute attribute : c.attributes.values()){
          if(offsets.containsKey(attribute.name)){// reuse super class offset (it must be the same!!!)
            attribute.ofs = offsets.get(attribute.name);
          }else{// we need to set this offset!!
            attribute.ofs = cummulative_offset;
            offsets.put(attribute.name, attribute.ofs);
            cummulative_offset += 8; // add 1 word to offset
          }
        }
      }
      
      c = tdClass.c; //ok now let's do local variables!
      cummulative_offset = 0;
      for(Method method : c.methods.values()){
        LinkedList<Variable> variables = method.params;
        for(Variable variable : variables){
          variable.ofs = cummulative_offset;
          cummulative_offset += 8;
        }
      }

    }

    // 3. compile the body of methods and constructors
    it = l.listIterator();
    while(it.hasNext()){
      TDClass tdClass = it.next();
      myTVisitor.setTDClass(tdClass);
      for(TDecl tdecl : tdClass.l){
        tdecl.accept(myTVisitor);
      }
    }

    return ret;
  }

}
