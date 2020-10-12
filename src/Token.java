package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*; 


class Token
{

	Token(TokenType type, String lexeme, Object literal, int line)
	{
		this.type = type;
		this.lexeme = lexeme;
		this.literal = literal;
		this.line = line + 1;
	}

	public String toString()
	{
		return type + " " + lexeme + " " + literal;
	}


	final TokenType type;
	final String 	lexeme;
	final Object 	literal;
	final int 		line;

}