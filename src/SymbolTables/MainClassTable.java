package SymbolTables;

import AST.Identifier;
import Types.TypeNode;
import Types.VoidType;

public class MainClassTable extends ClassSymbolTable {

	public MainClassTable(String name) {
		super(name, false);
		// Only contains one function: main. Add it now.
		methodDecls.put("main", new MethodSymbolTable(this, VoidType.getInstance(), new Identifier("main", -1)));
		// need add param list.
	}

	@Override
	public void addVarDeclaration(TypeNode type, Identifier name) {
		// Main Class can't contain var declarations
		System.err.println("Error: Identifier " + name + " can't be declared, "
				+ "Main class can't contain variable declarations");
	}

	@Override
	public void addMethodDeclaration(String name, MethodSymbolTable mst) {
		// Main Class can't contain method declarations
		System.err.println("Error: Method " + name + " can't be declared, "
				+ "Main class can't contain method declarations");
		
	}

	@Override
	public void addSymbolTable(String id, SymbolTable table) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SymbolTable getSymbol(String id) {
		// TODO Auto-generated method stub
		return null;
	}
}
