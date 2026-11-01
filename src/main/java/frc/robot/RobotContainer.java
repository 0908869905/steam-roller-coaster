package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.commands.LaunchCommand;
import frc.robot.subsystems.LauncherSubsystem;
import frc.robot.subsystems.SensorSubsystem;

public class RobotContainer {

    private final LauncherSubsystem launcher = new LauncherSubsystem();
    private final SensorSubsystem sensors = new SensorSubsystem();

    private final Joystick joystick = new Joystick(Constants.OI.JOYSTICK_PORT);

    public RobotContainer() {
        configureBindings();
        SmartDashboard.putString("系統狀態", "就緒");
        SmartDashboard.putString("發射狀態", "待機");
    }

    private void configureBindings() {
        new JoystickButton(joystick, Constants.OI.LAUNCH_BUTTON)
            .onTrue(new LaunchCommand(launcher, sensors));

        new JoystickButton(joystick, Constants.OI.SPIN_BUTTON)
            .whileTrue(Commands.runEnd(
                () -> launcher.spinToDefault(),
                () -> launcher.stop(),
                launcher
            ));
    }
}
