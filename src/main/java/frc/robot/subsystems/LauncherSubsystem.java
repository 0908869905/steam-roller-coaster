package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.Launcher;

public class LauncherSubsystem extends SubsystemBase {

    private final SparkMax motor;
    private final SparkClosedLoopController pidController;
    private final RelativeEncoder encoder;

    private double targetRPM = 0;

    private double currentP = Launcher.kP;
    private double currentI = Launcher.kI;
    private double currentD = Launcher.kD;
    private double currentFF = Launcher.kFF;

    public LauncherSubsystem() {
        motor = new SparkMax(Launcher.MOTOR_CAN_ID, MotorType.kBrushless);

        SparkMaxConfig config = new SparkMaxConfig();
        config.inverted(true);
        config.closedLoop
            .pid(Launcher.kP, Launcher.kI, Launcher.kD)
            .velocityFF(Launcher.kFF);

        motor.configure(
            config,
            SparkBase.ResetMode.kResetSafeParameters,
            SparkBase.PersistMode.kPersistParameters
        );

        pidController = motor.getClosedLoopController();
        encoder = motor.getEncoder();

        SmartDashboard.putNumber("目標 RPM", Launcher.DEFAULT_TARGET_RPM);
        SmartDashboard.putNumber("kP", Launcher.kP);
        SmartDashboard.putNumber("kI", Launcher.kI);
        SmartDashboard.putNumber("kD", Launcher.kD);
        SmartDashboard.putNumber("kFF", Launcher.kFF);
        SmartDashboard.putString("馬達狀態", "就緒");
    }

    public void setTargetRPM(double rpm) {
        targetRPM = Math.abs(rpm);
        pidController.setReference(targetRPM, SparkBase.ControlType.kVelocity);
    }

    public void spinToDefault() {
        double dashboardRPM = SmartDashboard.getNumber("目標 RPM", Launcher.DEFAULT_TARGET_RPM);
        setTargetRPM(dashboardRPM);
    }

    public void stop() {
        targetRPM = 0;
        motor.stopMotor();
    }

    public boolean isAtSpeed() {
        return Math.abs(encoder.getVelocity() - targetRPM) < Launcher.RPM_TOLERANCE;
    }

    public double getCurrentRPM() {
        return encoder.getVelocity();
    }

    public double getTargetRPM() {
        return targetRPM;
    }

    @Override
    public void periodic() {
        double rpm = encoder.getVelocity();

        double newP = SmartDashboard.getNumber("kP", currentP);
        double newI = SmartDashboard.getNumber("kI", currentI);
        double newD = SmartDashboard.getNumber("kD", currentD);
        double newFF = SmartDashboard.getNumber("kFF", currentFF);

        if (newP != currentP || newI != currentI || newD != currentD || newFF != currentFF) {
            SparkMaxConfig pidConfig = new SparkMaxConfig();
            pidConfig.closedLoop.pid(newP, newI, newD).velocityFF(newFF);
            motor.configure(pidConfig,
                SparkBase.ResetMode.kNoResetSafeParameters,
                SparkBase.PersistMode.kNoPersistParameters);
            currentP = newP;
            currentI = newI;
            currentD = newD;
            currentFF = newFF;
        }

        SmartDashboard.putNumber("實際 RPM", rpm);
        if (targetRPM > 0) {
            SmartDashboard.putString("馬達狀態", isAtSpeed() ? "到位" : "加速中");
        } else {
            SmartDashboard.putString("馬達狀態", "停止");
        }
    }
}
