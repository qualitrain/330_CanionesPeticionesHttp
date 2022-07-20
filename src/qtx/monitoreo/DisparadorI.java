package qtx.monitoreo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisparadorI implements Runnable {
	private String url;
	private int nPeticiones;
	private boolean mostrarRespuesta;
	private int pausaMilis;
	private int timeout;
	
	private Thread hiloI = null;
	
	private static Map<Long, Integer> ocurrenciasExitosasXhilo = new ConcurrentHashMap<>();

	public static Map<Long, Integer> getOcurrenciasExitosasXhilo() {
		return ocurrenciasExitosasXhilo;
	}

	public DisparadorI(String url, int nPeticiones, boolean mostrarRespuesta, int pausa, int timeout) {
		super();
		this.url = url;
		this.nPeticiones = nPeticiones;
		this.mostrarRespuesta = mostrarRespuesta;
		this.hiloI = new Thread(this);
		this.pausaMilis = pausa;
		this.timeout = timeout;
	}

	public void iniciar(){
		this.hiloI.start();
	}

	@Override
	public void run() {
		 bombardearURL(this.nPeticiones);
	}


	private void bombardearURL(int nPeticiones) {
		URL url = null;
		for(long i=0; i<nPeticiones; i++){
			try {
				url = new URL(this.url + UtilTest.getRecursoRandom() + "?hiloEnCte=" + Thread.currentThread().getId() + "&i="+i);
			} 
			catch (MalformedURLException e) {
				System.out.println("MalformedURLException");
				continue;
			}
			 generarPeticion(url);
			 hacerPausa();
		}
	}


	private void hacerPausa() {
		 try {
				Thread.sleep(this.pausaMilis);
		 } 
		 catch (InterruptedException e) {
				e.printStackTrace();
		 }
		
	}
	private void generarPeticion(URL url) {
		HttpURLConnection conexion = null;
		try {
			conexion = (HttpURLConnection)url.openConnection();
			conexion.setRequestProperty("User-Agent", "TestStress");
			conexion.setRequestMethod(UtilTest.getVerboRandom());
			conexion.setRequestProperty("Accept", UtilTest.getMediaTypeRandom());
			
			conexion.setConnectTimeout(this.timeout);
			conexion.connect();
			
			mostrarRespuesta(conexion);
			
			if(conexion.getResponseCode() == 200) {
				int nOcurrencias = DisparadorI.ocurrenciasExitosasXhilo.getOrDefault(Thread.currentThread().getId(),0);
				nOcurrencias++;
				DisparadorI.ocurrenciasExitosasXhilo.put(Thread.currentThread().getId(), nOcurrencias);
			}
			 
		 }
		catch(Exception ex){
			Errores.agregarError(ex, Thread.currentThread().getId());
		 }
		
	}

	private void mostrarRespuesta(URLConnection conexion) throws IOException {
		InputStream urlStream = conexion.getInputStream();
		 BufferedReader brUrlStream = 
				 new BufferedReader(
				     new InputStreamReader(urlStream));
		 if(this.mostrarRespuesta){
			 System.out.println("Hilo:" + Thread.currentThread().getId() + " Contenido de Url Stream:");
		 }
		 
		 while(true){
			 String linea = brUrlStream.readLine();
			 if(linea==null){
				 if(this.mostrarRespuesta){
					 System.out.println("Hilo:" + Thread.currentThread().getId() + " Fin del flujo");
				 }
				 break;
			 }
			 if(this.mostrarRespuesta){
				 System.out.println("Hilo:" + Thread.currentThread().getId() + " " + linea);					 
			 }
		 }
	}
	public void esperarFin() throws InterruptedException {
		this.hiloI.join();
	}

}
