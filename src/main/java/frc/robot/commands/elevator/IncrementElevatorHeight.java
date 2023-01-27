// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.elevator;

import java.util.function.DoubleSupplier;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Elevator;
import frc.robot.subsystems.Elevator.elevatorHeights;

public class IncrementElevatorHeight extends CommandBase {
  /** Creates a new IncrementElevatorHeight. */
  private DoubleSupplier m_joystickY;

  private Elevator m_elevator;

  public IncrementElevatorHeight(
    Elevator elevator, DoubleSupplier joystickY) {

    // Use addRequirements() here to declare subsystem dependencies.
    m_elevator = elevator;
    m_joystickY = joystickY;
    addRequirements(m_elevator);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    if (m_joystickY.getAsDouble() != 0) {
      Elevator.setElevatorDesiredHeightState(elevatorHeights.JOYSTICK);
    }

    Elevator.setElevatorJoystickY(m_joystickY);

    if (Elevator.getElevatorSimulated()) {
      Elevator.updateSimulatedElevatorHeight();
    } else {
      Elevator.updateElevatorHeight();
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
