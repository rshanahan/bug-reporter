package com.github.stkent.bugshaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

	public ImageView image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		String pathOfScreenshot = getIntent().getStringExtra("uri");
		File file = new File(pathOfScreenshot);
		if(file.exists()) {
			Bitmap myBitmap = BitmapFactory.decodeFile(pathOfScreenshot);
			//Uri imageUri = Uri.parse(pathOfScreenshot);
//		Uri imageUri = getIntent().getData();

			//ImageView image = (ImageView) findViewById(R.id.screen);
			image = (ImageView) findViewById(R.id.screen);
//		try {
////			BitmapFactoryp bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
////			Bitmap bm = getThumbnail(imageUri);
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//			setContentView(R.layout.activity_main);
//		}
			image.setImageBitmap(myBitmap);
		}
		else{
			throw new RuntimeException();
		}

//		image.setImageURI(imageUri);


		//View v = findViewById(R.id.screen);
//		((LinearLayout) image).addView(image);
//
//		LinearLayout l = (LinearLayout) findViewById(R.id.ll);
//		l.removeView(image);

////		image.setImageBitmap();

//		Bitmap screenshot = BitmapFactory.decodeFile(pathOfScreenshot);
//		image.setImageBitmap(screenshot);


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

//		double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//		bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
//		bitmapOptions.inDither=true;//optional
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
}
