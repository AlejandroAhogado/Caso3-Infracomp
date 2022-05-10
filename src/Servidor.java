import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;


import javax.crypto.*;
import java.security.*;
import javax.crypto.Cipher;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Servidor {

	
	private static final int PUERTO = 3400;
	private String serverMessage; //Mensajes entrantes (recibidos) en el servidor
	private ServerSocket ss; //Socket del servidor
	private Socket cs; //Socket del cliente
	private DataOutputStream salidaCliente;
    private ObjectOutputStream salidaClienteObjeto; //Flujo de datos de salida
	private DataInputStream entradaCliente;
    private String mensaje;
    private String mensajeNumero;
    private PrivateKey privateKey;
    private PublicKey publickey;

	private int numeroClientes = 0;
	private static int contador = 0;
    
	
    public Servidor(int nC) throws IOException{
    	numeroClientes = nC;
    } 

    enum Estado
    {
        PKT_EN_OFICINA, PKT_RECOGIDO, PKT_EN_CLASIFICACION, PKT_DESPACHADO,
        PKT_EN_ENTREGA, PKT_ENTREGADO, PKT_DESCONOCIDO;
    }

    //Creo que deberia ser el metodo run, para que funcione con cada thread
    public void startServer()
    { 

        
        try {
            //Generacion de llaves publica/privada servidor
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keypair = keygen.generateKeyPair();            
            Cipher rsaCipher = Cipher.getInstance("RSA");
            publickey = keypair.getPublic();
            privateKey = keypair.getPrivate();
           
        // para descifrar  
         // rsaCipher.init(Cipher.DECRYPT_MODE, keypair.getPublic());
         // byte[] mensajeDescifrado = rsaCipher.doFinal(mensajeCifrado);
         // String mensajeDescifrado2 = new String(mensajeDescifrado, "UTF8");
         // System.out.println(mensajeDescifrado2);
                       



            //Creacion socket del cliente y del servidor
        	ss = new ServerSocket(PUERTO);
    		cs = new Socket(); 
        	
            System.out.println("Esperando conexion\n");

            while(contador<numeroClientes) {
            	 cs = ss.accept(); //Inicia el socket y espera una conexion desde un cliente
            	 System.out.println("Cliente "+ contador + " en linea\n");
                 contador++;
            }

            // Se obtiene el flujo de salida del cliente para enviarle mensajes
            salidaCliente = new DataOutputStream(cs.getOutputStream());
            salidaClienteObjeto = new ObjectOutputStream(cs.getOutputStream());
            entradaCliente = new DataInputStream(cs.getInputStream());
          
            //Lee mensaje del cliente
             mensaje = entradaCliente.readUTF();
             if(mensaje.equals("INICIO"))
             {
                //Enviar mensaje al cliente de ACK
                salidaCliente.writeUTF("ACK");
             }
             
             //Recibir reto del cliente
             //Falta revisar que cuando lo recibe como lo maneja byte?
             mensajeNumero = String.valueOf(entradaCliente.readInt());

             //Encriptar reto cifrado con llave privada del servidor
             rsaCipher.init(Cipher.ENCRYPT_MODE, keypair.getPrivate());
             byte[] mensajeCifrado = rsaCipher.doFinal(mensajeNumero.getBytes("UTF8"));
            
             //Enviar mensaje cifrado a el cliente
             salidaCliente.write(mensajeCifrado);


            //Enviar llave publica
            // try (FileOutputStream fos = new FileOutputStream("public.key")) {
            // fos.write(keypair.getPublic().getEncoded());
            //}
            salidaClienteObjeto.writeObject(keypair.getPublic());
            salidaClienteObjeto.flush();
            
            System.out.println(keypair.getPublic());

            
             //Imprimir Mensaje encriptado
             System.out.println("Mensaje cifrado: "+mensajeCifrado);

            System.out.println("Conexion finalizada");
          
            //terminar conexion con cliente
            ss.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
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
