package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.Token.*;

import java.util.HashMap;
import java.util.Map;

class Environment
{
	final Environment enclosing;

	private final Map<String, Object> values = new HashMap<>();

	Environment() {
		enclosing = null;
	}

	Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}

	void define(String name, Object value)
	{
		values.put(name, value);
	}

	void assign(Token name, Object value)
	{
		if (values.containsKey(name.lexeme))
		{
			values.put(name.lexeme, value);
			return;
		}

		if (enclosing != null) {
			enclosing.assign(name, value);
			return;
		}

		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'");
	}

	void assignAt(Integer distance, Token name, Object value)
	{
		if (distance <= 0) {
			assign(name, value);
			return;
		}

		if (enclosing != null) {
			enclosing.assignAt(distance - 1, name, value);
			return ;
		}

		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'");
	}

	Object get(Token name)
	{
		if (values.containsKey(name.lexeme))
		{
			return values.get(name.lexeme);
		}

		if (enclosing != null) return enclosing.get(name);

		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'");
	}

	Object getAt(Integer distance, Token name)
	{
		if (distance <= 0) {
			return get(name);
		}

		if (enclosing != null)
			return enclosing.getAt(distance - 1, name);

		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'");
	}

	Object getAt(Integer distance, String name) {
		return getAt(distance, new Token(TokenType.STRING, name, null, -1));
	}
}