Wifi Deep Sleep
===============

**Only for Android KitKat.**

Once installed this mod prevents Wi-Fi from waking up during device sleep mode and when you turn on the screen without unlocking it (to look at the clock, for example).

Background
----------
Normally Wi-Fi can be awakened and kept on due to [Wi-Fi locks] (http://developer.android.com/reference/android/net/wifi/WifiManager.WifiLock.html) held by certain applications that often use to abuse this opportunity. This results in Wi-Fi being turned on over up to the half of the total sleep time (or even more) which in turn of course eats the battery. Another issue is that once turned on by a single application it notifies and awakes lots of other apps that may start, for example, their heavy synchronization tasks, etc., making everything even worse.
