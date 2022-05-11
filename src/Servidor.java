import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
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

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Servidor {

    private static final int PUERTO = 3400;
    private static String salt = "#$T0d0$Sab3#M3j0r!C0n=Sal#$";
    private String serverMessage; // Mensajes entrantes (recibidos) en el servidor
    private ServerSocket ss; // Socket del servidor
    private Socket cs; // Socket del cliente
    private DataOutputStream salidaCliente;
    private ObjectOutputStream salidaClienteObjeto; // Flujo de datos de salida
    private DataInputStream entradaCliente;
    private String mensaje;
    private String mensajeNumero;
    private PrivateKey privateKey;
    private PublicKey publickey;
    private SecretKey llaveSimetrica;

    private int numeroClientes = 0;
    private static int contador = 0;
    private byte[] nomCifrado;
    private byte[] idPaqueteCifrado;
    private byte[] arregloIvv;

    public Servidor(int nC) throws IOException {
        numeroClientes = nC;
    }

    enum Estado {
        PKT_EN_OFICINA, PKT_RECOGIDO, PKT_EN_CLASIFICACION, PKT_DESPACHADO,
        PKT_EN_ENTREGA, PKT_ENTREGADO, PKT_DESCONOCIDO;
    }

    //Creo que deberia ser el metodo run, para que funcione con cada thread
    public void startServer()
    { 
        while(contador<numeroClientes)
       {     
            try {
                //Generacion de llaves publica/privada servidor
                KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
                keygen.initialize(1024);
                KeyPair keypair = keygen.generateKeyPair();            
                Cipher rsaCipher = Cipher.getInstance("RSA");
                Cipher rsaCiphera = Cipher.getInstance("AES/CBC/PKCS5Padding");
                publickey = keypair.getPublic();
                privateKey = keypair.getPrivate();
            //A
            // para descifrar  
            // rsaCipher.init(Cipher.DECRYPT_MODE, keypair.getPublic());
            // byte[] mensajeDescifrado = rsaCipher.doFinal(mensajeCifrado);
            // String mensajeDescifrado2 = new String(mensajeDescifrado, "UTF8");
            // System.out.println(mensajeDescifrado2);


                //Creacion socket del cliente y del servidor
                ss = new ServerSocket(PUERTO);
                cs = new Socket(); 
                
                System.out.println("Esperando conexion...\n");

            // while(contador<numeroClientes) {
                    cs = ss.accept(); //Inicia el socket y espera una conexion desde un cliente
                    System.out.println("Cliente "+ contador + " en linea\n");
                //   contador++;
            ///}

                // Se obtiene el flujo de salida del cliente para enviarle mensajes
                salidaCliente = new DataOutputStream(cs.getOutputStream());
                
                entradaCliente = new DataInputStream(cs.getInputStream());
                
                //Lee mensaje del cliente
                mensaje = entradaCliente.readUTF();
                
                if(mensaje.equals("INICIO"))
                {
                        //Enviar mensaje al cliente de ACK
                        salidaCliente.writeUTF("ACK");
                        
                    
                    
                    //Recibir reto del cliente
                    //Falta revisar que cuando lo recibe como lo maneja byte?
                    mensajeNumero = entradaCliente.readUTF();
                    
                    //Encriptar reto cifrado con llave privada del servidor
                    long startTime = System.nanoTime();
                    rsaCipher.init(Cipher.ENCRYPT_MODE, keypair.getPrivate());
                    byte[] mensajeCifrado = rsaCipher.doFinal(mensajeNumero.getBytes("UTF8"));
                    long endTime = System.nanoTime() - startTime;
                    String msgt1 = "Tiempo cifrando reto con llave privada "+endTime/1e6+" ms.";

                    //Enviar mensaje cifrado a el cliente
                    salidaCliente.writeInt(mensajeCifrado.length);
                    salidaCliente.write(mensajeCifrado);


                    //Enviar llave publica
                    // try (FileOutputStream fos = new FileOutputStream("public.key")) {
                    // fos.write(keypair.getPublic().getEncoded());
                    //}
                    salidaClienteObjeto = new ObjectOutputStream(cs.getOutputStream());
                    salidaClienteObjeto.writeObject(keypair.getPublic());
                    salidaClienteObjeto.flush();
                    
                    //System.out.println(keypair.getPublic());

                    
                    //Imprimir Mensaje encriptado
                // System.out.println("Mensaje cifrado: "+mensajeCifrado);


                    //Recibir llave cifrada simetrica del cliente
                    ObjectInputStream entradaClienteObjeto = new ObjectInputStream(cs.getInputStream());
                    byte[] llaveSimetricaR = (byte[])entradaClienteObjeto.readObject();
                
                    //Descifrar llave simetrica
                    rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
                    byte[] mensajeDescifrado = rsaCipher.doFinal(llaveSimetricaR);
                    llaveSimetrica = new SecretKeySpec(mensajeDescifrado, 0, mensajeDescifrado.length, "AES");
                

                    //Enviar ACK de llave simetrica
                    salidaCliente.writeUTF("ACK");

                    //Recibir nombre cifrado
                    int length =entradaCliente.readInt();
                    if(length>0){
                        nomCifrado = new byte[length];
                        entradaCliente.readFully(nomCifrado, 0, length);
                    }

                    
                    //Descifrar nombre del cliente
                    rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
                    byte[] nombreDescifrado = rsaCipher.doFinal(nomCifrado);
                    String nombreDescifrado2 = new String(nombreDescifrado, "UTF8");
                    
                    //Buscar nombre del cliente en la tabla
                    //En caso de que si este envio ack, sino error
                    if(buscarNombre(nombreDescifrado2)){
                        salidaCliente.writeUTF("ACK");
                    }else{
                        salidaCliente.writeUTF("ERROR");
                    }

                    //Recibir Iv
                    
                    int length3 = entradaCliente.readInt();
                    
                    if (length3>0) {
                        arregloIvv = new byte[length3];
                        entradaCliente.readFully(arregloIvv, 0, length3);
                    }
                    IvParameterSpec ivv = new IvParameterSpec(arregloIvv);

                    //Recibir id del paquete cifrado
                    // int lengthP =entradaCliente.readInt();
                    // if(lengthP>0){
                    //     idPaqueteCifrado = new byte[lengthP];
                    //     entradaCliente.readFully(idPaqueteCifrado, 0, lengthP);
                    // }
                    String idCPaqCifrad = entradaCliente.readUTF();


                    //Descifrar id del paquete
                    rsaCiphera.init(Cipher.DECRYPT_MODE, llaveSimetrica, ivv);
                    byte[] idPaqueteDescifrado = rsaCiphera.doFinal(Base64.getDecoder().decode(idCPaqCifrad));
                    String idPaqueteDescifrado2 = new String(idPaqueteDescifrado);
                    

                    //Cifrar reto con la simetrica
                    long startTime2 = System.nanoTime();
                    rsaCiphera.init(Cipher.ENCRYPT_MODE, llaveSimetrica, ivv);
                    byte[] idPaqueteCifrado = rsaCiphera.doFinal(mensajeNumero.getBytes());
                    String idCifradaSimetrica = Base64.getEncoder().encodeToString(idPaqueteCifrado);
                    long endTime2 = System.nanoTime() - startTime2;
                    
                    System.out.println(msgt1+"\nTiempo en cifrar reto con llave simetrica: "+ endTime2/1e6+" ms.");

                    //Buscar id en la tabla
                    //En caso de que no corresponda el nombre con el id
                    // o no exista en la tabla se retorna NO
                    String estadoo = buscarIdyRetornarEstado(nombreDescifrado2, idPaqueteDescifrado2);
                    
                    //Cifrar estado de paquete con simetrica
                    rsaCiphera.init(Cipher.ENCRYPT_MODE, llaveSimetrica, ivv);
                    byte[] estadoPaqueteCifrado = rsaCiphera.doFinal(estadoo.getBytes());
                    String estadoPaqueteCifrado2 = Base64.getEncoder().encodeToString(estadoPaqueteCifrado);
            
                    //Enviar estado del paquete
                    salidaCliente.writeUTF(estadoPaqueteCifrado2);

                    //Recibir ACK del estado del paquete
                // System.out.println(entradaCliente.readUTF()+" del estado del paquete"); 
                    entradaCliente.readUTF();

                    //Calcular hmac y digest
                    String arregloHmac = Hmac(llaveSimetrica, digest(nombreDescifrado2+idPaqueteDescifrado2));
                    //System.out.println(nombreDescifrado2);
                    //System.out.println(idPaqueteDescifrado2);


                    //Enviar Hmac
                    // salidaCliente.writeInt(arregloHmac.length);
                    // salidaCliente.write(arregloHmac);
                    salidaCliente.writeUTF(arregloHmac);



                    //Recibir TERMINAR del hmac
                    //Revisar hmac, deberia ser igual con los mismos valores?
                    String msgi = entradaCliente.readUTF();
                System.out.println(msgi); 


                }

                System.out.println("Conexion finalizada");
            
                //terminar conexion con cliente
                ss.close();
            }
            catch (Exception e)
            {
            e.printStackTrace();
            }
            contador++;
        }
    }

    public String buscarIdyRetornarEstado(String Nombre, String idPaque) throws IOException {
        BufferedReader br = null;
        String line = "";
        String estadoPaquete = "";
        String rta = " ";
        try {
            br = new BufferedReader(new FileReader("src/Datos.csv"));

            String dataClientName = "";
            String idPackage = "";

            while ((line = br.readLine()) != null) {
                String[] dataArray = line.split(",");

                dataClientName = dataArray[0];
                idPackage = dataArray[1];

                if (dataClientName.equals(Nombre) && idPackage.equals(idPaque)) {
                    estadoPaquete = dataArray[2];
                    rta = estadoPaquete;
                    return rta;
                } else {
                    rta = "NO";
                }
            }

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        return rta;
    }

    public Boolean buscarNombre(String Nombre) throws IOException {
        boolean esta = false;
        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader("src/Datos.csv"));

            String dataClientName = "";

            while ((line = br.readLine()) != null) {
                String[] dataArray = line.split(",");

                dataClientName = dataArray[0];

                if (dataClientName.equals(Nombre)) {
                    esta = true;
                    ;
                }

            }

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        return esta;
    }

    // Deben venir hasheados los inputs
    public String getPackageByUser(String clientName, String packageID) throws FileNotFoundException {
        String packageState = "User name or package ID are invalid";
        Scanner sc = new Scanner(new File("Datos.csv"));

        String data = sc.nextLine();
        String dataClientName = "";
        String dataPackageID = "";

        while (data != null) {
            String[] dataArray = data.split(",");

            dataClientName = dataArray[0];
            dataPackageID = dataArray[1];

            if (dataClientName == clientName && dataPackageID == packageID) {
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

    // El mensaje se va a calcular a partir del nombre del cliente
    // y el id del paquete que este ingreso

    public static String Hmac(SecretKey llave, byte[] mensaje) {
        byte[] hmacc = null;
        StringBuilder r = new StringBuilder();
        try {
            Mac mac = Mac.getInstance("HMACSHA256");

            mac.init(llave);
            hmacc = mac.doFinal(mensaje);
            for (byte aByte : hmacc) {
                r.append(String.format("%02x", aByte));
            }

        } catch (Exception e) {
            e.getMessage();
        }
        return r.toString();
    }

    // Digest
    public static byte[] digest(String msg) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(msg.getBytes());
        return md.digest();

    }

}
