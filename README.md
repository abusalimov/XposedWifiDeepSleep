Wifi Deep Sleep
===============

**Only for Android KitKat.**

Once installed this mod prevents Wi-Fi from waking up during device sleep mode and when you turn on the screen without unlocking it (to look at the clock, for example).

Background
----------
Normally Wi-Fi can be awakened and kept on due to [Wifi locks](http://developer.android.com/reference/android/net/wifi/WifiManager.WifiLock.html) held by certain applications that often use to abuse this opportunity. This results in Wi-Fi being turned on over up to the half of the total sleep time (or even more) which in turn of course eats the battery. Another issue is that once turned on by a single application it notifies and awakes lots of other apps that may start, for example, their heavy synchronization tasks, etc., making everything even worse.

Auto Wi-Fi apps
---------------
Nevertheless, Wifi lock is a good mechanism when used carefully, and generally it should be respected when deciding whether to switch Wi-Fi on and off. This becomes a problem for most of Wi-Fi management apps found in Play Market: they can switch Wi-Fi off in the middle of a Skype call just because the screen was turned off.

The solution
------------
This mod alters a system state machine responsible for managing Wi-Fi so that once put to sleep it can not be woken up by any application that requests a lock. Wi-Fi is only put asleep when no application holds a lock. That is it doesn't cancel locks held already, but once all of them are gone, no one else is able to aquire a new lock until the screen is turned on and unlocked. This doesn't apply to special [high performance](http://developer.android.com/reference/android/net/wifi/WifiManager.html#WIFI_MODE_FULL_HIGH_PERF) Wifi lock, it still can be aquired as usual even in asleep mode (anyway, it seems that applications does not use to use this lock too often, so the battery should not became an issue).

Installation
------------
 1. Download and install [Xposed framework](http://repo.xposed.info/module/de.robv.android.xposed.installer)
 2. Search for and install **TBD** module
 3. Activate the module and reboot
