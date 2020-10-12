package com.craftinginterpreters.lox;

import java.lang.StringBuilder;
import static com.craftinginterpreters.lox.Token.*;
import static com.craftinginterpreters.lox.TokenType.*; 

class AstPrinter implements Expr.Visitor<String>
{

	String print(Expr expr)
	{
		return expr.accept(this);
	}

    @Override
    public String visitThisExpr(Expr.This expr) {
        return null;
    }

	@Override
	public String visitSetExpr(Expr.Set expr) { return null;}

	@Override
	public String visitGetExpr(Expr.Get expr) { return null; }

    @Override
    public String visitCallExpr(Expr.Call expr) { return null; }


    @Override
    public String visitLogicalExpr(Expr.Logical expr)
    {
        return null;
    }
    @Override
    public String visitSuperExpr(Expr.Super expr) { return null;}


    @Override
    public String visitAssignExpr(Expr.Assign expr)
    {
        return null;
    }

	@Override
	public String visitBinaryExpr(Expr.Binary expr)
	{
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping expr)
	{
		return parenthesize("group", expr.expression);
	}

	@Override
	public String visitLiteralExpr(Expr.Literal expr)
	{
		if (expr.value == null) return "nil";

		return expr.value.toString();
	}

	@Override
	public String visitUnaryExpr(Expr.Unary expr)
	{
		return parenthesize(expr.operator.lexeme, expr.right);
	}

    @Override
    public String visitVariableExpr(Expr.Variable expr)
    {
        return null;
    }


	private String parenthesize(String name, Expr... exprs)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("(").append(name);
		for (Expr expr : exprs)
		{
			builder.append(" ");
			builder.append(expr.accept(this));
		}
		builder.append(")");

		return builder.toString();
	}

	public static void main(String [] argv)
	{

	    Expr expression = new Expr.Binary(
	        new Expr.Literal("a"),
	        new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
	        new Expr.Binary(
	        		new Expr.Literal("b"),
			        new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
	        		new Expr.Binary(
		        		new Expr.Literal("c"),
				        new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
		        		new Expr.Literal("d")
	        			)
	        		)
	        );

		AstPrinter ast_printer = new AstPrinter();

		String expression_string = ast_printer.print(expression);

		System.out.println(expression_string);

	}

}