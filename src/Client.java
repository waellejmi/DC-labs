import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import eniso.*;

public class Client {
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(new String[]{"-ORBInitialPort","5000","-ORBInitialHost","localhost"},null);

            NamingContextExt nameServer = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

            Calc calc = CalcHelper.narrow(nameServer.resolve_str("MyCalc"));

            long x = calc.div(8, 3);
            System.out.println("Client: result = " + x);
        } catch (eniso.DivByZeroException dze) {
            System.err.println("Division by zero from server: " + dze.message);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
