package pl.kamituel.nfcbusinesscardwriter;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class PickContactActivity extends Activity implements OnItemClickListener {
	private static final String TAG = PickContactActivity.class.getCanonicalName();
	
	public static final String QUERY_BY_NAME = "query-contact-name";
	public static final String EXTRA_CONTACT_LOOKUP_KEY = "contact-lookup-key";
	private SimpleCursorAdapter mContactAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pick_contact);
		
		ListView pickContact = (ListView) findViewById(R.id.pickContactListView);
		mContactAdapter = new SimpleCursorAdapter(this, R.layout.name_autocomplete_item, null, new String[] {ContactsContract.Contacts.DISPLAY_NAME}, new int[] {R.id.name});
		pickContact.setAdapter(mContactAdapter);
		pickContact.setOnItemClickListener(this);
		
		startQuery(getIntent());
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent()");

		startQuery(intent);
	}
	
	private void startQuery(Intent intent) {
		String query = intent.getStringExtra(QUERY_BY_NAME);
		getLoaderManager().initLoader(ContactsLoader.CONTACT_LOADER, null, new ContactsLoader(query));
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Intent result = new Intent();
		
		Cursor contacts = mContactAdapter.getCursor();
		contacts.moveToPosition(position);
		
		result.putExtra(EXTRA_CONTACT_LOOKUP_KEY, contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
		setResult(RESULT_OK, result);
		finish();
	}

	private class ContactsLoader implements LoaderCallbacks<Cursor> {
		public static final int CONTACT_LOADER = 1;
		private String mQuery;
		
		public ContactsLoader (String query) {
			mQuery = query;
		}

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
			Log.d(TAG, "onCreateLoader(" + loaderId + ")");

			switch(loaderId) {
			case CONTACT_LOADER:
				Uri uri =  ContactsContract.Contacts.CONTENT_URI;
				String[] projection = null;
				String selection = ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + mQuery + "%'";
				String[] selectionArgs = null;
				String sortOrder = null;
				
				return new CursorLoader(PickContactActivity.this, uri, projection, selection, selectionArgs, sortOrder);
			default:
				return null;
			}
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
			Log.d(TAG, "onLoadFinished() with cursors size " + cursor.getCount());
			mContactAdapter.swapCursor(cursor);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			// TODO Auto-generated method stub

		}
	}
}
