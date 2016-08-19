/**
 * Copyright 2016 Stuart Kent
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * <p/>
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.github.stkent.bugshaker.flow.email;

import java.io.File;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.github.stkent.bugshaker.ActivityReferenceManager;
import com.github.stkent.bugshaker.MainActivity;
import com.github.stkent.bugshaker.R;
import com.github.stkent.bugshaker.ScreenshotUtil;
import com.github.stkent.bugshaker.flow.email.screenshot.ScreenshotProvider;
import com.github.stkent.bugshaker.utilities.ActivityUtils;
import com.github.stkent.bugshaker.utilities.FeedbackEmailIntentUtil;
import com.github.stkent.bugshaker.utilities.LogcatUtil;
import com.github.stkent.bugshaker.utilities.Logger;
import com.github.stkent.bugshaker.utilities.SendEmailUtil;
import com.github.stkent.bugshaker.utilities.SharedPreferencesUtil;
import com.github.stkent.bugshaker.utilities.Toaster;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public final class FeedbackEmailFlowManager {

	private static final int FLAG_SECURE_VALUE = 0x00002000;

	@Nullable
	private Dialog alertDialog;
	public static String[] emailAddresses;
	public static String emailSubjectLine;
	private boolean ignoreFlagSecure;
	private ScreenshotProvider screenshotProvider;
	private ActivityReferenceManager activityReferenceManager;
	private Context context;

	public FeedbackEmailFlowManager(ScreenshotProvider screenshotProvider, Application application) {
		this.screenshotProvider = ScreenshotUtil.getScreenshotProvider(application);
		activityReferenceManager = new ActivityReferenceManager();
		context = application.getBaseContext();

	}

	private final OnClickListener screenshotListener = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int i) {

			final Activity activity = activityReferenceManager.getValidatedActivity();
			final Context context = activity.getBaseContext();
			final Toaster toaster = new Toaster(activity);
			final Logger logger = new Logger(true);
			if (activity == null) {
				return;
			}

			if (shouldAttemptToCaptureScreenshot(activity)) {
				if (EmailCapabilitiesProvider.canSendEmailsWithAttachments(
					activityReferenceManager.getValidatedActivity().getPackageManager())) {
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
							}

							@Override
							public void onNext(final Uri uri) {
								startActivity(context);
							}
						});
				}
			}
			else {
				final String warningString = "Window is secured; no screenshot taken";
				toaster.toast(warningString);
				logger.d(warningString);
			}

		}

	};


	private final OnClickListener reportBugClickListener = new OnClickListener() {

		Set<String> stringSet = SharedPreferencesUtil.getEmailAddresses(context);
		String[] emailAddresses = (String[]) stringSet.toArray(new String[stringSet.size()]);

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			final Activity activity = activityReferenceManager.getValidatedActivity();
			final Context context = activity.getBaseContext();
			final Toaster toaster = new Toaster(activity);
			final Logger logger = new Logger(true);
			if (activity == null) {
				return;
			}

			if (shouldAttemptToCaptureScreenshot(activity)) {
				if (EmailCapabilitiesProvider.canSendEmailsWithAttachments
					(activityReferenceManager.getValidatedActivity().getPackageManager())) {
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
								sendEmailWithoutScreenshot(context, activity, emailAddresses, emailSubjectLine);
							}

							@Override
							public void onNext(final Uri uri) {
								LogcatUtil.saveLogcatToFile(context);
								Set<String> stringSet = SharedPreferencesUtil.getEmailAddresses(activity);
								String[] emailAddressesArray = stringSet.toArray(new String[stringSet.size()]);
								SendEmailUtil.sendEmailWithScreenshot(activity, uri, Uri.fromFile(LogcatUtil.getLogFile()),
									emailAddressesArray, SharedPreferencesUtil.getEmailSubjectLine(activity));
							}

						});
				}
				else {
					sendEmailWithoutScreenshot(context, activity, emailAddresses, emailSubjectLine);
				}
			}
			else {
				final String warningString = "Window is secured; no screenshot taken";
				toaster.toast(warningString);
				logger.d(warningString);

				sendEmailWithoutScreenshot(context, activity, emailAddresses, emailSubjectLine);
			}
		}
	};

	private void startActivity(Context applicationContext) {
		File screenshotFile = ScreenshotUtil.getScreenshotFile(applicationContext);
		String screenshotFileAbsolutePath = screenshotFile.getAbsolutePath();

		if (screenshotFile.exists()) {
			Intent goToMainActivityIntent = new Intent(applicationContext, MainActivity.class);
			goToMainActivityIntent.putExtra("uri", screenshotFileAbsolutePath);
			goToMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			applicationContext.startActivity(goToMainActivityIntent);
		}

	}

	public void onActivityResumed(@NonNull final Activity activity) {
		dismissDialog();
		activityReferenceManager.setActivity(activity);
	}

	public void onActivityStopped() {
		dismissDialog();
	}

	public void startFlowIfNeeded(Context context,
		final boolean ignoreFlagSecure
	) {
		final Logger logger = new Logger(true);

		if (isFeedbackFlowStarted()) {
			logger.d("Feedback flow already started; ignoring shake.");
			return;
		}

		this.ignoreFlagSecure = ignoreFlagSecure;
		showDialog(context);
	}

	private boolean isFeedbackFlowStarted() {
		return alertDialog != null && alertDialog.isShowing();
	}

	private void showDialog(Context context) {

		final Activity currentActivity = activityReferenceManager.getValidatedActivity();
		if (currentActivity == null) {
			return;
		}

		AlertDialog.Builder bugAlertBuilder = new AlertDialog.Builder(currentActivity);
		bugAlertBuilder.setMessage(context.getString(R.string.bug_alert_message));
		bugAlertBuilder.setCancelable(false);

		bugAlertBuilder.setPositiveButton(context.getString(R.string.report), reportBugClickListener);
		bugAlertBuilder.setNegativeButton(context.getString(R.string.cancel), null);
		bugAlertBuilder
			.setNeutralButton(context.getString(R.string.annotate_and_report), screenshotListener);

		AlertDialog alertDialog = bugAlertBuilder.create();
		alertDialog.show();
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
		final Logger logger = new Logger(true);

		if (!isWindowSecured) {
			logger.d("Window is not secured; should attempt to capture screenshot.");
		}
		else {
			if (ignoreFlagSecure) {
				logger.d("Window is secured, but we're ignoring that.");
			}
			else {
				logger.d("Window is secured, and we're respecting that.");
			}
		}
		return result;
	}

	private void sendEmailWithoutScreenshot(Context applicationContext,
		@NonNull final Activity activity, String[] emailAddresses, String emailSubjectLine) {
		final Intent feedbackEmailIntent = FeedbackEmailIntentUtil
			.getFeedbackEmailIntent(applicationContext,
				emailAddresses, emailSubjectLine);
		activity.startActivity(feedbackEmailIntent);

	}
}