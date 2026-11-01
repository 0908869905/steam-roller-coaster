package frc.robot.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.LauncherSubsystem;
import frc.robot.subsystems.SensorSubsystem;

public class LaunchCommand extends Command {

    private final LauncherSubsystem launcher;
    private final SensorSubsystem sensors;
    private double timeoutTimestamp;

    private static final double TIMEOUT_SECONDS = 5.0;

    public LaunchCommand(LauncherSubsystem launcher, SensorSubsystem sensors) {
        this.launcher = launcher;
        this.sensors = sensors;
        addRequirements(launcher, sensors);
    }

    @Override
    public void initialize() {
        sensors.reset();
        launcher.spinToDefault();
        timeoutTimestamp = Timer.getFPGATimestamp() + TIMEOUT_SECONDS;
        SmartDashboard.putString("發射狀態", String.format("發射中 (目標 %.0f RPM)", launcher.getTargetRPM()));
    }

    @Override
    public void execute() {
    }

    @Override
    public boolean isFinished() {
        if (sensors.isRunComplete()) return true;
        return Timer.getFPGATimestamp() > timeoutTimestamp;
    }

    @Override
    public void end(boolean interrupted) {
        launcher.stop();
        if (interrupted) {
            SmartDashboard.putString("發射狀態", "中斷");
        } else if (sensors.isRunComplete()) {
            double[] speeds = sensors.getAllSpeeds();
            SmartDashboard.putString("發射狀態",
                String.format("完成 — 入口:%.2f 90°:%.2f 頂:%.2f 270°:%.2f m/s",
                    speeds[0], speeds[1], speeds[2], speeds[3]));
        } else {
            SmartDashboard.putString("發射狀態", "逾時 (" + TIMEOUT_SECONDS + "s)");
        }
    }
}
