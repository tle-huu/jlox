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

abstract class Stmt
{
	interface Visitor<R>
	{
		R visitClassStmt(Class stmt);
		R visitReturnStmt(Return stmt);
		R visitFunctionStmt(Function stmt);
		R visitBlockStmt(Block stmt);
		R visitIfStmt(If stmt);
		R visitWhileStmt(While stmt);
		R visitVarStmt(Var stmt);
		R visitExpressionStmt(Expression stmt);
		R visitPrintStmt(Print stmt);
	}

	static class Class extends Stmt
	{
		Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods, List<Stmt> fields)
		{
			this.name = name;
			this.superclass = superclass;
			this.methods = methods;
			this.fields = fields;
		}

		Token name;
		Expr.Variable superclass;
		List<Stmt.Function> methods;
		List<Stmt> fields;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitClassStmt(this);
		}
	}

	static class Return extends Stmt
	{
		Return(Token keyword, Expr value)
		{
			this.keyword = keyword;
			this.value = value;
		}

		Token keyword;
		Expr value;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitReturnStmt(this);
		}
	}

	static class Function extends Stmt
	{
		Function(Token name, List<Token> parameters, List<Stmt> body)
		{
			this.name = name;
			this.parameters = parameters;
			this.body = body;
		}

		Token name;
		List<Token> parameters;
		List<Stmt> body;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitFunctionStmt(this);
		}
	}

	static class Block extends Stmt
	{
		Block(List<Stmt> statements)
		{
			this.statements = statements;
		}

		List<Stmt> statements;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitBlockStmt(this);
		}
	}

	static class If extends Stmt
	{
		If(Expr condition, Stmt thenBranch, Stmt elseBranch)
		{
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		Expr condition;
		Stmt thenBranch;
		Stmt elseBranch;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitIfStmt(this);
		}
	}

	static class While extends Stmt
	{
		While(Expr condition, Stmt body)
		{
			this.condition = condition;
			this.body = body;
		}

		Expr condition;
		Stmt body;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitWhileStmt(this);
		}
	}

	static class Var extends Stmt
	{
		Var(Token token, Expr initializer)
		{
			this.token = token;
			this.initializer = initializer;
		}

		Token token;
		Expr initializer;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitVarStmt(this);
		}
	}

	static class Expression extends Stmt
	{
		Expression(Expr expression)
		{
			this.expression = expression;
		}

		Expr expression;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitExpressionStmt(this);
		}
	}

	static class Print extends Stmt
	{
		Print(Expr expression)
		{
			this.expression = expression;
		}

		Expr expression;

		<R> R accept(Visitor<R> visitor)
		{
			return visitor.visitPrintStmt(this);
		}
	}


	abstract <R> R accept(Visitor<R> visitor);
}
