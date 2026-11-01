# steam教案 - 錯誤記錄

此檔案記錄本專案特有的錯誤（通用錯誤請記錄到 `~/.claude/ERROR_LOG.md`）

---

## 2026-05-28: CAN ID 0 馬達不動（無 timeout）

### 症狀
程式設定 CAN ID 0，不會報 CAN timeout 錯誤，但馬達完全不動

### 原因
SparkMax 實際 CAN ID 是 1（已被修改過），CAN ID 0 上無裝置但不會 timeout（CAN 協議特性）

### 解決
Constants.java 中 `LAUNCHER_MOTOR_ID` 改為 1

### 預防
- 新拿到的 SparkMax 先用 REV Hardware Client 確認 CAN ID
- 馬達不動時優先排查 CAN ID 是否正確

---

## 2026-05-28: SparkMax CAN 間歇性 Timeout

### 症狀
馬達時動時不動，console 報 CAN timeout 錯誤

### 原因
SparkMax CAN JST 接頭鬆脫，接觸不良

### 解決
確實壓緊 JST 接頭至卡榫鎖定

### 預防
- 測試前檢查所有 CAN JST 連接
- 正式部署用束帶或熱縮管固定

---

## 2026-05-28: NEO 550 PID 震盪 ±1000 RPM

### 症狀
設定目標 RPM 後馬達轉速劇烈波動（±1000 RPM），無法穩定

### 原因
kP=0.0004 對 NEO 550（MAX_RPM=11000）而言增益太大，造成過衝震盪

### 解決
kP 降至 0.0001，震盪消除

### 預防
- NEO 550 的 PID kP 起始值建議從 0.0001 開始
- 使用 Shuffleboard 即時調校面板觀察響應再逐步調高

---

## 2026-05-28: RioLog 中文亂碼

### 症狀
RioLog（Driver Station 終端機）中文字顯示為亂碼

### 原因
RoboRIO JVM 執行時預設編碼非 UTF-8

### 解決
build.gradle 新增 JVM 執行時參數：
```
-Dfile.encoding=UTF-8
-Dstdout.encoding=UTF-8
```

### 預防
WPILib 專案若有中文輸出，除了 javac 的 `options.encoding = 'UTF-8'`，還需要 JVM 執行時 encoding 參數

---

## 2026-05-28: REV Hardware Client 無法偵測 SparkMax（USB-C）

### 症狀
REV Hardware Client 1.7.5 透過 USB-C 連接 SparkMax，軟體無反應

### 原因
未確定。可能是 USB-C 線僅充電（無資料線）、驅動問題、或需 CAN 供電

### 解決
尚未解決。暫不影響開發（CAN 通訊正常）

### 待追蹤
- 嘗試換有資料功能的 USB-C 線
- 確認 SparkMax USB 通訊是否需要同時有 CAN 供電

---

## 2026-05-29: 中文路徑 gradlew.bat ClassNotFoundException

### 症狀
在含中文字元的 Windows 路徑（如 `D:\My project\steam教案`）下執行 `gradlew.bat`，報 `ClassNotFoundException`，Gradle wrapper 完全無法啟動

### 原因
gradlew.bat 的 classpath 拼接無法正確處理非 ASCII 字元路徑。JVM 啟動時收到損壞的 classpath，找不到 Gradle wrapper main class

### 解決
將專案複製到純英文路徑下（如 `C:\Users\xxx\Downloads\steam`），確保從根到葉的每一層路徑都不含中文

### 預防
- 所有 WPILib / Gradle 專案一律放在純英文路徑
- Windows 使用者名稱如果是中文，需改用其他磁碟或建立英文名稱的使用者

---

## 2026-05-16: Gradle Build 中文亂碼（Encoding）

### 症狀
`./gradlew build` 編譯時，Java 原始碼中的中文字串（SmartDashboard key）報 `unmappable character for encoding`

### 原因
Windows 預設 codepage 是 MS950/Big5，Gradle 的 javac task 預設跟隨系統編碼，但 .java 檔案是 UTF-8

### 解決
在 `build.gradle` 中加入：
```groovy
tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}
```

### 預防
所有 WPILib 專案只要有中文字串都需要這個設定

---

## 2026-05-16: SSL PKIX Path Building Failed

### 症狀
```
Could not resolve all files for configuration ':compileClasspath'.
PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException
```

### 原因
Gradle JVM 不信任 Windows 系統憑證存放區的 CA 根憑證

### 解決
`gradle.properties`:
```properties
systemProp.javax.net.ssl.trustStore=NUL
systemProp.javax.net.ssl.trustStoreType=Windows-ROOT
```

### 備註
此為 Windows + 學校網路環境特有問題

---

## 2026-05-16: Robot.java 缺少 main method

### 症狀
Build 成功但 deploy 或 simulate 時報 `Main method not found in class frc.robot.Main`

### 原因
WPILib 2026 專案需要 `Robot.java` 中有 `main` method 呼叫 `RobotBase.startRobot(Robot::new)`（或有獨立 Main.java）

### 解決
在 `Robot.java` 加入：
```java
public static void main(String[] args) {
    RobotBase.startRobot(Robot::new);
}
```

---
