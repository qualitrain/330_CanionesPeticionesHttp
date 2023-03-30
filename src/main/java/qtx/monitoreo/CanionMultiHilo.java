package qtx.monitoreo;

import java.util.Map;

public class CanionMultiHilo {
	public static final String PROTOCOLO = "http";
	public static final String IP = "localhost";
	public static final String PUERTO = "8080";
	public static final String CONTEXTO = "330_TestDenegServicio";
	
	public static final int NUM_HILOS = 4000;
	public static final int NUM_PETICIONES_X_HILO = 30;
	public static final int PAUSA_ENTRE_PETICIONES_MILIS = 0;
	
	public static final int TIMEOUT_MILIS = 1000; // 0 = Infinito
	
	public static final boolean MOSTRAR_RESPUESTA = false;
	public static final boolean MOSTRAR_ERRORES_X_HILO = true;
	public static final boolean MOSTRAR_ACIERTOS_X_HILO = true;

	public static void main(String[] args) {
		DisparadorI[] arrDisparadores = new DisparadorI[NUM_HILOS];
		String cadUrl = PROTOCOLO + "://" + IP + ":" + PUERTO + "/" + CONTEXTO;
		
		crearHilosDisparadoresDePeticiones(arrDisparadores, cadUrl);		
		ejecutarHilosDisparadores(arrDisparadores);		
		esperarFinalizacionHilos(arrDisparadores);
		
		Errores.mostrarCifrasControlErrores();
		if(MOSTRAR_ERRORES_X_HILO)
			Errores.mostrarCantErroresXhilo();
		if(MOSTRAR_ACIERTOS_X_HILO)
			mostrarPeticionesExitosasXhilo();
	}


	private static void ejecutarHilosDisparadores(DisparadorI[] arrDisparadores) {
		for(int i=0; i<NUM_HILOS; i++)
			arrDisparadores[i].iniciarAtaque();
	}


	private static void crearHilosDisparadoresDePeticiones(DisparadorI[] arrDisparadores, String cadUrl) {
		for(int i=0; i<NUM_HILOS; i++){
			arrDisparadores[i] = new DisparadorI(cadUrl, NUM_PETICIONES_X_HILO,
											MOSTRAR_RESPUESTA, PAUSA_ENTRE_PETICIONES_MILIS, 
											TIMEOUT_MILIS);
		}
	}


	private static void mostrarPeticionesExitosasXhilo() {
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


	private static void esperarFinalizacionHilos(DisparadorI[] arrDisparadores) {
		// El siguiente for espera a que todos los hilos finalicen, para que la bitacora de errores se despliegue
		try {
			for(int i=0; i<NUM_HILOS; i++)
				arrDisparadores[i].esperarFin();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
