package pl.kamituel.nfcbusinesscardwriter;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

public class ContactCursor {
	public static class ValueType {
		protected String mValue;
		protected String mType;
		
		public ValueType (String value, String type) {
			mValue = value;
			mType = type;
		}
		
		String getValue() {
			return mValue;
		}
		String getType() {
			return mType;
		}
	}
	
	private Context mCtx;
	
	//private static final String C_ID = Contacts._ID;
	private static final String C_DISPLAY_NAME = Contacts.DISPLAY_NAME;
	private static final String C_HAS_PHONE_NUMBER = Contacts.HAS_PHONE_NUMBER;
	//private static final String C_IN_VISIBLE_GROUP = Contacts.IN_VISIBLE_GROUP;
	//private static final String C_LOOKUP_KEY = Contacts.LOOKUP_KEY;
	
	public ContactCursor(Context ctx) {
		mCtx = ctx;
	}
	
	public Cursor getCursorByDisplayName(String name) {
		Uri uri =  ContactsContract.Contacts.CONTENT_URI;
		String[] projection = null;
		String selection = C_DISPLAY_NAME + " LIKE '%" + name + "%' AND " + C_DISPLAY_NAME + " NOT LIKE '%@%' AND " + C_HAS_PHONE_NUMBER + "=1";
		String[] selectionArgs = null;
		String sortOrder = null;
		
		Cursor c = mCtx.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
		if (c == null || !c.moveToFirst()) return null;
		
		return c;
	}
	
	public CursorLoader getCursorLoaderByDisplayName(String name) {
		Uri uri =  ContactsContract.Contacts.CONTENT_URI;
		String[] projection = null;
		String selection = C_DISPLAY_NAME + " LIKE '%" + name + "%'";
		String[] selectionArgs = null;
		String sortOrder = null;
		
		return new CursorLoader(mCtx, uri, projection, selection, selectionArgs, sortOrder);
	}
	
	public ValueType[] getPhoneNumbers (long id) {		
		Cursor phones = mCtx.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER}, 
				ContactsContract.Data.CONTACT_ID + "=" + id,
				null,
				null);
		
		return simpleCursorToValueTypeArray(
				phones, 
				phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER),
				phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
	}
	
	public ValueType[] getEmailAddresses (long id) {
		Cursor emails = mCtx.getContentResolver().query(
				ContactsContract.CommonDataKinds.Email.CONTENT_URI,
				new String[] {ContactsContract.CommonDataKinds.Email.ADDRESS},
				ContactsContract.Data.CONTACT_ID + "=" + id, 
				null,
				null);
		
		return simpleCursorToValueTypeArray(
				emails, 
				emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS),
				emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
	}
	
	private ValueType[] simpleCursorToValueTypeArray(Cursor c, int cursorValueColumnId, int cursorTypeColumnId) {
		if (c == null) {
			return new ValueType[] {};
		}
		
		ValueType[] res = new ValueType[c.getCount()];
		int i = 0;
		while (c.moveToNext()) {
			String value = c.getString(cursorValueColumnId);
			String type = "invalid"; //c.getString(cursorTypeColumnId);
			res[i++] = new ValueType(value, type);
		}
		
		return res;
	}
}
