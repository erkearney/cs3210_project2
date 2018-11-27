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

        if ( kind.equals("stmts") ) {
            if ( first != null ) {
                first.execute();
                if ( second != null ) {
                    second.execute();
                }
            }
        }

        else if ( kind.equals("prtstr") ) {
            System.out.print( info );
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

        else if (kind.equals("funcCall")) {
            if (first != null) {
                first.execute();
                if (second != null) {
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
            // NOTICE: This is not finished in parser
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

        else if ( kind.equals("term") ) {
            double value = this.evaluate();
            table.store(info, value);
        }

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

        if ( kind.equals("num") ) {
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

        else {
            error("Unknown node kind [" + kind + "]");
            return 0;
        }

    }// evaluate

}// Node
