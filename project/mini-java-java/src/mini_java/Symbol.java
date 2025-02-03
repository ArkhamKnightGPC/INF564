package mini_java;

public class Symbol{
  Symbol_type kind; //indicates whether the symbol is a local variable, a global variable or a function parameter
  TType type; //static typing
  Ident name;
  int which; //ordinal position of local variables and parameters

  Symbol(Symbol_type kind, TType type, Ident name, int which){
    this.kind = kind;
    this.type = type;
    this.name = name;
    this.which = which;
  }
}
