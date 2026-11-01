# 接線表 — 球球過山車

## RoboRIO DIO 接線

| DIO Port | 元件 | 位置 | 接線 |
|----------|------|------|------|
| DIO 0 | Flying Fish IR 反射式 #0 | 圓形軌道底部（入口） | OUT → DIO 0, VCC → 5V, GND → GND |
| DIO 1 | Flying Fish IR 反射式 #1 | 圓形軌道 90° | OUT → DIO 1, VCC → 5V, GND → GND |
| DIO 2 | Flying Fish IR 反射式 #2 | 圓形軌道頂部（制高點） | OUT → DIO 2, VCC → 5V, GND → GND |
| DIO 3 | Flying Fish IR 反射式 #3 | 圓形軌道 270° | OUT → DIO 3, VCC → 5V, GND → GND |

> 發射由 Driver Station 的 USB 搖桿（port 0）按鈕 1 觸發、按鈕 2 按住持續轉；roboRIO 端無實體按鈕。

## CAN Bus 接線

| CAN ID | 元件 | 用途 |
|--------|------|------|
| 1 | SparkMax (NEO 550) | 滾輪馬達 |

## 電源

| 元件 | 電源來源 |
|------|---------|
| RoboRIO | PDP/PDH 直連 |
| SparkMax | PDP/PDH 30A breaker |
| IR Sensors ×4 | RoboRIO 5V rail（每顆 <20mA，總共 <100mA 沒問題） |

## Flying Fish IR 反射式安裝注意

```
軌道截面圖：

    ┌─────────────┐ ← 雷切軌道板
    │  ● 乒乓球   │
    │             │
    └─────────────┘
         ↑
    [Flying Fish] ← 裝在軌道單側，朝向球經過的路徑
         ↑ 球經過時反射 IR → 輸出 LOW

- 只需要裝在軌道單側（不用對射，安裝更簡單）
- 感測距離用模組上的可調電位器調整（建議 1-3cm）
- 用熱熔膠或小支架固定在軌道外側，開一個小孔讓 IR 照到軌道內
- 乒乓球白色表面反射率高，偵測效果好
```

## 操作介面

```
Shuffleboard / SmartDashboard 畫面：

┌────────────────────────────────────┐
│  發射狀態: 待機（發射用搖桿按鈕 1）│
│                                    │
│  目標 RPM: [3000]  目前 RPM: 2987  │
│  轉速到位: ✔                        │
│  入口速度: 3.21 m/s                 │
│  通過頂部: ✔    完成一圈: ✔          │
└────────────────────────────────────┘
```
