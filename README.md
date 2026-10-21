本仓库为脱敏后的 Android 实现。历史记录中的 UID 已替换为合成测试值，不包含原始手机日志或签名私钥。

## 上游、许可与下载

- 主要上游：[kur0neko/huawei-epd-pn532](https://gitee.com/kur0neko/huawei-epd-pn532)。本项目改编其协议定义、D1 解析、四色编码及 D3 数据包布局，保留原有认证、上传、刷新与完成状态检查流程。
- 本项目与主要上游采用相同的 **PolyForm Noncommercial 1.0.0**，仅按该许可证允许的用途使用；不是 MIT/Apache，也不表示允许任意商用。见 [LICENSE](LICENSE)、[NOTICE](NOTICE)。
- 早期 PN532 参考项目：[solar2ain/ink-nfc-fmsc-by-pn532](https://github.com/solar2ain/ink-nfc-fmsc-by-pn532)。这不是本 Android App 的完整上游，也不代表其许可证与主要上游相同。
- [最新 APK / Release](https://github.com/wiwiwei/ink-nfc-lab/releases/latest)。当前为实验性调试签名构建，密钥不公开；自行构建会生成自己的调试密钥，可能不能覆盖官方附件。
- 真机结果：小米 17 / Android 17 / Root，经典算法与原厂 APC 完成 476 包上传，DE 返回 `009000`，用户确认画面无异常；七算法 × 三模式图像编码自检通过。`myron` 已按相同 ST54L 硬件加入 Root 允许列表，但尚未完成本机全流程回归。其他手机及免 Root 唤醒未验证。详见 [真机回归](docs/DEVICE_TEST-0.5.0.md)。
- 构建和安全边界见 [PUBLIC_SOURCE.md](PUBLIC_SOURCE.md)。下文保留历史开发记录；与本节冲突时以本节及真机回归记录为准。

# Ink NFC Lab 0.5.0 · Ink Studio

华为 552×768 四色墨水屏图像工作台。Android 标准 ISO-DEP 后端与小米17已验证的 Root/ST54L 唤醒后端分离；支持标准 API 不等于所有手机都能发送专用 7-bit 7A。

## 0.5.0 三项交付

1. 保留经典算法默认，算法目录扩展为七种；新增五类测试输入、35张离线对照图、批量图库与指标。`bash tools/algorithm-lab.sh` 可重现，详见 `docs/ALGORITHM_RESEARCH.md`。不能仅凭原测试图或 RGB 误差宣称实屏最佳。
2. 新增不请求 Root 的标准 NFC 读写会话，以及设备能力导出、连接方式选择、安全设备策略。未经适配的手机不注入、不写私有 NCI。小米15存在 ST54L 设备树/固件线索，但专用7A与激活未验证；详情及无Root路线见 `docs/DEVICE_COMPATIBILITY.md`。
3. 首页只保留预览、选图、折叠画面设置、必要状态和固定刷屏按钮；空闲时隐藏停止按钮。原有诊断/APC/日志全部在右上角 `⋯` 对话框，未删调试能力。

手机本轮未连接。已做离线测试与构建，不冒充安装或真机回归完成。安装包为 `build/ink-nfc-lab-debug.apk`，交付目录见 `release/`。

以下保留各版本的历史说明；与新版本后端策略冲突时，以本节和设备兼容性报告为准。

## 0.4.1 独立实验算法

“选择图片”下新增“算法”按钮，默认始终为“经典 0.4”，即已在实体面板上验证的 0.4.0 算法。原 `ImageQuantizer.java` 未修改，并用源码 SHA-256 与四组固定输出指纹防止回归；所有旧版 `ImageCodec.prepare` 调用仍走经典算法。

手动选择“实验优化 v1”才启用独立的 `ExperimentalQuantizer`，尝试平滑冷色去色权重与蛇形 Atkinson 扩散。它与照片/插画/黑白风格是两个独立选项，切换算法不改亮度、对比度、构图、旋转或抖动强度。预览、编码日志及刷图确认对话框明确标注本次算法。恢复画面默认参数会切回经典，重开 App 同样默认经典。

实验版不是已验证更优的替代品，也没有依据单张实拍照片修改或宣称校准面板色板。两版发送协议、颜色码和预览色板一致，方便用同图、同参数进行实体 A/B 对照；切换只重新生成预览，不自动写屏。

## 0.4.0 画面工作台

- 日常流程为选图、调节预览、底部固定按钮刷屏；连接诊断、APC 实验和日志默认折叠，保留停止入口。
- 照片、插画、黑白三种预设；可独立选择关闭/柔和/细腻抖动，完整显示或填满裁切，90° 旋转、亮度及对比度微调。原图对照不会改变实际发送内容。
- 四色匹配使用 Oklab 感知色差，误差扩散使用交替方向的 Floyd–Steinberg；中性灰只使用黑白，蓝/青等无法由红黄表达的冷色优先保留明暗。照片默认 0.85 抖动强度，插画关闭抖动，黑白模式禁用红黄。这些是可比较的处理策略，并不保证每张图都优于旧算法。
- 在 API 28+ 使用 ImageDecoder 按图片方向信息解码。参数变更拍快照并使用版本号丢弃过期处理结果，避免快速调整时旧预览覆盖新设置；处理期间禁止刷屏。
- 预览由同一份像素码生成，发送仍使用原有四色码、像素打包顺序、LZO、认证与 D3/D4/DE 流程。屏幕上的 RGB 色板为近似值，未经实体面板色度校准；预览不是实际纸面色彩的精确模拟。
- 上传进度与等待刷新分开显示；只有 DE 返回 `009000` 才报告成功，恢复配置及底层日志不再覆盖成功结果。低压失败时提示先调整耦合位置。

生成纯程序色阶/文字质量测试图和桌面预览（不读取照片）：

```sh
bash test.sh
java -Djava.awt.headless=true -cp build/tests tools/RenderQuantizerPreview.java build/examples/Ink-Studio-quality-test.png build/examples/ink-studio-algorithm-preview.png
```

`tests/AndroidImageSmoke.java` 是单独编译的手机端图像自检，不打入主 App；验证三个模式及旋转后的 423936 个预览像素与发送点阵逐点一致。不会执行 NFC 操作。

0.3.3 增加 APC 实验档选择与参数读取。默认不调整，可选相对本次原始参数的 -1、+1、+2，仅对下一次 7A 探测或刷图会话生效。实现参考本机小米 NFC 服务的 APC 策略，仅改 RF 自定义参数 2/3/4 的 W0、W24 两个字内各两个 5-bit 字段，沿用系统的 2..21 限幅及 CRC16。数值不等于瓦数，尚未确认调节方向与实际射频功率的对应关系；不能把 +2 称作最大功率或保证板子供电充足。

实验前保存三组完整原值到 `polling.snapshot.rf`，每次设置后读回逐字节验证；正常结束、取消或异常退出的 finally 路径恢复原值并读回。若进程被强杀、手机掉电或恢复失败，快照保留，下一次检查、探测、刷图或“停止 / 恢复轮询”先恢复；自动兼容注入不触碰 RF 快照。不能假设重启必定清除控制器设置。未修改 `/odm`、固件文件、硬件保护或系统射频校准文件。厂商配置命令是否跨重启持久保留尚未验证。

本版实现 root 厂商接口检查、限时 7-bit 0x7A 自定义轮询、Android NFC-A 标签发现、ISO-DEP 连接、D1 设备信息读取、图片选四色量化、LZO1X 字面量分块、双次认证、D3 上传、D4 刷新和状态轮询。打开 App 后自动启用 MIFARE 兼容拦截，无固定 UID 白名单；前台空闲时每 120 秒续期，后台停止续期，已有钩子最多 240 秒后卸载。只有用户确认刷图、注入成功且 D1 设备信息与当前图像格式兼容时才写入。

0.3.2 允许同协议驱动板的任意 4 字节 UID。拦截仍匹配已验证的 ISO-DEP/多协议激活结构，不会把所有普通 MIFARE 卡一律修改。换板后可直接运行“7A 探测”读取 D1，无需先选图片；该探测也会确认兼容注入就绪后才启动 Android 读卡。D1 中的 UID 一致性检查不包含白名单，尺寸、颜色和压缩格式限制仍适用于当前 552×768 四色编码器。

0.3.1 修正刷新阶段沿用 3 秒收发超时的问题：D4 与 DE 查询共用 60 秒预算，每次收发设置为剩余预算，并记录命令、原始响应、耗时和失败阶段。只有 DE 返回 `009000` 才报告刷新完成；D4 返回 `9000` 只表示刷新指令被接受。断连后不会重放 D4 或图像包。辅助进程刷图会话上限为 180 秒，避免原 90 秒等待过早恢复轮询。

本机最新测试中，NCI 查询和 7A 设置均成功，设置响应为 `1300`，读回为 `1500012A020A7A`。初版的 `0x06` 由轮询恢复时序竞态触发，改为连续配置事务后已消失。仍需实体驱动板的标签发现和 D1 响应，才能证明唤醒链路可用；设置接受不是独立射频测量。详见 VERIFICATION.md。

MIFARE 兼容注入器基于 Frida 17.24.0 Android arm64 官方发行文件；构建时按 SHA-256 校验。运行需已安装匹配版本的 `frida-server`（若已放在 `/data/local/tmp/ink-frida-server`，App 可按需启动）。启动 App 自动注入只修改进程内兼容行为，不设置 7A 轮询，也不发送图像。探测或刷图时会再次确认注入，系统服务异常或 Frida 版本不匹配时会中止该会话并恢复轮询配置。

## 使用

安装 `build/ink-nfc-lab-debug.apk`，开启 NFC，在 KernelSU 中允许 `Ink NFC Lab` 使用 root。打开 App 等待“MIFARE 兼容已开启 · 不限 UID”；换板后可先点“7A 探测”检查 D1。选择图片并确认刷图后，App 配置 7A 轮询，再启用 Android 读卡，D1 校验通过后逐包上传。刷图会覆盖当前画面；任何 APDU 失败均停止，不自动重放。刷新状态查询确认完成后会立即恢复手机原轮询配置；取消或关闭 App 同样请求恢复轮询。

“普通寻卡”本身可单独检查标准 Android 读卡链路，不设置 7A；App 启动时的兼容注入仍会使用 root。日志包含设备 UID，分享前可检查内容。App 不联网。

0.1.2 的“7A 探测”同时只读采集 `StNfcHal` 的 LL 收发日志，并保留厂商 NCI 通知。可显示 NCI 发现目标、接口激活、接口错误、数据包和 ST 固件调试通知；长包会按 HAL 包号拼接。方向“NFCC→主机”表示控制器向手机处理器发消息，不能直接视为墨水屏回复。ST `6F02` 调试通知尚未解码，不据此宣称收到 ATQA 或实际发射成功。不会修改系统日志属性、系统分区或固件。

探测结束后，“分享底层收发日志”导出原始记录（最多末尾 160000 字符）。完整日志在 App 私有目录 `polling.snapshot.trace.txt`，每次 7A 探测覆盖，仅采集本次时间窗口；可能包含附近标签数据。若当前系统未输出 HAL 日志，捕获计数为零不代表射频无回复。

标准 NCI 事件编号对照 [AOSP nci_defs.h](https://android.googlesource.com/platform/system/nfc/+/7066f224aaa6c240a58c8329219936e07753b019/src/include/nci_defs.h)。ST 私有记录和私有状态不套用标准含义。

## 调用路径与恢复

App 调用 `su -c` 启动本 APK 内 `RootProbe` 的 `app_process` 辅助进程。辅助进程初始化 Android NFC 服务管理器，运行主 Looper，在工作线程通过 `NfcAdapter` 厂商 NCI 回调交换命令。全局文件锁阻止多个新版辅助进程同时操作控制器。无需打包或分发从手机提取的 ST 系统 JAR。

ST 自定义轮询查询使用 GID 0x0F / OID 0x01 / 子命令 0x15；设置使用子命令 0x13。载荷来自手机当前 SDK 的 TLV 格式，`01 2A 02 0A 7A` 对应一帧、位置 1、标准响应、NFC-A 短帧、不加 CRC、等待字段 10、数据 0x7A。参数沿用本机系统已有的短帧条目结构，仅替换数据字节。该映射尚需真实射频测量确认。

写入前读取、验证并持久保存原始配置；若查询失败或格式不符则中止。设置后查询读回，恢复时也查询并逐字节核对。快照在本 App 私有目录 `polling.snapshot`，恢复验证完成后删除；残留快照会在下一次 root 操作时优先恢复。恢复失败或 55 秒看门狗超时时请求关闭再开启 NFC，并明确报告“恢复未验证”，不会把 NFC 开启状态当成配置已还原的证据。查询、设置和读回共用一次最长 30 秒的轮询暂停；完成读回后恢复轮询并等待标签，退出时另用一次暂停恢复原配置。这样避免 Android 异步恢复轮询与下一次命令交错。

进程被强杀、系统重启或断电可能打断软件清理；下次进入 App 使用“停止 / 恢复轮询”核对快照。此版本仅用于已确认的本机软件栈。

## 构建与检查

需要 JDK 17+、Android SDK platform 36、build-tools 36.0.0、`xz`、`shasum` 和 macOS/Linux 的 zip 工具。设置 `JAVA_HOME` 和 `ANDROID_SDK_ROOT` 后运行：

```sh
bash test.sh
bash build.sh
adb install -r build/ink-nfc-lab-debug.apk
```

项目使用原生 Java / Android SDK，无第三方 App 运行时依赖。脚本直接调用官方 aapt2、javac、D8、zipalign 和 apksigner；构建出的 APK 使用本地开发密钥签名。

仓库提供 `.github/workflows/build-apk.yml`。在 GitHub 的 Actions 页面选择 **Build APK** → **Run workflow** 即可构建；完成后在该次运行的 Artifacts 中下载 `ink-nfc-lab-debug-*.zip`。CI 每次使用临时调试密钥，连续覆盖安装前需先卸载旧包。

可在电脑直接执行 root 检查，便于区分 App 的 KernelSU 授权与 NFC 接口结果：

```sh
adb shell pm path dev.inknfclab
adb shell su -c 'CLASSPATH=/data/app/实际路径/base.apk app_process /system/bin dev.inknfclab.RootProbe inspect /data/local/tmp/ink-nfc.snapshot'
```

## 来源

D1 命令、设备 TLV 格式、四色色盘、点阵方向、LZO1X 格式、D3/D4/DE 命令及 552×1536/2 色到 552×768/4 色的特定屏幕修正来自 [huawei-epd-pn532](https://gitee.com/kur0neko/huawei-epd-pn532)。遵守上游 PolyForm Noncommercial 1.0.0 的非商业使用要求；参见 LICENSE 与 NOTICE。
