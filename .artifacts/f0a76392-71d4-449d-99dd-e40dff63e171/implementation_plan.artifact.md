# Implementation Plan - Final Fix for APK Installation (FileProvider & Permissions)

This plan provides a definitive fix for the "missing fileprovider" or "failed to find configured root" error by using a more robust context and checking for modern Android installation permissions.

## User Review Required

> [!IMPORTANT]
> - **Installation Permission**: Starting with Android 8.0, apps must have the "Install Unknown Apps" permission enabled. If this is not granted, the installation intent will fail. I will add a check for this.
> - **Context Robustness**: In the previous code, I was using the context from the `BroadcastReceiver`, which can sometimes be restricted. I will now use the `applicationContext` consistently to resolve the `FileProvider`.
> - **Path mapping**: I will change `path="."` to `path=""` in `file_paths.xml`, as some Android versions require an empty string to represent the directory root.

## Proposed Changes

### 1. Update Manager Logic
#### [MODIFY] [UpdateManager.kt](file:///D:/Surya/Android%20APP/RenangAbsensi/app/src/main/java/com/coachbro/absenrenang/utils/UpdateManager.kt)
- Store a reference to `applicationContext`.
- Update `installApk()`:
    - Check `packageManager.canRequestPackageInstalls()` for API 26+.
    - If not granted, redirect the user to the system settings page for this app.
    - Use the stored `applicationContext` for `FileProvider.getUriForFile`.
- Add more error handling and toasts for "Permission Denied" scenarios.

### 2. FileProvider Configuration
#### [MODIFY] [file_paths.xml](file:///D:/Surya/Android%20APP/RenangAbsensi/app/src/main/res/xml/file_paths.xml)
- Change `path="."` to `path=""` for better compatibility.
- Keep `<root-path>` as a backup.

## Verification Plan

### Manual Verification
1.  **Grant Permission**: If prompted, allow "Install Unknown Apps" for FSS SwimTrack.
2.  **Trigger Update**: Download the APK.
3.  **Install Test**: Click "Instal Sekarang".
4.  **Confirm Installer**: Verify the system installer screen ("Do you want to install an update...") appears correctly.
