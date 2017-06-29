package Types;

import SymbolTables.ClassSymbolTable;

public class BoolType extends TypeNode {
	private static BoolType instance = null;

	private BoolType(){name = "bool";};
	
	public static BoolType getInstance() {
		if (instance == null) {
			instance = new BoolType();
		}

		return instance;
	}

	@Override
	public boolean isCompound() {
		return false;
	}

	@Override
	public ClassSymbolTable getCompoundInstance() {
		return null; // bools aren't compound
	}
	
	
	@Override
	public boolean isChild() {
		return false;
	}
}
