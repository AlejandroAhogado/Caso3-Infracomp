import java.io.IOException;
import java.util.Scanner;

import javax.sql.rowset.CachedRowSet;


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
		
		System.out.println("Ingrese cuantas consultas desea realizar");
		int numeroClientes = sc.nextInt();

		System.out.println("Ingrese el numero del escenario que desea ejecutar \n 1.Iterativo \n 2.Sincronizado  ");
		int escenario = sc.nextInt();
		
			switch(escenario)
			{
				case 1:
		
						if(tipoEjecutar.equals("servidor"))
						{
							Servidor servidor = new Servidor(numeroClientes); 

							System.out.println("\nIniciando servidor\n");
							servidor.startServer(); 
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

								System.out.println("\nIniciando consulta numero "+i);
								Cliente cliente = new Cliente(i,nombreCliente, idPaquete);
								cliente.startClient(); 
								
							}
							sa.close();
						}
						break;

				case 2:
				//Usar para caso sincronizado
			}
						
	sc.close();
	}


}
