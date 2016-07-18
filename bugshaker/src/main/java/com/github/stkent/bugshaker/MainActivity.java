package com.github.stkent.bugshaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.stkent.bugshaker.flow.widget.DrawingView;

public class MainActivity extends AppCompatActivity implements OnClickListener {

	public ImageView image;
	private DrawingView drawing;
	private DrawingView drawView;


	private float smallBrush, mediumBrush, largeBrush;
	private ImageButton currPaint, drawBtn, eraseBtn, sendBtn;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		drawView = (DrawingView)findViewById(R.id.drawing);
		LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		currPaint = (ImageButton)paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

		smallBrush = getResources().getInteger(R.integer.small_size);
		mediumBrush = getResources().getInteger(R.integer.medium_size);
		largeBrush = getResources().getInteger(R.integer.large_size);

		drawBtn = (ImageButton)findViewById(R.id.draw_btn);
		drawBtn.setOnClickListener(this);
		drawView.setBrushSize(mediumBrush);
		eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
		eraseBtn.setOnClickListener(this);
		sendBtn = (ImageButton)findViewById(R.id.sendEmail);
		sendBtn.setOnClickListener(this);



		String pathOfScreenshot = getIntent().getStringExtra("uri");
		File file = new File(pathOfScreenshot);
		if(file.exists()) {
			Bitmap myBitmap = BitmapFactory.decodeFile(pathOfScreenshot);

			drawing = (DrawingView) findViewById(R.id.drawing);
			Drawable temp = new BitmapDrawable(getResources(), myBitmap);


			drawing.setBackground(temp);
		}
		else{
			throw new RuntimeException();
		}
	}

	@Override
	public void onClick(View view){
		//respond to clicks
		if(view.getId()==R.id.draw_btn){
			//draw button clicked
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Brush size:");
			brushDialog.setContentView(R.layout.brush_chooser);

			ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					drawView.setBrushSize(smallBrush);
					drawView.setLastBrushSize(smallBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});

			ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					drawView.setBrushSize(mediumBrush);
					drawView.setLastBrushSize(mediumBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});

			ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					drawView.setBrushSize(largeBrush);
					drawView.setLastBrushSize(largeBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});

			brushDialog.show();
		} else if (view.getId() == R.id.erase_btn){
			//siwtch to erase, choose size
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Eraser size:");
			brushDialog.setContentView(R.layout.brush_chooser);



			ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					drawView.setErase(true);
					drawView.setBrushSize(smallBrush);
					brushDialog.dismiss();
				}
			});

			ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					drawView.setErase(true);
					drawView.setBrushSize(mediumBrush);
					brushDialog.dismiss();
				}
			});

			ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					drawView.setErase(true);
					drawView.setBrushSize(largeBrush);
					brushDialog.dismiss();
				}
			});
			brushDialog.show();
		}


	}

	public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException {
		try {
			InputStream input = this.getContentResolver().openInputStream(uri);

			BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
			onlyBoundsOptions.inJustDecodeBounds = true;
			onlyBoundsOptions.inDither = true;//optional
			onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
			BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
			input.close();
			if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
				return null;

			int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
				: onlyBoundsOptions.outWidth;



			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

			bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
			input = getApplication().getContentResolver().openInputStream(uri);
			Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
			input.close();
			return bitmap;
		}
		catch(IOException e){

		}
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap b = Bitmap.createBitmap(10, 10, conf);
		return b;

	}

	public void paintClicked(View view){
		//use chosen color
	//	drawView.setColor("#000000");

		drawView.setBrushSize(drawView.getLastBrushSize());

		if(view!=currPaint){
			//update color
			ImageButton imgView = (ImageButton)view;
			String color = view.getTag().toString();
			drawView.setColor(color);

			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
			currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			currPaint=(ImageButton)view;
		}
	 }
}