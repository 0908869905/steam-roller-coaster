# steam教案 - 進度追蹤

## Session: 2026-05-30 (全部完工確認)

### 完成項目
- [x] 新筆電 deploy 成功確認
- [x] 完整發射流程實測（Shuffleboard 虛擬按鈕觸發）
- [x] 實際軌道球速測試
- [x] Shuffleboard layout 排版美化

### 修改檔案
- 無程式碼修改，本次為實體測試 + 部署確認

### 5-Question Reboot Check
1. **做什麼？** 球球過山車教案 — 程式部分已全部完工
2. **進度？** 100% 完成。Deploy、發射流程、軌道球速測試、Shuffleboard layout 全部完成
3. **下一步？** 專案程式部分已結束。後續僅需：活動現場支援、如有需要撰寫教案文件
4. **阻礙？** 無
5. **檔案？** 無需特別查看，程式碼已穩定

---

## Session: 2026-05-29 (Shuffleboard 改造 + 馬達限制移除 + 換筆電部署)

### 完成項目
- [x] System.out 全部改為 Shuffleboard（SmartDashboard）輸出
  - `LauncherSubsystem.java` — 移除所有 System.out，改為 SmartDashboard.putNumber("實際 RPM") 和 SmartDashboard.putString("馬達狀態")；移除 testSpin() 診斷方法和 periodicCounter
  - `SensorSubsystem.java` — 移除所有 System.out，在 periodic() 中用 SmartDashboard 顯示各 sensor 速度、通過頂部、完成一圈、最新偵測
  - `LaunchCommand.java` — 移除所有 System.out，改用 SmartDashboard.putString("發射狀態") 顯示發射中/完成/中斷/逾時
  - `RobotContainer.java` — 移除 System.out init 訊息，改用 SmartDashboard.putString("系統狀態"/"發射狀態")
- [x] Constants.java — kP 從 0.0004 更新為 0.0001（實測穩定值，上次 session 調出來的）
- [x] 馬達限制移除：移除 setTargetRPM 中的 Math.min(rpm, MAX_RPM) 上限 + 移除 smartCurrentLimit(30) 電流限制
- [x] 換筆電部署（新筆電路徑：C:\Users\6998a_rvpyw2t\Downloads\steam）
- [x] 解決中文路徑導致 gradlew.bat 無法運行問題（改用英文資料夾名稱 workaround）

### Shuffleboard 欄位清單（完整）
| 區域 | 欄位 | 類型 |
|------|------|------|
| 馬達 | 目標 RPM | Number |
| 馬達 | 實際 RPM | Number |
| 馬達 | 馬達狀態（停止/加速中/到位） | String |
| 馬達 | kP / kI / kD / kFF | Number（可調） |
| Sensor | 底部入口 / 90度 / 頂部 / 270度 速度 (m/s) | Number |
| Sensor | 通過頂部 | Boolean |
| Sensor | 完成一圈 | Boolean |
| Sensor | 最新偵測 | String |
| 發射 | 發射狀態 | String |
| 系統 | 系統狀態 | String |

### 修改檔案
- `src/main/java/frc/robot/Constants.java` — kP 0.0004 → 0.0001
- `src/main/java/frc/robot/subsystems/LauncherSubsystem.java` — 移除 System.out + testSpin() + periodicCounter + RPM上限 + 電流限制，改 SmartDashboard 輸出
- `src/main/java/frc/robot/subsystems/SensorSubsystem.java` — 移除 System.out，改 SmartDashboard 輸出（各 sensor 速度、狀態）
- `src/main/java/frc/robot/commands/LaunchCommand.java` — 移除 System.out，改 SmartDashboard.putString("發射狀態")
- `src/main/java/frc/robot/RobotContainer.java` — 移除 System.out，改 SmartDashboard.putString("系統狀態"/"發射狀態")

### 5-Question Reboot Check
1. **做什麼？** 球球過山車教案 — 完整發射流程實測 + 軌道測試 + Shuffleboard layout 美化
2. **進度？** 程式碼已全面改為 Shuffleboard 輸出（無 System.out）、kP 已寫回 Constants、馬達無限制；已換筆電但尚未確認 deploy 成功
3. **下一步？** 在新筆電確認 deploy 成功 → 完整發射流程測試（按鈕 1）→ 實際軌道球速測試 → Shuffleboard layout 排版美化
4. **阻礙？** REV Hardware Client USB-C 無法連線 SparkMax（舊問題）；新筆電中文路徑需用英文資料夾 workaround；尚未確認新筆電 deploy 是否成功
5. **檔案？** `LauncherSubsystem.java`（Shuffleboard 馬達狀態）、`SensorSubsystem.java`（Shuffleboard sensor 資料）、`LaunchCommand.java`（發射狀態）、`RobotContainer.java`（系統狀態）

---

## Session: 2026-05-28 (硬體實測 + PID 調校 + Sensor 驗證)

### 完成項目
- [x] Shuffleboard 參數面板（目標 RPM, kP/kI/kD/kFF 即時調校）
- [x] RioLog 終端機中文顯示修復（JVM -Dfile.encoding=UTF-8 等參數）
- [x] 馬達方向修正（config.inverted(true)）
- [x] CAN ID 確認為 1（非出廠預設 0）
- [x] PID 調校：kP=0.0001 穩定運轉（原始 0.0004 造成 ±1000 RPM 震盪）
- [x] IR sensor 測速改為「單顆 sensor 瞬時速率」（球直徑 4cm / 通過時間），falling + rising edge
- [x] 4 顆 IR sensor 全部測試通過
- [x] 按鈕 2 = 按住持續轉動（PID 調校用途）
- [x] CAN 接觸不良問題排除（JST 接頭需確實插好）
- [x] 下載 REV Hardware Client 1.7.5（但 USB-C 無法連線 SparkMax）

### 修改檔案
- `src/main/java/frc/robot/Constants.java` — CAN ID 改回 1, 新增 SPIN_BUTTON=2, BALL_DIAMETER=0.04
- `src/main/java/frc/robot/subsystems/LauncherSubsystem.java` — 馬達反轉 inverted(true), 即時 PID 調校面板, testSpin 診斷方法
- `src/main/java/frc/robot/subsystems/SensorSubsystem.java` — 改為 per-sensor 瞬時速率測量（falling+rising edge, 球直徑法）
- `src/main/java/frc/robot/commands/LaunchCommand.java` — 結果輸出改為 4 個位置速度
- `src/main/java/frc/robot/RobotContainer.java` — 新增按鈕 2 持續轉動綁定, Commands import
- `build.gradle` — 新增 JVM UTF-8 編碼參數（-Dfile.encoding=UTF-8, -Dstdout.encoding=UTF-8 等）

### 5-Question Reboot Check
1. **做什麼？** 球球過山車教案 — 完整發射流程測試 + 軌道實測 + Shuffleboard layout
2. **進度？** 馬達 PID 調校完成（kP=0.0001 穩定）、4 顆 IR sensor 驗證通過、測速邏輯已改為球直徑法；待存 PID 到 Constants + 完整發射流程測試
3. **下一步？** 把 kP=0.0001 寫回 Constants.java → 完整發射流程測試（按鈕 1）→ 實際軌道球速測試 → Shuffleboard layout 設計 → 清理 testSpin 診斷方法
4. **阻礙？** REV Hardware Client USB-C 無法連線 SparkMax（可能驅動或接口問題）；實際軌道尚待組裝測試
5. **檔案？** `Constants.java`（kP 待更新為 0.0001）、`LauncherSubsystem.java`（清理 testSpin）、`SensorSubsystem.java`（球直徑法測速）、`RobotContainer.java`（按鈕綁定）

---

## Session: 2026-05-27 (環境建置 + 首次部署)

### 完成項目
- [x] 清理 C 槽空間（0.19 GB → 5.07 GB），主要釋放來源：壓縮 WSL Ubuntu 虛擬磁碟（~4.5 GB）
- [x] 安裝 NI FRC Game Tools（NI Package Manager + roboRIO Imaging Tool）
- [x] RoboRIO 韌體更新：FRC_roboRIO_2021_v3.0 → FRC_roboRIO_2026_v1.2（韌體 6.0.0f1）
- [x] RoboRIO 隊號設定為 6998
- [x] `./gradlew deploy` 首次成功部署程式碼到 RoboRIO
- [x] JRE 已安裝到 RoboRIO（roborio-2024-17.0.9u7-3）
- [x] REVLib native libraries 已部署，機器人程式已啟動

### 修改檔案
- 無程式碼修改，本次為環境建置 + 部署作業

### RoboRIO 資訊
- **型號**：roboRIO-2101-FRC（RoboRIO 1 代）
- **映像**：FRC_roboRIO_2026_v1.2
- **韌體**：6.0.0f1
- **MAC**：（略）
- **隊號**：6998

### 5-Question Reboot Check
1. **做什麼？** 球球過山車教案 — PID 調校 + IR sensor 實測 + Shuffleboard layout
2. **進度？** 環境建置 100% 完成，程式已部署到 RoboRIO；待接硬體 + PID 調校 + sensor 實測
3. **下一步？** 接上 SparkMax + NEO 550 → PID 調校 → 接 IR sensor → 實測球速 → Shuffleboard layout
4. **阻礙？** 需要實體硬體（馬達 + sensor + 軌道）才能進行 PID 調校和 sensor 測試
5. **檔案？** `Constants.java`（PID 參數待調校）、`LauncherSubsystem.java`（馬達控制）、`SensorSubsystem.java`（sensor 邏輯）

---

## Session: 2026-05-17 (電控)

### 完成項目
- [x] 職責擴大確認：程式 + 電控（接線）都由自己負責，只有機構跟馬達不用管
- [x] 確認改用 PDP（Power Distribution Panel），不用 PDH（太貴）
- [x] 確認 RoboRIO 1（綠色 Weidmuller 端子版本）
- [x] 電源系統規劃完成：電池 → 主 breaker → PDP → (20A slot) → RoboRIO、PDP → (30A slot) → SparkMax
- [x] 確認 PDP → RoboRIO 電源線需自製（18AWG 紅黑線），非隨附
- [x] CAN Bus 接線講解（SparkMax JST-to-裸線 → RoboRIO CAN H/L），用戶表示到時候再處理

### 未完成 / 待做
- [ ] CAN Bus 實際接線（等到時候再做）
- [ ] IR sensor 安裝（等軌道好再裝）
- [ ] 實際接線施工（等零件到齊）

### 5-Question Reboot Check
1. **做什麼？** 球球過山車教案 — 電控接線（電源系統 + CAN Bus + sensor）
2. **進度？** 電源系統規劃完成（電池→主breaker→PDP→RoboRIO/SparkMax），CAN Bus 已講解但未動手
3. **下一步？** 自製 18AWG 電源線（PDP→RoboRIO）→ 接電源 → 接 CAN Bus → 等軌道好裝 sensor
4. **阻礙？** 軌道還沒好（sensor 等軌道好再裝）；CAN Bus 用戶說到時候再做
5. **檔案？** `WIRING.md`（接線表）、`PARTS_LIST.md`（零件清單）

---

## Session: 2026-05-17

### 完成項目
- [x] 下載 5 張零件圖片到 `parts-images/`（NEO Vortex, SparkFlex, IR Break-Beam, PDH, RoboRIO）
- [x] 確認職責分工：程式組只負責寫程式，電控/機構由其他人負責
- [x] 馬達改型：NEO Vortex → NEO 550（SparkFlex → SparkMax）
- [x] IR sensor 改型：對射 break-beam → Flying Fish 反射式（邏輯不變，都是 active low falling edge）
- [x] 按鈕改為虛擬：實體按鈕 → Shuffleboard SmartDashboard toggle "發射"
- [x] 更新 WIRING.md 和 PARTS_LIST.md 反映所有硬體變更
- [x] 全部改動通過 `./gradlew build`（BUILD SUCCESSFUL，5 個 REVLib deprecation warnings）

### 修改檔案
- `src/main/java/frc/robot/subsystems/LauncherSubsystem.java` — SparkFlex → SparkMax，電流限制 40A → 30A
- `src/main/java/frc/robot/Constants.java` — MAX_RPM 5000→11000, DEFAULT_TARGET_RPM 2000→3000, PID/FF 調整，移除 LAUNCH_BUTTON DIO
- `src/main/java/frc/robot/subsystems/SensorSubsystem.java` — 註解更新（Flying Fish 反射式，邏輯不變）
- `src/main/java/frc/robot/RobotContainer.java` — 移除 DigitalInput 按鈕，改用 SmartDashboard boolean "發射"
- `src/main/java/frc/robot/commands/LaunchCommand.java` — 結束時重置 "發射" 為 false
- `WIRING.md` — 反映硬體變更（SparkMax、Flying Fish、無實體按鈕）
- `PARTS_LIST.md` — 更新零件清單
- `parts-images/` — 新增 5 張零件參考圖片

### 5-Question Reboot Check
1. **做什麼？** 球球過山車教案 — RoboRIO 程式（IR 測速 + NEO 550 控制 + Shuffleboard 虛擬按鈕）
2. **進度？** 程式碼 100% 完成（含硬體改型），build 成功；待實測部署 + PID 調校
3. **下一步？** 等零件到貨 → deploy 到 RoboRIO → PID 調校（NEO 550 參數）→ IR sensor 實測 → Shuffleboard layout
4. **阻礙？** NEO 550 的 PID 參數需在真機調校；Flying Fish sensor 反射靈敏度需實測驗證（乒乓球白色表面反射率）
5. **檔案？** `Constants.java`（NEO 550 參數）、`LauncherSubsystem.java`（SparkMax 控制）、`SensorSubsystem.java`（sensor 邏輯）、`RobotContainer.java`（虛擬按鈕）

---

## Session: 2026-05-16 (下午)

### 完成項目
- [x] 建立完整 WPILib 2026 專案結構（GradleRIO 2026.2.1）
- [x] build.gradle（含 UTF-8 編碼設定）、settings.gradle、gradle wrapper
- [x] .wpilib/wpilib_preferences.json（隊號 6998）
- [x] gradle.properties（SSL 信任 Windows 憑證存放區修復）
- [x] vendordeps/REVLib.json（REVLib 2026.0.5）
- [x] 修正 Robot.java — 加入 main method（RobotBase.startRobot）
- [x] Build 成功（僅剩 5 個 REVLib deprecation warnings，不影響功能）
- [x] 確認採購清單：IR 對射感測器 ×4、按鈕 LA38-11D、乒乓球

### 修改檔案
- `build.gradle` — GradleRIO 2026.2.1 + UTF-8 javac 設定
- `settings.gradle` — 專案名稱設定
- `gradle/wrapper/gradle-wrapper.properties` — Gradle 8.11.1
- `gradle/wrapper/gradle-wrapper.jar` — Gradle wrapper binary
- `gradlew` / `gradlew.bat` — Gradle wrapper scripts
- `.wpilib/wpilib_preferences.json` — 隊號 6998、Java
- `gradle.properties` — SSL truststore 設定（解決 PKIX 問題）
- `vendordeps/REVLib.json` — REVLib 2026.0.5 完整 vendordep
- `src/main/deploy/` — 空部署目錄
- `src/main/java/frc/robot/Robot.java` — 加入 main method

### 5-Question Reboot Check
1. **做什麼？** 球球過山車教案 — RoboRIO 程式（IR 測速 + NEO Vortex 控制 + 按鈕介面）
2. **進度？** 專案結構 100%、程式碼 100%、build 成功；待實測部署
3. **下一步？** 等零件到貨 → deploy 到 RoboRIO → PID 調校 → IR sensor 實測
4. **阻礙？** IR sensor / 按鈕待採購到貨（蝦皮）；5/18 雷切軌道出件後才能裝 sensor
5. **檔案？** `build.gradle`（建置設定）、`gradle.properties`（SSL fix）、`vendordeps/REVLib.json`（REVLib 版本）、`Constants.java`（所有可調參數）

---

## Session: 2026-05-16

### 完成項目
- [x] 技術研究：速度測量方案比較（光電閘、Hall effect、高速攝影、編碼器）
- [x] 確定方案：IR break-beam × 4，AsynchronousInterrupt + FPGA timestamp 計時
- [x] 物理計算：乒乓球 + 30cm loop，最低入口速度 ≈ 2.52 m/s，建議 ≈ 3.5 m/s
- [x] 確認硬體規格：NEO Vortex/550 單邊滾輪直驅、軌道寬 35mm、雷切製作
- [x] 確認控制器：RoboRIO + SparkFlex
- [x] 程式碼框架完成：WPILib Java Command-Based（6 個 Java 檔）
- [x] 接線表 WIRING.md
- [x] 零件清單 PARTS_LIST.md
- [x] 更新 CLAUDE.md

### 修改檔案
- `src/main/java/frc/robot/Constants.java` — DIO pin、PID、物理常數
- `src/main/java/frc/robot/Robot.java` — TimedRobot 進入點
- `src/main/java/frc/robot/RobotContainer.java` — 按鈕綁定
- `src/main/java/frc/robot/subsystems/SensorSubsystem.java` — IR 中斷計時 + 速度計算
- `src/main/java/frc/robot/subsystems/LauncherSubsystem.java` — NEO Vortex PID 控速
- `src/main/java/frc/robot/commands/LaunchCommand.java` — 發射流程
- `WIRING.md` — 完整接線表
- `PARTS_LIST.md` — 零件清單
- `CLAUDE.md` — 專案架構文件

### 待做
- [x] ~~在 WPILib VS Code 建立正式專案（build.gradle + REVLib vendordep）~~ → 已完成（下午 session）
- [ ] PID 調校（需要實體硬體）
- [ ] 實測 RPM → 出口速度效率係數 η
- [ ] 測試 IR sensor 可靠性（乒乓球 40mm vs 軌道寬 35mm）
- [ ] Shuffleboard layout（活動現場用）
- [ ] 教案文件（教學流程、學習單）— 如有需要

### 5-Question Reboot Check
1. **做什麼？** 球球過山車教案 — RoboRIO 程式（IR 測速 + NEO Vortex 控制 + 按鈕介面）
2. **進度？** 程式碼框架 100% 完成，待建立 WPILib 專案 + 部署實測
3. **下一步？** WPILib VS Code 建新專案 → 加 REVLib vendordep → 放入程式碼 → deploy
4. **阻礙？** 等機構組 5/18 前出雷切軌道 + 間隔柱裝 IR sensor 才能實測；PID 需在真機調
5. **檔案？** `SensorSubsystem.java`（核心計時邏輯）、`LauncherSubsystem.java`（馬達控制）、`Constants.java`（所有可調參數）、`WIRING.md`（接線）

---

## Session: 2026-05-15

### 完成項目
- [x] 專案初始化（4 個 md 檔）
