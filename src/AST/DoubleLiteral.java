package AST;
import AST.Visitor.Visitor;

public class DoubleLiteral extends Exp {
  public double i;

  public DoubleLiteral(double ai, int ln) {
    super(ln);
    i=ai;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
