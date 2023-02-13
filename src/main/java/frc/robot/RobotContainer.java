// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Command.InterruptionBehavior;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.commands.Intake.RunIntake;
import frc.robot.commands.Intake.RunReverseIntake;
import frc.robot.commands.auto.BlueTopConeCubeBalance;
import frc.robot.commands.auto.DriveForward;
import frc.robot.commands.auto.DriveSideway;
import frc.robot.commands.elevator.IncrementElevatorHeight;
import frc.robot.commands.elevator.MoveToElevatorHeight;
import frc.robot.commands.elevator.SetElevatorControlLoop;
import frc.robot.commands.led.SetPieceTypeIntent;
import frc.robot.commands.swerve.ResetOdometry;
import frc.robot.commands.swerve.SetSwerveCoastMode;
import frc.robot.commands.swerve.SetSwerveDrive;
import frc.robot.commands.swerve.SetSwerveDriveBalance;
import frc.robot.simulation.FieldSim;
import frc.robot.simulation.MemoryLog;
import frc.robot.subsystems.Controls;
// import frc.robot.subsystems.DistanceSensor;
import frc.robot.subsystems.Elevator;
import frc.robot.subsystems.Elevator.elevatorHeights;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.LED;
import frc.robot.subsystems.LED.PieceType;
import frc.robot.subsystems.SwerveDrive;
import frc.robot.subsystems.Vision;
import frc.robot.subsystems.Wrist;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {
  private final DataLog m_logger = DataLogManager.getLog();

  // The robot's subsystems and commands are defined here...4
  private final Intake m_intake = new Intake();
  private final Elevator m_elevator = new Elevator();
  private final SwerveDrive m_swerveDrive = new SwerveDrive();
  private final Vision m_vision = new Vision(m_swerveDrive, m_logger);
  private final FieldSim m_fieldSim = new FieldSim(m_swerveDrive, m_vision);
  private final SendableChooser<Command> m_autoChooser = new SendableChooser<Command>();
  private final Controls m_controls = new Controls();
  private final Wrist m_wrist = new Wrist();
  private final LED m_led = new LED(m_controls);
  // private final DistanceSensor m_distanceSensor = new DistanceSensor();

  // Replace with CommandPS4Controller or CommandJoystick if needed
  private final CommandXboxController m_driverController =
      new CommandXboxController(
          frc.robot.Constants.constants.OperatorConstants.kDriverControllerPort);

  private final MemoryLog m_memorylog = new MemoryLog();

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  static Joystick leftJoystick = new Joystick(Constants.constants.USB.leftJoystick);

  static Joystick rightJoystick = new Joystick(Constants.constants.USB.rightJoystick);
  static XboxController xBoxController = new XboxController(Constants.constants.USB.xBoxController);

  public Trigger[] leftTriggers = new Trigger[2];
  public Trigger[] rightTriggers = new Trigger[2];
  public Trigger[] xBoxTriggers = new Trigger[10];
  public Trigger[] xBoxPOVTriggers = new Trigger[4];

  public Trigger xBoxLeftTrigger, xBoxRightTrigger;

  public void initializeSubsystems() {
    m_swerveDrive.setDefaultCommand(
        new SetSwerveDrive(
            m_swerveDrive,
            () -> leftJoystick.getRawAxis(1),
            () -> leftJoystick.getRawAxis(0),
            () -> rightJoystick.getRawAxis(0)));

    // Control elevator height by moving the joystick up and down
    m_elevator.setDefaultCommand(
        new IncrementElevatorHeight(m_elevator, () -> xBoxController.getLeftY()));
    m_fieldSim.initSim();
  }

  public RobotContainer() {
    initializeSubsystems();
    initializeAutoChooser();

    // Configure the button bindings
    configureBindings();

    // Choose which constants class to use
    chooseConstants();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureBindings() { // TODO: Replace Joystick Button?
    for (int i = 0; i < leftTriggers.length; i++)
      leftTriggers[i] = new JoystickButton(leftJoystick, (i + 1));
    for (int i = 0; i < rightTriggers.length; i++)
      rightTriggers[i] = new JoystickButton(rightJoystick, (i + 1));
    for (int i = 0; i < xBoxTriggers.length; i++)
      xBoxTriggers[i] = new JoystickButton(xBoxController, (i + 1));
    for (int i = 0; i < xBoxPOVTriggers.length; i++)
      xBoxPOVTriggers[i] = new POVButton(xBoxController, (i * 90));

    xBoxTriggers[5].whileTrue(new SetPieceTypeIntent(m_led, PieceType.CONE));
    xBoxTriggers[5].whileTrue(new SetPieceTypeIntent(m_led, PieceType.CONE));

    xBoxLeftTrigger =
        new Trigger(
            () -> xBoxController.getLeftTriggerAxis() > 0.1); // getTrigger());// getRawAxis(2));
    xBoxRightTrigger = new Trigger(() -> xBoxController.getRightTriggerAxis() > 0.1);
    xBoxLeftTrigger.whileTrue(new RunIntake(m_intake));
    xBoxRightTrigger.whileTrue(new RunReverseIntake(m_intake));

    // Elevator button bindings
    m_driverController.a().whileTrue(new MoveToElevatorHeight(m_elevator, elevatorHeights.LOW));
    m_driverController.b().whileTrue(new MoveToElevatorHeight(m_elevator, elevatorHeights.MID));
    m_driverController.x().whileTrue(new MoveToElevatorHeight(m_elevator, elevatorHeights.STOWED));
    m_driverController.y().whileTrue(new MoveToElevatorHeight(m_elevator, elevatorHeights.HIGH));

    // Will switch behtween closed and open loop on button press
    m_driverController.start().whileTrue(new SetElevatorControlLoop(m_elevator));

    leftTriggers[0].whileTrue(
        new SetSwerveDriveBalance(m_swerveDrive, null, null, null)
            .withInterruptBehavior(InterruptionBehavior.kCancelIncoming));

    SmartDashboard.putData(new ResetOdometry(m_swerveDrive));
    SmartDashboard.putData(new SetSwerveCoastMode(m_swerveDrive));
  }

  public void disableInit() {
    m_swerveDrive.setNeutralMode(NeutralMode.Coast);
  }

  public void teleopeInit() {
    m_swerveDrive.setNeutralMode(NeutralMode.Brake);
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public void initializeAutoChooser() {
    m_autoChooser.setDefaultOption("Do Nothing", new WaitCommand(0));
    //   m_autoChooser.addOption("MiddleOneConeBalance", new
    // RedMiddleOneConeBalance(m_swerveDrive, m_fieldSim));

    // m_autoChooser.addOption("DriveSideway2", new DriveSideway2(m_swerveDrive, m_fieldSim));
    m_autoChooser.addOption(
        "BlueTopConeCubeBalance", new BlueTopConeCubeBalance(m_swerveDrive, m_fieldSim));
    m_autoChooser.addOption("DriveSideway", new DriveSideway(m_swerveDrive, m_fieldSim));
    m_autoChooser.addOption("DriveForward", new DriveForward(m_swerveDrive, m_fieldSim));
    // m_autoChooser.addOption("DriveTest", new DriveTest(m_swerveDrive, m_fieldSim));
    SmartDashboard.putData("Auto Selector", m_autoChooser);
  }

  // Switches between constants class depending on the MAC address of the roboRIO we're running on
  public void chooseConstants() {
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    String mac = inst.getTable("RIO-Info").getEntry("MAC").getString("N/A");
    if (mac == Constants.alphaRobotMAC) {
      Constants.constants = new ConstantsAlpha();
    } else if (mac == Constants.betaRobotMAC) {
      Constants.constants = new ConstantsBeta();
    }
  }

  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    return m_autoChooser.getSelected();
  }

  public void simulationPeriodic() {
    m_elevator.simulationPeriodic();
    m_memorylog.simulationPeriodic();
  }

  public void periodic() {
    m_fieldSim.periodic();
  }
}
