package com.github.stkent.bugshaker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
import android.widget.ImageView;
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

	public ImageView image;

	private DrawingView drawView;

	private FeedbackEmailFlowManager feedbackEmailFlowManager;

	private Application application;

	private EmailCapabilitiesProvider emailCapabilitiesProvider;

	private Uri mUri;

	private Activity activity;

	private float smallBrush, mediumBrush, largeBrush;
	private ImageButton currPaint, drawBtn, eraseBtn, sendBtn;


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

		drawBtn = (ImageButton)findViewById(R.id.draw_btn);
		drawBtn.setOnClickListener(this);
		drawView.setBrushSize(mediumBrush);
		eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
		eraseBtn.setOnClickListener(this);
		sendBtn = (ImageButton)findViewById(R.id.sendEmail);
		sendBtn.setOnClickListener(this);
		final GenericEmailIntentProvider genericEmailIntentProvider
			= new GenericEmailIntentProvider();

		Logger logger = new Logger(true);
		application = getApplication();

		emailCapabilitiesProvider = new EmailCapabilitiesProvider(
			application.getPackageManager(), genericEmailIntentProvider, logger);

		feedbackEmailFlowManager = new FeedbackEmailFlowManager(
			application,
			emailCapabilitiesProvider,
			new Toaster(application),
			new ActivityReferenceManager(),
			new FeedbackEmailIntentProvider(application, genericEmailIntentProvider), true);



		String pathOfScreenshot = getIntent().getStringExtra("uri");
		File file = new File(pathOfScreenshot);
		if(file.exists()) {
			Bitmap myBitmap = BitmapFactory.decodeFile(pathOfScreenshot);

			drawView = (DrawingView) findViewById(R.id.drawing);
			Drawable temp = new BitmapDrawable(getResources(), myBitmap);


			drawView.setBackground(temp);
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

		else if (view.getId()==R.id.sendEmail){
			saveDrawing();
			//should send screenshot to email

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

	public Uri getImageUri(Context inContext, Bitmap inImage) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
		return Uri.parse(path);
	}

	private void saveDrawing() {
		AlertDialog.Builder sendDialog = new AlertDialog.Builder(this);
		sendDialog.setTitle("Send Annotated Screenshot");
		sendDialog.setMessage("Send Annotated Screenshot on email?");
		sendDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				drawView.setDrawingCacheEnabled(true);


				String imgSaved = MediaStore.Images.Media.insertImage(
					getContentResolver(), drawView.getDrawingCache(),
					"Screenshot" + ".png", "drawing");
				Bitmap bm = drawView.getDrawingCache();

				Uri bitmapUri = getImageUri(getApplicationContext(), bm);
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
		sendDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		});
		sendDialog.show();

	}
}