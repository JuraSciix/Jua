package jua.interpreter.lang;

import jua.interpreter.Environment;

public interface Function {

    void call(Environment env, String name, int argc);
}
