package mini_java;

import java.util.HashMap;
import java.util.Stack;

public class SymbolTable{
  /* we will structure our symbol table as a stack of hash tables
    *        - Inner scope Table (stack top)
    *        - Class scope Table
    *        - Global scope Table
    * this allows a symbol (like x) to exist in multiple scopes without conflict !!
    * As we proceed through the program,
    *         - we will push a new table every time a scope is entered
    *         - and pop a table every time a scope is left
  */

  private static Stack<HashMap<Ident, Symbol>> scopes = new Stack<>();
  private static int level = 0;

  SymbolTable(){
    //constructor will initialize global scope
    scopes.push(new HashMap<Ident, Symbol>());
  }

  void scope_enter(){
    scopes.push(new HashMap<Ident, Symbol>());
    level++;
  }

  void scope_exit(){
    if(level > 0){
      scopes.pop();
      level--;
    }
  }

  int scope_level(){
    return level;
  }

  void scope_bind(Ident name, Symbol sym){
    if(!scopes.isEmpty()){
      scopes.peek().put(name, sym);
    }
  }

  Symbol scope_lookup(Ident name){
    for(int i = scopes.size()-1; i >= 0; i--){
      Symbol sym = scopes.get(i).get(name);
      if(sym != null){
        return sym;
      }
    }
    return null; // if element is not found in any scope :(
  }

  Symbol scope_look_current(Ident name){
    if(!scopes.isEmpty()){
      return scopes.peek().get(name);
    }
    return null; // if element is not in current scope
  }
}
