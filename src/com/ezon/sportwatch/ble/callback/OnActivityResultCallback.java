package com.ezon.sportwatch.ble.callback;

import android.content.Intent;

/**
 * 间接层activity结果返回callback
 * 
 * @author yxy
 * 
 */
public interface OnActivityResultCallback {
	void onActivityResult(int requestCode, int resultCode, Intent data);
}
