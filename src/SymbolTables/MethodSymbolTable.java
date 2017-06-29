package SymbolTables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import AST.Identifier;
import AST.Visitor.SymbolTableVisitor;
import Types.TypeNode;

public class MethodSymbolTable extends SymbolTable{

	private ClassSymbolTable parent;
	private TypeNode rType;
	private Identifier id;
	private List<Param> paramList;
	private List<Param> vList; // var. decl. list
	//private List<Statement> sList; // statement list
	private Map<String, TypeNode> symbolTable;
	private Map<String, Integer> symbolOffsets;
	
	public MethodSymbolTable(ClassSymbolTable parent, TypeNode returnType, Identifier id) {
		super();
		this.parent = parent;
		this.rType = returnType;
		this.id = id;
		paramList = new ArrayList<Param>();
		vList = new ArrayList<Param>();
		symbolTable = new HashMap<String, TypeNode>();
		symbolOffsets = new HashMap<String, Integer>();
	}
	
	public void addParameter(TypeNode t, Identifier id) {
		// TODO: FOR PARAM, VARDECL, CHECK DUPLICATES!!!
		paramList.add(new Param(t, id.s));
		this.addToTable(symbolTable, id.s, t);
	}
	
	public void addVarDeclaration(TypeNode t, Identifier id) {
		vList.add(new Param(t, id.s));
		this.addToTable(symbolTable, id.s, t);
	}
	
	public TypeNode getReturnType() {
		return rType;
	}
	
	public Identifier getName() {
		return id;
	}
	
	public List<Param> getParameterList() {
		return paramList;
	}
	
	public List<Param> getVarDeclList() {
		return vList;
	}
	
	public Map<String, TypeNode> getSymbols() {
		return symbolTable;
	}
	
	public TypeNode lookup(String name) {
		// looks up first: from symbolTable.
		// then in parent's class: checks for var declaration.
		// then checks extended
		// TODO:
		TypeNode tn = symbolTable.get(name);
		if (tn != null) {
			return tn;
		}
		// tn null, need check up: check class table.
		tn = parent.varDecls.get(name);
		if (tn != null) {
			return tn;
		}
		if (parent.isExtension()) {
		// tn still null: need check what our parent class extends:
			ClassSymbolTable cstparent = SymbolTableVisitor.getGlobalTable().getSymbol(parent.getExtendedClassName());
			while (true) {
				tn = cstparent.getVarDeclarations().get(name);
				if (tn != null) {
					return tn;
				}
				if (!cstparent.isExtension()) {
					break;
				}
				cstparent = SymbolTableVisitor.getGlobalTable().getSymbol(cstparent.getExtendedClassName());
			}
		}
		return tn;
	}

	public void addMethodDeclaration(String name, MethodSymbolTable mst) {
		System.err.println("Error: Method " + name + " can't be declared at this scope");
	}

	public void addSymbolTable(String id, SymbolTable table) {
		System.err.println("Error: Can't have nested scopes within methods");
	}

	public SymbolTable getSymbol(String id) {
		System.err.println("Error: Method does not contain nested scopes");
		return null;
	}
	
	public ClassSymbolTable getParent() {
		return this.parent;
	}
	
	public Map<String, Integer> getOffsets() {
		return symbolOffsets;
	}
}
