/*
 * Copyright (c) 2010, InfraDNA, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *    Kohsuke Kawaguchi
 *
 */
header {
  package hudson.model.labels;
  import hudson.model.Label;
}

class LabelExpressionParser extends Parser;
options {
  defaultErrorHandler=false;
}

// order of precedence is as per http://en.wikipedia.org/wiki/Logical_connective#Order_of_precedence

expr
returns [Label l]
  : l=term1 EOF
  ;

term1
returns [Label l]
{ Label r; }
  : l=term2( IFF r=term2 {l=l.iff(r);} )?
  ;

term2
returns [Label l]
{ Label r; }
  : l=term3( IMPLIES r=term3 {l=l.implies(r);} )?
  ;

term3
returns [Label l]
{ Label r; }
  : l=term4 ( OR r=term4 {l=l.or(r);} )?
  ;

term4
returns [Label l]
{ Label r; }
  : l=term5 ( AND r=term5 {l=l.and(r);} )?
  ;

term5
returns [Label l]
{ Label x; }
  : l=term6
  | NOT x=term6
    { l=x.not(); }
  ;

term6
returns [Label l]
options { generateAmbigWarnings=false; }
  : LPAREN l=term1 RPAREN
    { l=l.paren(); }
  | a:ATOM
    { l=LabelAtom.get(a.getText()); }
  | s:STRINGLITERAL
    { l=LabelAtom.get(hudson.util.QuotedStringTokenizer.unquote(s.getText())); }
  ;

class LabelExpressionLexer extends Lexer;

AND:    "&&";
OR:     "||";
NOT:    "!";
IMPLIES:"->";
IFF:    "<->";
LPAREN: "(";
RPAREN: ")";

protected
IDENTIFIER_PART
    :   ~( '&' | '|' | '!' | '<' | '>' | '(' | ')' | ' ' | '\t' | '\"' | '\'' )
    ;

ATOM
/* the real check of valid identifier happens in LabelAtom.get() */
    :   (IDENTIFIER_PART)+
    ;

WS
  : (' '|'\t')+
    { $setType(Token.SKIP); }
  ;

STRINGLITERAL
    :   '"'
        ( '\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\'' | '\\' )   /* escape */
        |  ~( '\\' | '"' | '\r' | '\n' )
        )*
        '"'
    ;
