public class ServeurDeNom {
    public static void main(String[] args) {
        try {
            String[] cmd = new String[] { "orbd", "-ORBInitialPort", "5000" };
            System.out.println("Started ORB");
            java.lang.Runtime.getRuntime().exec(cmd);
            Object o = new Object();
            synchronized(o) {
            o.wait();
      }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
