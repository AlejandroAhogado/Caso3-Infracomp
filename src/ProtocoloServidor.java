import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ProtocoloServidor {

        // observe que es un método estático. 
        // observe que lanza excepciones de tipo IOException 
        public static void procesar(BufferedReader pin, PrintWriter pout) throws IOException { 
            String inputLine; 
            String outputLine;
            // lee del flujo de entrada 
            inputLine = pin.readLine(); 
            System.out.println("Entrada a procesar: " + inputLine);
        // procesa la entrada 
            outputLine = inputLine;
        // escribe en el flujo de salida 
        pout.println(outputLine); 
        System.out.println("Salida procesada: " + outputLine);
        }
}
