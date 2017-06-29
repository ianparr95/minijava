package AST.Visitor;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import AST.And;
import AST.ArrayAssign;
import AST.ArrayLength;
import AST.ArrayLookup;
import AST.Assign;
import AST.Block;
import AST.BooleanType;
import AST.Call;
import AST.ClassDecl;
import AST.ClassDeclExtends;
import AST.ClassDeclSimple;
import AST.Display;
import AST.DoubleLiteral;
import AST.DoubleType;
import AST.False;
import AST.Formal;
import AST.Identifier;
import AST.IdentifierExp;
import AST.IdentifierType;
import AST.If;
import AST.IntArrayType;
import AST.IntegerLiteral;
import AST.IntegerType;
import AST.LessThan;
import AST.MainClass;
import AST.MethodDecl;
import AST.Minus;
import AST.NewArray;
import AST.NewObject;
import AST.Not;
import AST.Plus;
import AST.Print;
import AST.Program;
import AST.This;
import AST.Times;
import AST.True;
import AST.VarDecl;
import AST.While;
import SymbolTables.ClassSymbolTable;
import SymbolTables.GlobalSymbolTable;
import SymbolTables.MethodSymbolTable;
import Types.DoubType;
import Types.IntType;
import Types.TypeNode;

// Assembly visitor for generating x86 assembly code from minijava.

public class AssemblyVisitor implements Visitor {
	private int numTabs;
	private GlobalSymbolTable gst;
	private ClassSymbolTable curClass;
	private MethodSymbolTable curMethod;
	private String curThis;
	private String[] regs = { "rsi", "rdx", "rcx", "r8", "r9" };
	
	private Map<String, String> doubleList = new HashMap<String, String>();

	// we need this to ensure in subclasses, we can order the methods correctly.
	private List<ClassDecl> seenClasses = new ArrayList<ClassDecl>();

	private class CMPair {

		public CMPair(String cname, String mname) {
			this.cname = cname;
			this.mname = mname;
		}

		public String toString() {
			return "(" + cname + "," + mname + ")";
		}

		String cname;
		String mname;
	}

	private Map<ClassDecl, List<CMPair>> methodDeclStrings = new HashMap<ClassDecl, List<CMPair>>();
	private Map<ClassDecl, List<CMPair>> variableDeclStrings = new HashMap<ClassDecl, List<CMPair>>();
	private boolean parsingBaseClassesOnly = true;

	public AssemblyVisitor(GlobalSymbolTable gst) {
		this.gst = gst;
	}

	public void visit(Display n) {
		return;
	}

	private boolean firstPass = true;
	private String curType_parse;
	// MainClass m;
	// ClassDeclList cl;
	public void visit(Program n) {
//		try { 			
//			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("test.s")), true)); 		
//		} 
//		catch (FileNotFoundException e) { 			// TODO Auto-generated catch block 			
//			e.printStackTrace(); 		
//		}
		// first pass: collect method names and variable names in order.
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.get(i).accept(this);
		}
		parsingBaseClassesOnly = false;
		while (seenClasses.size() != n.cl.size()) {
			for (int i = 0; i < n.cl.size(); i++) {
				n.cl.get(i).accept(this);
			}
		}
		// do extended classes
		/*parsingBaseClassesOnly = false;
		while (seenClasses.size() != n.cl.size()) {
			for (int i = 0; i < n.cl.size(); i++) {
				if (!seenClasses.contains(n.cl.get(i))) {
					n.cl.get(i).accept(this);
				}
			}
		}*/

		firstPass = false;
		parsingBaseClassesOnly = true;
		// second pass: parse the methods themselves.
		//parsingBaseClassesOnly = true;
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.get(i).accept(this);
		}
		parsingBaseClassesOnly = false;
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.get(i).accept(this);
		}
		// do extended classes
		/*parsingBaseClassesOnly = false;
		while (seenClasses.size() != n.cl.size()) {
			for (int i = 0; i < n.cl.size(); i++) {
				if (!seenClasses.contains(n.cl.get(i))) {
					n.cl.get(i).accept(this);
				}
			}
		}*/
		// OK see if var table is correct: for debugging
		// System.out.println(variableDeclStrings);
		// should parse this after parsing classes?
		System.out.println();
		System.out.println(".text");
		System.out.println(".global _asm_main");
		System.out.println();
		n.m.accept(this);
		
		System.out.println(".data");
		for (String dName : doubleList.keySet()) {
			System.out.println(dName + ":  .double  " + doubleList.get(dName));
		}

	}

	// Identifier i1,i2;
	// Statement s;
	public void visit(MainClass n) {
		System.out.println("_asm_main:");
		System.out.println("   pushq %rbp");
		System.out.println("   movq %rsp,%rbp");
		n.s.accept(this);
		System.out.println("   movq %rbp,%rsp");
		System.out.println("   popq %rbp");
		System.out.println("   ret");
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		if (parsingBaseClassesOnly) {
			curClass = gst.getSymbol(n.i.s);
			// simple class: no super class
			if (firstPass) { // just collect variables + methods in order.
				seenClasses.add(n); // we've seen this.
				System.out.println(".data");
				System.out.println(n.i.s + "$$: .quad 0 #no superclass");
				methodDeclStrings.put(n, new ArrayList<CMPair>());
				for (int i = 0 ; i < n.ml.size(); i++) {
					System.out.println("   .quad " + n.i.s + "$" + n.ml.get(i).i.s);
					methodDeclStrings.get(n).add(new CMPair(n.i.s, n.ml.get(i).i.s));
					// done with method decl: now need declare methods themselves.
				}
				// parse variable declarations: store it in a table for later use.
				variableDeclStrings.put(n, new ArrayList<CMPair>());
				for (int i = 0 ; i < n.vl.size(); i++) {
					n.vl.get(i).accept(this);
					variableDeclStrings.get(n).add(new CMPair(curType_parse, n.vl.get(i).i.s));
				}
			} else { // second pass : parse the methods.
				System.out.println();
				System.out.println(".text");
				for (int i = 0 ; i < n.ml.size(); i++) {
					n.ml.get(i).accept(this);
				}
				return;
			}
			}
	}

	// Identifier i;
	// Identifier j;
	// TODO VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclExtends n) {
		if (!parsingBaseClassesOnly) {
			curClass = gst.getSymbol(n.i.s);
			if (!variableDeclStrings.containsKey(n)) {
				variableDeclStrings.put(n, new ArrayList<CMPair>());
			}
			if (!methodDeclStrings.containsKey(n)) {
				methodDeclStrings.put(n, new ArrayList<CMPair>());
			}
			if (firstPass && !seenClasses.contains(n)) {
				// want to collect variables + methods in order.
				// There are two types of class extends:
				// 1. Extends a base class. 2. Extends an extended class.
				ClassSymbolTable pClass = gst.getSymbol(curClass.getExtendedClassName());
				if (!pClass.isExtension()) {
					// Case 1: extends a base class, we can directly parse it.
					seenClasses.add(n);
					System.out.println(".data");
					System.out.println(n.i.s + "$$: .quad " + curClass.getExtendedClassName() + "$$" + " #superclass");
					for (int i = 0; i < seenClasses.size(); i++) {
						String cName;
						if (seenClasses.get(i) instanceof ClassDeclSimple) {
							cName = ((ClassDeclSimple) seenClasses.get(i)).i.s;
						} else {
							continue; // class not simple, so it can't be pClass
						}
						// need to get the classDeclSimple so that we can get from variable and method decls 
						if (cName.equals(pClass.getClassName())) {
							ClassDecl parentDecl = seenClasses.get(i);
							List<CMPair> mdcl = methodDeclStrings.get(parentDecl);
							List<CMPair> vdcl = variableDeclStrings.get(parentDecl);
							// now for variables: want to copy everything over from parentDecl to us.
							for (int j = 0 ; j < vdcl.size(); j++) {
								variableDeclStrings.get(n).add(vdcl.get(j));
							}
							// now add own variables
							for (int j = 0; j < n.vl.size(); j++) {
								n.vl.get(j).accept(this);
								variableDeclStrings.get(n).add(new CMPair(curType_parse, n.vl.get(j).i.s));
							}
							// now for methods: want to copy everything over from parentDecl to us.
							// But if it exists in us: don't do it!.
							Set<String> seenMethods = new HashSet<String>();
							for (int j = 0 ; j < mdcl.size(); j++) {
								MethodSymbolTable mst = curClass.getMethodDeclarations().get(mdcl.get(j).mname);
								if (mst != null) {
									// contain it.
									System.out.println("   .quad " + curClass.getClassName() + "$" + mdcl.get(j).mname);
									seenMethods.add(mdcl.get(j).mname);
									methodDeclStrings.get(n).add(new CMPair(curClass.getClassName(), mdcl.get(j).mname));
								} else {
									System.out.println("   .quad " + mdcl.get(j).cname + "$" + mdcl.get(j).mname);
									methodDeclStrings.get(n).add(mdcl.get(j));
								}
							}
							// now add own methods
							for (int j = 0; j < n.ml.size(); j++) {
								if (!seenMethods.contains(n.ml.get(j).i.s)) {
									System.out.println("   .quad " + n.i.s + "$" + n.ml.get(j).i.s);
									methodDeclStrings.get(n).add(new CMPair(n.i.s, n.ml.get(j).i.s));
								}
							}
						}
						// all good.
					}
				} else { // we extend an extended class: make sure we parsed parent class.
					for (ClassDecl cd : seenClasses) {
						String cname;
						if (cd instanceof ClassDeclExtends) {
							cname = ((ClassDeclExtends) cd).i.s;
							if (cname.equals(pClass.getClassName())) {
								// good, we've parsed it, now we can parse this.
								seenClasses.add(n);
								ClassDecl parentDecl = cd;
								List<CMPair> mdcl = methodDeclStrings.get(parentDecl);
								List<CMPair> vdcl = variableDeclStrings.get(parentDecl);
								System.out.println(".data");
								System.out.println(n.i.s + "$$: .quad " + curClass.getExtendedClassName() + "$$" + " #superclass");
								// now for variables: want to copy everything over from parentDecl to us.
								for (int j = 0 ; j < vdcl.size(); j++) {
									variableDeclStrings.get(n).add(vdcl.get(j));
								}
								// now add own variables
								for (int j = 0; j < n.vl.size(); j++) {
									n.vl.get(j).accept(this);
									variableDeclStrings.get(n).add(new CMPair(curType_parse, n.vl.get(j).i.s));
								}
								Set<String> seenMethods = new HashSet<String>();
								// now for methods: want to copy everything over from parentDecl to us.
								for (int j = 0 ; j < mdcl.size(); j++) {
									MethodSymbolTable mst = curClass.getMethodDeclarations().get(mdcl.get(j).mname);
									if (mst != null) {
										// contain it.
										System.out.println("   .quad " + curClass.getClassName() + "$" + mdcl.get(j).mname);
										seenMethods.add(mdcl.get(j).mname);
										methodDeclStrings.get(n).add(new CMPair(curClass.getClassName(), mdcl.get(j).mname));
									} else {
										System.out.println("   .quad " + mdcl.get(j).cname + "$" + mdcl.get(j).mname);
										methodDeclStrings.get(n).add(mdcl.get(j));
									}
								}
								// now add own methods
								for (int j = 0; j < n.ml.size(); j++) {
									System.out.println("   .quad " + n.i.s + "$" + n.ml.get(j).i.s);
									if (!seenMethods.contains(n.ml.get(j).i.s)) {
										System.out.println("   .quad " + n.i.s + "$" + n.ml.get(j).i.s);
										methodDeclStrings.get(n).add(new CMPair(n.i.s, n.ml.get(j).i.s));
									}
								}
								return;
							}
						} else {
							continue;
						}
					}
					// we didn't parse the parent class yet.
					return;
				}
			} else if (!firstPass) {
				System.out.println();
				System.out.println(".text");
				for (int i = 0 ; i < n.ml.size(); i++) {
					n.ml.get(i).accept(this);
				}
				return;
			}
		}
		//System.out.println(methodDeclStrings);
	}

	// Type t;
	// Identifier i;
	public void visit(VarDecl n) {
		// nothing to do here? Just allocate space, which can be done from
		// parent
		// TODO: get type!
		n.t.accept(this);
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public void visit(MethodDecl n) {
		curMethod = curClass.getMethodDeclarations().get(n.i.s);
		System.out.println(curClass.getClassName() + "$" + n.i.s + ":");
		System.out.println("   pushq %rbp");
		System.out.println("   movq %rsp, %rbp");
		// push "this" ptr
		System.out.println("   pushq %rdi   # push 'this'");
		// need to push parameters, so those registers don't get clobbered
		for (int i = 0; i < n.fl.size(); i++) {
			curMethod.getOffsets().put(n.fl.get(i).i.s, -8 * (i + 2));
			System.out.println("   pushq %" + regs[i]);
		}
		// now need calculate size of stack frame:
		int num_vars = n.vl.size() * 8;
		for (int i = 0; i < n.vl.size(); i++) {
			// offset is -(numParams * 8 + 8 * i)
			curMethod.getOffsets().put(n.vl.get(i).i.s, -(n.fl.size() * 8 + 8 * (i + 2)));
		}
		System.out.println("   subq $" + num_vars + ",%rsp");

		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
		n.e.accept(this);
		System.out.println("   movq %rbp,%rsp");
		System.out.println("   popq %rbp");
		System.out.println("   ret");
	}

	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		// nothing to do
	}

	public void visit(IntArrayType n) {
		// nothing to do
		curType_parse = "int []";
	}

	public void visit(BooleanType n) {
		// nothing to do
		curType_parse = "bool";
	}

	public void visit(IntegerType n) {
		// nothing to do
		curType_parse = "int";
	}

	// String s;
	public void visit(IdentifierType n) {
		// nothing to do
		curType_parse = n.s;
	}

	// StatementList sl;
	public void visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
	}

	// Exp e;
	// Statement s1,s2;
	private int curIfCount = 0;

	public void visit(If n) {
		System.out.println("   # starting if statement");
		curIfCount++;
		int current_if = curIfCount;
		n.e.accept(this);
		// now we have 1 or 0 stored in rax, a boolean value.
		System.out.println("   test %rax,%rax");
		System.out.println("   jz else$" + current_if);
		n.s1.accept(this);
		System.out.println("   jmp done$" + current_if);
		System.out.println("else$" + current_if + ":");
		n.s2.accept(this);
		System.out.println("done$" + current_if + ":");
		System.out.println("   # done with if statement");
	}

	// Exp e;
	// Statement s;
	private int curWhileCount = 0;

	public void visit(While n) {
		System.out.println("   # starting while loop");
		curWhileCount++;
		int current_while = curWhileCount;
		System.out.println("   jmp while_test$" + current_while);
		System.out.println("while_body$" + current_while + ":");
		n.s.accept(this);
		System.out.println("while_test$" + current_while + ":");
		n.e.accept(this);
		// jump if not zero back to while_body.
		System.out.println("   test %rax,%rax");
		System.out.println("   jnz while_body$" + current_while);
		System.out.println("   # done with while loop");
	}

	// Exp e;
	public void visit(Print n) {
		System.out.println("   # starting print statement");
		n.e.accept(this);
		if (processingInt) {
			System.out.println("# print int");
			System.out.println("   movq %rax,%rdi");
			System.out.println("   call put");
		} else {
			System.out.println("# print double");
			System.out.println("   movq %rax,%xmm0");
			System.out.println("   call putd");
		}

		System.out.println("   # done with print statement");
	}

	// Identifier i;
	// Exp e;
	public void visit(Assign n) {
		System.out.println("   # starting assignment statement");
		n.e.accept(this);
		// get offset of this variable to look up value
		if (curMethod.getOffsets().containsKey(n.i.s)) {
			int offset = curMethod.getOffsets().get(n.i.s);
			System.out.println("   movq %rax," + offset + "(%rbp)");
		} else {
			int offset = -99999999;
			for (ClassDecl cd : variableDeclStrings.keySet()) {
				String name;
				if (cd instanceof ClassDeclSimple) {
					name = ((ClassDeclSimple) cd).i.s;
				} else {
					name = ((ClassDeclExtends) cd).i.s;
				}
				if (name.equals(curClass.getClassName())) {
					List<CMPair> fields = variableDeclStrings.get(cd);
					for (int i = fields.size() - 1; i >= 0; i--) {
						if (fields.get(i).mname.equals(n.i.s)) {
							offset = 8 + i * 8;
							break;
						}
					}
					break;
				}
			}
			// "this" is in -8(%rbp)
			System.out.println("   pushq %rax");
			System.out.println("   movq -8(%rbp),%rax");
			System.out.println("   popq %rdx");
			System.out.println("   addq $" + offset + ",%rax");
			System.out.println("   movq %rdx,0(%rax)");
		}
		System.out.println("   # done with assignment statement");
	}

	// Identifier i;
	// Exp e1,e2;
	public void visit(ArrayAssign n) {
		System.out.println("   # starting array assign");
		if (curMethod.getOffsets().containsKey(n.i.s)) {
			int offset = curMethod.getOffsets().get(n.i.s);
			System.out.println("   movq " + offset + "(%rbp), %rax");
		} else {
			int offset = -99999999;
			for (ClassDecl cd : variableDeclStrings.keySet()) {
				String name;
				if (cd instanceof ClassDeclSimple) {
					name = ((ClassDeclSimple) cd).i.s;
				} else {
					name = ((ClassDeclExtends) cd).i.s;
				}
				if (name.equals(curClass.getClassName())) {
					List<CMPair> fields = variableDeclStrings.get(cd);
					for (int i = fields.size() - 1; i >= 0; i--) {
						if (fields.get(i).mname.equals(n.i.s)) {
							offset = 8 + i * 8;
							break;
						}
					}
					break;
				}
			}
			System.out.println("   movq -8(%rbp),%rax"); // move "this" to rax
			System.out.println("   movq " + offset + "(%rax),%rax"); // arr is
																		// in
																		// rax
		}
		System.out.println("   pushq %rax"); // stored offset in stack.
		// e1 = index, e2 = value.
		n.e1.accept(this);
		// now we have index at rax, but want to add one! (first is length)
		System.out.println("   addq $1, %rax");
		System.out.println("   pushq %rax"); // stored index.
		n.e2.accept(this);
		// now we value in %rax.
		System.out.println("   popq %rdx"); // now rdx has index
		System.out.println("   popq %rdi"); // now rdi has address
		System.out.println("   movq %rax,(%rdi, %rdx, 8)");

		System.out.println("   # done with array assign");
	}

	// Exp e1,e2;
	private int cur_and_label = 0;

	public void visit(And n) {
		System.out.println("   # starting and operation");
		cur_and_label++;
		int cur_and_label_scope = cur_and_label;
		// need care since this are short circuit operators
		// if e1 is false, no need to evaluate e2.
		n.e1.accept(this);
		// now check rax, if 1 then need evaluate e2.
		System.out.println("   test %rax,%rax");
		System.out.println("   jz and_false$" + cur_and_label_scope);
		// false: need evaluate e2.
		n.e2.accept(this);
		System.out.println("   test %rax,%rax");
		System.out.println("   jz and_false$" + cur_and_label_scope);
		// good here: set rax to 1
		System.out.println("   movq $1,%rax");
		System.out.println("   jmp and_true$" + cur_and_label_scope);
		System.out.println("and_false$" + cur_and_label_scope + ":");
		System.out.println("   movq $0,%rax"); // false.
		System.out.println("and_true$" + cur_and_label_scope + ":");
		System.out.println("   # done with and operation");
	}

	// Exp e1,e2;
	private int cur_lessthan_label = 0;

	public void visit(LessThan n) {
		cur_lessthan_label++;
		int cur_scope = cur_lessthan_label;
		// evaluates to rax 1 if true, 0 if false?
		n.e1.accept(this); // should store in rax left side.
		if (processingInt) {
			System.out.println("   # starting less than operation for int");
			// now store this in rdx.
			System.out.println("   pushq %rax");
			n.e2.accept(this); // now rax is on the stack
			// at the moment: testing rdx < rax.
			// want rax - rdx, then if > 0, then above was true.
			System.out.println("   popq %rdx");
			System.out.println("   cmpq %rdx,%rax");
			System.out.println("   jng less_than$$" + cur_scope);
			System.out.println("   movq $1, %rax");
			System.out.println("   jmp less_than_done$$" + cur_scope);
			System.out.println("less_than$$" + cur_scope + ":");
			System.out.println("   movq $0, %rax");
			System.out.println("less_than_done$$" + cur_scope + ":");
		} else {
			System.out.println("   # starting less than operation for double");
			System.out.println("   pushq %rax");
			n.e2.accept(this);
			System.out.println("   movsd 0(%rsp),%xmm0");
			System.out.println("   popq %rdi");
			System.out.println("   pushq %rax");
			System.out.println("   movsd 0(%rsp),%xmm1");
			System.out.println("   popq %rdi");
			System.out.println("   movq %xmm0,%rdi");
			System.out.println("   movq %xmm1,%rsi");
			System.out.println("   call lessThanDouble");
		}
		System.out.println("   # done with less than operation");
		
	}

	// Exp e1,e2;
	public void visit(Plus n) {
		System.out.println("   # starting addition operation");
		n.e1.accept(this);
		if (processingInt) {
			System.out.println("   #  for int");
			// System.out.println(" movq %rax,%rdx");
			System.out.println("   pushq %rax");
			n.e2.accept(this);
			System.out.println("   popq %rdx");
			System.out.println("   addq %rdx,%rax");
		} else {
			System.out.println("   #  for double");
			System.out.println("   pushq %rax");
			n.e2.accept(this);
			System.out.println("   movsd 0(%rsp),%xmm0");
			System.out.println("   popq %rdx");
			System.out.println("   pushq %rax");
			System.out.println("   movsd 0(%rsp),%xmm1");
			System.out.println("   popq %rdx");
			System.out.println("   addsd %xmm1,%xmm0");
			System.out.println("   movq %xmm0,%rax");
		}
		System.out.println("   # done with addition operation");
	}

	// Exp e1,e2;
	public void visit(Minus n) {
		System.out.println("   # starting subtraction operation");
		n.e1.accept(this);
		if (processingInt) {
			System.out.println("  # for int");
			System.out.println("   pushq %rax");
			n.e2.accept(this);
			// we want rax - rdx, but it's opposite way, cause sub isn't
			// commutative.
			System.out.println("   popq %rdx");
			System.out.println("   subq %rax,%rdx");
			System.out.println("   movq %rdx,%rax");
			System.out.println("   # done with subtraction operation");
		} else {
			System.out.println("   # for double");
			System.out.println("   pushq %rax");
			n.e2.accept(this);
			System.out.println("   movsd 0(%rsp),%xmm0");
			System.out.println("   popq %rdx");
			System.out.println("   pushq %rax");
			System.out.println("   movsd 0(%rsp),%xmm1");
			System.out.println("   popq %rdx");
			System.out.println("   subsd %xmm1,%xmm0");
			System.out.println("   movq %xmm0,%rax");
		}
	}

	// Exp e1,e2;
	public void visit(Times n) {
		System.out.println("   # starting multiplication operation");
		n.e1.accept(this);
		if (processingInt) {
			System.out.println("   #  for int");
			// System.out.println(" movq %rax,%rdx");
			System.out.println("   pushq %rax");
			n.e2.accept(this);
			System.out.println("   popq %rdx");
			System.out.println("   imulq %rdx,%rax");
		} else {
			System.out.println("   #  for double");
			System.out.println("   pushq %rax");
			n.e2.accept(this);
			System.out.println("   movsd 0(%rsp),%xmm0");
			System.out.println("   popq %rdx");
			System.out.println("   pushq %rax");
			System.out.println("   movsd 0(%rsp),%xmm1");
			System.out.println("   popq %rdx");
			System.out.println("   mulsd %xmm1,%xmm0");
			System.out.println("   movq %xmm0,%rax");
		}
		System.out.println("   # done with multiplication operation");
	}

	// Exp e1,e2;
	public void visit(ArrayLookup n) {
		System.out.println("   # starting array lookup");
		// e1 = name, e2 = offset.
		// get offset of this variable to look up value
		n.e1.accept(this);
		// now we have array at rax
		System.out.println("   pushq %rax");
		n.e2.accept(this);
		// now we have offset, but we want to add one to it
		System.out.println("   addq $1, %rax");
		System.out.println("   popq %rdx"); // now rdx has address
		System.out.println("   movq (%rdx, %rax, 8),%rax");
		System.out.println("   # done with array lookup");
	}

	// Exp e;
	public void visit(ArrayLength n) {
		System.out.println("   # starting array length");
		n.e.accept(this);
		// length stored in first!!!
		System.out.println("   movq 0(%rax),%rax");
		System.out.println("   # done with array length");
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	// Format: e.i(el)
	public void visit(Call n) {
		//System.out.println("---------");
		System.out.println("   # starting method call");
		boolean stackAligned = true;
		n.e.accept(this);
		String thisIgnoreParams = curThis;
		// parse parameters.
		// TODO:
		// firstly, "this" pointer is first argument
		// it is in %rax since we placed it there due to
		// n.e.accept(this) above.
		// System.out.println(" movq %rax,%rdi");
		System.out.println("   pushq %rax"); // save rax, since rdi can be
												// messed up!
		stackAligned = false;
		// now compute other parameters
		for (int i = 0; i < n.el.size(); i++) {
			if (!stackAligned)
				System.out.println("   pushq %rax   # align the stack");
			n.el.get(i).accept(this);
			if (!stackAligned)
				System.out.println("   popq %rdi   # undo hacky alignment");
			// push result onto stack
			System.out.println("   pushq %rax");
			stackAligned = !stackAligned;
		}

		// now load parameters to registers
		for (int i = n.el.size() - 1; i >= 0; i--) {
			System.out.println("   popq %rax");
			stackAligned = !stackAligned;
			System.out.println("   movq %rax,%" + regs[i]);
		}
		// call function via method table
		System.out.println("   popq %rdi");
		stackAligned = !stackAligned;
		System.out.println("   movq 0(%rdi),%rax");
		// want to get offset:
		String cname;
		int offset = -99999;
//		System.out.println("this: " + curThis + " (line " + n.line_number + ")");
		for (ClassDecl cd : methodDeclStrings.keySet()) {
			if (cd instanceof ClassDeclExtends) {
				cname = ((ClassDeclExtends) cd).i.s;
			} else {
				cname = ((ClassDeclSimple) cd).i.s;
			}
//			System.out.println("\t" + cname);
			if (thisIgnoreParams.equals(cname)) {
				// cd is good.
				List<CMPair> cml = methodDeclStrings.get(cd);
//				System.out.println("\t\t" + n.i.s);				
				for (int i = cml.size() - 1; i >= 0; i--) {
					// they should be in order
//					System.out.println("\t\t\t" + cml.get(i).mname + " " + cml.get(i).cname);
					if (cml.get(i).mname.equals(n.i.s)) {
//						System.out.println("made it further");
						offset = 8 + i * 8;
						break;
					}
				}
			}
		}
		if (!stackAligned)
			System.out.println("   pushq %rax   # align stack for call");
		System.out.println("   call *" + offset + "(%rax)");
		if (!stackAligned)
			System.out.println("   popq %rdi   # undo hacky alignment for call");
		System.out.println("   # done with method call ");
		// check return type:
		for (ClassDecl cd : methodDeclStrings.keySet()) {
			if (cd instanceof ClassDeclExtends) {
				cname = ((ClassDeclExtends) cd).i.s;
			} else {
				cname = ((ClassDeclSimple) cd).i.s;
			}
			if (thisIgnoreParams.equals(cname)) {
				// cd is good.
				List<CMPair> cml = methodDeclStrings.get(cd);	
				for (int i = cml.size() - 1; i >= 0; i--) {
					if (cml.get(i).mname.equals(n.i.s)) {
						processingInt = (gst.getSymbol(cml.get(i).cname).getMethodDeclarations().get(cml.get(i).mname).getReturnType()
										== IntType.getInstance());
						break;
					}
				}
			}
		}
		//System.out.println("---------");
	}

	// int i;
	public void visit(IntegerLiteral n) {
		System.out.println("   movq $" + n.i + ",%rax");
		processingInt = true;
	}

	public void visit(True n) {
		// True is just a value of 1?
		System.out.println("   movq $1,%rax");
	}

	public void visit(False n) {
		System.out.println("   movq $0,%rax");
	}

	// String s;
	public void visit(IdentifierExp n) {
		System.out.println("   # starting identifier lookup");
		// get offset of this variable to look up value
		if (curMethod.getOffsets().containsKey(n.s)) {
			curThis = curMethod.getSymbols().get(n.s).getName();
			int offset = curMethod.getOffsets().get(n.s);
			System.out.println("   movq " + offset + "(%rbp),%rax");
			//System.out.println("it's not a field!");
			
		} else { // field.
			for (ClassDecl s : variableDeclStrings.keySet()) {
				String cname;
				if(s instanceof ClassDeclSimple) {
					cname = ((ClassDeclSimple) s).i.s;
				} else {
					cname = ((ClassDeclExtends) s).i.s;
				}
				if (cname.equals(curClass.getClassName())) {
					List<CMPair> cml = variableDeclStrings.get(s);
					for (CMPair cmp : cml) {
						if (n.s.equals(cmp.mname)) {
							//System.out.println("it's not a parameter!");
							curThis = cmp.cname;
						}
					}
				}
			}


			int offset = -99999999;
			for (ClassDecl cd : variableDeclStrings.keySet()) {
				String name;
				if (cd instanceof ClassDeclSimple) {
					name = ((ClassDeclSimple) cd).i.s;
				} else {
					name = ((ClassDeclExtends) cd).i.s;
				}
				if (name.equals(curClass.getClassName())) {
					List<CMPair> fields = variableDeclStrings.get(cd);
					for (int i = fields.size() - 1; i >= 0; i--) {
						if (fields.get(i).mname.equals(n.s)) {
							offset = 8 + i * 8;
							break;
						}
					}
					break;
				}
			}
			// "this" is in -8(%rbp)
			System.out.println("   movq -8(%rbp),%rax");
			System.out.println("   addq $" + offset + ",%rax");
			System.out.println("   movq 0(%rax),%rax");
		}
		System.out.println("   # done with identifier lookup " + curThis);
		processingInt = curThis.equals("int");
	}

	public void visit(This n) {
		// TODO
		// set rax to be whereever rbp points to? THIS?test
		System.out.println("   movq -8(%rbp),%rax");
		curThis = curClass.getClassName();
	}

	// Exp e;
	public void visit(NewArray n) {
		System.out.println("   # starting new array");
		// get array length: stored in rax
		n.e.accept(this);
		System.out.println("   pushq %rax"); // save the array length.
		System.out.println("   addq $1, %rax"); // 1 extra: for length field
		System.out.println("   imulq $8, %rax"); // times 8 for num bytes
		System.out.println("   leaq 0(%rax),%rdi"); // now we have total num
														// bytes: call mjcalloc
		System.out.println("   call mjcalloc");
		System.out.println("   popq %rdi");
		// now rax points to where new array is.
		// rdi contains array length: store it to where rax points to.
		System.out.println("   movq %rdi,0(%rax)");
		System.out.println("   # done with new array");
	}

	// Identifier i;
	public void visit(NewObject n) {
		curThis = n.i.s;
		System.out.println("   # starting new object");
		int num_bytes = 8 + gst.getSymbol(n.i.s).getVarDeclarations().keySet().size() * 8;
		System.out.println("   movq $" + num_bytes + ",%rdi");
		System.out.println("   call mjcalloc");
		System.out.println("   leaq " + n.i.s + "$$,%rdx");
		System.out.println("   movq %rdx,0(%rax)"); // store vtable pointer
		// rax at this point still is at mjcalloc'd location!!! the above just
		// stored the vtable address at address of where rax points, it doesn't
		// change rax!!
		// curClass = gst.getSymbol(n.i.s);
		System.out.println("   # done with new object");
	}

	// Exp e;
	private int cur_not = 0;

	public void visit(Not n) {
		System.out.println("   # starting not operation");
		// want to set rax to 0 if it is currently 1, or 1 if currently 0.
		cur_not++;
		int cur_scope = cur_not;
		n.e.accept(this);
		System.out.println("   test %rax,%rax");
		System.out.println("   jz not_false$" + cur_scope);
		// rax is 1 here: set it to zero:
		System.out.println("   movq $0,%rax");
		System.out.println("   jmp not_done$" + cur_scope);
		System.out.println("not_false$" + cur_scope + ":");
		System.out.println("   movq $1,%rax");
		System.out.println("not_done$" + cur_scope + ":");
		System.out.println("   # done with not operation");
	}

	// String s;
	public void visit(Identifier n) {
		// shouldn't need to do anything, will be handled by parent
	}

	@Override
	public void visit(DoubleType n) {
		// TODO Auto-generated method stub
		curType_parse = "double";
	}

	private int curDouble = 0;
	private boolean processingInt;
	@Override
	public void visit(DoubleLiteral n) {
		// TODO Auto-generated method stub
		doubleList.put("double$$$" + curDouble, Double.toString((n.i)));
		System.out.println("   movsd  double$$$" + curDouble + "(%rip),%xmm0");
		System.out.println("   movq %xmm0,%rax");
		processingInt = false;
		curDouble++;
	}

}