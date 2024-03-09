package jua.stdlib;

import jua.runtime.Function;
import jua.runtime.heap.ListHeap;
import jua.runtime.heap.StringHeap;
import jua.runtime.interpreter.Address;
import jua.runtime.interpreter.Histogram;
import jua.stdlib.util.ObjectSizeAnalyzing;

import java.util.Arrays;
import java.util.Collection;

import static jua.stdlib.SignatureBuilder.builder;

public class Lib {

    private static final Function print = builder()
            .name("print")
            .optional()
            .optional("value", "")
            .callable((context, args, returnAddress) -> {
                Address stringAddress = new Address();
                args[0].stringVal(stringAddress);
                System.out.print(stringAddress.getStringHeap().toString());
            })
            .build();

    private static final Function println = builder()
            .name("println")
            .optional()
            .optional("value", "")
            .callable((context, args, returnAddress) -> {
                Address stringAddress = new Address();
                args[0].stringVal(stringAddress);
                System.out.println(stringAddress.getStringHeap().toString());
            })
            .build();

    private static final Function panic = builder()
            .name("panic")
            .flags(Function.FLAG_HIDDEN | Function.FLAG_KILLER)
            .optional()
            .optional("msg", null)
            .callable((context, args, returnAddress) -> {
                Address stringAddress = new Address();
                args[0].stringVal(stringAddress);
                context.error("panic: %s", stringAddress.getStringHeap());
            })
            .build();

    private static final Function hashCode = builder()
            .name("hashCode")
            .param("value")
            .callable((context, args, returnAddress) -> {
                returnAddress.set(args[0].hashCode());
            })
            .build();

    private static final Function strCharAt = builder()
            .name("strCharAt")
            .param("str")
            .param("index")
            .callable((context, args, returnAddress) -> {
                StringHeap str = args[0].getStringHeap();
                long index = args[1].getLong();
                if (index < 0 || index + 1 >= str.length()) {
                    returnAddress.set(-1);
                    return;
                }
                returnAddress.set(new StringHeap(str, (int)index, 1));
            })
            .build();

    private static final Function strCodePointAt = builder()
            .name("strCodePointAt")
            .param("str")
            .param("index")
            .callable((context, args, returnAddress) -> {
                StringHeap str = args[0].getStringHeap();
                long index = args[1].getLong();
                if (index < 0 || index >= str.length()) {
                    returnAddress.set(-1);
                    return;
                }
                returnAddress.set(str.codePointAt((int) index));
            })
            .build();

    private static final Function strToCharArray = builder()
            .name("strToCharArray")
            .param("str")
            .callable((context, args, returnAddress) -> {
                StringHeap str = args[0].getStringHeap();
                ListHeap charArray = new ListHeap(str.length());
                for (int codePoint : str.codePoints().toArray()) {
                    charArray.add().set(new StringHeap(new StringBuilder().appendCodePoint(codePoint)));
                }
                returnAddress.set(charArray);
            })
            .build();

    private static final Function strToCodePointArray = builder()
            .name("strToCodePointArray")
            .param("str")
            .callable((context, args, returnAddress) -> {
                StringHeap str = args[0].getStringHeap();
                ListHeap codePointArray = new ListHeap(str.length());
                for (int codePoint : str.codePoints().toArray()) {
                    codePointArray.add().set(codePoint);
                }
                returnAddress.set(codePointArray);
            })
            .build();

    private static final Function charArrayToStr = builder()
            .name("charArrayToStr")
            .param("charArray")
            .callable((context, args, returnAddress) -> {
                ListHeap charArray = args[0].getListHeap();
                StringHeap str = new StringHeap("", 0, charArray.length());
                for (int i = 0; i < charArray.length(); i++) {
                    str.append(charArray.get(i).getStringHeap());
                }
                returnAddress.set(str);
            })
            .build();

    private static final Function codePointArrayToStr = builder()
            .name("codePointArrayToStr")
            .param("codePointArray")
            .callable((context, args, returnAddress) -> {
                ListHeap codePointArray = args[0].getListHeap();
                StringHeap str = new StringHeap("", 0, codePointArray.length());
                for (int i = 0; i < codePointArray.length(); i++) {
                    int codePoint = (int) codePointArray.get(i).getLong();
                    str.append(new StringHeap(new StringBuilder().appendCodePoint(codePoint)));
                }
                returnAddress.set(str);
            })
            .build();

    private static final Function time = builder()
            .name("time")
            .callable((context, args, returnAddress) -> {
                returnAddress.set(System.currentTimeMillis() / 1000.0);
            })
            .build();

    private static final Function dummy = builder()
            .name("dummy")
            .callable((context, args, returnAddress) -> {
                returnAddress.setNull();
            })
            .build();

    private static final Function sleep = builder()
            .name("sleep")
            .param("timeout")
            .callable((context, args, returnAddress) -> {
                Address timeoutAddr = new Address();
                args[0].doubleVal(timeoutAddr);
                long millis = (long) (timeoutAddr.getDouble() * 1000L);
                // todo: nanos?
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    context.error("interrupted");
                }
                returnAddress.setNull();
            })
            .build();

    private static final Function invoke = builder()
            .name("invoke")
            .param("callee")
            .optional()
            .optional("args", new Object[0])
            .callable((context, args, returnAddress) -> {
                String name = args[0].getStringHeap().toString();
                context.directCall(name, args[1].getListHeap().getArray(), returnAddress);
            })
            .build();

    private static final Function ord = builder()
            .name("ord")
            .param("char")
            .callable((context, args, returnAddress) -> {
                returnAddress.set(args[0].getStringHeap().codePointAt(0));
            })
            .build();

    private static final Function chr = builder()
            .name("chr")
            .param("codePoint")
            .callable((context, args, returnAddress) -> {
                int codePoint = (int) args[0].getLong();
                returnAddress.set(new StringHeap(new StringBuilder().appendCodePoint(codePoint)));
            })
            .build();

    private static final Function typeof = builder()
            .name("typeof")
            .param("value")
            .callable((context, args, returnAddress) -> {
                returnAddress.set(new StringHeap(args[0].getTypeName()));
            })
            .build();

    private static final Function substr = builder()
            .name("substr")
            .param("str")
            .param("offset")
            .optional()
            .optional("count", null)
            .callable((context, args, returnAddress) -> {
                StringHeap str = args[0].getStringHeap();
                int offset = (int) args[1].getLong();
                int count;
                if (args[2].isNull()) {
                    if (offset >= 0) {
                        count = str.length() - offset;
                    } else {
                        count = -offset;
                    }
                } else {
                    count = (int) args[2].getLong();
                }

                if (offset >= 0) {
                    returnAddress.set(new StringHeap(str, offset, count));
                } else {
                    // a - (-b) = a + b
                    returnAddress.set(new StringHeap(str, str.length() + offset, count));
                }
            })
            .build();

    private static final Function strLowerCase = builder()
            .name("strLowerCase")
            .param("str")
            .callable((context, args, returnAddress) -> {
                StringHeap str = args[0].getStringHeap();
                returnAddress.set(new StringHeap(str.toString().toLowerCase()));
            })
            .build();

    private static final Function strUpperCase = builder()
            .name("strUpperCase")
            .param("str")
            .callable((context, args, returnAddress) -> {
                StringHeap str = args[0].getStringHeap();
                returnAddress.set(new StringHeap(str.toString().toUpperCase()));
            })
            .build();

    private static final Function strTrim = builder()
            .name("strTrim")
            .param("str")
            .callable((context, args, returnAddress) -> {
                StringHeap str = args[0].getStringHeap();
                returnAddress.set(new StringHeap(str.toString().trim()));
            })
            .build();

    private static final Function sizeof = builder()
            .name("sizeof")
            .param("value")
            .callable((context, args, returnAddress) -> {
                returnAddress.set(ObjectSizeAnalyzing.analyzeSize(args[0]));
            })
            .build();

    private static final Function histogramAction = builder()
            .name("histogramAction")
            .param("actionId")
            .callable((context, args, returnAddress) -> {
                switch ((int)args[0].getLong()) {
                    case 0:
                        Histogram.enable();
                        break;
                    case 1:
                        Histogram.get().print();
                        break;
                    case 2:
                        Histogram.disable();
                        break;
                }
                returnAddress.setNull();
            })
            .build();


    public static Collection<Function> getFunctions() {
        return Arrays.asList(
                print,
                println,
                panic,
                hashCode,
                strCharAt,
                strCodePointAt,
                strToCharArray,
                strToCodePointArray,
                charArrayToStr,
                codePointArrayToStr,
                time,
                dummy,
                sleep,
                invoke,
                ord,
                chr,
                typeof,
                substr,
                strLowerCase,
                strUpperCase,
                strTrim,
                sizeof,
                histogramAction
        );
    }
}
