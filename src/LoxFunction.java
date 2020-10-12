package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.LoxCallable;
import com.craftinginterpreters.lox.Return;

import java.util.List;

class LoxFunction implements LoxCallable
{
	private final Stmt.Function declaration;
	private final Environment closure;
    private final boolean isInitializer;

	LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
		this.declaration = declaration;
		this.closure = closure;
        this.isInitializer = isInitializer;
	}

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment, isInitializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments)
    {
    	Environment environment = new Environment(this.closure);

    	for (int i = 0; i < arguments.size(); ++i)
    	{
    		environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
    	}

    	try
    	{
	    	interpreter.executeBlock(declaration.body, environment);
    	}
    	catch (Return returnValue)
    	{
            if (isInitializer) return closure.getAt(0, "this");
    		return returnValue.value;
    	}

        if (isInitializer) return closure.getAt(0, "this");

    	return null;
    }

    @Override
    public int arity()
    {
        return declaration.parameters.size();
    }

    @Override
    public String toString()
    {
        return "<fn " + declaration.name.lexeme + ">";
    }

}