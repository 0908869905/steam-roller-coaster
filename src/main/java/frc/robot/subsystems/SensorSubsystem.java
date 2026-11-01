package frc.robot.subsystems;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.AsynchronousInterrupt;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.Sensors;

public class SensorSubsystem extends SubsystemBase {

    private final DigitalInput[] irSensors = new DigitalInput[Sensors.SENSOR_COUNT];
    private final AsynchronousInterrupt[] interrupts = new AsynchronousInterrupt[Sensors.SENSOR_COUNT];

    private final double[] entryTimes = new double[Sensors.SENSOR_COUNT];
    private final double[] sensorSpeeds = new double[Sensors.SENSOR_COUNT];
    private final boolean[] sensorCompleted = new boolean[Sensors.SENSOR_COUNT];

    private boolean runComplete = false;
    private boolean ballDetectedAtTop = false;

    private static final String[] SENSOR_NAMES = {"底部入口", "90度", "頂部", "270度"};

    public SensorSubsystem() {
        int[] ports = {Sensors.IR_BOTTOM, Sensors.IR_RIGHT, Sensors.IR_TOP, Sensors.IR_LEFT};

        for (int i = 0; i < Sensors.SENSOR_COUNT; i++) {
            irSensors[i] = new DigitalInput(ports[i]);
            final int index = i;

            interrupts[i] = new AsynchronousInterrupt(irSensors[i], (rising, falling) -> {
                if (falling) {
                    onBallEnter(index);
                } else if (rising) {
                    onBallExit(index);
                }
            });
            interrupts[i].setInterruptEdges(true, true);
            interrupts[i].enable();
        }

        for (String name : SENSOR_NAMES) {
            SmartDashboard.putNumber(name + " 速度 (m/s)", 0);
        }
        SmartDashboard.putBoolean("通過頂部", false);
        SmartDashboard.putBoolean("完成一圈", false);
        SmartDashboard.putString("最新偵測", "待機");
    }

    private synchronized void onBallEnter(int sensorIndex) {
        entryTimes[sensorIndex] = Timer.getFPGATimestamp();

        if (sensorIndex == 2) {
            ballDetectedAtTop = true;
        }
    }

    private synchronized void onBallExit(int sensorIndex) {
        if (entryTimes[sensorIndex] <= 0) return;

        double dt = Timer.getFPGATimestamp() - entryTimes[sensorIndex];
        if (dt > 0 && dt < 1.0) {
            sensorSpeeds[sensorIndex] = Sensors.BALL_DIAMETER / dt;
            sensorCompleted[sensorIndex] = true;
        }

        boolean allDone = true;
        for (boolean c : sensorCompleted) {
            if (!c) { allDone = false; break; }
        }
        if (allDone) {
            runComplete = true;
        }
    }

    public synchronized void reset() {
        for (int i = 0; i < Sensors.SENSOR_COUNT; i++) {
            entryTimes[i] = 0;
            sensorSpeeds[i] = 0;
            sensorCompleted[i] = false;
        }
        runComplete = false;
        ballDetectedAtTop = false;
    }

    public synchronized boolean isRunComplete() {
        return runComplete;
    }

    public synchronized boolean didBallPassTop() {
        return ballDetectedAtTop;
    }

    public synchronized double getEntrySpeed() {
        return sensorSpeeds[0];
    }

    public synchronized double[] getAllSpeeds() {
        return sensorSpeeds.clone();
    }

    @Override
    public void periodic() {
        synchronized (this) {
            for (int i = 0; i < Sensors.SENSOR_COUNT; i++) {
                SmartDashboard.putNumber(SENSOR_NAMES[i] + " 速度 (m/s)", sensorSpeeds[i]);
            }
            SmartDashboard.putBoolean("通過頂部", ballDetectedAtTop);
            SmartDashboard.putBoolean("完成一圈", runComplete);

            String latest = "待機";
            for (int i = Sensors.SENSOR_COUNT - 1; i >= 0; i--) {
                if (sensorCompleted[i]) {
                    latest = SENSOR_NAMES[i] + String.format(" %.2f m/s", sensorSpeeds[i]);
                    break;
                }
            }
            // 如果有球進入但還沒離開，顯示正在偵測
            for (int i = 0; i < Sensors.SENSOR_COUNT; i++) {
                if (entryTimes[i] > 0 && !sensorCompleted[i]) {
                    latest = SENSOR_NAMES[i] + " 偵測中...";
                    break;
                }
            }
            SmartDashboard.putString("最新偵測", latest);
        }
    }
}
