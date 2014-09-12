package com.ezon.sportwatch.ble.util;

import java.util.Stack;

import android.app.Activity;

public class ActvityStack {

	private static Stack<Activity> mActivityStacks = new Stack<Activity>();

	public static void pushActivity(Activity activity) {
		mActivityStacks.push(activity);
	}

	public static void popActivity(Activity activity) {
		mActivityStacks.remove(activity);
	}

	public static Activity peek() {
		return mActivityStacks.peek();
	}
}
