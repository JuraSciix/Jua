package jua.vm;

import jua.vm.arena.DataArena;

import static jua.vm.SimpleType.*;

public class SimpleArithm {

    public static final int STATE_DONE = 0;
    public static final int STATE_LEAVE = 5;
    public static final int STATE_ERROR = 10;
    public static final int STATE_ERR_DBZ = 11;

    public static int typePair(DataArena arena, int top) {
        return typeUnionOf(arena.readType(top - 2), arena.readType(top - 1));
    }

    // ВНИМАНИЕ!
    // Операции несут ответственность за ссылки, оставляемые на стеке.
    // Если не очищать ссылки, то они перезапишутся впоследствии,
    // а сам объект останется висеть в памяти.

    public static int add(DataArena arena, int top) {
        int tp = typePair(arena, top);

        if (typeUnionOf(TYPE_INT64, TYPE_INT64) == tp) {
            arena.writeInt64(top - 2, arena.readInt64(top - 2) + arena.readInt64(top - 1));
            return STATE_DONE;
        }
        if (typeUnionOf(TYPE_FLOAT64, TYPE_FLOAT64) == tp) {
            arena.writeFloat64(top - 2, arena.readFloat64(top - 2) + arena.readFloat64(top - 1));
            return STATE_DONE;
        }

        // Перегрузки оператора с конвертацией типов
        if (typeUnionOf(TYPE_FLOAT64, TYPE_INT64) == tp) {
            arena.writeFloat64(top - 2, arena.readFloat64(top - 2) + arena.readInt64(top - 1));
            return STATE_DONE;
        }
        if (typeUnionOf(TYPE_INT64, TYPE_FLOAT64) == tp) {
            arena.writeType(top - 2, TYPE_FLOAT64);
            arena.writeFloat64(top - 2, arena.readInt64(top - 2) + arena.readFloat64(top - 1));
            return STATE_DONE;
        }

        return STATE_ERROR;
    }

    public static int sub(DataArena arena, int top) {
        int tp = typePair(arena, top);

        if (typeUnionOf(TYPE_INT64, TYPE_INT64) == tp) {
            arena.writeInt64(top - 2, arena.readInt64(top - 2) - arena.readInt64(top - 1));
            return STATE_DONE;
        }
        if (typeUnionOf(TYPE_FLOAT64, TYPE_FLOAT64) == tp) {
            arena.writeFloat64(top - 2, arena.readFloat64(top - 2) - arena.readFloat64(top - 1));
            return STATE_DONE;
        }

        // Перегрузки оператора с конвертацией типов
        if (typeUnionOf(TYPE_FLOAT64, TYPE_INT64) == tp) {
            arena.writeFloat64(top - 2, arena.readFloat64(top - 2) - arena.readInt64(top - 1));
            return STATE_DONE;
        }
        if (typeUnionOf(TYPE_INT64, TYPE_FLOAT64) == tp) {
            arena.writeType(top - 2, TYPE_FLOAT64);
            arena.writeFloat64(top - 2, arena.readInt64(top - 2) - arena.readFloat64(top - 1));
            return STATE_DONE;
        }

        return STATE_ERROR;
    }

    public static int mul(DataArena arena, int top) {
        int tp = typePair(arena, top);

        if (typeUnionOf(TYPE_INT64, TYPE_INT64) == tp) {
            arena.writeInt64(top - 2, arena.readInt64(top - 2) * arena.readInt64(top - 1));
            return STATE_DONE;
        }
        if (typeUnionOf(TYPE_FLOAT64, TYPE_FLOAT64) == tp) {
            arena.writeFloat64(top - 2, arena.readFloat64(top - 2) * arena.readFloat64(top - 1));
            return STATE_DONE;
        }

        // Перегрузки оператора с конвертацией типов
        if (typeUnionOf(TYPE_FLOAT64, TYPE_INT64) == tp) {
            arena.writeFloat64(top - 2, arena.readFloat64(top - 2) * arena.readInt64(top - 1));
            return STATE_DONE;
        }
        if (typeUnionOf(TYPE_INT64, TYPE_FLOAT64) == tp) {
            arena.writeType(top - 2, TYPE_FLOAT64);
            arena.writeFloat64(top - 2, arena.readInt64(top - 2) * arena.readFloat64(top - 1));
            return STATE_DONE;
        }

        return STATE_ERROR;
    }

    public static int div(DataArena arena, int top) {
        int tp = typePair(arena, top);

        if (typeUnionOf(TYPE_INT64, TYPE_INT64) == tp) {
            long denominator = arena.readInt64(top - 1);
            if (denominator == 0L) {
                return STATE_ERR_DBZ;
            }
            arena.writeInt64(top - 2, arena.readInt64(top - 2) / denominator);
            return STATE_DONE;
        }

        if (typeUnionOf(TYPE_FLOAT64, TYPE_FLOAT64) == tp) {
            arena.writeFloat64(top - 2, arena.readFloat64(top - 2) / arena.readFloat64(top - 1));
            return STATE_DONE;
        }

        // Перегрузки оператора с конвертацией типов

        if (typeUnionOf(TYPE_FLOAT64, TYPE_INT64) == tp) {
            arena.writeFloat64(top - 2, arena.readFloat64(top - 2) / arena.readInt64(top - 1));
            return STATE_DONE;
        }

        if (typeUnionOf(TYPE_INT64, TYPE_FLOAT64) == tp) {
            arena.writeType(top - 2, TYPE_FLOAT64);
            arena.writeFloat64(top - 2, arena.readInt64(top - 2) / arena.readFloat64(top - 1));
            return STATE_DONE;
        }

        return STATE_ERROR;
    }

    // ...

    public static int inc(DataArena arena, int addr, long step) {
        switch (arena.readType(addr)) {
            case TYPE_INT64:
                arena.writeInt64(addr, arena.readInt64(addr) + step);
                return STATE_DONE;
            case TYPE_FLOAT64:
                arena.writeFloat64(addr, arena.readFloat64(addr) + step);
                return STATE_DONE;
        }
        return STATE_ERROR;
    }

    public static boolean compare(DataArena arena, int top, boolean eq, boolean gt, boolean lt) {
        int tp = typePair(arena, top);

        if (typeUnionOf(TYPE_INT64, TYPE_INT64) == tp) {
            long lhs = arena.readInt64(top - 2);
            long rhs = arena.readInt64(top - 1);
            return eq && lhs == rhs || gt && lhs > rhs || lt && lhs < rhs;
        }

        if (typeUnionOf(TYPE_FLOAT64, TYPE_FLOAT64) == tp) {
            double lhs = arena.readFloat64(top - 2);
            double rhs = arena.readFloat64(top - 1);
            return eq && lhs == rhs || gt && lhs > rhs || lt && lhs < rhs;
        }

        return false;
    }

    public static boolean compareInt64Zero(DataArena arena, int top) {

        return false;
    }

    public static boolean compareRefNull(DataArena arena, int top) {

        return false;
    }
}
