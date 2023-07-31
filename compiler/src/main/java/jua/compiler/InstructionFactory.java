package jua.compiler;

import jua.interpreter.instruction.Instruction;
import jua.interpreter.instruction.InstructionImpls.*;

public interface InstructionFactory {

    Nop nop                = new Nop();
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
    ArrayLoad aload        = new ArrayLoad();
    ArrayStore astore      = new ArrayStore();
    ArrayInc ainc          = new ArrayInc();
    ArrayDec adec          = new ArrayDec();
    Leave leave            = new Leave();
    Length length          = new Length();
    ConstFalse const_false = new ConstFalse();
    ConstTrue const_true   = new ConstTrue();
    ConstNull const_null   = new ConstNull();
    ConstIntM1 const_im1   = new ConstIntM1();
    ConstInt0 const_i0     = new ConstInt0();
    ConstInt1 const_i1     = new ConstInt1();
    Dup dup                = new Dup();
    Dup2 dup2              = new Dup2();
    DupX1 dup_x1           = new DupX1();
    DupX2 dup_x2           = new DupX2();
    Dup2X1 dup2_x1         = new Dup2X1();
    Dup2X2 dup2_x2         = new Dup2X2();
    NewMap newmap          = new NewMap();
    NewList newlist        = new NewList();
    Pop pop                = new Pop();
    Pop2 pop2              = new Pop2();
    Rem rem                = new Rem();
    Return return_         = new Return();
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
