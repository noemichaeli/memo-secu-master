package com.app.memosecu;

import java.io.IOException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import com.app.memosecu.database.PasswordDatabase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class SaveDatabaseAsyncTask extends AsyncTask<PasswordDatabase, Void, String> {

    private ProgressDialog progressDialog;
    private Activity activity;
    private Callback callback;

    public SaveDatabaseAsyncTask(Activity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.saving_database));
    }

    @Override
    protected String doInBackground(PasswordDatabase... params) {
        String message = null;

        try {
            synchronized (MSApplication.sDataLock) {
                params[0].save();
            }

            // Ask the BackupManager to backup the database using
            // Google's cloud backup service.
            Log.i("SaveDatabaseAsyncTask", "Calling BackupManager().dataChanged()");
            ((MSApplication) activity.getApplication()).getBackupManager().dataChanged();
        } catch (IllegalBlockSizeException e) {
            Log.e("SaveDatabaseAsyncTask", e.getMessage(), e);
            message = String.format(activity.getString(R.string.problem_saving_db), e.getMessage());
        } catch (BadPaddingException e) {
            Log.e("SaveDatabaseAsyncTask", e.getMessage(), e);
            message = String.format(activity.getString(R.string.problem_saving_db), e.getMessage());
        } catch (IOException e) {
            Log.e("SaveDatabaseAsyncTask", e.getMessage(), e);
            message = String.format(activity.getString(R.string.problem_saving_db), e.getMessage());
        }

        return message;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            UIUtilities.showToast(activity, result, true);
        }

        progressDialog.dismiss();
        
        callback.execute();
    }

}
