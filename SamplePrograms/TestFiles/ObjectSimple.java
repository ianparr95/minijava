class MainClass {
	public static void main(String[] args) {
		System.out.println(new A().start());
	}
}

class A extends B {
	int[] first;

	public int[] start() {
		return first;
	}
}

class B {
	int first;
}