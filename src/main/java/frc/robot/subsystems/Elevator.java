// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Elevator extends SubsystemBase {

  // Initializing both motors
  public static final TalonFX[] elevatorMotors = {
    new TalonFX(Constants.Elevator.elevatorMotorLeft), new TalonFX(Constants.Elevator.elevatorMotorRight)
  };
  
  // Used by RobotContainer to specify which button has been pressed
  public enum elevatorHeights {
    LOW,
    MID,
    HIGH,
    JOYSTICK
  }
  
  // Limit switch at bottom of elevator
  private static DigitalInput elevatorLowerSwitch = new DigitalInput(Constants.Elevator.elevatorLowerSwitch);

  private static double desiredHeightValue; // The height in encoder units our robot is trying to reach
  private static elevatorHeights desiredHeightState = elevatorHeights.LOW; // Think of this as our "next state" in our state machine.

  private static double elevatorJoystickY;

  private final double kF = 0; // Only F and P control is needed for Elevator
  private final double kP = 0.2;

  private static boolean elevatorClimbState;

  private static double elevatorHeight = 0; // the amount of rotations the motor has gone up from the initial low position

  // Simulation setup

  private final ElevatorSim elevatorSim = new ElevatorSim(
    Constants.Elevator.elevatorGearbox,
    Constants.Elevator.elevatorGearing,
    Constants.Elevator.elevatorMassKg,
    Constants.Elevator.elevatorDrumRadiusMeters,
    Constants.Elevator.elevatorMinHeightMeters,
    Constants.Elevator.elevatorMaxHeightMeters,
    true,
    VecBuilder.fill(0.01)
  );

  // Shuffleboard setup

  public static ShuffleboardTab elevatorTab = Shuffleboard.getTab("Elevator");
  
  public static GenericEntry elevatorClimbingTab = elevatorTab.add("Elevator Climbing", false).getEntry();
  public static GenericEntry elevatorHeightTab = elevatorTab.add("Elevator Height", 0.0).getEntry();
  public static GenericEntry elevatorTargetHeightTab = elevatorTab.add("Elevator Target Height", desiredHeightValue).getEntry();
  public static GenericEntry elevatorTargetPosTab = elevatorTab.add("Elevator Target Position", desiredHeightState.name()).getEntry();
  public static GenericEntry elevatorRawPerOutTab = elevatorTab.add("Elevator Raw Percent Output", 0.0).getEntry();
  public static GenericEntry elevatorPerOutTab = elevatorTab.add("Elevator Percent Output", "0%").getEntry();

  /* Constructs a new Elevator. Mostly motor setup */
  public Elevator() {
    for(TalonFX motor : elevatorMotors){
      motor.configFactoryDefault();
      motor.setNeutralMode(NeutralMode.Brake);
      motor.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
      motor.setSelectedSensorPosition(elevatorHeight);
    }
    
    elevatorMotors[0].setInverted(true);
    elevatorMotors[1].setInverted(true);

    elevatorMotors[1].set(TalonFXControlMode.Follower, elevatorMotors[0].getDeviceID());

    elevatorMotors[0].config_kF(0, kF);
    elevatorMotors[0].config_kP(0, kP);

    Shuffleboard.selectTab("Elevator");
  }

  public static boolean getElevatorClimbState() {
    return elevatorClimbState;
  }
  public static void setElevatorClimbState(boolean climbState) {
    elevatorClimbState = climbState;
  }


  public static double getElevatorPercentOutput() {
    return elevatorMotors[0].getMotorOutputPercent();
  }
  public static void setElevatorPercentOutput(double output) {
    elevatorMotors[0].set(ControlMode.PercentOutput, output);
  }


  public static double getElevatorHeight() {
    return elevatorHeight;
  }
  public static void setElevatorHeight(double height) {
    elevatorHeight = height;
  }


  public double getElevatorMotorVoltage() {
    return elevatorMotors[0].getMotorOutputVoltage();
  }
  
  public static boolean getElevatorLowerSwitch() {
    return elevatorLowerSwitch.get();
  }

  public static void setElevatorSensorPosition(double position) {
    elevatorMotors[0].setSelectedSensorPosition(position);
  }

  public static void setElevatorDesiredHeightState(elevatorHeights heightEnum) {
    desiredHeightState = heightEnum;
  }
  
  public static void setElevatorJoystickY(double joystickY) {
    elevatorJoystickY = joystickY;
  }

  public void setElevatorNeutralMode(NeutralMode mode) {
    elevatorMotors[0].setNeutralMode(mode);
    elevatorMotors[1].setNeutralMode(mode);
  }

  // Update elevator height using encoders and bottom limit switch
  public static void updateElevatorHeight() {

    /* Uses limit switch to act as a baseline
    * to reset the sensor position and height to improve accuracy
    */
    if(getElevatorLowerSwitch()) {
      setElevatorHeight(0.0);
      setElevatorSensorPosition(0.0);
    }
    else {
      /* Uses built in feedback sensor if not at limit switch */
      setElevatorHeight(
        elevatorMotors[0].getSelectedSensorPosition()
      );
    }
  }

  public static void updateShuffleboard() {
    // TODO: Add encoder counts per second or since last scheduler run
    elevatorClimbingTab.setBoolean(getElevatorClimbState());

    elevatorHeightTab.setDouble(getElevatorHeight());
    elevatorTargetHeightTab.setDouble(desiredHeightValue);
    elevatorTargetPosTab.setString(desiredHeightState.name());

    elevatorRawPerOutTab.setDouble(getElevatorPercentOutput());

    /* Converts the raw percent output to something more readable, by 
    *  rounding it to the nearest whole number and turning it into an actual percentage.
    *  Example: -0.71247 -> -71%
    */
    elevatorPerOutTab.setString(
      String.valueOf(
      Math.round(
      getElevatorPercentOutput()*100
      ))
      +"%"
    );
  }



  @Override
  public void simulationPeriodic() {
    elevatorSim.setInput(getElevatorMotorVoltage() * RobotController.getBatteryVoltage());

    elevatorSim.update(0.020);

    setElevatorHeight(elevatorSim.getPositionMeters());

    RoboRioSim.setVInVoltage(
        BatterySim.calculateDefaultBatteryLoadedVoltage(elevatorSim.getCurrentDrawAmps()));
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run

    switch(desiredHeightState) {
      case JOYSTICK:
        desiredHeightValue = elevatorHeight+elevatorJoystickY; // Add limits/clamp function
      case LOW:
        desiredHeightValue = 0.0; // Placeholder values
      case MID:
        desiredHeightValue = 5.0; // Placeholder values
      case HIGH:
        desiredHeightValue = 10.0; // Placeholder values
    }
    double distanceBetween = desiredHeightValue-elevatorHeight;
    if(distanceBetween < 5.0 && distanceBetween > -5.0) { // Placeholder values
      setElevatorClimbState(false);
      distanceBetween = 0;
    }
    else {
      setElevatorClimbState(true);
    }

    // TODO: Replace bang-bang controls with motion magic
    // The part where we actually determine where the elevator should move
    if (distanceBetween < 0) {
      Elevator.setElevatorPercentOutput(0.8);
    }
    else if (distanceBetween > 0) {
      Elevator.setElevatorPercentOutput(-0.8);
    }
    else if (distanceBetween == 0) {
      Elevator.setElevatorPercentOutput(0.0);
    }
  }
}
