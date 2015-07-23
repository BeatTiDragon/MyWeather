package com.example.john.textweather;

import android.app.Application;

import com.thinkland.sdk.android.JuheSDKInitializer;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		JuheSDKInitializer.initialize(getApplicationContext());
	}
}
