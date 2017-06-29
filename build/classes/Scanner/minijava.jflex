/*
 * JFlex specification for the lexical analyzer for a simple demo language.
 * Change this into the scanner for your implementation of MiniJava.
 * CSE 401/P501 Au11
 */


package Scanner;

import java_cup.runtime.*;
import Parser.sym;

%%

%public
%final
%class scanner
%unicode
%cup
%line
%column

/* Code copied into the generated scanner class.   */
/* Can be referenced in scanner action code. */
%{
  // Return new symbol objects with line and column numbers in the symbol 
  // left and right fields. This abuses the original idea of having left 
  // and right be character positions, but is   // is more useful and 
  // follows an example in the JFlex documentation.
  private Symbol symbol(int type) {
    return new Symbol(type, yyline+1, yycolumn+1);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline+1, yycolumn+1, value);
  }

  // Return a readable representation of symbol s (aka token)
  public String symbolToString(Symbol s) {
    String rep;
    switch (s.sym) {
      case sym.BECOMES: return "BECOMES";
      case sym.SEMICOLON: return "SEMICOLON";
      case sym.PLUS: return "PLUS";
      case sym.LPAREN: return "LPAREN";
      case sym.RPAREN: return "RPAREN";
      case sym.IDENTIFIER: return "ID(" + (String)s.value + ")";
      case sym.EOF: return "<EOF>";
      case sym.error: return "<ERROR>";
      case sym.WHILE: return "WHILE";
      case sym.IF: return "IF";
      case sym.ELSE: return "ELSE";
      case sym.PRINTLN: return "PRINTLN";
      case sym.TRUE: return "TRUE";
      case sym.FALSE: return "FALSE";
      case sym.THIS: return "THIS";
      case sym.NEW: return "NEW";
      case sym.BOOL: return "BOOL";
      case sym.INT: return "INT";
      case sym.DOUBLE: return "DOUBLE";
      case sym.NOT: return "NOT";
      case sym.MULT: return "MULT";
      case sym.LESS: return "LESS";
      case sym.MINUS: return "MINUS";
      case sym.AND: return "AND";
      case sym.LBRACKET: return "LBRACKET";
      case sym.RBRACKET: return "RBRACKET";
      case sym.DOT: return "DOT";
      case sym.LCURLY: return "LCURLY";
      case sym.RCURLY: return "RCURLY";
      case sym.PUBLIC: return "PUBLIC";
      case sym.STATIC: return "STATIC";
      case sym.VOID: return "VOID";
      case sym.MAIN: return "MAIN";
      case sym.STRING: return "STRING";
      case sym.CLASS: return "CLASS";
      case sym.EXTENDS: return "EXTENDS";
      case sym.RETURN: return "RETURN";
      case sym.COMMA: return "COMMA";
      case sym.LENGTH: return "LENGTH";
      case sym.DOUBLE_LITERAL: return "DOUBLE_LITERAL(" + (String)s.value + ")";
      case sym.INTEGER_LITERAL: return "INT_LITERAL(" + (String)s.value + ")";
      default: return "<UNEXPECTED TOKEN " + s.toString() + ">";
    }
  }
%}

/* Helper definitions */
letter = [a-zA-Z]
digit = [0-9]
eol = [\r\n]
white = {eol}|[ \t]
inputchar = [^\r\n]
comment = "//" {inputchar}* {eol}?
multiline_comment = "/*" "*"+ "/" | "/*" (("*"+ [^/]+) | [^*])*  "*/"

%%

/* Token definitions */

/* reserved words */
/* (put here so that reserved words take precedence over identifiers) */
"while" { return symbol(sym.WHILE); }
"if" { return symbol(sym.IF); }
"else" { return symbol(sym.ELSE); }
"System.out.println" { return symbol(sym.PRINTLN); }
"true" { return symbol(sym.TRUE); }
"false" { return symbol(sym.FALSE); }
"this" { return symbol(sym.THIS); }
"new" { return symbol(sym.NEW); }
"boolean" { return symbol(sym.BOOL); }
"int" { return symbol(sym.INT); }
"double" { return symbol(sym.DOUBLE); }
"public" { return symbol(sym.PUBLIC); }
"static" { return symbol(sym.STATIC); }
"void" { return symbol(sym.VOID); }
"main" { return symbol(sym.MAIN); }
"String" { return symbol(sym.STRING); }
"class" { return symbol(sym.CLASS); }
"extends" { return symbol(sym.EXTENDS); }
"return" { return symbol(sym.RETURN); }
"length" { return symbol(sym.LENGTH); }


/* operators */
"+" { return symbol(sym.PLUS); }
"=" { return symbol(sym.BECOMES); }
"!" { return symbol(sym.NOT); }
"*" { return symbol(sym.MULT); }
"<" { return symbol(sym.LESS); }
"-" { return symbol(sym.MINUS); }
"&&" { return symbol(sym.AND); }
"." { return symbol(sym.DOT); }


/* delimiters */
"(" { return symbol(sym.LPAREN); }
")" { return symbol(sym.RPAREN); }
";" { return symbol(sym.SEMICOLON); }
"[" { return symbol(sym.LBRACKET); }
"]" { return symbol(sym.RBRACKET); }
"{" { return symbol(sym.LCURLY); }
"}" { return symbol(sym.RCURLY); }
"," { return symbol(sym.COMMA); }

/* identifiers */
{letter} ({letter}|{digit}|_)* { return symbol(sym.IDENTIFIER, yytext()); }
{digit}+ { return symbol(sym.INTEGER_LITERAL, yytext()); }
{digit}+"."{digit}*("e"|"E"){digit}+ | {digit}+"."{digit}* | {digit}+("e"|"E"){digit}+ {return symbol(sym.DOUBLE_LITERAL, yytext()); }

/* comments */
{comment} { /* ignore comments */}
{multiline_comment} { /* ignore comments */}


/* whitespace */
{white}+ { /* ignore whitespace */ }

/* lexical errors (put last so other matches take precedence) */
. { System.err.println(
	"\nunexpected character in input: '" + yytext() + "' at line " +
	(yyline+1) + " column " + (yycolumn+1));
	/* return error symbol so we can count how many errors */
	return symbol(sym.error);
  }
