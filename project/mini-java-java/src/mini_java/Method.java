package mini_java;

import java.util.LinkedList;

/** Method */
public class Method {
    final String name;
    final TType type;
    final LinkedList<Variable> params;
  
    Method(String name, TType type, LinkedList<Variable> params) {
      this.name = name;
      this.type = type;
      this.params = params;
    }
  }