package com.app.memosecu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.app.memosecu.database.AccountInformation;
import com.app.memosecu.database.PasswordDatabase;

public class ViewAccountDetails extends Activity {

    public static AccountInformation account;

    private static final int CONFIRM_DELETE_DIALOG = 0;
    public static final int VIEW_ACCOUNT_REQUEST_CODE = 224;

    private int editAccountResultCode = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE);
        setContentView(R.layout.view_account_details);
    }

    /**
     * This method is called when returning from the edit activity. Since the
     * account details may have been changed we should repopulate the view 
     */
    @Override
    protected void onResume() {
        super.onResume();
        // If the account is null then finish (may be null because activity was
        // recreated since it was last visible
        if (account == null) {
            finish();
        } else {
            populateView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.edit:
            if (Utilities.isSyncRequired(this)) {
                UIUtilities.showToast(this, R.string.sync_required);
            } else {
                Intent i = new Intent(ViewAccountDetails.this, AddEditAccount.class);
                i.putExtra(AddEditAccount.MODE, AddEditAccount.EDIT_MODE);
                i.putExtra(AddEditAccount.ACCOUNT_TO_EDIT, account.getAccountName());
                startActivityForResult(i, AddEditAccount.EDIT_ACCOUNT_REQUEST_CODE);
            }
            break;
        case R.id.delete:
            if (Utilities.isSyncRequired(this)) {
                UIUtilities.showToast(this, R.string.sync_required);
            } else {
                showDialog(CONFIRM_DELETE_DIALOG);
            }
            break;
        }

        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch(id) {
        case CONFIRM_DELETE_DIALOG:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure?")
                .setTitle("Confirm Delete")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getPasswordDatabase().deleteAccount(account.getAccountName());
                        final String accountName = account.getAccountName();

                        new SaveDatabaseAsyncTask(ViewAccountDetails.this, new Callback() {
                            @Override
                            public void execute() {
                                String message = String.format(getString(R.string.account_deleted), accountName);
                                Toast.makeText(ViewAccountDetails.this, message, Toast.LENGTH_SHORT).show();
                                // Set this flag so that when we're returned to the FullAccountList
                                // activity the list is refreshed
                                ViewAccountDetails.this.setResult(AddEditAccount.EDIT_ACCOUNT_RESULT_CODE_TRUE);
                                finish();
                            }
                        }).execute(getPasswordDatabase());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
            dialog = builder.create();
        }

        return dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode) {
            case AddEditAccount.EDIT_ACCOUNT_REQUEST_CODE:
                editAccountResultCode = resultCode;
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // If the back button is pressed pass back the edit account flag
        // This is used to indicate if the list of account names on 
        // FullAccountList needs to be refreshed
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(editAccountResultCode);
        }
        return super.onKeyDown(keyCode, event);
    } 

    private void populateView() {
        TextView accountNameTextView = findViewById(R.id.account_name);
        accountNameTextView.setText(account.getAccountName());

        TextView accountUseridTextView = findViewById(R.id.account_userid);
        accountUseridTextView.setText(account.getUserId());

        TextView accountPasswordTextView = findViewById(R.id.account_password);
        accountPasswordTextView.setText(account.getPassword());

        TextView accountURLTextView = findViewById(R.id.account_url);
        accountURLTextView.setText(account.getUrl());

        TextView accountNotesTextView = findViewById(R.id.account_notes);
        accountNotesTextView.setText(account.getNotes());
    }

    private PasswordDatabase getPasswordDatabase() {
        return ((MSApplication) getApplication()).getPasswordDatabase();
    }

}
