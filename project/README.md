# Project: Mini-Java compiler

The goal of this project (to be developed over six lab sessions) is to build a compiler for a tiny fragment of the Java language, called *Mini Java*, to *x86-64 assembly*. All the details are given in the project statement.

Project submission must be a *tarball* containing

- the sources of your compiler;
- a short report;
- possibly other files, e.g. test files of your own.

**Important**: the project must include an extension of your choice (see the last section of the project statement).

## Code supplied

The parser and the abstract syntax are provided.
- in Java: [mini-java-java.tar.gz](https://www.enseignement.polytechnique.fr/informatique/CSC_52064/td/mini-java/mini-java-java.tar.gz)
- in OCaml: [mini-java-ocaml.tar.gz](https://www.enseignement.polytechnique.fr/informatique/CSC_52064/td/mini-java/mini-java-ocaml.tar.gz)

You have to read and understand the abstract syntax trees (respectively in Syntax.java and ast.ml). They are two kinds of syntax trees:
- **parsed trees** = output of the parser = input of the type checker
- **typed trees** = output of the type checker = input of the compiler

**Note for Java**: a shell script minijava is provided to run your compiler from the command line (useful when testing your compiler; see below).

## Roadmap

- (labs 4-5) Static Typing

This is described in section 2. Code to be written in *Typing.java* or *typing.ml*.
Test with

```
bash ./test -2 path-to-your-compiler
(see below)
```

- (labs 6-7-8-9) Code Generation

This is described in sections 3 and 4. Code to be written in *Compile.java* or *compile.ml*.
A class/module X86_64 is provided, to build and then print x86-64 assembly code.
Test with

```
bash ./test -3 path-to-your-compiler
(see below)
```

## Test Files

Test files are provided, together with a bash script to test your compiler:
- [tests-16-jan.tar.gz](https://www.enseignement.polytechnique.fr/informatique/CSC_52064/tests-16-jan.tar.gz)
