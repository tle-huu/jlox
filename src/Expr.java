/*********************************/
/*                               */
/*                               */
/*      AUTO GENERATED FILE      */
/*      DO NOT MODIFY  !!!       */
/*                               */
/*                               */
/*********************************/

package com.craftinginterpreters.lox;

import java.util.List;
import static com.craftinginterpreters.lox.Token.*;

abstract class Expr
{
	interface Visitor<R>
	{
		R visitSuperExpr(Super expr);
		R visitThisExpr(This expr);
		R visitSetExpr(Set expr);
		R visitGetExpr(Get expr);
		R visitAssignExpr(Assign expr);
		R visitVariableExpr(Variable expr);
		R visitLogicalExpr(Logical expr);
		R visitBinaryExpr(Binary expr);
		R visitGroupingExpr(Grouping expr);
		R visitLiteralExpr(Literal expr);
		R visitUnaryExpr(Unary expr);
		R visitCallExpr(Call expr);
	}

	static class Super extends Expr
	{
		Super(Token keyword, Token method)
		{
			this.keyword = keyword;
			this.method = method;
		}

		Token keyword;
		Token method;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitSuperExpr(this);
		}
	}

	static class This extends Expr
	{
		This(Token keyword)
		{
			this.keyword = keyword;
		}

		Token keyword;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitThisExpr(this);
		}
	}

	static class Set extends Expr
	{
		Set(Expr object, Token name, Expr value)
		{
			this.object = object;
			this.name = name;
			this.value = value;
		}

		Expr object;
		Token name;
		Expr value;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitSetExpr(this);
		}
	}

	static class Get extends Expr
	{
		Get(Expr object, Token name)
		{
			this.object = object;
			this.name = name;
		}

		Expr object;
		Token name;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitGetExpr(this);
		}
	}

	static class Assign extends Expr
	{
		Assign(Token name, Expr value)
		{
			this.name = name;
			this.value = value;
		}

		Token name;
		Expr value;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitAssignExpr(this);
		}
	}

	static class Variable extends Expr
	{
		Variable(Token name)
		{
			this.name = name;
		}

		Token name;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitVariableExpr(this);
		}
	}

	static class Logical extends Expr
	{
		Logical(Expr left, Token operator, Expr right)
		{
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		Expr left;
		Token operator;
		Expr right;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitLogicalExpr(this);
		}
	}

	static class Binary extends Expr
	{
		Binary(Expr left, Token operator, Expr right)
		{
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		Expr left;
		Token operator;
		Expr right;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitBinaryExpr(this);
		}
	}

	static class Grouping extends Expr
	{
		Grouping(Expr expression)
		{
			this.expression = expression;
		}

		Expr expression;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitGroupingExpr(this);
		}
	}

	static class Literal extends Expr
	{
		Literal(Object value)
		{
			this.value = value;
		}

		Object value;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitLiteralExpr(this);
		}
	}

	static class Unary extends Expr
	{
		Unary(Token operator, Expr right)
		{
			this.operator = operator;
			this.right = right;
		}

		Token operator;
		Expr right;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitUnaryExpr(this);
		}
	}

	static class Call extends Expr
	{
		Call(Expr callee, Token paren, List<Expr> arguments)
		{
			this.callee = callee;
			this.paren = paren;
			this.arguments = arguments;
		}

		Expr callee;
		Token paren;
		List<Expr> arguments;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitCallExpr(this);
		}
	}


	abstract <R> R accept(Visitor<R> visitor);
}
