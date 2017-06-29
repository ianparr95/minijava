package SymbolTables;

import java.util.HashMap;
import java.util.Map;

import AST.Identifier;
import Types.TypeNode;

public class GlobalSymbolTable extends SymbolTable{
	
	private Map<String, ClassSymbolTable> symbolTable;
	
	public GlobalSymbolTable() {
		symbolTable = new HashMap<String, ClassSymbolTable>();
	}
	
	public Map<String, ClassSymbolTable> getSymbolTable() {
		return symbolTable;
	}
	
	public void addSymbol(String id, ClassSymbolTable table) {
		addToTable(symbolTable, id, table);
	}
	
	public ClassSymbolTable getSymbol(String id) {
		return symbolTable.get(id);
	}

	public void addVarDeclaration(TypeNode type, Identifier name) {
		System.err.println("Error: Identifier " + name + " can't be declared at this scope");
	}

	public void addMethodDeclaration(String name, MethodSymbolTable mst) {
		System.err.println("Error: Method " + name + " can't be declared at this scope");
	}

	@Override
	public void addSymbolTable(String id, SymbolTable table) {
		// TODO Auto-generated method stub
		
	}

}
