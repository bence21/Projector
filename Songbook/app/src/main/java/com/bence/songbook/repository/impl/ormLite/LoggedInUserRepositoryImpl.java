package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.LoggedInUser;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.LoggedInUserRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;

import java.sql.SQLException;

public class LoggedInUserRepositoryImpl extends BaseRepositoryImpl<LoggedInUser> implements LoggedInUserRepository {
    private static final String TAG = LoggedInUserRepositoryImpl.class.getSimpleName();

    public LoggedInUserRepositoryImpl(Context context) {
        super(LoggedInUser.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            CustomDao<LoggedInUser, Long> loggedInUserDao = databaseHelper.getLoggedInUserDao();
            super.setDao(loggedInUserDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize LoggedInUserRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }
}
