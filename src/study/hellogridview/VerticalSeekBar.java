package study.hellogridview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;
 
public class VerticalSeekBar extends SeekBar {
 
  public OnSeekBarChangeListener mlistener;
  public VerticalSeekBar(Context context) {
      super(context);
    }
 
  public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
    }
 
    public VerticalSeekBar(Context context,AttributeSet attrs) {
      super(context, attrs);
    }
 
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      super.onSizeChanged(h, w, oldh, oldw);
    }
 
  @Override
  protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      super.onMeasure(heightMeasureSpec, widthMeasureSpec);
      setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }
 
  protected void onDraw(Canvas c) {
      c.rotate(-90);
      c.translate(-getHeight(),0);
      
//      Rect barbound;
//      barbound.left = 
 
      super.onDraw(c);
    }
 
  @SuppressLint("ClickableViewAccessibility")
@Override
  public boolean onTouchEvent(MotionEvent event) {
      if (!isEnabled()) {
          return false;
      }
 
      switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
        	  mlistener.onStartTrackingTouch(this);
        	  break;
          case MotionEvent.ACTION_MOVE:
        	  int i=0;
              i=getMax() - (int)(getMax() * event.getY() / getHeight());
              setProgress(i);
              Log.i("Progress",getProgress()+" i = " +i);
              onSizeChanged(getWidth(), getHeight(), 0, 0);
              break;
          case MotionEvent.ACTION_UP:
              mlistener.onStopTrackingTouch(this);
              break;
 
          case MotionEvent.ACTION_CANCEL:
        	  mlistener.onStopTrackingTouch(this);
              break;
      }
      return true;
  }
  
}
