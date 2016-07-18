/**
 * Copyright 2016 Stuart Kent
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.github.stkent.bugshaker.flow.email;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.github.stkent.bugshaker.ActivityReferenceManager;
import com.github.stkent.bugshaker.MainActivity;
import com.github.stkent.bugshaker.ScreenshotUtil;
import com.github.stkent.bugshaker.flow.dialog.DialogProvider;
import com.github.stkent.bugshaker.flow.email.screenshot.ScreenshotProvider;
import com.github.stkent.bugshaker.utilities.ActivityUtils;
import com.github.stkent.bugshaker.utilities.Logger;
import com.github.stkent.bugshaker.utilities.Toaster;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public final class FeedbackEmailFlowManager {

	private static final int FLAG_SECURE_VALUE = 0x00002000;

	private static final String SCREENSHOTS_DIRECTORY_NAME = "bug-reports";
	private static final String SCREENSHOT_FILE_NAME = "latest-screenshot.jpg";


	@NonNull
	private final Context applicationContext;

	@NonNull
	private final Toaster toaster;

	@NonNull
	private final ActivityReferenceManager activityReferenceManager;

	@NonNull
	private final EmailCapabilitiesProvider emailCapabilitiesProvider;

	@NonNull
	private final FeedbackEmailIntentProvider feedbackEmailIntentProvider;

	@NonNull
	private final ScreenshotProvider screenshotProvider;

	@NonNull
	private final DialogProvider alertDialogProvider;

	@NonNull
	private final Logger logger;

	@Nullable
	private Dialog alertDialog;

	@NonNull
	private static File outputFile;

	private String[] emailAddresses;
	private String emailSubjectLine;
	private boolean ignoreFlagSecure;


	private final OnClickListener screenshotListener = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			final Activity activity = activityReferenceManager.getValidatedActivity();
			if (activity == null) {
				return;
			}

			if (shouldAttemptToCaptureScreenshot(activity)) {
				if (emailCapabilitiesProvider.canSendEmailsWithAttachments()) {
					screenshotProvider.getScreenshotUri(activity)
						.single()
						.observeOn(AndroidSchedulers.mainThread())
						.subscribeOn(AndroidSchedulers.mainThread())
						.subscribe(new Subscriber<Uri>() {
							@Override
							public void onCompleted() {
								// This method intentionally left blank.
							}

							@Override
							public void onError(final Throwable e) {
								final String errorString = "Screenshot capture failed";
								toaster.toast(errorString);
								logger.e(errorString);

								logger.printStackTrace(e);
							}

							@Override
							public void onNext(final Uri uri) {
								startActivity(uri);
							}


						});
				}
			} else {
				final String warningString = "Window is secured; no screenshot taken";

				toaster.toast(warningString);
				logger.d(warningString);
			}

		}

	};

	private final OnClickListener reportBugClickListener = new OnClickListener() {
		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			final Activity activity = activityReferenceManager.getValidatedActivity();
			if (activity == null) {
				return;
			}

			if (shouldAttemptToCaptureScreenshot(activity)) {
				if (emailCapabilitiesProvider.canSendEmailsWithAttachments()) {
					screenshotProvider.getScreenshotUri(activity)
						.single()
						.observeOn(AndroidSchedulers.mainThread())
						.subscribeOn(AndroidSchedulers.mainThread())
						.subscribe(new Subscriber<Uri>() {
							@Override
							public void onCompleted() {
								// This method intentionally left blank.
							}

							@Override
							public void onError(final Throwable e) {
								final String errorString = "Screenshot capture failed";
								toaster.toast(errorString);
								logger.e(errorString);

								logger.printStackTrace(e);

								sendEmailWithoutScreenshot(activity);
							}

							@Override
							public void onNext(final Uri uri) {
								saveLogcatToFile(applicationContext);

								sendEmailWithScreenshot(activity, uri, Uri.fromFile(outputFile));
							}


						});
				} else {
					sendEmailWithoutScreenshot(activity);
				}
			} else {
				final String warningString = "Window is secured; no screenshot taken";

				toaster.toast(warningString);
				logger.d(warningString);

				sendEmailWithoutScreenshot(activity);
			}
		}
	};

	private void startActivity(Uri uri) {
		File file = ScreenshotUtil.getScreenshotFile(applicationContext);

		String path = file.getAbsolutePath();

		if(file.exists()) {
			Intent ii = new Intent(applicationContext, MainActivity.class);
			ii.putExtra("uri", path);
			ii.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			applicationContext.startActivity(ii);
		}else{
			throw new RuntimeException();
		}
	}

	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public FeedbackEmailFlowManager(
		@NonNull final Context applicationContext,
		@NonNull final EmailCapabilitiesProvider emailCapabilitiesProvider,
		@NonNull final Toaster toaster,
		@NonNull final ActivityReferenceManager activityReferenceManager,
		@NonNull final FeedbackEmailIntentProvider feedbackEmailIntentProvider,
		@NonNull final ScreenshotProvider screenshotProvider,
		@NonNull final DialogProvider alertDialogProvider,
		@NonNull final Logger logger) {

		this.applicationContext = applicationContext;
		this.emailCapabilitiesProvider = emailCapabilitiesProvider;
		this.toaster = toaster;
		this.activityReferenceManager = activityReferenceManager;
		this.feedbackEmailIntentProvider = feedbackEmailIntentProvider;
		this.screenshotProvider = screenshotProvider;
		this.alertDialogProvider = alertDialogProvider;
		this.logger = logger;
	}

	public void onActivityResumed(@NonNull final Activity activity) {
		dismissDialog();
		activityReferenceManager.setActivity(activity);
	}

	public void onActivityStopped() {
		dismissDialog();
	}

	public void startFlowIfNeeded(
		@NonNull final String[] emailAddresses,
		@Nullable final String emailSubjectLine,
		final boolean ignoreFlagSecure) {

		if (isFeedbackFlowStarted()) {
			logger.d("Feedback flow already started; ignoring shake.");
			return;
		}

		this.emailAddresses = Arrays.copyOf(emailAddresses, emailAddresses.length);
		this.emailSubjectLine = emailSubjectLine;
		this.ignoreFlagSecure = ignoreFlagSecure;

		showDialog();
	}

	private boolean isFeedbackFlowStarted() {
		return alertDialog != null && alertDialog.isShowing();
	}

	private void showDialog() {
		final Activity currentActivity = activityReferenceManager.getValidatedActivity();
		if (currentActivity == null) {
			return;
		}

		LayoutInflater inflater = new LayoutInflater(applicationContext) {
			@Override
			public LayoutInflater cloneInContext(Context context) {
				return null;
			}
		};

//
//		View alertLayout = inflater.inflate(R.layout.alert_dialog, null);
//		final Button report = (Button) alertLayout.findViewById(R.id.btn1);
//		final Button annotate = (Button) alertLayout.findViewById(R.id.btn2);
//		final Button cancel = (Button) alertLayout.findViewById(R.id.btn3);
//
//		AlertDialog.Builder hi = new AlertDialog.Builder(currentActivity);
//		hi.setMessage("Shake Detected!"+ '\n' + "Would you like to report a bug?");


//		final Dialog dialog = new Dialog(applicationContext);
//		dialog.setTitle("Shake Detected!");
//		dialog.setContentView(R.layout.alert_dialog);
//		dialog.show();



		AlertDialog.Builder hi = new AlertDialog.Builder(currentActivity);
		hi.setMessage("Shake Detected!"+ '\n' + "Would you like to report a bug?");
		hi.setCancelable(false);

		hi.setPositiveButton("Report", reportBugClickListener);
		hi.setNegativeButton("Cancel", null);
		hi.setNeutralButton("Annotate screenshot and report", screenshotListener);

		AlertDialog alert = hi.create();
		alert.show();

//		alertDialog.setContentView(R.layout.alert_dialog);
//
//      alertDialog = alertDialogProvider.getAlertDialog(currentActivity, reportBugClickListener);
//      alertDialog.show();






	}

	private void dismissDialog() {
		if (alertDialog != null) {
			alertDialog.dismiss();
			alertDialog = null;
		}
	}

	private boolean shouldAttemptToCaptureScreenshot(@NonNull final Activity activity) {
		final int windowFlags = ActivityUtils.getWindow(activity).getAttributes().flags;

		final boolean isWindowSecured =
			(windowFlags & WindowManager.LayoutParams.FLAG_SECURE) == FLAG_SECURE_VALUE;

		final boolean result = ignoreFlagSecure || !isWindowSecured;

		if (!isWindowSecured) {
			logger.d("Window is not secured; should attempt to capture screenshot.");
		} else {
			if (ignoreFlagSecure) {
				logger.d("Window is secured, but we're ignoring that.");
			} else {
				logger.d("Window is secured, and we're respecting that.");
			}
		}

		return result;
	}

	private void sendEmailWithScreenshot(
		@NonNull final Activity activity,
		@NonNull final Uri screenshotUri, final Uri file) {
		//	logger.d("visited the SEWS that has 3 parameters");

		final Intent feedbackEmailIntent = feedbackEmailIntentProvider
			.getFeedbackEmailIntent(emailAddresses, emailSubjectLine, screenshotUri, file);

		final List<ResolveInfo> resolveInfoList = applicationContext.getPackageManager()
			.queryIntentActivities(feedbackEmailIntent, PackageManager.MATCH_DEFAULT_ONLY);

		for (final ResolveInfo receivingApplicationInfo : resolveInfoList) {
			// FIXME: revoke these permissions at some point!
			applicationContext.grantUriPermission(
				receivingApplicationInfo.activityInfo.packageName,
				screenshotUri,
				Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}
		activity.startActivity(Intent.createChooser(feedbackEmailIntent, "Send email"));
		outputFile.delete();
	}

	private void sendEmailWithScreenshot(
		@NonNull final Activity activity,
		@NonNull final Uri screenshotUri) {
		//	logger.d("visited the SEWS that has 2 parameters");

		final Intent feedbackEmailIntent = feedbackEmailIntentProvider
			.getFeedbackEmailIntent(emailAddresses, emailSubjectLine, screenshotUri);

		final List<ResolveInfo> resolveInfoList = applicationContext.getPackageManager()
			.queryIntentActivities(feedbackEmailIntent, PackageManager.MATCH_DEFAULT_ONLY);

		for (final ResolveInfo receivingApplicationInfo : resolveInfoList) {
			// FIXME: revoke these permissions at some point!
			applicationContext.grantUriPermission(
				receivingApplicationInfo.activityInfo.packageName,
				screenshotUri,
				Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}

		activity.startActivity(feedbackEmailIntent);




	}

	public static File saveLogcatToFile(Context context) {
		String fileName = "logcat.txt";
		outputFile = new File(context.getExternalCacheDir(),fileName);

		@SuppressWarnings("unused")

		Process process;
		{
			try {
				process = Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
			}
			catch (IOException e) {
				System.out.println(e.toString());
			}
		}
		return outputFile;
	}

	private void sendEmailWithoutScreenshot(@NonNull final Activity activity) {
		final Intent feedbackEmailIntent = feedbackEmailIntentProvider
			.getFeedbackEmailIntent(emailAddresses, emailSubjectLine);

		activity.startActivity(feedbackEmailIntent);

		logger.d("Sending email with no screenshot.");
	}



}