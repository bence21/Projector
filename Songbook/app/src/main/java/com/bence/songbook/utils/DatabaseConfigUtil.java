package com.bence.songbook.utils;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;

/**
 * To run this you need to edit the configurations. You need to <b>build with no
 * error checks</b>. You need to set working directory to
 * <b>$MODULE_DIR$\src\main</b>
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {
//    private static final Class<?>[] classes = new Class[]{Song.class};

    public static void main(final String[] args) throws SQLException, IOException {
        writeConfigFile("ormlite_config.txt");
    }
}
