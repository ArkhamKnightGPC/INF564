package mini_java;

import java.util.HashMap;

public class ClassesTable{

  public static HashMap<String, Class_> classesTable;

  static void init(){//initialize HashMap for the desired scope
    classesTable = new HashMap<String, Class_>();
  }

  static void add(String name, Class_ class_){
    classesTable.put(name, class_);
  }

  static Class_ lookup(String name){
    return classesTable.get(name);
  }
}
