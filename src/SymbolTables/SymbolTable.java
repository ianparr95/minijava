package SymbolTables;

import java.util.Map;

import AST.Identifier;
import AST.Visitor.SymbolTableVisitor;
import Types.TypeNode;
import Types.UnknownType;

public abstract class SymbolTable {
	
	/**
	 * Maps the identifier with the given name to the given info in the given symbolTable,
	 * prints an error if an identifier with the given name already exists.
	 * @param symbolTable The symbol table to add the mapping to
	 * @param name The name of the new identifier
	 * @param info The info that the new identifier should map to
	 */
	public <T> void addToTable(Map<String, T> symbolTable, String name, T info) {
		if (symbolTable.containsKey(name)) {
			System.err.println("Error: Identifier " + name + " already declared.");
			SymbolTableVisitor.incrErrorCount();
		} else {
			symbolTable.put(name, info);
		}
	}
	
	/**
	 * Makes sure that the given symbol table contains an entry for the given name,
	 * displays an error if the name already exists, then adds an entry for
	 * that name in the symbol table with type 'UnknownType'. 
	 * @param symbolTable The symbol table to check for a mapping in
	 * @param name The name to check for existence
	 * @return true if the table contains the given name, false otherwise
	 */
	public boolean lookup(Map<String, TypeNode> symbolTable, String name) {
		if (!symbolTable.containsKey(name)) {
			System.err.println("Error: Identifier " + name + " not found.");
			symbolTable.put(name, UnknownType.getInstance());
			return false;
		} else {
			return true;
		}
	}
	
	public abstract void addVarDeclaration(TypeNode type, Identifier name);
	public abstract void addMethodDeclaration(String name, MethodSymbolTable mst);
	public abstract void addSymbolTable(String id, SymbolTable table);
	public abstract SymbolTable getSymbol(String id);

}
