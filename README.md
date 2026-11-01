# 球球過山車 STEAM 教案（steam-roller-coaster）

> 給學弟妹的物理教案「球球過山車」的控制程式與電控。NEO 550 滾輪把乒乓球射進直徑 30 cm 的圓形軌道做鉛直圓周運動，軌道上 4 顆紅外線反射式感測器各自量出球經過該點的速度，結果即時顯示在 Shuffleboard 面板上。
>
> 我負責程式與電控接線；軌道、滾輪等機構由隊上機構組製作（見 `PARTS_LIST.md`）。

| | |
|---|---|
| 作者 | 李昌侑（Rick Lee）— FRC 6998 UNIPARDS 程式組 |
| 期間 | 2026/05/15 – 2026/05/30（`PROGRESS.md` 共 9 筆工作紀錄）。原專案未使用 git，2026-11 整理後上傳，為單一提交；開發過程見 `PROGRESS.md` |
| 佐證／後續 | 實機紀錄見 `PROGRESS.md`：2026-05-28 四顆感測器測試通過、轉速穩定；05-30 完成完整發射流程與軌道球速實測，程式部分完工。RPM 對出口速度的效率係數 η 沒有留下實測表；教案文件（教學流程、學習單）不在本 repo |

**開發方式（AI 協作聲明）**：本專案以「與 AI 結對開發」完成：問題定義、架構設計、電控接線與實機驗證由我負責，程式碼由我與 AI（Claude Code）協作產出；每個模組做什麼、為什麼選這個方案、哪裡會失效，由我判斷並負責。`PROGRESS.md`／`FINDINGS.md`／`ERROR.md` 為開發期間的真實工作紀錄（公開版改動見文末）。

**相關專案**：[科展・電腦視覺計分](https://github.com/0908869905/scoring-analyzer) ・ [影像標註平台](https://github.com/0908869905/frc-train-review) ・ [偵察 App](https://github.com/0908869905/frc-scouting-pass) ・ [偵察掃描與 OPR](https://github.com/0908869905/frc-scout-scanner) ・ [報帳系統](https://github.com/0908869905/frc-expense-money) ・ [台灣手語影音辭典](https://github.com/0908869905/tsl-sign-dictionary) ・ [園遊會點餐系統](https://github.com/0908869905/ordering-system) ・ [練習場自動計分裝置](https://github.com/0908869905/frc-practice-scorer)

---

## 裝置在做什麼

按下 Driver Station 搖桿的按鈕 1，滾輪以面板上設定的轉速轉動，把乒乓球送進圓形軌道。球依序經過軌道底部入口、90°、頂部、270° 四顆感測器，每顆感測器各自以「球徑 ÷ 遮斷時間」算出球在該點的速度。四顆都量到速度，或 5 秒內沒有完成，程式就停下馬達，並把結果顯示在 Shuffleboard 上。

設計時的理論估算（滑動假設，`FINDINGS.md` 2026-05-16）：

| 項目 | 數值 |
|------|------|
| 乒乓球 | 2.7 g、直徑 40 mm |
| 軌道內徑 | 30 cm（有效半徑 R − r = 13 cm） |
| 頂部最低速度 | √(g(R − r)) ≈ 1.13 m/s |
| 入口最低速度 | √(5g(R − r)) ≈ 2.52 m/s |
| 設計目標 | 約 3.5 m/s（含摩擦與 1.3 倍安全係數） |

球在 35 mm 寬的雷切軌道上靠兩側邊緣支撐，`FINDINGS.md` 記錄實際所需速度可能比理論值高 20–40%。

## 硬體

| 元件 | 數量 | 說明 |
|------|------|------|
| roboRIO（第一代，roboRIO-2101-FRC） | 1 | 映像 FRC_roboRIO_2026_v1.2，隊號 6998 |
| SparkMax | 1 | CAN ID 1，控制 NEO 550 |
| NEO 550 無刷馬達 | 1 | 單邊滾輪直驅 |
| Flying Fish 紅外線反射式感測器 | 4 | 3.3–5 V 數位輸出，電位器可調偵測距離 |
| PDP 配電板 | 1 | 選 PDP 不選 PDH，成本考量 |
| FRC 12 V 電池 | 1 | |
| 乒乓球 | 10 顆以上 | 標準 40 mm |

機構組負責的部分：雷切圓形軌道板 2 片（直徑 30 cm，帶感測器開孔）、雷切直線軌道、滾輪、軌道支撐結構、3D 列印銜接件。

## 接線

詳見 `WIRING.md`。

| roboRIO 介面 | 接到 | 位置 |
|------|------|------|
| DIO 0 | 感測器 #0 | 圓形軌道底部（入口） |
| DIO 1 | 感測器 #1 | 90° |
| DIO 2 | 感測器 #2 | 頂部（制高點） |
| DIO 3 | 感測器 #3 | 270° |
| CAN H/L | SparkMax（CAN ID 1） | SparkMax 的 JST 轉裸線接到 roboRIO 的 CAN 端子 |

- 每顆感測器 OUT 接 DIO、VCC 接 5V、GND 接 GND，由 roboRIO 的 5V 供電。
- 電源：12 V 電池 → 120 A 主斷路器 → PDP；PDP 的 20 A 槽以自製 18 AWG 線接 roboRIO，30 A 槽接 SparkMax。
- 發射由 Driver Station 的 USB 搖桿（port 0）觸發：按鈕 1 執行一次發射，按鈕 2 按住持續轉、放開停止。roboRIO 端沒有實體按鈕。

## 程式架構

WPILib Java，Command-Based 架構，所有輸出都走 Shuffleboard（SmartDashboard），不使用 `System.out`。

| 檔案 | 做什麼 |
|------|------|
| `Robot.java` | 進入點，`main` 呼叫 `RobotBase.startRobot`；每個週期執行 `CommandScheduler` |
| `RobotContainer.java` | 建立兩個 subsystem，綁定搖桿：按鈕 1 `onTrue` 執行 `LaunchCommand`，按鈕 2 `whileTrue` 持續轉、放開停止 |
| `Constants.java` | 所有常數：搖桿 port 與按鈕、CAN ID、預設目標 3000 RPM、kP／kI／kD／kFF、到速容許誤差 100 RPM、四個 DIO 腳位、球徑 0.04 m |
| `subsystems/LauncherSubsystem.java` | SparkMax 速度閉迴路控制 NEO 550（`inverted(true)`）；每個週期讀面板上的 kP／kI／kD／kFF，有變動就即時套用；顯示實際 RPM 與馬達狀態（停止／加速中／到位） |
| `subsystems/SensorSubsystem.java` | 四顆感測器的中斷計時與速度計算；顯示各點速度、通過頂部、完成一圈、最新偵測 |
| `commands/LaunchCommand.java` | 一次發射：清除上次的感測資料 → 依面板的目標 RPM 轉動滾輪 → 四顆都量到速度或 5 秒逾時就停馬達，顯示「完成」「中斷」或「逾時」 |

## 技術重點一：用中斷量遮斷時間

Flying Fish 模組是 active low：球把紅外線反射回去時輸出 LOW（falling edge），球離開後回到 HIGH（rising edge）。

`TimedRobot` 的主迴圈每 20 ms 執行一次，球遮住一顆感測器的時間在 10 ms 量級（`FINDINGS.md` 的設計估算），在主迴圈裡輪詢 DIO 量不到。所以每顆感測器都註冊一個 `AsynchronousInterrupt`，falling 與 rising 兩個邊緣都觸發：

1. falling 邊緣當下讀一次 `Timer.getFPGATimestamp()`，記為進入時間。
2. rising 邊緣當下再讀一次，相減得到遮斷時間 Δt。
3. Δt 介於 0 到 1 秒之間才採用，速度 ＝ 球徑 0.04 m ÷ Δt。

中斷回呼與主迴圈在不同執行緒，`SensorSubsystem` 的讀寫都用 `synchronized` 保護。

最初的設計是用相鄰兩顆感測器之間的弧長除以時間差，要兩顆都觸發才有一個平均速度。2026-05-28 改成四顆感測器各自以球徑 ÷ 遮斷時間算速度，一次發射可以得到四個位置的速度（`FINDINGS.md`「IR sensor 測速改為球直徑法」）。

## 技術重點二：轉速先前饋、再回授

滾輪轉速用 SparkMax 內建的速度閉迴路（`ControlType.kVelocity`）。`Constants.java` 先給前饋 `kFF = 0.000090`，讓滾輪接近目標轉速，再用很小的 `kP` 修剩下的誤差，`kI`、`kD` 維持 0。

實機只調整 kP：0.0004 時轉速震盪約 ±1000 RPM，降到 0.0001 後穩定（`FINDINGS.md`、`ERROR.md` 2026-05-28）。這個值在 2026-05-29 寫回 `Constants.java`。

## Shuffleboard 面板

| 區域 | 欄位（中文 key） |
|------|------|
| 馬達 | 目標 RPM（預設 3000，可改）、實際 RPM、馬達狀態、kP／kI／kD／kFF（可改） |
| 感測器 | 底部入口／90度／頂部／270度 速度 (m/s)、通過頂部、完成一圈、最新偵測 |
| 發射 | 發射狀態（待機／發射中／完成／中斷／逾時） |
| 系統 | 系統狀態 |

面板上改的 kP／kI／kD／kFF 只在這次執行有效，重新啟動後回到 `Constants.java` 的值；要保留就寫回 `Constants.java`。

## 建置與部署

需要 WPILib 2026（GradleRIO 2026.2.1、REVLib 2026.0.5、Java 17）。

```bash
./gradlew build     # 建置（Windows 用 gradlew.bat）
./gradlew deploy    # 部署到 roboRIO（隊號 6998，見 .wpilib/wpilib_preferences.json）
```

- 在 WPILib VS Code 以外的終端機執行時，`JAVA_HOME` 要指向 WPILib 內附的 JDK 17（Windows 預設在 `C:\Users\Public\wpilib\2026\jdk`）。
- 專案路徑必須全是英文字元。
- `gradle.properties` 讓 Gradle 使用 Windows 憑證存放區，處理學校網路的 PKIX 憑證錯誤。
- roboRIO 映像要和 WPILib 同一年度（2026 對 2026）。首次部署會自動安裝 JRE，時間較長。
- 建置會出現 REVLib 的 deprecation 警告（`velocityFF`、`ResetMode`、`PersistMode`、三參數 `configure`、`setReference`），不影響功能，尚未改用新 API。

## 踩過的坑

| 症狀 | 原因 | 處理 |
|------|------|------|
| 馬達完全不動，也不報 CAN timeout | 這顆 SparkMax 的 CAN ID 已被改成 1，程式送到出廠預設的 ID 0，CAN 上沒有這個裝置 | `MOTOR_CAN_ID` 改成 1 |
| 馬達時動時不動，間歇性 CAN timeout | SparkMax 的 CAN JST 接頭鬆脫 | 壓到卡榫確實扣上，測試時避免移動 CAN 線 |
| 含中文的路徑下 `gradlew.bat` 報 `ClassNotFoundException` | wrapper 拼接 classpath 時處理不了非 ASCII 路徑 | 專案放在全英文路徑 |
| Gradle 報 `Type T not present` | 系統裝了 JDK 25 之類較新的版本，與 Gradle 8.11 不相容 | 改用 WPILib 內附的 JDK 17 |
| 中文 key 編譯報 `unmappable character`、RioLog 中文亂碼 | javac 跟隨 Windows 的 MS950 編碼；roboRIO 上的 JVM 執行時預設編碼也並非 UTF-8 | `build.gradle` 設定 `options.encoding = 'UTF-8'`，並加 `-Dfile.encoding=UTF-8` 等 JVM 參數 |
| REV Hardware Client 1.7.5 用 USB-C 偵測不到 SparkMax | 未確定，可能是只能充電的線、驅動，或需要 CAN 供電 | **未解決**。CAN 通訊正常，不影響使用 |

## 已知限制

- 球速以球徑 4 cm 計算。`FINDINGS.md` 記錄感測器的有效偵測寬度可能略小於球徑、需要實測校正，紀錄中沒有校正數據。
- 程式沒有限制目標轉速上限，也沒有設定馬達電流限制（2026-05-29 移除）。
- 感測器與球面的距離、環境光都會影響反射式感測器，安裝時要用模組上的電位器調整偵測距離。

## 公開版說明

原專案沒有使用 git，這是 2026-11 整理後的單一提交。與原專案的差異：

- `CLAUDE.md`、`PROGRESS.md` 刪去 roboRIO 的 MAC 位址，改為「（略）」。
- `WIRING.md`、`PARTS_LIST.md` 的發射觸發說明改成與程式碼一致。原文寫 Shuffleboard 虛擬按鈕，是 2026-05-17 的設計，2026-05-28 實機測試時已改成搖桿按鈕；`FINDINGS.md` 2026-11-01 條的備註同步更新。`PROGRESS.md` 2026-05-30 與 `FINDINGS.md` 2026-05-17 的原始寫法保留不改，以程式碼為準。
- 沒有收錄 `parts-images/`（多為改型前的舊零件圖）與建置產物。
- 新增本 README、`.gitignore` 與 `src/main/deploy/.gitkeep`。
- 整理時在本副本以 WPILib 2026 內附的 JDK 執行 `./gradlew build --offline`，建置成功，出現 9 個 REVLib 棄用警告，與 `CLAUDE.md` 的紀錄相同。
