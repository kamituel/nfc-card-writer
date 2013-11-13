package pl.kamituel.nfcbusinesscardwriter;

import java.util.ArrayList;

import pl.kamituel.nfcbusinesscardwriter.ContactCursorHelper.ValueType;
import pl.kamituel.nfcbusinesscardwriter.ui.IconEditText;
import pl.kamituel.nfcbusinesscardwriter.ui.IconEditText.OnIconClickListener;
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
		void onTextRemoved();
	}
	
	private OnAddNewItemTextChangedListener mAddNewItemPopulatedListener;
	
	public ContactFieldArrayAdapter (Context ctx, int rootLayoutId, int editTextId) {
		mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRootLayoutId = rootLayoutId;
		mEditTextId = editTextId;
	}
	
	public void add (ValueType item) {		
		mItems.add(item);
		notifyDataSetChanged();
	}
	
	public void addAll(ValueType[] items) {
		for (int i = items.length - 1; i >= 0; i -= 1) {
			Log.d("xxx", "addding element " + items[i].getValue());
			mItems.add(0, items[i]);
		}
		notifyDataSetChanged();
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
	
	protected void addNewItemEmptied() {
		if (mAddNewItemPopulatedListener != null) {
			mAddNewItemPopulatedListener.onTextRemoved();
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ValueType item = getItem(position);
		View view = mInflater.inflate(mRootLayoutId, parent, false);
		IconEditText value = (IconEditText) view.findViewById(mEditTextId);
		value.setTag(position);
		
		/*ImageButton remove = (ImageButton) view.findViewById(mRemoveItemButtonId);
		remove.setTag(position);
		remove.setOnClickListener(this);*/
		
		value.setText(item.getValue());
		ensureRowRemovedWhenEmpty(position, value);
		value.setOnIconClickListener(this);
		
		
		
		return view;
	}
	
	private void ensureRowRemovedWhenEmpty (final int position, EditText value) {
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
				if (s.length() == 0 && mItems.size() > 1) {
					mItems.remove(position);
					notifyDataSetChanged();
				}
				
				if (s.length() > 0) {
					getItem(position).mValue = s.toString();
					
					if (position == getCount() - 1) {
						addNewItemPopulated();
					}
				} else {
					if (getCount() == 1) {
						addNewItemEmptied();
					}
				}
			}
		});
	}

	@Override
	public void iconClicked(IconEditText v) {
		if (v.getText().length() > 0) {
			v.setText("");
			if (mItems.size() > 1) {
				int position = (Integer) v.getTag();
				mItems.remove(position);
				notifyDataSetChanged();
			} else {
				mItems.get(0).mValue = "";
			}
		}
	}

	/*@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		
		if (mItems.size() > 1) {
			mItems.remove(position);
		} else {
			mItems.get(0).mValue = "";
		}
		
		notifyDataSetChanged();
	}*/
}
