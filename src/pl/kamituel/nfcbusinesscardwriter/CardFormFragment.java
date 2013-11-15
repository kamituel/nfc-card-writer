package pl.kamituel.nfcbusinesscardwriter;

import pl.kamituel.nfcbusinesscardwriter.ContactCursorHelper.ValueType;
import pl.kamituel.nfcbusinesscardwriter.ContactFieldArrayAdapter.OnAddNewItemTextChangedListener;
import pl.kamituel.nfcbusinesscardwriter.ui.IconEditText;
import pl.kamituel.nfcbusinesscardwriter.ui.IconEditText.OnIconClickListener;
import pl.kamituel.nfcbusinesscardwriter.ui.LinearLayoutList;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class CardFormFragment extends Fragment implements OnIconClickListener {
	private ContactFieldArrayAdapter mPhonesAdapter;
	private ContactFieldArrayAdapter mEmailsAdapter;

	private CardFormFragmentListener mParent;
	private ContactCursorHelper mContact;

	public interface CardFormFragmentListener {
		//public void searchContact(String displayName);
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
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (mContact != null) {
			clearEditorFields();
			populateEditorFields(mContact);
		} else {
			mPhonesAdapter.add(new ValueType("", ""));
			mEmailsAdapter.add(new ValueType("", ""));
		}
	}

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
		((IconEditText) getView().findViewById(R.id.nameEditText)).setText(contact.getDisplayName());

		ValueType[] phones = contact.getPhoneNumbers();
		mPhonesAdapter.addAll(phones);

		ValueType[] emails = contact.getEmailAddresses();
		mEmailsAdapter.addAll(emails);

		mPhonesAdapter.add(new ValueType("", ""));
		mEmailsAdapter.add(new ValueType("", ""));
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
		ValueType[] arr = new ValueType[adapter.getCount()];
		for (int p = 0; p < arr.length; p += 1) {
			arr[p] = new ValueType(adapter.getItem(p).mValue, adapter.getItem(p).mValue);
		}
		
		return arr;
	}

	public String getContactName() {
		return ((EditText) getView().findViewById(R.id.nameEditText)).getText().toString();
	}

}
