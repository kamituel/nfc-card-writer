package pl.kamituel.nfcbusinesscardwriter.ui;

import pl.kamituel.nfcbusinesscardwriter.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;

public class ClearableEditText extends EditText implements OnTouchListener {

	private Drawable mRightCompoundDrawable;
	private OnClearListener mClearListener;
	
	public static interface OnClearListener {
		public void textCleared(View v);
	}
	
	public ClearableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public void setOnClearListener (OnClearListener listener) {
		mClearListener = listener;
	}
	
	protected void init() {
		mRightCompoundDrawable = getCompoundDrawables()[2];
		if (mRightCompoundDrawable == null) {
			mRightCompoundDrawable = getResources().getDrawable(R.drawable.ic_clear_normal);
		}
		
		Log.d("xxx", "w,h" + mRightCompoundDrawable.getIntrinsicWidth() + " , " + getWidth());
		mRightCompoundDrawable.setBounds(0, 0, mRightCompoundDrawable.getIntrinsicWidth(), mRightCompoundDrawable.getIntrinsicHeight());
		setClearIconVisible(false);
		
		setOnTouchListener(this);
	}
	
	@Override
	protected void onTextChanged(CharSequence text, int start,
			int lengthBefore, int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		
		setClearIconVisible(getText().length() > 0);
	}

	protected void setClearIconVisible (boolean visible) {
		Drawable right = visible ? mRightCompoundDrawable : null;
		setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], 
				right, getCompoundDrawables()[3]);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (getCompoundDrawables()[2] != null) {
			boolean tappedClearButton = event.getX() > (getWidth() - getPaddingRight() - mRightCompoundDrawable.getIntrinsicWidth());
			if (tappedClearButton) {
				if (MotionEvent.ACTION_UP == event.getAction()) {
					setText("");
					notifyListener();
				}
				return true;
			}
		}
		return false;
	}

	protected void notifyListener () {
		if (mClearListener != null) {
			mClearListener.textCleared(this);
		}
	}
	
}
