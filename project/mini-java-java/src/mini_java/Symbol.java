package mini_java;

public class Symbol{
  TType type; // value (static type)
  Ident name; // key

  Symbol(TType type, Ident name){
    this.type = type;
    this.name = name;
  }
}
