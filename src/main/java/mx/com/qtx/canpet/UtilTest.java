package mx.com.qtx.canpet;

public class UtilTest {
	public static String verbosHttp[] = {"GET","POST","PUT","DELETE", "GET","GET","GET"};
	public static String mediaTypes[] = {"application/json","text/plain","text/html"};
	public static String recursos[] = {"/img/gato.gif","/img/osoPolar.jpg",
			"/img/desierto.jpg","/img/snoopy.JPG","/Test","/Test","/Test"};
	public static String getVerboRandom() {
		int n = (int)(Math.random() * 10033);
		return verbosHttp[n%verbosHttp.length];
	}
	public static String getMediaTypeRandom() {
		int n = (int)(Math.random() * 10000);
		return mediaTypes[n%mediaTypes.length];
	}
	public static String getRecursoRandom() {
		int n = (int)(Math.random() * 77773);
		return recursos[n%recursos.length];
	}

}
