# README: Project details

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

In this first pass, we will add all classes to a HashMap called **ClassesTable**. This way, in the next steps, if a class refers to a class later in the file we will find it in ClassesTable (no problem!).

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

...

## Appendix: The Symbol Table

In the global scope, we have a hash map for class.

Each object of class **Class_** will have HashMaps for attributes and methods(including a constructor).

Inside a local scope, we will need another HashMap for local variables.