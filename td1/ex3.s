    .text
    .globl main
main:
    pushq %rbp
    movq  %rsp, %rbp # aligning %rsp and %rbp at beginning of main
    movq  $15, %rbx # we use register %rbx to store N=15
    xorq  %rdi, %rdi # pass i=0 as first parameter(%rdi) to f
    movq  %rbx, %rsi # pass c=(1<<N)-1 as second parameter(%rsi) to f
    call  f
    movq  $format_string, %rdi # pass format string to register %rdi
    movq  %rax, %rsi # pass return of f call(%rax) to printf 
    xorq  %rax, %rax # we set rax to zero before calling printf
    call  printf
f:
    pushq %r12 # we save callee-save registers onto the stack before f body
    pushq %r13
    pushq %r14
    pushq %r15
    subq  $0x88, %rsp # allocate space for callee frame onto the stack
    movq %rdi, %r10 # lets store i in r10
    movq %rsi, %r11 # lets store c in r11
f_body:
    cmpq  %r10, %rbx # compare N(rbx) and i(r10)
    je i_eq_N
    # now, we need to check memo to see if we computed f(i,c) before or not => we start by computing position we want to access in memo (variable key in the C code)
    xorq %r13, %r13 # we use %r13 for variable key
    movq %rsi, %r13
    shl $4, %r13 # c << L
    addq %rdi, %r13 # key = c<<L + i (equivalent to OR)
    movq (memo, %r13, 1), %r14 # we use %r14 to store memo[key]
    cmpq  $0, %r14 # compare 0 and memo[key](%r14)
    jne ret_memo
    # now we must do the recurrence step of f(i, c)
    xorq %r12, %r12 # we use %r12 to store s
    xorq %r13, %r13 # we use %r13 to store j
f_loop:
    cmpq  %r13, %rbx # compare N(rbx) and j(r13)
    jle end_f_loop # if j >= N we end the loop
    xorq %r14, %r14 # we use %r14 to store col
    addq $1, %r14
    movq %r13, %rcx
    shl %cl, %r14 # 1<<j (shl expects immediate as first operand, except for %cl this is why we move %r13 to %rcx)
    movq %r11, %r15
    andq %r14, %r15 # r15 = c&col
    cmpq $0, %r15
    je prep_next_f_loop
    movq (m, %r13, %r10, 15), %r15 #r15 = m[i][j]
    #we need to call f(i+1, c - col)
    movq %r10, %rdi
    addq $1, %rdi # pass i+1 as parameter %rdi
    movq %r11, %rsi
    subq %r14, %rsi # pass c-col as parameter %rsi
    call f
    addq %rax, %r15  # r15 = m[i][j] + f(i+1, c - col)
    cmpq %r15, %r12 # compare s(%r12) and x(%r15)
    jg x_bg_s # if x > s
    jmp prep_next_f_loop
x_bg_s:
    movq %r15, %r12
    jmp prep_next_f_loop
prep_next_f_loop:
    addq $1, %r13
    jmp f_loop
end_f_loop:
    movq %r12, (memo, %r13) # we make memo[key] = s
    jmp f_end
ret_memo:
    movq %r14, %r12 # if memo[key] != 0, we can return memo[key]
    jmp f_end
i_eq_N:
    xorq %r12, %r12 # if i==N, we set s=0 and return
    jmp f_end
f_end:
    movq  %r12, %rax # store final value of s in register rax for return
    addq  $0x88, %rsp # free callee frame space
    popq  %r15 # pop callee-save registers we pushed onto the stack
    popq  %r14
    popq  %r13
    popq  %r12
    ret
    .data
format_string:
    .string "solution = %d\n"
m:
	.long	7
	.long	53
	.long	183
	.long	439
	.long	863
	.long	497
	.long	383
	.long	563
	.long	79
	.long	973
	.long	287
	.long	63
	.long	343
	.long	169
	.long	583
	.long	627
	.long	343
	.long	773
	.long	959
	.long	943
	.long	767
	.long	473
	.long	103
	.long	699
	.long	303
	.long	957
	.long	703
	.long	583
	.long	639
	.long	913
	.long	447
	.long	283
	.long	463
	.long	29
	.long	23
	.long	487
	.long	463
	.long	993
	.long	119
	.long	883
	.long	327
	.long	493
	.long	423
	.long	159
	.long	743
	.long	217
	.long	623
	.long	3
	.long	399
	.long	853
	.long	407
	.long	103
	.long	983
	.long	89
	.long	463
	.long	290
	.long	516
	.long	212
	.long	462
	.long	350
	.long	960
	.long	376
	.long	682
	.long	962
	.long	300
	.long	780
	.long	486
	.long	502
	.long	912
	.long	800
	.long	250
	.long	346
	.long	172
	.long	812
	.long	350
	.long	870
	.long	456
	.long	192
	.long	162
	.long	593
	.long	473
	.long	915
	.long	45
	.long	989
	.long	873
	.long	823
	.long	965
	.long	425
	.long	329
	.long	803
	.long	973
	.long	965
	.long	905
	.long	919
	.long	133
	.long	673
	.long	665
	.long	235
	.long	509
	.long	613
	.long	673
	.long	815
	.long	165
	.long	992
	.long	326
	.long	322
	.long	148
	.long	972
	.long	962
	.long	286
	.long	255
	.long	941
	.long	541
	.long	265
	.long	323
	.long	925
	.long	281
	.long	601
	.long	95
	.long	973
	.long	445
	.long	721
	.long	11
	.long	525
	.long	473
	.long	65
	.long	511
	.long	164
	.long	138
	.long	672
	.long	18
	.long	428
	.long	154
	.long	448
	.long	848
	.long	414
	.long	456
	.long	310
	.long	312
	.long	798
	.long	104
	.long	566
	.long	520
	.long	302
	.long	248
	.long	694
	.long	976
	.long	430
	.long	392
	.long	198
	.long	184
	.long	829
	.long	373
	.long	181
	.long	631
	.long	101
	.long	969
	.long	613
	.long	840
	.long	740
	.long	778
	.long	458
	.long	284
	.long	760
	.long	390
	.long	821
	.long	461
	.long	843
	.long	513
	.long	17
	.long	901
	.long	711
	.long	993
	.long	293
	.long	157
	.long	274
	.long	94
	.long	192
	.long	156
	.long	574
	.long	34
	.long	124
	.long	4
	.long	878
	.long	450
	.long	476
	.long	712
	.long	914
	.long	838
	.long	669
	.long	875
	.long	299
	.long	823
	.long	329
	.long	699
	.long	815
	.long	559
	.long	813
	.long	459
	.long	522
	.long	788
	.long	168
	.long	586
	.long	966
	.long	232
	.long	308
	.long	833
	.long	251
	.long	631
	.long	107
	.long	813
	.long	883
	.long	451
	.long	509
	.long	615
	.long	77
	.long	281
	.long	613
	.long	459
	.long	205
	.long	380
	.long	274
	.long	302
	.long	35
	.long	805

    .bss
memo:
    .space	3932160
