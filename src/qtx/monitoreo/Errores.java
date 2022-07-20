package qtx.monitoreo;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Errores {
	private static Map<Long, List<Exception> > erroresXhilo;
	private static Map<String,Integer> erroresXtipo;
	static {
		erroresXhilo = new ConcurrentHashMap<>();
		erroresXtipo = new ConcurrentHashMap<>();
	}
	public static void agregarError(Exception ex, long idHilo) {
		List<Exception> listaErrores = erroresXhilo.get(idHilo);
		if(listaErrores == null) {
			listaErrores = new Vector<>();
			erroresXhilo.put(idHilo, listaErrores);
		}
		listaErrores.add(ex);
		
		Integer nIncidenciasXtipo = erroresXtipo.get(ex.getClass().getName());
		if(nIncidenciasXtipo == null) {
			erroresXtipo.put(ex.getClass().getName(), 1);
			return;
		}
		int n = nIncidenciasXtipo + 1;
		erroresXtipo.put(ex.getClass().getName(), n);
	}
	public static void mostrarErroresPorHilo() {
		if(erroresXhilo.isEmpty()) {
			System.out.println("No hubo erroresXhilo");
			return;
		}
		System.out.println("Errores:");
		for(Long idHilo : erroresXhilo.keySet()) {
			int nErrores = erroresXhilo.get(idHilo).size();
			System.out.println("hilo:" + idHilo+ ", erroresXhilo:"+ nErrores);
		}
	}
	public static void mostrarDetallesErroresPorHilo() {
		if(erroresXhilo.isEmpty()) {
			System.out.println("No hubo erroresXhilo");
			return;
		}
		System.out.println("Hilos con errores:" + erroresXhilo.size());
		int totalErrores = 0;
		for(Long idHilo : erroresXhilo.keySet()) {
			int nErrores = erroresXhilo.get(idHilo).size();
			totalErrores += nErrores;
//			System.out.print("hilo:" + idHilo+ ", erroresXhilo:"+ nErrores);
//			for(Exception exI : erroresXhilo.get(idHilo)) {
//				System.out.print(" " + exI.getClass().getSimpleName()+",");
//			}
//			System.out.println();
		}
		double promErroresXhilo = totalErrores / (double) erroresXhilo.size();
		System.out.println("Promedio de errores por hilo: " + promErroresXhilo);
		for(String ex : erroresXtipo.keySet()) {
			System.out.println(ex + ": " + erroresXtipo.get(ex));
		}
		
	}
}
