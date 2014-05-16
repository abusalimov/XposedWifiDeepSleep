package ru.abusalimov.xposed.wifideepsleep;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;
import static de.robv.android.xposed.XposedHelpers.getStaticBooleanField;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;
import static de.robv.android.xposed.XposedHelpers.getSurroundingThis;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import ru.abusalimov.xposed.util.SingletonMethodHook;
import android.net.wifi.WifiManager;
import android.os.Message;

public class WifiControllerDeviceInactiveStateProcessMessageMethodHook extends SingletonMethodHook {
	public static final String WIFI_CONTROLLER = "com.android.server.wifi.WifiController";

	protected static final int CMD_SCREEN_ON;
	protected static final int CMD_USER_PRESENT;
	protected static final int CMD_LOCKS_CHANGED;

	protected static final boolean HANDLED;
	protected static final boolean NOT_HANDLED;

	protected static final Field M_NO_LOCK_HELD_STATE;
	protected static final Field M_DEVICE_ACTIVE_STATE;
	protected static final Field M_LOCKS;

	protected static final Method GET_CURRENT_STATE;
	protected static final Method TRANSITION_TO;
	protected static final Method GET_STRONGEST_LOCK_MODE;

	@Override
	protected void beforeHookedMethod(MethodHookParam param)
			throws Throwable {
		Message msg = (Message) param.args[0];
		Object controller = getSurroundingThis(param.thisObject);

		/* NoLockHeldState does not override processMessage, unfortunately.
		 * So, we hook the method of its parent instead and check for
		 * actual state here. */
		if (GET_CURRENT_STATE.invoke(controller) ==
				M_NO_LOCK_HELD_STATE.get(controller)) {

			if (msg.what == CMD_SCREEN_ON) {
				/* Default action is to transitionTo(mDeviceActiveState).
				 * Skip instead, and proceed to DefaultState handling. */
				param.setResult(NOT_HANDLED);

			} else if (msg.what == CMD_USER_PRESENT) {
				/* There is no default action for this. Do what it used
				 * to do for CMD_SCREEN_ON before and delegate the rest
				 * to DefaultState as usual. */
				TRANSITION_TO.invoke(controller,
						M_DEVICE_ACTIVE_STATE.get(controller));
				param.setResult(NOT_HANDLED);

			} else if (msg.what == CMD_LOCKS_CHANGED) {
				Object lockList = M_LOCKS.get(controller);
				int strongestLock = (int) GET_STRONGEST_LOCK_MODE.invoke(lockList);

				/* Take a default action only for high performance locks. */
				if (strongestLock != WifiManager.WIFI_MODE_FULL_HIGH_PERF) {
					param.setResult(HANDLED);
				}

			}
		}

	}

	static {
		Class<?> clazz = findClass(WIFI_CONTROLLER, null);

		CMD_SCREEN_ON      = getStaticIntField(clazz, "CMD_SCREEN_ON");
		CMD_USER_PRESENT   = getStaticIntField(clazz, "CMD_USER_PRESENT");
		CMD_LOCKS_CHANGED  = getStaticIntField(clazz, "CMD_LOCKS_CHANGED");

		HANDLED            = getStaticBooleanField(clazz, "HANDLED");
		NOT_HANDLED        = getStaticBooleanField(clazz, "NOT_HANDLED");

		M_NO_LOCK_HELD_STATE     = findField(clazz, "mNoLockHeldState");
		M_DEVICE_ACTIVE_STATE    = findField(clazz, "mDeviceActiveState");
		M_LOCKS                  = findField(clazz, "mLocks");
		GET_CURRENT_STATE        = findMethodBestMatch(clazz, "getCurrentState");
		TRANSITION_TO            = findMethodBestMatch(clazz, "transitionTo",
				GET_CURRENT_STATE.getReturnType());
		GET_STRONGEST_LOCK_MODE  = findMethodBestMatch(M_LOCKS.getType(),
				"getStrongestLockMode");
	}

}