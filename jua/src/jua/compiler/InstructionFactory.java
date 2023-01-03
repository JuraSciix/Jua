package jua.compiler;

import jua.interpreter.instruction.*;

public interface InstructionFactory {

    Add add = new Add();
    Sub sub = new Sub();
    Mul mul = new Mul();
    Div div = new Div();
    Shl shl = new Shl();
    Shr shr = new Shr();
    Or or = new Or();
    And and = new And();
    Xor xor = new Xor();
    Neg neg = new Neg();
    Pos pos = new Pos();
    Aload aload = new Aload();
    Astore astore = new Astore();
    Leave leave = new Leave();
    Length length = new Length();
    Cmp cmp = new Cmp();
    ConstFalse const_false = new ConstFalse();
    ConstTrue const_true = new ConstTrue();
    ConstNull const_null = new ConstNull();
    Dup dup = new Dup();
    Dup2 dup2 = new Dup2();
    Dup_x1 dup_x1 = new Dup_x1();
    Dup_x2 dup_x2 = new Dup_x2();
    Dup2_x1 dup2_x1 = new Dup2_x1();
    Dup2_x2 dup2_x2 = new Dup2_x2();
    Newarray newarray = new Newarray();
    Pop pop = new Pop();
    Pop2 pop2 = new Pop2();
    Rem rem = new Rem();
    Dec dec = new Dec();
    Inc inc = new Inc();
    Not not = new Not();
}
