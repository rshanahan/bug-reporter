package com.github.stkent.bugshaker;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.stkent.bugshaker.flow.email.EmailCapabilitiesProvider;
import com.github.stkent.bugshaker.flow.email.FeedbackEmailFlowManager;
import com.github.stkent.bugshaker.flow.email.FeedbackEmailIntentProvider;
import com.github.stkent.bugshaker.flow.email.GenericEmailIntentProvider;
import com.github.stkent.bugshaker.flow.widget.DrawingView;
import com.github.stkent.bugshaker.utilities.Logger;
import com.github.stkent.bugshaker.utilities.Toaster;

public class MainActivity extends AppCompatActivity implements OnClickListener {

	private DrawingView drawView;

	private FeedbackEmailFlowManager feedbackEmailFlowManager;



	private Activity activity;

	private float smallBrush, mediumBrush, largeBrush, smallEraser, mediumEraser, largeEraser;
	private ImageButton currPaint;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		activity = this;

		drawView = (DrawingView)findViewById(R.id.drawing);
		LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		currPaint = (ImageButton)paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

		smallBrush = getResources().getInteger(R.integer.small_size);
		mediumBrush = getResources().getInteger(R.integer.medium_size);
		largeBrush = getResources().getInteger(R.integer.large_size);

		smallEraser = getResources().getInteger(R.integer.small_eraser_size);
		mediumEraser = getResources().getInteger(R.integer.medium_eraser_size);
		largeEraser = getResources().getInteger(R.integer.large_eraser_size);

		ImageButton drawBtn = (ImageButton) findViewById(R.id.draw_btn);
		drawBtn.setOnClickListener(this);

		drawView.setBrushSize(smallBrush);

		ImageButton eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
		eraseBtn.setOnClickListener(this);

		ImageButton sendBtn = (ImageButton) findViewById(R.id.sendEmail);
		sendBtn.setOnClickListener(this);

		ImageButton speechBtn = (ImageButton) findViewById(R.id.speechBox);
		speechBtn.setOnClickListener(this);

		final GenericEmailIntentProvider genericEmailIntentProvider
			= new GenericEmailIntentProvider();

		Logger logger = new Logger(true);
		Application application = getApplication();

		EmailCapabilitiesProvider emailCapabilitiesProvider = new EmailCapabilitiesProvider(
			application.getPackageManager(), genericEmailIntentProvider, logger);

		feedbackEmailFlowManager = new FeedbackEmailFlowManager(
			application,
			emailCapabilitiesProvider,
			new Toaster(application),
			new ActivityReferenceManager(),
			new FeedbackEmailIntentProvider(application, genericEmailIntentProvider));



		String pathOfScreenshot = getIntent().getStringExtra("uri");
		File screenshotFile = new File(pathOfScreenshot);
		if(screenshotFile.exists()) {
			Bitmap myBitmap = BitmapFactory.decodeFile(pathOfScreenshot);

			drawView = (DrawingView) findViewById(R.id.drawing);
			Drawable temp = new BitmapDrawable(getResources(), myBitmap);


			drawView.setBackgroundDrawable(temp);
		}
		else {
			throw new RuntimeException();
		}
	}

	@Override
	public void onClick(View view){
		//respond to clicks
		if(view.getId()==R.id.draw_btn){
			//draw button clicked
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle(getApplicationContext().getString(R.string.brush_dialog_title));
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
			//switch to erase, choose size
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle(getApplicationContext().getString(R.string.brush_dialog_title));
			brushDialog.setContentView(R.layout.brush_chooser);
			ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					drawView.setErase(true);
					drawView.setBrushSize(smallEraser);
					brushDialog.dismiss();
				}
			});

			ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					drawView.setErase(true);
					drawView.setBrushSize(mediumEraser);
					brushDialog.dismiss();
				}
			});

			ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					drawView.setErase(true);
					drawView.setBrushSize(largeEraser);
					brushDialog.dismiss();
				}
			});
			brushDialog.show();
		}

		else if (view.getId()==R.id.sendEmail){
			saveDrawing();
		}
		else if (view.getId()==R.id.speechBox){

			createTextEdit();
		}



	}

	private void createTextEdit(){
		//get x and y
	}

	public void paintClicked(View view){
		//use chosen color
		drawView.setBrushSize(drawView.getLastBrushSize());

		if(view!=currPaint){
			//update color
			ImageButton imgView = (ImageButton)view;
			String clickedButtonColor = view.getTag().toString();
			drawView.setColor(clickedButtonColor);

			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
			currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			currPaint=(ImageButton)view;
		}
	 }

	private Uri getImageUri(Context inContext, Bitmap inImage) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
		return Uri.parse(path);
	}

	private void saveDrawing() {
		AlertDialog.Builder sendDialog = new AlertDialog.Builder(this);

		sendDialog.setTitle(getApplicationContext().getString(R.string.send_annotated_screenshot));
		sendDialog.setMessage(getApplicationContext().getString(R.string.attach_annotated_screenshot_to_email));
		sendDialog.setPositiveButton(getApplicationContext().getString(R.string.yes), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				drawView.setDrawingCacheEnabled(true);

				//Saves image in phone Gallery
				String imgSaved = MediaStore.Images.Media.insertImage(
					getContentResolver(), drawView.getDrawingCache(),
					ScreenshotUtil.getImageFileName(), ScreenshotUtil.getImageDescription());
				Bitmap screenshotBitmap = drawView.getDrawingCache();

				Uri bitmapUri = getImageUri(getApplicationContext(), screenshotBitmap);
				System.out.println(bitmapUri.getPath());

				if (imgSaved != null) {
					Toast savedToast = Toast.makeText(getApplicationContext(),
						"Drawing saved to Gallery!", Toast.LENGTH_SHORT);


					File log = feedbackEmailFlowManager.saveLogcatToFile(getApplicationContext());
					Uri logUri = Uri.fromFile(log);
					feedbackEmailFlowManager.sendEmailWithScreenshot(activity, bitmapUri, logUri);

					savedToast.show();


				} else {
					Toast unsavedToast = Toast.makeText(getApplicationContext(),
						"Oops! Image could not be saved.", Toast.LENGTH_SHORT);
					unsavedToast.show();
				}
				drawView.destroyDrawingCache();
			}
		});
		sendDialog.setNegativeButton(getApplicationContext().getString(R.string.cancel), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		});
		sendDialog.show();

	}
}