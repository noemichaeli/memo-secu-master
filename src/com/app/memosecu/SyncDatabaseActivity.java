package com.app.memosecu;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.app.memosecu.database.PasswordDatabase;

public abstract class SyncDatabaseActivity extends Activity {

    private static final int ENTER_PW_REQUEST_CODE = 222;

    public static final int RESULT_REFRESH = 1;

    public static interface SyncResult {
        int IN_SYNC = 0;
        int UPLOAD_LOCAL = 1;
        int KEEP_REMOTE = 2;
    }

    protected File downloadedDatabaseFile;

    protected abstract void uploadDatabase();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ENTER_PW_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                UIUtilities.showToast(this, R.string.enter_password_cancalled);
                finish();
            } else {
                syncDb(EnterMasterPassword.decryptedPasswordDatabase);
            }
        }
    }

    /**
     * Check if the downloaded DB is more recent than the current db.
     * If it is the replace the current DB with the downloaded one and reload
     * the accounts listview
     */
    protected int syncDb(PasswordDatabase dbDownloadedOnSync) {
        int syncResult = SyncResult.IN_SYNC;
        MSApplication app = (MSApplication) getApplication();
        if (dbDownloadedOnSync == null || dbDownloadedOnSync.getRevision() < app.getPasswordDatabase().getRevision()) {
            uploadDatabase();
            syncResult = SyncResult.UPLOAD_LOCAL;
        } else if (dbDownloadedOnSync.getRevision() > app.getPasswordDatabase().getRevision()) {
            app.copyFile(downloadedDatabaseFile, Utilities.getDatabaseFile(this), this);
            app.setPasswordDatabase(dbDownloadedOnSync);
            dbDownloadedOnSync.setDatabaseFile(Utilities.getDatabaseFile(this));
            setResult(RESULT_REFRESH);
            syncResult = SyncResult.KEEP_REMOTE;
            UIUtilities.showToast(this, R.string.new_db_downloaded);

            // Ask the BackupManager to backup the database using
            // Google's cloud backup service.
            Log.i("SyncDatabaseActivity", "Calling BackupManager().dataChanged()");
            app.getBackupManager().dataChanged();

            finish();
        } else if (dbDownloadedOnSync.getRevision() == app.getPasswordDatabase().getRevision()) {
            UIUtilities.showToast(this, R.string.db_uptodate);
            finish();
        }
        app.setTimeOfLastSync(new Date());
        if (downloadedDatabaseFile != null) {
            downloadedDatabaseFile.delete();
        }
        return syncResult;
    }

    protected PasswordDatabase getPasswordDatabase() {
        return ((MSApplication) getApplication()).getPasswordDatabase();
    }

}
