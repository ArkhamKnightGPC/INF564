package mini_java;

/** Visitor for the parsed trees

   (feel free to modify it for your needs) */
    interface Visitor {
    void visit(PTboolean t);
    void visit(PTint t);
    void visit(PTident t);

    void visit(Cbool c);
    void visit(Cstring c);
    void visit(Cint c);

    void visit(PEcst e);
    void visit(PEbinop e);
    void visit(PEunop e);
    void visit(PEthis e);
    void visit(PEnull e);
    void visit(PEident e);
    void visit(PEassignIdent e);
    void visit(PEdot e);
    void visit(PEassignDot e);
    void visit(PEnew e);
    void visit(PEcall e);
    void visit(PEcast e);
    void visit(PEinstanceof e);

    void visit(PSexpr s);
    void visit(PSvar s);
    void visit(PSif s);
    void visit(PSreturn s);
    void visit(PSblock s);
    void visit(PSfor s);

    void visit(PDattribute s);
    void visit(PDconstructor s);
    void visit(PDmethod s);
}