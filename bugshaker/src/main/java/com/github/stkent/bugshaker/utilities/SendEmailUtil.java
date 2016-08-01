package com.github.stkent.bugshaker.utilities;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;

public class SendEmailUtil {

	public static void sendEmailWithScreenshot(
		@NonNull final Context context,
		@NonNull final Uri screenshotUri, @NonNull final Uri file,
		String[] emailAddresses, String emailSubjectLine
	) {
		final Intent feedbackEmailIntent = com.github.stkent.bugshaker.utilities.FeedbackEmailIntentUtil
			.getFeedbackEmailIntent(context, emailAddresses, emailSubjectLine, screenshotUri, file);

		final List<ResolveInfo> resolveInfoList = context.getPackageManager()
			.queryIntentActivities(feedbackEmailIntent, PackageManager.MATCH_DEFAULT_ONLY);

		for (final ResolveInfo receivingApplicationInfo : resolveInfoList) {
			context.grantUriPermission(
				receivingApplicationInfo.activityInfo.packageName,
				screenshotUri,
				Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}
		Intent sendEmailIntent = Intent.createChooser(feedbackEmailIntent, "Send email");
		sendEmailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		sendEmailIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		context.startActivity(sendEmailIntent);
	}

}
