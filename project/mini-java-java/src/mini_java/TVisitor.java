package mini_java;


/* visitor for the typed trees
   (feel free to modify it for your needs) */

   interface TVisitor {
    void visit(TTvoid t);
    void visit(TTnull t);
    void visit(TTboolean t);
    void visit(TTint t);
    void visit(TTclass t);
  
    void visit(Cbool c);
    void visit(Cstring c);
    void visit(Cint c);
  
    void visit(TEcst e);
    void visit(TEbinop e);
    void visit(TEunop e);
    void visit(TEthis e);
    void visit(TEnull e);
    void visit(TEvar e);
    void visit(TEassignVar e);
    void visit(TEattr e);
    void visit(TEassignAttr e);
    void visit(TEnew e);
    void visit(TEcall e);
    void visit(TEcast e);
    void visit(TEinstanceof e);
    void visit(TEprint e);
  
    void visit(TSexpr s);
    void visit(TSvar s);
    void visit(TSif s);
    void visit(TSreturn s);
    void visit(TSblock s);
    void visit(TSfor s);
  
    void visit(TDconstructor d);
    void visit(TDmethod d);
  }
  