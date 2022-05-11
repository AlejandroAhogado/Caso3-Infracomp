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
			ServidorConcurrente servidor = new ServidorConcurrente(numeroClientes); 

		    System.out.println("\nIniciando servidor\n");
		}
		else
		{		
			Scanner sa = new Scanner(System.in);
			for(int i=0; i<numeroClientes ; i++)
			{
				//id, nombre, idPaquete
				System.out.println("\nEsperando...\n");

				System.out.println("Ingrese el nombre del cliente:");
				String nombreCliente = sa.nextLine();

				System.out.println("\nIngrese el id del paquete");
				String idPaquete = sa.nextLine();

				System.out.println("\nIniciando cliente numero "+i);
				ClienteConcurrente cliente = new ClienteConcurrente(i,nombreCliente, idPaquete);
		        cliente.run(); 
				
			}
			sa.close();
		}
		
		sc.close();
	}


}
