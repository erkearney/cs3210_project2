/*
    This class provides a recursive descent parser 
    for Corgi (a simple calculator language),
    creating a parse tree which can be interpreted
    to simulate execution of a Corgi program
*/

import java.util.*;
import java.io.*;

public class Parser {

   private Lexer lex;

   public Parser( Lexer lexer ) {
      lex = lexer;
   }

   public Node parseProgram() {
      //System.out.println("-----> parsing <program>");
      Node first = parseFuncCall();

      // look ahead to see if there are funcDefs
      Token token = lex.getNextToken();

      if ( token.isKind("eof") ) {
         lex.putBackToken( token );
         //System.out.println("Finished parsing <program> -> <funcCall>");
         return new Node( "program", first, null, null );    
      }
      else {
         lex.putBackToken( token );
         Node second = parseFuncDefs();
         //System.out.println("Finished parsing <program> -> <funcCall> <funcDefs>");
         return new Node( "program", first, second, null );
      }
   }

   private Node parseFuncDefs() {
      //System.out.println("-----> parsing <funcDefs>");
      Node first = parseFuncDef();
      // Check if there is another funcDef
      Token token = lex.getNextToken();
      if( token.isKind( "funcDef" ) ) {
         Node second = parseFuncDef();
         //System.out.println("Finished parsing <funcDefs> -> <funcDef> <funcDefs>");
         return new Node( "funcDefs", first, second, null );
      }
      else {
         //System.out.println("Finished parsing <funcDefs> -> <funcDef>");
         return new Node( "funcDefs", first, null, null );
      }
   } // <funcDefs>

   private Node parseFuncDef() {
      // A funcDef node will be created using the 
      // public Node( <k>, one, two, three ) constructor, and it will have 
      // two children, the first will be a <params> node, and the second will 
      // be a <statemenets> node. If there are no <params>, then the first 
      // child will be null, if there are no <statements>, the second child 
      // will be null. The third child will always be null
      //System.out.println("-----> parsing <funcDef>:");
      Token token = lex.getNextToken();
      errorCheck( token, "def", "", "funcDef" );
      token = lex.getNextToken();
      String functionName = token.getDetails();
      token = lex.getNextToken();
      errorCheck( token, "Single", "(", "funcDef" );
      // Look ahead to see if there are any parameters
      token = lex.getNextToken();
      if( token.isKind("var") ) {
         //System.out.println("There are params");
         lex.putBackToken(token);
         Node first = parseParams();
         token = lex.getNextToken();
         errorCheck( token, "Single", ")", "funcDef" );
         // Look ahead again to see if there are any statements
         token = lex.getNextToken();
         if ( token.isKind("end") ) {
            //System.out.println("Finished parsing <funcDef> -> def <var> ( <params> ) end");
            return new Node( "funcDef", functionName, first, null, null );
         }
         else {
            lex.putBackToken( token );
            Node second = parseStatements();
            token = lex.getNextToken();
            errorCheck( token, "end", "", "funcDef" );
            lex.putBackToken( token );
            //System.out.println("Finished parsing <funcDef> -> def <var> ( <params> ) <statements> end");
            return new Node( "funcDef", functionName, first, second, null );
         }
      }
      else {
         //System.out.println("There are no parameters");
         errorCheck( token, "Single", ")", "funcDef" );
         // Look ahead to see if there are any statements
         token = lex.getNextToken();
         //System.out.println(token.getKind());
         if ( token.isKind("end") ) {
             //System.out.println("There are no more statements");
             errorCheck( token, "end", "", "funcDef" );
             //System.out.println("Finished parsing <funcDef> -> def <var> ( ) end");
             return new Node( "funcDef", functionName, null, null, null );
         }
         else {
             lex.putBackToken( token );
             Node second = parseStatements();
             //System.out.println("Checking for end");
             token = lex.getNextToken();
             errorCheck( token, "end", "", "funcDef" );
             //System.out.println("Finished parsing <funcDef> -> def <var> ( ) <statements> end");
             return new Node( "funcDef", functionName, null, second, null );
         }
      }
   } // funcDef

   private Node parseParams() {
      //System.out.println("-----> parsing <params>");
      Token token = lex.getNextToken();
      errorCheck( token, "var", "params" );
      String firstVar = token.getDetails();
      // Look ahead for more vars
      token = lex.getNextToken();
      if( token.isKind( "var" ) ) {
         // <params> -> <var>, <params>    
         lex.putBackToken( token );
         Node first = parseParams();
         //System.out.println("Finished parsing <params> -> <var>, <params>");
         return new Node("params", firstVar, first, null, null);
      }
      else {
         lex.putBackToken( token );
         //System.out.println("Finished parsing <params> -> <var>");
         return new Node("params", firstVar, null, null, null);
      }
   } // <params>

   private Node parseStatements() {
      //System.out.println("-----> parsing <statements>:");
 
      Node first = parseStatement();
 
      Token token = lex.getNextToken();
      if ( token.isKind("string") ||
           token.isKind("var") ||
           token.isKind("single") || // +-*/()=
           token.isKind("funcCall") ||
           token.isKind("bif0") ||
           token.isKind("bif1") ||
           token.isKind("bif2") ||
           token.isKind("if") ||
           token.isKind("return") ) {
         lex.putBackToken( token );
         Node second = parseStatements();
         //System.out.println("Finished parsing <statements> -> <statement> <statements>");
         return new Node( "stmts", first, second, null );
      }       
      else {
         //System.out.println("The last statement was " + token);
         lex.putBackToken(token);
         //System.out.println("Finished parsing <statements> -> <statement>");
         return new Node( "stmts", first, null, null );   
      }
 
      // Jerry's version
      /*
      if ( token.isKind("eof") ) {
         return new Node( "stmts", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseStatements();
         return new Node( "stmts", first, second, null );
      }
      */
   } // <statements>

   private Node parseFuncCall() {
        //System.out.println("-----> parsing <funcCall>:");
        // Check for built-in-functions
        Token token = lex.getNextToken();
        //System.out.println( token );
        if ( token.isKind("bif0") ) {
           // Check for the ( )
           String bif0Name = token.getDetails();
           token = lex.getNextToken();
           errorCheck( token, "Single", "(", "funcCall" );
           token = lex.getNextToken();
           errorCheck( token, "Single", ")", "funcCall" );
           //System.out.println("Finished parsing <funcCall> -> <bif0>");
           return new Node("bif0", bif0Name, null, null, null);
        }
        else if ( token.isKind("sqrt") ||
                  token.isKind("cos") ||
                  token.isKind("sin") ||
                  token.isKind("atan") ||
                  token.isKind("print") ||
                  token.isKind("round") ||
                  token.isKind("trunc") ||
                  token.isKind("bif1") ) {
            String bif1Name = token.getDetails();
            System.out.println(bif1Name);
            token = lex.getNextToken();
            errorCheck(token, "Single", "(", "funcCall" );
            Node first = parseExpr();
            token = lex.getNextToken();
            errorCheck(token, "Single", ")", "funcCall" );
            //System.out.println("Finished parsing <funcCall> -> <bif1>");
            //System.out.println("Creating a bif1 Node with " + token.getDetails() );
            return new Node("bif1", bif1Name, first, null, null);
        }
        else if ( token.isKind("pow") ||
                  token.isKind("lt") ||
                  token.matches("bif2", "le") ||
                  token.isKind("eq") ||
                  token.isKind("ne") ||
                  token.isKind("or") ||
                  token.isKind("and") ) {
            token = lex.getNextToken();
            errorCheck( token, "Single", "(", "funcCall" );
            Node first = parseArgs();
            /*
            Node first = parseExpr();
            Node second = parseExpr();
            //System.out.println("Finished parsing <funcCall> -> <bif2>");
            return new Node("bif2", token.getDetails(), first, second, null);
            */
            //System.out.println("Finished parsing <statements> -> <funcCall> -> <bif2>");
            return new Node("bif2", token.getDetails(), first, null, null);
        }
        else if ( token.isKind("var") ) {
            String functionName = token.getDetails();
            token = lex.getNextToken();
            errorCheck( token, "Single", "(", "funcCall" );
            // Check for args
            token = lex.getNextToken();
            if ( token.matches( "Single", ")" ) ) {
               //System.out.println("Finished parsing <funcCall> -> <var> ( )");
               return new Node("funcCall", functionName, null, null, null);
            }
            else {
               lex.putBackToken( token );
               Node first = parseArgs();
               //System.out.println("Finished parsing <funcCall> -> <var> ( <args> )");
               return new Node("funcCall", functionName, first, null, null);
            }
        }
        else {
            System.out.println("ERROR: <funcCall> can't begin with " + token.getDetails());    
            System.exit(1);
            return new Node("ERROR", null, null, null);
        }
                
   } // funcCall

   private Node parseArgs() {
      //System.out.println("-----> parsing <args>");
      Node first = parseExpr();
      // Look for ')'
      Token token = lex.getNextToken();
      //System.out.println(token);
      if ( token.matches("Single", ")") ) {
         //System.out.println("Finished parsing <args> -> <expr>");
         return new Node("args", first, null, null);
      }
      else {
         errorCheck( token, "Single", ",", "args" );
         token = lex.getNextToken();
         //System.out.println("I think " + token.getDetails() + " is another arg");
         lex.putBackToken( token );
         Node second = parseArgs();
         //System.out.println("Finished parsing <args> -> <expr> <args>");
         return new Node("args", first, second, null);
      }
   }

   private Node parseStatement() {
      //System.out.println("-----> parsing <statement>:");
 
      Token token = lex.getNextToken();
 
      if ( token.isKind("string") ) {// print <string>
         //System.out.println("Finished parsing <statement> -> <string>");
         return new Node( "prtstr", token.getDetails(),
                       null, null, null );
      }
      else if ( token.isKind("var") ) {
         String varName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "Single", "=", "statement" );
         Node first = parseExpr();
         //System.out.println("Finished parsing <statement> -> <var> = <expr>");
         return new Node( "sto", varName, first, null, null );
      }
      else if ( token.isKind("funcCall") ||
                token.isKind("bif0") ||
                token.isKind("bif1") ||
                token.isKind("bif2") ) {
         lex.putBackToken(token);
         Node first = parseFuncCall();
         //System.out.println("Finished parsing <statement> -> <funcCall>");
         return new Node( "funcCall", first, null, null);
      }
      else if ( token.isKind("if") ) {
         // Return a cond node, A cond node will have three children,
         // The first child will always be an expr, which represent
         // the condition of the if statement, the second child
         // will be the statement(s) to execute if the condition is
         // true, it may be null if there are no statements. The third
         // child is the statements to execute if the condition is false,
         // it may also be null if there are no statements.
         token = lex.getNextToken();
         lex.putBackToken(token);
         Node first = parseExpr();
         // Check for else
         token = lex.getNextToken();
         if ( token.isKind( "else" ) ) {
            // Check for end
            token = lex.getNextToken();
            if( token.isKind( " end " ) ) {
                //System.out.println("Finished parsing <statement> -> if <expr> else end");
                return new Node("cond", first, null, null);
            }
            else {
                Node third = parseStatements();
                // Check for end
                token = lex.getNextToken();
                errorCheck(token, "end", "statemement" );
                //System.out.println("Finished parsing <statement> -> if <expr> else <statements> end");
                return new Node("cond", first, null, third);
            }
         }
         else {
            lex.putBackToken(token);
            Node second = parseStatements();
            // Check for else
            token = lex.getNextToken();
            errorCheck(token, "else", "statement" );
            // Check for end
            token = lex.getNextToken();
            if ( token.isKind( "end" ) ) {
                //System.out.println("Finished parsing <statement> -> if <expr> <statements> else end");
                return new Node("cond", first, second, null);
            }
            else {
                lex.putBackToken(token);
                Node third = parseStatements();
                // Check for end
                token = lex.getNextToken();
                errorCheck(token, "end", "statement" );
                //System.out.println("Finished parsing <statement> -> if <expr> <statements> else <statements> end");
                return new Node("cond", first, second, third);
            }
         }
      }
      else if (token.isKind("return")) {
         Node first = parseExpr();
         //System.out.println("Finished parsing <statement> -> return <expr>");
         return new Node("return", first, null, null);
      }
      else {
         System.out.println("Token " + token + 
                             " can't begin a statement");
         System.exit(1);
         return null;
      }
 
   }// <statement>

   private Node parseExpr() {
      //System.out.println("-----> parsing <expr>");

      Node first = parseTerm();

      // look ahead to see if there's an addop
      Token token = lex.getNextToken();
 
      if ( token.matches("Single", "+") ||
           token.matches("Single", "-") ) {
         Node second = parseExpr();
         //System.out.println("Finished parsing <expr> -> <term> " + token.getDetails() + " <expr>");
         return new Node( "expr", token.getDetails(), first, second, null );
      }
      else {// is just one term
         lex.putBackToken( token );
         //System.out.println("Finished parsing <expr> -> <term>");
         return new Node( "expr", "term", first, null, null );
      }

   }// <expr>

   private Node parseTerm() {
      //System.out.println("-----> parsing <term>");

      Node first = parseFactor();

      // look ahead to see if there's a multop
      Token token = lex.getNextToken();
 
      if ( token.matches("Single", "*") ||
           token.matches("Single", "/") 
         ) {
         Node second = parseTerm();
         //System.out.println("Finished parsing <term> -> <factor> *// <term>");
         return new Node( "term", token.getDetails(), first, second, null );
      }
      else {// is just one factor
         lex.putBackToken( token );
         //System.out.println("Finished parsing <term> -> <factor>");
         return new Node( "term", token.getDetails(), first, null, null );
      }
      
   }// <term>

   private Node parseFactor() {
      //System.out.println("-----> parsing <factor>");

      Token token = lex.getNextToken();
      //System.out.println("Token: " + token);

      if ( token.isKind("num") ) {
         //System.out.println("Finished parsing <factor> -> <num>");
         return new Node("num", token.getDetails(), null, null, null );
      }
      else if ( token.isKind("var") ) {
         // Could be one of the following:
         // <factor> -> <var>
         // <factor> -> <funcCall>
         // If it's the latter, we'll need to put back both the function name 
         // and the "(" in order for parseFuncCall() to be able to handle it
         Token temp = token;
         token = lex.getNextToken();
         if ( token.matches("Single", "(") ) {
            lex.putBackToken( token );
            lex.putBackToken( temp );
            Node first = parseFuncCall();
            //System.out.println("Finished parsing <factor> -> <funcCall>");
            return new Node("factor", first, null, null);
         }
         else {
            lex.putBackToken( token );
            //System.out.println("Finished parsing <factor> -> <var>");
            return new Node("factor", token.getDetails(), null, null, null);
         }
      }
      else if ( token.matches("Single","(") ) {
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "Single", ")", "factor" );
         return first;
      }
      else if ( token.isKind("bif0") ) {
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "Single", "(", "factor" );
         token = lex.getNextToken();
         errorCheck( token, "Single", ")", "factor" );
         
         return new Node( bifName, null, null, null );
      }
      else if ( token.isKind("bif1") ) {
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "Single", "(", "factor" );
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "Single", ")", "factor" );
         
         return new Node( bifName, first, null, null );
      }
      else if ( token.isKind("bif2") ) {
         //System.out.println("-----> parsing bif2 " + token.getDetails());
         String bifName = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "Single", "(", "factor" );
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "Single", ",", "factor" );
         //System.out.println("Now parsing second argument");
         Node second = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "Single", ")", "factor" );
         
         //System.out.println("Finished parsing <factor> -> <funcCall> -> <bif2>");
         return new Node( bifName, first, second, null );
      }
      else if ( token.matches("Single","-") ) {
         Node first = parseFactor();
         return new Node("opp", first, null, null );
      }
      else {
         //System.out.println("Can't have factor starting with " + token );
         System.exit(1);
         return null;
      }
      
   }// <factor>

  // check whether token is correct kind
  private void errorCheck( Token token, String kind, String parseLocation ) {
    if( ! token.isKind( kind ) ) {
      System.out.println("Error in " + parseLocation + 
                         " expected " + token + 
                         " to be of kind " + kind );
      System.exit(1);
    }
  }

  // check whether token is correct kind and details
  private void errorCheck( Token token, String kind, String details, String parseLocation ) {
    if( ! token.isKind( kind ) || 
        ! token.getDetails().equals( details ) ) {
      
      System.out.println("Error in " + parseLocation + 
                          " expected " + token + 
                          " to be kind=" + kind + 
                          " and to be details=" + details +
			              " it is actually kind=" + token.getKind() + 
                          " and details=" + details );
      System.exit(1);
    }
  }

}
