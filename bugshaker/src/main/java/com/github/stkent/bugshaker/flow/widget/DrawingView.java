package com.github.stkent.bugshaker.flow.widget;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.github.stkent.bugshaker.R;

public class DrawingView extends View {

	//drawing path
	private Path mDrawPath;
	//drawing and canvas paint
	private Paint mDrawPaint, mCanvasPaint;
	//initial color
	private int mPaintColor = 0xFF660000;
	//canvas
	private Canvas mDrawCanvas;
	//canvas bitmap
	private Bitmap mCanvasBitmap;

	private float mBrushSize, mLastBrushSize;
	private boolean isFilling = false;  //for flood fill

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupDrawing();
	}

	//get drawing area setup for interaction
	private void setupDrawing() {

		mBrushSize = getResources().getInteger(R.integer.medium_size);
		mLastBrushSize = mBrushSize;

		mDrawPath = new Path();

		mDrawPaint = new Paint();
		mDrawPaint.setColor(mPaintColor);
		mDrawPaint.setAntiAlias(true);
		mDrawPaint.setStrokeWidth(mBrushSize);
		mDrawPaint.setStyle(Paint.Style.STROKE);
		mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
		mDrawPaint.setStrokeCap(Paint.Cap.ROUND);

		mCanvasPaint = new Paint(Paint.DITHER_FLAG);
	}

	//view given size
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		super.onSizeChanged(w, h, oldw, oldh);

		mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mDrawCanvas = new Canvas(mCanvasBitmap);
	}

	//draw view
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mCanvasBitmap, 0, 0, mCanvasPaint);
		canvas.drawPath(mDrawPath, mDrawPaint);
	}

	//detect user touch
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float touchX = event.getX();
		float touchY = event.getY();

		if (isFilling) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				FloodFill(new Point((int) touchX, (int) touchY));
				break;

			default:
				return true;
			}
		} else {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mDrawPath.moveTo(touchX, touchY);
				break;

			case MotionEvent.ACTION_MOVE:
				mDrawPath.lineTo(touchX, touchY);
				break;

			case MotionEvent.ACTION_UP:
				mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
				mDrawPath.reset();
				break;

			default:
				return false;
			}
		}

		invalidate();
		return true;
	}

	//set color
	public void setColor(String newColor) {
		invalidate();

		mPaintColor = Color.parseColor(newColor);
		mDrawPaint.setColor(mPaintColor);
		mDrawPaint.setShader(null);
	}

	//set pattern
	public void setPattern(String newPattern) {
		invalidate();

		int patternID = getResources().getIdentifier(newPattern, "drawable", "com.example.ankit.drawingfun");

		Bitmap patternBMP = BitmapFactory.decodeResource(getResources(), patternID);

		BitmapShader patternBMPshader = new BitmapShader(patternBMP,
			Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

		mDrawPaint.setColor(0xFFFFFFFF);
		mDrawPaint.setShader(patternBMPshader);
	}

	//update size
	public void setBrushSize(float newSize) {
		mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
			newSize, getResources().getDisplayMetrics());
		mDrawPaint.setStrokeWidth(mBrushSize);
	}

	public void setLastBrushSize(float lastSize) {
		mLastBrushSize = lastSize;
	}

	public float getLastBrushSize() {
		return mLastBrushSize;
	}

	//set mErase true or false
	public void setErase(boolean isErase) {
		if (isErase) {
			mDrawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

		} else {
			mDrawPaint.setXfermode(null);
		}
	}

	//clear canvas
	public void startNew() {
		mDrawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		invalidate();
	}

	//fill effect
	public void fillColor() {
		isFilling = true;
	}

	private synchronized void FloodFill(Point startPoint) {

		Queue<Point> queue = new LinkedList<>();
		queue.add(startPoint);

		int targetColor = mCanvasBitmap.getPixel(startPoint.x, startPoint.y);

		while (queue.size() > 0) {
			Point nextPoint = queue.poll();
			if (mCanvasBitmap.getPixel(nextPoint.x, nextPoint.y) != targetColor)
				continue;

			Point point = new Point(nextPoint.x + 1, nextPoint.y);

			while ((nextPoint.x > 0) && (mCanvasBitmap.getPixel(nextPoint.x, nextPoint.y) == targetColor)) {
				mCanvasBitmap.setPixel(nextPoint.x, nextPoint.y, mPaintColor);
				if ((nextPoint.y > 0) && (mCanvasBitmap.getPixel(nextPoint.x, nextPoint.y - 1) == targetColor))
					queue.add(new Point(nextPoint.x, nextPoint.y - 1));
				if ((nextPoint.y < mCanvasBitmap.getHeight() - 1)
					&& (mCanvasBitmap.getPixel(nextPoint.x, nextPoint.y + 1) == targetColor))
					queue.add(new Point(nextPoint.x, nextPoint.y + 1));
				nextPoint.x--;
			}

			while ((point.x < mCanvasBitmap.getWidth() - 1)
				&& (mCanvasBitmap.getPixel(point.x, point.y) == targetColor)) {
				mCanvasBitmap.setPixel(point.x, point.y, mPaintColor);

				if ((point.y > 0) && (mCanvasBitmap.getPixel(point.x, point.y - 1) == targetColor))
					queue.add(new Point(point.x, point.y - 1));
				if ((point.y < mCanvasBitmap.getHeight() - 1)
					&& (mCanvasBitmap.getPixel(point.x, point.y + 1) == targetColor))
					queue.add(new Point(point.x, point.y + 1));
				point.x++;
			}
		}

		isFilling = false;
	}
}













//package com.github.stkent.bugshaker.flow.widget;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Path;
//import android.graphics.Point;
//import android.util.AttributeSet;
//import android.util.TypedValue;
//import android.view.MotionEvent;
//import android.view.View;
//
//import com.github.stkent.bugshaker.R;
//
///**
// * Created by rshanahan on 7/15/16.
// */
//public class DrawingView extends View {
//	public DrawingView(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		setupDrawing();
//	}
//
//	private boolean erase = false;
//	//drawing path
//	private Path drawPath;
//	//drawing and canvas paint
//	private Paint drawPaint, canvasPaint;
//	//initial color
//	private int paintColor = 0xFF660000;
//	//canvas
//	private Canvas drawCanvas;
//	//canvas bitmap
//	private Bitmap canvasBitmap;
//	private float brushSize, lastBrushSize;
//
//	private void setupDrawing() {
//		brushSize = getResources().getInteger(R.integer.medium_size);
//		lastBrushSize = brushSize;
//
//		drawPath = new Path();
//		drawPaint = new Paint();
//		drawPaint.setColor(paintColor);
//		drawPaint.setAntiAlias(true);
//		drawPaint.setStrokeWidth(brushSize);
//		drawPaint.setStyle(Paint.Style.STROKE);
//		drawPaint.setStrokeJoin(Paint.Join.ROUND);
//		drawPaint.setStrokeCap(Paint.Cap.ROUND);
//
//		//instantiating the canvas paint object
//		canvasPaint = new Paint(Paint.DITHER_FLAG);
//
//
//	}
//
////	public void setErase(boolean isErase){
////		//set erase true or false
////
////		erase = isErase;
////		if(erase) {
////			drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
////		}
////		else drawPaint.setXfermode(null);
////	}
//
//	public void setErase(boolean isErase){
//		//set erase true or false
//
//		erase = isErase;
//		if(erase) {
//			drawPaint.setColor(Color.WHITE);
//		}
//
//		else drawPaint.setColor(paintColor);
//	}
//
//
//	public void setBrushSize(float newSize) {
//		//update size
//		float pixelAmount = TypedValue
//			.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
//		brushSize = pixelAmount;
//		drawPaint.setStrokeWidth(brushSize);
//	}
//
//	public void setLastBrushSize(float lastSize) {
//		lastBrushSize = lastSize;
//	}
//
//	public float getLastBrushSize(){
//
//	return lastBrushSize;
//}
//	@Override
//	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//		super.onSizeChanged(w,h,oldw,oldh);
//		canvasBitmap=Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888);
//		drawCanvas = new Canvas(canvasBitmap);
//	//view given size
//	}
//
//	@Override
//	protected void onDraw(Canvas canvas){
//		//draw view
//		canvas.drawBitmap(canvasBitmap, 1, 1, canvasPaint);
//		canvas.drawPath(drawPath, drawPaint);
//	}
//
//
//	//detect user touch
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		float touchX = event.getX();
//		float touchY = event.getY();
//
//		if (isFilling) {
//			switch (event.getAction()) {
//			case MotionEvent.ACTION_UP:
//				FloodFill(new Point((int) touchX, (int) touchY));
//				break;
//
//			default:
//				return true;
//			 }
//			} else {
//			            switch (event.getAction()) {
//			                case MotionEvent.ACTION_DOWN:
//				                    mDrawPath.moveTo(touchX, touchY);
//				                    break;
//
//			                case MotionEvent.ACTION_MOVE:
//				                    mDrawPath.lineTo(touchX, touchY);
//				                    break;
//
//			                case MotionEvent.ACTION_UP:
//				                    mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
//				                    mDrawPath.reset();
//				                    break;
//
//			                default:
//				                    return false;
//			            }
//			        }
//
//		        invalidate();
//		        return true;
//		    }
//
//
//
////	@Override
////	public boolean onTouchEvent(MotionEvent event){
////		//detect user touch
////		float touchX = event.getX();
////		float touchY = event.getY();
////
////		switch(event.getAction()) {
////		case MotionEvent.ACTION_DOWN:
////			drawPath.moveTo(touchX, touchY);
////			break;
////		case MotionEvent.ACTION_MOVE:
////			drawPath.lineTo(touchX, touchY);
////			break;
////		case MotionEvent.ACTION_UP:
////			drawCanvas.drawPath(drawPath, drawPaint);
////			drawPath.reset();
////			break;
////		default:
////			return false;
////
////		}
////		invalidate();
////		return true;
////	}
//
//	public void setColor(String newColor){
//		//set color
//		invalidate();
//
//		paintColor = Color.parseColor(newColor);
//		drawPaint.setColor(paintColor);
//	}
//
//
//}


