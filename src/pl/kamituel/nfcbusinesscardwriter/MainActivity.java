package pl.kamituel.nfcbusinesscardwriter;

import java.io.IOException;

import android.content.Intent;
import android.database.Cursor;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends NfcRequiredActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		((Button) findViewById(R.id.writeTagButton)).setOnClickListener(this);
		
		Cursor contacts = new ContactCursor(this).getByDisplayName("");	
		if (contacts != null) {
			AutoCompleteTextView nameEditText = (AutoCompleteTextView) findViewById(R.id.nameEditText);
			CursorAdapter nameAdapter = new CursorAdapter(this, cursor);
			nameEditText.setAdapter(nameAdapter);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {                          
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			NdefRecord[] records = {
					new NdefContact.Builder()
					.appendPhone(NdefContact.Builder.PHONE_TYPE_HOME, getEditTextValue(R.id.phoneEditText))
					.appendPhone(NdefContact.Builder.PHONE_TYPE_HOME, "13123123")
					.appendEmail(getEditTextValue(R.id.emailEditText))
					.appendName(getEditTextValue(R.id.nameEditText))
					.build()
					.toNdefRecord()
			};
			NdefMessage message = new NdefMessage(records);

			Ndef ndef = Ndef.get(tag);
			try {
				ndef.connect();
				ndef.writeNdefMessage(message);
			} catch (IOException e) {
				Toast.makeText(this, "adasd err1 " + e.getMessage(), Toast.LENGTH_SHORT).show();
			} catch (FormatException e) {
				Toast.makeText(this, "adasd err2 " + e.getMessage(), Toast.LENGTH_SHORT).show();
			} finally {
				try { ndef.close(); } catch (Exception e) {}
			}

			Toast.makeText(this, "adasd", Toast.LENGTH_SHORT).show();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.writeTagButton:
			writeTag();
			break;
		}
	}

	private void writeTag () {

	}
	
	private String getEditTextValue (int id) {
		return ((EditText) findViewById(id)).getText().toString();
	}

}
