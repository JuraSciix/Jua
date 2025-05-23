package jua.runtime.interpreter;

public interface OPCodes {
    int
            Nop = 0,
            ConstNull = 1,
            ConstTrue = 2,
            ConstFalse = 3,
            ConstIntM1 = 4,
            ConstInt0 = 5,
            ConstInt1 = 6,
            ConstInt2 = 7,
            Dup = 8,
            DupX1 = 9,
            DupX2 = 10,
            Swap = 11,
            Dup2 = 12,
            Dup2X1 = 13,
            Dup2X2 = 14,
            Push = 15,
            NewList = 16,
            NewMap = 17,
            Pop = 18,
            Pop2 = 19,
            Add = 20,
            Sub = 21,
            Mul = 22,
            Div = 23,
            Rem = 24,
            And = 25,
            Or = 26,
            Xor = 27,
            Shl = 28,
            Shr = 29,
            Length = 30,
            Pos = 31,
            Neg = 32,
            Not = 33,
            Load = 34,
            Load0 = 35,
            Load1 = 36,
            Load2 = 37,
            Store = 38,
            Store0 = 39,
            Store1 = 40,
            Store2 = 41,
            Inc = 42,
            Dec = 43,
            ArrayLoad = 44,
            ArrayStore = 45,
            ArrayDec = 46,
            ArrayInc = 47,
            Goto = 48,
            IfEq = 49,
            IfNe = 50,
            IfGe = 51,
            IfLt = 52,
            IfGt = 53,
            IfLe = 54,
            IfZ = 55,
            IfNz = 56,
            IfNull = 57,
            IfNonNull = 58,
            IfPresent = 59,
            IfAbsent = 60,
            LinearSwitch = 61,
            BinarySwitch = 62,
            Call = 63,
            Return = 64,
            Leave = 65,
            // Мнимые инструкции, которые нужны лишь для гистограммы
            Shload = 66,
            Shstore = 67,

    _InstrCount = Shstore + 1;

    String[] NAMES = {
            "Nop",
            "ConstNull",
            "ConstTrue",
            "ConstFalse",
            "ConstIntM1",
            "ConstInt0",
            "ConstInt1",
            "ConstInt2",
            "Push",
            "NewList",
            "NewMap",
            "Dup",
            "Dup2",
            "Dup2X1",
            "Dup2X2",
            "DupX1",
            "DupX2",
            "Pop",
            "Pop2",
            "Add",
            "Sub",
            "Mul",
            "Div",
            "Rem",
            "And",
            "Or",
            "Xor",
            "Shl",
            "Shr",
            "Length",
            "Pos",
            "Neg",
            "Not",
            "Load",
            "Load0",
            "Load1",
            "Load2",
            "Store",
            "Store0",
            "Store1",
            "Store2",
            "Inc",
            "Dec",
            "ArrayLoad",
            "ArrayStore",
            "ArrayDec",
            "ArrayInc",
            "Goto",
            "IfEq",
            "IfNe",
            "IfGe",
            "IfLt",
            "IfGt",
            "IfLe",
            "IfZ",
            "IfNz",
            "IfNull",
            "IfNonNull",
            "IfPresent",
            "IfAbsent",
            "LinearSwitch",
            "BinarySwitch",
            "", // skipped
            "Call",
            "Return",
            "Leave",
            "_JoinFrame",
            "_PopFrame",
            "_JoinNativeFrame",
            "_PopNativeFrame"
    };
}
