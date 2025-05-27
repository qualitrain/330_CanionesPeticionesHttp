package mx.com.qtx.canpet.multiThread;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GestorErrores {
	private static Map<Long, List<Exception> > erroresXhilo;
	private static Map<String,Integer> erroresXtipo;
	private static Map<String,Integer> msgsErrorXtipo;
	
	static {
		erroresXhilo = new ConcurrentHashMap<>();
		erroresXtipo = new ConcurrentHashMap<>();
		msgsErrorXtipo = new ConcurrentHashMap<>();
	}
	
	private static AtomicInteger counter = new AtomicInteger(0);
	
	public static void agregarError(Exception ex, long idHilo) {
		counter.incrementAndGet();
		synchronized(erroresXhilo){			
			List<Exception> listaErrores = erroresXhilo.getOrDefault(idHilo, new Vector<Exception>());
			listaErrores.add(ex);
		    erroresXhilo.put(idHilo, listaErrores);
		}
		synchronized(erroresXtipo) {			
			Integer nIncidenciasXtipo = erroresXtipo.getOrDefault(ex.getClass().getName(),0);
			nIncidenciasXtipo++;
			erroresXtipo.put(ex.getClass().getName(), nIncidenciasXtipo);
		}
		synchronized(msgsErrorXtipo) {
			String infoMsg = getInfoMsg(ex);
			Integer nIncidenciasMsgError = msgsErrorXtipo.getOrDefault(infoMsg,0);
			nIncidenciasMsgError++;
			msgsErrorXtipo.put(infoMsg, nIncidenciasMsgError);
		}
	}
	
	private static String getInfoMsg(Exception ex) {
		String mensaje = ex.getMessage();
		if(mensaje.length() > 40) {
			mensaje = mensaje.substring(0, 40);
		}
		String msg = ex.getClass().getSimpleName() + "(";
		msg += mensaje + ")";
		if(ex.getCause()!=null) {
			msg += "->" + ex.getCause().getClass().getName() + ":"
					+ ex.getCause().getMessage().substring(0,30);
			
		}
		return msg;
	}
	
	public static void mostrarCantErroresXhilo() {
		if(erroresXhilo.isEmpty()) {
			System.out.println("No hubo erroresXhilo");
			return;
		}
		System.out.println("Errores:");
		TreeSet<Long> setHilos = new TreeSet<>();
		setHilos.addAll(erroresXhilo.keySet());
		for(Long idHilo : setHilos) {
			mostrarErroresPorHilo(erroresXhilo.get(idHilo), idHilo);
		}
	}
	
	private static void mostrarErroresPorHilo(List<Exception> listErr, Long idHilo) {
		Map<String,Integer> mapErrHiloXtipo = new TreeMap<>();
		for(Exception exI : listErr) {
			int nErrs = mapErrHiloXtipo.getOrDefault(exI.getClass().getSimpleName(),0);
			nErrs++;
			mapErrHiloXtipo.put(exI.getClass().getSimpleName(), nErrs);
		}
		System.out.printf("hilo:%5d Errores: [", idHilo);
		for(String tipoErrI:mapErrHiloXtipo.keySet()) {
			System.out.print(String.format("%-20s", tipoErrI) + ":");
			System.out.print(String.format("%4d",mapErrHiloXtipo.get(tipoErrI)) + ", ");			
		}
		System.out.println("tot:" + listErr.size() + "]");
	}

	public static void imprimirCantErroresXhilo() {
		String nomArchivo = getNomArchivoErroresXhilo();
		try (PrintWriter pw = new PrintWriter(new FileWriter(nomArchivo))){
			if(erroresXhilo.isEmpty()) {
				pw.println("No hubo erroresXhilo");
				return;
			}
			pw.println("Errores:");
			TreeSet<Long> setHilos = new TreeSet<>();
			setHilos.addAll(erroresXhilo.keySet());
			for(Long idHilo : setHilos) {
				imprimirErroresPorHilo(pw, erroresXhilo.get(idHilo), idHilo);
			}			
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private static String getNomArchivoErroresXhilo() {
		String rutaTemporales = System.getenv("TEMP");
		
		LocalDateTime ahora = LocalDateTime.now();
	    String nomArchivo = rutaTemporales + "\\" 
	    							+ CanionMultiHilo.class.getSimpleName() + "_"
		                            + GestorErrores.class.getSimpleName() + "_" 
		                            + "ErrsXhilo" + "_" 
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

	private static void imprimirErroresPorHilo(PrintWriter pw, List<Exception> listErr, Long idHilo) {
		Map<String,Integer> mapErrHiloXtipo = new TreeMap<>();
		for(Exception exI : listErr) {
			int nErrs = mapErrHiloXtipo.getOrDefault(exI.getClass().getSimpleName(),0);
			nErrs++;
			mapErrHiloXtipo.put(exI.getClass().getSimpleName(), nErrs);
		}
		pw.printf("hilo:%5d Errores: [", idHilo);
		for(String tipoErrI:mapErrHiloXtipo.keySet()) {
			pw.print(String.format("%-20s", tipoErrI) + ":");
			pw.print(String.format("%4d",mapErrHiloXtipo.get(tipoErrI)) + ", ");			
		}
		pw.println("tot:" + listErr.size() + "]");
	}

	public static void mostrarCifrasControlErrores() {
		long totPeticiones = CanionMultiHilo.NUM_HILOS * CanionMultiHilo.NUM_PETICIONES_X_HILO;
		System.out.println("====================================================================");
		System.out.println("Total de peticiones:" + totPeticiones);
		System.out.println("Total de peticiones erroneas:" + counter.get() 
		                  + String.format(" [%5.2f%%]", (counter.get()/(double)totPeticiones) * 100));
		System.out.println("Total Hilos:" + CanionMultiHilo.NUM_HILOS);
		if(erroresXhilo.isEmpty()) {
			System.out.println("No hubo erroresXhilo");
			return;
		}
		System.out.println("Hilos con errores:" + erroresXhilo.size()
							+ String.format(" [%5.2f%%]", 
									 (erroresXhilo.size()/(double)CanionMultiHilo.NUM_HILOS) * 100));
		System.out.println("Peticiones por Hilo:" + CanionMultiHilo.NUM_PETICIONES_X_HILO);
		
		double promErroresXhilo = calcularPromErroresXhilo();
		System.out.println("Promedio de errores por hilo: " 
		                    + String.format("%4.2f", promErroresXhilo));

		System.out.println("\n=== Excepciones y ocurrencias: ===\n");
		for(String ex : erroresXtipo.keySet()) {
			System.out.printf("%-27s: %5d [%5.2f%%]\n", ex, erroresXtipo.get(ex), 
					(erroresXtipo.get(ex)/(double)totPeticiones) * 100);
		}
		
		System.out.println();
		for(String msgErrI: msgsErrorXtipo.keySet()) {
			System.out.printf("%-59s: %5d\n", msgErrI, msgsErrorXtipo.get(msgErrI));
		}
		
		System.out.println("====================================================================\n");
	}
	public static void imprimirCifrasControlErrores() {
		
		String nomArchivo = getNomArchivoCtrlErrores();
		try (PrintWriter pw = new PrintWriter(new FileWriter(nomArchivo))){
			long totPeticiones = CanionMultiHilo.NUM_HILOS * CanionMultiHilo.NUM_PETICIONES_X_HILO;
			pw.println("====================================================================");
			pw.println("Total de peticiones:" + totPeticiones);
			pw.println("Total de peticiones erroneas:" + counter.get() 
			                  + String.format(" [%5.2f%%]", (counter.get()/(double)totPeticiones) * 100));
			pw.println("Total Hilos:" + CanionMultiHilo.NUM_HILOS);
			if(erroresXhilo.isEmpty()) {
				pw.println("No hubo erroresXhilo");
				return;
			}
			pw.println("Hilos con errores:" + erroresXhilo.size()
								+ String.format(" [%5.2f%%]", 
										 (erroresXhilo.size()/(double)CanionMultiHilo.NUM_HILOS) * 100));
			pw.println("Peticiones por Hilo:" + CanionMultiHilo.NUM_PETICIONES_X_HILO);
			
			double promErroresXhilo = calcularPromErroresXhilo();
			pw.println("Promedio de errores por hilo: " 
			                    + String.format("%4.2f", promErroresXhilo));

			pw.println("\n=== Excepciones y ocurrencias: ===\n");
			for(String ex : erroresXtipo.keySet()) {
				pw.printf("%-27s: %5d [%5.2f%%]\n", ex, erroresXtipo.get(ex), 
						(erroresXtipo.get(ex)/(double)totPeticiones) * 100);
			}
			
			pw.println();
			for(String msgErrI: msgsErrorXtipo.keySet()) {
				pw.printf("%-59s: %5d\n", msgErrI, msgsErrorXtipo.get(msgErrI));
			}
			
			pw.println("====================================================================\n");
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getNomArchivoCtrlErrores() {
		String rutaTemporales = System.getenv("TEMP");
		
		LocalDateTime ahora = LocalDateTime.now();
	    String nomArchivo = rutaTemporales + "\\" 
	    							+ CanionMultiHilo.class.getSimpleName() + "_"
		                            + GestorErrores.class.getSimpleName() + "_" 
		                            + "CtrlErrores" + "_" 
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

	private static double calcularPromErroresXhilo() {
		int totalErrores = 0;
		for(Long idHilo : erroresXhilo.keySet()) {
			int nErrores = erroresXhilo.get(idHilo).size();
			totalErrores += nErrores;
		}
		double promErroresXhilo = totalErrores / (double) erroresXhilo.size();
		return promErroresXhilo;
	}
	
}
