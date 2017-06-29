class MainClass {
	public static void main(String[] args) {
		System.out.println(new A().start());
	}
}

class A {
	int count;
	int otherCount;
	boolean test;
	public int start() {
		int aux;
		if (count < 100) {
			System.out.println(count);
			count = count + 1;
			if (test) {
				System.out.println(0 - otherCount);
			} else {

			}
			otherCount = otherCount + 1;
			test = !test;
			aux = this.start();
		} else {
			aux = 0 - 1;
		}
		return aux;
	}
}