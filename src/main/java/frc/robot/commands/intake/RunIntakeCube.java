// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.intake;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Intake;

public class RunIntakeCube extends CommandBase {
  @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
  private final Intake m_intake;

  private final double m_PercentOutput;

  /** Creates a new RunIntake. */
  public RunIntakeCube(Intake intake, double PercentOutput) {
    m_intake = intake;
    m_PercentOutput = PercentOutput;

    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(m_intake);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_intake.setIntakeStateCube(true);
    m_intake.setIntakeStateCube(true);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    m_intake.setPercentOutput(m_PercentOutput);
    // if (m_vision.searchLimelightTarget(CAMERA_SERVER.INTAKE)) {
    //   m_swerve.enableHeadingTarget(true);
    //   m_swerve.setRobotHeading(
    //       m_swerve
    //           .getHeadingRotation2d()
    //           .minus(Rotation2d.fromDegrees(m_vision.getTargetXAngle(CAMERA_SERVER.INTAKE)))
    //           .getRadians());
    // }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_intake.setPercentOutput(0);
    m_intake.setIntakeStateCube(false);
    // m_swerve.enableHeadingTarget(false);
    m_intake.setIntakeStateCube(false);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
