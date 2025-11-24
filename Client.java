import java.io.*;
import java.net.*;

public class Client {
  private static final String HOST = "localhost";
  private static final int PORT = 12345;

  public static void main(String[] args) throws Exception {
    Socket socket = new Socket(HOST, PORT);
    System.out.println("Connected to server");

    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

    // Thread pour lire des messages de server
    new Thread(() -> {
      try {
        String line;
        while ((line = in.readLine()) != null) {
          System.out.println(line);
        }
      } catch (IOException e) {
        System.out.println("Disconnected from server.");
      }
    }).start();

    // Envoyer user input a le server
    String input;
    while ((input = console.readLine()) != null) {
      out.println(input);
    }

  }
}
