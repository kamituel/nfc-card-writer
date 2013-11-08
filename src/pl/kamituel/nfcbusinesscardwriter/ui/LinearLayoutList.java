package pl.kamituel.nfcbusinesscardwriter.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

public class LinearLayoutList extends LinearLayout {
	private ListAdapter mAdapter;
	private DataSetObserverImpl mDataSetObserver;
	
	public LinearLayoutList(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mDataSetObserver = new DataSetObserverImpl();
	}

	public void setAdapter (ListAdapter adapter) {
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
			addView(mAdapter.getView(v, null, this));
		}
	}
	
	private class DataSetObserverImpl extends DataSetObserver {
		@Override
		public void onChanged() {
			super.onChanged();
			
			// TODO: optimize
			removeAllViews();
			addAllFromAdapter();
		}

		@Override
		public void onInvalidated() {
			// TODO Auto-generated method stub
			super.onInvalidated();
		}
		
	}
}
