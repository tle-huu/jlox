package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class LoxInstance
{
    private final Map<String, Object> fields = new HashMap<>();
    protected LoxClass klass;

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    protected LoxInstance() {
        this.klass = null;
    }

    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        LoxFunction method = klass.findMethod(name.lexeme);
        if (method != null) {
            return method.bind(this);
        }

        throw new RuntimeError(name, "Undefined properties " + name.lexeme + " in instance of type " + klass.name + ".");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);

    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}