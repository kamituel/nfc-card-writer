package pl.kamituel.nfcbusinesscardwriter;

import java.util.ArrayList;

import pl.kamituel.nfcbusinesscardwriter.ContactCursorHelper.ValueType;
import pl.kamituel.nfcbusinesscardwriter.ui.IconEditText;
import pl.kamituel.nfcbusinesscardwriter.ui.IconEditText.OnIconClickListener;
import pl.kamituel.nfcbusinesscardwriter.ui.LinearLayoutList.CustomDataSetObserver;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

public class ContactFieldArrayAdapter extends BaseAdapter /*implements OnClickListener*/ implements OnIconClickListener {	
	private final ArrayList<ValueType> mItems = new ArrayList<ValueType>();
	private LayoutInflater mInflater;
	
	private int mRootLayoutId;
	private int mEditTextId;
	
	public static interface OnAddNewItemTextChangedListener {
		void onTextInserted ();
	}
	
	private OnAddNewItemTextChangedListener mAddNewItemPopulatedListener;
	private CustomDataSetObserver mDataSetObserver;
	
	public ContactFieldArrayAdapter (Context ctx, int rootLayoutId, int editTextId) {
		mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRootLayoutId = rootLayoutId;
		mEditTextId = editTextId;
	}
	
	public void add (ValueType item) {		
		mItems.add(item);
		notifyDataSetAdded();
	}
	
	public void addAll(ValueType[] items) {
		for (int i = items.length - 1; i >= 0; i -= 1) {
			mItems.add(items[i]);
			notifyDataSetAdded();
		}
	}
	
	public void removeAll() {
		mItems.clear();
		notifyDataSetRemoved(-1);
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public ValueType getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	public void setAddNewItemPopulatedListener (OnAddNewItemTextChangedListener listener) {
		mAddNewItemPopulatedListener = listener;
	}
	
	protected void addNewItemPopulated () {
		if (mAddNewItemPopulatedListener != null) {
			mAddNewItemPopulatedListener.onTextInserted();
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ValueType item = getItem(position);
		
		View view;
		if (convertView == null) {
			view = mInflater.inflate(mRootLayoutId, parent, false);
		} else {
			view = convertView;
		}
		
		IconEditText value = (IconEditText) view.findViewById(mEditTextId);		
		value.setText(item.getValue());
		Log.d("pl.kamituel", position + " eee " + value.getText().toString() + " - " + mItems.size());
		value.setOnIconClickListener(this);
		
		value.setTag(item);
		ensureRowRemovedWhenEmpty(value);

		
		return view;
	}
	
	private void ensureRowRemovedWhenEmpty (final EditText value) {
		value.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				int position = mItems.indexOf(value.getTag());
				
				if (s.length() == 0 && mItems.size() > 1) {
					mItems.remove(position);
					notifyDataSetRemoved(position);
				}
				
				if (s.length() > 0) {
					getItem(position).mValue = s.toString();
					
					if (position == getCount() - 1) {
						addNewItemPopulated();
					}
				/*} else {
					if (getCount() == 1) {
						addNewItemEmptied();
					}*/
				}
			}
		});
	}

	@Override
	public void iconClicked(IconEditText v) {
		if (v.getText().length() > 0) {
			v.setText("");
		}
	}

	public void unregisterDataSetObserver(CustomDataSetObserver dataSetObserver) {
		mDataSetObserver = dataSetObserver;
	}

	public void registerDataSetObserver(CustomDataSetObserver dataSetObserver) {
		mDataSetObserver = dataSetObserver;
	}

	public void notifyDataSetAdded() {
		Log.d("pl.kamituel", "???" + mDataSetObserver + " = " + mItems.size());
		if (mDataSetObserver != null) {
			mDataSetObserver.onAdded();
		}
	}

	public void notifyDataSetRemoved(int position) {
		if (mDataSetObserver != null) {
			mDataSetObserver.onRemoved(position);
		}
	}
}
