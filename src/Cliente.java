import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


public class Cliente extends Main
{
	
	private static final int PUERTO = 3400;
	private final String HOST = "192.168.226.1"; 
	private Socket cs; 
	private DataOutputStream salidaServidor; 
	private DataInputStream entradaServidor;
	private String mensaje;

	private int id;
	
    public Cliente(int idd) throws IOException{
    	this.id = idd;
    	} 
    
    public void startClient() //Metodo para iniciar el cliente
    {
        try
        {
        	cs = new Socket(HOST, PUERTO); //Socket para el cliente en localhost en puerto 3400
            //Flujo de datos hacia el servidor
            salidaServidor = new DataOutputStream(cs.getOutputStream());
            entradaServidor = new DataInputStream(cs.getInputStream());
       

            //Se enviar�n dos mensajes
            for (int i = 0; i < 2; i++)
            {
                //Se escribe en el servidor usando su flujo de datos
                salidaServidor.writeUTF("Este es el mensaje numero " + (i+1) + "\n");
            }
            
            
            System.out.println(entradaServidor.readUTF());
                                  
            cs.close();//Fin de la conexion

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
