/*
Thanks again to this group: https://github.com/jamesahhh/Corgi for sharing
their solution with us, it was invaluable.
*/
/*  a Node holds one node of a parse tree
    with several pointers to children used
    depending on the kind of node
*/

import java.util.*;
import java.io.*;
import java.awt.*;

public class Node {

    public static int count = 0;  // maintain unique id for each node

    private int id;

    private static Node funcDefs; // Points to function Definition nodes,
                                  // used for funcCall, see README

    private static Node args;     // A global args pointer, whenever a function
                                  // is called, this points to the first child
                                  // of the called functions' funcDef node, 
                                  // which will be of type args

    private static Node argNode;  // TODO maybe add comments explaining here

    private static double returnValue;

    private static boolean returnBoolean;

    private String kind;  // non-terminal or terminal category for the node
    private String info;  // extra information about the node such as
    // the actual identifier for an I

    // references to children in the parse tree
    private Node first, second, third;

    // The following 4 lines were taken directly from this group: https://github.com/jamesahhh/Corgi
    private static boolean returnBool; //true if return statement has been executed

    // memory tables shared by all nodes but private to a function
    private static Stack<MemTable> tables;

    /*
    // memory table shared by all nodes
    private static MemTable table = new MemTable();
    */

    private static Scanner keys = new Scanner( System.in );

    // construct a common node with no info specified
    public Node( String k, Node one, Node two, Node three ) {
        kind = k;  info = "";
        first = one;  second = two;  third = three;
        id = count;
        count++;
        //System.out.println( this );
    }

    // construct a node with specified info
    public Node( String k, String inf, Node one, Node two, Node three ) {
        kind = k;  info = inf;
        first = one;  second = two;  third = three;
        id = count;
        count++;
        //System.out.println( this );
    }

    // construct a node that is essentially a token
    public Node( Token token ) {
        kind = token.getKind();  info = token.getDetails();
        first = null;  second = null;  third = null;
        id = count;
        count++;
        //System.out.println( this );
    }

    public String toString() {
        return "#" + id + "[" + kind + "," + info + "]<" + nice(first) +
                " " + nice(second) + ">";
    }

    public String nice( Node node ) {
        if ( node == null ) {
            return "";
        }
        else {
            return "" + node.id;
        }
    }

    // produce array with the non-null children
    // in order
    private Node[] getChildren() {
        int count = 0;
        if( first != null ) count++;
        if( second != null ) count++;
        if( third != null ) count++;
        Node[] children = new Node[count];
        int k=0;
        if( first != null ) {  children[k] = first; k++; }
        if( second != null ) {  children[k] = second; k++; }
        if( third != null ) {  children[k] = third; k++; }

        return children;
    }

    //******************************************************
    // graphical display of this node and its subtree
    // in given camera, with specified location (x,y) of this
    // node, and specified distances horizontally and vertically
    // to children
    public void draw( Camera cam, double x, double y, double h, double v ) {

        //System.out.println("draw node " + id + " which is of type " + kind );

        // set drawing color
        cam.setColor( Color.black );

        String text = kind;
        if( ! info.equals("") ) text += "(" + info + ")";
        cam.drawHorizCenteredText( text, x, y );

        // positioning of children depends on how many
        // in a nice, uniform manner
        Node[] children = getChildren();
        int number = children.length;
        //System.out.println("has " + number + " children");

        double top = y - 0.75*v;

        if( number == 0 ) {
            return;
        }
        else if( number == 1 ) {
            children[0].draw( cam, x, y-v, h/2, v );     cam.drawLine( x, y, x, top );
        }
        else if( number == 2 ) {
            children[0].draw( cam, x-h/2, y-v, h/2, v );     cam.drawLine( x, y, x-h/2, top );
            children[1].draw( cam, x+h/2, y-v, h/2, v );     cam.drawLine( x, y, x+h/2, top );
        }
        else if( number == 3 ) {
            children[0].draw( cam, x-h, y-v, h/2, v );     cam.drawLine( x, y, x-h, top );
            children[1].draw( cam, x, y-v, h/2, v );     cam.drawLine( x, y, x, top );
            children[2].draw( cam, x+h, y-v, h/2, v );     cam.drawLine( x, y, x+h, top );
        }
        else {
            System.out.println("no Node kind has more than 3 children???");
            System.exit(1);
        }

    }// draw

    public static void error( String message ) {
        System.out.println( message );
        System.exit(1);
    }

    // ask this node to execute itself
    // (for nodes that don't return a value)
    public void execute() {
        //System.out.println("Executing a " + this.kind + " " + this.info);

        if ( kind.equals("program") ) {
           tables = new Stack<MemTable>();
           // <program> -> <funcCall> | <funcCall> <funcDefs>
           if ( first == null ) {
              error("A Corgi program must have at least one function call");
              System.exit(1);
           }
           else if ( second == null ) {
              System.out.println("WARNING: This program has no function definitions");
           }
           else {
              funcDefs = second; 
           }

           // evaluate the <funcCall>
           //System.out.println("Evaluating " + first);
           first.evaluate();
        }
        else if ( kind.equals("funcDef") ) {
            //System.out.println("funcDef: Executing " + this.info);

            Node params = this.first;
            if ( params == null ) {
                System.out.println("function: " + this.info + " has no parameters");
            }
            else {
                MemTable table = tables.pop();
                while ( args != null ) {
                    table.store(args.first.info, 0);
                    args = args.second;
                }
                // TODO maybe check if num args match num params when a function is called
                tables.push(table);
                //System.out.println("params: " + params);
                // Store the params in the paramNames array
                /*
                for (int i = 0; i < paramNames.length; i++ ) {
                    if ( params.first == null ) {
                        paramNames[i] = params.info;
                        //System.out.println("The last param was " + paramNames[i]);
                        break;
                    }
                    else {
                        paramNames[i] = params.info;
                        //System.out.println("Added " + paramNames[i] + " to paramNames");
                        params = params.first;
                    }
                }
                */
            }

            Node statements = this.second;
            //System.out.println(statements);
            if ( statements != null ) {
                //System.out.println("Executing statements");
                statements.execute();
            }
            else {
                System.out.println("WARING no statements found in " + this.info);
            }
        }
        else if ( kind.equals("funcCall" ) ) {
            String functionName = this.first.info;
            Node args = this.first;
            //System.out.println(functionName + " called with " + args);
            // TODO see if more is needed here.

            switch (functionName) {
                // Check if the called function is a built-in function
                case "print":
                    args = this.first.first;
                    // print is a bif 1
                    if ( args.first == null ) {
                        error("ERROR: print takes 1 argument, use nl() to print an empty line");    
                    } else if ( args.second != null ) {
                        error("ERROR: print takes only 1 argument");    
                    } else {
                        Node arg = args.first;
                        System.out.print(arg.evaluate());
                    }
                    break;
                case "nl":
                    System.out.print("\n");
                    break;
                case "input":
                    this.evaluate();
                    break;
                default:
                    // May be a user-defined function
                    this.first.execute();
            }
        }
        else if ( kind.equals("stmts") ) {
            Node statement = this.first;
            //System.out.println("statement: " + statement);
            statement.execute();
            if ( this.second != null ) {
                Node statements = this.second;
                //System.out.println("statements: " + statements);
                statements.execute();
            }
        }
        else if ( kind.equals("prtstr") ) {
            System.out.print( info );
        }
        /*
        else if ( kind.equals("funcCall") ) {
            args = this.first;  // grab the args node
            System.out.println(this);
            System.out.println("Got args " + args);
        }
        */
        else if ( kind.equals("bif0") ) {
            //System.out.println("Got a bif0");    
            // Currently the only bif0 is nl()
            switch ( this.info ) {
                case "nl":
                    System.out.print("\n");
                    break;
                case "input":
                    // TODO implement variable storage
                    String temp = keys.next();
                    //System.out.println("Temp is " + temp);
                    break;
                default:
                    System.out.println("ERROR: Unrecognized bif0: " + this.info);
                    System.exit(1);
            }
        }
        else if ( kind.equals("bif1") ) {
            //System.out.println("Got a bif1"); 
            switch ( this.info ) {
                case "print":
                    //System.out.println("Print first type: " + this.first.kind);
                    double writeString = this.first.evaluate();
                    System.out.print(writeString);
            }

        }
        else if ( kind.equals("bif2") ) {
            //System.out.println("Got a bif2 " + this.info);
            switch ( this.info ) {
                case "le":
                    //System.out.println("le");
                    break;
                default:
                    System.out.println("ERROR: " + this.info + " was not recognized as a bif2");
            }
        }
        else if ( kind.equals("sto") ) {
            // TODO implement variable storage
            double value = this.first.evaluate();
            String varName = this.info;
            MemTable table = tables.pop();
            table.store(varName, value);
            tables.push( table );
            System.out.println("Stored " + varName + " = " + value);
        }
        else if ( kind.equals("return") ) {
            // TODO implement return
            //System.out.println("Got return");
            returnValue = this.evaluate();
        }
        else if ( kind.equals("num")) {
            double value = this.evaluate();
            //table.store(info, value);
        }

        else if ( kind.equals("cond")) {
            double value = first.evaluate();
            if(value != 0) {
                //System.out.println("Condition is true");
                second.execute();
            }
            else {
                //System.out.println("Condition is false");
                third.execute();
            }
        }
        else {
            error("Unknown kind of node [" + kind + "]");
        }

    }// execute

    // compute and return value produced by this node
    public double evaluate() {
        //System.out.println("evaluate() called with " + this);

        if ( kind.equals("funcCall") ) {
            String functionName = this.info;
            //System.out.println("funcCall: " + this);
            //System.out.println("functionName is " + functionName);
            boolean found = false;
            tables.push(new MemTable());
            args = this.first;
            // funcDefs starts out as the first funcDefs Node created by Parser
            // we need to check all the funcDefs until we find one with the
            // same name as the one that was just called.
            Node checkFunction = funcDefs;
            while ( !found ) {
                String checkFunctionName = checkFunction.first.info;
                if ( functionName.equals(checkFunctionName) ) {
                    found = true;

                    /*
                    // Find the arguments
                    if ( this.first != null ) {
                        Node args = this.first;
                        for ( int i = 0; i < paramNames.length; i++ ) {
                            if ( args.first == null ) {
                                System.out.println("There are no more arguments");
                                break;
                            }
                            else {
                                double paramValue = args.first.evaluate();
                                //System.out.println("paramValue: " + paramValue);
                                args = args.first;
                                // TODO memtable problem caused here.
                                String paramName = checkFunction.first.paramNames[i];
                                //System.out.println("paramName: " + paramName);
                                if ( paramName != null ) {
                                    table.store(paramName, paramValue);
                                }
                                //this.paramValues[i] = paramValue;
                                //System.out.println("paramValues[" + i + "] set to " + paramValue);
                                //double test = table.retrieve(paramName);
                                //System.out.println("Test is " + test);
                                break;
                            }
                        }
                    }
                    else {
                        //System.out.println(functionName + " called with no arguments");
                    }
                    */

                    checkFunction.first.execute();
                    // Reset checkFunction to beginning
                    /*
                    checkFunction = funcDefs;
                    return retval;
                    */
                }
                else {
                    if ( checkFunction.second != null ) {
                        // If this node was of form <funcDefs> -> <funcDef> <funcDefs>,
                        // check the next <funcDefs> node
                        checkFunction = checkFunction.second;
                    }
                    else {
                        // We've checked all the function definitions,
                        // and didn't find a match.
                        System.out.format(checkFunction.first.info);
                        System.out.format("ERROR: %s is undefined\n", functionName);
                        System.exit(1);
                    }
                }
            }
            tables.pop();
            returnBool = false;
            return returnValue;

        }
        else if ( kind.equals("args") ) {
            //System.out.println("args: " + this);    

            // TODO take another look at args
            if ( this.second != null ) {
                System.out.println("Fix args, ~line 475");
                return -1;
            }
            //System.out.println(this.info);
            return this.first.evaluate();
        }
        else if ( kind.equals("expr") ) {
            if ( this.info.equals("term") ) {
                return first.evaluate();
            }
            else {
                if ( Double.isNaN(this.second.evaluate()) ) {
                    System.out.println("ERROR when evaluating term, the second child is NaN");
                    System.out.println(this.second);
                }
                else if ( this.info.equals("+") ) {
                    return this.first.evaluate() + this.second.evaluate();
                }
                else if ( this.info.equals("-") ) {
                    return this.first.evaluate() - this.second.evaluate();
                }
                else {
                    System.out.println("ERROR evaluting expr node");
                }
            }
        }
        else if ( kind.equals("term") ) {
            //System.out.println("term " + this + " " + this.info);
            if ( this.info.equals("*") ) {
                return this.first.evaluate() * this.second.evaluate();
            }
            else if ( this.info.equals("/") ) {
                return this.first.evaluate() / this.second.evaluate();
            }
            else if ( this.info.equals("factor") ) {
                return this.first.evaluate();
            }
            else {
                error("ERROR in term: Unrecognized info");
            }
        }
        else if ( kind.equals("factor") ) {
            //System.out.println("factor " + this);
            switch(this.info) {
                case "( expr )":
                    return this.first.evaluate();
                case "funcCall":
                    return this.first.evaluate();
                case "bif":
                    //System.out.println("factor bif: " + this.first);
                    return this.first.evaluate();
                default:
                    error("ERROR in exexuting factor: Unrecognized factor info: " + this.info);
            }
        }
        else if ( kind.equals("num") ) {
            return Double.parseDouble(this.info);
        }
        else if ( kind.equals("bif0") ) {
            switch (this.info) {
                case "nl":
                    System.out.print("\n");
                    return 0;
                case "input":
                    // TODO implement a try catch here, if I decide to not be lazy
                    double value = keys.nextDouble();
                    return value;
                default:
                    System.out.println("ERROR in evaluate: Unrecognized bif0: " + this.info);
            }
        }
        else if ( kind.equals("bif1") ) {
            args = this.first;
            double arg1 = this.first.evaluate();
            switch (this.info) {
                case "not":
                    if ( arg1 == 0 ) {
                        return 1;    
                    }
                    else {
                        return 0;    
                    }
                case "print":
                    this.execute();
                    return 0;
                case "round":
                    return Math.round(arg1);
                case "trunc":
                    return Math.floor(arg1);
                case "sqrt":
                    if ( arg1 < 0 ) {
                        System.out.format("Corgi does not support imaginary " +
                                          "numbers, so it cannot take the " +
                                          "square root of %f, exiting ...\n", arg1);
                        System.exit(1);
                    }
                    return Math.sqrt(arg1);
                case "cos":
                    return Math.cos(arg1);
                case "sin":
                    return Math.sin(arg1);
                case "atan":
                    return Math.atan(arg1);
                case "abs":
                    return Math.abs(arg1);
                default:
                    System.out.println("ERROR in evaluate: Unrecognized bi1 " + this.info);
            }
        }
        else if ( kind.equals("bif2") ) {
            //System.out.println("bif2 this.info: " + this.info);
            args = this.first;
            double arg1 = args.first.evaluate();
            //System.out.println("arg1: " + arg1);
            args = args.second;
            //System.out.println("second args in " + this.info + ": " + args);
            if ( args == null ) {
                System.out.println("ERROR, bif2: " + this.info + " could not find its second argument!");    
                return -1;
            }
            double arg2 = args.first.evaluate();
            //System.out.println("arg2: " + arg2);
            switch (this.info) {
                case "lt":
                    if ( arg1 < arg2 ) {
                        return 1;
                    }
                    else {
                        return 0;
                    }
                case "le":
                    if ( arg1 <= arg2 ) {
                        return 1;
                    }
                    else {
                        return 0;
                    }
                case "eq":
                    if ( arg1 == arg2 ) {
                        return 1;
                    }
                    else {
                        return 0;
                    }
                case "ne":
                    if ( arg1 != arg2 ) {
                        return 1;
                    }
                    else {
                        return 0;
                    }
                case "or":
                    if ( arg1 != 0 || arg2 != 0 ) {
                        return 1;
                    }
                    else {
                        return 0;
                    }
                case "and":
                    if ( arg1 != 0 && arg2 != 0 ) {
                       return 1;
                    }
                    else {
                        return 0;
                    }
                case "pow":
                    return Math.pow(arg1, arg2);
                default:
                    System.out.println("ERROR in evaluate: Unrecognized bif2 " + this.info);
                    return -1;
            }    
        }
        else if ( kind.equals("var") ) {
            String varName = this.info;
            MemTable table = tables.pop();
            double value = table.retrieve(varName);
            tables.push(table);
            System.out.println(varName + " = " + value);
            return value;
        }
        else if ( kind.equals("opp") ) {
            double arg = this.first.evaluate();
            return arg * -1;
        }
        else if ( kind.equals("return") ) {
            //System.out.println("returning " + this.first.evaluate());
            //System.out.println("return first type: " + this.first);
            returnValue = this.first.evaluate();
            return returnValue;
        }
        else {
            error("Unknown node kind in evaluate [" + kind + "]");
        }

        System.out.println("Something went wrong in Node");
        return -1337;
    }// evaluate

}// Node
