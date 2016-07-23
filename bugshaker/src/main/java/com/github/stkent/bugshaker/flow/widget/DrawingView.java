package com.github.stkent.bugshaker.flow.widget;

import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.stkent.bugshaker.R;

public class DrawingView extends ViewGroup {

	public static final String PREFS_NAME3 = "name3";
	public static final String PREFS_KEY3 = "key3";

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
	private Context context1;
	private Bitmap combined;

	private EditText editText;

	private float brushSize, lastBrushSize;
	private boolean isFilling = false;  //for flood fill

	public DrawingView(Context context, AttributeSet attrs) {

		super(context, attrs);
		context1 = context;
		editText = new EditText(context1);

		setupDrawing();
	}

	@Override
	public void onLayout(boolean one, int a, int b, int c, int d) {
	}

	//get drawing area setup for interaction
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

		canvasPaint = new Paint(Paint.DITHER_FLAG);
	}

	//view given size
	@Override
	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {

		super.onSizeChanged(width, height, oldWidth, oldHeight);

		canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
	}

	//draw view
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		canvas.drawPath(drawPath, drawPaint);
	}

	public Bitmap combineImages(Bitmap background, Bitmap text) {
		int width = 0, height = 0;
		Bitmap canvasBitmap;

		width = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
		height = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight();

		canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas combined = new Canvas(canvasBitmap);

		background = Bitmap.createScaledBitmap(background, width, height, true);
		combined.drawBitmap(background, 0, 0, null);
		combined.drawBitmap(text, 20, 20, null);

		return canvasBitmap;
	}


	public Bitmap getCombinedBitmap() {
		return combined;
	}

	public boolean getValue(Context context){
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME3, Context.MODE_PRIVATE);
		boolean isTextButtonPressed = settings.getBoolean(PREFS_KEY3, false);
		return isTextButtonPressed;
	}

	//detect user touch
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float touchX = event.getX();
		float touchY = event.getY();

		if (getValue(context1)) {



//			LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll);
//
//			editText = (EditText) linearLayout.findViewById(R.id.textEditing);
//			//final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll);
//			final DrawingView drawingView = (DrawingView) findViewById(R.id.drawing);
//
//			drawingView.setOnTouchListener(new View.OnTouchListener() {
//				@Override
//				public boolean onTouch(View v, MotionEvent event) {
//					editText.setVisibility(View.VISIBLE);
//					editText
//						.setText("touch coordinates : " + String.valueOf(event.getX()) + String.valueOf(event.getY()));
//					return true;
//				}
//			});
//
//			DrawingView containerLayout = (DrawingView) findViewById(R.id.drawing);
//			EditText editText = new EditText(getContext());
//			editText = (EditText) findViewById(R.id.textEditing);
//			editText.setText("testing!!!!!!!!!");
//			editText.setVisibility(ViewGroup.VISIBLE);
//			containerLayout.addView(editText);
//			editText.setGravity(Gravity.RIGHT);
//			DrawingView.LayoutParams layoutParams = (DrawingView.LayoutParams) (editText.getLayoutParams());


//
//			layoutParams.width = DrawingView.LayoutParams.MATCH_PARENT;
//			layoutParams.setMargins(23, 34, 0, 0);
			// RelativeLayout.LayoutParams()
//			editText.setLayoutParams(layoutParams);
			//if you want to identify the created editTexts, set a tag, like below


//			setDrawingCacheEnabled(true);
//			measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//			layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
//			buildDrawingCache(true);
////			Bitmap screenshotBitmap = Bitmap.createBitmap(getMeasuredWidth(),getMeasuredHeight(),
////				Bitmap.Config.ARGB_8888);
//
//				//getDrawingCache();
//
//			Bitmap test = Bitmap.createBitmap(getDrawingCache());
//			Bitmap bitmapOfText = TextAppearingUtils.convertToBitmap();
//			combined = combineImages(test, bitmapOfText);
//
//			setDrawingCacheEnabled(false);

		}

		if (isFilling) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				FloodFill(new Point((int) touchX, (int) touchY));
				break;
			default:
				return true;
			}
		}
		else {
			switch (event.getAction()) {
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
		}

		invalidate();
		return true;
	}

	//set color
	public void setColor(String newColor) {
		invalidate();

		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
		drawPaint.setShader(null);
	}


	//update size
	public void setBrushSize(float newSize) {
		brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
			newSize, getResources().getDisplayMetrics());
		drawPaint.setStrokeWidth(brushSize);
	}

	public void setLastBrushSize(float lastSize) {
		lastBrushSize = lastSize;
	}

	public float getLastBrushSize() {
		return lastBrushSize;
	}

	//set mErase true or false
	public void setErase(boolean isErase) {
		if (isErase) {
			drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

		}
		else {
			drawPaint.setXfermode(null);
		}
	}

	private synchronized void FloodFill(Point startPoint) {

		Queue<Point> queue = new LinkedList<>();
		queue.add(startPoint);

		int targetColor = canvasBitmap.getPixel(startPoint.x, startPoint.y);

		while (queue.size() > 0) {
			Point nextPoint = queue.poll();
			if (canvasBitmap.getPixel(nextPoint.x, nextPoint.y) != targetColor) {
				continue;
			}

			Point point = new Point(nextPoint.x + 1, nextPoint.y);

			while ((nextPoint.x > 0) && (canvasBitmap.getPixel(nextPoint.x, nextPoint.y) == targetColor)) {
				canvasBitmap.setPixel(nextPoint.x, nextPoint.y, paintColor);
				if ((nextPoint.y > 0) && (canvasBitmap.getPixel(nextPoint.x, nextPoint.y - 1) == targetColor)) {
					queue.add(new Point(nextPoint.x, nextPoint.y - 1));
				}
				if ((nextPoint.y < canvasBitmap.getHeight() - 1)
					&& (canvasBitmap.getPixel(nextPoint.x, nextPoint.y + 1) == targetColor)) {
					queue.add(new Point(nextPoint.x, nextPoint.y + 1));
				}
				nextPoint.x--;
			}

			while ((point.x < canvasBitmap.getWidth() - 1)
				&& (canvasBitmap.getPixel(point.x, point.y) == targetColor)) {
				canvasBitmap.setPixel(point.x, point.y, paintColor);

				if ((point.y > 0) && (canvasBitmap.getPixel(point.x, point.y - 1) == targetColor)) {
					queue.add(new Point(point.x, point.y - 1));
				}
				if ((point.y < canvasBitmap.getHeight() - 1)
					&& (canvasBitmap.getPixel(point.x, point.y + 1) == targetColor)) {
					queue.add(new Point(point.x, point.y + 1));
				}
				point.x++;
			}
		}

		isFilling = false;
	}
}