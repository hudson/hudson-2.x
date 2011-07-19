/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *       
 *
 *******************************************************************************/ 

package hudson.model.labels;

import hudson.model.Label;
import hudson.util.VariableResolver;

/**
 * Boolean expression of labels.
 * 
 * @author Kohsuke Kawaguchi
 * @since  1.372
 */
public abstract class LabelExpression extends Label {
    protected LabelExpression(String name) {
        super(name);
    }

    @Override
    public String getExpression() {
        return getDisplayName();
    }

    public static class Not extends LabelExpression {
        private final Label base;

        public Not(Label base) {
            super('!'+paren(LabelOperatorPrecedence.NOT,base));
            this.base = base;
        }

        @Override
        public boolean matches(VariableResolver<Boolean> resolver) {
            return !base.matches(resolver);
        }

        @Override
        public LabelOperatorPrecedence precedence() {
            return LabelOperatorPrecedence.NOT;
        }
    }

    /**
     * No-op but useful for preserving the parenthesis in the user input.
     */
    public static class Paren extends LabelExpression {
        private final Label base;

        public Paren(Label base) {
            super('('+base.getExpression()+')');
            this.base = base;
        }

        @Override
        public boolean matches(VariableResolver<Boolean> resolver) {
            return base.matches(resolver);
        }

        @Override
        public LabelOperatorPrecedence precedence() {
            return LabelOperatorPrecedence.ATOM;
        }
    }

    /**
     * Puts the label name into a parenthesis if the given operator will have a higher precedence.
     */
    static String paren(LabelOperatorPrecedence op, Label l) {
        if (op.compareTo(l.precedence())<0)
            return '('+l.getExpression()+')';
        return l.getExpression();
    }

    public static abstract class Binary extends LabelExpression {
        private final Label lhs,rhs;

        public Binary(Label lhs, Label rhs, LabelOperatorPrecedence op) {
            super(combine(lhs, rhs, op));
            this.lhs = lhs;
            this.rhs = rhs;
        }

        private static String combine(Label lhs, Label rhs, LabelOperatorPrecedence op) {
            return paren(op,lhs)+op.str+paren(op,rhs);
        }

        /**
         * Note that we evaluate both branches of the expression all the time.
         * That is, it behaves like "a|b" not "a||b"
         */
        @Override
        public boolean matches(VariableResolver<Boolean> resolver) {
            return op(lhs.matches(resolver),rhs.matches(resolver));
        }

        protected abstract boolean op(boolean a, boolean b);
    }

    public static final class And extends Binary {
        public And(Label lhs, Label rhs) {
            super(lhs, rhs, LabelOperatorPrecedence.AND);
        }

        @Override
        protected boolean op(boolean a, boolean b) {
            return a && b;
        }

        @Override
        public LabelOperatorPrecedence precedence() {
            return LabelOperatorPrecedence.AND;
        }
    }

    public static final class Or extends Binary {
        public Or(Label lhs, Label rhs) {
            super(lhs, rhs, LabelOperatorPrecedence.OR);
        }

        @Override
        protected boolean op(boolean a, boolean b) {
            return a || b;
        }

        @Override
        public LabelOperatorPrecedence precedence() {
            return LabelOperatorPrecedence.OR;
        }
    }

    public static final class Iff extends Binary {
        public Iff(Label lhs, Label rhs) {
            super(lhs, rhs, LabelOperatorPrecedence.IFF);
        }

        @Override
        protected boolean op(boolean a, boolean b) {
            return !(a ^ b);
        }

        @Override
        public LabelOperatorPrecedence precedence() {
            return LabelOperatorPrecedence.IFF;
        }
    }

    public static final class Implies extends Binary {
        public Implies(Label lhs, Label rhs) {
            super(lhs, rhs, LabelOperatorPrecedence.IMPLIES);
        }

        @Override
        protected boolean op(boolean a, boolean b) {
            return !a || b;
        }

        @Override
        public LabelOperatorPrecedence precedence() {
            return LabelOperatorPrecedence.IMPLIES;
        }
    }
}
