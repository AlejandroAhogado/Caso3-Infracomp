import java.io.IOException;
import java.util.Scanner;


public class Main {

	public static void main(String[] args) throws IOException,  InterruptedException
	{	
		Scanner sc = new Scanner(System.in);
		String tipoEjecutar = " ";
		
		while(!tipoEjecutar.equals("servidor") && !tipoEjecutar.equals("cliente"))
		{
			System.out.println("Ingrese el tipo de servicio que desea ejecutar (cliente o servidor)");
			tipoEjecutar = sc.nextLine();
		}
		
		System.out.println("Ingrese cuantos clientes desea");
		int numeroClientes = sc.nextInt();
		
		if(tipoEjecutar.equals("servidor"))
		{
			 Servidor servidor = new Servidor(numeroClientes); 

		     System.out.println("\nIniciando servidor\n");
		     servidor.startServer(); 
		}
		else
		{	
			for(int i=0; i<numeroClientes ; i++)
			{
				Cliente cliente = new Cliente(i);
		        System.out.println("\nIniciando cliente numero "+i+"\n");
		        cliente.startClient(); 
			}
			
		}
		sc.close();
	}


}
