package SymbolTables;

import java.util.HashMap;
import java.util.Map;

import AST.Identifier;
import AST.Type;
import Types.TypeNode;

public abstract class ClassSymbolTable extends SymbolTable{

	private String name;
	private String extendedClass;
	private boolean isExtension;

	// maps Identifier to it's type.
	protected Map<String, TypeNode> varDecls = new HashMap<String, TypeNode>();
	protected Map<String, MethodSymbolTable> methodDecls = new HashMap<String, MethodSymbolTable>();
	
	public ClassSymbolTable(String name, boolean isExtension) {
		this.name = name;
		this.isExtension = isExtension;
	}
	
	public String getClassName() {
		return name;
	}
	
	public boolean isExtension() {
		return isExtension;
	}
	
	public void setExtendedClassName(String s) {
		extendedClass = s;
	}
	
	public String getExtendedClassName() {
		return extendedClass;
	}
	
	public Map<String, TypeNode> getVarDeclarations() {
		return varDecls;
	}
	
	public Map<String, MethodSymbolTable> getMethodDeclarations() {
		return methodDecls;
	}
}
