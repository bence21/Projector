package com.bence.songbook.ui.activity;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.bence.songbook.ui.activity.LoginActivity.RESULT_LOGGED_IN;
import static com.bence.songbook.ui.activity.NewSongActivity.sortLanguagesByRecentlyViewedSongs;
import static com.bence.songbook.ui.activity.SongActivity.saveGmail;
import static com.bence.songbook.ui.utils.SaveFavouriteInGoogleDrive.REQUEST_CODE_SIGN_IN;
import static com.bence.songbook.utils.BaseURL.BASE_URL;
import static java.lang.Math.min;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bence.projector.common.dto.StackDTO;
import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.LoginApiBean;
import com.bence.songbook.api.SongApiBean;
import com.bence.songbook.api.SongListApiBean;
import com.bence.songbook.api.StackApiBean;
import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.LoggedInUser;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.models.SongList;
import com.bence.songbook.models.SongListElement;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.repository.FavouriteSongRepository;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.impl.ormLite.FavouriteSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.LanguageRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.LoggedInUserRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.QueueSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongCollectionRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongListElementRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongListRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.bence.songbook.service.FavouriteSongService;
import com.bence.songbook.service.UserService;
import com.bence.songbook.ui.adapter.LanguageAdapter;
import com.bence.songbook.ui.utils.CheckSongForUpdate;
import com.bence.songbook.ui.utils.DynamicListView;
import com.bence.songbook.ui.utils.GoogleSignInIntent;
import com.bence.songbook.ui.utils.MainPageAdapter;
import com.bence.songbook.ui.utils.Preferences;
import com.bence.songbook.ui.utils.QueueSongAdapter;
import com.bence.songbook.ui.utils.SyncFavouriteInGoogleDrive;
import com.bence.songbook.ui.utils.SyncInBackground;
import com.bence.songbook.ui.utils.song.OrderMethod;
import com.bence.songbook.utils.Config;
import com.bence.songbook.utils.Utility;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings({"ConstantConditions", "deprecation"})
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int SONG_DELETED = 10;
    public static final int SONG_REQUEST = 3;
    public static final int SONG_UNDO_DELETION = 11;
    private final Memory memory = Memory.getInstance();
    private final int DOWNLOAD_SONGS_REQUEST_CODE = 1;
    private final int LOGIN_IN_ACTIVITY_REQUEST_CODE = 12;
    private List<Song> songs;
    private List<Song> values = new ArrayList<>();
    private String lastSearchedText = "";
    private Thread loadSongVersesThread;
    private Toast searchInSongTextIsAvailableToast;
    private boolean searchInSongTextIsAvailable;
    private SongRepository songRepository;
    private LinearLayout linearLayout;
    private PopupWindow filterPopupWindow;
    private PopupWindow selectLanguagePopupWindow;
    private MainActivity mainActivity;
    private List<Language> languages;
    private RecyclerView songListView;
    private PopupWindow sortPopupWindow;
    private int sortMethod;
    private PopupWindow collectionPopupWindow;
    private List<SongCollection> songCollections;
    private ListView collectionListView;
    private LanguageRepositoryImpl languageRepository;
    private SongCollectionRepositoryImpl songCollectionRepository;
    private SongAdapter adapter;
    private boolean reverseSortMethod;
    private boolean shortCollectionName;
    private boolean light_theme_switch;
    private boolean wasOrdinalNumber;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch containingVideosSwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch favouriteSwitch;
    private List<FavouriteSong> favouriteSongs;
    private boolean inSongSearchSwitch = false;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private DynamicListView<QueueSongAdapter> queueListView;
    private MenuItem searchItem;
    private QueueSongRepositoryImpl queueSongRepository;
    private View buttonLayout;
    private View peekLayout;
    private PopupWindow saveQueuePopupWindow;
    private PopupWindow addDuplicatesPopupWindow;
    private PopupWindow addSongListLinkPopupWindow;
    private boolean alreadyTried;
    private ViewPager viewPager;
    private int view_mode;
    private MainPageAdapter pageAdapter;
    private int firstVisibleItemPosition;
    private String previouslyTitleSearchText = "";
    private String previouslyInSongSearchText = "";
    private Thread lastSearchThread;
    private boolean suggestionStackUploaded = false;
    private QueueSongAdapter queueSongAdapter;

    public static String stripAccents(String s) {
        String nfdNormalizedString = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        s = pattern.matcher(nfdNormalizedString).replaceAll("");
        s = s.replaceAll("[^a-zA-Z0-9]", "");
        return s;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            setTheme(Preferences.getTheme(this));
            super.onCreate(savedInstanceState);
            onCreate2();
        } catch (Exception e) {
            uploadExceptionStack(e);
            throw e;
        }
    }

    private void uploadExceptionStack(Exception e) {
        try {
            if (!suggestionStackUploaded) {
                suggestionStackUploaded = true;
            } else {
                return; // we only upload once per run
            }
            uploadExceptionStack_(e);
        } catch (Exception e2) {
            logError(e2);
        }
    }

    private static void logError(Exception e) {
        Log.e(TAG, e.getMessage(), e);
    }

    private void uploadExceptionStack_(Exception e) {
        logError(e);
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        final StackApiBean stackApiBean = new StackApiBean();
        final StackDTO stackDTO = new StackDTO();
        stackDTO.setCreatedDate(new Date());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String email = UserService.getInstance().getEmailFromUserOrGmail(this);
        if (!email.isEmpty()) {
            stackDTO.setEmail(email);
        } else {
            stackDTO.setEmail(sharedPreferences.getString("email", ""));
        }
        stackDTO.setMessage(e.getMessage());
        stackDTO.setStackTrace(writer.toString());
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            stackDTO.setVersion(version);
        } catch (Exception ignored) {
        }
        Thread thread = new Thread(() -> stackApiBean.uploadStack(stackDTO));
        thread.start();
        try {
            thread.join(7000L);
        } catch (InterruptedException e1) {
            logError(e1);
        }
    }

    @SuppressLint({"ShowToast", "ClickableViewAccessibility"})
    private void onCreate2() {
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int queueIndex = sharedPreferences.getInt("queueIndex", -1);
        memory.setQueueIndex(queueIndex, this);
        initializeQueueListView();
        onCreate4();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeQueueListView() {
        queueListView = findViewById(R.id.queueList);
        if (queueListView == null) {
            return;
        }
        queueListView.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Disallow NestedScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    // Allow NestedScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }

            // Handle ListView touch events.
            v.onTouchEvent(event);
            return true;
        });

        queueListView.setListener(new DynamicListView.Listener() {
            @Override
            public void swapElements(int indexOne, int indexTwo) {
                List<QueueSong> values = memory.getQueue();
                QueueSong temp = values.get(indexOne);
                QueueSong secondTmp = values.get(indexTwo);
                int queueNumber = temp.getQueueNumber();
                temp.setQueueNumber(secondTmp.getQueueNumber());
                secondTmp.setQueueNumber(queueNumber);
                values.set(indexOne, secondTmp);
                values.set(indexTwo, temp);
                queueSongRepository.save(temp);
                queueSongRepository.save(secondTmp);
            }

            @Override
            public void deleteElement(int originalItem) {
                List<QueueSong> values = memory.getQueue();
                QueueSong temp = values.get(originalItem);
                List<QueueSong> listElements = new ArrayList<>();
                for (int i = originalItem + 1; i < values.size(); ++i) {
                    QueueSong queueSong = values.get(i);
                    queueSong.setQueueNumber(queueSong.getQueueNumber() - 1);
                    listElements.add(queueSong);
                }
                queueSongRepository.save(listElements);
                memory.removeQueueSong(temp);
                queueSongRepository.delete(temp);
                queueListView.invalidateViews();
                queueListView.refreshDrawableState();
            }
        });
    }

    private void onCreate4() {
        queueSongRepository = new QueueSongRepositoryImpl(this);
        final LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        buttonLayout = findViewById(R.id.buttonLayout);
        buttonLayout.setVisibility(View.GONE);
        peekLayout = findViewById(R.id.peekLayout);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    searchItem.collapseActionView();
                    buttonLayout.setVisibility(View.VISIBLE);
                    peekLayout.setVisibility(View.GONE);
                } else {
                    buttonLayout.setVisibility(View.GONE);
                    peekLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        memory.addOnQueueChangeListener(new Memory.Listener() {
            @Override
            public void onAdd(QueueSong queueSong) {
                if (!memory.getQueue().isEmpty()) {
                    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        setBottomSheetPadding();
                    }
                    bottomSheetBehavior.setHideable(false);
                    bottomSheetBehavior.setSkipCollapsed(false);
                }
                onQueueChanged();
            }

            @Override
            public void onRemove(QueueSong queueSong) {
                if (memory.getQueue().isEmpty()) {
                    setBottomSheetHideable();
                    if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                    linearLayout.setPadding(0, 0, 0, 0);
                }
                onQueueChanged();
            }
        });

        initPreferences();
        memory.setMainActivity(this);
        songListView = findViewById(R.id.songListView);
        CenterLayoutManager layoutManager = new CenterLayoutManager(this);
        layoutManager.setOrientation(CenterLayoutManager.VERTICAL);
        songListView.setLayoutManager(layoutManager);
        songListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE) {
                    CenterLayoutManager manager = (CenterLayoutManager) songListView.getLayoutManager();
                    firstVisibleItemPosition = manager.findFirstCompletelyVisibleItemPosition();
                }
            }
        });
        viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                songListView.smoothScrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });
        mainActivity = this;
        linearLayout = findViewById(R.id.mainLinearLayout);
        languageRepository = new LanguageRepositoryImpl(getApplicationContext());
        loadLanguages();
        songCollections = memory.getSongCollections();
        favouriteSongs = memory.getFavouriteSongs();
        songCollectionRepository = new SongCollectionRepositoryImpl(getApplicationContext());
        if (songCollections == null) {
            songCollections = songCollectionRepository.findAll();
            setShortNamesForSongCollections(songCollections);
            memory.setSongCollections(songCollections);
        }
        if (favouriteSongs == null) {
            loadFavouriteSongsFromDatabase();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //noinspection NullableProblems
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                hideKeyboard();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                hideKeyboard();
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        updateNavSignInTitleByLoggedIn();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Intent intent = new Intent(this, ExplanationActivity.class);
                startActivity(intent);
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, 1);
        }
        setSongs(memory.getSongs());
        createLoadSongVerseThread();
        songRepository = new SongRepositoryImpl(getApplicationContext());
        searchInSongTextIsAvailableToast = Toast.makeText(getApplicationContext(), R.string.SearchInSongTextIsAvailable, Toast.LENGTH_LONG);
        if (songs != null) {
            filter();
            loadAll();
            List<QueueSong> queue = memory.getQueue();
            if (queue == null || queue.size() == 0) {
                setDataToQueueSongs();
            }
        } else {
            setSongs(new ArrayList<>());
            initialLoad();
        }
        onCreateEnd();
    }

    private void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    private void onQueueChanged() {
        if (queueSongAdapter != null) {
            queueSongAdapter.notifyDataSetChanged();
        }
    }

    private void initialLoad() {
//        Thread thread = new Thread(() -> {
        // need to be faster
        filter();
        runOnUiThread(() -> {
            if (songs.size() > 0) {
                setDataToQueueSongs();
                Memory memory = Memory.getInstance();
                memory.setSongs(songs);
                loadAll();
                loadSongVersesThread.start();
                uploadViewsFavourites();
            } else {
                Intent loadIntent = new Intent(MainActivity.this, LanguagesActivity.class);
                startActivityForResult(loadIntent, DOWNLOAD_SONGS_REQUEST_CODE);
            }
        });
//        });
//        thread.start();
        // or a simple initial load
    }

    private void onCreateEnd() {
        Intent appLinkIntent = getIntent();
        Uri appLinkData = appLinkIntent.getData();
        if (appLinkData != null) {
            parseAppLink(appLinkData);
        }
        syncDatabase();
        setView();
        hideBottomSheetIfNoQueue();
        Config.getInstance().getYouTubeApiKey(this);
    }

    private void loadFavouriteSongsFromDatabase() {
        FavouriteSongRepository favouriteSongRepository = new FavouriteSongRepositoryImpl(this);
        favouriteSongs = favouriteSongRepository.findAll();
        memory.setFavouriteSongs(favouriteSongs);
    }

    private void hideBottomSheetIfNoQueue() {
        List<QueueSong> queue = memory.getQueue();
        if (queue != null && queue.size() < 1) {
            hideBottomSheet();
        }
    }

    private void updateNavSignInTitleByLoggedIn() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        MenuItem signInMenuItem = menu.findItem(R.id.nav_sign_in);
        if (signInMenuItem != null) {
            int resId;
            if (isLoggedIn()) {
                resId = R.string.sign_out;
            } else {
                resId = R.string.sign_in;
            }
            signInMenuItem.setTitle(getString(resId));
        }
    }

    private LoggedInUser getLoggedInUser() {
        return UserService.getInstance().getLoggedInUser(this);
    }

    private void setBottomSheetPadding() {
        int paddingDp = (int) getResources().getDimension(R.dimen.bottom_sheet_peek_height);
        linearLayout.setPadding(0, 0, 0, paddingDp);
    }

    @SuppressLint("ShowToast")
    private void parseAppLink(Uri appLinkData) {
        try {
            Toast this_song_is_not_saved = Toast.makeText(this, R.string.this_song_is_not_saved, Toast.LENGTH_LONG);
            String text = appLinkData.toString();
            if (text != null) {
                String prefix = "queue?ids=";
                if (text.contains(prefix)) {
                    parseQueueLink(text, prefix);
                    return;
                }
                prefix = "songList/";
                if (text.contains(prefix)) {
                    parseSongListLink(text, prefix);
                    return;
                }
                String[] str = {"/#/song/", "/song/"};
                for (String s : str) {
                    if (text.contains(s)) {
                        parseSongLink(this_song_is_not_saved, text, s);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void parseSongLink(final Toast this_song_is_not_saved, String text, String s) {
        final String songUuid = text.substring(text.lastIndexOf(s) + s.length());
        Song song = null;
        for (Song song1 : songs) {
            if (song1.getUuid() != null && song1.getUuid().equals(songUuid)) {
                song = song1;
                break;
            }
        }
        if (song == null) {
            song = songRepository.findByUUID(songUuid);
        }
        if (song != null) {
            showSongFullscreen(song);
        } else {
            Thread thread = new Thread(() -> {
                SongApiBean songApiBean = new SongApiBean();
                Song newSong = songApiBean.getSong(songUuid);
                if (newSong != null) {
                    this_song_is_not_saved.show();
                    showSongFullscreen(newSong);
                }
            });
            thread.start();
        }
    }

    private void parseQueueLink(String text, String prefix) {
        String ids = text.substring(text.lastIndexOf(prefix) + prefix.length());
        String[] split = ids.split(",");
        for (final String uuid : split) {
            Song song = null;
            for (Song song1 : songs) {
                if (song1.getUuid() != null && song1.getUuid().equals(uuid)) {
                    song = song1;
                    break;
                }
            }
            if (song == null) {
                song = songRepository.findByUUID(uuid);
            }
            if (song == null) {
                Thread thread = new Thread(() -> {
                    SongApiBean songApiBean = new SongApiBean();
                    Song newSong = songApiBean.getSong(uuid);
                    addToQueue(newSong);
                });
                thread.start();
            }
            addToQueue(song);
        }
        setDataToQueueSongs();
        queueListView.invalidateViews();
        showToaster(getString(R.string.added_to_queue), Toast.LENGTH_SHORT);
    }

    private void parseSongListLink(String text, String prefix) {
        String substring = text.substring(text.lastIndexOf(prefix) + prefix.length());
        String[] split = substring.split("\\?");
        if (split.length > 0) {
            substring = split[0];
        }
        final String uuid = substring;
        SongListRepositoryImpl songListRepository = new SongListRepositoryImpl(this);
        final SongList byUuid = songListRepository.findByUuid(uuid);
        if (byUuid == null) {
            Thread thread = new Thread(() -> {
                SongListApiBean songListApiBean = new SongListApiBean(MainActivity.this);
                SongList songList = songListApiBean.getSongList(uuid);
                if (songList != null) {
                    askSongListLink(songList);
                }
            });
            thread.start();
        } else {
            Intent intent = new Intent(MainActivity.this, SongListActivity.class);
            memory.setPassingSongList(byUuid);
            startActivityForResult(intent, 7);
        }
    }

    private void askSongListLink(final SongList songList) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.content_ask_song_list_link, null);
        addSongListLinkPopupWindow = new PopupWindow(
                customView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            addSongListLinkPopupWindow.setElevation(5.0f);
        }
        Button addButton = customView.findViewById(R.id.addToButton);
        addButton.setOnClickListener(view -> {
            List<SongListElement> songListElements = songList.getSongListElements();
            QueueSongRepositoryImpl queueSongRepository = new QueueSongRepositoryImpl(MainActivity.this);
            List<QueueSong> newQueueSongs = new ArrayList<>(songListElements.size());
            for (SongListElement element : songListElements) {
                QueueSong queueSong = new QueueSong();
                queueSong.setSong(element.getSong());
                memory.addSongToQueue(queueSong);
                newQueueSongs.add(queueSong);
            }
            queueSongRepository.save(newQueueSongs);
            showToaster(getString(R.string.added_to_queue), Toast.LENGTH_SHORT);
            addSongListLinkPopupWindow.dismiss();
        });
        Button newSongListButton = customView.findViewById(R.id.newSongListButton);
        newSongListButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SongListActivity.class);
            SongListRepositoryImpl songListRepository = new SongListRepositoryImpl(MainActivity.this);
            songListRepository.save(songList);
            SongListElementRepositoryImpl songListElementRepository = new SongListElementRepositoryImpl(MainActivity.this);
            songListElementRepository.save(songList.getSongListElements());
            memory.setPassingSongList(songList);
            intent.putExtra("newSongList", true);
            startActivityForResult(intent, 7);
            addSongListLinkPopupWindow.dismiss();
        });
        addSongListLinkPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        addSongListLinkPopupWindow.setOutsideTouchable(true);
        new Handler(Looper.getMainLooper()).post(() -> addSongListLinkPopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0));
    }

    private void addToQueue(Song song) {
        if (song != null) {
            QueueSong queueSong = new QueueSong();
            queueSong.setSong(song);
            memory.addSongToQueue(queueSong);
            queueSongRepository.save(queueSong);
        }
    }

    private void setBottomSheetHideable() {
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setSkipCollapsed(true);
    }

    private List<Song> copyListOfSongs(List<Song> songs) {
        ArrayList<Song> songArrayList = new ArrayList<>(songs.size());
        songArrayList.addAll(songs);
        return songArrayList;
    }

    private void uploadViewsFavourites() {
        List<Song> songs = copyListOfSongs(this.songs);
        Thread uploadViews = new Thread(() -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            Date lastUploadedViewsDate = new Date(sharedPreferences.getLong("lastUploadedViewsDate", 0));
            Date now = new Date();
            long lastInterval = 86400000; // one day
            long nowTime = now.getTime();
            long lastUploadedViewsDateTime = lastUploadedViewsDate.getTime();
            SongApiBean songApiBean = new SongApiBean();
            if (nowTime - lastUploadedViewsDateTime > lastInterval) {
                List<Song> uploadingSongs = new ArrayList<>();
                for (Song song : songs) {
                    if (song.getLastAccessed().getTime() > lastUploadedViewsDateTime && song.getModifiedDate().getTime() != 123L) {
                        uploadingSongs.add(song);
                    }
                }
                Collections.sort(uploadingSongs, (song1, song2) -> song1.getLastAccessed().compareTo(song2.getLastAccessed()));
                boolean oneUploaded = false;
                boolean successfully = true;
                for (Song song : uploadingSongs) {
                    if (songApiBean.uploadView(song) == null) {
                        successfully = false;
                        break;
                    } else {
                        oneUploaded = true;
                        lastUploadedViewsDateTime = song.getLastAccessed().getTime();
                    }
                }
                if (successfully) {
                    lastUploadedViewsDateTime = nowTime;
                }
                if (oneUploaded) {
                    sharedPreferences.edit().putLong("lastUploadedViewsDate", lastUploadedViewsDateTime).apply();
                }
            }

            // upload songs
            List<Song> uploadingSongs = new ArrayList<>();
            for (Song song : songs) {
                if (song.getModifiedDate().getTime() == 123L && !song.isAsDeleted() && !song.isSavedOnlyToDevice()) {
                    uploadingSongs.add(song);
                }
            }
            for (Song song : uploadingSongs) {
                final Song uploadedSong = songApiBean.uploadSong(song);
                if (uploadedSong != null && !uploadedSong.getUuid().trim().isEmpty()) {
                    song.setUuid(uploadedSong.getUuid());
                    song.setModifiedDate(uploadedSong.getModifiedDate());
                    songRepository.save(song);
                }
            }

            //upload inc favourites
            List<FavouriteSong> favouriteUploadingSongs = new ArrayList<>();
            for (FavouriteSong favouriteSong : favouriteSongs) {
                if (favouriteSong.isFavourite() && favouriteSong.isFavouriteNotPublished()) {
                    favouriteUploadingSongs.add(favouriteSong);
                }
            }
            FavouriteSongRepository favouriteSongRepository = new FavouriteSongRepositoryImpl(MainActivity.this);
            for (FavouriteSong favouriteSong : favouriteUploadingSongs) {
                Song song = favouriteSong.getSong();
                if (song != null && song.getUuid() != null && songApiBean.uploadIncFavourite(song) != null) {
                    favouriteSong.setFavouritePublished(true);
                    favouriteSongRepository.save(favouriteSong);
                } else {
                    break;
                }
            }
        });
        uploadViews.start();
    }

    private void setFavouritesForSongs() {
        HashMap<String, Song> hashMap = getStringSongHashMap();
        setFavourites(hashMap);
    }

    private void syncDatabase() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean syncAutomatically = sharedPreferences.getBoolean(LanguagesActivity.syncAutomatically, true);
        if (syncAutomatically) {
            String syncDateTime = "lastSyncDateTime";
            long lastSyncDateTime = sharedPreferences.getLong(syncDateTime, 0);
            Date date = new Date();
            if (date.getTime() - 1000 * 60 * 60 * 12 > lastSyncDateTime) {
                SyncInBackground syncInBackground = SyncInBackground.getInstance();
                if (lastSyncDateTime == 0) {
                    syncInBackground.setSyncFrom();
                }
                syncInBackground.sync(getApplicationContext());
                sharedPreferences.edit().putLong(syncDateTime, date.getTime()).apply();
            }
            syncDateTime = "lastViewsSyncDateTime";
            lastSyncDateTime = sharedPreferences.getLong(syncDateTime, 0);
            long i = 1000L * 60L * 60L * 24L * 30L;
            if (date.getTime() - i > lastSyncDateTime) {
                SyncInBackground syncInBackground = SyncInBackground.getInstance();
                syncInBackground.syncViews(getApplicationContext());
                sharedPreferences.edit().putLong(syncDateTime, date.getTime()).apply();
            }
        }
        if (sharedPreferences.getBoolean("YoutubeUrl", true)) {
            SyncInBackground syncInBackground = SyncInBackground.getInstance();
            syncInBackground.syncYoutubeUrl(getApplicationContext());
            sharedPreferences.edit().putBoolean("YoutubeUrl", false).apply();
        }
        syncFavouriteSongs();
    }

    private void syncFavouriteSongs() {
        FavouriteSongService.getInstance().syncFavourites(this);
        syncFavouriteSongsFromServer();
    }

    private void syncFavouriteSongsFromServer() {
        FavouriteSongService.getInstance().syncFavouritesFromServer(this, () -> {
            HashMap<String, Song> hashMap = getStringSongHashMap();
            loadFavouriteSongsFromDatabase();
            setFavourites(hashMap);
            search(lastSearchedText, adapter);
        });
    }

    private HashMap<String, Song> getStringSongHashMap() {
        HashMap<String, Song> hashMap = new HashMap<>(songs.size());
        for (Song song : songs) {
            hashMap.put(song.getUuid(), song);
        }
        return hashMap;
    }

    private void initPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sortMethod = sharedPreferences.getInt("sortMethod", 7);
        if (!sharedPreferences.getBoolean("wasRelevanceSort", false)) {
            sharedPreferences.edit().putBoolean("wasRelevanceSort", true).apply();
            sortMethod = 7;
            sharedPreferences.edit().putInt("sortMethod", sortMethod).apply();
        }
        reverseSortMethod = sharedPreferences.getBoolean("reverseSortMethod", false);
        shortCollectionName = sharedPreferences.getBoolean("shortCollectionName", false);
    }

    private void createLoadSongVerseThread() {
        loadSongVersesThread = new Thread() {
            @Override
            public void run() {
                try {
                    for (Song song : songs) {
                        song.fetchVerses();
                    }
                    searchInSongTextIsAvailableToast.show();
                    searchInSongTextIsAvailable = true;
                } catch (Exception ignored) {
                }
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DOWNLOAD_SONGS_REQUEST_CODE:
                if (resultCode >= 1) {
                    setSongs(songRepository.findAllExceptAsDeleted());
                    memory.setSongs(songs);
                    List<QueueSong> queue = memory.getQueue();
                    if (queue == null) {
                        queue = queueSongRepository.findAll();
                        Collections.sort(queue, (o1, o2) -> Utility.compare(o1.getQueueNumber(), o2.getQueueNumber()));
                        memory.setQueue(queue);
                    }
                    if (queue.size() < 1) {
                        hideBottomSheet();
                    }
                    loadLanguages();
                    songCollections = songCollectionRepository.findAll();
                    setShortNamesForSongCollections(songCollections);
                    memory.setSongCollections(songCollections);
                    selectLanguagePopupWindow = null;
                    collectionPopupWindow = null;
                    filterPopupWindow = null;
                    createLoadSongVerseThread();
                    loadSongVersesThread.start();
                    filter();
                    syncFavouriteSongsFromServer();
                    loadAll();
                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.downloaded) + " " + (resultCode - 1), Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case 2:
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (light_theme_switch != sharedPreferences.getBoolean("light_theme_switch", false)) {
                    recreate();
                }
                break;
            case SONG_REQUEST:
                switch (resultCode) {
                    case 1:
                        values.clear();
                        values.addAll(memory.getValues());
                        break;
                    case SONG_DELETED:
                        refreshSongs();
                        break;
                    case SONG_UNDO_DELETION:
                        sortSongs(songs);
                        refreshSongs();
                        break;
                    case CheckSongForUpdate.UPDATE_SONGS_RESULT:
                        Intent loadIntent = new Intent(this, LoadActivity.class);
                        startActivityForResult(loadIntent, DOWNLOAD_SONGS_REQUEST_CODE);
                        break;
                }
                break;
            case 4:
                if (resultCode == 1) {
                    setSongs(memory.getSongsOrEmptyList());
                    values.clear();
                    int size = songs.size();
                    if (size > 0) {
                        values.add(songs.get(size - 1));
                    }
                    adapter.setSongList(values);
                    if (pageAdapter != null) {
                        pageAdapter.notifyDataSetChanged();
                    }
                }
                break;
            case REQUEST_CODE_SIGN_IN:
                String TAG = "MainActivity";
                if (resultCode != RESULT_OK) {
                    Log.e(TAG, "Sign-in failed.");
                    showToaster("Sign-in failed.", Toast.LENGTH_LONG);
                    return;
                }
                Task<GoogleSignInAccount> getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    GoogleSignInAccount result = getAccountTask.getResult();
                    saveGmail(result, getApplicationContext());
                } else {
                    Log.e(TAG, "Sign-in failed.");
                    showToaster("Sign-in failed.", Toast.LENGTH_LONG);
                }
                break;
            case LOGIN_IN_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_LOGGED_IN) {
                    updateNavSignInTitleByLoggedIn();
                    syncFavouriteSongs();
                }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        queueListView.invalidateViews();
    }

    private void loadLanguages() {
        languages = languageRepository.findAllSelectedForDownload();
        sortLanguagesByRecentlyViewedSongs(languages, this);
    }

    private void refreshSongs() {
        setSongs(memory.getSongsOrEmptyList());
        values.clear();
        values.addAll(songs);
        adapter.setSongList(values);
        if (pageAdapter != null) {
            pageAdapter.notifyDataSetChanged();
        }
    }

    private void setShortNamesForSongCollections(List<SongCollection> songCollections) {
        HashMap<String, SongCollection> hashMap = new HashMap<>();
        List<SongCollection> collectionList = new ArrayList<>(songCollections.size());
        collectionList.addAll(songCollections);
        Collections.sort(collectionList, (lhs, rhs) -> Utility.compare(rhs.getSongCollectionElements().size(), lhs.getSongCollectionElements().size()));
        for (SongCollection songCollection : collectionList) {
            String shortName = songCollection.getShortName();
            if (hashMap.containsKey(shortName)) {
                SongCollection sameShortNameSongCollection = hashMap.get(shortName);
                String a = sameShortNameSongCollection.getName();
                String b = songCollection.getName();
                String newA = removeCommonString(a, b);
                if (newA == null) {
                    String newB = removeCommonString(b, a);
                    sameShortNameSongCollection.setShortName(newB);
                } else {
                    shortName = newA;
                }
            }
            songCollection.setShortName(shortName);
            hashMap.put(shortName, songCollection);
        }
    }

    private String removeCommonString(String a, String b) {
        StringBuilder newA;
        int k = 1;
        String[] splitA = a.split(" ");
        String[] splitB = b.split(" ");
        int i;
        for (i = 0; i < splitA.length && i < splitB.length; ++i) {
            try {
                String sA = splitA[i];
                String sB = splitB[i];
                if (sA.length() > k && sB.length() > k && sA.charAt(k) != sB.charAt(k)) {
                    newA = new StringBuilder();
                    for (int j = 0; j < splitB.length; ++j) {
                        newA.append((String.valueOf(splitB[j].charAt(0))).toUpperCase());
                        if (j == i) {
                            newA.append(splitB[j].substring(1, k + 1).toLowerCase());
                        }
                    }
                    return newA.toString();
                }
            } catch (StringIndexOutOfBoundsException e) {
                logError(e);
            }
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                loadAll();
            } else {
                Intent intent = new Intent(this, ExplanationActivity.class);
                startActivity(intent);
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissPopups();
    }

    private void dismissPopups() {
        if (sortPopupWindow != null) {
            sortPopupWindow.dismiss();
        }
        if (filterPopupWindow != null) {
            filterPopupWindow.dismiss();
        }
        if (selectLanguagePopupWindow != null) {
            selectLanguagePopupWindow.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissPopups();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void loadAll() {
        if (songs != null) {
            sortSongs(songs);
            values = new ArrayList<>();
            values.addAll(songs);
            adapter = new SongAdapter(values, new OnItemClickListener() {
                @Override
                public void onItemClick(Song song, int position) {
                    if (view_mode == 0) {
                        showSongFullscreen(song);
                    } else {
                        if (viewPager.getCurrentItem() == position) {
                            showSongFullscreen(song);
                        } else {
                            try {
                                viewPager.setCurrentItem(position);
                            } catch (IllegalStateException ignored) {
                            }
                        }
                    }
                }

                @Override
                public void onLongClick(Song song, int position) {
                    QueueSong queueSong = new QueueSong();
                    queueSong.setSong(song);
                    memory.addSongToQueue(queueSong);
                    queueSongRepository.save(queueSong);
                    queueListView.invalidateViews();
                    showToaster(getString(R.string.added_to_queue), Toast.LENGTH_SHORT);
                }
            });
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            view_mode = sharedPreferences.getInt("view_mode", 0);
            List<QueueSong> queue = memory.getQueue();
            if (queue == null) {
                setDataToQueueSongs();
            }
            if (queue == null) {
                return;
            }
            queueSongAdapter = new QueueSongAdapter(this, R.layout.list_row, queue, (position, row) -> queueListView.onGrab(position, row), shortCollectionName);
            if (queue.size() < 1) {
                linearLayout.setPadding(0, 0, 0, 0);
            } else {
                setBottomSheetPadding();
            }

            queueListView.setAdapter(queueSongAdapter);
            queueListView.setOnItemClickListener((parent, view, position, id) -> {
                List<QueueSong> queue1 = memory.getQueue();
                Song tmp = queue1.get(position).getSong();
                if (position + 1 < queue1.size()) {
                    memory.setQueueIndex(position + 1, MainActivity.this);
                } else {
                    memory.setQueueIndex(0, MainActivity.this);
                }
                showSongFullscreen(tmp);
            });
            queueListView.setOnTouchListener((v, event) -> {
                hideKeyboard();
                return false;
            });
            songListView.setHasFixedSize(true);
            songListView.setAdapter(adapter);
            songListView.setOnTouchListener((v, event) -> {
                if (!values.isEmpty()) {
                    hideKeyboard();
                }
                return false;
            });
            if (view_mode == 1) {
                pageAdapter = new MainPageAdapter(getSupportFragmentManager(), values);
                viewPager.setAdapter(pageAdapter);
            }
            search(lastSearchedText, adapter);
        }
    }

    public void search(final String text, final SongAdapter adapter) {
        Thread searchThread = new Thread(() -> {
            if (inSongSearchSwitch && (searchInSongTextIsAvailable)) {
                inSongSearch(text);
            } else {
                titleSearch(text);
            }
            if (Thread.interrupted()) {
                return;
            }
            runOnUiThread(() -> {
                if (adapter == null) {
                    loadAll();
                    return;
                }
                if (Thread.interrupted()) {
                    return;
                }
                adapter.setSongList(values);
                if (pageAdapter != null) {
                    pageAdapter.notifyDataSetChanged();
                }
            });
        });
        if (lastSearchThread != null && lastSearchThread.isAlive()) {
            lastSearchThread.interrupt();
        }
        searchThread.start();
        lastSearchThread = searchThread;
    }

    public void titleSearch(final String title) {
        memory.setLastSearchedInText(null);
        String text = title.toLowerCase();
        String stripped = stripAccents(text);
        String firstWord = stripAccents(text.split(" ")[0].toLowerCase());
        String other;
        if (text.length() > firstWord.length()) {
            other = stripAccents(text.substring(firstWord.length() + 1).toLowerCase());
        } else {
            other = "";
        }
        String ordinalNumber = firstWord;
        String collectionName = "";
        if (!firstWord.matches("^[0-9]+.*")) {
            char[] chars = firstWord.toCharArray();
            int i;
            for (i = 0; i < chars.length; ++i) {
                if (chars[i] >= '0' && chars[i] <= '9') {
                    break;
                }
            }
            collectionName = stripAccents(firstWord.substring(0, i).toLowerCase());
            ordinalNumber = firstWord.substring(i);
        }
        int ordinalNumberInt = Integer.MIN_VALUE;
        try {
            ordinalNumberInt = Integer.parseInt(ordinalNumber);
        } catch (Exception ignored) {
        }
        wasOrdinalNumber = false;
        List<Song> songList = new ArrayList<>();
        if (!values.isEmpty() && title.contains(previouslyTitleSearchText) && !previouslyTitleSearchText.trim().isEmpty()) {
            songList.addAll(values);
        } else {
            songList.addAll(songs);
        }
        final List<Song> tempSongList = new ArrayList<>();
        for (Song song : songList) {
            if (song == null) {
                continue;
            }
            if (containsInTitle(stripped, song, other, collectionName, ordinalNumber, ordinalNumberInt)) {
                tempSongList.add(song);
            }
            if (Thread.interrupted()) {
                return;
            }
        }
        if (wasOrdinalNumber) {
            try {
                String finalCollectionName = collectionName;
                String finalOrdinalNumber = ordinalNumber;
                int finalOrdinalNumberInt = ordinalNumberInt;
                sortSongCollectionElementsForSongs(ordinalNumber, collectionName, ordinalNumberInt, tempSongList);
                if (Thread.interrupted()) {
                    return;
                }
                Collections.sort(tempSongList, (l, r) -> {
                    List<SongCollectionElement> lSongCollectionElements = l.getSongCollectionElements();
                    List<SongCollectionElement> rSongCollectionElements = r.getSongCollectionElements();
                    return compareSongCollectionElementsFirstByOrdinalNumber(lSongCollectionElements, rSongCollectionElements, finalCollectionName, finalOrdinalNumber, finalOrdinalNumberInt);
                });
                if (Thread.interrupted()) {
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        runOnUiThread(() -> {
            values.clear();
            values.addAll(tempSongList);
            previouslyTitleSearchText = title;
            previouslyInSongSearchText = "";
        });
    }

    private void sortSongCollectionElementsForSongs(String ordinalNumber, String collectionName, int ordinalNumberInt, List<Song> tempSongList) {
        Comparator<SongCollectionElement> sortBySongCollection = getSongCollectionElementComparator(collectionName, ordinalNumber, ordinalNumberInt);
        for (Song song : tempSongList) {
            List<SongCollectionElement> songCollectionElements = song.getSongCollectionElements();
            if (songCollectionElements.size() > 1) {
                List<SongCollectionElement> synchronizedList = Collections.synchronizedList(songCollectionElements);
                Collections.sort(synchronizedList, sortBySongCollection);
                song.setSongCollectionElements(synchronizedList);
                if (Thread.interrupted()) {
                    return;
                }
            }
        }
    }

    private boolean containsInCollectionName(SongCollection songCollection, String collectionName, String name) {
        if (collectionName.trim().isEmpty()) {
            return false;
        }
        return name.contains(collectionName) || songCollection.getStrippedShortName().contains(collectionName);
    }

    private boolean isContainsInCollectionName(String collectionName, SongCollectionElement songCollectionElement) {
        SongCollection songCollection = songCollectionElement.getSongCollection();
        String name = songCollection.getStrippedName();
        return containsInCollectionName(songCollection, collectionName, name);
    }

    private int compareSongCollectionElementByIntMatch(SongCollectionElement songCollectionElement1, SongCollectionElement songCollectionElement2, int ordinalNumberInt) {
        boolean ordinalNumberMatch1 = songCollectionElement1.getOrdinalNumberInt() == ordinalNumberInt;
        boolean ordinalNumberMatch2 = songCollectionElement2.getOrdinalNumberInt() == ordinalNumberInt;
        if (ordinalNumberMatch1 == ordinalNumberMatch2) {
            return 0;
        } else if (ordinalNumberMatch1) {
            return -1;
        } else {
            return 1;
        }
    }

    private int compareSongCollectionElementByPartialMatch(SongCollectionElement songCollectionElement1, SongCollectionElement songCollectionElement2, String ordinalNumber) {
        boolean ordinalNumberMatch1 = songCollectionElement1.getOrdinalNumberLowerCase().contains(ordinalNumber);
        boolean ordinalNumberMatch2 = songCollectionElement2.getOrdinalNumberLowerCase().contains(ordinalNumber);
        if (ordinalNumberMatch1 == ordinalNumberMatch2) {
            if (ordinalNumberMatch1) {
                return Integer.compare(songCollectionElement1.getOrdinalNumberInt(), songCollectionElement2.getOrdinalNumberInt());
            }
            return 0;
        } else if (ordinalNumberMatch1) {
            return -1;
        } else {
            return 1;
        }
    }

    private int compareSongCollectionElementByMatch(SongCollectionElement songCollectionElement1, SongCollectionElement songCollectionElement2, String ordinalNumber, int ordinalNumberInt) {
        boolean ordinalNumberMatch1 = songCollectionElement1.getOrdinalNumberLowerCase().equals(ordinalNumber);
        boolean ordinalNumberMatch2 = songCollectionElement2.getOrdinalNumberLowerCase().equals(ordinalNumber);
        if (ordinalNumberMatch1 == ordinalNumberMatch2) {
            if (ordinalNumberMatch1) {
                return 0;
            }
            int compare = compareSongCollectionElementByIntMatch(songCollectionElement1, songCollectionElement2, ordinalNumberInt);
            if (compare == 0) {
                return compareSongCollectionElementByPartialMatch(songCollectionElement1, songCollectionElement2, ordinalNumber);
            }
            return compare;
        } else if (ordinalNumberMatch1) {
            return -1;
        } else {
            return 1;
        }
    }

    private int compareSongCollectionElementFirstByOrdinalNumber(SongCollectionElement lSongCollectionElement, SongCollectionElement rSongCollectionElement) {
        int compareOrdinalNumber = Integer.compare(lSongCollectionElement.getOrdinalNumberInt(), rSongCollectionElement.getOrdinalNumberInt());
        if (compareOrdinalNumber != 0) {
            return compareOrdinalNumber;
        }
        return getSongComparator().compare(lSongCollectionElement.getSong(), rSongCollectionElement.getSong());
    }

    private int compareSongCollectionElementsFirstByOrdinalNumber(List<SongCollectionElement> lSongCollectionElements, List<SongCollectionElement> rSongCollectionElements, String collectionName, String ordinalNumber, int ordinalNumberInt) {
        int lSize = lSongCollectionElements.size();
        int rSize = rSongCollectionElements.size();
        if (lSize != rSize) {
            if (lSize == 0) {
                return 1;
            }
            if (rSize == 0) {
                return -1;
            }
        }
        return compareSongCollectionElementsFirstByOrdinalNumberEnd(lSongCollectionElements, rSongCollectionElements, collectionName, ordinalNumber, ordinalNumberInt, lSize, rSize);
    }

    @NonNull
    private Comparator<SongCollectionElement> getSongCollectionElementComparator(String collectionName, String ordinalNumber, int ordinalNumberInt) {
        return (songCollectionElement1, songCollectionElement2) -> {
            boolean containsInCollection1 = isContainsInCollectionName(collectionName, songCollectionElement1);
            boolean containsInCollection2 = isContainsInCollectionName(collectionName, songCollectionElement2);
            if (containsInCollection1 == containsInCollection2) {
                return compareSongCollectionElementByMatch(songCollectionElement1, songCollectionElement2, ordinalNumber, ordinalNumberInt);
            } else if (containsInCollection1) {
                return -1;
            } else {
                return 1;
            }
        };
    }

    private int compareSongCollectionElementsFirstByOrdinalNumberEnd(List<SongCollectionElement> lSongCollectionElements, List<SongCollectionElement> rSongCollectionElements, String collectionName, String ordinalNumber, int ordinalNumberInt, int lSize, int rSize) {
        int minSize = min(lSize, rSize);
        for (int i = 0; i < minSize; ++i) {
            SongCollectionElement lSongCollectionElement = lSongCollectionElements.get(i);
            SongCollectionElement rSongCollectionElement = rSongCollectionElements.get(i);
            boolean containsInCollectionL = isContainsInCollectionName(collectionName, lSongCollectionElement);
            boolean containsInCollectionR = isContainsInCollectionName(collectionName, rSongCollectionElement);
            if (containsInCollectionL == containsInCollectionR) {
                int compare = compareSongCollectionElementByMatch(lSongCollectionElement, rSongCollectionElement, ordinalNumber, ordinalNumberInt);
                if (compare != 0) {
                    return compare;
                }
                compare = compareSongCollectionElementFirstByOrdinalNumber(lSongCollectionElement, rSongCollectionElement);
                if (compare != 0) {
                    return compare;
                }
            } else if (containsInCollectionL) {
                return -1;
            } else {
                return 1;
            }
        }
        return 0;
    }

    private boolean containsInTitle(String stripped, Song song, String other, String collectionName, String ordinalNumber, int ordinalNumberInt) {
        String strippedTitle = song.getStrippedTitle();
        if (strippedTitle.contains(stripped)) {
            return true;
        }
        List<SongCollectionElement> songCollectionElements = song.getSongCollectionElements();
        for (SongCollectionElement songCollectionElement : songCollectionElements) {
            if (containsInSongCollectionElement(songCollectionElement, other, collectionName, ordinalNumber, ordinalNumberInt, strippedTitle)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsInSongCollectionElement(SongCollectionElement songCollectionElement, String other, String collectionName, String ordinalNumber, int ordinalNumberInt, String strippedTitle) {
        SongCollection songCollection = songCollectionElement.getSongCollection();
        if (songCollection == null) {
            return false;
        }
        String songOrdinalNumber = songCollectionElement.getOrdinalNumberLowerCase();
        boolean equals = songOrdinalNumber.equals(ordinalNumber);
        boolean contains = songOrdinalNumber.contains(ordinalNumber) || ordinalNumberInt == songCollectionElement.getOrdinalNumberInt();
        if (collectionName.isEmpty()) {
            boolean b = (!ordinalNumber.isEmpty() && contains) || equals;
            if (other.isEmpty()) {
                if (b && equals) {
                    wasOrdinalNumber = true;
                }
                return b;
            }
            boolean b1 = b && strippedTitle.contains(other);
            if (b1 && equals) {
                wasOrdinalNumber = true;
            }
            return b1;
        }
        String name = songCollection.getStripedName();
        String shortName = songCollection.getStrippedShortName();
        boolean b = name.contains(collectionName) || shortName.contains(collectionName);
        if (ordinalNumber.isEmpty()) {
            if (other.isEmpty()) {
                if (b && equals) {
                    wasOrdinalNumber = true;
                }
                return b;
            }
            boolean b1 = b && strippedTitle.contains(other);
            if (b1 && equals) {
                wasOrdinalNumber = true;
            }
            return b1;
        }
        if (other.isEmpty()) {
            boolean b2 = b && contains;
            if (b2 && equals) {
                wasOrdinalNumber = true;
            }
            return b2;
        }
        boolean b2 = b && contains && strippedTitle.contains(other);
        if (b2 && equals) {
            wasOrdinalNumber = true;
        }
        return b2;
    }

    public void inSongSearch(final String title) {
        try {
            inSongSearch_(title);
        } catch (Exception e) {
            uploadExceptionStack(e);
        }
    }

    private void inSongSearch_(final String title) {
        if (!searchInSongTextIsAvailable) {
            titleSearch(title);
            return;
        }
        String text = stripAccents(title.toLowerCase());
        memory.setLastSearchedInText(text);
        List<Song> songList = new ArrayList<>();
        if (!values.isEmpty() && title.contains(previouslyInSongSearchText) && !previouslyInSongSearchText.trim().isEmpty()) {
            songList.addAll(values);
        } else {
            songList.addAll(songs);
        }
        final List<Song> tempSongList = new ArrayList<>();
        for (Song song : songList) {
            boolean contains = song.getStrippedTitle().contains(text);
            if (!contains) {
                for (SongVerse verse : song.getVerses()) {
                    if (verse.getStrippedText().contains(text)) {
                        contains = true;
                        break;
                    }
                }
            }
            if (contains) {
                tempSongList.add(song);
            }
            if (Thread.interrupted()) {
                return;
            }
        }
        runOnUiThread(() -> {
            values.clear();
            values.addAll(tempSongList);
            previouslyInSongSearchText = title;
            previouslyTitleSearchText = "";
        });
    }

    public void showSongFullscreen(Song song) {
        Intent intent = new Intent(this, SongActivity.class);
        memory.setPassingSong(song);
        intent.putExtra("verseIndex", 0);
        startActivityForResult(intent, SONG_REQUEST);
    }

    private Comparator<Song> getSongComparator() {
        OrderMethod orderMethod = getOrderMethod(sortMethod);
        Comparator<Song> songComparator = null;
        if (orderMethod == null || orderMethod.equals(OrderMethod.RELEVANCE)) {
            songComparator = getSongComparatorByRelevanceOrder();
        } else if (orderMethod.equals(OrderMethod.ASCENDING_BY_TITLE)) {
            songComparator = getSongComparatorByAscendingByTitle();
        } else if (orderMethod.equals(OrderMethod.BY_MODIFIED_DATE)) {
            songComparator = getSongComparatorByModifiedDate();
        } else if (orderMethod.equals(OrderMethod.BY_CREATED_DATE)) {
            songComparator = getSongComparatorByCreatedDate();
        } else if (orderMethod.equals(OrderMethod.BY_LAST_ACCESSED)) {
            songComparator = getSongComparatorByLastAccessed();
        } else if (orderMethod.equals(OrderMethod.BY_COLLECTION)) {
            songComparator = getSongComparatorByCollection();
        }
        return songComparator;
    }

    private OrderMethod getOrderMethod(int sortMethod) {
        switch (sortMethod) {
            case 7:
                return OrderMethod.RELEVANCE;
            case 0:
                return OrderMethod.BY_MODIFIED_DATE;
            case 1:
                return OrderMethod.ASCENDING_BY_TITLE;
            case 3:
                return OrderMethod.BY_CREATED_DATE;
            case 5:
                return OrderMethod.BY_LAST_ACCESSED;
            case 6:
                return OrderMethod.BY_COLLECTION;
        }
        return OrderMethod.RELEVANCE;
    }

    private void sortSongs(List<Song> all) {
        Collections.sort(all, getSongComparator());
        if (reverseSortMethod) {
            Collections.reverse(all);
        }
    }

    @NonNull
    private Comparator<Song> getSongComparatorByRelevanceOrder() {
        return (lhs, rhs) -> {
            Integer scoreL = lhs.getScore();
            Integer scoreR = rhs.getScore();
            if (scoreL.equals(scoreR)) {
                return rhs.getModifiedDate().compareTo(lhs.getModifiedDate());
            }
            return scoreR.compareTo(scoreL);
        };
    }

    @NonNull
    private Comparator<Song> getSongComparatorByModifiedDate() {
        return (lhs, rhs) -> rhs.getModifiedDate().compareTo(lhs.getModifiedDate());
    }

    @NonNull
    private Comparator<Song> getSongComparatorByAscendingByTitle() {
        return (lhs, rhs) -> lhs.getStrippedTitle().compareTo(rhs.getStrippedTitle());
    }

    @NonNull
    private Comparator<Song> getSongComparatorByCreatedDate() {
        return (lhs, rhs) -> lhs.getCreatedDate().compareTo(rhs.getCreatedDate());
    }

    @NonNull
    private Comparator<Song> getSongComparatorByLastAccessed() {
        return (lhs, rhs) -> rhs.getLastAccessed().compareTo(lhs.getLastAccessed());
    }

    @NonNull
    private Comparator<Song> getSongComparatorByCollection() {
        return (l, r) -> {
            List<SongCollectionElement> lSongCollectionElements = l.getSongCollectionElements();
            List<SongCollectionElement> rSongCollectionElements = r.getSongCollectionElements();
            return compareSongCollectionElements(lSongCollectionElements, rSongCollectionElements);
        };
    }

    private int compareSongCollectionElements(List<SongCollectionElement> lSongCollectionElements, List<SongCollectionElement> rSongCollectionElements) {
        int lSize = lSongCollectionElements.size();
        int rSize = rSongCollectionElements.size();
        Comparator<SongCollectionElement> sortBySongCollection = (o1, o2)
                -> o1.getSongCollection().getName().compareTo(o2.getSongCollection().getName());
        Collections.sort(lSongCollectionElements, sortBySongCollection);
        Collections.sort(rSongCollectionElements, sortBySongCollection);
        int minSize = min(lSize, rSize);
        for (int i = 0; i < minSize; ++i) {
            SongCollectionElement lSongCollectionElement = lSongCollectionElements.get(i);
            SongCollectionElement rSongCollectionElement = rSongCollectionElements.get(i);
            int compare = compareSongCollectionElement(lSongCollectionElement, rSongCollectionElement);
            if (compare != 0) {
                return compare;
            }
        }
        if (lSize > rSize) {
            return -1;
        } else if (lSize < rSize) {
            return 1;
        }
        return 0;
    }

    private int compareSongCollectionElement(SongCollectionElement lSongCollectionElement, SongCollectionElement rSongCollectionElement) {
        SongCollection lSongCollection = lSongCollectionElement.getSongCollection();
        SongCollection rSongCollection = rSongCollectionElement.getSongCollection();
        if (lSongCollection.getName().equals(rSongCollection.getName())) {
            return Integer.compare(lSongCollectionElement.getOrdinalNumberInt(), rSongCollectionElement.getOrdinalNumberInt());
        }
        return lSongCollection.getStrippedName().compareTo(rSongCollection.getStrippedName());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (sortPopupWindow != null && sortPopupWindow.isShowing()) {
            sortPopupWindow.dismiss();
        } else if (selectLanguagePopupWindow != null && selectLanguagePopupWindow.isShowing()) {
            selectLanguagePopupWindow.dismiss();
        } else if (collectionPopupWindow != null && collectionPopupWindow.isShowing()) {
            collectionPopupWindow.dismiss();
        } else if (filterPopupWindow != null && filterPopupWindow.isShowing()) {
            filterPopupWindow.dismiss();
        } else if (saveQueuePopupWindow != null && saveQueuePopupWindow.isShowing()) {
            saveQueuePopupWindow.dismiss();
        } else if (addDuplicatesPopupWindow != null && addDuplicatesPopupWindow.isShowing()) {
            addDuplicatesPopupWindow.dismiss();
        } else if (addSongListLinkPopupWindow != null && addSongListLinkPopupWindow.isShowing()) {
            addSongListLinkPopupWindow.dismiss();
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        hideKeyboard();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        searchItem = menu.findItem(R.id.action_search);
        final MenuItem searchInTextMenuItem = menu.findItem(R.id.action_search_in_text);
        searchInTextMenuItem.setOnMenuItemClickListener(item -> {
            Drawable drawable = item.getIcon();
            if (!inSongSearchSwitch) {
                if (drawable != null) {
                    drawable.mutate();
                    drawable.setColorFilter(Color.rgb(153, 175, 174), PorterDuff.Mode.SRC_ATOP);
                }
                inSongSearchSwitch = true;
            } else {
                if (drawable != null) {
                    drawable.mutate();
                    drawable.setColorFilter(Color.rgb(94, 89, 94), PorterDuff.Mode.SRC_ATOP);
                }
                inSongSearchSwitch = false;
            }
            if (searchInSongTextIsAvailable) {
                search(lastSearchedText, adapter);
            } else {
                if (!loadSongVersesThread.isAlive()) {
                    try {
                        loadSongVersesThread.start();
                    } catch (IllegalThreadStateException e) {
                        createLoadSongVerseThread();
                        loadSongVersesThread.start();
                    }
                }
                Toast toast = Toast.makeText(getApplicationContext(), R.string.You_need_to_wait_for_this_feature, Toast.LENGTH_LONG);
                toast.show();
            }
            return false;
        });

        final SearchView mSearchView = (SearchView) searchItem.getActionView();
        searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS |
                MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        mSearchView.setQueryHint(getString(R.string.search));
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String enteredText = newText.trim();
                search(enteredText, adapter);
                lastSearchedText = enteredText;
                if (enteredText.equals("show similar")) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    sharedPreferences.edit().putBoolean("show_similar", true).apply();
                }
                return false;
            }
        });
        searchItem.expandActionView();
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                searchItem.getActionView().requestFocusFromTouch();
                mSearchView.setIconified(false);
                showKeyboard();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                hideKeyboard();
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchItem != null) {
            searchItem.expandActionView();
            showKeyboard();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_download_songs) {
            Intent loadIntent = new Intent(this, LanguagesActivity.class);
            startActivityForResult(loadIntent, DOWNLOAD_SONGS_REQUEST_CODE);
        } else if (id == R.id.nav_settings) {
            Intent loadIntent = new Intent(this, SettingsActivity.class);
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            light_theme_switch = sharedPreferences.getBoolean("light_theme_switch", false);
            startActivityForResult(loadIntent, 2);
        } else if (id == R.id.nav_new_song) {
            Intent loadIntent = new Intent(this, NewSongActivity.class);
            startActivityForResult(loadIntent, 4);
        } else if (id == R.id.nav_library) {
            Intent loadIntent = new Intent(this, LibraryActivity.class);
            startActivityForResult(loadIntent, 5);
        } else if (id == R.id.nav_privacy_policy) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://comsongbook.firebaseapp.com/privacy_policy.html")));
        } else if (id == R.id.nav_sign_in) {
            if (!isLoggedIn()) {
                login();
            } else {
                logout();
            }
//            if (!gSignIn) {
//                googleSignInPopupWindow = showGoogleSignIn((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE), true);
//                if (googleSignInPopupWindow != null) {
//                    if (alreadyTried2) {
//                        View viewById = googleSignInPopupWindow.getContentView().findViewById(R.id.notWorksTextView);
//                        viewById.setVisibility(View.VISIBLE);
//                    }
//                    googleSignInPopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
//                }
//            } else {
//                new SyncFavouriteInGoogleDrive(null, this, null, null).signOut();
//                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//                sharedPreferences.edit().putBoolean("gSignIn", false).apply();
//                if (signInMenuItem != null) {
//                    signInMenuItem.setTitle(getString(R.string.sign_in));
//                }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isLoggedIn() {
        return UserService.getInstance().isLoggedIn(this);
    }

    private void login() {
        Intent loadIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loadIntent, LOGIN_IN_ACTIVITY_REQUEST_CODE);
    }

    private void logout() {
        Thread thread = new Thread(() -> {
            LoggedInUserRepositoryImpl loggedInUserRepository = new LoggedInUserRepositoryImpl(MainActivity.this);
            LoggedInUser loggedInUser = getLoggedInUser();
            loggedInUserRepository.delete(loggedInUser);

            LoginApiBean loginApiBean = new LoginApiBean();
            loginApiBean.logout();
            runOnUiThread(this::updateNavSignInTitleByLoggedIn);
        });
        thread.start();
    }

    public void onSortButtonClick(View view) {
        if (sortPopupWindow != null) {
            sortPopupWindow.dismiss();
        } else {
            createSortPopup();
        }
        hideKeyboard();
        sortPopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
    }

    private void createSortPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View customView = inflater.inflate(R.layout.content_sort, null);
        sortPopupWindow = new PopupWindow(
                customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        @SuppressLint("UseSwitchCompatOrMaterialCode") final Switch reverseSwitch = customView.findViewById(R.id.reverseSwitch);
        RadioGroup radioGroup = customView.findViewById(R.id.radioSort);
        if (sortMethod == 7) {
            radioGroup.check(R.id.relevanceRadioButton);
        } else if (sortMethod == 0) {
            radioGroup.check(R.id.modifiedDateRadioButton);
        } else if (sortMethod == 1) {
            radioGroup.check(R.id.byTitleRadioButton);
        } else if (sortMethod == 3) {
            radioGroup.check(R.id.byCreatedDateRadioButton);
        } else if (sortMethod == 5) {
            radioGroup.check(R.id.recentlyViewedRadioButton);
        } else if (sortMethod == 6) {
            radioGroup.check(R.id.byCollectionRadioButton);
        } else if (sortMethod == 2) {
            reverseSortMethod = true;
            sortMethod = 1;
            saveReverseSortMethod();
            radioGroup.check(R.id.byTitleRadioButton);
        } else if (sortMethod == 4) {
            reverseSortMethod = true;
            sortMethod = 3;
            saveReverseSortMethod();
            radioGroup.check(R.id.byCreatedDateRadioButton);
        }
        reverseSwitch.setChecked(reverseSortMethod);
        reverseSwitch.setOnClickListener(v -> {
            reverseSortMethod = reverseSwitch.isChecked();
            saveReverseSortMethod();
            loadAll();
            sortPopupWindow.dismiss();
        });
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.relevanceRadioButton) {
                sortMethod = 7;
            } else if (checkedId == R.id.modifiedDateRadioButton) {
                sortMethod = 0;
            } else if (checkedId == R.id.byTitleRadioButton) {
                sortMethod = 1;
            } else if (checkedId == R.id.byCreatedDateRadioButton) {
                sortMethod = 3;
            } else if (checkedId == R.id.recentlyViewedRadioButton) {
                sortMethod = 5;
            } else if (checkedId == R.id.byCollectionRadioButton) {
                sortMethod = 6;
            }
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            sharedPreferences.edit().putInt("sortMethod", sortMethod).apply();
            loadAll();
            sortPopupWindow.dismiss();
        });
        if (Build.VERSION.SDK_INT >= 21) {
            sortPopupWindow.setElevation(5.0f);
        }
        sortPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        sortPopupWindow.setOutsideTouchable(true);
    }

    private void saveReverseSortMethod() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        sharedPreferences.edit().putBoolean("reverseSortMethod", reverseSortMethod).apply();
    }

    public void onFilterButtonClick(View view) {
        if (filterPopupWindow != null) {
            filterPopupWindow.dismiss();
        } else {
            createFilterPopupWindow();
        }
        hideKeyboard();
        filterPopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
    }

    private void createFilterPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.content_filter, null);
        filterPopupWindow = new PopupWindow(
                customView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            filterPopupWindow.setElevation(5.0f);
        }
        ImageButton closeButton = customView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(view -> filterPopupWindow.dismiss());
        if (songCollections.isEmpty()) {
            customView.findViewById(R.id.collectionButton).setVisibility(View.GONE);
        }
        containingVideosSwitch = customView.findViewById(R.id.containingVideosSwitch);
        containingVideosSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hideKeyboard();
            filter();
            loadAll();
        });
        favouriteSwitch = customView.findViewById(R.id.favouriteSwitch);
        favouriteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hideKeyboard();
            filter();
            loadAll();
        });
        filterPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        filterPopupWindow.setOutsideTouchable(true);
    }

    private void sortCollectionBySelectedLanguages() {
        boolean oneLanguageSelected = isOneLanguageSelected();
        LongSparseArray<Object> languageHashMap = new LongSparseArray<>(languages.size());
        for (Language language : languages) {
            if (oneLanguageSelected) {
                if (language.isSelected()) {
                    languageHashMap.put(language.getId(), true);
                }
            } else {
                languageHashMap.put(language.getId(), false);
            }
        }
        List<SongCollection> filteredSongCollections = new ArrayList<>();
        for (SongCollection songCollection : songCollections) {
            if (songCollection.getLanguage() == null || languageHashMap.get(songCollection.getLanguage().getId()) != null) {
                filteredSongCollections.add(songCollection);
            }
        }
        for (SongCollection songCollection : songCollections) {
            if (!filteredSongCollections.contains(songCollection)) {
                filteredSongCollections.add(songCollection);
            }
        }
        SongCollectionAdapter songCollectionAdapter = new SongCollectionAdapter(mainActivity,
                R.layout.activity_language_checkbox_row, filteredSongCollections);
        collectionListView.setAdapter(songCollectionAdapter);
    }

    private void createCollectionPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.content_select_collection, null);
        collectionPopupWindow = new PopupWindow(
                customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            collectionPopupWindow.setElevation(5.0f);
        }
        Button selectButton = customView.findViewById(R.id.selectButton);
        selectButton.setOnClickListener(view -> {
            filter();
            loadAll();
            filterPopupWindow.dismiss();
            collectionPopupWindow.dismiss();
        });
        Collections.sort(songCollections, (o1, o2) -> Utility.compare(o2.getSongCollectionElements().size(), o1.getSongCollectionElements().size()));
        collectionListView = customView.findViewById(R.id.listView);
        collectionPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        collectionPopupWindow.setOutsideTouchable(true);
    }

    private void filter() {
        filterSongsByLanguage();
        filterSongsByCollection();
        filterSongsByVideos();
        filterSongsByFavourites();
        setFavouritesForSongs();
    }

    private void setDataToQueueSongs() {
        LongSparseArray<Song> sparseArray = new LongSparseArray<>(songs.size());
        for (Song song : songs) {
            sparseArray.put(song.getId(), song);
        }
        List<QueueSong> queue = memory.getQueue();
        if (queue == null) {
            queue = queueSongRepository.findAll();
            Collections.sort(queue, (o1, o2) -> Utility.compare(o1.getQueueNumber(), o2.getQueueNumber()));
            memory.setQueue(queue);
            if (queue.isEmpty()) {
                hideBottomSheet();
            }
        }
        List<Song> songs = new ArrayList<>();
        for (QueueSong queueSong : queue) {
            if (queueSong.getSong() != null) {
                Long id = queueSong.getSong().getId();
                if (id == null) {
                    continue;
                }
                Song song = sparseArray.get(id);
                if (song != null) {
                    queueSong.setSong(song);
                } else {
                    song = songRepository.findOne(id);
                    if (song != null) {
                        queueSong.setSong(song);
                        sparseArray.put(id, song);
                    }
                }
                if (song != null) {
                    songs.add(song);
                }
            }
        }
        HashMap<String, Song> hashMap = new HashMap<>();
        for (Song song : songs) {
            if (song.getUuid() != null) {
                hashMap.put(song.getUuid(), song);
            }
        }
        setSongCollections(hashMap);
        setFavourites(hashMap);
    }

    private void clearSongSongCollections(Collection<Song> songs) {
        for (Song song : songs) {
            song.getSongCollections().clear();
            song.getSongCollectionElements().clear();
        }
    }

    private void setSongCollections(HashMap<String, Song> hashMap) {
        clearSongSongCollections(hashMap.values());
        for (SongCollection songCollection : songCollections) {
            for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
                String songUuid = songCollectionElement.getSongUuid();
                if (hashMap.containsKey(songUuid)) {
                    pairSongWithSongCollectionElement_hashMap(hashMap, songCollection, songCollectionElement, songUuid, false);
                }
            }
        }
    }

    private void setFavourites(HashMap<String, Song> hashMap) {
        for (FavouriteSong favouriteSong : favouriteSongs) {
            if (favouriteSong.getSong() != null) {
                String songUuid = favouriteSong.getSong().getUuid();
                if (hashMap.containsKey(songUuid)) {
                    Song song = hashMap.get(songUuid);
                    song.setFavourite(favouriteSong);
                }
            }
        }
    }

    private void filterSongsByFavourites() {
        if (favouriteSwitch != null && favouriteSwitch.isChecked()) {
            ArrayList<Song> tmpSongs = new ArrayList<>(songs);
            songs.clear();
            for (Song song : tmpSongs) {
                if (song.isFavourite()) {
                    songs.add(song);
                }
            }
        }
    }

    private void filterSongsByVideos() {
        if (containingVideosSwitch != null && containingVideosSwitch.isChecked()) {
            ArrayList<Song> tmpSongs = new ArrayList<>(songs);
            songs.clear();
            for (Song song : tmpSongs) {
                if (song.getYoutubeUrl() != null) {
                    songs.add(song);
                }
            }
        }
    }

    private void filterSongsByCollection() {
        HashMap<String, Song> hashMap = getStringSongHashMap();
        HashMap<String, Boolean> addedSongsHashMap = new HashMap<>(hashMap.size());
        songs.clear();
        clearSongCollectionForSongs(hashMap.values());
        if (ifAtLeastOneSongCollectionIsSelected()) {
            for (SongCollection songCollection : songCollections) {
                if (songCollection.isSelected()) {
                    addSongCollectionElementsByHashMap(songCollection, hashMap, addedSongsHashMap, true);
                }
            }
        } else {
            for (SongCollection songCollection : songCollections) {
                addSongCollectionElementsByHashMap(songCollection, hashMap, addedSongsHashMap, false);
            }
            addRestOfSongs(hashMap);
        }
    }

    private void addSongCollectionElementsByHashMap(SongCollection songCollection, HashMap<String, Song> hashMap, HashMap<String, Boolean> addedSongsHashMap, boolean filteringByCollection) {
        for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
            String songUuid = songCollectionElement.getSongUuid();
            if (hashMap.containsKey(songUuid)) {
                Song song = pairSongWithSongCollectionElement_hashMap(hashMap, songCollection, songCollectionElement, songUuid, filteringByCollection);
                if (!addedSongsHashMap.containsKey(songUuid)) {
                    songs.add(song);
                    addedSongsHashMap.put(songUuid, true);
                }
            }
        }
    }

    private void addRestOfSongs(HashMap<String, Song> hashMap) {
        for (Song song : hashMap.values()) {
            if (song.getSongCollectionElements().isEmpty()) {
                songs.add(song);
            }
        }
    }

    @NonNull
    public static Song pairSongWithSongCollectionElement_hashMap(HashMap<String, Song> hashMap, SongCollection songCollection, SongCollectionElement songCollectionElement, String songUuid, boolean filteringByCollection) {
        Song song = hashMap.get(songUuid);
        pairSongWithSongCollectionElement(song, songCollection, songCollectionElement, filteringByCollection);
        return song;
    }

    public static void pairSongWithSongCollectionElement(Song song, SongCollection songCollection, SongCollectionElement songCollectionElement, boolean filteringByCollection) {
        if (filteringByCollection && !songCollection.isSelected()) {
            return;
        }
        song.addToSongCollections(songCollection);
        song.addToSongCollectionElements(songCollectionElement);
        songCollectionElement.setSong(song);
    }

    private void clearSongCollectionForSongs(Collection<Song> songs) {
        for (Song song : songs) {
            song.setSongCollections(null);
            song.setSongCollectionElements(null);
        }
    }

    private boolean ifAtLeastOneSongCollectionIsSelected() {
        for (SongCollection songCollection : songCollections) {
            if (songCollection.isSelected()) {
                return true;
            }
        }
        return false;
    }

    private void createSelectLanguagePopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.content_select_languages, null);
        selectLanguagePopupWindow = new PopupWindow(
                customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            selectLanguagePopupWindow.setElevation(5.0f);
        }
        Button selectButton = customView.findViewById(R.id.selectButton);
        selectButton.setOnClickListener(view -> {
            languageRepository.save(languages);
            filter();
            lastSearchedText = "";
            loadAll();
            selectLanguagePopupWindow.dismiss();
            filterPopupWindow.dismiss();
        });
        LanguageAdapter dataAdapter = new LanguageAdapter(mainActivity,
                R.layout.activity_language_checkbox_row, languages,
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        ListView listView = customView.findViewById(R.id.listView);
        listView.setAdapter(dataAdapter);
        selectLanguagePopupWindow.setBackgroundDrawable(new BitmapDrawable());
        selectLanguagePopupWindow.setOutsideTouchable(true);
    }

    private void filterSongsByLanguage() {
        songs.clear();
        if (isOneLanguageSelected()) {
            for (Language language : languages) {
                if (language.isSelected()) {
                    for (Song song : language.getSongs()) {
                        if (!song.isAsDeleted()) {
                            songs.add(song);
                        }
                    }
                }
            }
        }
        if (songs.isEmpty()) {
            setSongs(songRepository.findAllExceptAsDeleted());
        }
    }

    private boolean isOneLanguageSelected() {
        for (Language language : languages) {
            if (language.isSelected()) {
                return true;
            }
        }
        return false;
    }

    public void setShortCollectionName(boolean shortCollectionName) {
        this.shortCollectionName = shortCollectionName;
    }

    public void onLanguageButtonClick(View view) {
        if (selectLanguagePopupWindow != null) {
            selectLanguagePopupWindow.dismiss();
        } else {
            createSelectLanguagePopup();
        }
        hideKeyboard();
        selectLanguagePopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
    }

    public void onCollectionButtonClick(View view) {
        if (collectionPopupWindow != null) {
            collectionPopupWindow.dismiss();
        } else {
            createCollectionPopup();
        }
        hideKeyboard();
        sortCollectionBySelectedLanguages();
        collectionPopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
    }

    public void onGoogleSignIn(View view) {
        SyncFavouriteInGoogleDrive syncFavouriteInGoogleDrive = new SyncFavouriteInGoogleDrive(new GoogleSignInIntent() {
            @Override
            public void task(Intent signInIntent) {
                if (!alreadyTried) {
                    startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
                    alreadyTried = true;
                } else {
                    GoogleSignIn.requestPermissions(
                            MainActivity.this,
                            REQUEST_CODE_SIGN_IN,
                            GoogleSignIn.getLastSignedInAccount(MainActivity.this),
                            Drive.SCOPE_APPFOLDER);
                }
            }
        }, this, songs, favouriteSongs);
        syncFavouriteInGoogleDrive.signIn();
    }

    private void showToaster(String s, int lengthLong) {
        Toast.makeText(this, s, lengthLong).show();
    }

    public void onClearAllQueueClick(View view) {
        List<QueueSong> all = queueSongRepository.findAll();
        queueSongRepository.deleteAll(all);
        memory.getQueue().clear();
        queueListView.invalidateViews();
        hideBottomSheet();
        linearLayout.setPadding(0, 0, 0, 0);
    }

    private void hideBottomSheet() {
        setBottomSheetHideable();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void onExpandBottomSheetClick(View view) {
        List<QueueSong> queue = memory.getQueue();
        if (queue == null || queue.isEmpty()) {
            hideBottomSheet();
            return;
        }
        bottomSheetBehavior.setState(
                bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ?
                        BottomSheetBehavior.STATE_COLLAPSED :
                        BottomSheetBehavior.STATE_EXPANDED);
    }

    public void onShareQueue(View view) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        String string = getString(R.string.queue);
        share.putExtra(Intent.EXTRA_SUBJECT, string);
        share.putExtra(Intent.EXTRA_TITLE, string);
        StringBuilder ids = new StringBuilder();
        for (QueueSong queueSong : memory.getQueue()) {
            Song song = queueSong.getSong();
            if (song == null) {
                continue;
            }
            String uuid = song.getUuid();
            if (uuid != null) {
                ids.append(",").append(uuid);
            }
        }
        share.putExtra(Intent.EXTRA_TEXT, BASE_URL + "queue?ids=" + ids.substring(1, ids.length()));
        startActivity(Intent.createChooser(share, "Share queue!"));
    }

    public void onSaveQueue(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.content_save_queue, null);
        saveQueuePopupWindow = new PopupWindow(
                customView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            saveQueuePopupWindow.setElevation(5.0f);
        }
        Button closeButton = customView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(view1 -> saveQueuePopupWindow.dismiss());
        ListView listView = customView.findViewById(R.id.listView);
        SongListRepositoryImpl songListRepository = new SongListRepositoryImpl(this);
        final List<SongList> songLists = songListRepository.findAll();
        List<String> all = new ArrayList<>(songLists.size());
        for (SongList songList : songLists) {
            all.add(songList.getTitle());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                all);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view12, position, id) -> {
            SongList songList = songLists.get(position);
            List<QueueSong> queue = memory.getQueue();
            List<SongListElement> songListElements = songList.getSongListElements();
            LongSparseArray<Object> hashMap = new LongSparseArray<>(songListElements.size());
            for (SongListElement element : songListElements) {
                Song song = element.getSong();
                if (song == null) {
                    continue;
                }
                hashMap.put(song.getId(), song);
            }
            boolean duplicate = false;
            for (QueueSong queueSong : queue) {
                if (hashMap.get(queueSong.getSong().getId()) != null) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                addQueueToList(songList, queue, false, hashMap);
            } else {
                askAddDuplicates(songList, queue, hashMap);
            }
            saveQueuePopupWindow.dismiss();
        });
        saveQueuePopupWindow.setBackgroundDrawable(new BitmapDrawable());
        saveQueuePopupWindow.setOutsideTouchable(true);
        saveQueuePopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
    }

    private void addQueueToList(SongList songList, List<QueueSong> queue, boolean skipDuplicate, LongSparseArray<Object> hashMap) {
        List<SongListElement> songListElements = songList.getSongListElements();
        int count = 0;
        for (QueueSong queueSong : queue) {
            if (skipDuplicate && hashMap.get(queueSong.getSong().getId()) != null) {
                continue;
            }
            SongListElement element = new SongListElement();
            element.setSong(queueSong.getSong());
            element.setNumber(songListElements.size());
            element.setSongList(songList);
            songListElements.add(element);
            ++count;
        }
        SongListElementRepositoryImpl songListElementRepository = new SongListElementRepositoryImpl(this);
        songListElementRepository.save(songListElements);
        SongListRepositoryImpl songListRepository = new SongListRepositoryImpl(this);
        songList.setModifiedDate(new Date());
        songListRepository.save(songList);
        showToaster(count + " " + getString(R.string.songs_added_to) + " " + songList.getTitle(), Toast.LENGTH_LONG);
    }

    private void askAddDuplicates(final SongList songList, final List<QueueSong> queue, final LongSparseArray<Object> hashMap) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.content_add_duplicate, null);
        addDuplicatesPopupWindow = new PopupWindow(
                customView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            addDuplicatesPopupWindow.setElevation(5.0f);
        }
        Button skipButton = customView.findViewById(R.id.skipButton);
        skipButton.setOnClickListener(view -> {
            addQueueToList(songList, queue, true, hashMap);
            addDuplicatesPopupWindow.dismiss();
        });
        Button addButton = customView.findViewById(R.id.okButton);
        addButton.setOnClickListener(view -> {
            addQueueToList(songList, queue, false, hashMap);
            addDuplicatesPopupWindow.dismiss();
        });
        addDuplicatesPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        addDuplicatesPopupWindow.setOutsideTouchable(true);
        addDuplicatesPopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
    }

    public void onNewSongListClick(View view) {
        saveQueuePopupWindow.dismiss();
        Intent intent = new Intent(this, NewSongListActivity.class);
        intent.putExtra("saveQueue", true);
        startActivity(intent);
    }

    public void onChangeViewButtonClick(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        view_mode = sharedPreferences.getInt("view_mode", 0);
        switch (view_mode) {
            case 0:
                sharedPreferences.edit().putInt("view_mode", 1).apply();
                break;
            case 1:
                sharedPreferences.edit().putInt("view_mode", 0).apply();
                break;
        }
        loadAll();
        setView();
    }

    private void setView() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        int view_mode = sharedPreferences.getInt("view_mode", 0);
        ImageView changeViewButton = findViewById(R.id.changeViewButton);
        CenterLayoutManager layoutManager = (CenterLayoutManager) songListView.getLayoutManager();
        switch (view_mode) {
            case 0:
                if (viewPager != null) {
                    int currentItem = viewPager.getCurrentItem();
                    if (currentItem >= 0 && values.size() > currentItem) {
                        songListView.scrollToPosition(currentItem);
                    }
                }
                changeViewButton.setImageResource(R.drawable.ic_view_array_black_24dp);
                findViewById(R.id.array_view).setVisibility(View.GONE);
                layoutManager.setOrientation(CenterLayoutManager.VERTICAL);
                break;
            case 1:
                if (songListView != null) {
                    int position = firstVisibleItemPosition;
                    if (position >= 0 && values.size() > position) {
                        viewPager.setCurrentItem(position);
                        songListView.scrollToPosition(position);
                        songListView.smoothScrollToPosition(position);
                    }
                }
                changeViewButton.setImageResource(R.drawable.ic_view_headline_black_24dp);
                findViewById(R.id.array_view).setVisibility(View.VISIBLE);
                layoutManager.setOrientation(CenterLayoutManager.HORIZONTAL);
                break;
        }
    }

    public interface Listener {
        void onGrab(int position, LinearLayout row);
    }

    public interface OnItemClickListener {
        void onItemClick(Song song, int position);

        void onLongClick(Song song, int position);
    }

    public static class CenterLayoutManager extends LinearLayoutManager {

        CenterLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        private static class CenterSmoothScroller extends LinearSmoothScroller {

            CenterSmoothScroller(Context context) {
                super(context);
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
            }
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView ordinalNumberTextView;
        TextView titleTextView;
        View imageView;
        View parentLayout;

        MyViewHolder(View v) {
            super(v);
            titleTextView = v.findViewById(R.id.titleTextView);
            ordinalNumberTextView = v.findViewById(R.id.ordinalNumberTextView);
            titleTextView = v.findViewById(R.id.titleTextView);
            imageView = v.findViewById(R.id.starImageView);
            parentLayout = v.findViewById(R.id.parentLayout);
        }

        void bind(final Song song, final OnItemClickListener listener, final int position) {
            parentLayout.setOnClickListener(v -> listener.onItemClick(song, position));
            parentLayout.setOnLongClickListener(v -> {
                listener.onLongClick(song, position);
                return true;
            });
        }
    }

    private class SongCollectionAdapter extends ArrayAdapter<SongCollection> {

        private final List<SongCollection> songCollectionList;

        SongCollectionAdapter(Context context, int textViewResourceId,
                              List<SongCollection> songCollectionList) {
            super(context, textViewResourceId, songCollectionList);
            this.songCollectionList = new ArrayList<>();
            this.songCollectionList.addAll(songCollectionList);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            SongCollectionAdapter.ViewHolder holder;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.checkbox_row, null);

                holder = new SongCollectionAdapter.ViewHolder();
                holder.textView = convertView.findViewById(R.id.code);
                holder.checkBox = convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.checkBox.setOnClickListener(view -> {
                    CheckBox checkBox = (CheckBox) view;
                    SongCollection songCollection = (SongCollection) checkBox.getTag();
                    songCollection.setSelected(checkBox.isChecked());
                });
            } else {
                holder = (SongCollectionAdapter.ViewHolder) convertView.getTag();
            }
            if (0 <= position && position < songCollectionList.size()) {
                SongCollection songCollection = songCollectionList.get(position);
                holder.checkBox.setText(songCollection.getName());
                holder.checkBox.setChecked(songCollection.isSelected());
                holder.checkBox.setTag(songCollection);
            }
            return convertView;
        }

        public void setList(List<SongCollection> filteredSongCollections) {
            songCollectionList.clear();
            songCollectionList.addAll(filteredSongCollections);
            this.notifyDataSetChanged();
        }

        private class ViewHolder {
            TextView textView;
            CheckBox checkBox;
        }

    }

    public class SongAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private final OnItemClickListener listener;
        private final List<Song> songList;

        SongAdapter(List<Song> songList, OnItemClickListener onItemClickListener) {
            this.songList = new ArrayList<>();
            this.songList.addAll(songList);
            this.listener = onItemClickListener;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // create a new view
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.content_song_list_row, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            View parentLayout = holder.parentLayout;
            LayoutParams layoutParams = parentLayout.getLayoutParams();
            if (view_mode == 0) {
                layoutParams.width = LayoutParams.MATCH_PARENT;
            } else {
                layoutParams.width = LayoutParams.WRAP_CONTENT;
            }
            parentLayout.setLayoutParams(layoutParams);
            Song song = songList.get(position);
            holder.bind(song, listener, position);
            holder.imageView.setVisibility(song.isFavourite() ? View.VISIBLE : View.INVISIBLE);
            holder.ordinalNumberTextView.setText(getOrdinalNumberText(song, MainActivity.this.shortCollectionName));
            holder.titleTextView.setText(song.getTitle());
            holder.titleTextView.setTag(song);
        }

        @Override
        public int getItemCount() {
            return songList.size();
        }

        @SuppressLint("NotifyDataSetChanged")
        void setSongList(List<Song> songs) {
            songList.clear();
            songList.addAll(songs);
            this.notifyDataSetChanged();
        }

    }

    public static String getOrdinalNumberText(Song song, boolean shortCollectionName) {
        StringBuilder text = new StringBuilder();
        for (SongCollectionElement songCollectionElement : song.getSongCollectionElements()) {
            SongCollection songCollection = songCollectionElement.getSongCollection();
            String collectionName;
            if (shortCollectionName) {
                if (text.length() != 0) {
                    text.append(", ");
                }
                collectionName = songCollection.getShortName();
            } else {
                if (text.length() != 0) {
                    text.append("\n");
                }
                collectionName = songCollection.getName();
            }
            text.append(collectionName).append(" ").append(songCollectionElement.getOrdinalNumber());
        }
        return text.toString();
    }
}
