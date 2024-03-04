package jua.stdlib;

import jua.runtime.Function;
import jua.runtime.NativeExecutor;
import jua.runtime.interpreter.Address;
import jua.runtime.interpreter.AddressSupport;
import jua.runtime.interpreter.AddressUtils;

import java.util.ArrayList;

public interface NativeSupport {

    /** Представление нативной функции. */
    abstract class NativeFunctionPresent implements NativeExecutor {

        /** Название функции. */
        private final String name;

        /** Информация о параметрах. */
        private final ParamsData paramsData;

        protected NativeFunctionPresent(String name, ParamsData paramsData) {
            this.name = name;
            this.paramsData = paramsData;
        }

        public final Function build() {
//            String module = getClass()
//                    .getProtectionDomain()
//                    .getCodeSource()
//                    .getLocation()
//                    .getFile();
            String module = "stdlib";
            String[] params = paramsData.params.stream()
                    .map(param -> param.name)
                    .toArray(String[]::new);
            Address[] defaults = paramsData.params.stream()
                    .filter(param -> AddressUtils.valid(param.defaultValue))
                    .map(param -> param.defaultValue)
                    .toArray(Address[]::new);
            return new Function(name, module, params.length - defaults.length, params.length, params, defaults, Function.FLAG_NATIVE, null, this);
        }
    }

    class ParamsData {
        public static ParamsData create() { return new ParamsData(); }
        private static class Param {
            final String name;
            final Address defaultValue;

            Param(String name, Address defaultValue) {
                this.name = name;
                this.defaultValue = defaultValue;
            }
        }
        final ArrayList<Param> params = new ArrayList<>();

        public ParamsData add(String name) {
            params.add(new Param(name, null));
            return this;
        }

        public ParamsData optional(String name, Object value) {
            Address defaultVal = new Address();
            AddressSupport.assignObject(defaultVal, value);
            params.add(new Param(name, defaultVal));
            return this;
        }
    }
}
