import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import Scanner.*;
import SymbolTables.ClassSymbolTable;
import SymbolTables.MethodSymbolTable;
import Parser.*;
import AST.*;
import AST.Visitor.*;
import java_cup.runtime.Symbol;
import java.util.*;
import java.io.*;

/*
 * Runs our scanner for the MiniJava language. Takes two command line arguments: -S as the first, and the
 * name of the file to be scanned as the second. Can be ran from the ant build.xml file with the following
 * syntax: 
 * 
 * 				ant run-scanner -Dfilename="fileToScan.java"
 * 
 * Ouptuts the tokens in the given file to stdout
 */
public class MiniJava {
	
	// keeps track of if we've seen errors
	private static int errorCount = 0;
	
	public static void main(String args[]) {		
		String fileName;
		if (args.length > 1) {
			fileName = args[1];
		} else {
			fileName = args[0];
		}
		
		if (args[0].equals("-S")) {
	        try {
	            // create a scanner on the input file
	            scanner s = new scanner(new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)))));
	            Symbol t = s.next_token();
	            while (t.sym != sym.EOF){ 
	                // print each token that we scan
	            	if (t.sym == sym.error) {
	            		errorCount++;
	            	} else {
	            		System.out.print(s.symbolToString(t) + " ");
	            	}
	                t = s.next_token();
	            }
	            System.out.println("\nLexical analysis completed"); 
	            if (errorCount > 0) {
	            	System.out.println("Errors detected: " + errorCount);
	            	System.exit(1);
	            } else {
		            System.exit(0);	            	
	            }
	        } catch (Exception e) {
	            // yuck: some kind of error in the compiler implementation
	            // that we're not expecting (a bug!)
	            System.err.println("Unexpected internal compiler error: " + 
	                        e.toString());
	            // print out a stack dump
	            e.printStackTrace();
	            System.exit(1);
	        }
		} else if (args[0].equals("-P")) {
	        try {
	            // create a scanner on the input file
	            scanner s = new scanner(new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)))));
	            parser p = new parser(s);
	            Symbol root;
	            root = p.parse();
	            //root = p.debug_parse();
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
		} else if (args[0].equals("-A")) {
	        try {
	            // create a scanner on the input file
	            scanner s = new scanner(new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)))));
	            parser p = new parser(s);
	            Symbol root;
	            root = p.parse();

	            System.out.println("Line of the main class is: " + ((ASTNode)root.value).line_number);
	            System.out.println("Class name is: " + ((Program)root.value).m.i1);
	            System.out.println("Printing AST: ");

	            Program prog = (Program)root.value;
	            prog.accept(new ASTVisitor());
	            
	            System.out.print("\nParsing completed");
	            System.exit(0);
	        } catch (Exception e) {
	        	System.err.println("Syntax error (or other bug) detected");
	        	System.err.println(e.toString());
	            e.printStackTrace();
	            System.exit(1);
	        }			
		} else if (args[0].equals("-T")) { // print contents of symbol tables.
			try {
	            // create a scanner on the input file
	            scanner s = new scanner(new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)))));
	            parser p = new parser(s);
	            Symbol root;
	            root = p.parse();

	            Program prog = (Program)root.value;
	            SymbolTableVisitor stv = new SymbolTableVisitor();
	            prog.accept(stv);
	            if (stv.fatalErrorOccurred()) {
	            	System.exit(1);
	            } else if (stv.errorCount() > 0) {
	            	System.exit(1);
	            }
	            for (String ss : stv.getGlobalTable().getSymbolTable().keySet()) {
	            	System.out.print(ss);
	            	ClassSymbolTable cst = stv.getGlobalTable().getSymbolTable().get(ss);
	            	if (cst.isExtension()) {
	            		System.out.println(" extends " + stv.getGlobalTable().getSymbolTable().get(ss).getExtendedClassName());
	            	} else {
	            		System.out.println();
	            	}
	            	System.out.println("     ----------------------");
	            	// print out variables + types in each class
	            	System.out.println("     Variable Declarations: ");
	            	for (String id : cst.getVarDeclarations().keySet()) {
	            		System.out.print("     " + cst.getVarDeclarations().get(id).getName() + " " + id + "\n");
	            	}
	            	System.out.println("     ----------------------");
	            	// print out method declarations:
	            	System.out.println("     Method Declarations: ");
	            	for (String mname : cst.getMethodDeclarations().keySet()) {
	            		MethodSymbolTable mst = cst.getMethodDeclarations().get(mname);
	            		System.out.print("     " + mst.getReturnType().getName() + " " + mst.getName() + "\n");
	            		System.out.println("     Param list: " + Arrays.toString(mst.getParameterList().toArray()));
	            		System.out.println("     Var decl list: " + Arrays.toString(mst.getVarDeclList().toArray()));
	            	}
	            }
	            System.exit(0);
	            
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			try {
				// generate assembly
	            scanner s = new scanner(new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)))));
	            parser p = new parser(s);
	            Symbol root;
	            root = p.parse();

	            Program prog = (Program)root.value;
	            SymbolTableVisitor stv = new SymbolTableVisitor();
	            prog.accept(stv);
	            if (stv.fatalErrorOccurred()) {
	            	System.exit(1);
	            } else if (stv.errorCount() > 0) {
	            	System.exit(1);
	            }				
	            // made it here, so semantic check passed
	            Visitor codeGenerator = new AssemblyVisitor(SymbolTableVisitor.getGlobalTable());
	            prog.accept(codeGenerator);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
