package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

class LoxClass extends LoxInstance implements LoxCallable {
    final String name;
    final LoxClass superclass;

    private final Map<String, LoxFunction> methods;

    LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        super();
        this.klass = this;
        
        this.name = name;
        this.methods = methods;
        this.superclass = superclass;
    }

    LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }
        return null;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);

        LoxFunction initializer = findMethod("init");

        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            return initializer.arity();
        }
        return 0;
    }

    @Override
    public String toString() {
        return name;
    }
}