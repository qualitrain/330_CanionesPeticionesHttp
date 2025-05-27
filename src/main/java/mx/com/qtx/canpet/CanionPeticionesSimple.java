package mx.com.qtx.canpet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class CanionPeticionesSimple {
	/* Envia una sola peticion a la vez. Es uni-hilo */
	
	public static final String PROTOCOLO = "http";
	public static final String IP_HOST = "localhost";
	public static final String PUERTO = "8080";
	public static final String RECURSO = "330_TestDenegServicio/Test?App=CanionPeticionesSimple";
	public static final boolean MOSTRAR_RESPUESTA = false;
	public static final int NUM_PETICIONES_X_ATAQUE = 100;
	
	public static void main(String[] args) {
		String cadBaseUrl = PROTOCOLO + "://" + IP_HOST + ":" + PUERTO + "/" + RECURSO;
		
		for(long i=0; i<NUM_PETICIONES_X_ATAQUE; i++){
			String cadUrl = cadBaseUrl +"&nPeticion=" + (i+1);
			URL url = null;
			try {
				url = new URL(cadUrl);
			} 
			catch (MalformedURLException e) {
				System.out.println("MalformedURLException:" + cadUrl + " [" + e.getMessage() + "]");
			}
			
//			System.out.println("URL solicitada: " + url.toString());
			generarPeticion(url);
		}

	}
	
	public static void generarPeticion(URL url){
		URLConnection conexionHttp = null;
		try {
			conexionHttp = url.openConnection();
			conexionHttp.setRequestProperty("User-Agent", "Mozilla/6.0 (Windows NT 6.1; WOW64; rv:49.0) Gecko/20100101 "
					+ "Firefox/49.0 Accept: */*");
			conexionHttp.connect();
			InputStream urlStream = conexionHttp.getInputStream();
			try (BufferedReader brUrlStream = 
					 new BufferedReader(
					     new InputStreamReader(urlStream))){
				
				 if(!MOSTRAR_RESPUESTA)
					 return;
				 
				 while(true){
					 String linea = brUrlStream.readLine();
					 if(linea==null){
							 System.out.println("Fin del flujo");
						 break;
					 }
						 System.out.println(linea);					 
				 }
				 
			 }
		 }catch(Exception ex){
			 System.out.println("Exception:"+ex.getClass().getSimpleName());
			 ex.printStackTrace();
		 }
	}
}
