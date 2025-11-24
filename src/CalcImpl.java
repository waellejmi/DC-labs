import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import eniso.*;

public class CalcImpl extends CalcPOA {
  private ORB orb;

  public void setORB(ORB orb_val) {
    orb = orb_val;
  }

  @Override
  public long div(long a, long b) throws DivByZeroException {
    if (b == 0) {
      System.out.println("Attempted division by zero: " + a + "/" + b);
      throw new DivByZeroException("Division par zero");
    }
    long r = a / b;
    System.out.println("div(" + a + "," + b + ") = " + r);
    return r;
  }
}
