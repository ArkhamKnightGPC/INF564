# Dev diary

Throughout this project, I used the book *Introduction to Compilers and Language Design* by *Douglas Thain* as a reference (available for free at [compilerbook.org](compilerbook.org)). The comments provided in the code often consists of passages from the book that I found helpful while coding a specific part.

**Table of Contents**

- [Static Type Checking](#static-type-checking)
    - [Step 1: declare all classes and check for uniqueness of classes](#step-1-declare-all-classes-and-check-for-uniqueness-of-classes)
- [Appendix: The Symbol Table](#appendix-the-symbol-table)


## Static Type Checking

As indicated in the project statement, static type checking is implement in 3 steps. By step here we mean a pass through the Parsed File **PFile f**.

- declare all classes and check for uniqueness of classes;
- declare inheritance relations (extends) attributes, constructors, and methods;
- type check the body of constructors and methods

### Step 1: declare all classes and check for uniqueness of classes

The statement says that 

- Classes must be declared only once
- Classes can appear in any order. At any point we can refer to a class which is declared later in the file.

In this first pass, we will add all classes to our symbol table. This way, in the next steps, if a class refers to a class later in the file we will find it in the symbol table (no problem!).

We observe that **this step happens entirely in the global scope table of our symbol table**.

If a same class *name* is used for than one time (violating uniqueness of classes), we can throw an error with the appropriate message here.

### Step 2: declare inheritance relations (extends) attributes, constructors, and methods

The statement says

- a class must inherit from an existing class, **different from String**;
- the inheritance relation must not contain a cycle;
- attributes of a given class must be distinct;
- each class has at most one constructor (no overloading);
- each class has at most one method of a given name (no overloading).
- When a class does not inherit from another class with extends, it implicitly inherits from Object. The class String inherits from Object. The class Object does not inherit from any other class.
-  When no constructor is explicitly declared, an implicit constructor with no parameters is assumed.

### Step 3: type check the body of constructors and methods

We do this step in the pass of the PFile as step 2 because we reuse the Class scope Hash Map in the Symbol Table which will be popped if we leave this scope to perfom a new pass.


## Appendix: The Symbol Table

We will structure our symbol table as a stack of hash tables

- **Inner scope** Table (stack top)
- **Class scope** Table
- **Global scope** Table

This allows a symbol (like x) to exist in multiple scopes without conflict !! As we proceed through the program,

- we will push a new table every time a scope is entered
- and pop a table every time a scope is left

The functions provided for the symbol table, match the Symbol Table API in Thain's book:

- **void scope_enter()** causes a new hash table to be pushed on the top of the stack
- **void scope_exit()** causes topmost hash table to be removed
- **int scope_level()** returns number of hash tables in current stack (helps us know if we are at global scope or not)
- **void scope_bind(name, sym)** adds an entry to the topmost hash table mapping *name* to the symbol *sym*
- **symbol scope_lookup(name)** searches the stack of hash tables from top to bottom, looking for the first entry that matches *name* exactly. If no match is found, it returns *null*.
- **symbol scope_lookup_current(name)** works like *scope_lookup* but only searches the topmost table. Used to determine if a variable has already been defined in the current scope!! 
