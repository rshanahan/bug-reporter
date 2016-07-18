package com.github.stkent.bugshaker.flow.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.github.stkent.bugshaker.R;

/**
 * Created by rshanahan on 7/15/16.
 */
public class DrawingView extends View {
	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupDrawing();
	}

	private boolean erase = false;
	//drawing path
	private Path drawPath;
	//drawing and canvas paint
	private Paint drawPaint, canvasPaint;
	//initial color
	private int paintColor = 0xFF660000;
	//canvas
	private Canvas drawCanvas;
	//canvas bitmap
	private Bitmap canvasBitmap;
	private float brushSize, lastBrushSize;

	private void setupDrawing() {
		brushSize = getResources().getInteger(R.integer.medium_size);
		lastBrushSize = brushSize;

		drawPath = new Path();
		drawPaint = new Paint();
		drawPaint.setColor(paintColor);
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(brushSize);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);

		//instantiating the canvas paint object
		canvasPaint = new Paint(Paint.DITHER_FLAG);


	}



	public void setErase(boolean isErase){
		//set erase true or false

		erase = isErase;
		if(erase) {
			drawPaint.setColor(Color.WHITE);
		}

		else drawPaint.setColor(paintColor);
	}


	public void setBrushSize(float newSize) {
		//update size
		float pixelAmount = TypedValue
			.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
		brushSize = pixelAmount;
		drawPaint.setStrokeWidth(brushSize);
	}

	public void setLastBrushSize(float lastSize) {
		lastBrushSize = lastSize;
	}

	public float getLastBrushSize(){

	return lastBrushSize;
}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w,h,oldw,oldh);
		canvasBitmap=Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
	//view given size
	}

	@Override
	protected void onDraw(Canvas canvas){
		//draw view
		canvas.drawBitmap(canvasBitmap, 1, 1, canvasPaint);
		canvas.drawPath(drawPath, drawPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		//detect user touch
		float touchX = event.getX();
		float touchY = event.getY();

		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			drawPath.moveTo(touchX, touchY);
			break;
		case MotionEvent.ACTION_MOVE:
			drawPath.lineTo(touchX, touchY);
			break;
		case MotionEvent.ACTION_UP:
			drawCanvas.drawPath(drawPath, drawPaint);
			drawPath.reset();
			break;
		default:
			return false;

		}
		invalidate();
		return true;
	}

	public void setColor(String newColor){
		//set color
		invalidate();

		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
	}


}


