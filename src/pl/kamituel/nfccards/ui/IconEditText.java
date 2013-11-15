package pl.kamituel.nfccards.ui;

import pl.kamituel.nfccards.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;

public class IconEditText extends EditText implements OnTouchListener {

	private int mRightCompoundDrawableId;
	private Drawable mRightCompoundDrawable;
	private OnIconClickListener mOnIconClickListener;
	
	public static interface OnIconClickListener {
		public void iconClicked(IconEditText v);
	}
	
	public IconEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.IconEditText);
		mRightCompoundDrawableId = styledAttrs.getResourceId(R.styleable.TypefacedTextView_typeface, R.drawable.ic_clear_normal);
        styledAttrs.recycle();
		
		init();
	}
	
	public void setOnIconClickListener (OnIconClickListener listener) {
		mOnIconClickListener = listener;
	}
	
	protected void init() {
		mRightCompoundDrawable = getCompoundDrawables()[2];
		if (mRightCompoundDrawable == null) {
			mRightCompoundDrawable = getResources().getDrawable(mRightCompoundDrawableId);
		}
		
		Log.d("xxx", "w,h" + mRightCompoundDrawable.getIntrinsicWidth() + " , " + getWidth());
		mRightCompoundDrawable.setBounds(0, 0, 30, 30);
		setIconVisible(false);
		
		setOnTouchListener(this);
	}
	
	@Override
	protected void onTextChanged(CharSequence text, int start,
			int lengthBefore, int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		
		setIconVisible(getText().length() > 0);
	}

	protected void setIconVisible (boolean visible) {
		Drawable right = visible ? mRightCompoundDrawable : null;
		setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], 
				right, getCompoundDrawables()[3]);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (getCompoundDrawables()[2] != null) {
			boolean tappedIcon = event.getX() > (getWidth() - getPaddingRight() - mRightCompoundDrawable.getIntrinsicWidth());
			if (tappedIcon) {
				if (MotionEvent.ACTION_UP == event.getAction()) {
					notifyOnIconClickListener();
				}
				return true;
			}
		}
		return false;
	}

	protected void notifyOnIconClickListener () {
		if (mOnIconClickListener != null) {
			mOnIconClickListener.iconClicked(this);
		}
	}
	
}
