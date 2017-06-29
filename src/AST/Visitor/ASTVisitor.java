package AST.Visitor;

import AST.*;

// Sample print visitor from MiniJava web site with small modifications for UW CSE.
// HP 10/11

public class ASTVisitor implements Visitor {
	private int numTabs;

	// Display added for toy example language. Not used in regular MiniJava
	public void visit(Display n) {
		System.out.print("display ");
		n.e.accept(this);
		System.out.print(";");
	}

	// MainClass m;
	// ClassDeclList cl;
	public void visit(Program n) {
		System.out.println("Program");
		numTabs++;
		n.m.accept(this);
		numTabs--;
		for (int i = 0; i < n.cl.size(); i++) {
			System.out.println();
			numTabs++;
			n.cl.get(i).accept(this);
			numTabs--;
		}
	}

	// Identifier i1,i2;
	// Statement s;
	public void visit(MainClass n) {
		printTabs();
		System.out.println("MainClass  (line " + n.line_number + ")");
		numTabs++;
		n.i1.accept(this);
		System.out.println();
		n.s.accept(this);
		numTabs--;
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		printTabs();
		System.out.println("Class (line " + n.line_number + ")");
		numTabs++;
		n.i.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			System.out.println();
			n.ml.get(i).accept(this);
		}
		numTabs--;
		System.out.println();
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclExtends n) {
		printTabs();
		System.out.println("Class (line " + n.line_number + ")");
		numTabs++;
		n.i.accept(this);
		numTabs--;
		printTabs();
		System.out.println("extends");
		numTabs++;
		n.j.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			System.out.println();
			n.ml.get(i).accept(this);
		}
		numTabs--;
		System.out.println();
	}

	// Type t;
	// Identifier i;
	public void visit(VarDecl n) {
		printTabs();
		System.out.println("Declare (line " + n.line_number + ")");
		numTabs++;
		n.t.accept(this);
		numTabs--;
		printTabs();
		System.out.println("with identifier");
		numTabs++;
		n.i.accept(this);
		numTabs--;
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public void visit(MethodDecl n) {
		printTabs();
		System.out.println("MethodDecl (line " + n.line_number + ")");
		numTabs++;
		n.i.accept(this);
		printTabs();
		System.out.println("returns ");
		numTabs++;
		n.t.accept(this);
		numTabs--;
		printTabs();
		System.out.println("parameters:");
		numTabs++;
		for (int i = 0; i < n.fl.size(); i++) {
			n.fl.get(i).accept(this);
		}
		numTabs--;
		printTabs();
		System.out.println("variables:");
		numTabs++;
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		numTabs--;
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
		printTabs();
		System.out.println("Return ");
		numTabs++;
		n.e.accept(this);
		numTabs--;
		numTabs--;
	}

	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		printTabs();
		System.out.println("Declare parameter");
		numTabs++;
		n.t.accept(this);
		numTabs--;
		printTabs();
		System.out.println("with identifier");
		numTabs++;
		n.i.accept(this);
		numTabs--;
	}

	public void visit(IntArrayType n) {
		printTabs();
		System.out.println("int []");
	}

	public void visit(BooleanType n) {
		printTabs();
		System.out.println("boolean");
	}

	public void visit(IntegerType n) {
		printTabs();
		System.out.println("int");
	}

	// String s;
	public void visit(IdentifierType n) {
		printTabs();
		System.out.println(n.s);
	}

	// StatementList sl;
	public void visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
	}

	// Exp e;
	// Statement s1,s2;
	public void visit(If n) {
		printTabs();
		System.out.println("If (line " + n.line_number + ")");
		numTabs++;
		n.e.accept(this);
		numTabs--;
		printTabs();
		System.out.println("then");
		numTabs++;
		n.s1.accept(this);
		numTabs--;
		printTabs();
		System.out.println("else");
		numTabs++;
		n.s2.accept(this);
		numTabs--;
	}

	// Exp e;
	// Statement s;
	public void visit(While n) {
		printTabs();
		System.out.println("While (line " + n.line_number + ")");
		numTabs++;
		n.e.accept(this);
		numTabs--;
		printTabs();
		System.out.println("do");
		numTabs++;
		n.s.accept(this);
		numTabs--;
	}

	// Exp e;
	public void visit(Print n) {
		printTabs();
		System.out.println("Print (line " + n.line_number + ")");
		numTabs++;
		n.e.accept(this);
		numTabs--;
	}

	// Identifier i;
	// Exp e;
	public void visit(Assign n) {
		printTabs();
		System.out.println("Assign (line " + n.line_number + ")");
		numTabs++;
		n.i.accept(this);
		numTabs--;
		printTabs();
		System.out.println("the value");
		numTabs++;
		n.e.accept(this);
		numTabs--;
		System.out.println();
	}

	// Identifier i;
	// Exp e1,e2;
	public void visit(ArrayAssign n) {
		printTabs();
		System.out.println("Assign array (line " + n.line_number + ")");
		numTabs++;
		n.i.accept(this);
		numTabs--;
		printTabs();
		System.out.println("at index ");
		numTabs++;
		n.e1.accept(this);
		numTabs--;
		printTabs();
		System.out.println("the value");
		numTabs++;
		n.e2.accept(this);
		numTabs--;
	}

	// Exp e1,e2;
	public void visit(And n) {
		printTabs();
		System.out.println("And (line " + n.line_number + ")");
		numTabs++;
		n.e1.accept(this);
		numTabs--;
		printTabs();
		System.out.println("with");
		numTabs++;
		n.e2.accept(this);
		numTabs--;
	}

	// Exp e1,e2;
	public void visit(LessThan n) {
		printTabs();
		System.out.println("Less than (line " + n.line_number + ")");
		numTabs++;
		n.e1.accept(this);
		numTabs--;
		printTabs();
		System.out.println("with");
		numTabs++;
		n.e2.accept(this);
		numTabs--;
	}

	// Exp e1,e2;
	public void visit(Plus n) {
		printTabs();
		System.out.println("Add (line " + n.line_number + ")");
		numTabs++;
		n.e1.accept(this);
		numTabs--;
		printTabs();
		System.out.println("to");
		numTabs++;
		n.e2.accept(this);
		numTabs--;
	}

	// Exp e1,e2;
	public void visit(Minus n) {
		printTabs();
		System.out.println("Subtract (line " + n.line_number + ")");
		numTabs++;
		n.e1.accept(this);
		numTabs--;
		printTabs();
		System.out.println("from");
		numTabs++;
		n.e2.accept(this);
		numTabs--;
	}

	// Exp e1,e2;
	public void visit(Times n) {
		printTabs();
		System.out.println("Multiply (line " + n.line_number + ")");
		numTabs++;
		n.e1.accept(this);
		numTabs--;
		printTabs();
		System.out.println("by");
		numTabs++;
		n.e2.accept(this);
		numTabs--;
	}

	// Exp e1,e2;
	public void visit(ArrayLookup n) {
		printTabs();
		System.out.println("Lookup in array (line " + n.line_number + ")");
		numTabs++;
		n.e1.accept(this);
		numTabs--;
		printTabs();
		System.out.println("at index");
		numTabs++;
		n.e2.accept(this);
		numTabs--;
	}

	// Exp e;
	public void visit(ArrayLength n) {
		printTabs();
		System.out.println("Length of array (line " + n.line_number + ")");
		numTabs++;
		n.e.accept(this);
		numTabs--;
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public void visit(Call n) {
		printTabs();
		System.out.println("Call to class (line " + n.line_number + ")");
		numTabs++;
		n.e.accept(this);
		numTabs--;
		printTabs();
		System.out.println("method");
		numTabs++;
		n.i.accept(this);
		numTabs--;
		printTabs();
		System.out.println("with parameters: ");
		numTabs++;
		for (int i = 0; i < n.el.size(); i++) {
			n.el.get(i).accept(this);
		}
		numTabs--;
	}

	// int i;
	public void visit(IntegerLiteral n) {
		printTabs();
		System.out.println(n.i);
	}

	public void visit(True n) {
		printTabs();
		System.out.println("true");
	}

	public void visit(False n) {
		printTabs();
		System.out.println("false");
	}

	// String s;
	public void visit(IdentifierExp n) {
		printTabs();
		System.out.println(n.s);
	}

	public void visit(This n) {
		printTabs();
		System.out.println("this");
	}

	// Exp e;
	public void visit(NewArray n) {
		printTabs();
		System.out.println("New array with length (line " + n.line_number + ")");
		numTabs++;
		n.e.accept(this);
		numTabs--;
	}

	// Identifier i;
	public void visit(NewObject n) {
		printTabs();
		System.out.println("New (line " + n.line_number + ")");
		numTabs++;
		n.i.accept(this);
		numTabs--;
	}

	// Exp e;
	public void visit(Not n) {
		printTabs();
		System.out.println("Not");
		numTabs++;
		n.e.accept(this);
		numTabs--;
	}

	// String s;
	public void visit(Identifier n) {
		printTabs();
		System.out.println(n.s);
	}
	
	private void printTabs() {
		for (int i = 0; i < numTabs; i++) {
			System.out.print("    ");
		}
	}

	@Override
	public void visit(DoubleType n) {
		// TODO Auto-generated method stub
		printTabs();
		System.out.print("double");
	}

	@Override
	public void visit(DoubleLiteral n) {
		// TODO Auto-generated method stub
		printTabs();
		System.out.print(n.i);
	}
}
