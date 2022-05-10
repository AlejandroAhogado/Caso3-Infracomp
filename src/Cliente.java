import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.*;


public class Cliente extends Main
{
	
	private static final int PUERTO = 3400;
	private final String HOST = "192.168.226.1"; 
	private Socket cs; 
	private DataOutputStream salidaServidor; 
	private DataInputStream entradaServidor;
   // private ObjectInputStream entradaServidorObjeto;
    
   private BigInteger prueba = new BigInteger("1000000000000000000000000");

	private String mensaje;

	private int id;
	
    public Cliente(int idd) throws IOException{
    	this.id = idd;
    	} 
    
    public void startClient() //deberia ser el run para los threads del cliente
    {
        try
        {
        	cs = new Socket(HOST, PUERTO); //Socket para el cliente en localhost en puerto 3400
            //Flujo de datos hacia el servidor
            salidaServidor = new DataOutputStream(cs.getOutputStream());
    
            entradaServidor = new DataInputStream(cs.getInputStream());
    
            Cipher rsaCipher = Cipher.getInstance("RSA");
           
              
            
            //Se manda al servidor el mensaje de inicio
            salidaServidor.writeUTF("INICIO");      
            
            //Recibir ACK
            mensaje = entradaServidor.readUTF();
            if(mensaje.equals("ACK"))
            {
                //Imprimir ACK recibido del servidor
                System.out.println(mensaje);

                //Pendiente
                //Enviar reto de 24 digitos
                salidaServidor.writeInt(20);; 

                //Recibir reto cifrado
                byte[] msgCifrado = entradaServidor.readAllBytes();

                //Recibir llave publica del servidor
               // ObjectInputStream entradaServidorObjeto = new ObjectInputStream(new FileInputStream("server.pub")); 
                ObjectInputStream entradaServidorObjeto = new ObjectInputStream(cs.getInputStream()); 
                PublicKey llavePublicaServidor = (PublicKey)entradaServidorObjeto.readObject();

               //Descifrar msg
                rsaCipher.init(Cipher.DECRYPT_MODE, llavePublicaServidor);
                byte[] mensajeDescifrado = rsaCipher.doFinal(msgCifrado);
                String mensajeDescifrado2 = new String(mensajeDescifrado, "UTF8");
                System.out.println(mensajeDescifrado2);
               
               
                // File publicKeyFile = new File("public.key");
                //byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
            }
            
            //Finalizar conexion                      
            cs.close();

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

     //Generar reto
     private BigInteger numAleatorio(){

        //Random rand = new Random();
        //Math.random() * (max-min) + min;
        int numero = (int) Math.random() * (99999999-10000000) + 10000000;
        int numero2 = (int) Math.random() * (99999999-10000000) + 10000000;
        int numero3 = (int) Math.random() * (99999999-10000000) + 10000000;

        String numeroCompleto = Integer.toString(numero)+Integer.toString(numero2)+Integer.toString(numero3);
        BigInteger prueba = new BigInteger(numeroCompleto);
        return prueba;
        }

}
