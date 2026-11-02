# YT-Shield

Android local DNS filtering app using Android VpnService.

## Build APK
Push to `main`, then open **Actions → Android APK**. Download the `YT-Shield-debug-apk` artifact from the completed workflow run.

## Included
- Local VPN-based IPv4 UDP DNS filtering
- Built-in ad/tracker domain blocklist
- User-editable blocklist and allowlist
- Basic query/block counters

## Limitations
This starter filters DNS requests only. It does not decrypt HTTPS, filter IPv6 DNS, or reliably remove YouTube in-stream ads when ads share delivery domains with video content. Non-blocked DNS requests are forwarded to Cloudflare DNS (1.1.1.1); other app traffic is not sent through a remote VPN.
