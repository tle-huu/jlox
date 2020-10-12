package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser
{

    private static class ParseError extends RuntimeException {};

    private final List<Token> tokens_;
    private int current_ = 0;

    Parser(List<Token> tokens)
    {
        tokens_ = tokens;
    }

    List<Stmt> parse()
    {
        try
        {
            List<Stmt> statements = new ArrayList<>();

            while (!isAtEnd())
            {
                statements.add(declaration());
            }
            return statements;            
        }
        catch (ParseError error)
        {
            return null;
        }
    }

    private Expr call()
    {
        Expr expr = primary();

        while (true)
        {
            if (match(LEFT_PAREN))
            {
                expr = finishCall(expr);
            }
            else if (match(DOT))
            {
                Token name = consume(IDENTIFIER, "Expect property name after a '.'");
                expr = new Expr.Get(expr, name);
            }
            else
            {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr expr)
    {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN))
        {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Cant't have more than 254 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expected closing '0' after function call");
        return new Expr.Call(expr, previous(), arguments);
    }

    private Stmt declaration()
    {
        try
        {
            if (match(VAR)) return varDeclaration();
            if (match(FUN)) return funDeclaration("function");
            if (match(CLASS)) return classDeclaration();
            return statement();
        }
        catch (ParseError error)
        {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration()
    {
        Token name = consume(IDENTIFIER, "Expect class name identifier after 'class' keyword.");
        Expr.Variable superclass = null;

        if (match(LESS)) {
            consume(IDENTIFIER, "Expect super class name.");
            superclass = new Expr.Variable(previous());
        }

        consume(LEFT_BRACE, "Expect opening '{' after class name declaration.");

        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt> fields = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd())
        {
            if (match(CLASS)) {
                if (match(VAR)) fields.add(varDeclaration());
                else fields.add(funDeclaration("static method"));
            }
            else {
                methods.add((Stmt.Function)funDeclaration("method"));
            }
        }

        consume(RIGHT_BRACE, "Expect closing '}' after class definition body.");

        return new Stmt.Class(name, superclass, methods, fields);
    }

    private Stmt funDeclaration(String kind)
    {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name identifier.");

        consume(LEFT_PAREN, "Expect opening '(' after " + kind + " declaration.");

        List<Token> parameters = new ArrayList<>();

        if (!check(RIGHT_PAREN))
        {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), kind + " Cannot have more than 254 parameters.");
                }

                parameters.add(consume(IDENTIFIER, "Expect identifier in " + kind + " parameters declaration."));

            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expect closing ')' after " + kind + " parameters declaration.");
        consume(LEFT_BRACE, "Expect opening '{' before " + kind + " body.");

        List<Stmt> body = block();

        return new Stmt.Function(name, parameters, body);
    }

    private Stmt varDeclaration()
    {
        Token name = consume(IDENTIFIER, "Expect variable name");

        Expr initializer = null;
        if (match(EQUAL))
        {
            initializer = expression();
        }
        consume(SEMICOLON, "Expected ';' after variable declaration");

        return new Stmt.Var(name, initializer);
    }


    private Stmt statement()
    {
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        if (match(IF)) return ifStatement();
        if (match(WHILE)) return whileStatement();
        if (match(FOR)) return forStatement();
        if (match(RETURN)) return returnStatement();

        return expressionStatement();
    }

    private Stmt returnStatement()
    {
        Token keyword = previous();
        Expr value = null;

        if (!check(SEMICOLON))
            value = expression();
        consume(SEMICOLON, "Expect terminating ';' after return statement.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt forStatement()
    {
        consume(LEFT_PAREN, "Expect opening '(' after for statement.");

        Stmt initializer;
        if (match(SEMICOLON))
        {
            initializer = null;
        }
        else if (match(VAR))
        {
            initializer = varDeclaration();
        }
        else
        {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON))
        {
            condition = expression();
        }
        else
        {
            condition = new Expr.Literal(true);
        }
        consume(SEMICOLON, "Expect ';' in for loop after condition");

        Expr increment = null;
        if (!check(RIGHT_PAREN))
        {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect closing ')' after for statement.");

        Stmt body = statement();

        List<Stmt> whileBlock = new ArrayList<>();
        whileBlock.add(body);
        if (increment != null)
            whileBlock.add(new Stmt.Expression(increment));
        Stmt whileStmt = new Stmt.While(condition, new Stmt.Block(whileBlock));

        List<Stmt> statements = new ArrayList<>();
        if (initializer != null)
            statements.add(initializer);
        statements.add(whileStmt);

        return new Stmt.Block(statements);
    }

    private Stmt whileStatement()
    {
        consume(LEFT_PAREN, "Expect opening '(' in while statement.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect closing ')' after while condition.");

        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement()
    {
        consume(LEFT_PAREN, "Expect opening '(' in if statement.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect closing ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE))
        {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private List<Stmt> block()
    {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd())
        {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect closing '}' after block.");

        return statements;

    }

    private Stmt printStatement()
    {
        Expr value = expression();

        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement()
    {
        Expr expr = expression();

        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression()
    {
        Expr expr = assignment();

        return expr;
    }

    private Expr assignment()
    {
        Expr expr = or();

        if (match(EQUAL))
        {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable)
            {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }
            else if (expr instanceof Expr.Get)
            {
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.object, get.name, value);
            }
            error(equals, "Invalid assignment target.");
        }

        return expr;

    }

    private Expr or()
    {
        Expr left = and();

        while (match(OR))
        {
            Token operator = previous();
            Expr right = and();
            left = new Expr.Logical(left, operator, right);
        }
        return left;
    }

    private Expr and()
    {
        Expr left = equality();

        while (match(AND))
        {
            Token operator = previous();
            Expr right = equality();
            left = new Expr.Logical(left, operator, right);
        }
        return left;
    }

    private Expr equality()
    {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL))
        {
            Token operator = previous();
            Expr right = comparison();

            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;

    }

    private Expr comparison()
    {
        Expr expr = addition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL))
        {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition()
    {
        Expr expr = multiplication();

        while (match(MINUS, PLUS))
        {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication()
    {
        Expr expr = unary();

        while (match(STAR, SLASH))
        {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary()
    {
        while (match(BANG, MINUS))
        {
            Token operator = previous();
            Expr right = unary();
            return  new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr primary()
    {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE))  return new Expr.Literal(true);
        if (match(NIL))   return new Expr.Literal(null);

        if (match(NUMBER, STRING))
        {
            return new Expr.Literal(previous().literal);
        }

        if (match(THIS)) {
            return new Expr.This(previous());
        }

        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expect '.' after 'super'");
            consume(IDENTIFIER, "Expect method name after 'super.'.");
            Token method = previous();
            return new Expr.Super(keyword, method);

        }

        if (match(LEFT_PAREN))
        {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect a closing ')' after expression");
            return new Expr.Grouping(expr);
        }

        if (match(IDENTIFIER))
        {
            return new Expr.Variable(previous());
        }

        throw error(peek(), "Expect expression.");
    }

    private void synchronize()
    {
        advance();

        while (!isAtEnd())
        {
            if (previous().type == SEMICOLON) return;

            switch(peek().type)
            {
                case IF:
                case CLASS:
                case FUN:
                case VAR:
                case WHILE:
                case PRINT:
                case FOR:
                case RETURN:
                    return;
            }
            advance();
        }

    }

    private boolean match(TokenType... types)
    {
        for (TokenType type : types)
        {
            if (check(type))
            {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type)
    {
        if (isAtEnd())
        {
            return false;
        }

        return peek().type == type;
    }

    private Token consume(TokenType type, String message)
    {
        if (check(type)) return advance();

        throw error(peek(), message);
    }


    private Token advance()
    {
        if (!isAtEnd())
        {
            ++current_;
        }
        return previous();
    }

    private Token previous()
    {
        return tokens_.get(current_ - 1);
    }

    private boolean isAtEnd()
    {
        return peek().type == EOF;
    }

    private Token peek()
    {
        return tokens_.get(current_);
    }

    private ParseError error(Token token, String message)
    {
        Lox.error(token, message);
        return new ParseError();
    }
}