package mx.com.qtx.canpet.multiThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mx.com.qtx.canpet.test.UtilTest;

/**
 *  Crea un hilo en el que ejecuta múltiples envíos de peticiones Http hacia la url configurada
 */
public class DisparadorPeticionesI implements Runnable {
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

	public DisparadorPeticionesI(String url, int nPeticiones, boolean mostrarRespuesta, int pausa, int timeout) {
		super();
		this.url = url;
		this.nPeticiones = nPeticiones;
		this.mostrarRespuesta = mostrarRespuesta;
		this.hiloI = new Thread(this);
		this.pausaMilis = pausa;
		this.timeout = timeout;
	}

	public void iniciarAtaque(){
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
				url = new URL(this.url + UtilTest.getRecursoRandom() 
				+ "?App=" + CanionMultiHilo.class.getSimpleName()
				+ "&hiloEnCte=" + Thread.currentThread().getId() 
				+ "&nPeticion=" + (i+1));
			} 
			catch (MalformedURLException e) {
				System.out.println("MalformedURLException");
				continue;
			}
			 this.generarYprocesarPeticion(url);
			 this.hacerPausa();
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
	private void generarYprocesarPeticion(URL url) {
		HttpURLConnection conexionHttp = null;
		try {
			conexionHttp = configurarConexionYpeticionHttp(url);
			conexionHttp.connect(); // envía la petición
			
			mostrarRespuesta(conexionHttp);
			
			if(conexionHttp.getResponseCode() == 200) {
				synchronized(DisparadorPeticionesI.ocurrenciasExitosasXhilo) {					
					int nOcurrencias = DisparadorPeticionesI.ocurrenciasExitosasXhilo.getOrDefault(Thread.currentThread().getId(),0);
					nOcurrencias++;
					DisparadorPeticionesI.ocurrenciasExitosasXhilo.put(Thread.currentThread().getId(), nOcurrencias);
				}
			}
			 
		 }
		catch(Exception ex){
			GestorErrores.agregarError(ex, Thread.currentThread().getId());
		 }
		
	}

	private HttpURLConnection configurarConexionYpeticionHttp(URL apuntadorRecurso) throws IOException, ProtocolException {
		HttpURLConnection conexionHttp;
		conexionHttp = (HttpURLConnection) apuntadorRecurso.openConnection();
		conexionHttp.setRequestProperty("User-Agent", "TestStress");
		conexionHttp.setRequestMethod(UtilTest.getVerboRandom());
		conexionHttp.setRequestProperty("Accept", UtilTest.getMediaTypeRandom());
		
		conexionHttp.setConnectTimeout(this.timeout);
		return conexionHttp;
	}

	private void mostrarRespuesta(URLConnection conexionHttp) throws IOException {
		if(this.mostrarRespuesta == false)
			return;
		
		InputStream urlStream = conexionHttp.getInputStream();
		try (BufferedReader brUrlStream = 
				 new BufferedReader(
				     new InputStreamReader(urlStream))){
			
		    System.out.println("Hilo:" + Thread.currentThread().getId() + " Contenido de Url Stream:");
			 
			while(true){
				 String linea = brUrlStream.readLine();
				 if(linea==null){
					 System.out.println("Hilo:" + Thread.currentThread().getId() + " Fin del flujo");
					 break;
				 }
				 System.out.println("Hilo:" + Thread.currentThread().getId() + " " + linea);					 
			}			
		}
		
	}
	
	public void esperarFin() throws InterruptedException {
		this.hiloI.join();
	}

}
