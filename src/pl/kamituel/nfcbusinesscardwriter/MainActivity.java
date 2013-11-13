package pl.kamituel.nfcbusinesscardwriter;

import java.io.IOException;

import pl.kamituel.nfcbusinesscardwriter.ContactCursorHelper.ValueType;
import pl.kamituel.nfcbusinesscardwriter.ContactFieldArrayAdapter.OnAddNewItemTextChangedListener;
import pl.kamituel.nfcbusinesscardwriter.NdefContact.Builder;
import pl.kamituel.nfcbusinesscardwriter.ui.IconEditText;
import pl.kamituel.nfcbusinesscardwriter.ui.IconEditText.OnIconClickListener;
import pl.kamituel.nfcbusinesscardwriter.ui.LinearLayoutList;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends NfcRequiredActivity 
implements OnIconClickListener {
	private final static String TAG = MainActivity.class.getCanonicalName();

	private ContactFieldArrayAdapter mPhonesAdapter;
	private ContactFieldArrayAdapter mEmailsAdapter;

	private final static int PICK_CONTACT_REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		IconEditText name = (IconEditText) findViewById(R.id.nameEditText);
		name.setOnIconClickListener(this);

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
	
	private void populateEditorFields(ContactCursorHelper contact) {
		((IconEditText) findViewById(R.id.nameEditText)).setText(contact.getDisplayName());
		
		ValueType[] phones = contact.getPhoneNumbers();
		mPhonesAdapter.addAll(phones);

		ValueType[] emails = contact.getEmailAddresses();
		mEmailsAdapter.addAll(emails);
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

	private String getEditTextValue (int id) {
		return ((EditText) findViewById(id)).getText().toString();
	}

	@Override
	public void iconClicked(IconEditText v) {
		if (R.id.nameEditText == v.getId()) {
			Intent pickContact = new Intent(this, PickContactActivity.class);
			pickContact.putExtra(PickContactActivity.QUERY_BY_NAME, v.getText().toString());
			startActivityForResult(pickContact, PICK_CONTACT_REQUEST_CODE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case PICK_CONTACT_REQUEST_CODE:
			if (RESULT_OK == resultCode) {
				String contactLookupKey = data.getStringExtra(PickContactActivity.EXTRA_CONTACT_LOOKUP_KEY);
				ContactCursorHelper contact = ContactCursorHelper.byLookupKey(this, contactLookupKey);
				
				Log.d(TAG, "Found " + contactLookupKey + contact.getDisplayName());
				populateEditorFields(contact);
			}
			break;
		default:
			Log.w("xxx", "Invalid request code: " + requestCode);
		}
	}
}
