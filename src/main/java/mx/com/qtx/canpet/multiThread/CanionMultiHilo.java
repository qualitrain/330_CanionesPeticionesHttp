package mx.com.qtx.canpet.multiThread;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Map;

public class CanionMultiHilo {
	public static final String PROTOCOLO = "http";
	public static final String IP = "localhost";
	public static final String PUERTO = "8080";
	public static final String CONTEXTO = "330_TestDenegServicio";
	
	public static final int NUM_HILOS = 300;
	public static final int NUM_PETICIONES_X_HILO = 40;
	public static final int PAUSA_ENTRE_PETICIONES_MILIS = 0;
	
	public static final int TIMEOUT_MILIS = 1500; // 0 = Infinito
	
	public static final boolean MOSTRAR_RESPUESTA = false;
	public static final boolean MOSTRAR_ERRORES_X_HILO = true;
	public static final boolean MOSTRAR_ACIERTOS_X_HILO = true;
	public static final boolean MOSTRAR_EN_ARCHIVO = true;

	public static void main(String[] args) {
		DisparadorPeticionesI[] arrDisparadores = new DisparadorPeticionesI[NUM_HILOS];
		String cadUrl = PROTOCOLO + "://" + IP + ":" + PUERTO + "/" + CONTEXTO;
		
		crearHilosDisparadoresDePeticiones(arrDisparadores, cadUrl);		
		ejecutarHilosDisparadores(arrDisparadores);		
		esperarFinalizacionHilos(arrDisparadores);
		
		if(MOSTRAR_EN_ARCHIVO){
			GestorErrores.imprimirCifrasControlErrores();
			if(MOSTRAR_ERRORES_X_HILO)
				GestorErrores.imprimirCantErroresXhilo();
			if(MOSTRAR_ACIERTOS_X_HILO)
				imprimirPeticionesExitosasXhilo();
		}
		else {
			GestorErrores.mostrarCifrasControlErrores();
			if(MOSTRAR_ERRORES_X_HILO)
				GestorErrores.mostrarCantErroresXhilo();
			if(MOSTRAR_ACIERTOS_X_HILO)
				mostrarPeticionesExitosasXhilo();
		}
	}


	private static void ejecutarHilosDisparadores(DisparadorPeticionesI[] arrDisparadores) {
		for(int i=0; i<NUM_HILOS; i++)
			arrDisparadores[i].iniciarAtaque();
	}


	private static void crearHilosDisparadoresDePeticiones(DisparadorPeticionesI[] arrDisparadores, String cadUrl) {
		for(int i=0; i<NUM_HILOS; i++){
			arrDisparadores[i] = new DisparadorPeticionesI(cadUrl, NUM_PETICIONES_X_HILO,
											MOSTRAR_RESPUESTA, PAUSA_ENTRE_PETICIONES_MILIS, 
											TIMEOUT_MILIS);
		}
	}


	private static void mostrarPeticionesExitosasXhilo() {
		System.out.println("--- Peticiones exitosas ----");
		Map<Long, Integer> exitosXhilo = DisparadorPeticionesI.getOcurrenciasExitosasXhilo();
		int total = 0;
		for(Long idHiloI:exitosXhilo.keySet()) {
			int nPeticionesOk = exitosXhilo.get(idHiloI);
			total+=nPeticionesOk;
			System.out.println("Hilo " + idHiloI + " : " + nPeticionesOk + " de " + NUM_PETICIONES_X_HILO);
		}
		System.out.println("Total de peticiones exitosas (status=200) : " 
		                          + total + " de " + NUM_PETICIONES_X_HILO * NUM_HILOS);
	}

	private static void imprimirPeticionesExitosasXhilo() {
		String nomArchivo = getNomArchivoReqsExitosasXhilo();
		
		try (PrintWriter pw = new PrintWriter(new FileWriter(nomArchivo))){
			pw.println("--- Peticiones exitosas ----");
			Map<Long, Integer> exitosXhilo = DisparadorPeticionesI.getOcurrenciasExitosasXhilo();
			int total = 0;
			for(Long idHiloI:exitosXhilo.keySet()) {
				int nPeticionesOk = exitosXhilo.get(idHiloI);
				total+=nPeticionesOk;
				pw.println("Hilo " + idHiloI + " : " + nPeticionesOk + " de " + NUM_PETICIONES_X_HILO);
			}
			pw.println("Total de peticiones exitosas (status=200) : " 
			                          + total + " de " + NUM_PETICIONES_X_HILO * NUM_HILOS);			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getNomArchivoReqsExitosasXhilo() {
		String rutaTemporales = System.getenv("TEMP");
		
		LocalDateTime ahora = LocalDateTime.now();
	    String nomArchivo = rutaTemporales + "\\" 
	    							+ CanionMultiHilo.class.getSimpleName() + "_"
		                            + "ReqsOkXhilo" + "_" 
									+ ahora.getYear() 
									+ ahora.getMonthValue()
									+ ahora.getDayOfMonth() 
									+ ahora.getHour()
									+ ahora.getMinute() 
									+ ahora.getSecond()
									+ ".txt";
	    System.out.println("nomArchivo:" + nomArchivo);
	    return nomArchivo;
	}


	private static void esperarFinalizacionHilos(DisparadorPeticionesI[] arrDisparadores) {
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
