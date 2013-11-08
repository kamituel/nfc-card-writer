package pl.kamituel.nfcbusinesscardwriter;

import java.io.IOException;

import pl.kamituel.nfcbusinesscardwriter.ContactCursor.ValueType;
import pl.kamituel.nfcbusinesscardwriter.ContactFieldArrayAdapter.OnAddNewItemTextChangedListener;
import pl.kamituel.nfcbusinesscardwriter.NdefContact.Builder;
import pl.kamituel.nfcbusinesscardwriter.ui.LinearLayoutList;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.Toast;

public class MainActivity extends NfcRequiredActivity 
implements /*OnClickListener,*/ LoaderCallbacks<Cursor> {

	private final static int CONTACT_SUGGESTIONS_LOADER = 1;
	private SimpleCursorAdapter mNameAutocompleteAdapter;
	private ContactCursor mContactCursor;

	private ContactFieldArrayAdapter mPhonesAdapter;
	private ContactFieldArrayAdapter mEmailsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//((Button) findViewById(R.id.writeTagButton)).setOnClickListener(this);
		setupAutocomplete();

		mPhonesAdapter = new ContactFieldArrayAdapter(this, R.layout.contact_editor_field_phone, R.id.phoneEditText);
		setupFieldList(R.id.phoneList, mPhonesAdapter, R.id.addAnotherPhone);
		
		mEmailsAdapter = new ContactFieldArrayAdapter(this, R.layout.contact_editor_field_email, R.id.emailEditText);
		setupFieldList(R.id.emailList, mEmailsAdapter, R.id.addAnotherEmail);
	}

	private void setupFieldList (int listId, final ContactFieldArrayAdapter adapter, int addNewButtonId) {
		adapter.add(new ValueType("", ""));
		
		final Button addNewItem = (Button) findViewById(addNewButtonId);
		LinearLayoutList phones = (LinearLayoutList) findViewById(listId);
		phones.setAdapter(adapter);
		
		adapter.setAddNewItemPopulatedListener(new OnAddNewItemTextChangedListener() {
			@Override
			public void onTextInserted() {
				addNewItem.setVisibility(View.VISIBLE);
			}

			@Override
			public void onTextRemoved() {
				addNewItem.setVisibility(View.GONE);
			}
		});
		
		
		addNewItem.setVisibility(View.GONE);
		addNewItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				adapter.add(new ValueType("", ""));
				addNewItem.setVisibility(View.GONE);
			}
		});	
	}
	
	private void setupAutocomplete() {
		mContactCursor = new ContactCursor(this);

		AutoCompleteTextView nameEditText = (AutoCompleteTextView) findViewById(R.id.nameEditText);
		getLoaderManager().initLoader(CONTACT_SUGGESTIONS_LOADER, null, this);
		mNameAutocompleteAdapter = new SimpleCursorAdapter(this, R.layout.name_autocomplete_item, null, new String[] {ContactsContract.Contacts.DISPLAY_NAME}, new int[] {R.id.name});

		mNameAutocompleteAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence constraint) {
				return new ContactCursor(MainActivity.this).getCursorByDisplayName(constraint.toString());
			}
		});

		mNameAutocompleteAdapter.setCursorToStringConverter(new CursorToStringConverter() {
			@Override
			public CharSequence convertToString(Cursor cursor) {
				return cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			}
		});

		nameEditText.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d("xxx", "selected " + position + " - " + id);
				Cursor cursor = mNameAutocompleteAdapter.getCursor();
				cursor.moveToPosition(position);
				long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));

				ValueType[] phones = mContactCursor.getPhoneNumbers(contactId);
				mPhonesAdapter.addAll(phones);

				ValueType[] emails = mContactCursor.getEmailAddresses(contactId);
				mEmailsAdapter.addAll(emails);

				/*String[] phones = mContactCursor.getPhoneNumbers(contactId);				
				setEditTextValue(R.id.emailEditText, phones.length == 0 ? "" : phones[0]);

				String[] emails = mContactCursor.getEmailAddresses(contactId);
				setEditTextValue(R.id.emailEditText, emails.length == 0 ? "" : emails[0]);*/
			}
		});


		nameEditText.setAdapter(mNameAutocompleteAdapter);
	}

	@Override
	protected void onNewIntent(Intent intent) {                          
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			NdefContact.Builder builder = new NdefContact.Builder()
				.appendName(getEditTextValue(R.id.nameEditText));
			
			for (int p = 0; p < mPhonesAdapter.getCount(); p += 1) {
				if (mPhonesAdapter.getItem(p).getValue().length() > 0) {
					builder.appendPhone(Builder.PHONE_TYPE_HOME, mPhonesAdapter.getItem(p).getValue());
				}
			}
			
			for (int p = 0; p < mEmailsAdapter.getCount(); p += 1) {
				if (mEmailsAdapter.getItem(p).getValue().length() > 0) {
					builder.appendEmail(mEmailsAdapter.getItem(p).getValue());
				}
			}
			
			NdefRecord[] records = { builder.build().toNdefRecord() };
			NdefMessage message = new NdefMessage(records);

			Ndef ndef = Ndef.get(tag);
			try {
				ndef.connect();
				ndef.writeNdefMessage(message);
			} catch (IOException e) {
				Toast.makeText(this, "ERR#1 " + e.getMessage(), Toast.LENGTH_SHORT).show();
			} catch (FormatException e) {
				Toast.makeText(this, "ERR#2 " + e.getMessage(), Toast.LENGTH_SHORT).show();
			} finally {
				try { ndef.close(); } catch (Exception e) {}
			}

			Toast.makeText(this, getResources().getString(R.string.nfc_tag_written), Toast.LENGTH_SHORT).show();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_write_nfc_tag:
			Toast.makeText(this, getResources().getString(R.string.write_nfc_tag_explanation), Toast.LENGTH_LONG).show();
			return true;
		default: 
			return false;
		}
	}


	/*@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.writeTagButton:
			writeTag();
			break;
		}
	}

	private void writeTag () {

	}*/



	private String getEditTextValue (int id) {
		return ((EditText) findViewById(id)).getText().toString();
	}

	private void setEditTextValue (int id, String value) {
		((EditText) findViewById(id)).setText(value);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case CONTACT_SUGGESTIONS_LOADER:
			return new ContactCursor(this).getCursorLoaderByDisplayName(null);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d("xxx", "Loaded " + cursor.getCount());
		mNameAutocompleteAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d("xxx", "abecadlo");
	}
}
