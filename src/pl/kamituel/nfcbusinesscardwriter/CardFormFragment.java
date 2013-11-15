package pl.kamituel.nfcbusinesscardwriter;

import java.util.ArrayList;
import java.util.Arrays;

import pl.kamituel.nfcbusinesscardwriter.ContactCursorHelper.ValueType;
import pl.kamituel.nfcbusinesscardwriter.ContactFieldArrayAdapter.OnAddNewItemTextChangedListener;
import pl.kamituel.nfcbusinesscardwriter.ui.IconEditText;
import pl.kamituel.nfcbusinesscardwriter.ui.IconEditText.OnIconClickListener;
import pl.kamituel.nfcbusinesscardwriter.ui.LinearLayoutList;
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
		
		return layout;
	}

	private Bundle mSavedInstanceState;
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// For some reason, this throws an exception
		/*if (savedInstanceState != null) {
			populateEditorName(savedInstanceState.getString(BUNDLE_CONTACT_NAME));
			
			Parcelable[] phones = savedInstanceState.getParcelableArray(BUNDLE_CONTACT_PHONES);
			populateEditorPhones(Arrays.copyOf(phones, phones.length, ValueType[].class));
			
			Parcelable[] emails = savedInstanceState.getParcelableArray(BUNDLE_CONTACT_EMAILS);
			populateEditorEmails(Arrays.copyOf(emails, emails.length, ValueType[].class));
		}*/
		
		// Workaround:
		mSavedInstanceState = savedInstanceState;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mSavedInstanceState != null) {
			populateEditorName(mSavedInstanceState.getString(BUNDLE_CONTACT_NAME));
			
			Parcelable[] phones = mSavedInstanceState.getParcelableArray(BUNDLE_CONTACT_PHONES);
			populateEditorPhones(Arrays.copyOf(phones, phones.length, ValueType[].class));
			
			Parcelable[] emails = mSavedInstanceState.getParcelableArray(BUNDLE_CONTACT_EMAILS);
			populateEditorEmails(Arrays.copyOf(emails, emails.length, ValueType[].class));
		}
		
		if (mContact != null) {
			clearEditorFields();
			populateEditorFields(mContact);
			mContact = null;
		} else {
			mPhonesAdapter.add(new ValueType("", ""));
			mEmailsAdapter.add(new ValueType("", ""));
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
						
		outState.putString(BUNDLE_CONTACT_NAME, getContactName());
		outState.putParcelableArray(BUNDLE_CONTACT_PHONES, getContactPhones());
		outState.putParcelableArray(BUNDLE_CONTACT_EMAILS, getContactEmails());
	}

	private void setupFieldList (LinearLayoutList list, final ContactFieldArrayAdapter adapter) {
		list.setAdapter(adapter);

		adapter.setAddNewItemPopulatedListener(new OnAddNewItemTextChangedListener() {
			@Override
			public void onTextInserted() {
				adapter.add(new ValueType("", ""));
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

		mPhonesAdapter.add(new ValueType("", ""));
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
	
	

}
