package jua.compiler;

import jua.interpreter.instruction.*;

public interface InstructionFactory {

    Add add                = new Add();
    Sub sub                = new Sub();
    Mul mul                = new Mul();
    Div div                = new Div();
    Shl shl                = new Shl();
    Shr shr                = new Shr();
    Or or                  = new Or();
    And and                = new And();
    Xor xor                = new Xor();
    Neg neg                = new Neg();
    Pos pos                = new Pos();
    Aload aload            = new Aload();
    Astore astore          = new Astore();
    Ainc ainc              = new Ainc();
    Adec adec              = new Adec();
    Leave leave            = new Leave();
    Length length          = new Length();
    Cmp cmp                = new Cmp();
    ConstFalse const_false = new ConstFalse();
    ConstTrue const_true   = new ConstTrue();
    ConstNull const_null   = new ConstNull();
    ConstIm1 const_im1     = new ConstIm1();
    ConstI0 const_i0       = new ConstI0();
    ConstI1 const_i1       = new ConstI1();
    Dup dup                = new Dup();
    Dup2 dup2              = new Dup2();
    Dup_x1 dup_x1          = new Dup_x1();
    Dup_x2 dup_x2          = new Dup_x2();
    Dup2_x1 dup2_x1        = new Dup2_x1();
    Dup2_x2 dup2_x2        = new Dup2_x2();
    Newarray newarray      = new Newarray();
    Pop pop                = new Pop();
    Pop2 pop2              = new Pop2();
    Rem rem                = new Rem();
    Not not                = new Not();
    Load0 load_0           = new Load0();
    Load1 load_1           = new Load1();
    Load2 load_2           = new Load2();
    Store0 store_0         = new Store0();
    Store1 store_1         = new Store1();
    Store2 store_2         = new Store2();

    Instruction[] load_x = {load_0, load_1, load_2};
    Instruction[] store_x = {store_0, store_1, store_2};
    Instruction[] const_ix = {const_im1, const_i0, const_i1};
}
