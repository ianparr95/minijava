package AST.Visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import AST.*;
import SymbolTables.*;
import Types.*;

public class SymbolTableVisitor implements Visitor {

	private static enum Stage {
		COLLECT_CLASS_NAMES, COLLECT_VARIABLES_METHOD_NAMES, COLLECT_METHOD_STATEMENTS
	};

	private Stage curStage = Stage.COLLECT_CLASS_NAMES;

	// Global table:
	private static GlobalSymbolTable globalTable = new GlobalSymbolTable();

	// Current class table we are processing.
	private ClassSymbolTable curCSTable;

	// Current method table we are processing.
	private MethodSymbolTable curMTable;

	// Current type just returned that we processed:
	private TypeNode curType;
	
	private boolean fatalError = false;
	
	private static int eCount = 0;

	public static GlobalSymbolTable getGlobalTable() {
		return globalTable;
	}
	
	public static int errorCount() {
		return eCount;
	}
	
	public static void incrErrorCount() {
		eCount++;
	}
	
	public boolean fatalErrorOccurred() {
		return fatalError;
	}

	private void checkGlobalTable() {
		Map<String, ClassSymbolTable> symbolTable = globalTable.getSymbolTable();
		for (ClassSymbolTable cst : symbolTable.values()) {
			if (cst.isExtension()) {
				// Need check classes that have extends.
				// First check that the class actually exists:
				String classExtends = cst.getExtendedClassName();
				ClassSymbolTable cstParent = symbolTable.get(classExtends);
				if (cstParent == null) {
					// Error: we do not contain that class it extends.
					System.err.println("Error: class " + cst.getClassName() + " extends " 
							+ classExtends + ", but " + classExtends + " doesn't exist!");
					eCount++;
					break;
				}
				// Check that we have no inheritance loops ie:
				// a extends b, b extends c, c extends a. So we want to avoid
				// this.
				// We do this by: checking parents up until we get to a base
				// class.
				// However, at every step we check this against a set of already
				// seen classes.
				// If we saw a class already, then it is bad, we have a loop:
				Set<String> seenClasses = new HashSet<String>();
				String curClass = cst.getClassName();
				while (cst != null && cst.isExtension()) {
					if (seenClasses.contains(cst.getClassName())) {
						// We already saw this class: we have a loop
						System.err.println("Error: class " + cst.getClassName() 
						+ " has an inheritance loop with " + curClass); 
						fatalError = true;
						break;
					}
					seenClasses.add(cst.getClassName());
					cst = symbolTable.get(cst.getExtendedClassName());
				}
			}
		}
	}

	private void checkOverridingMethods() {
		Map<String, ClassSymbolTable> symbolTable = globalTable.getSymbolTable();
		for (ClassSymbolTable cst : symbolTable.values()) {
			if (cst.isExtension()) {
				// By now we know that its super class should exist.
				/*
				 * Check: If a method in a subclass overrides one in any of its
				 * superclasses, the overriding method has the same parameter
				 * list as the original method, and the result type of the
				 * method is the same as the result type of the original one, or
				 * a subclass if the original result type is a reference type.
				 */
				for (String mName : cst.getMethodDeclarations().keySet()) {
					MethodSymbolTable mstCur = cst.getMethodDeclarations().get(mName);
					TypeNode curRType = mstCur.getReturnType();
					// for each method name, check if already exists in parents.
					ClassSymbolTable cstParent = symbolTable.get(cst.getExtendedClassName());
					while (true) {
						if (cstParent.getMethodDeclarations().containsKey(mName)) {
							// We contain an overriden method: need to check
							// parameter list.
							MethodSymbolTable mstParent = cstParent.getMethodDeclarations().get(mName);
							// Check return types are the same:
							TypeNode parentRType = mstParent.getReturnType();
							// Check if return types are compatible:
							if (!compareTypes(curRType, parentRType)) {
								System.err.println("Error: method " + mstCur.getName() + " in class "
										+ mstCur.getParent().getClassName() + "; return type " + mstCur.getReturnType()
										+ " is not compatible with parent class's " + cstParent.getClassName() + " method"
										+ " whose return type is: " + mstParent.getReturnType());
								eCount++;
								break;
							}

							if (mstParent.getParameterList().size() != mstCur.getParameterList().size()) {
								System.err.println("Error: method " + mstCur.getName() + " in class "
										+ mstCur.getParent().getClassName() + " has incorrect parameter list size of "
										+ mstCur.getParameterList().size() + ".");
								System.err.println("Parent class " + cstParent.getClassName() + " has method with"
										+ " parameter list of size " + mstParent.getParameterList().size() + ".");
								eCount++;
								break;
							}
							// Now check: if parameter types are equal:
							for (int i = 0; i < mstParent.getParameterList().size(); i++) {
								Param tn = mstParent.getParameterList().get(i);
								Param curObjType = mstCur.getParameterList().get(i);
								if (!tn.getType().isCompound() || !curObjType.getType().isCompound()) {
									// at least one is a basic type, can just do simple comparison
									Param curtn = mstCur.getParameterList().get(i);
									if (curtn.getType() != tn.getType()) {
										System.err.println("Error: method " + mstCur.getName() + " in class "
												+ mstCur.getParent().getClassName() + " has incorrect type of parameter "
												+ curObjType.getType() + " for identifier " + curObjType.getId() + ".");
										System.err.println("Expected from parent class " + cstParent.getClassName()
												+ " to have parameter type of " + tn.getType() + "."); 
										eCount++;
									}
								} else {
									// not a basic type, need do more extensive
									// checking.
									String parentObjName = tn.getType().getName();
									String curObjName = curObjType.getType().getName();
									// Check if names are equal:
									if (curObjName.equals(parentObjName)) {
										// good, can continue
										continue;
									
									}
									
									// else not equal, so we know that child's
									// parameter may be a subclass
									// of whatever the parent's parameter was.
									ClassSymbolTable curObjTable = symbolTable.get(curObjName);
									if (curObjTable.getClassName().equals(parentObjName)) {
										// good
										break;
									} else {
										// bad, cause it should extend the
										// parentObjName
										System.err.println("Error: method " + mstCur.getName() + " in class "
												+ mstCur.getParent().getClassName()
												+ " has incorrect type of parameter " + curObjType.getType()
												+ " for identifier " + curObjType.getId() + ".");
										System.err.println("Expected from parent class " + cstParent.getClassName()
												+ " to have parameter type of " + tn.getType() + ".");
										eCount++;
										break;

									}
								}
							}
						}
						if (!cstParent.isExtension()) {
							break; // reached base class, and we passed all
								   // above tests, so good.
						} else {
							cstParent = symbolTable.get(cstParent.getExtendedClassName());
						}
					}
				}
			}
		}
	}

	/**
	 * Compares type a to type b. Returns true if they are equal types, or type
	 * a is a subclass of type b.
	 */
	private boolean compareTypes(TypeNode a, TypeNode b) {
		if (a == null || b == null) {
			// shouldn't happen...
			System.err.println("!!!SHOULDN'T BE REACHED!!!");
			return false;
		}
		if (a == UnknownType.getInstance() || b == UnknownType.getInstance()) {
			return true;
		}
		// special handling for arrays here:
		if (a.getName().equals(b.getName())) {
			return true;
		}
		if (!a.isCompound() && !b.isCompound()) {
			// both basic: check equality
			return a == b;
		} else if ((a.isCompound() && !b.isCompound()) || (b.isCompound() && !a.isCompound())) {
			return false; // one basic, other isn't.
		}
		// Both compounds. Need check if a is a subtype of b.
		ClassSymbolTable acst = a.getCompoundInstance();
		ClassSymbolTable bcst = b.getCompoundInstance();
		while (true) {
			if (acst.getClassName().equals(bcst.getClassName())) {
				return true;
			}
			if (!acst.isExtension()) {
				break; // bad: acst is not extension and not same name as bcst.
			}
			acst = globalTable.getSymbolTable().get(acst.getExtendedClassName());
		}
		return false;
	}

	/**
	 * Prints an error message if type a is not equal to type b (wrapper
	 * function for compareTypes)
	 * 
	 * @param a
	 *            the first type to check
	 * @param b
	 *            the second type to check
	 */
	private void checkTypes(TypeNode a, TypeNode b, String message) {
		if (!compareTypes(a, b)) {
			System.err.println(message);
			eCount++;
		}
	}

	@Override
	public void visit(Display n) {
		return;
	}

	@Override
	// MainClass m;
	// ClassDeclList cl;
	public void visit(Program n) {
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.get(i).accept(this);
		}
		// Done first pass: Do second.
		curStage = Stage.COLLECT_VARIABLES_METHOD_NAMES;
		checkGlobalTable();
		if (fatalError) {
			System.err.println("Fatal error occurred while checking class tables. Not continuing");
			return;
		}
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.get(i).accept(this);
		}

		checkOverridingMethods();
		curStage = Stage.COLLECT_METHOD_STATEMENTS;
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.get(i).accept(this);
		}
	}

	@Override
	// Identifier i1,i2;
	// Statement s;
	public void visit(MainClass n) {
		switch (curStage) {
		case COLLECT_CLASS_NAMES:
			globalTable.addSymbol(n.i1.s, new MainClassTable(n.i1.s));
			break;
		case COLLECT_VARIABLES_METHOD_NAMES:
			// nothing to do here?
			break;
		case COLLECT_METHOD_STATEMENTS:
			n.s.accept(this);
			break;
		}
	}

	@Override
	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		switch (curStage) {
		case COLLECT_CLASS_NAMES:
			NormalClassTable nct = new NormalClassTable(n.i.s, false);
			globalTable.addSymbol(n.i.s, nct);
			break;
		case COLLECT_VARIABLES_METHOD_NAMES:
			// now in second pass, want to fill up its variables + methods
			// table.
			curCSTable = globalTable.getSymbol(n.i.s);

			// First for all variables:
			for (int i = 0; i < n.vl.size(); i++) {
				n.vl.get(i).accept(this);
			}
			// For all methods.
			for (int i = 0; i < n.ml.size(); i++) {
				n.ml.get(i).accept(this);
			}
			curCSTable = globalTable.getSymbol(n.i.s);
			break;
		case COLLECT_METHOD_STATEMENTS:
			curCSTable = globalTable.getSymbol(n.i.s);
			for (int i = 0; i < n.ml.size(); i++) {
				n.ml.get(i).accept(this);
			}
			break;
		}

	}

	@Override
	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclExtends n) {
		switch (curStage) {
		case COLLECT_CLASS_NAMES:
			NormalClassTable nct = new NormalClassTable(n.i.s, true);
			nct.setExtendedClassName(n.j.s);
			globalTable.addSymbol(n.i.s, nct);
			break;
		case COLLECT_VARIABLES_METHOD_NAMES:
			// Fill up variables + methods table.
			curCSTable = globalTable.getSymbol(n.i.s);

			// First for all variables:
			for (int i = 0; i < n.vl.size(); i++) {
				n.vl.get(i).accept(this);
			}

			// Then for all methods:
			for (int i = 0; i < n.ml.size(); i++) {
				n.ml.get(i).accept(this);
			}
			curCSTable = globalTable.getSymbol(n.i.s);
			break;
		case COLLECT_METHOD_STATEMENTS:
			curCSTable = globalTable.getSymbol(n.i.s);
			for (int i = 0; i < n.ml.size(); i++) {
				n.ml.get(i).accept(this);
			}
			break;
		}
	}

	@Override
	// Type t;
	// Identifier i;
	public void visit(VarDecl n) {
		// Get type:
		n.t.accept(this);

		// add them to current table:
		curCSTable.addVarDeclaration(curType, n.i);

	}

	@Override
	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public void visit(MethodDecl n) {
		switch (curStage) {
		case COLLECT_VARIABLES_METHOD_NAMES:
			// TypeNode tn = new TypeNode();
			// Visit type node to get its type:
			n.t.accept(this);
			MethodSymbolTable mst = new MethodSymbolTable(curCSTable, curType, n.i);
			curCSTable.addMethodDeclaration(n.i.s, mst);
			for (int i = 0; i < n.fl.size(); i++) {
				// fill up param list.
				// first retrieve current type:
				n.fl.get(i).t.accept(this);
				// add it
				mst.addParameter(curType, n.fl.get(i).i);
			}
			// Fill up the var declarations in the MethodSymbolTable
			for (int i = 0; i < n.vl.size(); i++) {
				n.vl.get(i).t.accept(this);
				mst.addVarDeclaration(curType, n.vl.get(i).i);
			}
			break;
		case COLLECT_METHOD_STATEMENTS:
			curMTable = curCSTable.getMethodDeclarations().get(n.i.s);
			for (int i = 0; i < n.sl.size(); i++) {
				n.sl.get(i).accept(this);
			}
			// Check return type is same as method type.
			n.e.accept(this);
			checkTypes(curType, curMTable.getReturnType(),
					"Error: Return type doesn't match expected" + " type. Expected " + curMTable.getReturnType()
							+ ", got " + curType + ". (line " + n.line_number + ")");
			break;
		}
	}

	@Override
	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		// nothing to do here?
	}

	@Override
	public void visit(IntArrayType n) {
		curType = new IntArrType();
	}

	@Override
	public void visit(BooleanType n) {
		curType = BoolType.getInstance();
	}

	@Override
	public void visit(IntegerType n) {
		curType = IntType.getInstance();
	}

	@Override
	// String s;
	public void visit(IdentifierType n) {
		String objName = n.s;
		ClassSymbolTable cst = globalTable.getSymbol(objName);
		if (cst == null) {
			curType = UnknownType.getInstance();
			System.err.println("Error: Unknown class name. (line " + n.line_number + ")");
			eCount++;
		} else {
			curType = new ObjectType(cst);
		}

	}

	@Override
	// StatementList sl;
	public void visit(Block n) {
		// just evaluate each statement, no variables possible
		// so no additional symbol table needed
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
	}

	@Override
	// Exp e;
	// Statement s1,s2;
	public void visit(If n) {
		n.e.accept(this);
		checkTypes(curType, BoolType.getInstance(), "Error: Expected type " + BoolType.getInstance() + ", got "
				+ curType + ". (line " + n.line_number + ")");
		n.s1.accept(this);
		n.s2.accept(this);
	}

	@Override
	// Exp e;
	// Statement s;
	public void visit(While n) {
		n.e.accept(this);
		checkTypes(curType, BoolType.getInstance(), "Error: Expected type " + BoolType.getInstance() + ", got "
				+ curType + ". (line " + n.line_number + ")");
		n.s.accept(this);
	}

	@Override
	// Exp e;
	public void visit(Print n) {
		n.e.accept(this);
		if (curType != IntType.getInstance() && curType != DoubType.getInstance()) {
			System.err.println("Error: Expected type int or double for print statement. Got "
					+ curType.toString() + ". (line " + n.line_number + ")");
			eCount++;
		}
	}

	@Override
	// Identifier i;
	// Exp e;
	public void visit(Assign n) {
		// first check: i in scope
		n.i.accept(this);
		// Ok, now need check if type compatible:
		n.e.accept(this);
		// do compareTypes with curType and an, knowing that
		// curType could be a subtype of type an (eg: Map a = HashMap, cause
		// Hashmap is a subtype for map).
		TypeNode an = curMTable.lookup(n.i.s);
		checkTypes(curType, an,
				"Error: Type " + an + " not compatible with " + curType + ". (line " + n.line_number + ")");
	}

	@Override
	// Identifier i;
	// Exp e1,e2;
	public void visit(ArrayAssign n) {
		n.i.accept(this);
		n.e1.accept(this);
		checkTypes(curType, IntType.getInstance(),
				"Error: Array must indexed with int, got " + curType + ". (line " + n.line_number + ")");
		n.e2.accept(this);
		TypeNode idType = curMTable.lookup(n.i.s);
		if (idType.getName().equals("int[]")) {
			idType = IntType.getInstance();
			checkTypes(curType, idType,
					"Error: Type " + idType + " not compatible with " + curType + ". (line " + n.line_number + ")");
		}
	}

	@Override
	// Exp e1,e2;
	public void visit(And n) {
		n.e1.accept(this);
		checkTypes(curType, BoolType.getInstance(), "Error: Operand '&&' expects a boolean on the right. Received "
				+ curType + ". (line " + n.line_number + ")");
		n.e2.accept(this);
		checkTypes(curType, BoolType.getInstance(), "Error: Operand '&&' expects a boolean on the left. Received "
				+ curType + ". (line " + n.line_number + ")");
		curType = BoolType.getInstance();
	}

	@Override
	// Exp e1,e2;
	public void visit(LessThan n) {
		n.e1.accept(this);
		TypeNode firstType = curType;
		n.e2.accept(this);
		if (firstType != curType && (firstType != IntType.getInstance() || firstType != DoubType.getInstance())) {
			System.err.println("Error: Operand '<' expects types (int, int) or (double, double). Received (" + firstType + 
					", " + curType + ") (line " + n.line_number + ")");
			eCount++;
		}
		curType = BoolType.getInstance();
	}

	@Override
	// Exp e1,e2;
	public void visit(Plus n) {
		n.e1.accept(this);
		TypeNode firstType = curType;
		n.e2.accept(this);
		if (firstType != curType && (firstType != IntType.getInstance() || firstType != DoubType.getInstance())) {
			System.err.println("Error: Operand '+' expects types (int, int) or (double, double). Received (" + firstType + 
					", " + curType + ") (line " + n.line_number + ")");
			eCount++;
		}
		if (firstType.getName().equals("double")) {
			curType = DoubType.getInstance();
		} else {
			curType = IntType.getInstance();
		}
	}

	@Override
	// Exp e1,e2;
	public void visit(Minus n) {
		n.e1.accept(this);
		TypeNode firstType = curType;
		n.e2.accept(this);
		if (firstType != curType && (firstType != IntType.getInstance() || firstType != DoubType.getInstance())) {
			System.err.println("Error: Operand '-' expects types (int, int) or (double, double). Received (" + firstType + 
					", " + curType + ") (line " + n.line_number + ")");
			eCount++;
		}
		if (firstType.getName().equals("double")) {
			curType = DoubType.getInstance();
		} else {
			curType = IntType.getInstance();
		}
	}

	@Override
	// Exp e1,e2;
	public void visit(Times n) {
		n.e1.accept(this);
		TypeNode firstType = curType;
		n.e2.accept(this);
		if (firstType != curType && (firstType != IntType.getInstance() || firstType != DoubType.getInstance())) {
			System.err.println("Error: Operand '*' expects types (int, int) or (double, double). Received (" + firstType + 
					", " + curType + ") (line " + n.line_number + ")");
			eCount++;
		}
		if (firstType.getName().equals("double")) {
			curType = DoubType.getInstance();
		} else {
			curType = IntType.getInstance();
		}
	}

	@Override
	// Exp e1,e2;
	public void visit(ArrayLookup n) {
		n.e1.accept(this);
		n.e2.accept(this);
		checkTypes(curType, IntType.getInstance(),
				"Error: Array must indexed with int, got " + curType + ". (line " + n.line_number + ")");
		curType = IntType.getInstance();
	}

	@Override
	// Exp e;
	public void visit(ArrayLength n) {
		n.e.accept(this);
		if (curType != UnknownType.getInstance() && curType.getName() != "int[]") {
			System.err.println("Error: Array length function called on something that is not an array. (line"
								+ n.line_number + ")");
		}
		curType = IntType.getInstance();
	}

	@Override
	// Exp e;
	// Identifier i;
	// ExpList el;
	public void visit(Call n) {
		// first make sure expression is correct type
		n.e.accept(this);
		if (curType == UnknownType.getInstance()) {
			// System.err.println("Error: Called method: " + n.i.s + " for a class that doesn't exist. (line " + n.line_number + ")");
			// Don't need to report multiple errors
			eCount++;
			return;
		}
		if (!curType.isCompound()) {
			// bad: curType should be a compound type
			System.err.println("Error: Type " + curType + " is not an Object type. (line " + n.line_number + ")");
			eCount++;
			curType = UnknownType.getInstance();
			return;
		}
		// now check function name exists:
		String fname = n.i.s;
		MethodSymbolTable mst = curType.getCompoundInstance().getMethodDeclarations().get(fname);
		if (mst == null) {
			// method name doesn't exist:
			// need check parents.
			TypeNode pNode = curType;
			while (pNode.isChild()) {
				mst = pNode.getCompoundInstance().getMethodDeclarations().get(fname);
				//System.out.println(pNode.getCompoundInstance().getMethodDeclarations());
				if (mst != null) {
					// good.
					break;
				}
				pNode = new ObjectType(globalTable.getSymbol(pNode.getCompoundInstance().getExtendedClassName()));
			}
			mst = pNode.getCompoundInstance().getMethodDeclarations().get(fname);
			//System.out.println(pNode.getCompoundInstance().getMethodDeclarations());
			if (mst != null) {
				// good.
			}
			if (mst == null) {
				System.err.println("Error: Method " + fname + " doesn't exist in class " + curType.getName()
									+ ". (line " + n.line_number + ")");
				eCount++;
				curType = UnknownType.getInstance();
				return;
			}
		}
		// now check parameter list:
		// check sizes are equal:
		if (mst.getParameterList().size() != n.el.size()) {
			System.err.println("Error: Method " + fname + " called with incorrect number of parameters."
								+ " Expected " + mst.getParameterList().size() + " but got " + n.el.size()
								+ ". (line " + n.line_number + ")");
			eCount++;
			curType = UnknownType.getInstance();
			return;
		}
		// now check for each type in the caller:
		// they must either be equal or subtype:
		for (int i = 0; i < mst.getParameterList().size(); i++) {
			// first get type:
			n.el.get(i).accept(this);
			Param p1 = mst.getParameterList().get(i);
			if (!compareTypes(curType, p1.getType())) {
				System.err.println("Error: Method " + fname + " called with incorrect argument type."
								    + " Expected " + p1.getType() + " but got " + curType
									+ " for identifier " + p1.getId() + ". (line " + n.line_number + ")");
				eCount++;
				continue;
			}
		}
		curType = mst.getReturnType();
	}

	@Override
	// int i;
	public void visit(IntegerLiteral n) {
		curType = IntType.getInstance();
	}

	@Override
	public void visit(True n) {
		curType = BoolType.getInstance();
	}

	@Override
	public void visit(False n) {
		curType = BoolType.getInstance();
	}

	@Override
	// String s;
	public void visit(IdentifierExp n) {
		curType = curMTable.lookup(n.s);
		if (curType == null) {
			curType = UnknownType.getInstance();
			System.err.println("Error: Unknown Identifier " + n.s + ". (line " + n.line_number + ")");
			eCount++;
		}
	}

	@Override
	public void visit(This n) {
		curType = new ObjectType(curMTable.getParent());
	}

	@Override
	// Exp e;
	public void visit(NewArray n) {
		n.e.accept(this);
		if (curType != IntType.getInstance()) {
			System.err.println("Error: New array initialized without integer type as length. (line " + n.line_number + ")");
			eCount++;
		}
		curType = new IntArrType();
	}

	@Override
	// Identifier i;
	public void visit(NewObject n) {
		// make sure n.i exists.
		if (!globalTable.getSymbolTable().containsKey(n.i.s)) {
			System.err.println("Error: Class " + n.i.s + " does not exist. (line " + n.line_number + ")");
			curType = UnknownType.getInstance();
			eCount++;
			return;
		}
		// if type checks, it's good
		curType = new ObjectType(globalTable.getSymbolTable().get(n.i.s));
	}

	@Override
	// Exp e;
	public void visit(Not n) {
		n.e.accept(this);
		if (curType != BoolType.getInstance()) {
			System.err.println("Error: Unary negation operator called on a type that is not boolean. (line " + n.line_number + ")");
			eCount++;
		}
		curType = BoolType.getInstance();
	}

	@Override
	// String s;
	public void visit(Identifier n) {
		TypeNode an = curMTable.lookup(n.s);
		if (an == null) {
			// n.i.s not declared in scope, we need to
			// define it with the unknown type.
			System.err.println("Error: Variable " + n.s + " not declared before usage. (line " + n.line_number + ")");
			eCount++;
			// add it to current m table.
			curMTable.addVarDeclaration(UnknownType.getInstance(), n);
			an = UnknownType.getInstance();
		}
		curType = an;
	}

	@Override
	public void visit(DoubleType n) {
		// TODO Auto-generated method stub
		curType = DoubType.getInstance();
	}

	@Override
	public void visit(DoubleLiteral n) {
		// TODO Auto-generated method stub
		curType = DoubType.getInstance();
	}

}
