// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.led;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants.STATEHANDLER.SUPERSTRUCTURE_STATE;
import frc.robot.subsystems.*;

/*scoring = flashing white, intakingcube = blue,
intakingcone = orange, locked on = flashing green,
enable = green, disabled = red,
cubebutton = purple, conebutton = yellow */

/** Sets the LED based on the subsystems' statuses */
public class GetSubsystemStates extends CommandBase {
  @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
  private final LEDSubsystem m_led;
  private final StateHandler m_stateHandler;
  /** Sets the LED based on the subsystems' statuses */
  public GetSubsystemStates(
      LEDSubsystem led, StateHandler stateHandler) {
    m_led = led;
    m_stateHandler = stateHandler;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(led);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}
  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // the prioritized state to be expressed to the LEDs
    // set in order of priority to be expressed from the least priority to the
    // highest priority 
    if (DriverStation.isDisabled()) {
        m_led.expressState(SUPERSTRUCTURE_STATE.DISABLED);
    } else {
      switch (m_stateHandler.getDesiredZone()) {
            case LOW_ZONE:
            case INTAKE_LOW:
            case SCORE_LOW_REVERSE: 
            case SCORE_LOW:
            case SCORE_LOW_CONE:
            case SCORE_LOW_CUBE:
                m_led.expressState(SUPERSTRUCTURE_STATE.LOW_ZONE); // Solid Orange
              break;
            case MID_ZONE:
            case SCORE_MID_CONE:
            case SCORE_MID_CUBE:
            case SCORE_MID:
                m_led.expressState(SUPERSTRUCTURE_STATE.MID_ZONE);; // Solid White
              break;
            case HIGH_ZONE:
                m_led.expressState(SUPERSTRUCTURE_STATE.HIGH_ZONE);; // Solid Pink
              break;
            case EXTENDED_ZONE:
            case INTAKE_EXTENDED:
            case SCORE_HIGH:
            case SCORE_HIGH_CONE:
            case SCORE_HIGH_CUBE:
                m_led.expressState(SUPERSTRUCTURE_STATE.EXTENDED_ZONE);; // Solid White
              break;
              default:
                m_led.expressState(SUPERSTRUCTURE_STATE.ENABLED);
              break;
      }
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

  @Override
  public boolean runsWhenDisabled() {
    return true;
  }
}

