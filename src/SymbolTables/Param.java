package SymbolTables;

import Types.TypeNode;

public class Param {
	
	private TypeNode type;
	private String id;
	
	public Param(TypeNode type, String id) {
		this.type = type;
		this.id = id;
	}
	
	public TypeNode getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}
	
	public String toString() {
		return type + " " + id;
	}

}
