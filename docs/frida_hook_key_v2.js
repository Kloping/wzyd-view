/**
 * 查询PID: adb shell pidof -s com.tencent.gamehelper.smoba
 * 附着式hook: frida -U <pid> -l frida_hook_key_v2.js
 */

Java.perform(function() {
    try {
        var DataProvider = Java.use("com.tencent.crossing.sx.DataProvider");

        DataProvider.getNetworkSymmetricKey.implementation = function() {
            var result = this.getNetworkSymmetricKey();
            console.log("[+] ========== KEY FOUND ==========");
            console.log("[+] Key (string): " + result);
            var hex = '';
            for (var i = 0; i < result.length; i++) {
                hex += ('0' + result.charCodeAt(i).toString(16)).slice(-2);
            }
            console.log("[+] Key (hex): " + hex);
            console.log("[+] Key (length): " + result.length);
            console.log("[+] ================================");
            return result;
        };
        console.log("[*] Hook installed. Use the app now...");

        try {
            var key = DataProvider.getNetworkSymmetricKey();
            console.log("[+] ========== KEY (direct call) ==========");
            console.log("[+] Key: " + key);
            var hex = '';
            for (var i = 0; i < key.length; i++) {
                hex += ('0' + key.charCodeAt(i).toString(16)).slice(-2);
            }
            console.log("[+] Key (hex): " + hex);
            console.log("[+] ==========================================");
        } catch(e2) {
            console.log("[*] Direct call failed (need instance or app init): " + e2);
            console.log("[*] Please browse any page in the app to trigger the hook");
        }
    } catch(e) {
        console.log("[!] Error: " + e);
    }
});