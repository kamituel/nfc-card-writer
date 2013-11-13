package pl.kamituel.nfcbusinesscardwriter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactCursorHelper {
	private Context mCtx;
	private Cursor mContact;
	
	private ContactCursorHelper(Context ctx, Cursor contact) {
		mCtx = ctx;
		mContact = contact;
	}
	
	public static ContactCursorHelper fromCursor (Context ctx, Cursor contact) {
		return new ContactCursorHelper(ctx, contact);
	}
	
	public static ContactCursorHelper byLookupKey (Context ctx, String lookupKey) {		
		Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, Uri.encode("" + lookupKey));
		Cursor contact = ctx.getContentResolver().query(contactUri, null, null, null, null, null);
		
		if (!contact.moveToFirst()) {
			return null;
		}
		
		return new ContactCursorHelper(ctx, contact);
	}
	
	public long getId() {
		return mContact.getLong(mContact.getColumnIndex(ContactsContract.Contacts._ID));
	}
	
	public String getDisplayName() {
		return mContact.getString(mContact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	}
	
	public ValueType[] getPhoneNumbers() {
		Cursor phones = mCtx.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER}, 
				ContactsContract.Data.CONTACT_ID + "=" + getId(),
				null,
				null);
		
		return simpleCursorToValueTypeArray(
				phones, 
				phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER),
				phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
	}
	
	public ValueType[] getEmailAddresses() {
		Cursor emails = mCtx.getContentResolver().query(
				ContactsContract.CommonDataKinds.Email.CONTENT_URI,
				new String[] {ContactsContract.CommonDataKinds.Email.ADDRESS},
				ContactsContract.Data.CONTACT_ID + "=" + getId(), 
				null,
				null);
		
		return simpleCursorToValueTypeArray(
				emails, 
				emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS),
				emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
	}
	
	private static ValueType[] simpleCursorToValueTypeArray(Cursor c, int cursorValueColumnId, int cursorTypeColumnId) {
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
}
