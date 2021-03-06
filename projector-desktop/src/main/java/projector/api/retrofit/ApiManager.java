package projector.api.retrofit;

import com.bence.projector.common.serializer.DateDeserializer;
import com.bence.projector.common.serializer.DateSerializer;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ApiManager {

    public static Retrofit getClient() {
        String BASE_URL = "http://localhost:8080";
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor).build();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeAdapter(Date.class,
                new DateSerializer());
        GsonConverterFactory factory = GsonConverterFactory.create(gsonBuilder.create());
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(factory)
                .client(okHttpClient)
                .build();
    }
}
