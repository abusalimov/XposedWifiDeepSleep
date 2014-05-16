package ru.abusalimov.xposed.util;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XCallback;

/**
 * Singleton-like: all instances are the same to avoid unnecessary
 * repeated hook invocations.
 */
public abstract class SingletonMethodHook extends XC_MethodHook {

	@Override
	public int compareTo(XCallback other) {
		if (this.equals(other)) {
			return 0;
		}
		return super.compareTo(other);
	}

	@Override
	public boolean equals(Object o) {
		if (getClass().isInstance(o)) {
			return true;
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return ~getClass().hashCode() ^ 0xdeadbeef;
	}

}