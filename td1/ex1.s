    .text
    .globl main
main:
    pushq %rbp
    movq  %rsp, %rbp # aligning %rsp and %rbp at beginning of main
    movq  $format_string, %rdi # pass format string to register %rdi
    movq  $42, %rsi # pass integer argument to register %rsi
    xorq  %rax, %rax # set %rax to zero before calling printf
    call  printf
    movq  $0, %rax # return code
    popq  %rbp
    ret
    .data
format_string:
    .string "n = %d\n" # trailing 0 added by default in .data segment
