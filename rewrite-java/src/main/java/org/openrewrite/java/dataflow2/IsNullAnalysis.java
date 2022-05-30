package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.Collection;
import java.util.List;

public class IsNullAnalysis extends DataFlowAnalysis<Ternary>
{
    public Ternary join(Collection<Ternary> outs) {
        Ternary result = null;
        for(Ternary out : outs) {
            if(result == null) {
                result = out;
            } else {
                if(result == Ternary.DefinitelyYes && out != Ternary.DefinitelyYes) {
                    result = Ternary.CantTell;
                } else if(result == Ternary.DefinitelyNo && out != Ternary.DefinitelyNo) {
                    result = Ternary.CantTell;
                }
            }
        }
        if(result == null) result = Ternary.CantTell;
        return result;
    }

    public Ternary defaultTransfer(Cursor c, JavaType.Variable v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ternary transferBinary(Cursor c, JavaType.Variable storeOfInterest) {
        J.Binary pp = (J.Binary)c.getValue();
        return inputState(c, storeOfInterest);
    }

    public Ternary transferNamedVariable(Cursor c, JavaType.Variable storeOfInterest) {
        return inputState(c, storeOfInterest);
    }

    public Ternary transferAssignment(Cursor c, JavaType.Variable storeOfInterest) {
        J.Assignment a = (J.Assignment)c.getValue();
        if(a.getVariable() instanceof J.Identifier) {
            J.Identifier ident = (J.Identifier)a.getVariable();
            if(ident.getFieldType() == storeOfInterest) {
                return outputState(new Cursor(c, a.getAssignment()), storeOfInterest);
            } else {
                return inputState(c, storeOfInterest);
            }
        } else {
            return Ternary.CantTell;
        }
    }

    public Ternary transferMethodInvocation(Cursor c, JavaType.Variable storeOfInterest) {
        J.MethodInvocation m = (J.MethodInvocation)c.getValue();
        JavaType.Method type = m.getMethodType();
        if(type == null) {
            return Ternary.CantTell;
        } else switch(type.toString()) {
            case "java.lang.String{name=toUpperCase,return=java.lang.String,parameters=[]}":
                return Ternary.DefinitelyNo;
            default:
                return Ternary.CantTell; // unknown method
        }
        // return inputState(c, storeOfInterest);
    }

    @Override
    public Ternary transferLiteral(Cursor c, JavaType.Variable storeOfInterest) {
        J.Literal pp = (J.Literal)c.getValue();
        if(pp.getValue() == null) {
            return Ternary.DefinitelyYes;
        } else {
            return Ternary.DefinitelyNo;
        }
    }

    public Ternary transferIdentifier(Cursor c, JavaType.Variable storeOfInterest) {
        J.Identifier pp = (J.Identifier)c.getValue();
        return inputState(c, storeOfInterest);
    }

    public Ternary transferEmpty(Cursor c, JavaType.Variable storeOfInterest) {
        return inputState(c, storeOfInterest);
    }

    @Override
    public Ternary transferIf(Cursor c, JavaType.Variable storeOfInterest) {
        return inputState(c, storeOfInterest);
    }

    @Override
    public Ternary transferIfElse(Cursor c, JavaType.Variable storeOfInterest) {
        J.If.Else ifElse = (J.If.Else)c.getValue();
        return outputState(new Cursor(c, ifElse.getBody()), storeOfInterest);
    }

    @Override
    public Ternary transferBlock(Cursor c, JavaType.Variable storeOfInterest) {
        J.Block block = (J.Block)c.getValue();
        List<Statement> stmts = block.getStatements();
        if(stmts.size() > 0) {
            return outputState(new Cursor(c, stmts.get(stmts.size()-1)), storeOfInterest);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }
}

