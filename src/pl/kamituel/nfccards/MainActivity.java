package pl.kamituel.nfccards;

import java.io.IOException;

import pl.kamituel.nfccards.CardFormFragment.CardFormFragmentListener;
import pl.kamituel.nfccards.ContactCursorHelper.ValueType;
import pl.kamituel.nfccards.NdefContact.Builder;
import pl.kamituel.nfccards.PickContactFragment.PickContactFragmentListener;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends NfcRequiredActivity implements CardFormFragmentListener, PickContactFragmentListener {
	private final static String TAG = MainActivity.class.getCanonicalName();

	private CardFormFragment mCardFormFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#33ffffff")));
		actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#55ffffff")));

		setContentView(R.layout.main);
		String cardFormFragmentTag = getResources().getString(R.string.card_form_fragment_tag);

		if (savedInstanceState == null) {
			// On small screen, create fragment programatically
			if (isOnSmallScreen()) {
				getFragmentManager().beginTransaction().add(R.id.main_container, new CardFormFragment(), cardFormFragmentTag).commit();
			}
		}		
	}

	@Override
	protected void onResume() {
		super.onResume();
		String cardFormFragmentTag = getResources().getString(R.string.card_form_fragment_tag);
		mCardFormFragment = (CardFormFragment) getFragmentManager().findFragmentByTag(cardFormFragmentTag);
	}

	private boolean isOnSmallScreen() {
		return findViewById(R.id.main_container) != null;
	}

	@Override
	public void onContactPicked(String cursorLookupKey) {
		ContactCursorHelper contact = ContactCursorHelper.byLookupKey(this, cursorLookupKey);
		mCardFormFragment.setContact(contact);

		if (isOnSmallScreen()) {
			switchBackToCardFormFragment();
		} else {
			mCardFormFragment.fillForm();
		}
	}

	private void switchBackToCardFormFragment() {
		getFragmentManager().popBackStack();
	}

	private void switchToPickContactFragment() {
		mCardFormFragment.setContact(null);

		PickContactFragment pickContact = (PickContactFragment)
				getFragmentManager().findFragmentById(R.id.pick_contact_fragment);

		if (pickContact != null) {
			//pickContact.startQuery(displayName);
		} else {
			pickContact = new PickContactFragment();

			getFragmentManager().beginTransaction()
			.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
			.replace(R.id.main_container, pickContact)
			.addToBackStack(null)
			.commit();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		//Toast.makeText(this, "action " + intent.getAction(), Toast.LENGTH_LONG).show();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

			Ndef ndef = Ndef.get(tag);
			try {
				NdefMessage message = getNdefMessage();
				ndef.connect();
				ndef.writeNdefMessage(message);
			} catch (IOException e) {
				Toast.makeText(this, getString(R.string.nfc_tag_not_writable), Toast.LENGTH_SHORT).show();
			} catch (FormatException e) {
				Toast.makeText(this, "ERR#2 " + e.getMessage(), Toast.LENGTH_SHORT).show();
			} finally {
				try { ndef.close(); } catch (Exception e) {}
			}

			Toast.makeText(this, getResources().getString(R.string.nfc_tag_written), Toast.LENGTH_SHORT).show();
		} else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			NdefFormatable formatable = NdefFormatable.get(tag);
			
			if (formatable == null) {
				Toast.makeText(this, R.string.nfc_tag_not_supported, Toast.LENGTH_LONG).show();
				return;
			}
			
			try {
				NdefMessage message = getNdefMessage();
				formatable.connect();
				formatable.format(message);
				formatable.close();
			} catch (IOException e) {
				Toast.makeText(this, "ERR#3 " + e.getMessage(), Toast.LENGTH_SHORT).show();
			} catch (FormatException e) {
				Toast.makeText(this, "ERR#4 " + e.getMessage(), Toast.LENGTH_SHORT).show();
			} finally {
				try { formatable.close(); } catch (Exception e) {}
			}
		}
	}

	private NdefMessage getNdefMessage() {
		NdefContact.Builder builder = new NdefContact.Builder()
		.appendName(mCardFormFragment.getContactName());

		ValueType[] phones = mCardFormFragment.getContactPhones();
		for (int p = 0; p < phones.length; p += 1) {
			if (phones[p].getValue().length() > 0) {
				builder.appendPhone(phones[p].getType(), phones[p].getValue());
			}
		}

		ValueType[] emails = mCardFormFragment.getContactEmails();
		for (int e = 0; e < emails.length; e += 1) {
			if (emails[e].getValue().length() > 0) {
				builder.appendEmail(emails[e].getValue());
			}
		}

		String organisation = mCardFormFragment.getContactOrganisation();
		if (organisation.length() > 0) {
			builder.appendOrg(organisation);
		}
		
		String title = mCardFormFragment.getContactTitle();
		if (title.length() > 0) {
			builder.appendTitle(title);
		}
		
		NdefRecord[] records = { builder.build().toNdefRecord() };
		NdefMessage message = new NdefMessage(records);
	
		
		return message;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (isOnSmallScreen()) {
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_contacts:
			switchToPickContactFragment();
			return true;
		default: 
			return false;
		}
	}
}
