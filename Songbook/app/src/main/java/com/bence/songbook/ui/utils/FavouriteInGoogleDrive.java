package com.bence.songbook.ui.utils;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.util.Log;

import com.bence.projector.common.serializer.DateDeserializer;
import com.bence.projector.common.serializer.DateSerializer;
import com.bence.songbook.models.FavouriteSong;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bence.songbook.ui.activity.SongActivity.saveGmail;

abstract class FavouriteInGoogleDrive {
    private static final String TAG = "FavouriteInGoogleDrive";
    final String FAVOURITES = "FAVOURITES";
    GoogleSignInIntent googleSignInIntent;
    DriveResourceClient mDriveResourceClient;
    boolean found;
    Activity activity;

    void getFileInFolder(MetadataBuffer metadata) {
        found = false;
        for (Metadata next : metadata) {
            System.out.println("next.getTitle() = " + next.getTitle());
            DriveId driveId = next.getDriveId();
            if (next.isFolder()) {
                listFilesInFolder(driveId.asDriveFolder());
            } else {
                if (next.getTitle().equals(FAVOURITES)) {
                    found = true;
                    retrieveContents(driveId.asDriveFile());
                    break;
                }
            }
            if (found) {
                break;
            }
        }
    }

    void rewriteContents(DriveFile file, final List<FavouriteSong> favouriteSongs) {
        Task<DriveContents> openTask = mDriveResourceClient.openFile(file, DriveFile.MODE_WRITE_ONLY);
        openTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                DriveContents driveContents = task.getResult();
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new OutputStreamWriter(driveContents.getOutputStream()));
                    Gson gson = getGson();
                    String toJson = gson.toJson(favouriteSongs);
                    System.out.println("gson: " + toJson);
                    writer.write(toJson);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setStarred(true)
                        .setLastViewedByMeDate(new Date())
                        .build();
                return mDriveResourceClient.commitContents(driveContents, changeSet);
            }
        });
    }

    private void listFilesInFolder(DriveFolder folder) {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "application/json"))
                .build();
        Task<MetadataBuffer> queryTask = mDriveResourceClient.queryChildren(folder, query);
        queryTask
                .addOnSuccessListener(activity, new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer metadata) {
                        for (Metadata next : metadata) {
                            System.out.println("next.getTitle() = " + next.getTitle());
                            DriveId driveId = next.getDriveId();
                            if (!next.isFolder()) {
                                if (next.getTitle().equals(FAVOURITES)) {
                                    found = true;
                                    retrieveContents(driveId.asDriveFile());
                                    break;
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error retrieving files", e);
                    }
                });
    }

    abstract void retrieveContents(final DriveFile file);

    public void signIn() {
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        requiredScopes.add(new Scope(Scopes.EMAIL));
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount);
        } else {
            try {
                GoogleSignInClient googleSignInClient = buildGoogleSignInClient();
                Task<GoogleSignInAccount> googleSignInAccountTask = googleSignInClient.silentSignIn();
                updateViewWithGoogleSignInAccountTask(googleSignInAccountTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateViewWithGoogleSignInAccountTask(final Task<GoogleSignInAccount> task) {
        task.addOnSuccessListener(
                new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        Log.i(TAG, "Sign in success");
                        saveGmail(googleSignInAccount, activity);
                        initializeDriveClient(googleSignInAccount);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                GoogleSignInClient googleSignInClient = buildGoogleSignInClient();
                                googleSignInIntent.task(googleSignInClient.getSignInIntent());
                            }
                        });
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .requestEmail()
                        .build();
        return GoogleSignIn.getClient(activity, signInOptions);
    }

    public void initializeDriveClient(GoogleSignInAccount signInAccount) {
        DriveResourceClient mDriveResourceClient = Drive.getDriveResourceClient(activity, signInAccount);
        readingFavouritesFromDrive(mDriveResourceClient);
    }

    abstract void readingFavouritesFromDrive(DriveResourceClient mDriveResourceClient);

    @NonNull
    Gson getGson() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeAdapter(Date.class, new DateSerializer()).create();
    }

    public void signOut() {
        Task<Void> voidTask = buildGoogleSignInClient().signOut();
        voidTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "Sign out success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Sign out failed", e);
            }
        });
    }
}
