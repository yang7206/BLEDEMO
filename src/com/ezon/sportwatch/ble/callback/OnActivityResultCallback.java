package com.ezon.sportwatch.ble.callback;

import android.content.Intent;

/**
 * ��Ӳ�activity�������callback
 * 
 * @author yxy
 * 
 */
public interface OnActivityResultCallback {
	void onActivityResult(int requestCode, int resultCode, Intent data);
}
