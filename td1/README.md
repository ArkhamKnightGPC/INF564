# TD 1: x86-64 assembly

## Exercice 1: Call library function *printf*

Compile the following C program:

```
#include <stdio.h>

int main() {
  printf("n = %d\n", 42);
  return 0;
}
```

To call the library function printf, we pass its first argument (the format string) in register %rdi and its second argument (here the integer 42) in register %rsi, as specified by the calling conventions. We must also set register %rax to zero before calling printf, since it is a variadic function (in that case, %rax indicates the number of arguments passed in vector registers).
The format string must be declared in the data segment (.data) using the directive .string that adds a trailing 0-character.

## Exercise 2: Integer Square Root

Compile the following C program:

```
#include <stdio.h>

int isqrt(int n) {
  int c = 0, s = 1;
  while (s <= n) {
    c++;
    s += 2*c + 1;
  }
  return c;
}

int main() {
  int n;
  for (n = 0; n <= 20; n++)
    printf("sqrt(%2d) = %2d\n", n, isqrt(n));
  return 0;
}
```

## Exercise 3: Matrix Sum (Problem 345 from Project Euler)

We will compile the [C solution in this link](https://www.enseignement.polytechnique.fr/informatique/INF564/td/1/matrix.c.html).

This solution uses two main ingredients:

- Generalize the problem to a subset of rows and columns. This subset is defined using two integers i and c : We only consider rows i..14; We only consider columns j for which the bit j in the integer c is 1. (It is an invariant that c has exactly 15-i bits that are set.)
The call f(i, c) returns the maximal sum for the subset defined by i and c.
- We use memoization, to avoid recomputing f(i, c) several times. For this purpose, an array memo is declared. We store the result of f(i, c) at index c << 4 | i, when it is computed, and 0 otherwise.

### Representing the Matrix

In the C program, the matrix m is declared as follows:

```
const int m[N][N] = { { 7, 53, ..., 583 }, { 627, 343, ... }, ... };
```

In the memory layout, integers are stored consecutively, by rows. Each integer is stored on 32 bits and thus the matrix m uses 900 bytes in total. The integer m[i][j] is located at address m + 4 * (15*i + j).

We provide a [file matrix.s](https://www.enseignement.polytechnique.fr/informatique/INF564/td/1/matrix.s) that contains static data for the matrix m, as well as space for the array memo. The latter is initialized with zeros, and thus can be allocated in section .bss so that it does not increase the size of the executable unnecessarily.

### Compiling the program

Compile functions f and main. Regarding function f, we need to allocate registers for local variables key, r, s, etc. If we choose callee-saved registers, we need to restore them before returning. If we choose caller-saved registers, we weed to restore them after a call (if we need their value after the call).

Be careful: to compute 1 << j, one must do a shift whose size is not a constant. To do so, the operand of instruction sal, which is j here, must be placed in the byte register %cl. For this reason, it is a good idea to allocate variable j in register %rcx.