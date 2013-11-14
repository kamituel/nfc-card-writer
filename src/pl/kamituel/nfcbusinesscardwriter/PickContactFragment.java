package pl.kamituel.nfcbusinesscardwriter;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class PickContactFragment extends Fragment implements OnItemClickListener {
	private static final String TAG = PickContactFragment.class.getCanonicalName();
	
	public static final String QUERY_BY_NAME = "query-contact-name";
	public static final String EXTRA_CONTACT_LOOKUP_KEY = "contact-lookup-key";
	private SimpleCursorAdapter mContactAdapter;
	
	private PickContactFragmentListener mParent;
	
	public interface PickContactFragmentListener {
		public void onContactPicked(String cursorLookupKey);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.pick_contact, container, false);
		
		ListView pickContact = (ListView) layout.findViewById(R.id.pickContactListView);
		mContactAdapter = new SimpleCursorAdapter(getActivity(), R.layout.name_autocomplete_item, null, new String[] {ContactsContract.Contacts.DISPLAY_NAME}, new int[] {R.id.name});
		pickContact.setAdapter(mContactAdapter);
		pickContact.setOnItemClickListener(this);
		
		EditText filterEditText = (EditText) layout.findViewById(R.id.pickContactFilter);
		filterEditText.addTextChangedListener(new FilterBoxTextWatcher());
		
		return layout;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		startQuery(null);
	}

	/*@Override
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
	}*/
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mParent = (PickContactFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement " 
				+ PickContactFragmentListener.class.getCanonicalName() + " interface");
		}
	}

	public void startQuery(String nameQuery) {
		// TODO: is it possible to reuse loader?
		getLoaderManager().destroyLoader(ContactsLoader.CONTACT_LOADER);
		getLoaderManager().initLoader(ContactsLoader.CONTACT_LOADER, null, new ContactsLoader(nameQuery));
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Cursor contacts = mContactAdapter.getCursor();
		contacts.moveToPosition(position);
		
		mParent.onContactPicked(contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
	}

	private class ContactsLoader implements LoaderCallbacks<Cursor> {
		public static final int CONTACT_LOADER = 1;
		private String mQuery;
		
		public ContactsLoader (String query) {
			mQuery = query != null ? query : "";
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
				String sortOrder = ContactsContract.Contacts.SORT_KEY_PRIMARY + " ASC";
				
				return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
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
	
	private class FilterBoxTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			startQuery(s.toString());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
