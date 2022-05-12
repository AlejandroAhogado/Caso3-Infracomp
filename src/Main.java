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
				ClienteConcurrente cliente = new ClienteConcurrente(i,nombreCliente, idPaquete, numeroClientes);
				if (numeroClientes <= 4) {
					Thread cliente1 = new Thread(cliente);
					cliente1.start();
					Thread cliente2 = new Thread(cliente);
					cliente2.start();
					Thread cliente3 = new Thread(cliente);
					cliente3.start();
					Thread cliente4 = new Thread(cliente);
					cliente4.start();
				}
		
				if (numeroClientes <= 16) {
					Thread cliente5 = new Thread(cliente);
					cliente5.start();
					Thread cliente6 = new Thread(cliente);
					cliente6.start();
					Thread cliente7 = new Thread(cliente);
					cliente7.start();
					Thread cliente8 = new Thread(cliente);
					cliente8.start();
					Thread cliente9 = new Thread(cliente);
					cliente9.start();
					Thread cliente10 = new Thread(cliente);
					cliente10.start();
					Thread cliente11 = new Thread(cliente);
					cliente11.start();
					Thread cliente12 = new Thread(cliente);
					cliente12.start();
					Thread cliente13 = new Thread(cliente);
					cliente13.start();
					Thread cliente14 = new Thread(cliente);
					cliente14.start();
					Thread cliente15 = new Thread(cliente);
					cliente15.start();
					Thread cliente16 = new Thread(cliente);
					cliente16.start();
				}
				if (numeroClientes <= 32) {
					Thread cliente17 = new Thread(cliente);
					cliente17.start();
					Thread cliente18 = new Thread(cliente);
					cliente18.start();
					Thread cliente19 = new Thread(cliente);
					cliente19.start();
					Thread cliente20 = new Thread(cliente);
					cliente20.start();
					Thread cliente21 = new Thread(cliente);
					cliente21.start();
					Thread cliente22 = new Thread(cliente);
					cliente22.start();
					Thread cliente23 = new Thread(cliente);
					cliente23.start();
					Thread cliente24 = new Thread(cliente);
					cliente24.start();
					Thread cliente25 = new Thread(cliente);
					cliente25.start();
					Thread cliente26 = new Thread(cliente);
					cliente26.start();
					Thread cliente27 = new Thread(cliente);
					cliente27.start();
					Thread cliente28 = new Thread(cliente);
					cliente28.start();
					Thread cliente29 = new Thread(cliente);
					cliente29.start();
					Thread cliente30 = new Thread(cliente);
					cliente30.start();
					Thread cliente31 = new Thread(cliente);
					cliente31.start();
					Thread cliente32 = new Thread(cliente);
					cliente32.start();
				}
				
			}
			sa.close();
		}
		
		sc.close();
	}


}
