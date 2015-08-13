package com.next.simply.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;

import com.next.simply.R;
import com.next.simply.utils.SimplyConstants;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SortByActivity extends AppCompatActivity {

    @Bind(R.id.nameButton) RadioButton mName;
    @Bind(R.id.surnameButton) RadioButton mSurname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_by);
        ButterKnife.bind(this);

        SharedPreferences mPrefs = getSharedPreferences(SimplyConstants.KEY_FILE, MODE_PRIVATE);
        boolean name = mPrefs.getBoolean(SimplyConstants.KEY_NAME_SURNAME, true);

        if (name) {
            mName.setChecked(true);
        }
        else {
            mSurname.setChecked(true);
        }

        mName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ContactsActivity.class);

                SharedPreferences.Editor mEditor = getSharedPreferences(SimplyConstants.KEY_FILE, MODE_PRIVATE).edit();
                mEditor.putBoolean(SimplyConstants.KEY_NAME_SURNAME, true);
                mEditor.apply();

                mSurname.setChecked(false);

                startActivity(intent);
            }
        });

        mSurname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ContactsActivity.class);

                SharedPreferences.Editor mEditor = getSharedPreferences(SimplyConstants.KEY_FILE, MODE_PRIVATE).edit();
                mEditor.putBoolean(SimplyConstants.KEY_NAME_SURNAME, false);
                mEditor.apply();

                mName.setChecked(false);

                startActivity(intent);
            }
        });
    }
}
