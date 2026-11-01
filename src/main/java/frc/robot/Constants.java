package frc.robot;

public final class Constants {

    public static final class OI {
        public static final int JOYSTICK_PORT = 0;
        public static final int LAUNCH_BUTTON = 1;
        public static final int SPIN_BUTTON = 2;
    }

    public static final class Launcher {
        public static final int MOTOR_CAN_ID = 1;
        public static final double DEFAULT_TARGET_RPM = 3000;
        public static final double MAX_RPM = 11000;

        // PID for NEO 550 velocity control (tune on real hardware)
        public static final double kP = 0.0001;
        public static final double kI = 0.0;
        public static final double kD = 0.0;
        public static final double kFF = 0.000090;

        // RPM tolerance to consider "ready"
        public static final double RPM_TOLERANCE = 100;
    }

    public static final class Sensors {
        // DIO ports for IR break-beam sensors
        public static final int IR_BOTTOM = 0;   // 0° 底部入口
        public static final int IR_RIGHT = 1;    // 90°
        public static final int IR_TOP = 2;      // 180° 頂部（制高點）
        public static final int IR_LEFT = 3;     // 270°

        // Physical: arc length between adjacent sensors (m)
        // = π × 0.30 / 4 = 0.2356 m
        public static final double ARC_LENGTH_BETWEEN_SENSORS = Math.PI * 0.30 / 4.0;

        public static final int SENSOR_COUNT = 4;
        public static final double BALL_DIAMETER = 0.04; // 乒乓球直徑 4cm
    }
}
