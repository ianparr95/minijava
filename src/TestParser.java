import Scanner.*;
import Parser.*;
import AST.*;
import AST.Visitor.*;
import java_cup.runtime.Symbol;
import java.util.*;
import java.io.*;

public class TestParser {
    public static void main(String [] args) {
        try {
            // create a scanner on the input file
            scanner s = new scanner(new BufferedReader(new InputStreamReader(System.in)));
            parser p = new parser(s);
            Symbol root;
            // replace p.parse() with p.debug_parse() in next line to see trace of
            // parser shift/reduce actions during parse
            root = p.parse();
            //root = p.debug_parse();
            /*List<Statement> program = (List<Statement>)root.value;
            for (Statement statement: program) {
                statement.accept(new PrettyPrintVisitor());
				System.out.print("\n");
            }*/
            System.out.println("Line of the main class is: " + ((ASTNode)root.value).line_number);
            System.out.println("Class name is: " + ((Program)root.value).m.i1);
            System.out.println("Doing the pretty print: ");
            Program prog = (Program)root.value;
            prog.accept(new PrettyPrintVisitor());
            
            System.out.print("\nParsing completed"); 
        } catch (Exception e) {
            // yuck: some kind of error in the compiler implementation
            // that we're not expecting (a bug!)
            System.err.println("Unexpected internal compiler error: " + 
                               e.toString());
            // print out a stack dump
            e.printStackTrace();
        }
    }
}
