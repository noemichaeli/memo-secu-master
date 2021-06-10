package com.app.memosecu;

import java.io.IOException;
import java.security.GeneralSecurityException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.memosecu.crypto.InvalidPasswordException;
import com.app.memosecu.database.PasswordDatabase;
import com.app.memosecu.database.ProblemReadingDatabaseFile;

public class ChangeMasterPassword extends Activity implements OnClickListener {

    private EditText existingPassword;
    private EditText newPassword;
    private EditText newPasswordConfirmation;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_master_password);
    
        existingPassword = (EditText) findViewById(R.id.existing_master_password);
        newPassword = (EditText) findViewById(R.id.new_master_password);
        newPasswordConfirmation = (EditText) findViewById(R.id.new_master_password_confirm);
    
        // Make this class the listener for the click event on the OK button
        Button okButton = (Button) findViewById(R.id.change_master_password_ok_button);
        okButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.change_master_password_ok_button:
            // Check the two new password match
            if (existingPassword.getText().length() == 0) {
                Toast.makeText(this, R.string.request_master_password, Toast.LENGTH_SHORT).show();
            } else if (!newPassword.getText().toString().equals(newPasswordConfirmation.getText().toString())) {
                Toast.makeText(this, R.string.new_passwords_dont_match, Toast.LENGTH_SHORT).show();
            } else if (newPassword.getText().length() < CreateNewDatabase.MIN_PASSWORD_LENGTH) {
                String passwordTooShortResStr = getString(R.string.password_too_short);
                String resultsText = String.format(passwordTooShortResStr, CreateNewDatabase.MIN_PASSWORD_LENGTH);
                Toast.makeText(this, resultsText, Toast.LENGTH_SHORT).show();
            } else {
                new DecryptAndSaveDatabaseAsyncTask().execute();
            }
            break;
        }
    }

    private MSApplication getUPMApplication() {
        return (MSApplication) getApplication();
    }

    private PasswordDatabase getPasswordDatabase() {
        return getUPMApplication().getPasswordDatabase();
    }

    public class DecryptAndSaveDatabaseAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ChangeMasterPassword.this, "", getString(R.string.saving_database));
        }
        
        @Override
        protected Integer doInBackground(Void... params) {
            Integer messageCode = null;
            try {
                // Attempt to decrypt the database so-as to test the password
                char[] password = existingPassword.getText().toString().toCharArray();
                new PasswordDatabase(Utilities.getDatabaseFile(ChangeMasterPassword.this), password);

                // Re-encrypt the database
                getPasswordDatabase().changePassword(newPassword.getText().toString().toCharArray());
                synchronized (MSApplication.sDataLock) {
                    getPasswordDatabase().save();
                }

                // Ask the BackupManager to backup the database using
                // Google's cloud backup service.
                Log.i("ChangeMasterPassword", "Calling BackupManager().dataChanged()");
                getUPMApplication().getBackupManager().dataChanged();

                // We're finished with this activity so take it off the stack
                finish();
            } catch (InvalidPasswordException e) {
                Log.e("ChangeMasterPassword", e.getMessage(), e);
                messageCode = R.string.invalid_password;
            } catch (IOException e) {
                Log.e("ChangeMasterPassword", e.getMessage(), e);
                messageCode = R.string.generic_error;
            } catch (GeneralSecurityException e) {
                Log.e("ChangeMasterPassword", e.getMessage(), e);
                messageCode = R.string.generic_error;
            } catch (ProblemReadingDatabaseFile e) {
                Log.e("ChangeMasterPassword", e.getMessage(), e);
                messageCode = R.string.generic_error;
            }
            
            return messageCode;
        }
        
        protected void onPostExecute(Integer messageCode) {
            progressDialog.dismiss();

            if (messageCode != null) {
                Toast.makeText(ChangeMasterPassword.this, messageCode, Toast.LENGTH_SHORT).show();
                if (messageCode == R.string.invalid_password) {
                    // Set focus back to the password and select all characters
                    existingPassword.requestFocus();
                    existingPassword.selectAll();
                }
            }
            
        }

    }

}
