package com.craftinginterpreters.lox;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;
import static com.craftinginterpreters.lox.Token.*;

class Scanner
{
	private final String source_code_;
	private final List<Token> tokens_ = new ArrayList<>();

	private int start = 0;
	private int current = 0;
	private int line = 0;

	private static final Map<String, TokenType> keywords;

	static {

		keywords = new HashMap<>();

		keywords.put("and", AND);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("fun", FUN);
		keywords.put("for", FOR);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);

		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	};

	Scanner(String source)
	{
		source_code_ = source;
	}


	List<Token> scanTokens()
	{

		while (!isAtEnd())
		{
			start = current;

			scanToken();
		}

		tokens_.add(new Token(EOF, "", null, line));
		return tokens_;
	}

	private void scanToken()
	{
		char c = advance();

		switch (c)
		{
			case '(': addToken(LEFT_PAREN); break;
			case ')': addToken(RIGHT_PAREN); break;
			case '{': addToken(LEFT_BRACE); break;
			case '}': addToken(RIGHT_BRACE); break;
			case ',': addToken(COMMA); break;
			case '.': addToken(DOT); break;
			case '-': addToken(MINUS); break;
			case '+': addToken(PLUS); break;
			case ';': addToken(SEMICOLON); break;
			case '*': addToken(STAR); break;

			case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
			case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
			case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
			case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;

			case '/':

				if (match('/'))
				{
					while (peek() != '\n' && !isAtEnd())
					{
						advance();
					}
				}
				else if (match('*'))
				{
					handle_long_comment();
				}
				else
				{
					addToken(SLASH);
				}
				break;

			case '\n':
				++line;
				break;

			// Ignored
			case ' ':
			case '\r':
			case '\t':
				break;

			// Literals
			case '"':
				handle_string();
				break;

			default:

				if (isDigit(c))
				{
					handle_number();
				}
				else if (isAlpha(c))
				{
					handle_identifier();
				}
				else
				{
					Lox.error(line, "Unexpected character: [" + c + "]");
				}

				break;
		}
	}

	private void handle_long_comment()
	{
		while (!isAtEnd() && (peek() != '/' && (peek() != '*' || peekNext() != '/')))
		{
			if (peek() == '\n')
			{
				++line;
			}
			advance();
		}

		if (isAtEnd())
		{
			Lox.error(line, "Unterminated comment.");
			return;
		}

		if (peek() == '/')
		{
			advance();
			if (peek() == '*')
			{
				advance();
			}
			// Close the nested long comment
			handle_long_comment();

			// Resume the scanning of the outter long comment
			handle_long_comment();
		}
		else
		{
			// Consuming the '/'
			advance();
			advance();
		}


	}

	private void handle_string()
	{
		while (peek() != '"' && !isAtEnd())
		{
			if (peek() == '\n')
			{
				++line;
			}

			advance();
		}

		if (isAtEnd())
		{
			Lox.error(line, "Unterminated string");
			return;
		}

		advance();

		String string_value = source_code_.substring(start + 1, current - 1);
		addToken(STRING, string_value);
	}

	private void handle_number()
	{
		while (isDigit(peek()))
		{
			advance();
		}

		if (peek() == '.' && isDigit(peekNext()))
		{
			// Consume the '.'
			advance();

			// Fractional part
			while (isDigit(peek()))
			{
				advance();
			}
		}
		addToken(NUMBER, Double.parseDouble(source_code_.substring(start, current)));
	}

	private void handle_identifier()
	{
		while (isAlphaNumeric(peek()))
		{
			advance();
		}

		String text = source_code_.substring(start, current);

		TokenType type = keywords.get(text);

		if (type == null) type = IDENTIFIER;

		addToken(type);
	}

	private char peek()
	{
		if (isAtEnd())
		{
			return '\0';
		}

		return source_code_.charAt(current);
	}

	private char peekNext()
	{
		if (current + 1 >= source_code_.length())
		{
			return '\0';
		}

		return source_code_.charAt(current + 1);
	}

	private char advance()
	{
		++current;

		return source_code_.charAt(current - 1);
	}

	private void addToken(TokenType type)
	{
		addToken(type, null);
	}

	private void addToken(TokenType type, Object literal)
	{
		String text = source_code_.substring(start, current);

		tokens_.add(new Token(type, text, literal, line));
	}

	private boolean match(char expected)
	{
		if (isAtEnd())
		{
			return false;
		}

		if (source_code_.charAt(current) != expected)
		{
			return false;
		}

		++current;
		return true;
	}

	private boolean isAtEnd()
	{
		return current >= source_code_.length();
	}

	private boolean isDigit(char c)
	{
		return c >= '0' && c <= '9';
	}

	private boolean isAlpha(char c )
	{
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	private boolean isAlphaNumeric(char c)
	{
		return isDigit(c) || isAlpha(c);
	}

}