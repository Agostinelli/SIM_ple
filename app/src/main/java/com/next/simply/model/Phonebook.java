package com.next.simply.model;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.next.simply.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by manfredi on 07/08/15.
 */
public class Phonebook {

    public Map<String, String> getAllMobileNumbersByName(final Context context) {
        Map<String, String> contacts = new TreeMap<String, String>();
        final Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            final int phone_id = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            final String contactName = cursor.getString(phone_id);

            final int phone_number = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            final String contactNumber = cursor.getString(phone_number);

            contacts.put(contactName, contactNumber);
        }
        return contacts;
    }

    public ArrayList<String> getContactsName(final Context context) {
        ArrayList<String> contacts = new ArrayList<String>();
        final Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            final int phone_id = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            final String contactName = cursor.getString(phone_id);

            contacts.add(contactName);
        }
        return contacts;
    }

    private String sortByLastName(String toSort) {
        String sorted = "";
        String[] split = toSort.split(" ");

        for (int i = 0; split.length > i; i++) {
            sorted = sorted + " " + split[split.length - i -1];
        }
        return sorted;
    }

    public Map<String, String> sortedByLastName(Map<String, String> list) {
        Map<String, String> contacts = new TreeMap<String, String>();

        String[] mKeys = list.keySet().toArray(new String[list.size()]);

        for(int i = 0; i < list.size(); i++) {
            contacts.put(sortByLastName(mKeys[i]), list.get(mKeys[i]));
        }

        return contacts;
    }

    public boolean createNewContact(String name, String[] keys, String telephone, Context context) {

        if (areDifferent(name, keys)) {

            ArrayList<ContentProviderOperation> cntProOper = new ArrayList<ContentProviderOperation>();
            int contactIndex = cntProOper.size();//ContactSize

            //Newly Inserted contact
            // A raw contact will be inserted ContactsContract.RawContacts table in contacts database.
            cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)//Step1
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

            //Display name will be inserted in ContactsContract.Data table
            cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step2
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name) // Name of the contact
                    .build());
            //Mobile number will be inserted in ContactsContract.Data table
            cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step 3
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, telephone) // Number to be added
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); //Type like HOME, MOBILE etc
            try {
                // We will do batch operation to insert all above data
                //Contains the output of the app of a ContentProviderOperation.
                //It is sure to have exactly one of uri or count set
                ContentProviderResult[] contentProresult = null;
                contentProresult = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cntProOper); //apply above data insertion into contacts list
                return true;
            } catch (RemoteException exp) {
                //logs;
            } catch (OperationApplicationException exp) {
                //logs
            }
        }
        else {
            Toast.makeText(context, "Contact already exists in the phone", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void createNewContact(String name, String telephone, Context context) {

            ArrayList<ContentProviderOperation> cntProOper = new ArrayList<ContentProviderOperation>();
            int contactIndex = cntProOper.size();//ContactSize

            //Newly Inserted contact
            // A raw contact will be inserted ContactsContract.RawContacts table in contacts database.
            cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)//Step1
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

            //Display name will be inserted in ContactsContract.Data table
            cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step2
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name) // Name of the contact
                    .build());
            //Mobile number will be inserted in ContactsContract.Data table
            cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step 3
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, telephone) // Number to be added
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); //Type like HOME, MOBILE etc
            try {
                // We will do batch operation to insert all above data
                //Contains the output of the app of a ContentProviderOperation.
                //It is sure to have exactly one of uri or count set
                ContentProviderResult[] contentProresult = null;
                contentProresult = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cntProOper); //apply above data insertion into contacts list
            } catch (RemoteException exp) {
                //logs;
            } catch (OperationApplicationException exp) {
                //logs
            }
    } // **************REMOVE BEFORE FLIGHT******************

    public void importSimContact(String[] keys, Context context) {
        if (isSimAvailable(context)) {
            int index = 0;

            Uri simUri = Uri.parse("content://icc/adn");

            Cursor cursorSim = context.getContentResolver().query(simUri, null, null, null, null);

            while (cursorSim.moveToNext()) {
                final String name = cursorSim.getString(cursorSim.getColumnIndex("name"));
                final String number = cursorSim.getString(cursorSim.getColumnIndex("number"));

                if (areDifferent(name, keys)) {
                    createNewContact(name, keys, number, context);
                    index++;
                }

            }
            Toast.makeText(context, index + " contacts added.", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context, R.string.sim_not_available, Toast.LENGTH_LONG).show();
        }
    }

    public Map<String, String> getSimContacts(Context context) {

        Map<String, String> contacts = new TreeMap<String, String>();

        if (isSimAvailable(context)) {

            Uri simUri = Uri.parse("content://icc/adn");

            Cursor cursorSim = context.getContentResolver().query(simUri, null, null, null, null);

            while (cursorSim.moveToNext()) {
                final String name = cursorSim.getString(cursorSim.getColumnIndex("name"));
                final String number = cursorSim.getString(cursorSim.getColumnIndex("number"));

                contacts.put(name, number);
            }
        }
        else {
            Toast.makeText(context, R.string.sim_not_available, Toast.LENGTH_LONG).show();
        }
        return contacts;
    }

    public ArrayList<String> getSimNames(Context context) {
        ArrayList<String> contacts = new ArrayList<String>();

        if (isSimAvailable(context)) {

            Uri simUri = Uri.parse("content://icc/adn");

            Cursor cursorSim = context.getContentResolver().query(simUri, null, null, null, null);

            while (cursorSim.moveToNext()) {
                final String name = cursorSim.getString(cursorSim.getColumnIndex("name"));

                contacts.add(name);
            }
        }
        else {
            Toast.makeText(context, R.string.sim_not_available, Toast.LENGTH_LONG).show();
        }

        return contacts;
    }

    public boolean areDifferent(String name, String[] contacts) {

        for (String firstName : contacts) {
            if (name.equalsIgnoreCase(firstName)) {
                return false;
            }
        }
        return true;
    }

    public boolean areDifferent(String name, ArrayList<String> contacts) {

        for (String firstName : contacts) {
            if (name.equals(firstName)) {
                return false;
            }
        }
        return true;
    }

    public boolean insertSIMContact(String name, String number, Context context) {
        if (isSimAvailable(context)) {
            if (name.length() > 16) {
                name = name.substring(0, 16);
            }
                if (areDifferent(name, getSimNames(context))) {
                    Uri simUri = Uri.parse("content://icc/adn");

                    ContentValues values = new ContentValues();
                    values.put("tag", name);
                    values.put("number", number);

                    context.getContentResolver().insert(simUri, values);
                    context.getContentResolver().notifyChange(simUri, null);
                    return true;
                }
                else {
                    Toast.makeText(context, "Contact already exists in the SIM", Toast.LENGTH_SHORT).show();
                }
        }
        else {
            Toast.makeText(context, R.string.sim_not_available, Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public boolean deleteContact(Context context, String name) {
        try {
            Uri simUri = Uri.parse("content://icc/adn/");
            ContentResolver mContentResolver = context.getContentResolver();
            Cursor cursor = mContentResolver.query(simUri, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor.getColumnIndex("name")).equalsIgnoreCase(name)) {
                        mContentResolver.delete(
                                simUri,
                                "tag='" + cursor.getString(cursor.getColumnIndex("name")) +
                                        "' AND " +
                                        "number='" + cursor.getString(cursor.getColumnIndex("number")) + "'"
                                , null);
                        break;
                    }
                }
                while (cursor.moveToNext());
            }
           cursor.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }




        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        cr.delete(uri, null, null);
                        return true;
                    }

                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }
        catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        return false;
    }

    public void deleteAll(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        while (cur.moveToNext()) {
            try{
                String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                System.out.println("The uri is " + uri.toString());
                cr.delete(uri, null, null);
            }
            catch(Exception e)
            {
                System.out.println(e.getStackTrace());
            }
        }
    }

    public void addAllContactsToSim(Context context, String[] keys, String[] values) {
        if (isSimAvailable(context)) {

            int index = 0;

            for (int i = 0; i < keys.length; i++) {
                if (areDifferent(keys[i], getSimNames(context))) {
                    insertSIMContact(keys[i], values[i], context);
                    index++;
                }
            }
            Toast.makeText(context, index + " contacts added.", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context, R.string.sim_not_available, Toast.LENGTH_LONG).show();
        }
    }

    public boolean isSimAvailable(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        if (simState == TelephonyManager.SIM_STATE_ABSENT) {
            return false;
        }
        return true;
    }

}
