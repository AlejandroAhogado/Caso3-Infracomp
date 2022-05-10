import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

public class Servidor {

    private static final int PUERTO = 3400;
    private String serverMessage; // Mensajes entrantes (recibidos) en el servidor
    private ServerSocket ss; // Socket del servidor
    private Socket cs; // Socket del cliente
    private DataOutputStream salidaCliente; // Flujo de datos de salida
    private static String salt = "#$T0d0$Sab3#M3j0r!C0n=Sal#$"; // Sal para el hash

    private int numeroClientes = 0;
    private static int contador = 0;

    public Servidor(int nC) throws IOException {
        numeroClientes = nC;
    }

    enum Estado {
        PKT_EN_OFICINA, PKT_RECOGIDO, PKT_EN_CLASIFICACION, PKT_DESPACHADO,
        PKT_EN_ENTREGA, PKT_ENTREGADO, PKT_DESCONOCIDO;
    }

    public void startServer()// M�todo para iniciar el servidor
    {
        try {
            ss = new ServerSocket(PUERTO);
            cs = new Socket();

            System.out.println("Esperando conexion");

            while (contador < numeroClientes) {
                cs = ss.accept(); // Accept comienza el socket y espera una conexi�n desde un cliente
                System.out.println("Cliente " + contador + " en linea");
                contador++;
            }

            // Se obtiene el flujo de salida del cliente para enviarle mensajes
            salidaCliente = new DataOutputStream(cs.getOutputStream());

            // Se le envia un mensaje al cliente usando su flujo de salida
            salidaCliente.writeUTF("Peticion recibida y aceptada");

            // Se obtiene el flujo entrante desde el cliente
            BufferedReader entrada = new BufferedReader(new InputStreamReader(cs.getInputStream()));

            while ((serverMessage = entrada.readLine()) != null) // Mientras haya mensajes desde el cliente
            {
                // Se muestra por pantalla el mensaje recibido
                System.out.println(serverMessage);
            }

            System.out.println("Fin de la conexion");

            ss.close();// Se finaliza la conexi�n con el cliente
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Deben venir hasheados los inputs
    public String getPackageByUser(String clientName, String packageID) throws FileNotFoundException 
    {        
        String packageState = "User name or package ID are invalid";
        Scanner sc = new Scanner(new File("Datos.csv"));

        String data = sc.nextLine();
        String dataClientName = "";
        String dataPackageID = "";
        
        while (data != null) {
            String[] dataArray = data.split(",");
            
            dataClientName = dataArray[0];
            dataPackageID = dataArray[1];
            
            if (dataClientName == clientName && dataPackageID == packageID){
                packageState = dataArray[2]; 
                return packageState; 
            }
            
            data = sc.nextLine();
        
        }
        return packageState;
    }

    public static String getHashMd5(String input) // Algoritmo para hash de datos.
    {
        input = input + salt;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
