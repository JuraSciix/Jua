package jua;

import jua.compiler.InstructionUtils;
import jua.runtime.interpreter.instruction.Instruction;
import jua.runtime.interpreter.instruction.InstructionImpls.*;

public class InstructionFactory {

    private static final Nop nop = new Nop();
    private static final Add add = new Add();
    private static final Sub sub = new Sub();
    private static final Mul mul = new Mul();
    private static final Div div = new Div();
    private static final Shl shl = new Shl();
    private static final Shr shr = new Shr();
    private static final Or or = new Or();
    private static final And and = new And();
    private static final Xor xor = new Xor();
    private static final Neg neg = new Neg();
    private static final Pos pos = new Pos();
    private static final ArrayLoad aload = new ArrayLoad();
    private static final ArrayStore astore = new ArrayStore();
    private static final ArrayInc ainc = new ArrayInc();
    private static final ArrayDec adec = new ArrayDec();
    private static final Leave leave = new Leave();
    private static final Length length = new Length();
    private static final ConstFalse const_false = new ConstFalse();
    private static final ConstTrue const_true = new ConstTrue();
    private static final ConstNull const_null = new ConstNull();
    private static final ConstIntM1 const_im1 = new ConstIntM1();
    private static final ConstInt0 const_i0 = new ConstInt0();
    private static final ConstInt1 const_i1 = new ConstInt1();
    private static final ConstInt2 const_i2 = new ConstInt2();
    private static final Dup dup = new Dup();
    private static final Dup2 dup2 = new Dup2();
    private static final DupX1 dup_x1 = new DupX1();
    private static final DupX2 dup_x2 = new DupX2();
    private static final Dup2X1 dup2_x1 = new Dup2X1();
    private static final Dup2X2 dup2_x2 = new Dup2X2();
    private static final NewMap newmap = new NewMap();
    private static final NewList newlist = new NewList();
    private static final Pop pop = new Pop();
    private static final Pop2 pop2 = new Pop2();
    private static final Rem rem = new Rem();
    private static final Return return_ = new Return();
    private static final Not not = new Not();
    private static final Load0 load_0 = new Load0();
    private static final Load1 load_1 = new Load1();
    private static final Load2 load_2 = new Load2();
    private static final Store0 store_0 = new Store0();
    private static final Store1 store_1 = new Store1();
    private static final Store2 store_2 = new Store2();

    private static final Instruction[] MAPPING = new Instruction[InstructionUtils.OPCodes._InstrCount];

    static {
        MAPPING[InstructionUtils.OPCodes.Nop] =  nop;
        MAPPING[InstructionUtils.OPCodes.Add] =  add;
        MAPPING[InstructionUtils.OPCodes.Sub] =  sub;
        MAPPING[InstructionUtils.OPCodes.Mul] =  mul;
        MAPPING[InstructionUtils.OPCodes.Div] =  div;
        MAPPING[InstructionUtils.OPCodes.Shl] =  shl;
        MAPPING[InstructionUtils.OPCodes.Shr] =  shr;
        MAPPING[InstructionUtils.OPCodes.Or] =  or;
        MAPPING[InstructionUtils.OPCodes.And] =  and;
        MAPPING[InstructionUtils.OPCodes.Xor] =  xor;
        MAPPING[InstructionUtils.OPCodes.Neg] =  neg;
        MAPPING[InstructionUtils.OPCodes.Pos] =  pos;
        MAPPING[InstructionUtils.OPCodes.ArrayLoad] =  aload;
        MAPPING[InstructionUtils.OPCodes.ArrayStore] =  astore;
        MAPPING[InstructionUtils.OPCodes.ArrayInc] =  ainc;
        MAPPING[InstructionUtils.OPCodes.ArrayDec] =  adec;
        MAPPING[InstructionUtils.OPCodes.Leave] =  leave;
        MAPPING[InstructionUtils.OPCodes.Length] =  length;
        MAPPING[InstructionUtils.OPCodes.ConstFalse] =  const_false;
        MAPPING[InstructionUtils.OPCodes.ConstTrue] =  const_true;
        MAPPING[InstructionUtils.OPCodes.ConstNull] =  const_null;
        MAPPING[InstructionUtils.OPCodes.ConstIntM1] =  const_im1;
        MAPPING[InstructionUtils.OPCodes.ConstInt0] =  const_i0;
        MAPPING[InstructionUtils.OPCodes.ConstInt1] =  const_i1;
        MAPPING[InstructionUtils.OPCodes.ConstInt2] =  const_i2;
        MAPPING[InstructionUtils.OPCodes.Dup] =  dup;
        MAPPING[InstructionUtils.OPCodes.Dup2] =  dup2;
        MAPPING[InstructionUtils.OPCodes.DupX1] =  dup_x1;
        MAPPING[InstructionUtils.OPCodes.DupX2] =  dup_x2;
        MAPPING[InstructionUtils.OPCodes.Dup2X1] =  dup2_x1;
        MAPPING[InstructionUtils.OPCodes.Dup2X2] =  dup2_x2;
        MAPPING[InstructionUtils.OPCodes.NewMap] =  newmap;
        MAPPING[InstructionUtils.OPCodes.NewList] =  newlist;
        MAPPING[InstructionUtils.OPCodes.Pop] =  pop;
        MAPPING[InstructionUtils.OPCodes.Pop2] =  pop2;
        MAPPING[InstructionUtils.OPCodes.Rem] =  rem;
        MAPPING[InstructionUtils.OPCodes.Return] =  return_;
        MAPPING[InstructionUtils.OPCodes.Not] =  not;
        MAPPING[InstructionUtils.OPCodes.Load0] =  load_0;
        MAPPING[InstructionUtils.OPCodes.Load1] =  load_1;
        MAPPING[InstructionUtils.OPCodes.Load2] =  load_2;
        MAPPING[InstructionUtils.OPCodes.Store0] =  store_0;
        MAPPING[InstructionUtils.OPCodes.Store1] =  store_1;
        MAPPING[InstructionUtils.OPCodes.Store2] =  store_2;
    }

    public static Instruction create(int opcode) {
        if (opcode < 0 || opcode >= MAPPING.length || MAPPING[opcode] == null)
            throw new IllegalArgumentException(InstructionUtils.getOpcodeName(opcode));
        return MAPPING[opcode];
    }
}
