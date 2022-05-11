import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ProtocoloCliente {
    public static void procesar(BufferedReader stdin, BufferedReader pin, PrintWriter pout) throws IOException {
        // lee del teclado 
        System.out.println("Escriba el mensaje para enviar: ");
        String fromUser = stdin.readLine();
        
        pout.println(fromUser);
        String fromServer = "";

        if ((fromServer = pin.readLine()) != null) {
        System.out.println("Respuesta del Servidor: " + fromServer);
        }
    }
}
