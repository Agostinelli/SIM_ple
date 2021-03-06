package com.next.simply.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.next.simply.R;
import com.next.simply.adapters.ContactAdapter;
import com.next.simply.model.Phonebook;
import com.next.simply.utils.SimplyConstants;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ContactsActivity extends AppCompatActivity {
    private static final String TAG = ContactsActivity.class.getSimpleName();

    private ContactAdapter adapter;

    private Phonebook phonebook;

    private Map<String, String> mContacts;
    private Map<String, String> mFilteredMap = new TreeMap<String, String>();
    private android.support.v7.app.ActionBar mActionBar;
    private String[] mKeys;
    private String[] mValues;

    final private String mSearchFailed = "No contact named ";

    private boolean mOrderByName;
    private boolean mIsShownBySim;
    private boolean mShowBoth;

    @Bind(R.id.empty2) TextView mEmptySearch;
    @Bind(R.id.empty) TextView mEmpty;
    @Bind(R.id.listView) ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        ButterKnife.bind(this);

        mActionBar = getSupportActionBar();
        mActionBar.setTitle(Html.fromHtml("<b>SIM PLE</b>"));

        phonebook = new Phonebook();

        getContacts();
    }

    private void getContacts() {
        SharedPreferences mPrefs = getSharedPreferences(SimplyConstants.KEY_FILE, MODE_PRIVATE);
        mOrderByName = mPrefs.getBoolean(SimplyConstants.KEY_NAME_SURNAME, true);

        mIsShownBySim = mPrefs.getBoolean(SimplyConstants.KEY_SHOW_SIM, false);
        mShowBoth = mPrefs.getBoolean(SimplyConstants.KEY_SHOW_BOTH, false);

        mContacts = new TreeMap<String, String>();

        if (mShowBoth) {
            Map<String, String> sim = phonebook.getSimContacts(this);
            Map<String, String> phone = phonebook.getAllMobileNumbersByName(this);
            if(sim.size() > 0) {
                mContacts.putAll(sim);
            }
            mContacts.putAll(phone);
        }
        else if (mIsShownBySim) {
            mContacts = phonebook.getSimContacts(this);
            if (mContacts.isEmpty()) {
                Toast.makeText(this, "There are no contacts in the SIM", Toast.LENGTH_LONG).show();
            }
        }
        else {
            mContacts = phonebook.getAllMobileNumbersByName(this);
        }

        if (!mOrderByName) {
            mContacts = phonebook.sortedByLastName(mContacts);
        }

        // Retrieving names and numbers from Map
        mValues = mContacts.values().toArray(new String[mContacts.size()]);
        mKeys = mContacts.keySet().toArray(new String[mContacts.size()]);

        createContactAdapter(mContacts);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), EditContactActivity.class);
                String item = adapter.getKey(position);

                if (mValues[position] == null) {
                    intent.putExtra(SimplyConstants.KEY_CONTACT_NUMBER, "No number found");
                } else {
                    String number = adapter.getItem(position).toString();
                    intent.putExtra(SimplyConstants.KEY_CONTACT_NUMBER, number);
                }

                intent.putExtra(SimplyConstants.KEY_CONTACT_NAME, item);
                intent.putExtra(SimplyConstants.KEY_KEYS_CONTACT, mKeys);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getContacts();
    }

    private void createContactAdapter(Map<String, String> contacts) {
        adapter = new ContactAdapter(this, contacts);
        mListView.setAdapter(adapter);
        mListView.setEmptyView(mEmpty);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts, menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setQueryHint("Search contacts");

        AutoCompleteTextView searchTextView = (AutoCompleteTextView) search.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
        }

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                for (String name : mKeys) {
                    String number = mContacts.get(name);
                    name = name.trim();

                    // Search from the start
                    if (name.toLowerCase().startsWith(newText.toLowerCase()) && newText.length() > 0) {
                        mFilteredMap.put(name, number);

                        if (mFilteredMap.size() > 0) {
                            createContactAdapter(mFilteredMap);
                        }
                        else {
                            setEmptyText(newText);
                        }
                    }
                    else {
                        // Delete
                        if (!name.toLowerCase().startsWith(newText.toLowerCase()) && newText.length() > 0) {
                            mFilteredMap.remove(name);

                            if (mFilteredMap.size() > 0) {
                                createContactAdapter(mFilteredMap);
                            }
                            else {
                                setEmptyText(newText);
                            }
                        }
                        if (newText.length() == 0) {
                            mFilteredMap.clear();
                            createContactAdapter(mContacts);
                        }
                    }
                    // Searching from the end
                    String[] split = name.split(" ");
                    if (split.length > 1 && split[1].toLowerCase().startsWith(newText.toLowerCase()) && newText.length() > 0) {
                        mFilteredMap.put(name, number);

                        if (mFilteredMap.size() > 0) {
                            createContactAdapter(mFilteredMap);
                        }
                        else {
                            setEmptyText(newText);
                        }
                    }
                    else if (split.length > 1 && split[split.length - 1].toLowerCase().startsWith(newText.toLowerCase()) && newText.length() > 0) {
                        mFilteredMap.put(name, number);

                        if (mFilteredMap.size() > 0) {
                            createContactAdapter(mFilteredMap);
                        }
                        else {
                            setEmptyText(newText);
                        }
                    }
                }
                return false;
            }
        });

        return true;
    }

    private void setEmptyText(String newText) {
        mEmptySearch.setText(Html.fromHtml(mSearchFailed + "<b>'" + newText + "'</b>"));
        mListView.setEmptyView(mEmptySearch);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_show_contacts:
                Intent showContactIntent = new Intent(this, ShowContactsActivity.class);
                startActivity(showContactIntent);
            case R.id.action_search:
                break;
            case R.id.action_sort_by:
                Intent intent = new Intent(this, SortByActivity.class);
                startActivity(intent);
                break;
            case R.id.action_import_export:
                Intent importExportIntent = new Intent(this, ImportExportContactActivity.class);
                importExportIntent.putExtra(SimplyConstants.KEY_KEYS_CONTACT, mKeys);
                importExportIntent.putExtra(SimplyConstants.KEY_VALUES_CONTACT, mValues);
                startActivity(importExportIntent);
                break;
            case R.id.action_deleteAll:
                new AlertDialog.Builder(this)
                        .setMessage("All contacts will be deleted.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                phonebook.deleteAll(ContactsActivity.this);
                                Toast.makeText(ContactsActivity.this, "All contacts deleted.", Toast.LENGTH_LONG).show();
                                getContacts();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.addContact)
    public void startAddActivity() {
        Intent intent = new Intent(this, AddContactActivity.class);
        intent.putExtra(SimplyConstants.KEY_KEYS_CONTACT, mKeys);
        intent.putExtra(SimplyConstants.KEY_VALUES_CONTACT, mValues);
        startActivity(intent);
    }
}



























