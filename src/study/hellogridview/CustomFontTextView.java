package study.hellogridview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomFontTextView extends TextView {
	
	public 	CustomFontTextView (Context context) {
		super(context);
		init(context);
	}
	
	public CustomFontTextView(Context context, AttributeSet aset) {
		super(context, aset);
		init(context);
	}
	
	public CustomFontTextView(Context context, AttributeSet aset, int defStyle) {
		super(context, aset, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		setTypeface(MainActivity.typeFace_fzzy);
	}

}
