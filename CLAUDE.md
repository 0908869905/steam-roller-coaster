# steam教案 — 球球過山車（鉛直圓周運動）

## 專案概述
- **類型**：STEAM 教育教案 + FRC 程式控制
- **負責範圍**：程式 + 電控（接線），只有機構和馬達不用管
- **技術棧**：WPILib Java, REVLib, RoboRIO 1, SparkMax/NEO 550, PDP

## 目錄結構
```
build.gradle                # GradleRIO 2026.2.1 + UTF-8 設定
settings.gradle             # 專案名稱
gradle.properties           # SSL truststore 修復（Windows）
gradlew / gradlew.bat       # Gradle wrapper scripts
gradle/wrapper/             # Gradle 8.11.1 wrapper
vendordeps/REVLib.json      # REVLib 2026.0.5
.wpilib/wpilib_preferences.json  # 隊號 6998

src/main/java/frc/robot/
├── Robot.java              # 進入點（含 main method）
├── RobotContainer.java     # 搖桿按鈕綁定 + subsystem 初始化
├── Constants.java          # 所有常數（DIO pin, PID, 物理量）
├── subsystems/
│   ├── LauncherSubsystem.java   # NEO 550 + SparkMax 速度控制
│   └── SensorSubsystem.java     # Flying Fish IR 反射式 sensor 中斷計時 + 速度計算
└── commands/
    └── LaunchCommand.java       # 發射流程

src/main/deploy/            # RoboRIO 部署用檔案（目前為空）
parts-images/               # 零件參考圖片（多為改型前舊零件：NEO Vortex、SparkFlex、對射式 IR、PDH；僅 RoboRIO 為現用），分享時不要附
WIRING.md                   # 接線表
PARTS_LIST.md               # 零件清單
```

## 常用指令
```bash
# 建置（在 WPILib VS Code 中）
./gradlew build

# 部署到 RoboRIO
./gradlew deploy

# 模擬器測試
./gradlew simulateJava
```

## 開發規範
- Command-Based 架構
- SmartDashboard/Shuffleboard 用中文 key（給活動現場看）
- 發射由 Driver Station 的 USB 搖桿觸發（Joystick port 0，按鈕 1 = 執行 LaunchCommand），RoboRIO 端無實體按鈕
- 搖桿按鈕 2 = 按住持續轉動（whileTrue，PID 調校用途）
- IR sensor（Flying Fish 反射式）用 AsynchronousInterrupt + FPGA timestamp（微秒級精度）
- IR sensor 測速方式：單顆 sensor 球直徑法（BALL_DIAMETER / 通過時間），falling + rising edge
- Shuffleboard 參數面板：目標 RPM, kP, kI, kD, kFF 可即時調校
- PID 已調校值：kP=0.0001（NEO 550 穩定值，已寫入 Constants.java）
- 馬達方向：inverted(true)（NEO 550 安裝方向需反轉）

## 建置環境
- **GradleRIO**: 2026.2.1
- **Gradle**: 8.11.1（wrapper）
- **REVLib**: 2026.0.5（有 9 個 deprecation warnings，不影響功能）
- **Java**: WPILib bundled JDK
- **SSL**: gradle.properties 設定信任 Windows 憑證存放區（解決學校網路 PKIX 問題）
- **JVM UTF-8**: build.gradle 設定 -Dfile.encoding=UTF-8 等（RioLog 中文顯示）
- **NI FRC Game Tools**: 已安裝（roboRIO Imaging Tool）

## RoboRIO 硬體資訊
- **型號**：roboRIO-2101-FRC（RoboRIO 1 代）
- **映像**：FRC_roboRIO_2026_v1.2
- **韌體**：6.0.0f1
- **MAC**：（略）
- **隊號**：6998
- **JRE**：roborio-2024-17.0.9u7-3（首次 deploy 時自動安裝）

## 電源系統架構
```
電池 12V → 主 breaker (120A) → PDP
  PDP 20A slot → 18AWG 自製線 → RoboRIO 1 (綠色 Weidmuller 端子)
  PDP 30A slot → SparkMax (NEO 550)
```
- **配電板**：PDP（不用 PDH，成本考量）
- **RoboRIO**：第一代（綠色 Weidmuller 端子台），非 RoboRIO 2
- **PDP→RoboRIO 電源線**：需自製（18AWG 紅黑線），非隨附
- **CAN Bus**：SparkMax JST-to-裸線 → RoboRIO CAN H/L 端子

## 輸出方式
- **所有輸出皆透過 Shuffleboard（SmartDashboard）**，完全不使用 System.out
- 馬達區：目標 RPM、實際 RPM、馬達狀態（停止/加速中/到位）、kP/kI/kD/kFF
- Sensor 區：底部入口/90度/頂部/270度 速度 (m/s)、通過頂部、完成一圈、最新偵測
- 發射區：發射狀態（發射中/完成/中斷/逾時）
- 系統區：系統狀態

## 馬達控制
- **無 RPM 上限限制**（setTargetRPM 不做 Math.min clamp）
- **無電流限制**（已移除 smartCurrentLimit）
- 馬達需設定 inverted(true)
- PID 已調校值：kP=0.0001（已寫入 Constants.java）
- testSpin() 診斷方法已移除（不再需要）

## 注意事項
- Flying Fish IR sensor 是 active low：偵測到反射物 = LOW = falling edge
- Flying Fish 反射式需注意：sensor 與球面距離、環境光干擾、白色乒乓球反射率
- NEO 550 MAX_RPM = 11000，比 NEO Vortex (5000) 高很多，PID 參數需重新調校
- NEO 550 PID kP 起始值建議 0.0001（0.0004 會震盪）
- SparkMax CAN ID 是 1（非出廠預設 0），出廠預設已被修改
- SparkMax CAN JST 接頭容易鬆脫，需確實壓緊卡榫
- 乒乓球很輕（2.7g），滾輪壓縮量不用太大
- 乒乓球直徑 4cm（BALL_DIAMETER=0.04），用於 sensor 測速計算
- 發射與持續轉動都綁在搖桿按鈕上（RobotContainer.java），LaunchCommand 結束時會停馬達，沒有任何 Shuffleboard 按鈕 key
- build.gradle 已設定 `options.encoding = 'UTF-8'`（中文 SmartDashboard key 必需）
- build.gradle 已設定 JVM `-Dfile.encoding=UTF-8`（RioLog 中文輸出必需）
- REV Hardware Client 1.7.5 USB-C 連線 SparkMax 有問題（待排查）
- **Windows 中文路徑**：gradlew.bat 無法在含中文字元的路徑下運行，需使用純英文路徑
- 在 VS Code 以外的終端機跑 gradlew 前要確認用的是 WPILib 內附 JDK 17（`C:\Users\Public\wpilib\2026\jdk`），系統若裝了 JDK 25 之類較新版本，Gradle 8.11 會報 `Type T not present`
