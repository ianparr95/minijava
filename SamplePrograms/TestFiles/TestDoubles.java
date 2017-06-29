class MainClass {
	public static void main(String[] args) {
		System.out.println(new A().start());
	}
}

class A {
	double fieldA;

	public int start() {
		double a;
		double b;
		int c;
		b = this.second();
		a = this.first();
		if (a < b) {
			c = 0;
		} else {
			c = 1;
		}
		return c;
	}

	public double second() {
		return 12.3;
	}

	public double first() {
		fieldA = 7.7;
		return fieldA;
	}
}