package dev.inknfclab;

public final class DeviceSupportTest {
    public static void main(String[] args) {
        for (int sdk = 26; sdk <= 40; sdk++) {
            for (String device : new String[]{"pudding", "myron", "dada", "haotian", "husky", "unknown", "", null}) {
                boolean verified = "pudding".equals(device) && sdk == 37;
                boolean allowed = verified || ("myron".equals(device) && sdk >= 35);
                require(DeviceSupport.verifiedRootProfile(device, sdk) == verified);
                require(DeviceSupport.allowedRootProfile(device, sdk) == allowed);
                require(DeviceSupport.useRoot(device, sdk, true, false) == allowed);
                require(!DeviceSupport.useRoot(device, sdk, false, false));
                require(!DeviceSupport.useRoot(device, sdk, true, true));
            }
        }
        require(!DeviceSupport.verifiedRootProfile("myron", 37));
        require(DeviceSupport.allowedRootProfile("myron", 35));
        require(DeviceSupport.summary("myron", 35, true).contains("ST54L"));
        require(DeviceSupport.summary("dada", 35, true).contains("尚待"));
        require(DeviceSupport.summary("pudding", 37, false).contains("无 NFC"));
        require(DeviceSupport.summary("unknown", 36, true).contains("不请求 Root"));
        System.out.println("Device support safety policy tests passed");
    }
    private static void require(boolean condition) { if (!condition) throw new AssertionError(); }
}
