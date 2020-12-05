          % processing function 
multintegerinteger                 sw multintegerintegerlink(r0),r15
          % processing: multtmul0 := multintegerintegerp0 + multintegerintegerp1
          lw r3,multintegerintegerp0(r0)
          lw r4,multintegerintegerp1(r0)
          mul r2,r3,r4
          sw multtmul0(r0),r2
          % processing: return
          lw r1,multtmul0(r0)
          sw multintegerintegerreturn(r0),r1
          lw r15,multintegerintegerlink(r0)
          jr r15
          % processing function 
multinteger                        sw multintegerlink(r0),r15
          addi r1,r0,2
          sw tempNum0(r0),r1
          % processing: multn := tempNum0
          lw r1,tempNum0(r0)
          sw multn(r0),r1
          % processing: function call to getinteger 
          lw r2,multn(r0)
          sw getintegerp0(r0),r2
          jl r15,getinteger
          lw r2,getintegerreturn(r0)
          sw tempfCall1(r0),r2
          % processing: multtmul1 := tempfCall1 + multintegerp0
          lw r4,tempfCall1(r0)
          lw r3,multintegerp0(r0)
          mul r2,r4,r3
          sw multtmul1(r0),r2
          % processing: return
          lw r1,multtmul1(r0)
          sw multintegerreturn(r0),r1
          lw r15,multintegerlink(r0)
          jr r15
          % processing function 
getinteger                         sw getintegerlink(r0),r15
          % processing: return
          lw r1,getintegerp0(r0)
          sw getintegerreturn(r0),r1
          lw r15,getintegerlink(r0)
          jr r15
          entry
          addi r14,r0,topaddr
          addi r1,r0,1
          sw tempNum2(r0),r1
          addi r1,r0,2
          sw tempNum3(r0),r1
          addi r1,r0,3
          sw tempNum4(r0),r1
          % processing: maintmul2 := tempNum3 + tempNum4
          lw r2,tempNum3(r0)
          lw r3,tempNum4(r0)
          mul r1,r2,r3
          sw maintmul2(r0),r1
          % processing : maintadd3 := tempNum2 + maintmul2
          lw r3,tempNum2(r0)
          lw r2,maintmul2(r0)
          add r1,r3,r2
          sw maintadd3(r0),r1
          % processing: mainy := maintadd3
          lw r1,maintadd3(r0)
          sw mainy(r0),r1
          % processing: read(mainx)
          getc r1
          subi r2,r1,48
          sw mainx(r0), r2
          addi r2,r0,10
          sw tempNum5(r0),r2
          % processing : maintadd4 := mainy + tempNum5
          lw r1,mainy(r0)
          lw r3,tempNum5(r0)
          add r2,r1,r3
          sw maintadd4(r0),r2
          % processing: maintrel5 := mainx + maintadd4
          lw r3,mainx(r0)
          lw r1,maintadd4(r0)
          cgt r2,r3,r1
          sw maintrel5(r0),r2
          %processing ifStat
          lw r2, maintrel5(r0)
          bz r2, else1
          addi r1,r0,10
          sw tempNum6(r0),r1
          % processing : maintadd6 := mainx + tempNum6
          lw r3,mainx(r0)
          lw r4,tempNum6(r0)
          add r1,r3,r4
          sw maintadd6(r0),r1
          % processing: put(maintadd6)
          lw r1,maintadd6(r0)
          sw -8(r14),r1
          addi r1,r0, buf
          sw -12(r14),r1
          jl r15, intstr
          sw -8(r14),r13
          jl r15, putstr
          j endif1
else1          addi r1,r0,1
          sw tempNum7(r0),r1
          % processing : maintadd7 := mainx + tempNum7
          lw r4,mainx(r0)
          lw r3,tempNum7(r0)
          add r1,r4,r3
          sw maintadd7(r0),r1
          % processing: put(maintadd7)
          lw r1,maintadd7(r0)
          sw -8(r14),r1
          addi r1,r0, buf
          sw -12(r14),r1
          jl r15, intstr
          sw -8(r14),r13
          jl r15, putstr
endif1
          addi r2,r0,0
          sw tempNum8(r0),r2
          % processing: mainz := tempNum8
          lw r2,tempNum8(r0)
          sw mainz(r0),r2
          %processing whileStat
gowhile1          addi r2,r0,10
          sw tempNum9(r0),r2
          % processing: maintrel8 := mainz + tempNum9
          lw r1,mainz(r0)
          lw r3,tempNum9(r0)
          cle r2,r1,r3
          sw maintrel8(r0),r2
          lw r2, maintrel8(r0)
          bz r2, endwhile1
          % processing: put(mainz)
          lw r3,mainz(r0)
          sw -8(r14),r3
          addi r3,r0, buf
          sw -12(r14),r3
          jl r15, intstr
          sw -8(r14),r13
          jl r15, putstr
          addi r3,r0,1
          sw tempNum10(r0),r3
          % processing : maintadd9 := mainz + tempNum10
          lw r1,mainz(r0)
          lw r4,tempNum10(r0)
          add r3,r1,r4
          sw maintadd9(r0),r3
          % processing: mainz := maintadd9
          lw r3,maintadd9(r0)
          sw mainz(r0),r3
          j gowhile1
endwhile1          addi r3,r0,1
          sw tempNum11(r0),r3
          % processing sign: tempNum11 
          lw r3,tempNum11(r0)
          add r4,r0,r3
          sw maintsign10(r0),r4
          % processing: mainz := maintsign10
          lw r1,maintsign10(r0)
          sw mainz(r0),r1
          lw r1, mainz(r0)
          not r5, r1
          sw maintnot11(r0), r5
          % processing: put(maintnot11)
          lw r1,maintnot11(r0)
          sw -8(r14),r1
          addi r1,r0, buf
          sw -12(r14),r1
          jl r15, intstr
          sw -8(r14),r13
          jl r15, putstr
          % processing: function call to multintegerinteger 
          lw r1,mainx(r0)
          sw multintegerintegerp0(r0),r1
          lw r1,mainy(r0)
          sw multintegerintegerp1(r0),r1
          jl r15,multintegerinteger
          lw r1,multintegerintegerreturn(r0)
          sw tempfCall12(r0),r1
          % processing: put(tempfCall12)
          lw r1,tempfCall12(r0)
          sw -8(r14),r1
          addi r1,r0, buf
          sw -12(r14),r1
          jl r15, intstr
          sw -8(r14),r13
          jl r15, putstr
          % processing: function call to multinteger 
          lw r1,mainx(r0)
          sw multintegerp0(r0),r1
          jl r15,multinteger
          lw r1,multintegerreturn(r0)
          sw tempfCall13(r0),r1
          % processing: put(tempfCall13)
          lw r1,tempfCall13(r0)
          sw -8(r14),r1
          addi r1,r0, buf
          sw -12(r14),r1
          jl r15, intstr
          sw -8(r14),r13
          jl r15, putstr
          hlt

          % funcDef var res
multtmul0           res 4
multintegerintegerlink   res 4
multintegerintegerreturn res 4
multintegerintegerp0     res 4
multintegerintegerp1     res 4
          % funcDef var res
multn               res 4
multtmul1           res 4
multintegerlink          res 4
multintegerreturn        res 4
multintegerp0            res 4
tempNum0             res 4
          % space for function call expression factor
tempfCall1          res 4
          % funcDef var res
getintegerlink           res 4
getintegerreturn         res 4
getintegerp0             res 4
          % main var res
mainx               res 4
mainy               res 4
mainz               res 4
mainarr             res 28
maintmul2           res 4
maintadd3           res 4
maintadd4           res 4
maintrel5           res 4
maintadd6           res 4
maintadd7           res 4
maintrel8           res 4
maintadd9           res 4
maintsign10         res 4
maintnot11          res 4
tempNum2             res 4
tempNum3             res 4
tempNum4             res 4
tempNum5             res 4
tempNum6             res 4
tempNum7             res 4
tempNum8             res 4
tempNum9             res 4
tempNum10            res 4
tempNum11            res 4
          % space for function call expression factor
tempfCall12         res 4
          % space for function call expression factor
tempfCall13         res 4
          % buffer space used for console output
buf                 res 20

