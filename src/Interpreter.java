package com.craftinginterpreters.lox;

import java.util.List;


import com.craftinginterpreters.lox.Expr;
import com.craftinginterpreters.lox.Stmt;
import com.craftinginterpreters.lox.LoxCallable;
import com.craftinginterpreters.lox.LoxInstance;
import com.craftinginterpreters.lox.RuntimeError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>
{
    final Environment globals = new Environment();

    private final Map<Expr, Integer> locals = new HashMap<>();

    private Environment environment = globals;

    Interpreter()
    {
        globals.define("clock", new LoxCallable() {
                    
                    @Override
                    public int arity() { return 0;}

                    @Override
                    public Object call(Interpreter interpreter, List<Object> arguments) {
                        return (double)System.currentTimeMillis() / 1000.0;
                    }

                    @Override
                    public String toString() { return "clock: <native fn>";}
                }
        );
    }

    void interpret(List<Stmt> statements)
    {
        try
        {
            for (Stmt statement : statements)
            {
                if (statement == null)
                {
                    return;
                }
                execute(statement);
            }
        }
        catch (RuntimeError error)
        {
            Lox.runtimeError(error);
        }
    }

    private Object evaluate(Expr expr)
    {
        return expr.accept(this);
    }

    private void execute(Stmt statement)
    {
        if (statement == null)
        {
            System.out.println("[FATAL INTERNAL ERROR] Interpreter tried to execute a null statement !");

        }
        statement.accept(this);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {

        return lookUpVariable(expr.keyword, expr);
    }


    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.name, "Only instance have fields.");
        }

        Object value = evaluate(expr.value);
        ((LoxInstance)object).set(expr.name, value);

        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {

        int distance = locals.get(expr);

        LoxClass superclass = (LoxClass)environment.getAt(distance, expr.keyword);
        LoxInstance instance = (LoxInstance)environment.getAt(distance - 1, "this");
        LoxFunction method = superclass.findMethod(expr.method.lexeme);

        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property " + expr.method.lexeme + ".");
        }

        return method.bind(instance);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {

        Object object = evaluate(expr.object);

        if (object instanceof LoxInstance) {
            return ((LoxInstance)object).get(expr.name);
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt)
    {
        Object superclass = null;
        if (stmt.superclass != null)
        {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
            }
        }

        environment.define(stmt.name.lexeme, null);

        if (superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Function func : stmt.methods) {
            methods.put(func.name.lexeme, new LoxFunction(func, environment, func.name.lexeme.equals("init")));
        }
        LoxClass klass = new LoxClass(stmt.name.lexeme, (LoxClass)superclass, methods);

        for (Stmt field : stmt.fields)
        {
            if (field instanceof Stmt.Function)
            {
                klass.set(((Stmt.Function)field).name, new LoxFunction(((Stmt.Function)field), environment, false));
            }
            else if (field instanceof Stmt.Var)
            {
                Object value = null;
                if (((Stmt.Var)field).initializer != null) value = evaluate(((Stmt.Var)field).initializer);
                klass.set(((Stmt.Var)field).token, value);
            }
        }

        if (superclass != null) environment = environment.enclosing;

        environment.assign(stmt.name, klass);
        return null;           
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt)
    {
        Object value = null;

        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt)
    {
        globals.define(stmt.name.lexeme, new LoxFunction(stmt, environment, false));
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr)
    {

        Object callee = evaluate(expr.callee);

        if (!(callee instanceof LoxCallable))
        {
            throw new RuntimeError(expr.paren, "Object not callable: can only call functions and classes");
        }

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments)
        {
            arguments.add(evaluate(argument));
        }

        LoxCallable function = (LoxCallable)callee;

        if (arguments.size() != function.arity())
        {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " number of arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }


    @Override
    public Void visitWhileStmt(Stmt.While stmt)
    {
        while (isTruthy(evaluate(stmt.condition)))
        {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr)
    {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR)
        {
            if (isTruthy(left)) return left;
        }
        else
        {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }


    @Override
    public Void visitIfStmt(Stmt.If stmt)
    {
        Object condition = evaluate(stmt.condition);

        if (isTruthy(condition))
        {
            execute(stmt.thenBranch);
        }
        else if (stmt.elseBranch != null)
        {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt)
    {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr)
    {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr.name);

        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        }
    else {
            environment.assign(expr.name, value);
        }

        return value;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr)
    {
        return lookUpVariable(expr.name, expr);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt)
    {
        Object value = null;

        if (stmt.initializer != null)
        {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.token.lexeme, value);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt)
    {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt)
    {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }


    @Override
    public Object visitLiteralExpr(Expr.Literal expr)
    {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr)
    {
        Object right = evaluate(expr.right);
        switch (expr.operator.type)
        {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr)
    {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr)
    {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type)
        {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String)
                {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");

            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return isEqual(left, right);
        }

        return null;
    }

    void checkNumberOperands(Token operator, Object left, Object right)
    {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");

    }

    void checkNumberOperand(Token operator, Object operand)
    {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private boolean isEqual(Object left, Object right)
    {
        if (left == null && right == null) return true;
        if (left == null) return false;
        return left.equals(right);
    }

    private Boolean isTruthy(Object object)
    {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;

        return true;
    }

    private String stringify(Object object)
    {
        if (object == null) return "nil";

        if (object instanceof Double)
        {
            String text = object.toString();

            if (text.endsWith(".0"))
            {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    void executeBlock(List<Stmt> statements, Environment environment)
    {
        Environment previous_environment = this.environment;

        try
        {
            this.environment = environment;
            for (Stmt statement : statements)
            {
                execute(statement);
            }
        }
        finally
        {
            this.environment = previous_environment;
        }
    }

    private Object lookUpVariable(Token name, Expr expr)
    {
        Integer distance = locals.get(expr);

        if (distance != null) {
            return environment.getAt(distance, name);
        }
        else {
            return globals.get(name);
        }
    }

}