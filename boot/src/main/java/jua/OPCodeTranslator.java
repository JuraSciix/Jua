package jua;

import jua.compiler.InstructionUtils.*;
import jua.runtime.interpreter.instruction.Instruction;
import jua.runtime.interpreter.instruction.InstructionImpls.*;

import java.util.ArrayList;
import java.util.List;

import static jua.compiler.InstructionUtils.getOpcodeName;

public class OPCodeTranslator implements InstrVisitor {
    private final List<Instruction> instructions = new ArrayList<>();

    public List<Instruction> getInstructions() {
        return instructions;
    }

    private void put(Instruction i) {
        instructions.add(i);
    }

    @Override
    public void visitJump(JumpInstrNode node) {
        int offset = node.offset;
        switch (node.opcode) {
            case OPCodes.Goto:
                put(new Goto(offset));
                break;
            case OPCodes.IfAbsent:
                put(new IfAbsent(offset));
                break;
            case OPCodes.IfEq:
                put(new IfEq(offset));
                break;
            case OPCodes.IfGe:
                put(new IfGe(offset));
                break;
            case OPCodes.IfGt:
                put(new IfGt(offset));
                break;
            case OPCodes.IfLe:
                put(new IfLe(offset));
                break;
            case OPCodes.IfLt:
                put(new IfLt(offset));
                break;
            case OPCodes.IfNe:
                put(new IfNe(offset));
                break;
            case OPCodes.IfNonNull:
                put(new IfNonNull(offset));
                break;
            case OPCodes.IfNull:
                put(new IfNull(offset));
                break;
            case OPCodes.IfNz:
                put(new IfNz(offset));
                break;
            case OPCodes.IfPresent:
                put(new IfPresent(offset));
                break;
            case OPCodes.IfZ:
                put(new IfZ(offset));
                break;
            default:
                opcodeMismatch(node);
        }
    }

    @Override
    public void visitSingle(SingleInstrNode node) {
        put(InstructionFactory.create(node.opcode));
    }

    @Override
    public void visitCall(CallInstrNode node) {
        if (node.opcode == OPCodes.Call) {
            put(new Call(node.callee, node.argc));
        } else {
            opcodeMismatch(node);
        }
    }

    @Override
    public void visitIndexed(IndexedInstrNode node) {
        switch (node.opcode) {
            case OPCodes.Push:
                instructions.add(new Push(node.index));
                break;
            case OPCodes.Load:
                put(new Load(node.index));
                break;
            case OPCodes.Store:
                put(new Store(node.index));
                break;
            case OPCodes.Inc:
                put(new Inc(node.index));
                break;
            case OPCodes.Dec:
                put(new Dec(node.index));
                break;
            default:
                opcodeMismatch(node);
        }
    }

    @Override
    public void visitConst(ConstantInstrNode node) {
        switch (node.opcode) {
            case OPCodes.Push:
                put(new Push(node.index));
                break;
            case OPCodes.BinarySwitch:
            case OPCodes.LinearSwitch:
                // todo: *switch инструкции хранят свои данные в пуле констант.
            default:
                opcodeMismatch(node);
        }
    }

    @Override
    public void visitSwitch(SwitchInstrNode node) {
        switch (node.opcode) {
            case OPCodes.LinearSwitch:
                put(new LinearSwitch(node.literals, node.dstIps, node.defCp));
                break;
            case OPCodes.BinarySwitch:
                put(new BinarySwitch(node.literals, node.dstIps, node.defCp));
            default:
                opcodeMismatch(node);
        }
    }

    private static void opcodeMismatch(InstrNode node) {
        throw new IllegalStateException(node.getClass().getSimpleName()
                + " with opcode " + getOpcodeName(node.opcode));
    }
}
