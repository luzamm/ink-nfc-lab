# 多设备与无 Root 可行性 · 2026-10-17

## 可交付的边界

0.5.0 已将手机读写拆成两条路径：

- **标准 NFC / ISO-DEP**：公共 Android API，不请求 Root；在系统已经能发现并激活板子的前提下，可运行现有 D1、认证、传图、D4/DE 流程。不是“任意手机都能唤醒华为板子”的承诺。
- **已验证/已允许 ST54L Root 路径**：已验证小米17 `pudding` / API37；`myron` 因硬件与小米17一致加入允许列表（API 35+，未完成本机全流程回归）。两者都不再要求特定 `libnfc-hal-st.conf` 路径，改为依赖允许列表和运行时 `NfcAdapter` 厂商接口检查。其他设备不自动发送私有 NCI、不注入进程、不改射频。

没有 NFC 的设备也可选图和预览。主界面不显示底层调试；右上角 `⋯` 可查看/导出设备信息、切换标准 NFC、进行已支持设备的诊断。切换为标准模式不会新增 Root 操作；若原先启动过兼容钩子，已有钩子可能继续存活至 240 秒期限，纯净对照需等其到期。

## 为什么“传图”和“7A 寻卡”是两回事

[Android NfcA 文档](https://developer.android.com/reference/android/nfc/tech/NfcA#transceive(byte[]))明确要求完整字节，并自动附 CRC，不能发送用来管理寻卡的短帧。因此 `transceive(new byte[]{0x7A})` 不是 7-bit 7A 的替代品，还必须先拿到 Tag。

[IsoDep 文档](https://developer.android.com/reference/android/nfc/tech/IsoDep#transceive(byte[]))支持已激活标签的 INF/APDU 数据交换，普通 NFC 权限即可。它不负责替 App 发任意轮询短帧。已有标签发现、可连 ISO-DEP、D1 正确、刷新完成是不同验收级别。

## 小米15是否值得适配

**值得，存在同系 ST54L 的具体源码/配置线索，但尚未在小米15真机上验证 7A 或刷屏。** 本轮检查的是第三方设备维护者发布的实际设备树/固件文件，不是小米对本 App 的兼容承诺。

- `dada-devel/device_xiaomi_dada` 的 `device.mk` 继承 `sm8750-common`。固定提交：`e97c8edf41e46402cdbaecff3faf399c3a5ffad7`。
  [继承关系](https://github.com/dada-devel/device_xiaomi_dada/blob/e97c8edf41e46402cdbaecff3faf399c3a5ffad7/device.mk)
- 公共树 `odm.prop` 声明 `ro.vendor.se.chip.model=ST54L`。固定提交：`02029f7f2c76466b7b4be5ae2e7651a72160d747`。
  [设备属性](https://github.com/dada-devel/device_xiaomi_sm8750-common/blob/02029f7f2c76466b7b4be5ae2e7651a72160d747/configs/properties/odm.prop)
- 公布的 HAL 配置含 `NCI_HAL_MODULE="nfc_nci.st21nfc"`、`st54l_fw.bin`、`st54l_conf.txt`。
  [HAL 配置](https://github.com/dada-devel/vendor_xiaomi_sm8750-common/blob/lineage-23.2/proprietary/odm/etc/libnfc-hal-st.conf)
- 同一公共文件清单也包含多个 NXP 固件，所以不能只靠“文件存在”证明每个 SKU、每个 ROM 的实际活动控制器。必须采集用户那台手机的属性、活动 HAL、Android API、NFC 服务版本及原生符号。

App 没有简单把 `dada` 加进小米17的注入白名单。小米15当前可尝试标准 NFC，并导出能力报告；Root 7A/MIFARE 兼容仍是明确的待适配项。

## 其他机型的优先级

| 设备 | 证据 | 本 App 的当前状态 |
|---|---|---|
| 小米17 pudding / Android17 | 本项目已有 7A、D1、D3、DE009000 实测 | 已验证 Root 配置；0.5.0 改动后尚待回连回归 |
| myron | 用户确认硬件与小米17的 ST54L 方案一致 | 已加入 Root 允许列表；API 35+，不依赖 HAL 配置文件，框架接口运行时检查，本机全流程待回归 |
| 小米15 dada | 上述公共树/固件指向 ST54L | 高优先级候选；标准路径已接入，专用唤醒未验证 |
| 小米15 Pro/Ultra 等同公共树机型 | 公共树声明覆盖这些设备 | 只列候选，不推断所有 SKU 同硬件 |
| Pixel 9 Pro/Pro XL | NFCGate 项目记录 ST54L 的自测 | 相近硬件候选，不代表 7A 或本项目补丁兼容 |
| Pixel 6/7 | NFCGate 记录 ST54J/ST54K | 同厂不同代，不复制 ST54L 的 RF 参数 |
| Galaxy S21 / 小米12 Pro 等 NXP 机型 | NFCGate 记录 NXP 控制器，部分型号有不确定标记 | 需要独立 NXP 后端；目前仅标准路径 |

[NFCGate 自测表](https://github.com/nfcgate/nfcgate/blob/v2/doc/Compatibility.md)仅证明它自己的捕获/中继/回放等功能，**没有测试本项目 7A 华为墨水屏唤醒**。这张表用于筛候选，不用于宣称已支持。

## 不 Root 的几种情形

1. **普通安装 APK，无额外特权**：标准 ISO-DEP 路径可行，但必须先由系统发现板子；依赖 7A 的板子没有通用公开 API 解决唤醒。0.5.0 已实现此路径，未拿新手机实测。
2. **ADB/Shizuku 权限，不 Root**：有研究价值，但不能等同普通 NFC 权限。本机反编译服务中 `sendVendorNciMessage` 及管理接口使用 `WRITE_SECURE_SETTINGS` 检查；[Android 权限说明](https://developer.android.com/reference/android/Manifest.permission#WRITE_SECURE_SETTINGS)也标明它不是普通第三方权限。[Shizuku](https://github.com/RikkaApps/Shizuku)提供 ADB/Root 权限进程访问系统 API 的机制。由此推断，部分 ROM 可能通过 Shell 权限走厂商轮询配置，但仍受 API、SELinux、厂商接口限制；尤其当前 MIFARE 兼容通过进程内注入完成，ADB 权限不等于能注入 NFC 服务。本轮没有实现或验证完整的无 Root 7A + 激活 + 刷屏链路，也未为 App 添加系统设置写权限。
3. **厂商系统签名/预装授权**：理论上可由厂商实现私有唤醒与协议选择，不是独立开发 App 可自行获得的能力。
4. **USB OTG 外置 PN532 等读卡器**：可以把私有寻卡和供电交给外设，Android USB 授权通常无需 Root；这是另一种产品路线，不是“手机内置 NFC 免 Root”，本版未实现 USB 驱动。
5. **iPhone**：本 APK 不适用于 iOS。[Apple Core NFC 的 ISO7816 标签接口](https://developer.apple.com/documentation/corenfc/nfciso7816tag)也不能作为已有证据支持任意 7-bit 轮询；没有完整复现结论。

## 后续真机验证步骤

```sh
ADB=/opt/homebrew/bin/adb ANDROID_SERIAL=手机序列号 bash tools/collect-nfc-profile.sh > nfc-profile.txt
```

默认只读取公开属性、筛选过的服务状态和包路径，不收集卡 UID、支付 AID 或原始 NFC 流量，不执行 Root。已有 Root 且用户愿意时可加 `--root` 读取固定配置文件摘要；此选项也不写配置。

1. 确认实际活动控制器、系统版本、服务/API 和 ABI，不以型号营销名判定。
2. 先标准发现/ISO-DEP/D1，再只读核对厂商接口；记录失败级别。
3. 若尝试私有 7A，先有配置备份、可验证恢复和专用设备档案；不能跳过原生库差异直接开注入。
4. 分别验证短帧实际发射/应答、ISO-DEP 激活、D1 与最终 DE009000。读回接受不能替代射频证据。
5. 小米15、其他手机和本版标准路径都还需要真实硬件验证。手机已断开，本轮没有尝试连接、Root 或修改任何新手机。
