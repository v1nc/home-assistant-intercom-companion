# Home Assistant Intercom Companion

_Home Assistant Companion for Intercom Tablets like Hikvision, Metzler, Theben_

This is a fork of the Home Assistant Android Companion app to better work on intercom tablets.
Tablets from brandsd like Metzler and Theben that use Android are mostly based on Hikvision devices, running Android 10 with an old System Web View (e.g. 74). Updating the System Web View is hard or sometimes impossible.
This makes Home Assistant slow to use and makes it miss different front end features, because Home Assistant uses the System Web View.

**This fork bundles a more up to date Web View with the Home Assistant app and applies some quality of life features to make the app work better on intercom tables.**

## Version 1 (Chromium 93)
- branch `chromium-aw`
- bundles [chromium-aw](https://github.com/ridi/chromium-aw) which was discontinued on Chromium Version 93
- works with Android 10 Tablets
- **[Download](https://github.com/v1nc/home-assistant-intercom-companion/releases/download/version-1/home-assistant-webview-93.apk)**
