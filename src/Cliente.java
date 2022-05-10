import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;
import java.security.SecureRandom;
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
    private ObjectOutputStream salidaServidorObjeto; 

    //Atributos cliente
    private String nombre;
	private int id;
    private int idPaquete;
    private String estadoPaqueteCliente;


   // private ObjectInputStream entradaServidorObjeto;
    
   private BigInteger prueba = new BigInteger("1000000000000000000000000");

	private String mensaje;
    private byte[] msgCifrado;

	
    public Cliente(int idd, String nom, int idPaq) throws IOException{
    	this.id = idd;
        this.nombre = nom;
        this.idPaquete = idPaq;
       
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
            
           //Verificar que se haya recibido el ACK
            if(mensaje.equals("ACK"))
            {   
                //Imprimir ACK recibido del servidor
                System.out.println("Se recibio el mensaje: "+mensaje);

                //Pendiente
                //Enviar reto de 24 digitos como string al otro lado 
                String numeroAleatorio = numAleatorio();
                salidaServidor.writeUTF(numeroAleatorio);
                //System.out.println("El reto antes de cifrar es: "+numeroAleatorio);

                //Recibir reto cifrado
                int length =entradaServidor.readInt();
                if(length>0){
                    msgCifrado = new byte[length];
                    entradaServidor.readFully(msgCifrado, 0, length);
                }
               

                //Recibir llave publica del servidor
               // ObjectInputStream entradaServidorObjeto = new ObjectInputStream(new FileInputStream("server.pub")); 
                ObjectInputStream entradaServidorObjeto = new ObjectInputStream(cs.getInputStream()); 
                PublicKey llavePublicaServidor = (PublicKey)entradaServidorObjeto.readObject();

               //Descifrar msg
                rsaCipher.init(Cipher.DECRYPT_MODE, llavePublicaServidor);
                byte[] mensajeDescifrado = rsaCipher.doFinal(msgCifrado);
                String mensajeDescifrado2 = new String(mensajeDescifrado, "UTF8");
                              
                //Verificar que el mensaje descifrado es correcto
                //En caso de que no sea el valor esperado se finaliza la conexion
               if(!mensajeDescifrado2.equals(numeroAleatorio)){
                    cs.close();
               }
               System.out.println("El reto recibido es correcto");
                

               //Generar llave simetrica
               KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
               SecureRandom secureRandom = new SecureRandom();
                int keyBitSize = 256;
                keyGenerator.init(keyBitSize, secureRandom);
                SecretKey llaveSimetrica = keyGenerator.generateKey();

                //Cifrar llave simetrica
                //Revisar no se usa utf8
                rsaCipher.init(Cipher.ENCRYPT_MODE, llavePublicaServidor);
                byte[] llaveSimetricaCifrada = rsaCipher.doFinal(llaveSimetrica.getEncoded());

                //Enviar llave Simetrica cifrada
                salidaServidorObjeto = new ObjectOutputStream(cs.getOutputStream());
                salidaServidorObjeto.writeObject(llaveSimetricaCifrada);
                salidaServidorObjeto.flush();

                //Recibir ACK  de llave simetrica
               String msg2 =  entradaServidor.readUTF();
               System.out.println("Se recibio el mensaje "+msg2+ " correspondiente a la llave simetrica");

               //Cifrar nombre del cliente con publica del servidor
               rsaCipher.init(Cipher.ENCRYPT_MODE, llavePublicaServidor);
                byte[] nombreCifrado = rsaCipher.doFinal(nombre.getBytes("UTF8"));
               
                //Enviar nombre del cliente
               salidaServidor.writeInt(nombreCifrado.length);
               salidaServidor.write(nombreCifrado);

               //Recibir ACK como confirmacion de que el nombre del cliente existe
              //Si el mensaje es diferente a ACK es porque no existe y se termina comunicacion
               String msgCN = entradaServidor.readUTF();
               System.out.println(msgCN);
               if(!msgCN.equals("ACK")){
                    cs.close();
               }

               //Cifrar id del cliente con llave simetrica
               rsaCipher.init(Cipher.ENCRYPT_MODE, llaveSimetrica);
                byte[] idCifrado = rsaCipher.doFinal(id.getBytes("UTF8"));
               
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
     private String numAleatorio(){

        //Random rand = new Random();
        //Formula utilizada: Math.random() * (max-min) + min;
        int numero = (int) (Math.random() * (100000000-10000000) + 10000000);
        
        int numero2 = (int) (Math.random() * (100000000-10000000) + 10000000);
        
        int numero3 = (int) (Math.random() * (100000000-10000000) + 10000000);
       
        String numeroCompleto = Integer.toString(numero)+Integer.toString(numero2)+Integer.toString(numero3);
        return numeroCompleto;
        }

}
