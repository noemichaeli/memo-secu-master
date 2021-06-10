package com.app.memosecu;

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class SearchResults extends AccountsList {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);
        registerForContextMenu(getListView());
    }

    @Override
    public boolean onSearchRequested() {
        // Returning false here means that if the user can't initiate a search
        // while on the SearchResults page
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // If the pw database is null then just close the activity.
        if (getPasswordDatabase() == null) {
            finish();
        } else {
            doSearch();
        }
    }

    private void doSearch() {
        final Intent queryIntent = getIntent();
        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            filterAccountsList(queryIntent.getStringExtra(SearchManager.QUERY));
        }
    }

    private void filterAccountsList(String textToFilterOn) {
        ArrayList<String> allAccountNames = getPasswordDatabase().getAccountNames(); 
        ArrayList<String> filteredAccountNames = new ArrayList<String>();
        String textToFilterOnLC = textToFilterOn.toLowerCase();
        
        // Loop through all the accounts and pick out those that match the search string
        for (String accountName : allAccountNames) {
            if (accountName.toLowerCase().indexOf(textToFilterOnLC) > -1) {
                filteredAccountNames.add(accountName);
            }
        }

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filteredAccountNames));
    }

}
