package com.example.demo.app;

import com.ezon.sportwatch.ble.BLEManager;

import android.app.Application;

public class DemoApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		BLEManager.initApplication(this);
	}
}
