package com.github.stkent.bugshaker.utilities;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {

	private static final String EMAIL_ADDRESSES = "emailAddresses";

	private static final String KEY_FOR_EMAIL_ADDRESSES = "keyForEmailAddresses";
	private static final String KEY_FOR_EMAIL_SUBJECT_LINE = "keyForEmailSubjectLine";

	public static void saveEmailSubjectLineAndAddresses(Context context, String text, Set<String> values){
		SharedPreferences settings = context.getSharedPreferences(EMAIL_ADDRESSES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_FOR_EMAIL_SUBJECT_LINE, text);
		editor.putStringSet(KEY_FOR_EMAIL_ADDRESSES, values);
		editor.apply();

	}

	public static Set<String> getEmailAddresses(Context context) {
		Set<String> bob = new HashSet<>();
		bob.add("a");
		bob.add("b");
		SharedPreferences settings;
		settings = context.getSharedPreferences(EMAIL_ADDRESSES, Context.MODE_PRIVATE);
		Set<String> text = (Set<String>)settings.getStringSet(KEY_FOR_EMAIL_ADDRESSES, null);
		return text;
	}

	public static String getEmailSubjectLine(Context context) {
		SharedPreferences settings;
		String text;
		settings = context.getSharedPreferences(EMAIL_ADDRESSES, Context.MODE_PRIVATE);
		text = settings.getString(KEY_FOR_EMAIL_SUBJECT_LINE, null);
		return text;
	}

}
