package com.app.memosecu;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.app.memosecu.database.PasswordDatabase;

public class Utilities {

    public static final String DEFAULT_DATABASE_FILE = "ms.db";
    public static final String PREFS_DB_FILE_NAME = "DB_FILE_NAME";


    public static File getDatabaseFile(Activity activity) {
        String dbFileName = getDatabaseFileName(activity);
        if (dbFileName == null || dbFileName.equals("")) {
            return new File(activity.getFilesDir(), DEFAULT_DATABASE_FILE);
        } else {
            return new File(activity.getFilesDir(), dbFileName);
        }
    }

    public static String getDatabaseFileName(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Prefs.PREFS_NAME, Activity.MODE_PRIVATE);
        return settings.getString(PREFS_DB_FILE_NAME, DEFAULT_DATABASE_FILE);
    }

    public static String getSyncMethod(Activity activity) {
        MSApplication app = (MSApplication) activity.getApplication();
        String remoteHTTPLocation = app.getPasswordDatabase().getDbOptions().getRemoteLocation();
        SharedPreferences settings = activity.getSharedPreferences(Prefs.PREFS_NAME, Activity.MODE_PRIVATE);
        return getSyncMethod(settings, remoteHTTPLocation);
    }

    /**
     * If we've upgraded from an older version of MS the preference
     * 'sync.method' may not exist. In this case we should check if the
     * database has a value for sharedURL. If it does it means the database
     * has been configured to use "http" as the sync method
     * @param settings
     * @param remoteHTTPLocation
     * @return
     */
    public static String getSyncMethod(SharedPreferences settings, String remoteHTTPLocation) {
        String syncMethod = settings.getString(Prefs.SYNC_METHOD, null);

        if (syncMethod == null) {
            if (remoteHTTPLocation != null) {
                syncMethod = Prefs.SyncMethod.HTTP;
            } else {
                syncMethod = Prefs.SyncMethod.DISABLED;
            }
        }

        return syncMethod;
    }

    public static void setDatabaseFileName(String dbFileName, Activity activity) {
        SharedPreferences settings = activity.getSharedPreferences(Prefs.PREFS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_DB_FILE_NAME, dbFileName);
        editor.commit();
    }

    public static boolean isSyncRequired(Activity activity) {
        MSApplication app = (MSApplication) activity.getApplication();
        PasswordDatabase db = app.getPasswordDatabase();
        Date timeOfLastSync = app.getTimeOfLastSync();

        boolean syncRequired = false;

        if (db.getDbOptions().getRemoteLocation() != null && !db.getDbOptions().getRemoteLocation().equals("")) {
            if (timeOfLastSync == null || System.currentTimeMillis() - timeOfLastSync.getTime() > (5 * 60 * 1000)) {
                syncRequired = true;
            }
        }

        return syncRequired;
    }

    public static String getConfig(Context context, String fileName, String keyName) {
        SharedPreferences settings =
            context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return settings.getString(keyName, null);
    }

}
