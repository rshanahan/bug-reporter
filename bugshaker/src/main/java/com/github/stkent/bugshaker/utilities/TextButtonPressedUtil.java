package com.github.stkent.bugshaker.utilities;

/**
 * Created by aschen on 7/20/16.
 */
public class TextButtonPressedUtil {
	private static boolean textButtonPressed = false;

	public static boolean getTextButtonPressed(){
		return textButtonPressed;
	}

	public static void setTextButtonPressedTrue(){
		textButtonPressed = true;
	}

	public static void setTextButtonPressedFalse(){
		textButtonPressed = false;
	}
}
