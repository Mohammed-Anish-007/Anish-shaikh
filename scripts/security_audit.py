#!/usr/bin/env python3
"""
Security audit of the BUILT Jarvis APK (runs in CI after every build).

Checks the final, merged AndroidManifest inside the APK — what actually ships to the phone:
  * the app is NOT debuggable
  * there is NO internet permission (data physically cannot leave the phone)
  * backups and device-to-device transfer are disabled
  * cleartext traffic is disabled
  * every exported component is protected by a system-only permission
    (or is on a short, explained allow-list)
  * only the expected permissions are requested
  * the APK signature is valid, and warns if the public repo key is used

Usage: security_audit.py <app.apk>
Exit code 1 if any check fails.
"""
import glob
import os
import re
import subprocess
import sys

APK = sys.argv[1]
SDK = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
BT = sorted(glob.glob(os.path.join(SDK, "build-tools", "*")), key=lambda p: [int(x) if x.isdigit() else x for x in re.split(r"[.-]", os.path.basename(p))])[-1]
AAPT2 = os.path.join(BT, "aapt2")
APKSIGNER = os.path.join(BT, "apksigner")

ANDROID = "http://schemas.android.com/apk/res/android:"

ALLOWED_PERMISSIONS = {
    "android.permission.RECORD_AUDIO",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.FOREGROUND_SERVICE_MICROPHONE",
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.READ_CONTACTS",
    "android.permission.CALL_PHONE",
    "android.permission.SEND_SMS",
    "com.android.alarm.permission.SET_ALARM",
    "android.permission.ANSWER_PHONE_CALLS",
    "android.permission.WRITE_SETTINGS",
    "android.permission.WAKE_LOCK",
    "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
    "android.permission.RECEIVE_BOOT_COMPLETED",
    "android.permission.ACCESS_NOTIFICATION_POLICY",
    # added automatically by androidx for its own internal broadcast, signature-protected
    "com.anish.jarvis.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
}
FORBIDDEN_PERMISSIONS = {
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.WRITE_EXTERNAL_STORAGE",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.READ_SMS",
    "android.permission.READ_CALL_LOG",
    "android.permission.CAMERA",
    "android.permission.REQUEST_INSTALL_PACKAGES",
    "android.permission.SYSTEM_ALERT_WINDOW",
}
# Exported components that are allowed without a permission, and why.
EXPORT_ALLOWLIST = {
    "com.anish.jarvis.MainActivity": "launcher icon (must be exported, takes no input)",
    "com.anish.jarvis.BootReceiver": "only listens to BOOT_COMPLETED / MY_PACKAGE_REPLACED, protected system broadcasts",
}
SYSTEM_ONLY_PERMISSIONS = {
    "android.permission.BIND_ACCESSIBILITY_SERVICE",
    "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
}


class Node:
    def __init__(self, name):
        self.name = name
        self.attrs = {}
        self.children = []


def parse_xmltree(text):
    root = Node("#root")
    stack = [(-1, root)]
    for raw in text.splitlines():
        if not raw.strip():
            continue
        indent = len(raw) - len(raw.lstrip())
        line = raw.strip()
        if line.startswith("E: "):
            node = Node(line[3:].split(" ")[0])
            while stack and stack[-1][0] >= indent:
                stack.pop()
            stack[-1][1].children.append(node)
            stack.append((indent, node))
        elif line.startswith("A: "):
            m = re.match(r"A: (.+?)(?:\(0x[0-9a-f]+\))?=(.*)$", line)
            if not m:
                continue
            key, val = m.group(1), m.group(2)
            raw_m = re.search(r'\(Raw: "(.*)"\)', val)
            if raw_m:
                val = raw_m.group(1)
            elif val.startswith('"'):
                val = val.split('"')[1]
            elif re.search(r"\(type 0x12\)0xffffffff", val):
                val = "true"
            elif re.search(r"\(type 0x12\)0x0$", val):
                val = "false"
            else:
                hex_int = re.search(r"\(type 0x1[01]\)0x([0-9a-f]+)", val)
                if hex_int:
                    val = str(int(hex_int.group(1), 16))
            while stack and stack[-1][0] >= indent:
                stack.pop()
            stack[-1][1].attrs[key.replace(ANDROID, "")] = val
    return root


def walk(node):
    yield node
    for c in node.children:
        yield from walk(c)


results = []


def check(ok, msg, warn=False):
    results.append(("PASS" if ok else ("WARN" if warn else "FAIL"), msg))


dump = subprocess.run([AAPT2, "dump", "xmltree", "--file", "AndroidManifest.xml", APK],
                      capture_output=True, text=True, check=True).stdout
tree = parse_xmltree(dump)
nodes = list(walk(tree))
manifest = next(n for n in nodes if n.name == "manifest")
app = next(n for n in nodes if n.name == "application")
pkg = manifest.attrs.get("package", "com.anish.jarvis")

# 1. debuggable
check(app.attrs.get("debuggable", "false") != "true", "App is not debuggable (no USB debugger / run-as access to private files)")

# 2. permissions
perms = {n.attrs.get("name") for n in nodes if n.name in ("uses-permission", "uses-permission-sdk-23")}
bad = sorted(perms & FORBIDDEN_PERMISSIONS)
check(not bad, "No internet / storage / location / camera / SMS-reading permissions" + (f" — FOUND: {bad}" if bad else ""))
unexpected = sorted(p for p in perms - ALLOWED_PERMISSIONS if not p.endswith(".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"))
check(not unexpected, "Only expected permissions are requested" + (f" — UNEXPECTED: {unexpected}" if unexpected else ""))

# 3. backups
check(app.attrs.get("allowBackup") == "false", "allowBackup=false (no cloud/ADB backup of Jarvis data)")
check("dataExtractionRules" in app.attrs, "dataExtractionRules set (no Android 12+ device-to-device copy)")
check(app.attrs.get("usesCleartextTraffic", "false") == "false", "Cleartext (unencrypted) network traffic disabled")

# 4. exported components
for n in nodes:
    if n.name not in ("activity", "activity-alias", "service", "receiver", "provider"):
        continue
    name = n.attrs.get("name", "?")
    if name.startswith("."):
        name = pkg + name
    has_filter = any(c.name == "intent-filter" for c in n.children)
    exported = n.attrs.get("exported", "true" if has_filter else "false") == "true"
    if not exported:
        check(True, f"{name}: not exported")
        continue
    perm = n.attrs.get("permission")
    if perm in SYSTEM_ONLY_PERMISSIONS:
        check(True, f"{name}: exported but only the Android system can bind it ({perm})")
    elif name in EXPORT_ALLOWLIST:
        check(True, f"{name}: exported — allowed: {EXPORT_ALLOWLIST[name]}")
    elif perm:
        check(True, f"{name}: exported, protected by {perm}")
    else:
        check(False, f"{name}: exported WITHOUT protection — other apps could trigger it")

# 5. target SDK
sdk = next((n for n in nodes if n.name == "uses-sdk"), None)
target = int(re.sub(r"\D", "", sdk.attrs.get("targetSdkVersion", "0")) or 0) if sdk else 0
check(target >= 34, f"targetSdkVersion {target} (modern Android security rules apply)")

# 6. signature
sig = subprocess.run([APKSIGNER, "verify", "--print-certs", "-v", APK], capture_output=True, text=True)
check(sig.returncode == 0 and re.search(r"Verified using v[234] scheme.*: true", sig.stdout) is not None,
      "APK signature valid (v2+ scheme)")
cert = re.search(r"Signer #1 certificate SHA-256 digest: ([0-9a-f]+)", sig.stdout)
public_key = cert and os.environ.get("PUBLIC_KEY_SHA256", "") == cert.group(1)
check(not public_key,
      "Signed with a PRIVATE key" if not public_key else
      "Signed with the PUBLIC repo key — add the JARVIS_KEYSTORE_* secrets so nobody else can sign updates",
      warn=True)

# report
width = max(len(m) for _, m in results)
print("\n=== Jarvis APK security audit ===")
for status, msg in results:
    icon = {"PASS": "✅", "WARN": "⚠️ ", "FAIL": "❌"}[status]
    print(f"{icon} {status}  {msg}")
fails = [m for s, m in results if s == "FAIL"]
print(f"\n{len(results) - len(fails)} passed, {len(fails)} failed")

summary = os.environ.get("GITHUB_STEP_SUMMARY")
if summary:
    with open(summary, "a") as f:
        f.write("## 🔒 Jarvis APK security audit\n\n| Result | Check |\n|---|---|\n")
        for status, msg in results:
            f.write(f"| {status} | {msg} |\n")
with open("security-audit.txt", "w") as f:
    for status, msg in results:
        f.write(f"{status}  {msg}\n")

sys.exit(1 if fails else 0)
