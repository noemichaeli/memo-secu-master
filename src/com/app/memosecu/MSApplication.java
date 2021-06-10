package com.app.memosecu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

import android.app.Activity;
import android.app.Application;
import android.app.backup.BackupManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.app.memosecu.database.PasswordDatabase;

/**
 * This class replaces the regular Application class in the application and
 * allows us to store data at the application level.
 */
public class MSApplication extends Application {

    private PasswordDatabase passwordDatabase;
    private Date timeOfLastSync;
    private BackupManager backupManager;
    public static final Object[] sDataLock = new Object[0];

    @Override
    public void onCreate() {
        super.onCreate();
        backupManager = new BackupManager(this);
    }

    public BackupManager getBackupManager() {
        return backupManager;
    }

    public Date getTimeOfLastSync() {
        return timeOfLastSync;
    }

    public void setTimeOfLastSync(Date timeOfLastSync) {
        this.timeOfLastSync = timeOfLastSync;
    }

    public void setPasswordDatabase(PasswordDatabase passwordDatabase) {
        this.passwordDatabase = passwordDatabase;
    }

    public PasswordDatabase getPasswordDatabase() {
        return passwordDatabase;
    }

    protected boolean copyFile(File source, File dest, Activity activity) {
        boolean successful = false;

        FileChannel sourceChannel = null;
        FileChannel destinationChannel = null;
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(source);
            sourceChannel = is.getChannel();

            File destFile = null;
            if (dest.isDirectory()) {
                destFile = new File(dest, source.getName());
            } else {
                destFile = dest;
            }

            os = new FileOutputStream(destFile);
            destinationChannel = os.getChannel();
            destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());

            successful=true;
        } catch (IOException e) {
            Log.e(activity.getClass().getName(), getString(R.string.file_problem), e);
            Toast.makeText(activity, R.string.file_problem, Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (sourceChannel != null) {
                    sourceChannel.close();
                }
                if (is != null) {
                    is.close();
                }
                if (destinationChannel != null) {
                    destinationChannel.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                Log.e(activity.getClass().getName(), getString(R.string.file_problem), e);
                Toast.makeText(activity, R.string.file_problem, Toast.LENGTH_LONG).show();
            }
        }

        return successful;
    }

    protected void restoreDatabase(Activity activity) {
        deleteDatabase(activity);
        File fileOnSDCard = new File(Environment.getExternalStorageDirectory(), Utilities.DEFAULT_DATABASE_FILE);
        File databaseFile = Utilities.getDatabaseFile(activity);
        ((MSApplication) activity.getApplication()).copyFile(fileOnSDCard, databaseFile, activity);
    }

    protected void deleteDatabase(Activity activity) {
        Utilities.getDatabaseFile(activity).delete();
        Utilities.setDatabaseFileName(null, activity);
    }

}
