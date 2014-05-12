package ru.abusalimov.xposed.wifideepsleep;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticBooleanField;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;
import android.os.Looper;
import android.os.Message;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.callbacks.XCallback;

public class XposedMod implements IXposedHookLoadPackage {

	public static final String WIFI_CONTROLLER = "com.android.server.wifi.WifiController";

	protected static class SingletonMethodHook extends XC_MethodHook {
		// Singleton-like: all instances are the same to avoid
		// unnecessary repeated filtering (don't know where it comes from).

		@Override
		public int compareTo(XCallback other) {
			if (this.equals(other)) {
				return 0;
			}
			return super.compareTo(other);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof MethodHook) {
				return true;
			}
			return super.equals(o);
		}

		@Override
		public int hashCode() {
			return ~MethodHook.class.hashCode() ^ 0xdeadbeef;
		}
	}

	protected static class MethodHook extends SingletonMethodHook {

		protected static final int CMD_SCREEN_ON;
		protected static final int CMD_USER_PRESENT;
		protected static final int CMD_LOCKS_CHANGED;

		protected static final boolean HANDLED;
		protected static final boolean NOT_HANDLED;

		static {
			Class<?> clazz = findClass(WIFI_CONTROLLER, null);

			CMD_SCREEN_ON      = getStaticIntField(clazz, "CMD_SCREEN_ON");
			CMD_USER_PRESENT   = getStaticIntField(clazz, "CMD_USER_PRESENT");
			CMD_LOCKS_CHANGED  = getStaticIntField(clazz, "CMD_LOCKS_CHANGED");

			HANDLED            = getStaticBooleanField(clazz, "HANDLED");
			NOT_HANDLED        = getStaticBooleanField(clazz, "NOT_HANDLED");
		}

	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		XposedHelpers.findAndHookMethod(WIFI_CONTROLLER + "$DeviceInactiveState",
			lpparam.classLoader, "processMessage", Message.class,

			new MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					Message msg = (Message) param.args[0];
					Object controller = getObjectField(param.thisObject, "this$0");

					if (callMethod(controller, "getCurrentState") ==
							getObjectField(controller, "mNoLockHeldState")) {

						if (msg.what == CMD_SCREEN_ON) {
							XposedBridge.log("mNoLockHeldState: CMD_SCREEN_ON");
							param.setResult(NOT_HANDLED);

						} else if (msg.what == CMD_USER_PRESENT) {
							XposedBridge.log("mNoLockHeldState: CMD_USER_PRESENT");
							callMethod(controller, "transitionTo",
								getObjectField(controller, "mDeviceActiveState"));
							param.setResult(NOT_HANDLED);

						} else if (msg.what == CMD_LOCKS_CHANGED) {
							Object locks = getObjectField(controller, "mLocks");
							int lock = (int) callMethod(locks, "getStrongestLockMode");
							XposedBridge.log("mNoLockHeldState: CMD_LOCKS_CHANGED: " + lock);
							param.setResult(HANDLED);

						} else
							XposedBridge.log("mNoLockHeldState: "+(msg.what-0x00026000));
					}
				}
			});

		XposedHelpers.findAndHookMethod(WIFI_CONTROLLER,
			lpparam.classLoader, "initializeAndRegisterForSettingsChange", Looper.class,

			new MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					XposedHelpers.setLongField(param.thisObject, "mIdleMillis", 15 * 1000);
				}
			});

	}

}
