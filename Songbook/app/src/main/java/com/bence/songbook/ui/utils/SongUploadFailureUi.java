package com.bence.songbook.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.SongApiBean;
import com.bence.songbook.api.SongApiBean.SongUploadFailureKind;
import com.bence.songbook.models.Song;
import com.bence.songbook.ui.activity.LoginActivity;

public final class SongUploadFailureUi {

    /**
     * Request code for {@link Activity#startActivityForResult} when opening login after an upload auth failure.
     */
    public static final int REQUEST_LOGIN_FOR_UPLOAD_RETRY = 10001;

    private SongUploadFailureUi() {
    }

    public static void showForActivity(Activity activity, SongApiBean.SongUploadResult result) {
        showForActivity(activity, result, null, null);
    }

    /**
     * @param loginRequestCode    if non-null, "Open login" uses {@link Activity#startActivityForResult} so the caller
     *                            can retry upload after {@link LoginActivity#RESULT_LOGGED_IN}.
     * @param onUserDeclinedLogin optional; run when the user closes the dialog or taps Close (not when opening login).
     */
    public static void showForActivity(Activity activity, SongApiBean.SongUploadResult result,
                                       @Nullable Integer loginRequestCode,
                                       @Nullable Runnable onUserDeclinedLogin) {
        if (result.getFailureKind() == SongApiBean.SongUploadFailureKind.SUCCESS) {
            return;
        }
        if (activity.isFinishing()) {
            showAfterLeaveActivity(activity.getApplicationContext(), result.getFailureKind(), null);
            return;
        }
        switch (result.getFailureKind()) {
            case NEEDS_SIGN_IN:
            case SESSION_NOT_REFRESHED:
                int msg = result.getFailureKind() == SongUploadFailureKind.NEEDS_SIGN_IN
                        ? R.string.upload_needs_sign_in_message
                        : R.string.upload_session_expired_message;
                final boolean[] openedLogin = {false};
                AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                        .setMessage(msg)
                        .setPositiveButton(R.string.upload_open_login, (d, w) -> {
                            openedLogin[0] = true;
                            Intent loginIntent = new Intent(activity, LoginActivity.class);
                            if (loginRequestCode != null) {
                                activity.startActivityForResult(loginIntent, loginRequestCode);
                            } else {
                                activity.startActivity(loginIntent);
                            }
                        })
                        .setNegativeButton(R.string.close, null);
                AlertDialog dialog = builder.create();
                dialog.setOnDismissListener(di -> {
                    if (!openedLogin[0] && onUserDeclinedLogin != null) {
                        onUserDeclinedLogin.run();
                    }
                });
                dialog.show();
                break;
            case NETWORK_OR_SERVER:
            default:
                Toast.makeText(activity, R.string.upload_network_or_server_message, Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * After a post-login upload retry still fails: toasts only (no login loop).
     */
    public static void showUploadFailureAfterRetry(Activity activity, SongApiBean.SongUploadResult result) {
        if (result.getFailureKind() == SongUploadFailureKind.SUCCESS) {
            return;
        }
        showBackgroundUploadFailureAfterRetry(activity, result.getFailureKind());
    }

    public static void showBackgroundUploadFailureAfterRetry(Context appContext, SongUploadFailureKind kind) {
        if (kind == SongUploadFailureKind.SUCCESS) {
            return;
        }
        if (kind == SongUploadFailureKind.NEEDS_SIGN_IN || kind == SongUploadFailureKind.SESSION_NOT_REFRESHED) {
            showBackgroundUploadAuthHint(appContext, kind);
            return;
        }
        Toast.makeText(appContext, R.string.upload_network_or_server_message, Toast.LENGTH_LONG).show();
    }

    /**
     * Toast-only hint when upload runs in the background (e.g. from MainActivity) so we do not open Login automatically.
     */
    public static void showBackgroundUploadAuthHint(Context appContext, SongUploadFailureKind kind) {
        if (kind != SongUploadFailureKind.NEEDS_SIGN_IN && kind != SongUploadFailureKind.SESSION_NOT_REFRESHED) {
            return;
        }
        int resId = kind == SongUploadFailureKind.NEEDS_SIGN_IN
                ? R.string.upload_needs_sign_in_message
                : R.string.upload_session_expired_message;
        Toast.makeText(appContext, resId, Toast.LENGTH_LONG).show();
    }

    /**
     * @param pendingUploadSong in-memory song for one automatic upload retry after successful login; ignored if not auth failure
     */
    public static void showAfterLeaveActivity(Context appContext, SongUploadFailureKind kind,
                                              @Nullable Song pendingUploadSong) {
        if (kind == SongUploadFailureKind.SUCCESS) {
            return;
        }
        if (kind == SongUploadFailureKind.NEEDS_SIGN_IN || kind == SongUploadFailureKind.SESSION_NOT_REFRESHED) {
            if (pendingUploadSong != null) {
                Memory.getInstance().setPendingUploadRetrySong(pendingUploadSong);
            }
            int resId = kind == SongUploadFailureKind.NEEDS_SIGN_IN
                    ? R.string.upload_needs_sign_in_message
                    : R.string.upload_session_expired_message;
            Toast.makeText(appContext, resId, Toast.LENGTH_LONG).show();
            Intent i = new Intent(appContext, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (pendingUploadSong != null) {
                i.putExtra(LoginActivity.EXTRA_CLEAR_PENDING_UPLOAD_RETRY_ON_CANCEL, true);
            }
            appContext.startActivity(i);
            return;
        }
        Toast.makeText(appContext, R.string.upload_network_or_server_message, Toast.LENGTH_LONG).show();
    }
}
