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

    private String kind;  // non-terminal or terminal category for the node
    private String info;  // extra information about the node such as
    // the actual identifier for an I

    // references to children in the parse tree
    private Node first, second, third;

    // memory table shared by all nodes
    private static MemTable table = new MemTable();

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
           // <program> -> <funcCall> | <funcCall> <funcDefs>
           if ( first == null ) {
              System.out.println("A Corgi program must have at least one function call");
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
            //System.out.println(params);
            // TODO take care of arguments

            Node statements = second;
            //System.out.println(statements);
            if ( statements != null ) {
                //System.out.println("Executing statements");
                statements.execute();
            }
            else {
                System.out.println("No statements found");
            }
        }
        else if ( kind.equals("funcCall" ) ) {
            /*
            System.out.println(this);    
            System.out.println(this.first);
            System.out.println(this.second);
            */

            String functionName = this.first.info;
            switch (functionName) {
                // Check if the called function is a built-in function
                case "print":
                    args = this.first.first;
                    // print is a bif 1
                    if ( args.first == null ) {
                        System.out.println("ERROR: print takes 1 argument, use nl() to print an empty line");    
                    } else if ( args.second != null ) {
                        System.out.println("ERROR: print takes only 1 argument");    
                    } else {
                        Node arg = args.first;
                        System.out.print(arg.evaluate());
                    }
                    break;
                case "nl":
                    System.out.print("\n");
                    break;
                case "input":
                    // TODO implement variable storage
                    String temp = keys.next();
                    System.out.println("Temp is " + temp);
                    break;
                default:
                    System.out.println("Unrecognized funcCall");
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
                    System.out.println("Temp is " + temp);
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
                    System.out.println(writeString);
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
            System.out.println("sto");
        }
        else if ( kind.equals("return") ) {
            // TODO implement return
            //System.out.println("Got return");
        }


        /* ********************************* */
        /*
        else if ( kind.equals("stmts") ) {
            if ( first != null ) {
                first.execute();
                if ( second != null ) {
                    second.execute();
                }
            }
        }

        else if ( kind.equals("prtexp") ) {
            double value = first.evaluate();
            System.out.print( value );
        }


        else if ( kind.equals("nl") ) {
            System.out.print( "\n" );
        }

        else if ( kind.equals("sto") ) {
            double value = first.evaluate();
            table.store( info, value );
        }

        else if ( kind.equals("lt") ) {
            double value = first.evaluate();
            table.store( info, value );
        }

        else if ( kind.equals("le") ) {
            double value = first.evaluate();
            table.store( info, value );
        }

        else if ( kind.equals("eq") ) {
            double value = first.evaluate();
            table.store( info, value );
        }

        else if (kind.equals("ne") ) {
            double value = first.evaluate();
            table.store( info, value );
        }

        else if (kind.equals("funcDefs")) {
            if ( first != null ) {
                first.execute();
                if ( second != null ) {
                    second.execute();
                }
            }
        }
        */

        /*
        else if (kind.equals("funcCall")) {
            if (first != null) {
                //System.out.println("Executing " + first);
                first.execute();
                if (second != null) {
                    //System.out.println("Executing " + second);
                    second.execute();
                }
            }
        }

        else if (kind.equals("param")) {
            if (first != null) {
                first.execute();
                if (second != null) {
                    second.execute();
                }
            }
        }

        else if (kind.equals("funcDef")) {
            if (first != null) {
                first.execute();
                if (second != null) {
                    second.execute();
                    if (third != null) {
                        third.execute();
                    }
                }
            }
        }
        //TODO: Check if this is right
        else if (kind.equals("sto")) {
            double value = first.evaluate();
            table.store(info, value);
        }

        else if (kind.equals("args")) {
            if (first != null) {
                first.execute();
                if (second != null) {
                    second.execute();
                }
            }
        }

        else if ( kind.equals("expr") ) {
            double value = this.evaluate();
            table.store(info, value);
        }
        */

        /*
        else if ( kind.equals("term") ) {
            double value = this.evaluate();
            table.store(info, value);
        }
        */

        else if ( kind.equals("num")) {
            double value = this.evaluate();
            table.store(info, value);
        }

        else if ( kind.equals("cond")) {
            double value = first.evaluate();
            if(value > 0 || value < 0) {
                second.execute();
            }
            else {
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
            //System.out.println(this.info);

            boolean found = false;
            // funcDefs starts out as the first funcDefs Node created by Parser
            // we need to check all the funcDefs until we find one with the
            // same name as the one that was just called.
            Node checkFunction = funcDefs;
            while ( !found ) {
                String checkFunctionName = checkFunction.first.info;
                //System.out.format("Comparing %s to %s\n", functionName, checkFunctionName);
                if ( functionName.equals(checkFunctionName) ) {
                    found = true;
                    // Reset checkFunction to beginning
                    checkFunction = funcDefs;
                    //System.out.println("Found " + checkFunctionName);
                    //System.out.println(this);
                    //System.out.println(checkFunction);
                    //System.out.println(checkFunction.first);
                    checkFunction.first.execute();
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
                        System.out.format("ERROR: %s is undefined\n", functionName);
                        System.exit(1);
                    }
                }
            }

        }
        else if ( kind.equals("args") ) {
            //System.out.println("args: " + this);    
            // TODO I *think* we should only get here if called from print,
            // so we can assume that this args node has only one arg child,
            // feels really sketchy though ...
            if ( this.second != null ) {
                System.out.println("Fix args");
                return -1;
            }
            return this.first.evaluate();
        }
        else if ( kind.equals("expr") ) {
            //System.out.println("expr");
            if ( this.info.equals("term") ) {
                //System.out.println("term " + first.info);
                return first.evaluate();
            }
            else {
                // TODO, implement + and -
            }
        }
        else if ( kind.equals("term") ) {
            //System.out.println("term " + this.info);
        }
        else if ( kind.equals("factor") ) {
            if (this.second != null) {
                return this.first.evaluate() * -1;    
            }
            else {
                return this.first.evaluate();    
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
                    return Math.sqrt(arg1);
                case "cos":
                    return Math.cos(arg1);
                case "sin":
                    return Math.sin(arg1);
                case "atan":
                    return Math.atan(arg1);
                default:
                    System.out.println("ERROR in evaluate: Unrecognized bi1 " + this.info);
            }
        }
        else if ( kind.equals("bif2") ) {
            args = this.first;
            double arg1 = args.first.evaluate();
            args = args.second;
            double arg2 = args.first.evaluate();
            switch (this.info) {
                case "lt":
                    if ( arg1 < arg2 ) {
                        //System.out.println(arg1 + " is less than " + arg2);
                        return 1;
                    }
                    else {
                        //System.out.println(arg1 + " is not less than " + arg2);
                        return 0;
                    }
                case "le":
                    if ( arg1 <= arg2 ) {
                        //System.out.println(arg1 + " is less than or equal to " + arg2);
                        return 1;
                    }
                    else {
                        //System.out.println(arg1 + " is not less than or equal to " + arg2);
                        return 0;
                    }
                case "eq":
                    if ( arg1 == arg2 ) {
                        //System.out.println(arg1 + " is equal to " + arg2);
                        return 1;
                    }
                    else {
                        //System.out.println(arg1 + " is not equal to " + arg2);
                        return 0;
                    }
                case "ne":
                    if ( arg1 != arg2 ) {
                        //System.out.println(arg1 + " is not equal to " + arg2);
                        return 1;
                    }
                    else {
                        //System.out.println(arg2 + " is equal to " + arg2);
                        return 0;
                    }
                case "or":
                    if ( arg1 != 0 || arg2 != 0 ) {
                        //System.out.println(arg1 + " or " + arg2 + " is not 0");
                        return 1;
                    }
                    else {
                        //System.out.println(arg1 + " and " + arg2 + " are both 0");    
                        return 0;
                    }
                case "and":
                    if ( arg1 != 0 && arg2 != 0 ) {
                       //System.out.println(arg1 + " and " + arg2 + " are both not 0");
                       return 1;
                    }
                    else {
                        //System.out.println(arg1 + " or " + arg2 + " are 0");
                        return 0;
                    }
                case "pow":
                    return Math.pow(arg1, arg2);
                default:
                    System.out.println("ERROR in evaluate: Unrecognized bif2 " + this.info);
                    return -1;
            }    
        }

        /*
        else if ( kind.equals("bif0") ) {
            switch (this.info) {
                case ("nl"):
                    this.execute();
                    break;
                case ("input"):
                    // TODO implement variable storage on input
                    //String temp = keys.next();
                    this.execute();
                    break;
                default:
                    System.out.println("ERROR: " + this.info + " was not recognized as a bif0");
                    System.exit(1);
            }    
        }
        else if ( kind.equals("bif1") ) {
                
        }
        else if ( kind.equals("bif2") ) {
            args = this.first;
            double arg1 = args.first.evaluate();
            args = args.second;
            double arg2 = args.first.evaluate();
            switch (this.info) {
                case "lt":    
                    if ( arg1 < arg2 ) {
                        return 1;    
                    } else {
                        return 0;    
                    }
                default:
                    System.out.println("ERROR: " + this.info + " was not recognized as a bif2");
            }
        }
        */
        // TODO implement the other bif1s
        /*
        else if ( kind.equals("bif1") ) {
            if ( info.equals("print") ) {
                System.out.println(first.evaluate());
                return 0;
            }
            else {
                System.out.format("ERROR: For some reason I thought %s was a "
                                   + "bif1, maybe parser messed up?\n", this.info);
                System.exit(1);
            }
        }
        // TODO implement the other bif2s
        else if ( kind.equals("bif2") ) {
            if ( info.equals("le") ) {
                //System.out.println("le");
                // first <= second
                double x = first.evaluate();
                double y = first.evaluate();
                if ( x <= y ) {
                    return 1;
                } else {
                    return 0;
                }
            } 
        }
        */
        /* ******************** */
        /*
        else if ( kind.equals("num") ) {
            return Double.parseDouble( info );
        }

        else if ( kind.equals("var") ) {
            return table.retrieve( info );
        }

        else if ( kind.equals("+") || kind.equals("-") ) {
            double value1 = first.evaluate();
            double value2 = second.evaluate();
            if ( kind.equals("+") )
                return value1 + value2;
            else
                return value1 - value2;
        }

        else if ( kind.equals("*") || kind.equals("/") ) {
            double value1 = first.evaluate();
            double value2 = second.evaluate();
            if ( kind.equals("*") )
                return value1 * value2;
            else
                return value1 / value2;
        }

        else if ( kind.equals("input") ) {
            return keys.nextDouble();
        }

        else if ( kind.equals("sqrt") || kind.equals("cos") ||
                kind.equals("sin") || kind.equals("atan")
                ) {
            double value = first.evaluate();

            if ( kind.equals("sqrt") )
                return Math.sqrt(value);
            else if ( kind.equals("cos") )
                return Math.cos( Math.toRadians( value ) );
            else if ( kind.equals("sin") )
                return Math.sin( Math.toRadians( value ) );
            else if ( kind.equals("atan") )
                return Math.toDegrees( Math.atan( value ) );
            else {
                error("unknown function name [" + kind + "]");
                return 0;
            }

        }

        else if ( kind.equals("pow") ) {
            double value1 = first.evaluate();
            double value2 = second.evaluate();
            return Math.pow( value1, value2 );
        }

        else if ( kind.equals("opp") ) {
            double value = first.evaluate();
            return -value;
        }

        else if ( kind.equals("lt") ) {
            double value1 = first.evaluate();
            double value2 = second.evaluate();
            if (value1 < value2) {
                return 1;
            }
            return 0;
        }

        else if ( kind.equals("le") ) {
            double value1 = first.evaluate();
            double value2 = second.evaluate();
            if (value1 <= value2) {
                return 1;
            }
            return 0;
        }
        else if ( kind.equals("eq") ) {
            double value1 = first.evaluate();
            double value2 = second.evaluate();
            if (value1 == value2) {
                return 1;
            }
            return 0;
        }

        else if ( kind.equals("ne") ) {
            double value1 = first.evaluate();
            double value2 = second.evaluate();
            if (value1 != value2) {
                return 1;
            }
            return 0;
        }
        */

        else {
            error("Unknown node kind in evaluate [" + kind + "]");
            return 0;
        }

        // TODO hopefully remove this
        return -1337;
    }// evaluate

}// Node
