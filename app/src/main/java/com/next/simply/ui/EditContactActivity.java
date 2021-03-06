package com.next.simply.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.next.simply.R;
import com.next.simply.model.Phonebook;
import com.next.simply.utils.SimplyConstants;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EditContactActivity extends AppCompatActivity {

    @Bind(R.id.nameContactEditText) EditText mNameContact;
    @Bind(R.id.numberNameContactEditText) EditText mNumberContact;

    private String[] mKeys;
    private String mName;
    private String mNumber;
    private Phonebook mPhonebook;
    private boolean modifyEnabled = false;

    private android.support.v7.app.ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        ButterKnife.bind(this);

        mActionBar = getSupportActionBar();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mName = extras.getString(SimplyConstants.KEY_CONTACT_NAME);
            mNumber = extras.getString(SimplyConstants.KEY_CONTACT_NUMBER);
            mActionBar.setTitle(Html.fromHtml("<b>" + mName + "</b>"));
            mNameContact.setText(mName);
            mNumberContact.setText(mNumber);
            mKeys = extras.getStringArray(SimplyConstants.KEY_KEYS_CONTACT);
        }
        enableModify(modifyEnabled);

        if (!modifyEnabled) {
            mNameContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), R.string.enable_changes, Toast.LENGTH_SHORT).show();
                }
            });
            mNumberContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(EditContactActivity.this, R.string.enable_changes, Toast.LENGTH_SHORT).show();
                }
            });
        }

        mPhonebook = new Phonebook();
    }

    private void enableModify(boolean modify) {
        mNameContact.setFocusable(modify);
        mNameContact.setClickable(modify);
        mNumberContact.setFocusable(modify);
        mNumberContact.setClickable(modify);

        mNameContact.setFocusableInTouchMode(modify);
        mNumberContact.setFocusableInTouchMode(modify);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            modifyEnabled = true;
            enableModify(modifyEnabled);
            mNameContact.requestFocus();
        }
        else if (id == R.id.action_delete) {
            new AlertDialog.Builder(this)
                    .setMessage("This contact will be deleted.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mPhonebook.deleteContact(EditContactActivity.this, mName);
                            Toast.makeText(EditContactActivity.this, "Contact removed", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(EditContactActivity.this, ContactsActivity.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .show();
        }
        else if (id == android.R.id.home) {
            ArrayList<String> simContacts = mPhonebook.getSimNames(this);
            ArrayList<String> phoneContacts = mPhonebook.getContactsName(this);
            String name = mNameContact.getText().toString();
            String number = mNumberContact.getText().toString();

            for (String contactName : simContacts) {
                if (contactName.equalsIgnoreCase(mName)) {
                    if (name.length() > 15) {
                        Toast.makeText(this, "Too much characters for SIM", Toast.LENGTH_SHORT).show();
                        mPhonebook.insertSIMContact(mName, number, this);
                    }
                    else {
                        mPhonebook.deleteContact(this, mName);
                        mPhonebook.insertSIMContact(name, number, this);
                    }
                }
            }

            for (String contactName : phoneContacts) {
                if (contactName.equalsIgnoreCase(mName)) {
                    mPhonebook.deleteContact(this, mName);
                    mPhonebook.createNewContact(name, number, this);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
}


