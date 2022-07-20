package qtx.monitoreo;

import java.util.Map;

public class CanionMultiHilo {
	public static final int NUM_HILOS = 200;
	public static final int NUM_PETICIONES_X_HILO = 3;
	public static final String PROTOCOLO = "http";
	public static final String IP = "localhost";
	public static final String PUERTO = "8080";
	public static final String CONTEXTO = "330_TestDenegServicio";
	public static final boolean MOSTRAR_RESPUESTA = false;
	public static final int PAUSA_ENTRE_PETICIONES_MILIS = 0;
	
	public static final int TIMEOUT_MILIS = 2000; // 0 = Infinito
	

	public static void main(String[] args) {
		DisparadorI[] disparador = new DisparadorI[NUM_HILOS];
		String cadUrl = PROTOCOLO + "://" + IP + ":" + PUERTO + "/" + CONTEXTO;
		for(int i=0; i<NUM_HILOS; i++){
			disparador[i] = new DisparadorI(cadUrl, NUM_PETICIONES_X_HILO,
											MOSTRAR_RESPUESTA, PAUSA_ENTRE_PETICIONES_MILIS, TIMEOUT_MILIS);
		}
		for(int i=0; i<NUM_HILOS; i++)
			disparador[i].iniciar();
		// El siguiente for espera a que todos los hilos finalicen, para que la bitácora de errores se despliegue
		try {
			for(int i=0; i<NUM_HILOS; i++)
				disparador[i].esperarFin();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
//		Errores.mostrarErroresPorHilo();
		Errores.mostrarDetallesErroresPorHilo();
		
		System.out.println("--- Peticiones exitosas ----");
		Map<Long, Integer> exitosXhilo = DisparadorI.getOcurrenciasExitosasXhilo();
		int total = 0;
		for(Long idHiloI:exitosXhilo.keySet()) {
			int nPeticionesOk = exitosXhilo.get(idHiloI);
			total+=nPeticionesOk;
			System.out.println("Hilo " + idHiloI + " : " + nPeticionesOk + " de " + NUM_PETICIONES_X_HILO);
		}
		System.out.println("Total de peticiones exitosas (status=200) : " 
		                          + total + " de " + NUM_PETICIONES_X_HILO * NUM_HILOS);
	}

}
