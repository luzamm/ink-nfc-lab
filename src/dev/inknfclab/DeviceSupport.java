package dev.inknfclab;

public final class DeviceSupport {
    public static boolean verifiedRootProfile(String device, int sdk) {
        return "pudding".equals(device) && sdk == 37;
    }

    /**
     * myron uses the same ST54L hardware as pudding. RootProbe still checks the
     * ST21NFC HAL at runtime, so this only removes the device-codename gate.
     */
    public static boolean allowedRootProfile(String device, int sdk) {
        return verifiedRootProfile(device, sdk) || ("myron".equals(device) && sdk >= 35);
    }

    public static boolean useRoot(String device, int sdk, boolean hasNfc, boolean standardOnly) {
        return hasNfc && !standardOnly && allowedRootProfile(device, sdk);
    }

    public static String summary(String device, int sdk, boolean hasNfc) {
        if (!hasNfc) return "无 NFC 硬件：可选图与预览，不支持手机刷屏。";
        if (verifiedRootProfile(device, sdk)) return "小米 17 / Android 17：有已验证的 ST54L Root 7A 配置；仍需原机固件接口与授权。";
        if ("myron".equals(device) && sdk >= 35) return "myron：硬件已确认与小米 17 的 ST54L 方案一致，已加入 Root 允许列表；仍需 HAL/框架接口检查，未完成本机全流程验证。";
        if ("dada".equals(device)) return "小米 15：已支持标准 ISO-DEP 路径，7-bit 7A 唤醒与 MIFARE 补丁尚待真机适配。";
        return "标准 ISO-DEP 路径：不请求 Root；仅在系统能发现并连接驱动板时可读写，不能保证 7A 唤醒。";
    }

    private DeviceSupport() {}
}
