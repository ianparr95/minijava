package SymbolTables;

import AST.Identifier;
import AST.Type;
import Types.TypeNode;

// Normal Class, cause it ain't the Main Class!
public class NormalClassTable extends ClassSymbolTable {

	public NormalClassTable(String name, boolean isExtension) {
		super(name, isExtension);
	}

	@Override
	public void addVarDeclaration(TypeNode type, Identifier name) {
		addToTable(varDecls, name.s, type);
	}

	@Override
	public void addMethodDeclaration(String name, MethodSymbolTable mst) {
		addToTable(methodDecls, name, mst);
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

	public String toString() {
		return this.getClassName();
	}

}
