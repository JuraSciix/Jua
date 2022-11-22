package jua.compiler;

import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import jua.compiler.Code.JumpInstructionConstructor;
import jua.interpreter.instruction.*;
import jua.interpreter.instruction.Switch;
import jua.runtime.JuaFunction;
import jua.runtime.heap.*;
import jua.compiler.Tree.*;

import java.util.Iterator;
import java.util.List;

import static jua.compiler.InstructionUtils.*;

public final class MinorGen extends Gen {

    /**
     *
     */
    static final int STATE_ROOTED = (1 << 0);

    /**
     * Состояние кода, в котором нельзя определять функции и константы.
     */
    static final int STATE_NO_DECLS = (1 << 1);

    /**
     * Состояние кода, в котором любое обрабатываемое выражение должно оставлять за собой какое-либо значение.
     */
    static final int STATE_RESIDUAL = (1 << 2);

    /**
     * Состояние кода, в котором любое обрабатываемое выражение должно приводиться к логическому виду.
     */
    static final int STATE_COND = (1 << 3);

    /**
     * Состояние кода, в котором все логические выражения должны инвертироваться.
     */
    static final int STATE_COND_INVERT = (1 << 4);

    /**
     * Состояние кода, в котором текущий обрабатываемый цикл считается бесконечным.
     */
    static final int STATE_INFINITY_LOOP = (1 << 5);

    /**
     * Состояние кода, в котором оператор switch не является конечным.
     */
    static final int STATE_ALIVE_SWITCH = (1 << 6);

    private final IntArrayList breakChains = new IntArrayList();
    private final IntArrayList continueChains = new IntArrayList();
    private final IntArrayList fallthroughChains = new IntArrayList();
    private final IntArrayList conditionalChains = new IntArrayList();

    /**
     * Состояние кода.
     */
    private int state = 0; // unassigned state

    Code code;
    private Log log;

    private final ProgramLayout programLayout;

    MinorGen(ProgramLayout programLayout) {
        this.programLayout = programLayout;
    }

    // todo: исправить этот low-cohesion

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        code = tree.code;
        log = tree.source.getLog();
        code.pushContext(0);
        code.pushScope();
        int prev_state = state;
        setState(STATE_ROOTED);
        scan(tree.stats);
        state = prev_state;
        code.addInstruction(Halt.INSTANCE);
        code.popScope();
    }

    private boolean isState(int state_flag) {
        return (state & state_flag) != 0;
    }

    private void unsetState(int state_flag) {
        state &= ~state_flag;
    }

    private void setState(int state_flag) {
        state |= state_flag;
    }

    public void generateAnd(BinaryOp expression) {
//        beginCondition();
//        if (invertCond) {
//            if (expression.rhs == null) {
//                generateCondition(expression.lhs);
//            } else {
//                int fa = pushMakeConditionChain();
//                invertCond = false;
//                generateCondition(expression.lhs);
//                popConditionChain();
//                invertCond = true;
//                generateCondition(expression.rhs);
//                code.resolveChain(fa);
//            }
//        } else {
//            generateCondition(expression.lhs);
//            generateCondition(expression.rhs);
//        }
//        endCondition();

        beginCondition();
        if (isState(STATE_COND_INVERT)) {
            int fa = pushMakeConditionChain();
            int prev_state = state;
            unsetState(STATE_COND_INVERT);
            generateCondition(expression.lhs);
            popConditionChain();
            state = prev_state;
            generateCondition(expression.rhs);
            code.resolveChain(fa);
        } else {
            generateCondition(expression.lhs);
            generateCondition(expression.rhs);
        }
        endCondition();
    }

    public void geterateOr(BinaryOp expression) {
//        beginCondition();
//        if (invertCond) {
//            generateCondition(expression.lhs);
//            generateCondition(expression.rhs);
//        } else {
//            if (expression.rhs == null) {
//                generateCondition(expression.lhs);
//            } else {
//                int tr = pushMakeConditionChain();
//                invertCond = true;
//                generateCondition(expression.lhs);
//                popConditionChain();
//                invertCond = false;
//                generateCondition(expression.rhs);
//                code.resolveChain(tr);
//            }
//        }
//        endCondition();

        beginCondition();
        if (isState(STATE_COND_INVERT)) {
            generateCondition(expression.lhs);
            generateCondition(expression.rhs);
        } else {
            int tr = pushMakeConditionChain();
            int prev_state = state;
            setState(STATE_COND_INVERT);
            generateCondition(expression.lhs);
            popConditionChain();
            state = prev_state;
            generateCondition(expression.rhs);
            code.resolveChain(tr);
        }
        endCondition();
    }

    @Override
    public void visitArrayAccess(ArrayAccess tree) {
        visitExpression(tree.expr);
        visitExpression(tree.index);
        code.putPos(tree.pos);
        emitALoad();
    }

    @Override
    public void visitArrayLiteral(ArrayLiteral tree) {
//        code.incStack();
//        code.addInstruction(Newarray.INSTANCE);
//        AtomicInteger index = new AtomicInteger();
//        enableUsed();
//        expression.map.forEach((key, value) -> {
//            int line;
//            if (key.isEmpty()) {
//                line = value.getPosition().line;
//                emitPush(index.longValue(), IntOperand::valueOf);
//            } else {
//                line = key.getPosition().line;
//                visitStatement(key);
//            }
//            visitStatement(value);
//            emitAStore(line);
//            index.incrementAndGet();
//        });
//        disableUsed();
//        emitNecessaryPop();

        code.putPos(tree.pos);
        emitNewArray();
        generateArrayCreation(tree.entries);
    }

    void generateArrayCreation(List<ArrayLiteral.Entry> entries) {
        long implicitIndex = 0;
        Iterator<ArrayLiteral.Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            ArrayLiteral.Entry entry = iterator.next();
            if (iterator.hasNext() || isUsed()) emitDup();
            if (entry.key == null) {
                code.putPos(entry.pos);
                emitPushLong(implicitIndex++);
            } else {
                visitExpression(entry.key);
            }
            visitExpression(entry.value);
            code.putPos(entry.pos);
            emitAStore();
        }
    }

    private void emitNewArray() {
        code.addInstruction(Newarray.INSTANCE);
    }

    private void generateBinary(BinaryOp tree) {
        if (TreeInfo.isConditionalTag(tree.tag)) {
            generateComparison(tree);
            return;
        }
        tree.lhs.accept(this);
        if (tree.getTag() == Tag.NULLCOALESCE) {
            emitDup();
            int el = code.makeChain();
            code.putPos(tree.pos);
            code.addChainedInstruction(Ifnonnull::new, el);
            code.addInstruction(Pop.INSTANCE);
            visitExpression(tree.rhs);
            code.resolveChain(el);
            return;
        }
        tree.rhs.accept(this);
        code.putPos(tree.pos);
        code.addInstruction(fromBinaryOpTag(tree.getTag()));
    }

    @Override
    public void visitBreak(Break tree) {
        if (breakChains.isEmpty()) {
            cError(tree.pos, "'break' is not allowed outside of loop/switch.");
            return;
        }
        code.putPos(tree.pos);
        emitGoto(breakChains.topInt());
        unsetState(STATE_INFINITY_LOOP);
    }

    private int switch_start_ip;

    private Int2IntMap cases;

    private int default_case;

    @Override
    public void visitCase(Case tree) {
        if (tree.labels != null) { // is not default case?
            for (Expression expr : tree.labels) {
                if (expr.getTag() != Tag.LITERAL) {
                    cError(expr.pos, "constant expected");
                    continue;
                }
                int cp = ((Literal) expr).type.getConstantIndex();
                cases.put(cp, code.currentIP() - switch_start_ip);
            }

        } else {
            if (default_case != -1) {
                code.resolveChain(default_case);
                default_case = -1;
            }
        }
        int f = code.makeChain();
        fallthroughChains.push(f);
        boolean alive = visitBody(tree.body);
        if (alive) {
            setState(STATE_ALIVE_SWITCH);
            emitGoto(breakChains.topInt());
        }
        fallthroughChains.popInt();
        code.resolveChain(f);
    }

    @Override
    public void visitSwitch(Tree.Switch tree) {
        visitExpression(tree.expr);
        // emit switch
        int b = code.makeChain();
        breakChains.push(b);
        default_case = code.makeChain();
        Int2IntMap _cases = new Int2IntLinkedOpenHashMap();
        cases = _cases;
        // todo: Координация по кейзам должна основываться на Code.Chain. В этот раз сделать лучше не получилось.
        switch_start_ip = code.currentIP();
        code.addChainedInstruction(dest_ip -> {
            int[] literals = _cases.keySet().toIntArray();
            int[] destIps = _cases.values().toIntArray();
            return new Switch(literals, destIps, dest_ip /* default ip */);
        }, default_case);
        int cached_sp = code.getSp();
        int max_sp = cached_sp;
        int prev_state = state;
        unsetState(STATE_ALIVE_SWITCH);
        for (Case _case : tree.cases) {
            code.setSp(cached_sp);
            _case.accept(this);
            if (code.getSp() > max_sp) max_sp = code.getSp();
        }
        code.setSp(max_sp);
        breakChains.popInt();
        if (default_case != -1)
            code.resolveChain(default_case);
        code.resolveChain(b);
        cases = null;
        if (!isState(STATE_ALIVE_SWITCH)) {
            code.dead();
        }
        state = prev_state;
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        cError(tree.pos, "constants declaration is not allowed here.");
    }

    Operand resolveOperand(Literal literal) {
        // todo: Довести до literal.type.toOperand()
        Types.Type value = literal.type;
        if (value.isLong()) return LongOperand.valueOf(value.longValue());
        if (value.isDouble())
            return DoubleOperand.valueOf(value.doubleValue());
        if (value.isString()) return StringOperand.valueOf(value.stringValue());
        if (value.isBoolean()) return BooleanOperand.valueOf(value.booleanValue());
        assert value.isNull();
        return NullOperand.NULL;
    }

    @Override
    public void visitContinue(Continue tree) {
        if (continueChains.isEmpty()) {
            cError(tree.pos, "'continue' is not allowed outside of loop.");
            return;
        }
        code.putPos(tree.pos);
        emitGoto(continueChains.topInt());
        code.dead();
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        generateLoop(tree, null, tree.cond, null, tree.body, false);
    }

    private int pushMakeConditionChain() {
        int newChain = code.makeChain();
        conditionalChains.push(newChain);
        return newChain;
    }

    private int popConditionChain() {
        return conditionalChains.popInt();
    }

    private int peekConditionChain() {
        return conditionalChains.topInt();
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        if (fallthroughChains.isEmpty()) {
            cError(tree.pos, "'fallthrough' is not allowed outside of switch.");
            return;
        }
        code.putPos(tree.pos);
        emitGoto(fallthroughChains.topInt());
        unsetState(STATE_INFINITY_LOOP); // for cases
        code.dead();
    }

    @Override
    public void visitFor(ForLoop tree) {
        generateLoop(tree, tree.init, tree.cond, tree.step, tree.body, true);
    }

    @Override
    public void visitInvocation(Invocation tree) {
        Instruction instruction;
        int stack = 0;
        boolean noReturnValue = false;
        Name callee = unpackCallee(tree.callee);
        if (!tree.args.isEmpty()) {
            for (Invocation.Argument a : tree.args) {
                if (a.name != null) {
                    cError(a.name.pos, "Named arguments not allowed yet");
                }
            }
        }
        switch (callee.value) {
            case "bool":
                if (tree.args.size() != 1) {
                    cError(tree.pos, "mismatch call parameters: 1 expected, " + tree.args.size() + " got.");
                }
                visitExpression(tree.args.get(0).expr);
                instruction = Bool.INSTANCE;
                break;
            case "print":
                visitInvocationArgs(tree.args);
                instruction = new Print(tree.args.size());
                stack = -tree.args.size();
                noReturnValue = true;
                break;
            case "println":
                visitInvocationArgs(tree.args);
                instruction = new Println(tree.args.size());
                stack = -tree.args.size();
                noReturnValue = true;
                break;
            case "typeof":
            case "gettype":
                if (tree.args.size() != 1) {
                    cError(tree.pos, "mismatch call parameters: 1 expected, " + tree.args.size() + " got.");
                }
                visitExpression(tree.args.get(0).expr);
                instruction = Gettype.INSTANCE;
                break;
            case "ns_time":
                if (tree.args.size() != 0) {
                    cError(tree.pos, "mismatch call parameters: 0 expected, " + tree.args.size() + " got.");
                }
                instruction = NsTime.INSTANCE;
                stack = 1;
                break;
            case "length":
                if (tree.args.size() != 1) {
                    cError(tree.pos, "mismatch call parameters: 1 expected, " + tree.args.size() + " got.");
                }
                visitExpression(tree.args.get(0).expr);
                instruction = Length.INSTANCE;
                break;
            default:
                if (tree.args.size() > 0xff) {
                    cError(tree.pos, "too many parameters.");
                }
                visitInvocationArgs(tree.args);
                instruction = new Call(programLayout.tryFindFunc(callee), (byte) tree.args.size(), callee);
                stack = -tree.args.size() + 1;
                break;
        }
        code.putPos(tree.pos);
        code.addInstruction(instruction);
        if (noReturnValue)
            code.addInstruction(ConstNull.INSTANCE);
    }

    private Name unpackCallee(Expression expr) {
        Tree tree = TreeInfo.removeParens(expr);
        if (!tree.hasTag(Tag.MEMACCESS)) {
            cError(expr.pos, "Only a function calling allowed");
            return null;
        }
        return ((MemberAccess) tree).member;
    }

    private void visitInvocationArgs(List<Invocation.Argument> args) {
        for (Invocation.Argument arg : args) {
            visitExpression(arg.expr);
        }
    }

    Source funcSource;

    JuaFunction resultFunc;

    @Override
    public void visitFuncDef(FuncDef tree) {
        if (code != null) {
            cError(tree.pos, "Function declaration is not allowed here");
            return;
        }
        code = tree.code;
        log = funcSource.getLog();

        code.pushContext(tree.pos);
        code.pushScope();

        {
            int nOptionals = 0;
            for (FuncDef.Parameter param : tree.params) {
                Name name = param.name;
                if (code.localExists(name.value)) {
                    cError(name.pos, "Duplicate parameter named '" + name.value + "'.");
                }
                int localIdx = code.resolveLocal(name.value);
                if (param.expr != null) {
                    Expression expr = param.expr;
                    if (expr.getTag() != Tag.LITERAL) {
                        cError(expr.pos, "The values of the optional parameters can only be literals");
                    }
                    code.get_cpb().putDefaultLocalEntry(localIdx, ((Literal) expr).type.getConstantIndex());
                    nOptionals++;
                }
            }

            assert tree.body != null;

            Statement body = tree.body;
            body.accept(this);

            if (body.getTag() == Tag.BLOCK) {
                if (code.isAlive()) {
                    emitRetnull();
                }
            } else {
                assert body instanceof Expression;
                emitReturn();
            }

            resultFunc = JuaFunction.fromCode(
                    tree.name.value,
                    tree.params.size() - nOptionals,
                    tree.params.size(),
                    code.buildCodeSegment(),
                    funcSource.name
            );
        }

        code.popScope();
        code.popContext();
    }

    private boolean declarationsUnallowedHere() {
        return isState(STATE_NO_DECLS);
    }

    public void visitGreaterEqual(BinaryOp expression) {
//        beginCondition();
//        if (expression.lhs instanceof IntExpression) {
//            visitExpression(expression.rhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Ifle(((IntExpression) expression.lhs).value)
//                    : new Ifgt(((IntExpression) expression.lhs).value));
//            code.decStack();
//        } else if (expression.rhs instanceof IntExpression) {
//            visitExpression(expression.lhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Ifge(((IntExpression) expression.rhs).value)
//                    : new Iflt(((IntExpression) expression.rhs).value));
//            code.decStack();
//        } else {
//            visitBinaryOp(expression);
//            code.addFlow(TreeInfo.line(expression), ListDequeUtils.peekLastInt(conditionalChains),
//                    invertCond ? new Ifcmpge() : new Ifcmplt());
//            code.decStack(2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    public void visitGreater(BinaryOp expression) {
//        beginCondition();
//        if (expression.lhs instanceof IntExpression) {
//            visitExpression(expression.rhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Ifge(((IntExpression) expression.lhs).value)
//                    : new Iflt(((IntExpression) expression.lhs).value));
//            code.decStack();
//        } else if (expression.rhs instanceof IntExpression) {
//            visitExpression(expression.lhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Ifle(((IntExpression) expression.rhs).value)
//                    : new Ifgt(((IntExpression) expression.rhs).value));
//            code.decStack();
//        } else {
//            visitBinaryOp(expression);
//            code.addFlow(TreeInfo.line(expression), ListDequeUtils.peekLastInt(conditionalChains),
//                    invertCond ? new Ifcmpgt() : new Ifcmple());
//            code.decStack(2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    @Override
    public void visitIf(If tree) {
        // todo: Лишние стековые слоты (sp) не могут браться из воздуха
        //  Это значит, что вместо ленивого присвоения нового sp
        //  Нужно строго сравнивать и выбрасывать исключение в случае несоответствия.
        //  То же самое касается и visitTernary
        if (tree.elsebody == null) {
            pushMakeConditionChain();
            generateCondition(tree.cond);
            visitBody(tree.thenbody);
            code.resolveChain(popConditionChain());
        } else {
            int el = pushMakeConditionChain();
            int ex = code.makeChain();
            generateCondition(tree.cond);
            int cached_sp = code.getSp();
            boolean thenAlive = visitBody(tree.thenbody);
            emitGoto(ex);
            code.resolveChain(el);
            int body_sp = code.getSp();
            code.setSp(cached_sp);
            boolean elseAlive = visitBody(tree.elsebody);
            code.setSp(Math.max(body_sp, code.getSp()));
            code.resolveChain(ex);
            if (!thenAlive && !elseAlive) code.dead();
        }
    }

    public void visitLessEqual(BinaryOp expression) {
//        beginCondition();
//        Expression lhs = expression.lhs;
//        Expression rhs = expression.rhs;
//        int line = TreeInfo.line(expression);
//        if (TreeInfo.resolveShort(lhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifge(shortVal) : new Iflt(shortVal)),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(rhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifle(shortVal) : new Ifgt(shortVal)),
//                    peekConditionChain(), -1);
//        } else {
//            visitExpression(lhs);
//            visitExpression(rhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifcmple() : new Ifcmpgt()),
//                    peekConditionChain(), -2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    public void visitLess(BinaryOp expression) {
//        beginCondition();
//        if (expression.lhs instanceof IntExpression) {
//            visitExpression(expression.rhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Ifgt(((IntExpression) expression.lhs).value)
//                    : new Ifle(((IntExpression) expression.lhs).value));
//            code.decStack();
//        } else if (expression.rhs instanceof IntExpression) {
//            visitExpression(expression.lhs);
//            code.addFlow(ListDequeUtils.peekLastInt(conditionalChains), invertCond
//                    ? new Iflt(((IntExpression) expression.rhs).value)
//                    : new Ifge(((IntExpression) expression.rhs).value));
//            code.decStack();
//        } else {
//            visitBinaryOp(expression);
//            code.addFlow(TreeInfo.line(expression), ListDequeUtils.peekLastInt(conditionalChains),
//                    invertCond ? new Ifcmplt() : new Ifcmpge());
//            code.decStack(2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    static boolean isShortIntegerLiteral(Expression tree) {
        if (tree == null) return false;
        if (tree.hasTag(Tag.PARENS)) tree = TreeInfo.removeParens(tree);
        if (!tree.hasTag(Tag.LITERAL)) return false;
        Literal literal = (Literal) tree;
        if (!literal.type.isLong()) return false;
        long value = literal.type.longValue();
        return (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE);
    }

    static int unpackShortIntegerLiteral(Expression tree) {
        tree = TreeInfo.removeParens(tree);
        return (int) ((Literal) tree).type.longValue();
    }

    static boolean isNullLiteral(Expression tree) {
        if (tree == null) return true;
        if (tree.hasTag(Tag.PARENS)) tree = TreeInfo.removeParens(tree);
        if (!tree.hasTag(Tag.LITERAL)) return false;
        return ((Literal) tree).type.isNull();
    }


    private void generateComparison(BinaryOp tree) {
        beginCondition();
        JumpInstructionConstructor opcode;
        boolean negated = isState(STATE_COND_INVERT);
        if (isShortIntegerLiteral(tree.lhs)) {
            visitExpression(tree.rhs);
            opcode = offset -> {
                JumpInstruction o = fromConstComparisonOpTag(tree.tag, unpackShortIntegerLiteral(tree.lhs), negated);
                o.offset = offset;
                return o;
            };
        } else if (isShortIntegerLiteral(tree.rhs)) {
            visitExpression(tree.lhs);
            opcode = offset -> {
                JumpInstruction o = fromConstComparisonOpTag(tree.tag, unpackShortIntegerLiteral(tree.rhs), negated);
                o.offset = offset;
                return o;
            };
        } else if (isNullLiteral(tree.lhs)) {
            visitExpression(tree.rhs);
            opcode = offset -> negated ? new Ifnull(offset) : new Ifnonnull(offset);
        } else if (isNullLiteral(tree.rhs)) {
            visitExpression(tree.lhs);
            opcode = offset -> negated ? new Ifnull(offset) : new Ifnonnull(offset);
        } else {
            visitExpression(tree.lhs);
            visitExpression(tree.rhs);
            opcode = offset -> {
                JumpInstruction o = fromComparisonOpTag(tree.tag, negated);
                o.offset = offset;
                return o;
            };
        }
        code.putPos(tree.pos);
        code.addChainedInstruction(opcode, peekConditionChain());
        endCondition();
    }

    public void visitNotEqual(BinaryOp expression) {
//        beginCondition();
//        Expression lhs = expression.lhs;
//        Expression rhs = expression.rhs;
//        int line = TreeInfo.line(expression);
//        if (lhs instanceof NullExpression) {
//            visitExpression(rhs);
//            code.addChainedInstruction(line,
//                    invertCond ? new Ifnonnull() : new Ifnull(),
//                    peekConditionChain(), -1);
//        } else if (rhs instanceof NullExpression) {
//            visitExpression(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifnonnull() : new Ifnull()),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(lhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifne(shortVal) : new Ifeq(shortVal)),
//                    peekConditionChain(), -1);
//        } else if (TreeInfo.resolveShort(rhs) >= 0) {
//            visitExpression(rhs);
//            int shortVal = TreeInfo.resolveShort(lhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifne(shortVal) : new Ifeq(shortVal)),
//                    peekConditionChain(), -1);
//        } else {
//            visitExpression(lhs);
//            visitExpression(rhs);
//            code.addChainedInstruction(line,
//                    (invertCond ? new Ifcmpne() : new Ifcmpeq()),
//                    peekConditionChain(), -2);
//        }
//        endCondition();

        generateComparison(expression);
    }

    public void generateNullCoalescing(BinaryOp expression) {
        // todo: Это очевидно неполноценная реализация.
//        visitExpression(expression.lhs);
//        emitDup();
//        int el = code.makeChain();
//        code.addChainedInstruction(new Ifnonnull(), el, -1);
//        code.addInstruction(Pop.INSTANCE, -1);
//        visitExpression(expression.rhs);
//        code.resolveChain(el);

        generateBinary(expression);
    }

    @Override
    public void visitParens(Parens tree) {
        tree.expr.accept(this);
        // todo: выбрасывается AssertionError
        //throw new AssertionError(
        //        "all brackets should have been removed in ConstantFolder");
    }

    @Override
    public void visitAssign(Assign tree) {
        generateAssignment(tree.pos, Tag.ASSIGN, tree.var, tree.expr);
    }

    private void generateUnary(UnaryOp tree) {
        switch (tree.getTag()) {
            case POSTDEC:
            case PREDEC:
            case POSTINC:
            case PREINC:
                generateIncrease(tree);
                return;
        }

//        System.out.println(tree);
        if (tree.hasTag(Tag.NOT)) {
            // todo: Exception in thread "main" java.lang.AssertionError: context.current_nstack < 0, currentIP: 65, lineNumber: 44
            //	at jua.compiler.Code.adjustStack(Code.java:150)
            //	at jua.compiler.Code.addChainedInstruction0(Code.java:125)
            //	at jua.compiler.Code.addChainedInstruction(Code.java:118)
            //	at jua.compiler.MinorGen.generateCondition(MinorGen.java:1083)
            //	at jua.compiler.MinorGen.visitIf(MinorGen.java:626)
            //	at jua.compiler.Tree$If.accept(Tree.java:701)
            //	at jua.compiler.Tree$Scanner.scan(Tree.java:204)
            //	at jua.compiler.Tree$Scanner.visitBlock(Tree.java:231)
            //	at jua.compiler.Tree$Block.accept(Tree.java:679)
            //	at jua.compiler.MinorGen.visitFuncDef(MinorGen.java:534)
            //	at jua.compiler.Tree$FuncDef.accept(Tree.java:663)
            //	at jua.compiler.Tree$Scanner.scan(Tree.java:204)
            //	at jua.compiler.MinorGen.visitCompilationUnit(MinorGen.java:101)
            //	at jua.compiler.Tree$CompilationUnit.accept(Tree.java:592)
            //	at jua.compiler.JuaCompiler.next(JuaCompiler.java:35)
            //	at jua.Main.main(Main.java:34)
            beginCondition();
            int prev_state = state;
            state ^= STATE_COND_INVERT;
            generateCondition(tree.expr);
            state = prev_state;
            endCondition();
            return;
        }
        tree.expr.accept(this);
        code.putPos(tree.pos);
        code.addInstruction(fromUnaryOpTag(tree.getTag()));
    }

    @Override
    public void visitReturn(Tree.Return tree) {
        if (isNull(tree.expr)) {
            emitRetnull();
        } else {
            visitExpression(tree.expr);
            emitReturn();
        }
    }

    private void emitRetnull() {
        code.addInstruction(ReturnNull.INSTANCE);
        code.dead();
    }

    private static boolean isNull(Expression tree) {
        return tree == null || tree.getTag() == Tag.LITERAL && ((Literal) tree).type == null;
    }

    @Override
    public void visitTernaryOp(TernaryOp tree) {
        int el = pushMakeConditionChain();
        int ex = code.makeChain();

        int prev_state = state;
        unsetState(STATE_COND_INVERT);
        generateCondition(tree.cond);
        state = prev_state;

        int cached_sp = code.getSp();
        popConditionChain();
        visitExpression(tree.thenexpr);
        int lhs_sp = code.getSp();
        code.setSp(cached_sp);
        emitGoto(ex);
        code.resolveChain(el);
        int rhs_sp = code.getSp();
        visitExpression(tree.elseexpr);
        code.resolveChain(ex);
        code.setSp(Math.max(lhs_sp, rhs_sp));
    }

    @Override
    public void visitVariable(Var tree) {
        Name name = tree.name;
        code.putPos(tree.pos);
        if (programLayout.hasConstant(name)) {
            code.addInstruction(new Getconst(programLayout.tryFindConst(name), name));
        } else {
            emitVLoad(tree.name.value);
        }
    }

    @Override
    public void visitMemberAccess(MemberAccess tree) {
        visitExpression(tree.expr);
        emitPushString(tree.member.value);
        code.putPos(tree.pos);
        emitALoad();
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        generateLoop(tree, null, tree.cond, null, tree.body, true);
    }

    private void generateLoop(Statement loop, List<Expression> initials, Expression condition, List<Expression> steps,
                             Statement body, boolean testFirst) {
        // cond chain
        int cdc = code.makeChain();
        // begin chain
        int bgc = code.makeChain();
        // exit chain
        int exc = code.makeChain();

        int prev_state = state;
        setState(STATE_INFINITY_LOOP);

        if (initials != null) {
            initials.forEach(this::visitStatement);
        }
        if (condition != null) unsetState(STATE_INFINITY_LOOP);
        if (testFirst && condition != null) {
            code.putPos(loop.pos);
            emitGoto(cdc);
        }
        code.resolveChain(bgc);
        breakChains.push(exc);
        continueChains.push(cdc);
        visitBody(body);
        if (steps != null) {
            steps.forEach(this::visitStatement);
        }
        code.resolveChain(cdc);
        if (condition == null) {
            emitGoto(bgc);
        } else {
            conditionalChains.push(bgc);
            setState(STATE_COND_INVERT);
            generateCondition(condition);
            unsetState(STATE_COND_INVERT);
            popConditionChain();
        }
        code.resolveChain(exc);
        if (isState(STATE_INFINITY_LOOP)) code.dead();
        state = prev_state;
    }

    private void visitExpression(Expression expression) {
        int prev_state = state;
        setState(STATE_RESIDUAL);
        expression.accept(this);
        state = prev_state;
    }

    public void visitBinaryOp(BinaryOp tree) {
        switch (tree.tag) {
            case FLOW_AND:     generateAnd(tree);            break;
            case LT:           generateComparison(tree);     break;
            case EQ:           generateComparison(tree);     break;
            case GE:           generateComparison(tree);     break;
            case GT:           generateComparison(tree);     break;
            case LE:           generateComparison(tree);     break;
            case NE:           generateComparison(tree);     break;
            case FLOW_OR:      geterateOr(tree);             break;
            case NULLCOALESCE: generateNullCoalescing(tree); break;
            default:           generateBinary(tree);
        }
    }

    public void visitUnaryOp(UnaryOp expression) {
        generateUnary(expression);
    }

    @Override
    public void visitCompoundAssign(CompoundAssign tree) {
        generateAssignment(tree.pos, tree.tag, tree.dst, tree.src);
    }

    @Override
    public void visitLiteral(Literal tree) {
        if (tree.type.isLong()) {
            emitPushLong(tree.type.longValue());
        } else if (tree.type.isDouble()) {
            emitPushDouble(tree.type.doubleValue());
        } else if (tree.type.isBoolean()) {
            if (tree.type.booleanValue()) {
                emitPushTrue();
            } else {
                emitPushFalse();
            }
        } else if (tree.type.isString()) {
            emitPushString(tree.type.stringValue());
        } else if (tree.type.isNull()) {
            code.addInstruction(ConstNull.INSTANCE);
        } else {
            throw new AssertionError();
        }
    }

    private void visitList(List<? extends Tree> expressions) {
        int prev_state = state;
        setState(STATE_RESIDUAL);
        for (Tree expr : expressions)
            expr.accept(this);
        state = prev_state;
    }

    private void generateCondition(Expression tree) {
        assert tree != null;
        int prev_state = state;
        setState(STATE_COND);
        visitExpression(tree);
        state = prev_state;
        if (TreeInfo.isConditionalTag(TreeInfo.removeParens(tree).getTag())) {
            return;
        }
        // todo: Здешний код отвратителен. Следует переписать всё с нуля...
//        code.addInstruction(Bool.INSTANCE);
        code.putPos(tree.pos);
        code.addChainedInstruction(isState(STATE_COND_INVERT) ? Iftrue::new : Iffalse::new,
                peekConditionChain());
    }

    @Override
    public void visitDiscarded(Discarded tree) {
        visitStatement(tree.expr);
        switch (tree.expr.getTag()) {
            case ASSIGN: case ASG_ADD: case ASG_SUB: case ASG_MUL:
            case ASG_DIV: case ASG_REM: case ASG_AND: case ASG_OR:
            case ASG_XOR: case ASG_SL: case ASG_SR: case ASG_NULLCOALESCE:
            case PREINC: case PREDEC: case POSTINC: case POSTDEC:
                break;
            default:
                code.addInstruction(Pop.INSTANCE);
        }
    }

    private void generateAssignment(int pos, Tag tag, Expression lhs, Expression rhs) {
//        Expression var = expression.var.child();
//        checkAssignable(var);
//        int line = line(expression);
//        if (var instanceof ArrayAccess) {
//            ArrayAccess var0 = (ArrayAccess) var;
//            visitExpression(var0.hs);
//            visitExpression(var0.key);
//            if (state != null) {
//                emitDup2(line);
//                emitALoad(line(var0.key));
//                state.emit(line);
//            } else {
//                visitExpression(expression.expr);
//            }
//            if (isUsed())
//                // Здесь используется var0.key потому что
//                // он может быть дальше, чем var0, а если бы он был ближе
//                // к началу файла, то это было бы некорректно для таблицы линий
//                emitDup_x2(line(var0.key));
//            emitAStore(line);
//        } else if (var instanceof Var) {
//            if (state != null) {
//                visitExpression(var);
//                state.emit(line);
//            } else {
//                visitExpression(expression.expr);
//                if (isUsed()) {
//                    emitDup(line(var));
//                }
//            }
//            emitVStore(line, ((Var) var).name);
//        }

        switch (lhs.getTag()) {
            case ARRAYACCESS: {
                ArrayAccess arrayAccess = (ArrayAccess) lhs;
                code.putPos(arrayAccess.pos);
                visitExpression(arrayAccess.expr);
                visitExpression(arrayAccess.index);
                if (tag == Tag.ASG_NULLCOALESCE) {
                    int el = code.makeChain();
                    int ex = code.makeChain();
                    emitDup2();
                    emitALoad();
                    code.addChainedInstruction(Ifnonnull::new, el);
                    int sp_cache = code.getSp();
                    visitExpression(rhs);
                    if (isUsed()) {
                        emitDupX2();
                    }
                    code.putPos(arrayAccess.pos);
                    emitAStore();
                    emitGoto(ex);
                    int sp_cache2 = code.getSp();
                    code.resolveChain(el);
                    code.setSp(sp_cache);
                    if (isUsed()) {
                        code.putPos(arrayAccess.pos);
                        emitALoad();
                    } else {
                        code.addInstruction(Pop2.INSTANCE);
                    }
                    code.resolveChain(ex);
                    assert code.getSp() == sp_cache2;
                } else {
                    if (tag != Tag.ASSIGN) {
                        emitDup2();
                        code.putPos(arrayAccess.pos);
                        emitALoad();
                        visitExpression(rhs);
                        code.putPos(pos);
                        code.addInstruction(asg2state(tag));
                    } else {
                        visitExpression(rhs);
                    }
                    if (isUsed()) {
                        emitDupX2();
                    }
                    code.putPos(arrayAccess.pos);
                    emitAStore();
                }
                break;
            }
            case VARIABLE: {
                Var variable = (Var) lhs;
                if (tag == Tag.ASG_NULLCOALESCE) {
                    int ex = code.makeChain();
                    visitExpression(lhs);
                    code.addChainedInstruction(Ifnonnull::new, ex);
                    visitExpression(rhs);
                    if (isUsed()) {
                        emitDup();
                    }
                    code.putPos(pos);
                    emitVStore(variable.name.value);
                    if (isUsed()) {
                        int el = code.makeChain();
                        emitGoto(el);
                        code.resolveChain(ex);
                        visitExpression(lhs);
                        code.resolveChain(el);
                    } else {
                        code.resolveChain(ex);
                    }
                } else {
                    if (tag != Tag.ASSIGN) {
                        visitExpression(lhs);
                        visitExpression(rhs);
                        code.putPos(pos);
                        code.addInstruction(asg2state(tag));
                    } else {
                        visitExpression(rhs);
                    }
                    if (isUsed()) {
                        emitDup();
                    }
                    code.putPos(pos);
                    emitVStore(variable.name.value);
                }
                break;
            }
            default: cError(lhs.pos, "assignable expression expected.");
        }
    }

    public static Instruction asg2state(Tag tag) {
        return fromBinaryOpTag(TreeInfo.tagWithoutAsg(tag));
    }

    // todo: В будущем планируется заменить поле expressionDepth на более удобный механизм.
    private boolean isUsed() {
        return isState(STATE_RESIDUAL);
    }
    @Deprecated
    private void enableUsed() {

    }
    @Deprecated
    private void disableUsed() {

    }

    private void generateIncrease(UnaryOp expression) {
//        Expression hs = expression.hs.child();
//        checkAssignable(hs);
//        int line = line(expression);
//        if (hs instanceof ArrayAccess) {
//            ArrayAccess hs0 = (ArrayAccess) hs;
//            visitExpression(hs0.hs);
//            visitExpression(hs0.key);
//            emitDup2(line(expression));
//            emitALoad(line(hs0.key));
//            if (isPost && (isUsed())) {
//                emitDupX2(line(hs0.key));
//            }
//            code.addInstruction(line, isIncrement
//                    ? Inc.INSTANCE
//                    : Dec.INSTANCE);
//            if (!isPost && (isUsed())) {
//                emitDupX2(line(hs0.key));
//            }
//            emitAStore(line);
//        } else if (hs instanceof Var) {
//            String name = ((Var) hs).name;
//            if (isPost && (isUsed())) {
//                emitVLoad(line, name);
//            }
//            code.addInstruction(line, isIncrement
//                    ? new Vinc(name, code.getLocal(name))
//                    : new Vinc(name, code.getLocal(name)));
//            if (!isPost && (isUsed())) {
//                emitVLoad(line, name);
//            }
//        }

        Expression hs = expression.expr;

        switch (hs.getTag()) {
            case ARRAYACCESS: {
                ArrayAccess arrayAccess = (ArrayAccess) hs;
                code.putPos(arrayAccess.pos);
                visitExpression(arrayAccess.expr);
                visitExpression(arrayAccess.index);
                emitDup2();
                emitALoad();
                if (isUsed() && (expression.hasTag(Tag.POSTINC) || expression.hasTag(Tag.POSTDEC))) {
                    emitDupX2();
                }
                code.putPos(expression.pos);
                code.addInstruction(increase2state(expression.getTag(), -1));
                if (isUsed() && (expression.hasTag(Tag.PREINC) || expression.hasTag(Tag.PREDEC))) {
                    emitDupX2();
                }
                code.putPos(arrayAccess.pos);
                emitAStore();
                break;
            }
            case VARIABLE: {
                Var variable = (Var) hs;
                if (isUsed() && (expression.hasTag(Tag.POSTINC) || expression.hasTag(Tag.POSTDEC))) {
                    variable.accept(this);
                }
                code.putPos(expression.pos);
                code.addInstruction(increase2state(expression.getTag(), code.resolveLocal(variable.name.value)));
                if (isUsed() && (expression.hasTag(Tag.PREINC) || expression.hasTag(Tag.PREDEC))) {
                    variable.accept(this);
                }
                break;
            }
            default: cError(hs.pos, "assignable expression expected.");
        }
    }

    public static Instruction increase2state(Tag tag, int id) {
        switch (tag) {
            case PREINC: case POSTINC:
                return id >= 0 ? new Vinc(id) : Inc.INSTANCE;
            case PREDEC: case POSTDEC:
                return id >= 0 ? new Vdec(id) : Dec.INSTANCE;
            default: throw new AssertionError();
        }
    }

    private boolean visitBody(Statement statement) {
        code.pushScope();
        visitStatement(statement);
        boolean alive = code.isAlive();
        code.popScope();
        return alive;
    }

    private void visitStatement(Statement statement) {
        if (statement == null) return;
        int prev_state = state;
        setState(STATE_NO_DECLS);
        statement.accept(this);
        state = prev_state;
    }

    private void beginCondition() {
        if (isState(STATE_COND)) {
            return;
        }
        pushMakeConditionChain();
    }

    private void endCondition() {
        if (isState(STATE_COND)) {
            return;
        }
        int ex = code.makeChain();
        emitPushTrue();
        emitGoto(ex);
        code.resolveChain(popConditionChain());
        emitPushFalse();
        code.resolveChain(ex);
    }

    private void emitPushLong(long value) {
        if (isShort(value)) {
            code.addInstruction(new Push((short) value));
        } else {
            code.addInstruction(new Ldc(code.resolveLong(value)));
        }
    }

    private void emitPushDouble(double value) {
        long lv = (long) value;
        if (false && lv == value && isShort(lv)) {
//            code.addInstruction(new Push(Operand.Type.LONG, (short) lv), 1);
        } else {
            code.addInstruction(new Ldc(code.resolveDouble(value)));
        }
    }

    private void emitPushString(String value) {
        code.addInstruction(new Ldc(code.resolveString(value)));
    }

    private static boolean isShort(long value) {
        return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
    }

    // emit methods

    private void emitPushTrue() { code.addInstruction(ConstTrue.CONST_TRUE); }
    private void emitPushFalse() { code.addInstruction(ConstFalse.CONST_FALSE); }
    private void emitGoto(int chainId) { code.addChainedInstruction(Goto::new, chainId); }
    private void emitDup() { code.addInstruction(Dup.INSTANCE); }
    private void emitDupX1() { code.addInstruction(Dup_x1.INSTANCE); }
    private void emitDupX2() { code.addInstruction(Dup_x2.INSTANCE); }
    private void emitDup2() { code.addInstruction(Dup2.INSTANCE); }
    private void emitDup2X1() { code.addInstruction(Dup2_x1.INSTANCE); }
    private void emitDup2X2() { code.addInstruction(Dup2_x2.INSTANCE); }
    private void emitAdd() { code.addInstruction(Add.INSTANCE); }
    private void emitAnd() { code.addInstruction(And.INSTANCE); }
    private void emitOr() { code.addInstruction(Or.INSTANCE); }
    private void emitXor() { code.addInstruction(Xor.INSTANCE); }
    private void emitDiv() { code.addInstruction(Div.INSTANCE); }
    private void emitLhs() { code.addInstruction(Shl.INSTANCE); }
    private void emitMul() { code.addInstruction(Mul.INSTANCE); }
    private void emitRem() { code.addInstruction(Rem.INSTANCE); }
    private void emitRhs() { code.addInstruction(Shr.INSTANCE); }
    private void emitSub() { code.addInstruction(Sub.INSTANCE); }
    private void emitALoad() { code.addInstruction(Aload.INSTANCE); }
    private void emitVLoad(String name) { code.addInstruction(new Vload(code.resolveLocal(name))); }
    private void emitAStore() { code.addInstruction(Astore.INSTANCE); } // todo: Тут тоже было sp<0, вроде при generateArrayCreation.
    private void emitVStore(String name) { code.addInstruction(new Vstore(code.resolveLocal(name))); }
    private void emitCaseBody(Statement body) {
        code.resolveChain(fallthroughChains.popInt());
        fallthroughChains.push(code.makeChain());
        int prev_state = state;
        setState(STATE_INFINITY_LOOP);
        visitStatement(body);
        if (!isState(STATE_INFINITY_LOOP)) emitGoto(breakChains.topInt());
        state = prev_state;
    }
    private void emitReturn() {
        code.addInstruction(jua.interpreter.instruction.Return.RETURN);
        code.dead();
    }

    private void cError(int position, String message) {
        log.error(position, message);
    }

    /* НИЖЕ РАСПОЛАГАЕТСЯ НОВЫЙ ЭКСПЕРИМЕНТАЛЬНЫЙ КОД */

    // Но его пока нет
}