
package mini_turtle;

import java_cup.runtime.*;
import java.util.*;
import static mini_turtle.sym.*;

%%

%class Lexer
%unicode
%cup
%cupdebug
%line
%column
%yylexthrow Exception

%{

    /*
    To build a token, we call one of the two symbol methods below
    passing as argument a symbol for a Parser token (EOF, etc.) and
    possibly a value
    */

    /* See https://www2.in.tum.de/repos/cup/develop/src/java_cup/runtime/ */

    private Symbol symbol(int id)
    {
	return new Symbol(id, yyline, yycolumn);
    }

    private Symbol symbol(int id, Object value)
    {
	return new Symbol(id, yyline, yycolumn, value);
    }

%}

/* Here we declare shortcuts for regular expressions */
LineTerminator = \r | \n | \r\n
InputCharacter = [^\r\n] // ^ denotes 'any character except'
WhiteSpace = [ \t\f\r\n]
String = "\""[^\"]* "\""

Comment = "//" {InputCharacter}* {LineTerminator}

Identifier = [:jletter:] [:jletterdigit:]*

Integer = [:digit:]+

%state COMMENT

%%

/* STARTPOINT FOR LEXICAL ANALYSER */

<YYINITIAL> {

    /* 
    Here we define actions (in Java code) to be taken for certain regular expressions
    This action can be void or return a token
    */
    
    "+"
    { return symbol(PLUS); }

    "-"
    { return symbol(MINUS); }

    "*"
    { return symbol(TIMES); }

    "/"
    { return symbol(DIV); }

    "("
    { return symbol(LPAREN); }

    ")"
    { return symbol(RPAREN); }

    "{"
    { return symbol(BEGIN); }

    "}"
    { return symbol(END); }

    ","
    { return symbol(COMMA); }

    "if"
    { return symbol(IF); }

    "else"
    { return symbol(ELSE); }

    "def"
    { return symbol(DEF); }

    "repeat"
    { return symbol(REPEAT); }

    "penup"
    { return symbol(PENUP); }

    "pendown"
    { return symbol(PENDOWN); }

    "forward"
    { return symbol(FORWARD); }

    "turnleft"
    { return symbol(TURNLEFT); }

    "turnright"
    { return symbol(TURNRIGHT); }

    "color"
    { return symbol(COLOR); }

    "black"
    { return symbol(BLACK); }

    "white"
    { return symbol(WHITE); }

    "red"
    { return symbol(RED); }

    "green"
    { return symbol(GREEN); }

    "blue"
    { return symbol(BLUE); }

    {Identifier}
    { return symbol(IDENT, yytext().intern()); }
    // The call to intern() allows identifiers to be compared using == .

    {Integer}
    { return symbol(CST, Integer.parseInt(yytext())); }

    {Comment}
    { /* ignore */ }

    "(*"
    { yybegin(COMMENT); }

    {WhiteSpace}
    {

    }

    .
    { throw new Exception (String.format (
        "Line %d, column %d: illegal character: '%s'\n", yyline, yycolumn, yytext()
      ));
    }

}

<COMMENT> {
      "*)"         { yybegin(YYINITIAL); }
      {WhiteSpace} { /* ignore */ }
      .            { /* ignore */ }
      <<EOF>>    { throw new Exception(String.format(
        "Line %d, column %d: unclosed comment\n", yyline, yycolumn)); }
}
