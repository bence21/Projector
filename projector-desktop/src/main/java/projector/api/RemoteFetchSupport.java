package projector.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.utils.NetworkConnectivityUtil;
import retrofit2.Call;
import retrofit2.Response;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.function.Function;

public final class RemoteFetchSupport {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteFetchSupport.class);

    private RemoteFetchSupport() {
    }

    public static <T> RemoteFetchResult<T> execute(Call<T> call) {
        return execute(call, body -> body);
    }

    public static <T, R> RemoteFetchResult<R> execute(Call<T> call, Function<T, R> mapBody) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                R mapped = mapBody.apply(response.body());
                if (mapped != null) {
                    return RemoteFetchResult.success(mapped);
                }
                return RemoteFetchResult.failure(RemoteFetchFailureKind.SERVER_ERROR);
            }
            return RemoteFetchResult.failure(RemoteFetchFailureKind.SERVER_ERROR);
        } catch (ConnectException | UnknownHostException | SocketTimeoutException e) {
            return RemoteFetchResult.failure(classifyConnectivityFailure());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return RemoteFetchResult.failure(classifyConnectivityFailure());
        }
    }

    public static RemoteFetchFailureKind classifyConnectivityFailure() {
        if (NetworkConnectivityUtil.isInternetReachable()) {
            return RemoteFetchFailureKind.SERVER_UNREACHABLE;
        }
        return RemoteFetchFailureKind.NO_INTERNET;
    }
}
