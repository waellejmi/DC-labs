import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import eniso.*;

public class Serveur {
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(new String[]{"-ORBInitialPort","5000","-ORBInitialHost","localhost"},null);

            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            NamingContextExt nameServer = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

            CalcImpl calcImpl = new CalcImpl();
            calcImpl.setORB(orb);

            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(calcImpl);
            Calc calc = CalcHelper.narrow(ref);

            nameServer.rebind(nameServer.to_name("MyCalc"), calc);

            System.out.println("Serveur: Calc bound as 'MyCalc' and waiting...");
            orb.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
