import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ServidorConcurrente implements Runnable {

    private static final int PUERTO = 3400;
    //private static String salt = "#$T0d0$Sab3#M3j0r!C0n=Sal#$";
    //private String serverMessage; // Mensajes entrantes (recibidos) en el servidor
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
    //private byte[] idPaqueteCifrado;
    private byte[] arregloIvv;

    @Override
    public void run() {
        try {

            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(1024);
            KeyPair keypair = keygen.generateKeyPair();
            Cipher rsaCipher = Cipher.getInstance("RSA");
            Cipher rsaCiphera = Cipher.getInstance("AES/CBC/PKCS5Padding");
            publickey = keypair.getPublic();
            privateKey = keypair.getPrivate();

            ss = new ServerSocket(PUERTO);
            cs = new Socket();

            System.out.println("Esperando conexion...\n");

            while (contador < numeroClientes) {
                cs = ss.accept();
                System.out.println("Cliente " + contador + " en linea\n");
                contador++;
            }

            salidaCliente = new DataOutputStream(cs.getOutputStream());

            entradaCliente = new DataInputStream(cs.getInputStream());

            mensaje = entradaCliente.readUTF();

            if (mensaje.equals("INICIO")) {

                salidaCliente.writeUTF("ACK");

                mensajeNumero = entradaCliente.readUTF();

                long startTime = System.nanoTime();
                rsaCipher.init(Cipher.ENCRYPT_MODE, keypair.getPrivate());
                byte[] mensajeCifrado = rsaCipher.doFinal(mensajeNumero.getBytes("UTF8"));
                long endTime = System.nanoTime() - startTime;
                String msgt1 = "Tiempo cifrando reto con llave privada " + endTime / 1e6 + " ms.";

                salidaCliente.writeInt(mensajeCifrado.length);
                salidaCliente.write(mensajeCifrado);

                salidaClienteObjeto = new ObjectOutputStream(cs.getOutputStream());
                salidaClienteObjeto.writeObject(keypair.getPublic());
                salidaClienteObjeto.flush();

                ObjectInputStream entradaClienteObjeto = new ObjectInputStream(cs.getInputStream());
                byte[] llaveSimetricaR = (byte[]) entradaClienteObjeto.readObject();

                rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] mensajeDescifrado = rsaCipher.doFinal(llaveSimetricaR);
                llaveSimetrica = new SecretKeySpec(mensajeDescifrado, 0, mensajeDescifrado.length, "AES");

                salidaCliente.writeUTF("ACK");

                int length = entradaCliente.readInt();
                if (length > 0) {
                    nomCifrado = new byte[length];
                    entradaCliente.readFully(nomCifrado, 0, length);
                }

                rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] nombreDescifrado = rsaCipher.doFinal(nomCifrado);
                String nombreDescifrado2 = new String(nombreDescifrado, "UTF8");

                if (buscarNombre(nombreDescifrado2)) {
                    salidaCliente.writeUTF("ACK");
                } else {
                    salidaCliente.writeUTF("ERROR");
                }

                int length3 = entradaCliente.readInt();

                if (length3 > 0) {
                    arregloIvv = new byte[length3];
                    entradaCliente.readFully(arregloIvv, 0, length3);
                }
                IvParameterSpec ivv = new IvParameterSpec(arregloIvv);

                String idCPaqCifrad = entradaCliente.readUTF();

                rsaCiphera.init(Cipher.DECRYPT_MODE, llaveSimetrica, ivv);
                byte[] idPaqueteDescifrado = rsaCiphera.doFinal(Base64.getDecoder().decode(idCPaqCifrad));
                String idPaqueteDescifrado2 = new String(idPaqueteDescifrado);

                long startTime2 = System.nanoTime();
                rsaCiphera.init(Cipher.ENCRYPT_MODE, llaveSimetrica, ivv);
                //byte[] idPaqueteCifrado = rsaCiphera.doFinal(mensajeNumero.getBytes());
                //String idCifradaSimetrica = Base64.getEncoder().encodeToString(idPaqueteCifrado);
                long endTime2 = System.nanoTime() - startTime2;

                System.out.println(msgt1 + "\nTiempo en cifrar reto con llave simetrica: " + endTime2 / 1e6 + " ms.");

                String estadoo = buscarIdyRetornarEstado(nombreDescifrado2, idPaqueteDescifrado2);

                rsaCiphera.init(Cipher.ENCRYPT_MODE, llaveSimetrica, ivv);
                byte[] estadoPaqueteCifrado = rsaCiphera.doFinal(estadoo.getBytes());
                String estadoPaqueteCifrado2 = Base64.getEncoder().encodeToString(estadoPaqueteCifrado);

                salidaCliente.writeUTF(estadoPaqueteCifrado2);

                entradaCliente.readUTF();

                String arregloHmac = Hmac(llaveSimetrica, digest(nombreDescifrado2 + idPaqueteDescifrado2));
                salidaCliente.writeUTF(arregloHmac);

                String msgi = entradaCliente.readUTF();
                System.out.println(msgi);
            }
            System.out.println("Conexion finalizada");

            // terminar conexion con cliente
            ss.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                    br.close();
                    return rta;
                } else {
                    rta = "NO";
                }
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        br.close();
        return rta;
    }

    // Digest
    public static byte[] digest(String msg) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(msg.getBytes());
        return md.digest();

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

}
