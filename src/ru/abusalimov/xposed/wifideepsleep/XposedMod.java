package ru.abusalimov.xposed.wifideepsleep;

import android.os.Build;
import android.os.Message;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (Build.VERSION.SDK_INT != Build.VERSION_CODES.KITKAT) {
			Log.w("WifiDeepSleep", "Incompatible Android version: " + Build.VERSION.RELEASE);
			return;
		}

		XposedHelpers.findAndHookMethod(
				WifiControllerDeviceInactiveStateProcessMessageMethodHook.WIFI_CONTROLLER +
				"$DeviceInactiveState", lpparam.classLoader,
				"processMessage", Message.class,
				new WifiControllerDeviceInactiveStateProcessMessageMethodHook());
	}

}
