package it.antonio.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonUtils {

	private static Gson gson;
	
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		gson = gsonBuilder.create();
	}
	
     public static String toJson( final Object src) {
        return gson.toJson(src);
    }

    public static <T> T fromJson( final String json, final Class<T> classOfT) {
	       return gson.fromJson(json, classOfT);
    }


}