package com.github.stkent.bugshaker.utilities;

import android.view.View;
import android.widget.EditText;

/**
 * Created by aschen on 7/20/16.
 */
public class TextAppearingUtils {
	public static EditText editText;

	public TextAppearingUtils(EditText edit){
		editText = edit;
	}

	public static void setEditText(EditText e){
		editText = e;
	}

	public static void makeEditTextVisible(){
		editText.setVisibility(View.VISIBLE);
	}

	public static EditText getEditText(){
		return editText;
	}

}
