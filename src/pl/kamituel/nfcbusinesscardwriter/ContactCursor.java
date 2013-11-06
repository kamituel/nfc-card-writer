package pl.kamituel.nfcbusinesscardwriter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactCursor {
	private Context mCtx;
	
	public ContactCursor(Context ctx) {
		mCtx = ctx;
	}
	
	public Cursor getByDisplayName(String name) {
		Uri uri =  ContactsContract.Contacts.CONTENT_URI;
		String[] projection = null;
		String selection = ContactsContract.Contacts.DISPLAY_NAME + "='%" + name + "%'";
		String[] selectionArgs = null;
		String sortOrder = null;
		
		Cursor c = mCtx.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
		if (c == null || !c.moveToFirst()) return null;
		
		return c;
	}
}
