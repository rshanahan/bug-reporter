package com.github.stkent.bugshaker.flow.email;

import java.io.File;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by aschen on 7/14/16.
 */
public class ScreenshotUtil {

	private static final String SCREENSHOTS_DIRECTORY_NAME = "bug-reports";
	private static final String SCREENSHOT_FILE_NAME = "latest-screenshot.jpg";


	public static File getScreenshotFile(@NonNull final Context applicationContext) {
		final File screenshotsDir = new File(
			applicationContext.getFilesDir(), SCREENSHOTS_DIRECTORY_NAME);

		//noinspection ResultOfMethodCallIgnored
		screenshotsDir.mkdirs();

		return new File(screenshotsDir, SCREENSHOT_FILE_NAME);
	}
	}

