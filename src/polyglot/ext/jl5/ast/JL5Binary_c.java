/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.ext.jl5.ast;

import polyglot.ast.Binary_c;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Precedence;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.TypeChecker;

public class JL5Binary_c extends Binary_c implements JL5Binary {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public JL5Binary_c(Position pos, Expr left, Operator op, Expr right) {
        super(pos, left, op, right);
    }

    /** Type check the expression. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Type l = left.type();
        Type r = right.type();

        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();

        if (!ts.isPrimitiveWrapper(l) && !ts.isPrimitiveWrapper(r)) {
            return super.typeCheck(tc);
        }
        // at least one of l or r is a primitive wrapper
        // If both of them are non null, then just use their primitive types
        if (!l.isNull() && !r.isNull()) {
            l = ts.isPrimitiveWrapper(l) ? ts.primitiveTypeOfWrapper(l) : l;
            r = ts.isPrimitiveWrapper(r) ? ts.primitiveTypeOfWrapper(r) : r;
        }

        if (op == GT || op == LT || op == GE || op == LE) {
            if (!l.isNumeric()) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + l
                        + ".", left.position());
            }

            if (!r.isNumeric()) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + r
                        + ".", right.position());
            }

            return type(ts.Boolean());
        }

        if (op == EQ || op == NE) {
            if (!ts.isCastValid(l, r) && !ts.isCastValid(r, l)) {
                throw new SemanticException("The "
                                                    + op
                                                    + " operator must have operands of similar type.",
                                            position());
            }

            return type(ts.Boolean());
        }

        if (op == COND_OR || op == COND_AND) {
            if (!l.isBoolean()) {
                throw new SemanticException("The " + op
                        + " operator must have boolean operands, not type " + l
                        + ".", left.position());
            }

            if (!r.isBoolean()) {
                throw new SemanticException("The " + op
                        + " operator must have boolean operands, not type " + r
                        + ".", right.position());
            }

            return type(ts.Boolean());
        }

        if (op == ADD) {
            if (ts.isSubtype(l, ts.String()) || ts.isSubtype(r, ts.String())) {
                if (!ts.canCoerceToString(r, tc.context())) {
                    throw new SemanticException("Cannot coerce an expression "
                                                        + "of type " + r
                                                        + " to a String.",
                                                right.position());
                }
                if (!ts.canCoerceToString(l, tc.context())) {
                    throw new SemanticException("Cannot coerce an expression "
                            + "of type " + l + " to a String.", left.position());
                }

                return precedence(Precedence.STRING_ADD).type(ts.String());
            }
        }

        if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
            if (l.isBoolean() && r.isBoolean()) {
                return type(ts.Boolean());
            }
        }

        if (op == ADD) {
            if (!l.isNumeric()) {
                throw new SemanticException("The "
                                                    + op
                                                    + " operator must have numeric or String operands, not type "
                                                    + l + ".",
                                            left.position());
            }

            if (!r.isNumeric()) {
                throw new SemanticException("The "
                                                    + op
                                                    + " operator must have numeric or String operands, not type "
                                                    + r + ".",
                                            right.position());
            }
        }

        if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
            if (!ts.isImplicitCastValid(l, ts.Long())) {
                throw new SemanticException("The "
                                                    + op
                                                    + " operator must have numeric or boolean operands, not type "
                                                    + l + ".",
                                            left.position());
            }

            if (!ts.isImplicitCastValid(r, ts.Long())) {
                throw new SemanticException("The "
                                                    + op
                                                    + " operator must have numeric or boolean operands, not type "
                                                    + r + ".",
                                            right.position());
            }
        }

        if (op == SUB || op == MUL || op == DIV || op == MOD) {
            if (!l.isNumeric()) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + l
                        + ".", left.position());
            }

            if (!r.isNumeric()) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + r
                        + ".", right.position());
            }
        }

        if (op == SHL || op == SHR || op == USHR) {
            if (!ts.isImplicitCastValid(l, ts.Long())) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + l
                        + ".", left.position());
            }

            if (!ts.isImplicitCastValid(r, ts.Long())) {
                throw new SemanticException("The " + op
                        + " operator must have numeric operands, not type " + r
                        + ".", right.position());
            }
        }

        if (op == SHL || op == SHR || op == USHR) {
            // For shift, only promote the left operand.
            return type(ts.promote(l));
        }

        return type(ts.promote(l, r));
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        Expr other;

        if (child == left) {
            other = right;
        }
        else if (child == right) {
            other = left;
        }
        else {
            return child.type();
        }

        JL5TypeSystem ts = (JL5TypeSystem) av.typeSystem();

        if (!ts.isPrimitiveWrapper(child.type())
                && !ts.isPrimitiveWrapper(other.type())) {
            return super.childExpectedType(child, av);
        }

        try {
            if (op == EQ || op == NE) {
                // Coercion to compatible types.
                if ((child.type().isReference() || child.type().isNull())
                        && (other.type().isReference() || other.type().isNull())) {
                    return ts.leastCommonAncestor(child.type(), other.type());
                }

                if (child.type().isBoolean() && other.type().isBoolean()) {
                    return ts.Boolean();
                }
                // Added case for unboxing
                if (child.type().isBoolean()) {
                    Type otherType = ts.primitiveTypeOfWrapper(other.type());
                    if ((otherType != null) && otherType.isBoolean())
                        return ts.Boolean();
                }
                // Added case for unboxing
                if (other.type().isBoolean()) {
                    Type childType = ts.primitiveTypeOfWrapper(child.type());
                    if ((childType != null) && childType.isBoolean())
                        return ts.Boolean();
                }

                if (child.type().isNumeric() && other.type().isNumeric()) {
                    return ts.promote(child.type(), other.type());
                }
                // Added case for unboxing
                if (child.type().isNumeric()) {
                    Type otherType = ts.primitiveTypeOfWrapper(other.type());
                    if ((otherType != null) && otherType.isNumeric())
                        return ts.promote(child.type(), otherType);
                }
                // Added case for unboxing
                if (other.type().isNumeric()) {
                    Type childType = ts.primitiveTypeOfWrapper(child.type());
                    if ((childType != null) && childType.isNumeric())
                        return ts.promote(childType, other.type());
                }

                if (child.type().isImplicitCastValid(other.type())) {
                    return other.type();
                }

                return child.type();
            }

            if (op == ADD && ts.typeEquals(type, ts.String())) {
                // Implicit coercion to String.
                return ts.String();
            }

            if (op == GT || op == LT || op == GE || op == LE) {
                if (child.type().isNumeric() && other.type().isNumeric()) {
                    return ts.promote(child.type(), other.type());
                }
                // Added case for unboxing
                if (child.type().isNumeric()) {
                    Type otherType = ts.primitiveTypeOfWrapper(other.type());
                    if ((otherType != null) && otherType.isNumeric())
                        return ts.promote(child.type(), otherType);
                }
                // Added case for unboxing
                if (other.type().isNumeric()) {
                    Type childType = ts.primitiveTypeOfWrapper(child.type());
                    if ((childType != null) && childType.isNumeric())
                        return ts.promote(childType, other.type());
                }

                // Added case for unboxing, when both operands are wrappers
                Type otherType = ts.primitiveTypeOfWrapper(other.type());
                Type childType = ts.primitiveTypeOfWrapper(child.type());
                if (otherType != null && childType != null
                        && otherType.isNumeric() && childType.isNumeric()) {
                    return ts.promote(childType, otherType);
                }

                return child.type();
            }

            if (op == COND_OR || op == COND_AND) {
                return ts.Boolean();
            }

            if (op == BIT_AND || op == BIT_OR || op == BIT_XOR) {
                if (other.type().isBoolean()) {
                    return ts.Boolean();
                }
                // Added case for unboxing
                if (child.type().isBoolean()) {
                    Type otherType = ts.primitiveTypeOfWrapper(other.type());
                    if ((otherType != null) && otherType.isBoolean())
                        return ts.Boolean();
                }
                // Added case for unboxing
                if (other.type().isBoolean()) {
                    Type childType = ts.primitiveTypeOfWrapper(child.type());
                    if ((childType != null) && childType.isBoolean())
                        return ts.Boolean();
                }

                if (child.type().isNumeric() && other.type().isNumeric()) {
                    return ts.promote(child.type(), other.type());
                }
                // Added case for unboxing
                if (child.type().isNumeric()) {
                    Type otherType = ts.primitiveTypeOfWrapper(other.type());
                    if ((otherType != null) && otherType.isNumeric())
                        return ts.promote(child.type(), otherType);
                }
                // Added case for unboxing
                if (other.type().isNumeric()) {
                    Type childType = ts.primitiveTypeOfWrapper(child.type());
                    if ((childType != null) && childType.isNumeric())
                        return ts.promote(childType, other.type());
                }

                // Added case for unboxing, when both operands are wrappers
                Type otherType = ts.primitiveTypeOfWrapper(other.type());
                Type childType = ts.primitiveTypeOfWrapper(child.type());

                if (otherType != null && childType != null
                        && otherType.isBoolean() && childType.isBoolean()) {
                    return childType;
                }

                if (otherType != null && childType != null
                        && otherType.isNumeric() && childType.isNumeric()) {
                    return ts.promote(childType, otherType);
                }

                return child.type();
            }

            if (op == ADD || op == SUB || op == MUL || op == DIV || op == MOD) {
                if (child.type().isNumeric() && other.type().isNumeric()) {
                    Type t = ts.promote(child.type(), other.type());

                    if (ts.isImplicitCastValid(t, av.toType())) {
                        return t;
                    }
                    else {
                        return av.toType();
                    }
                }

                // Added case for unboxing
                if (child.type().isNumeric()) {
                    Type otherType = ts.primitiveTypeOfWrapper(other.type());
                    if ((otherType != null) && otherType.isNumeric()) {
                        Type t = ts.promote(child.type(), otherType);
                        if (ts.isImplicitCastValid(t, av.toType())) {
                            return t;
                        }
                        else {
                            return av.toType();
                        }
                    }
                }
                // Added case for unboxing
                if (other.type().isNumeric()) {
                    Type childType = ts.primitiveTypeOfWrapper(child.type());
                    if ((childType != null) && childType.isNumeric()) {
                        Type t = ts.promote(childType, other.type());
                        if (ts.isImplicitCastValid(t, av.toType())) {
                            return t;
                        }
                        else {
                            return av.toType();
                        }
                    }
                }
                // Added case for unboxing, when both operands are wrappers
                Type otherType = ts.primitiveTypeOfWrapper(other.type());
                Type childType = ts.primitiveTypeOfWrapper(child.type());
                if (otherType != null && childType != null
                        && otherType.isNumeric() && childType.isNumeric()) {
                    Type t = ts.promote(childType, otherType);
                    if (ts.isImplicitCastValid(t, av.toType())) {
                        return t;
                    }
                    else {
                        return av.toType();
                    }
                }

                return child.type();
            }

            if (op == SHL || op == SHR || op == USHR) {
                if (child.type().isNumeric() && other.type().isNumeric()) {
                    if (child == left) {
                        Type t = ts.promote(child.type());

                        if (ts.isImplicitCastValid(t, av.toType())) {
                            return t;
                        }
                        else {
                            return av.toType();
                        }
                    }
                    else {
                        return ts.promote(child.type());
                    }
                }

                // Added case for unboxing
                if (child.type().isNumeric()) {
                    Type otherType = ts.primitiveTypeOfWrapper(other.type());
                    if ((otherType != null) && otherType.isNumeric()) {
                        if (child == left) {
                            Type t = ts.promote(child.type(), otherType);
                            if (ts.isImplicitCastValid(t, av.toType())) {
                                return t;
                            }
                            else {
                                return av.toType();
                            }
                        }
                        else {
                            return ts.promote(child.type());
                        }
                    }
                }
                // Added case for unboxing
                if (other.type().isNumeric()) {
                    Type childType = ts.primitiveTypeOfWrapper(child.type());
                    if ((childType != null) && childType.isNumeric()) {
                        if (child == left) {
                            Type t = ts.promote(childType, other.type());
                            if (ts.isImplicitCastValid(t, av.toType())) {
                                return t;
                            }
                            else {
                                return av.toType();
                            }
                        }
                        else {
                            return ts.promote(childType);
                        }
                    }
                }

                // Added case for unboxing, when both operands are wrappers
                Type otherType = ts.primitiveTypeOfWrapper(other.type());
                Type childType = ts.primitiveTypeOfWrapper(child.type());
                if (otherType != null && childType != null
                        && otherType.isNumeric() && childType.isNumeric()) {
                    if (child == left) {
                        Type t = ts.promote(childType, otherType);
                        if (ts.isImplicitCastValid(t, av.toType())) {
                            return t;
                        }
                        else {
                            return av.toType();
                        }
                    }
                    else {
                        return ts.promote(childType);
                    }
                }

                return child.type();
            }

            return child.type();
        }
        catch (SemanticException e) {
        }

        return child.type();
    }

}
