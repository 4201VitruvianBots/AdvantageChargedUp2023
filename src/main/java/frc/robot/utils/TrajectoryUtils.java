// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utils;

import com.pathplanner.lib.PathConstraints;
import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.commands.PPSwerveControllerCommand;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import frc.robot.Constants.SWERVE_DRIVE;
import frc.robot.subsystems.SwerveDrive.SwerveDrive;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryUtils {
  public static List<PathPlannerTrajectory> readTrajectory(
      String fileName, PathConstraints segmentConstraints) {
    return readTrajectory(fileName, segmentConstraints, segmentConstraints);
  }

  public static List<PathPlannerTrajectory> readTrajectory(
      String fileName, PathConstraints pathConstraint, PathConstraints... segmentConstraints) {

    if (pathConstraint.maxVelocity == 0 || pathConstraint.maxAcceleration == 0) {
      DriverStation.reportError(fileName + " has an invalid velocity/acceleration", true);
    }
    for (var c : segmentConstraints) {
      if (c.maxVelocity == 0 || c.maxAcceleration == 0) {
        DriverStation.reportError(fileName + " has an invalid velocity/acceleration", true);
      }
    }

    if (fileName.startsWith("Red")) {
      var file = new File(Filesystem.getDeployDirectory(), "pathplanner/" + fileName + ".path");
      if (!file.exists()) {
        DriverStation.reportWarning(
            "TrajectoryUtils::readTrajectory failed for " + fileName, false);
        fileName = fileName.replace("Red", "Blue");

        var pathGroup = PathPlanner.loadPathGroup(fileName, pathConstraint, segmentConstraints);

        ArrayList<PathPlannerTrajectory> ppTrajectories = new ArrayList<>();
        for (var trajectory : pathGroup) {
          ppTrajectories.add(
              PathPlannerTrajectory.transformTrajectoryForAlliance(
                  trajectory, DriverStation.Alliance.Red));
        }
        return ppTrajectories;
      }
      return PathPlanner.loadPathGroup(fileName, pathConstraint, segmentConstraints);
    } else {
      try {
        var file = new File(Filesystem.getDeployDirectory(), "pathplanner/" + fileName + ".path");

        return PathPlanner.loadPathGroup(fileName, pathConstraint, segmentConstraints);
      } catch (Exception e) {
        DriverStation.reportError("TrajectoryUtils::readTrajectory failed for " + fileName, null);
        return new ArrayList<>();
      }
    }
  }

  public static List<PPSwerveControllerCommand> generatePPSwerveControllerCommand(
      SwerveDrive swerveDrive, String pathName, PathConstraints constraints) {
    var trajectories = readTrajectory(pathName, constraints);

    return generatePPSwerveControllerCommand(swerveDrive, trajectories);
  }

  public static List<PPSwerveControllerCommand> generatePPSwerveControllerCommand(
      SwerveDrive swerveDrive, List<PathPlannerTrajectory> trajectories) {
    List<PPSwerveControllerCommand> commands = new ArrayList<>();

    for (var trajectory : trajectories) {
      PPSwerveControllerCommand swerveCommand =
          new PPSwerveControllerCommand(
              trajectory,
              swerveDrive::getPoseMeters,
              SWERVE_DRIVE.kSwerveKinematics,
              swerveDrive.getXPidController(),
              swerveDrive.getYPidController(),
              swerveDrive.getThetaPidController(),
              swerveDrive::setSwerveModuleStatesAuto,
              true,
              swerveDrive);

      commands.add(swerveCommand);
    }
    return commands;
  }
}
