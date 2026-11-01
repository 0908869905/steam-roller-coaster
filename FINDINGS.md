# steam教案 - 技術發現

記錄格式：問題 → 原因 → 解決方案 → 選擇理由

---

## 2026-11-01: 發射觸發方式文件與程式碼不一致

### 問題
CLAUDE.md「開發規範」「注意事項」和 WIRING.md 都寫發射是 Shuffleboard 虛擬按鈕（SmartDashboard boolean "發射"），且「LaunchCommand 結束時自動重置為 false」，但程式碼不是這樣運作

### 分析
- 目前 `RobotContainer.java`（最後修改 2026-05-29 20:23，即 05-30 部署實測的版本）實際是 `Joystick`（port 0）+ `JoystickButton`：
  - 按鈕 1 `onTrue` → 觸發 LaunchCommand
  - 按鈕 2 `whileTrue` → 按住持續轉動
- `LaunchCommand.java` 沒有任何重置 "發射" 的程式
- Shuffleboard 虛擬按鈕是 05-17 的設計（見下方「2026-05-17: 實體按鈕 → Shuffleboard 虛擬按鈕」），05-28 硬體實測時已改成搖桿按鈕，但文件沒跟著改

### 解決方案
- 以程式碼為準：實際是 Driver Station 的 USB 搖桿（port 0），按鈕 1 發射、按鈕 2 按住持續轉
- CLAUDE.md 已同步修正（開發規範、注意事項，以及目錄結構中 RobotContainer.java 的說明）
- 已告知用戶此差異，用戶未表示異議

### 選擇理由
- 程式碼是 05-30 部署實測時實際運作的版本，落後的是文件
- 專案已完工，不為了配合舊文件去改已實測過的程式

### 備註
- WIRING.md 已於公開版同步修正（PARTS_LIST.md 同一句說明一併修正）
- 本檔 2026-05-17 條目是當時的設計紀錄，保留不改；PROGRESS.md 2026-05-30 條目寫「Shuffleboard 虛擬按鈕觸發」，也與程式碼不符，同樣保留原樣

---

## 2026-05-29: 中文路徑導致 gradlew.bat ClassNotFoundException

### 問題
在包含中文字元的 Windows 路徑下執行 `gradlew.bat`，報 `ClassNotFoundException` 無法啟動 Gradle wrapper

### 原因
`gradlew.bat` 使用的 Java classpath 解析無法正確處理中文（非 ASCII）字元的路徑。Windows cmd/PowerShell 在傳遞含中文的路徑給 JVM 時，codepage 轉換可能導致路徑損壞。

### 解決方案
將專案資料夾改為純英文名稱（例如 `C:\Users\xxx\Downloads\steam`），避免路徑中出現任何中文字元

### 選擇理由
- 這是 Gradle wrapper 的已知限制，短期內不會修復
- 改資料夾名稱是最簡單直接的 workaround
- 長期建議：所有開發專案都放在純英文路徑下

---

## 2026-05-29: 所有 System.out 改為 Shuffleboard 的設計決策

### 問題
原本程式中大量使用 System.out.println 輸出除錯資訊到 RioLog，但活動現場需要更直覺的即時資料呈現

### 分析
- System.out 只能在 RioLog 終端機看到，是串流文字，不適合即時監控
- Shuffleboard/SmartDashboard 可在圖形介面即時顯示數值、狀態字串、布林值
- 活動現場學生和老師不需要看終端機，只需看 Shuffleboard 面板

### 解決方案
- 數值資料（RPM、速度）→ `SmartDashboard.putNumber()`
- 狀態文字（馬達狀態、發射狀態）→ `SmartDashboard.putString()`
- 布林值（通過頂部、完成一圈）→ `SmartDashboard.putBoolean()`
- 完全移除所有 System.out，不留任何 RioLog 輸出

### 選擇理由
- 教學場景需要視覺化呈現，Shuffleboard 面板比終端機直覺得多
- 減少 RioLog 的噪音，讓真正的錯誤訊息更容易被發現
- 所有 Shuffleboard key 使用中文（給台灣教學現場使用）

---

## 2026-05-28: SparkMax CAN ID 是 1 而非出廠預設 0

### 問題
CAN ID 0 不會 timeout，但馬達完全不動

### 原因
SparkMax 出廠預設 CAN ID 是 0，但此顆 SparkMax 已被設定為 CAN ID 1（可能之前被其他隊伍或工具修改過）。CAN ID 0 不 timeout 是因為 CAN bus 上沒有衝突，但指令送到了不存在的裝置。

### 解決
將 `Constants.java` 中 `LAUNCHER_MOTOR_ID` 改為 1

### 教訓
若馬達不動但不報 CAN timeout，優先嘗試其他 CAN ID（1, 2, ...），或用 REV Hardware Client 查詢實際 ID

---

## 2026-05-28: SparkMax CAN JST 接頭接觸不良

### 問題
間歇性 CAN timeout 錯誤，馬達時動時不動

### 原因
SparkMax 的 CAN Bus JST 接頭容易鬆脫，特別是測試環境中線材被移動時

### 解決
確實壓緊 JST 接頭，聽到卡榫聲。測試期間避免移動 CAN 線材。

### 預防
- 部署前檢查所有 JST 連接
- 正式安裝時考慮用熱縮管或束帶固定 JST 接頭
- CAN 線材走線要避開會被移動的區域

---

## 2026-05-28: NEO 550 PID kP 值需遠低於預設

### 問題
kP=0.0004 造成 ±1000 RPM 的劇烈震盪

### 原因
NEO 550 的 MAX_RPM=11000，比 NEO Vortex (5000) 高 2.2 倍。相同的 kP 值產生的控制力矩過大，導致過衝（overshoot）和震盪。

### 解決
kP 降至 0.0001，轉速穩定無震盪

### 關鍵參數
| 參數 | 震盪值 | 穩定值 |
|------|--------|--------|
| kP | 0.0004 | 0.0001 |
| kI | 0 | 0 |
| kD | 0 | 0 |
| kFF | 視需求 | 視需求 |

### 備註
- 透過 Shuffleboard 即時調校面板找到穩定值
- kP=0.0001 待寫回 Constants.java（目前只在 Shuffleboard 調過）

---

## 2026-05-28: IR sensor 測速改為球直徑法

### 問題
原設計用相鄰 sensor 間距 / 通過時間算平均速度，但需要至少 2 顆 sensor 觸發才有一個速度值

### 新方案
改為單顆 sensor 瞬時速率：球直徑（4cm）/ sensor 上的通過時間

### 實現方式
- 同時註冊 falling edge 和 rising edge 中斷
- Falling edge = 球進入偵測範圍（開始計時）
- Rising edge = 球離開偵測範圍（結束計時）
- 速率 = BALL_DIAMETER / (rising_time - falling_time)

### 優勢
- 每顆 sensor 獨立產生速度值（4 顆 = 4 個獨立測量點）
- 不需要精確的 sensor 間距
- 可以看到球在軌道各位置的即時速度

### 注意
- 球直徑假設為 4cm（標準乒乓球），已加入 Constants.BALL_DIAMETER
- 實際有效偵測寬度可能略小於球物理直徑，需實測校正

---

## 2026-05-28: RioLog 中文顯示需要 JVM UTF-8 參數

### 問題
RioLog 終端機上中文 SmartDashboard key 和 System.out 中文輸出顯示亂碼

### 原因
RoboRIO 上的 JVM 預設編碼非 UTF-8，即使 javac 編譯時用了 UTF-8，執行時 stdout 仍可能用其他編碼

### 解決
在 `build.gradle` 中對 JVM 加入執行時參數：
```groovy
-Dfile.encoding=UTF-8
-Dstdout.encoding=UTF-8
// 其他相關 encoding 參數
```

### 備註
這與 build.gradle 中 javac 的 `options.encoding = 'UTF-8'` 是不同層面：
- `options.encoding` = 編譯時原始碼檔案編碼
- `-Dfile.encoding` = 執行時 JVM 字元編碼

---

## 2026-05-28: REV Hardware Client USB-C 無法偵測 SparkMax

### 問題
下載安裝 REV Hardware Client 1.7.5 後，透過 USB-C 連接 SparkMax 但無法偵測到

### 可能原因
- USB-C 線可能只有充電功能（無資料線）
- SparkMax USB-C 驅動程式未正確安裝
- SparkMax 需要先有 CAN 供電才能 USB 通訊（待確認）

### 現況
尚未解決，但不影響開發（CAN 通訊正常，PID 透過 Shuffleboard 調校）

---

## 2026-05-27: RoboRIO 映像更新 2021 → 2026

### 問題
RoboRIO 上的映像是 FRC_roboRIO_2021_v3.0（5 年前），無法執行 WPILib 2026 程式

### 解決流程
1. 安裝 NI FRC Game Tools（含 roboRIO Imaging Tool）
2. USB 連線 RoboRIO（172.22.11.2）
3. 使用 roboRIO Imaging Tool 刷入 FRC_roboRIO_2026_v1.2
4. 設定隊號 6998

### 關鍵資訊
- **Imaging Tool 路徑**：`C:\Program Files (x86)\National Instruments\LabVIEW 2025\project\roboRIO Tool\roboRIO_ImagingTool.exe`
- **NI Package Manager**：`C:\Program Files\National Instruments\NI Package Manager\`
- 映像更新需要 USB 連線（非 WiFi/Ethernet）
- 更新後韌體版本：6.0.0f1

### 注意
- C 槽需要足夠空間安裝 NI Game Tools（約需 2-3 GB）
- 若 C 槽空間不足，可壓縮 WSL 虛擬磁碟釋放空間：`wsl --shutdown` → `Optimize-VHD` 或 `diskpart compact`

---

## 2026-05-27: 首次部署 gradlew deploy

### 問題
首次將程式碼部署到 RoboRIO

### 結果
- `./gradlew deploy` BUILD SUCCESSFUL in 42s
- JRE 自動安裝到 RoboRIO（roborio-2024-17.0.9u7-3）
- REVLib native libraries 自動部署
- 機器人程式自動啟動

### 注意
- 首次部署會安裝 JRE，耗時較長（後續部署會快很多）
- 部署前確認 RoboRIO 映像版本與 WPILib 版本匹配（2026 對 2026）

---

## 2026-05-17: 配電板選擇 PDP vs PDH

### 問題
需要選擇配電板：PDP（Power Distribution Panel）或 PDH（Power Distribution Hub）

### 比較
| 項目 | PDP (CTRE) | PDH (REV) |
|------|-----------|-----------|
| 價格 | 較便宜 | 較貴 |
| 通訊 | CAN Bus | CAN Bus |
| 接口 | Weidmuller/螺絲端子 | WAGO lever nuts |
| 監控 | 電流監控各 slot | 電流監控各 slot |

### 決策
選擇 PDP，理由：成本考量（教案預算有限），功能對此專案完全夠用

---

## 2026-05-17: RoboRIO 1 電源接線

### 問題
PDP 到 RoboRIO 1 的電源線不是隨附的，需要自製

### 解決方案
- 使用 18AWG 紅黑線自製
- RoboRIO 1 電源輸入是綠色 Weidmuller 端子台（螺絲鎖緊）
- 接到 PDP 的 20A slot

### 注意
- RoboRIO 2 用 ferrule + Weidmuller，但我們是 RoboRIO 1
- 18AWG 足夠 RoboRIO 電流需求（峰值 ~2A）

---

## 2026-05-17: 馬達改型 NEO Vortex → NEO 550

### 問題
原設計使用 NEO Vortex + SparkFlex，但實際取得的馬達是 NEO 550

### 影響與調整
| 項目 | NEO Vortex | NEO 550 | 變更 |
|------|-----------|---------|------|
| 控制器 | SparkFlex | SparkMax | 類別名稱更換 |
| MAX_RPM | 5000 | 11000 | NEO 550 轉速更高 |
| DEFAULT_TARGET_RPM | 2000 | 3000 | 配合更高轉速 |
| 電流限制 | 40A | 30A | NEO 550 較小，降低保護 |
| PID/FF | 舊值 | 需重新調校 | 實機測試決定 |

### 選擇理由
- NEO 550 體積更小、重量更輕，適合乒乓球發射（不需要大扭力）
- 11000 RPM 的高轉速對單邊滾輪發射反而有利（更容易達到目標出口速度）
- SparkMax 與 SparkFlex API 幾乎相同，程式改動極小

---

## 2026-05-17: IR sensor 改型 Break-Beam → Flying Fish 反射式

### 問題
原設計使用對射式 IR break-beam，改為 Flying Fish 反射式模組

### 分析
- **邏輯相同**：兩者都是 active low + falling edge 觸發，程式邏輯完全不用改
- **觸發方式不同**：break-beam 是光束遮斷，Flying Fish 是偵測反射光
- **潛在風險**：乒乓球白色表面反射率需實測驗證，環境光可能干擾

### 決策
- 程式碼只改註解，不改邏輯（DIO 讀取 + AsynchronousInterrupt 完全相同）
- 實測時需注意 sensor 與球面的距離（太遠反射不到、太近會誤觸發）

---

## 2026-05-17: 實體按鈕 → Shuffleboard 虛擬按鈕

### 問題
移除實體按鈕（LA38-11D），改用 Shuffleboard SmartDashboard boolean toggle

### 解決方案
- `RobotContainer.java` 移除 `DigitalInput`，改用 `SmartDashboard.getBoolean("發射", false)` 輪詢
- `LaunchCommand.java` 在 `end()` 中重置 "發射" 為 false（自動回彈）
- 活動現場用筆電/平板操作 Shuffleboard 即可發射

### 選擇理由
- 減少一條 DIO 接線，簡化電控
- Shuffleboard 介面更直覺，可同時顯示速度數據 + 發射按鈕
- 適合教學場景：學生調整 RPM 後按螢幕上的按鈕發射

---

## 2026-05-16: 速度測量方案選擇

### 問題
如何精確測量乒乓球射出後進入圓形軌道的速度？

### 比較方案

| 方案 | 精度 | 成本 | 複雜度 | 結論 |
|------|------|------|--------|------|
| IR Break-Beam（光電閘） | ±0.05% | 低 | 低 | ✅ 採用 |
| Hall Effect 磁感測器 | ±2-5% | 低 | 中 | ❌ 乒乓球非金屬 |
| 高速攝影 + OpenCV | ±2% | 高 | 高 | ❌ 過度複雜 |
| 旋轉編碼器 | ±1% | 中 | 高 | ❌ 機構複雜 |

### 選擇理由
- IR break-beam 在 RoboRIO 上可用 AsynchronousInterrupt + FPGA timestamp，精度達微秒級
- 乒乓球 40mm > 軌道寬 35mm，球必定遮斷光束，觸發可靠
- 4 顆 sensor 繞圓周每 90° 一顆，可算出 3 段平均速度

### 關鍵技術決策

**計時方式**：RoboRIO FPGA timestamp（非 System.currentTimeMillis）
- FPGA 有獨立硬體時鐘，不受 Java GC 或 OS 排程影響
- 精度：微秒級，對 ~10ms 級的通過時間綽綽有餘

**中斷觸發邊緣**：Falling edge（break-beam 遮斷 = LOW）

**sensor 間距物理量**：
- 圓周弧長每 90° = π × 0.30 / 4 = 0.2356 m
- 球速 3 m/s 時通過相鄰 sensor 時間 ≈ 78.5 ms

---

## 2026-05-16: 單邊滾輪直驅 + 乒乓球

### 問題
單邊滾輪對乒乓球的速度傳遞效率 η 未知

### 分析
- 乒乓球是中空塑膠球（2.7g），極輕
- 單邊接觸 → 效率比雙滾輪低，且不穩定
- 無法用公式預測 v_exit = η × v_wheel，η 必須實測

### 解法
- 用光電閘實測不同 RPM 對應的出口速度
- 建立 RPM → v_exit 對照表
- Shuffleboard 上設定 RPM，即時顯示實測速度

---

## 2026-05-16: 鉛直圓周運動物理計算

### 參數
- 乒乓球：m = 2.7g, r = 20mm
- 圓形軌道內徑：D = 30cm, R = 15cm
- 有效半徑：R - r = 13cm
- 軌道寬度：35mm

### 最低速度（滑動假設）
- 頂部：v_top_min = √(g(R-r)) = 1.13 m/s
- 底部入口：v_entry_min = √(5g(R-r)) = 2.52 m/s
- 建議目標（含摩擦 + 安全係數 ×1.3）：~3.5 m/s

### 備註
乒乓球在 35mm 雷切軌道上，兩側邊緣支撐，實際所需速度可能比理論高 20-40%

---

## 2026-05-16: Windows Gradle SSL PKIX 問題修復

### 問題
`./gradlew build` 下載 WPILib Maven 依賴時報 `PKIX path building failed`，無法驗證 frcmaven.wpi.edu 的 SSL 憑證。

### 原因
Java（Gradle JVM）預設使用自己的 cacerts truststore，不信任 Windows 系統憑證存放區中的 CA 根憑證。公司/學校網路可能有中間人 proxy 注入自簽憑證。

### 解決方案
在 `gradle.properties` 加入：
```properties
systemProp.javax.net.ssl.trustStore=NUL
systemProp.javax.net.ssl.trustStoreType=Windows-ROOT
```
這讓 JVM 直接使用 Windows 憑證存放區，而非 Java 自帶的 cacerts。

### 適用場景
- Windows 環境 + 學校/企業網路
- 任何 Gradle 專案遇到 PKIX 問題都適用此修復

---

## 2026-05-16: REVLib 2026.0.5 Deprecated API

### 問題
Build 成功但有 5 個 deprecation warnings

### 被 deprecated 的 API
| API | 位置 | 說明 |
|-----|------|------|
| `velocityFF(double)` | ClosedLoopConfig | 速度前饋參數 |
| `ResetMode` enum | SparkBase | 配置重置模式 |
| `PersistMode` enum | SparkBase | 配置持久模式 |
| `configure(config, ResetMode, PersistMode)` | SparkBase | 三參數配置方法 |
| `setReference(double, ControlType)` | ClosedLoopController | 閉環控制目標設定 |

### 影響
- 不影響功能，程式正常運作
- REVLib 2026 可能在未來版本移除這些 API
- 等 REV 官方更新文件再遷移到新 API

---
