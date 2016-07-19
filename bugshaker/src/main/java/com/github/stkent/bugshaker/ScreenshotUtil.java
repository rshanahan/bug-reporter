package com.github.stkent.bugshaker;

import java.io.File;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by aschen on 7/14/16.
 */

public class ScreenshotUtil {

	private static final String SCREENSHOTS_DIRECTORY_NAME = "bug-reports";
	private static final String SCREENSHOT_FILE_NAME = "latest-screenshot.jpg";
	private static final String IMAGE_FILE_NAME = "Screenshot.png";
	private static final String IMAGE_DESCRIPTION = "drawing";

	public static File getScreenshotFile(@NonNull final Context applicationContext) {
		final File screenshotsDir = new File(
			applicationContext.getFilesDir(), SCREENSHOTS_DIRECTORY_NAME);

		//noinspection ResultOfMethodCallIgnored
		screenshotsDir.mkdirs();

		return new File(screenshotsDir, SCREENSHOT_FILE_NAME);
	}

	public static String getImageFileName(){
		return IMAGE_FILE_NAME;
	}

	public static String getImageDescription(){
		return IMAGE_DESCRIPTION;
	}
}
