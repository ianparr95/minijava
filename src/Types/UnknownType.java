package Types;

import SymbolTables.ClassSymbolTable;

public class UnknownType extends TypeNode {
	private static UnknownType instance = null;

	private UnknownType(){name = "!!error!!";};
	
	public static UnknownType getInstance() {
		if (instance == null) {
			instance = new UnknownType();
		}
		return instance;
	}

	@Override
	public boolean isCompound() {
		return false;
	}

	@Override
	public ClassSymbolTable getCompoundInstance() {
		return null; // int's aren't compound
	}

	// false or true? TODO
	@Override
	public boolean isChild() {
		return false;
	}
}
