class M {
    public static void main(String[] args) {
        System.out.println(new A().a());
    }
}

class A {
    B b;
    public int a() {
        b = new B();
        return this.h(b);
    }

    public int h(A zz) {
        return zz.get_val();
    }

    public int get_val() {
        return 45;
    }
}

class B extends A {
    public int get_val() {
        return 1337;
    }
}