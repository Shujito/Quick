package org.shujito.quick;

import android.app.Application;

/**
 * @author shujito
 */
public class QuickApplication extends Application {
	private static QuickApplication instance;

	public static QuickApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}
}
