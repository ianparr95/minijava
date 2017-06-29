.data
Fac$$: .quad 0 #no superclass

.text

.text
.global _asm_main

_asm_main:
   pushq %rbp
   movq %rsp,%rbp
   # starting print statement
   movsd  double$$$1(%rip),%xmm0
   movq %xmm0,%rax
   movq %rax,%rdi
   call putd
   # done with print statement
   movq %rbp,%rsp
   popq %rbp
   ret
.data
double$$$1:  .double  1.5
