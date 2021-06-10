package com.app.memosecu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class FullAccountList extends AccountsList {

    private static final int CONFIRM_RESTORE_DIALOG = 0;
    private static final int CONFIRM_OVERWRITE_BACKUP_FILE = 1;
    private static final int DIALOG_ABOUT = 2;
    private static final int CONFIRM_DELETE_DB_DIALOG = 3;

    public static final int RESULT_EXIT = 0;
    public static final int RESULT_ENTER_PW = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        registerForContextMenu(getListView());
        populateAccountList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode) {
            case AddEditAccount.EDIT_ACCOUNT_REQUEST_CODE:
            case ViewAccountDetails.VIEW_ACCOUNT_REQUEST_CODE:
                if (resultCode == AddEditAccount.EDIT_ACCOUNT_RESULT_CODE_TRUE) {
                    populateAccountList();
                }
                break;
        }
    }

    private void populateAccountList() {
        if (getPasswordDatabase() == null) {
            // If the MS process was restarted since AppEntryActivity was last
            // run then databaseFileToDecrypt won't be set so set it here.
            EnterMasterPassword.databaseFileToDecrypt = Utilities.getDatabaseFile(this);

            setResult(RESULT_ENTER_PW);
            finish();
        } else {
            setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getPasswordDatabase().getAccountNames()));
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (getPasswordDatabase() == null) {
            // If the MemoSecu process was restarted since AppEntryActivity was last
            // run then databaseFileToDecrypt won't be set so set it here.
            EnterMasterPassword.databaseFileToDecrypt = Utilities.getDatabaseFile(this);

            setResult(RESULT_ENTER_PW);
            finish();
        } else {
            menu.findItem(R.id.sync).setEnabled(!Utilities.getSyncMethod(this).equals(Prefs.SyncMethod.DISABLED));
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.confirm_exit_title)
            .setMessage(R.string.confirm_exit_message)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FullAccountList.this.setResult(RESULT_EXIT);
                    FullAccountList.this.finish();
                }
            })
            .setNegativeButton(R.string.no, null)
            .show();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }



    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean optionConsumed = false;

        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                optionConsumed = true;
                break;
            case R.id.add:
                if (Utilities.isSyncRequired(this)) {
                    UIUtilities.showToast(this, R.string.sync_required);
                } else {
                    Intent i = new Intent(FullAccountList.this, AddEditAccount.class);
                    i.putExtra(AddEditAccount.MODE, AddEditAccount.ADD_MODE);
                    startActivityForResult(i, AddEditAccount.EDIT_ACCOUNT_REQUEST_CODE);
                }
                break;
            case R.id.change_master_password:
                if (Utilities.isSyncRequired(this)) {
                    UIUtilities.showToast(this, R.string.sync_required);
                } else {
                    startActivity(new Intent(FullAccountList.this, ChangeMasterPassword.class));
                }
                break;
            case R.id.restore:
                // Check to ensure there's a file to restore
                File restoreFile = new File(Environment.getExternalStorageDirectory(), Utilities.DEFAULT_DATABASE_FILE);
                if (restoreFile.exists()) {
                    showDialog(CONFIRM_RESTORE_DIALOG);
                } else {
                    String messageRes = getString(R.string.restore_file_doesnt_exist);
                    String message = String.format(messageRes, restoreFile.getAbsolutePath());
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.backup:
                // If there's already a backup file prompt the user if they want to overwrite
                File backupFile = new File(Environment.getExternalStorageDirectory(), Utilities.DEFAULT_DATABASE_FILE);
                if (backupFile.exists()) {
                    showDialog(CONFIRM_OVERWRITE_BACKUP_FILE);
                } else {
                    backupDatabase();
                }
                break;
            case R.id.about:
                showDialog(DIALOG_ABOUT);
                break;
            case R.id.preferences:
                startActivity(new Intent(this, Prefs.class));
                break;
            case R.id.delete_db:
                showDialog(CONFIRM_DELETE_DB_DIALOG);
                break;
        }

        return optionConsumed;
    }

    @SuppressLint("StringFormatMatches")
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        switch(id) {
        case CONFIRM_RESTORE_DIALOG:
            dialogBuilder.setMessage(getString(R.string.confirm_restore_overwrite))
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       ((MSApplication) getApplication()).restoreDatabase(FullAccountList.this);
                       // Clear the activity stack and bring up AppEntryActivity
                       // This is effectively restarting the application
                       Intent i = new Intent(FullAccountList.this, AppEntryActivity.class);
                       i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       startActivity(i);
                       finish();
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });
            break;
        case CONFIRM_OVERWRITE_BACKUP_FILE:
            File backupFile = new File(Environment.getExternalStorageDirectory(), Utilities.DEFAULT_DATABASE_FILE);
            String messageRes = getString(R.string.backup_file_exists);
            String message = String.format(messageRes, backupFile.getAbsolutePath());

            dialogBuilder.setMessage(message)
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       backupDatabase();
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });
            break;
        case DIALOG_ABOUT:
            PackageInfo pinfo;
            String versionName = "<unknown>";
            int versionCode = 0;
            try {
                pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionName = pinfo.versionName;
                versionCode = pinfo.versionCode;
            } catch (NameNotFoundException e) {
                Log.e("FullAccountList", e.getMessage(), e);
            }

            View v = LayoutInflater.from(this).inflate(R.layout.dialog, null);
            TextView text = v.findViewById(R.id.dialogText);
            text.setText(getString(R.string.aboutText, versionName, versionCode));

            dialogBuilder
                .setTitle(R.string.about)
                .setIcon(android.R.drawable.ic_menu_info_details)
                .setPositiveButton(R.string.homepage, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse(getString(R.string.homepageUrl));
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    }
                })
                .setNegativeButton(R.string.close, null)
                .setView(v);
            break;
        case CONFIRM_DELETE_DB_DIALOG:
            dialogBuilder.setMessage(getString(R.string.confirm_delete_db))
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    deleteDatabase();
                    // Clear the activity stack and bring up AppEntryActivity
                    // This is effectively restarting the application
                    Intent i = new Intent(FullAccountList.this, AppEntryActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                     dialog.cancel();
                }
            });
            break;
        }

        return dialogBuilder.create();
    }

    private void deleteDatabase() {
        Utilities.getDatabaseFile(this).delete();
        Utilities.setDatabaseFileName(null, this);
    }

    private void backupDatabase() {
        File fileOnSDCard = new File(Environment.getExternalStorageDirectory(), Utilities.DEFAULT_DATABASE_FILE);
        File databaseFile = Utilities.getDatabaseFile(this);
        if (((MSApplication) getApplication()).copyFile(databaseFile, fileOnSDCard, this)) {
            String message = String.format(getString(R.string.backup_complete), fileOnSDCard.getAbsolutePath());
            UIUtilities.showToast(this, message, false);
        }
    }

}
