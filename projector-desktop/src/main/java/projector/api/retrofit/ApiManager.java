package projector.api.retrofit;

import com.bence.projector.common.serializer.DateDeserializer;
import com.bence.projector.common.serializer.DateSerializer;
import com.google.gson.GsonBuilder;
import okhttp3.Cookie;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static projector.Credentials.BASE_URL;

public class ApiManager {

    private static ApiManager instance;
    private List<Cookie> cookies = null;

    public ApiManager() {

    }

    public static ApiManager getInstance() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

    public static Retrofit getClient() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        HeaderInterceptor headerInterceptor = new HeaderInterceptor();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(headerInterceptor)
                .followRedirects(false)
                .build();
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

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    public static class HeaderInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request.Builder builder = chain.request()
                    .newBuilder();
            setCookieHeader(builder);
            Request request = builder.build();
            return chain.proceed(request);
        }

        private void setCookieHeader(Request.Builder builder) {
            List<Cookie> cookies = ApiManager.getInstance().getCookies();
            if (cookies != null) {
                StringBuilder cookieString = null;
                for (Cookie cookie : cookies) {
                    String s = cookie.name() + "=" + cookie.value();
                    if (cookieString == null) {
                        cookieString = new StringBuilder(s);
                    } else {
                        cookieString.append("; ").append(s);
                    }
                }
                if (cookieString != null) {
                    builder.addHeader("Cookie", cookieString.toString());
                }
            }
        }
    }
}
