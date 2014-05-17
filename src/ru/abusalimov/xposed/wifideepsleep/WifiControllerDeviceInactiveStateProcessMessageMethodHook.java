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
import android.os.WorkSource;

public class WifiControllerDeviceInactiveStateProcessMessageMethodHook extends SingletonMethodHook {
	public static final String WIFI_CONTROLLER = "com.android.server.wifi.WifiController";
	public static final String DEVICE_INACTIVE_STATE = WIFI_CONTROLLER + "$DeviceInactiveState";

	protected static final boolean HANDLED;
	protected static final boolean NOT_HANDLED;

	protected static final int CMD_SCREEN_ON;
	protected static final int CMD_USER_PRESENT;
	protected static final int CMD_LOCKS_CHANGED;

	protected static final Field M_NO_LOCK_HELD_STATE;
	protected static final Field M_DEVICE_ACTIVE_STATE;
	protected static final Field M_LOCKS;
	protected static final Field M_TMP_WORK_SOURCE;
	protected static final Field M_WIFI_STATE_MACHINE;

	protected static final Method GET_CURRENT_STATE;
	protected static final Method TRANSITION_TO;
	protected static final Method GET_STRONGEST_LOCK_MODE;
	protected static final Method UPDATE_BATTERY_WORK_SOURCE;

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

				/* Take a default action only for high performance locks,
				 * Otherwise only update battery stats ignoring any locks held. */
				if (strongestLock != WifiManager.WIFI_MODE_FULL_HIGH_PERF) {
					WorkSource tmpWorkSource =
							(WorkSource) M_TMP_WORK_SOURCE.get(controller);
					Object stateMachine = M_WIFI_STATE_MACHINE.get(controller);

					tmpWorkSource.clear();
					UPDATE_BATTERY_WORK_SOURCE.invoke(stateMachine, tmpWorkSource);

					param.setResult(HANDLED);
				}

			}
		}

	}

	static {
		Class<?> clazz = findClass(WIFI_CONTROLLER, null);

		HANDLED      = getStaticBooleanField(clazz, "HANDLED");
		NOT_HANDLED  = getStaticBooleanField(clazz, "NOT_HANDLED");

		CMD_SCREEN_ON      = getStaticIntField(clazz, "CMD_SCREEN_ON");
		CMD_USER_PRESENT   = getStaticIntField(clazz, "CMD_USER_PRESENT");
		CMD_LOCKS_CHANGED  = getStaticIntField(clazz, "CMD_LOCKS_CHANGED");

		M_NO_LOCK_HELD_STATE   = findField(clazz, "mNoLockHeldState");
		M_DEVICE_ACTIVE_STATE  = findField(clazz, "mDeviceActiveState");
		M_LOCKS                = findField(clazz, "mLocks");
		M_TMP_WORK_SOURCE      = findField(clazz, "mTmpWorkSource");
		M_WIFI_STATE_MACHINE   = findField(clazz, "mWifiStateMachine");

		GET_CURRENT_STATE           = findMethodBestMatch(clazz, "getCurrentState");
		TRANSITION_TO               = findMethodBestMatch(clazz, "transitionTo",
				GET_CURRENT_STATE.getReturnType());
		GET_STRONGEST_LOCK_MODE     = findMethodBestMatch(M_LOCKS.getType(),
				"getStrongestLockMode");
		UPDATE_BATTERY_WORK_SOURCE  = findMethodBestMatch(M_WIFI_STATE_MACHINE.getType(),
				"updateBatteryWorkSource", WorkSource.class);
	}

}
