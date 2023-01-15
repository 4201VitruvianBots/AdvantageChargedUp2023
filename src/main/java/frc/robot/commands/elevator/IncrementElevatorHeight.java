// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

// Called when the joystick moves up/down, also acts as manual override
package frc.robot.commands.elevator;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Elevator;
import frc.robot.subsystems.Elevator.elevatorHeights;

public class IncrementElevatorHeight extends CommandBase {
  /** Creates a new IncrementElevatorHeight. */

  private elevatorHeights heightEnum;
  private double joystickY;

  public IncrementElevatorHeight(elevatorHeights heightEnum, double joystickY) {
    // Use addRequirements() here to declare subsystem dependencies.
    this.heightEnum = heightEnum;
    this.joystickY = joystickY;

    addRequirements();
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    double distanceBetween = Elevator.moveToElevatorHeight(this.heightEnum, this.joystickY);
    // TODO: Add function to determine motor output based off of how far away our targeted height is
    if (distanceBetween < 0) {
      Elevator.setElevatorPercentOutput(0.8);
    }
    else if (distanceBetween > 0) {
      Elevator.setElevatorPercentOutput(-0.8);
    }
    Elevator.updateElevatorHeight();
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
