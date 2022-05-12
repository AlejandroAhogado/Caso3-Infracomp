import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.*;

public class ClienteConcurrente extends Main implements Runnable {

    private static final int PUERTO = 3400;
    private static final String HOST = "localhost";

    private Socket cs;
    private DataOutputStream salidaServidor;
    private DataInputStream entradaServidor;
    private ObjectOutputStream salidaServidorObjeto;

    // Atributos cliente
    private String nombre;
    private String idPaquete;
    private String estadoPaqueteCliente;

    private byte[] arregloMacD;

    // private ObjectInputStream entradaServidorObjeto;

    private BigInteger prueba = new BigInteger("1000000000000000000000000");

    private String mensaje;
    private byte[] msgCifrado;

    public ClienteConcurrente(String nom, String idPaq) throws IOException {
        this.nombre = nom;
        this.idPaquete = idPaq;
        cs = new Socket(HOST, PUERTO); // Socket para el cliente en localhost en puerto 3400
    }

    @Override
    public void run() // deberia ser el run para los threads del cliente
    {
        try {

            // Flujo de datos hacia el servidor
            salidaServidor = new DataOutputStream(cs.getOutputStream());

            entradaServidor = new DataInputStream(cs.getInputStream());

            Cipher rsaCipher = Cipher.getInstance("RSA");
            Cipher rsaCiphera = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Se manda al servidor el mensaje de inicio
            salidaServidor.writeUTF("INICIO");

            // Recibir ACK
            mensaje = entradaServidor.readUTF();

            // Verificar que se haya recibido el ACK
            if (mensaje.equals("ACK")) {
                // Imprimir ACK recibido del servidor
                // System.out.println("Se recibio el mensaje: "+mensaje);

                // Pendiente
                // Enviar reto de 24 digitos como string al otro lado
                String numeroAleatorio = numAleatorio();
                salidaServidor.writeUTF(numeroAleatorio);
                // System.out.println("El reto antes de cifrar es: "+numeroAleatorio);

                // Recibir reto cifrado
                int length = entradaServidor.readInt();
                if (length > 0) {
                    msgCifrado = new byte[length];
                    entradaServidor.readFully(msgCifrado, 0, length);
                }

                // Recibir llave publica del servidor
                // ObjectInputStream entradaServidorObjeto = new ObjectInputStream(new
                // FileInputStream("server.pub"));
                ObjectInputStream entradaServidorObjeto = new ObjectInputStream(cs.getInputStream());
                PublicKey llavePublicaServidor = (PublicKey) entradaServidorObjeto.readObject();

                // Descifrar msg
                rsaCipher.init(Cipher.DECRYPT_MODE, llavePublicaServidor);
                byte[] mensajeDescifrado = rsaCipher.doFinal(msgCifrado);
                String mensajeDescifrado2 = new String(mensajeDescifrado, "UTF8");

                // Verificar que el mensaje descifrado es correcto
                // En caso de que no sea el valor esperado se finaliza la conexion
                if (!mensajeDescifrado2.equals(numeroAleatorio)) {
                    cs.close();
                }
                // System.out.println("El reto recibido es correcto");

                // Generar llave simetrica
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                SecureRandom secureRandom = new SecureRandom();
                int keyBitSize = 256;
                keyGenerator.init(keyBitSize, secureRandom);
                SecretKey llaveSimetrica = keyGenerator.generateKey();

                // Cifrar llave simetrica
                // Revisar no se usa utf8
                rsaCipher.init(Cipher.ENCRYPT_MODE, llavePublicaServidor);
                byte[] llaveSimetricaCifrada = rsaCipher.doFinal(llaveSimetrica.getEncoded());

                // Enviar llave Simetrica cifrada
                salidaServidorObjeto = new ObjectOutputStream(cs.getOutputStream());
                salidaServidorObjeto.writeObject(llaveSimetricaCifrada);
                salidaServidorObjeto.flush();

                // Recibir ACK de llave simetrica
                String msg2 = entradaServidor.readUTF();
                // System.out.println("Se recibio el mensaje "+msg2+ " correspondiente a la
                // llave simetrica");

                // Cifrar nombre del cliente con publica del servidor
                rsaCipher.init(Cipher.ENCRYPT_MODE, llavePublicaServidor);
                byte[] nombreCifrado = rsaCipher.doFinal(nombre.getBytes("UTF8"));

                // Enviar nombre del cliente
                salidaServidor.writeInt(nombreCifrado.length);
                salidaServidor.write(nombreCifrado);

                // Recibir ACK como confirmacion de que el nombre del cliente existe
                // Si el mensaje es diferente a ACK es porque no existe y se termina
                // comunicacion
                String msgCN = entradaServidor.readUTF();
                // System.out.println(msgCN);
                if (!msgCN.equals("ACK")) {
                    cs.close();
                }

                // Crear iv
                IvParameterSpec ivv = crearIv();

                // Cifrar id del paquete con llave simetrica
                rsaCiphera.init(Cipher.ENCRYPT_MODE, llaveSimetrica, ivv);
                byte[] idPaqueteCifrado = rsaCiphera.doFinal(idPaquete.getBytes());
                String idCifradaSimetrica = Base64.getEncoder().encodeToString(idPaqueteCifrado);

                // Enviar Iv
                salidaServidor.writeInt((ivv.getIV()).length);
                salidaServidor.write(ivv.getIV());

                // Enviar id del paquete
                // salidaServidor.writeInt(idPaqueteCifrado.length);
                // salidaServidor.write(idPaqueteCifrado);
                salidaServidor.writeUTF(idCifradaSimetrica);

                // Recibir estado de paquete
                String estadoPaque = entradaServidor.readUTF();

                // Desencriptar estado paquete
                rsaCiphera.init(Cipher.DECRYPT_MODE, llaveSimetrica, ivv);
                byte[] estadoPaqueteDescifrado = rsaCiphera.doFinal(Base64.getDecoder().decode(estadoPaque));
                String estadoPaqueteDescifrado2 = new String(estadoPaqueteDescifrado);
                // System.out.println("El estado del paquete solicitado es:
                // "+estadoPaqueteDescifrado2);

                // Enviar ACK del estado del paquete
                salidaServidor.writeUTF("ACK");

                // Calcular hmac y digest

                String hmacCalculado = Hmac(llaveSimetrica, digest(nombre + idPaquete));

                // Recibir Hmac
                // int length4 = entradaServidor.readInt();

                // if (length4>0) {
                // arregloMacD = new byte[length4];
                // entradaServidor.readFully(arregloMacD, 0, length4);
                // }
                String arregloMacD = entradaServidor.readUTF();

                if (hmacCalculado.equals(arregloMacD)) {
                    // System.out.println("Hmac calculado: "+hmacCalculado);
                    // System.out.println("Hmac recibido: "+arregloMacD);
                    salidaServidor.writeUTF("TERMINAR");
                    System.out.println("Nombre cliente: " + nombre + "\nId del paquete: " + idPaquete
                            + "\nEl estado del paquete solicitado es: " + estadoPaqueteDescifrado2 + "\n");
                }

                // Finalizar conexion
                cs.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Generar reto
    private String numAleatorio() {

        // Random rand = new Random();
        // Formula utilizada: Math.random() * (max-min) + min;
        int numero = (int) (Math.random() * (100000000 - 10000000) + 10000000);

        int numero2 = (int) (Math.random() * (100000000 - 10000000) + 10000000);

        int numero3 = (int) (Math.random() * (100000000 - 10000000) + 10000000);

        String numeroCompleto = Integer.toString(numero) + Integer.toString(numero2) + Integer.toString(numero3);
        return numeroCompleto;
    }

    public static IvParameterSpec crearIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
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
