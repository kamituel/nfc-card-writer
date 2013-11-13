package pl.kamituel.nfcbusinesscardwriter.ui;

import pl.kamituel.nfcbusinesscardwriter.ContactFieldArrayAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class LinearLayoutList extends LinearLayout {
	private ContactFieldArrayAdapter mAdapter;
	private CustomDataSetObserver mDataSetObserver;
	
	public LinearLayoutList(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mDataSetObserver = new CustomDataSetObserver();
	}

	public void setAdapter (ContactFieldArrayAdapter adapter) {
		if (null != mAdapter) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}
		
		removeAllViews();
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataSetObserver);
		
		addAllFromAdapter();
	}
	
	protected void addAllFromAdapter() {
		for (int v = 0; v < mAdapter.getCount(); v += 1) {
			View view = mAdapter.getView(v, null, this);
			addView(view);
		}
	}
	
	public class CustomDataSetObserver {
		public void onRemoved(int position) {
			if (position < 0) {
				removeAllViews();
			} else {
				removeViewAt(position);
			}
		}
		
		public void onAdded() {
			Log.d("pl.kamituel", "eee2 " + mAdapter.getCount());
			addView(mAdapter.getView(mAdapter.getCount() - 1, null, LinearLayoutList.this));
		}
	}
}
