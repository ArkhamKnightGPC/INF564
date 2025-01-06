    .text
    .globl main
main:
    pushq %rbp
    movq  %rsp, %rbp # aligning %rsp and %rbp at beginning of main
    movq  $0, %rbx # we use register %rbx to store n
    movq  $20, %r12 # we store constant 20 in %rcx
main_loop:
    cmpq  %r12, %rbx # compare n(rbx) and 20(r12)
    jnle  end_loop # if n>20 => break loop!
    movq  %rbx, %rdi # pass n as first parameter to isqrt 
    call  isqrt
    movq  $format_string, %rdi # pass format string to register %rdi
    movq  %rbx, %rsi # pass n as first parameter to printf 
    movq  %rax, %rdx # pass isqrt(n) returned value as second parameter to printf
    xorq  %rax, %rax # we set rax to zero before calling printf
    call  printf
    addq  $1, %rbx # n++ before next iteration
    jmp   main_loop # go to start of loop body
end_loop:
    movq  $0, %rax # return code
    popq  %rbp
    ret
isqrt:
    pushq %r12 # we push callee-save registers onto the stack before calling isqrt
    pushq %r13
    pushq %r14
    subq  $0x88, %rsp # allocate space for callee frame onto the stack
    movq  $0, %r12 # we use register %r12 to store c
    movq  $1, %r13 # we use register %r13 to store s
isqrt_loop:
    cmpq  %rdi, %r13 # compare s(r13) and n(rdi)
    jnbe  end_isqrt_loop # if s > n => break loop!
    addq  $1, %r12 # c++
    xorq  %r14, %r14 # we will use r14 for intermediary calculations
    addq  %r12, %r14
    addq  %r12, %r14
    addq  $1, %r14 # r14 now stores 2*c + 1
    addq  %r14, %r13 # s += 2*c + 1
    jmp   isqrt_loop
end_isqrt_loop:
    movq  %r12, %rax # store final value of c in register rax for return
    addq  $0x88, %rsp # free callee frame space
    popq  %r14 # pop callee-save registers we pushed onto the stack before call
    popq  %r13
    popq  %r12
    ret
    .data
format_string:
    .string "sqrt(%2d) = %2d\n"
