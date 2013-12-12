package pl.kamituel.nfccards;

import java.util.ArrayList;
import java.util.Arrays;

import pl.kamituel.nfccards.ContactCursorHelper.ValueType;
import pl.kamituel.nfccards.ContactFieldArrayAdapter.OnAddNewItemTextChangedListener;
import pl.kamituel.nfccards.ui.IconEditText;
import pl.kamituel.nfccards.ui.IconEditText.OnIconClickListener;
import pl.kamituel.nfccards.ui.LinearLayoutList;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class CardFormFragment extends Fragment implements OnIconClickListener {
	private ContactFieldArrayAdapter mPhonesAdapter;
	private ContactFieldArrayAdapter mEmailsAdapter;

	private CardFormFragmentListener mParent;
	private ContactCursorHelper mContact;
	
	private final static String BUNDLE_CONTACT_NAME = "contact-name";
	private final static String BUNDLE_CONTACT_PHONES = "contact-phones";
	private final static String BUNDLE_CONTACT_EMAILS = "contact-emails";

	// I don't use onSaveInstanceState(), because it's not always called
	// (i.e. when this fragment is being replaced using FragmentManager.replace()).
	private Bundle mSavedInstanceState;
	
	public interface CardFormFragmentListener {
		//public void searchContact(String displayName);
	}
	
	//TODO: do I need this?
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mParent = (CardFormFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement " 
					+ CardFormFragmentListener.class.getSimpleName() + " interface");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO: 
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.card_form, container, false);
				
		mPhonesAdapter = new ContactFieldArrayAdapter(getActivity(), R.layout.contact_editor_field_phone, R.id.phoneEditText);
		mEmailsAdapter = new ContactFieldArrayAdapter(getActivity(), R.layout.contact_editor_field_email, R.id.emailEditText);
		setupFieldList((LinearLayoutList) layout.findViewById(R.id.phoneList), mPhonesAdapter);
		setupFieldList((LinearLayoutList) layout.findViewById(R.id.emailList), mEmailsAdapter);

		IconEditText name = (IconEditText) layout.findViewById(R.id.nameEditText);
		name.setOnIconClickListener(this);
		
		// Used on rotation.
		if (savedInstanceState != null) {
			mSavedInstanceState = savedInstanceState;
		}
		
		return layout;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		saveState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Restore state on screen rotation
		if (mSavedInstanceState != null) {
			populateEditorName(mSavedInstanceState.getString(BUNDLE_CONTACT_NAME));
			
			Parcelable[] phones = mSavedInstanceState.getParcelableArray(BUNDLE_CONTACT_PHONES);
			mPhonesAdapter.removeAll();
			populateEditorPhones(Arrays.copyOf(phones, phones.length, ValueType[].class));
			
			Parcelable[] emails = mSavedInstanceState.getParcelableArray(BUNDLE_CONTACT_EMAILS);
			mEmailsAdapter.removeAll();
			populateEditorEmails(Arrays.copyOf(emails, emails.length, ValueType[].class));
		}
		
		if (mContact != null) {
			clearEditorFields();
			populateEditorFields(mContact);
			mContact = null;
		} else {
			mPhonesAdapter.add(new ValueType("", NdefContact.Builder.PHONE_TYPE_WORK));
			mEmailsAdapter.add(new ValueType("", ""));
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();

		// Used when another fragments is being pushed in front of this one.
		mSavedInstanceState = new Bundle();
		saveState(mSavedInstanceState);
	}
	
	private void saveState(Bundle bundle) {
		bundle.putString(BUNDLE_CONTACT_NAME, getContactName());
		bundle.putParcelableArray(BUNDLE_CONTACT_PHONES, getContactPhones());
		bundle.putParcelableArray(BUNDLE_CONTACT_EMAILS, getContactEmails());
	}

	private void setupFieldList (LinearLayoutList list, final ContactFieldArrayAdapter adapter) {
		list.setAdapter(adapter);

		adapter.setAddNewItemPopulatedListener(new OnAddNewItemTextChangedListener() {
			@Override
			public void onTextInserted() {
				if (adapter == mPhonesAdapter) {
					adapter.add(new ValueType("", NdefContact.Builder.PHONE_TYPE_WORK));
				} else {
					adapter.add(new ValueType("", ""));
				}
			}
		});
	}

	@Override
	public void iconClicked(IconEditText v) {
		if (R.id.nameEditText == v.getId()) {
			v.setText("");
		}
	}
	
	public void setContact(ContactCursorHelper contact) {
		mContact = contact;
	}
	
	public void fillForm() {
		clearEditorFields();
		populateEditorFields(mContact);
	}

	private void populateEditorFields(ContactCursorHelper contact) {
		populateEditorName(contact.getDisplayName());
		populateEditorPhones(contact.getPhoneNumbers());
		populateEditorEmails(contact.getEmailAddresses());

		mPhonesAdapter.add(new ValueType("", NdefContact.Builder.PHONE_TYPE_WORK));
		mEmailsAdapter.add(new ValueType("", ""));
	}
	
	private void populateEditorPhones(ValueType[] phones) {
		for (int p = 0; p < phones.length; p++) {
			Log.e("xxx", "phone " + p + " = " + phones[p].mType + " -> " + phones[p].mValue);
		}
		mPhonesAdapter.addAll(phones);
	}
	
	private void populateEditorEmails(ValueType[] emails) {
		mEmailsAdapter.addAll(emails);
	}
	
	private void populateEditorName(String name) {
		((IconEditText) getView().findViewById(R.id.nameEditText)).setText(name);
	}

	private void clearEditorFields() {
		mPhonesAdapter.removeAll();
		mEmailsAdapter.removeAll();
	}

	public ValueType[] getContactPhones() {
		return getAdapter(mPhonesAdapter);
	}

	public ValueType[] getContactEmails() {
		return getAdapter(mEmailsAdapter);
	}
	
	private static ValueType[] getAdapter(ContactFieldArrayAdapter adapter) {
		ArrayList<ValueType> res = new ArrayList<ValueType>(adapter.getCount());
		for (int p = 0; p < adapter.getCount(); p += 1) {
			if (adapter.getItem(p).getValue().length() > 0) {
				res.add(adapter.getItem(p));
			}
		}
		
		return res.toArray(new ValueType[] {});
	}

	public String getContactName() {
		return ((EditText) getView().findViewById(R.id.nameEditText)).getText().toString();
	}

	public String getContactOrganisation() {
		return ((EditText) getView().findViewById(R.id.organisationEditText)).getText().toString();
	}

	public String getContactTitle() {
		return ((EditText) getView().findViewById(R.id.titleEditText)).getText().toString();

	}
	
	

}
