// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.simulation;

import java.util.ArrayList;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.Vision.CAMERA_POSITION;
import frc.robot.subsystems.SwerveDrive;
import frc.robot.subsystems.Vision;
import frc.robot.utils.ModuleMap;
import frc.robot.simulation.SimConstants.Grids;

public class FieldSim extends SubsystemBase {
  private final SwerveDrive m_swerveDrive;
  private final Vision m_vision;

  private final Field2d m_field2d = new Field2d();

  private ArrayList<Pose2d> gridNodes = new ArrayList<>();

  public FieldSim(SwerveDrive swerveDrive, Vision vision) {
  
    for (int i = 0; i < Grids.nodeRowCount; i++) {
      gridNodes.add(new Pose2d(Grids.outerX/2+Grids.lowX, Grids.nodeFirstY+(Grids.nodeSeparationY*i), new Rotation2d(0)));
      gridNodes.add(new Pose2d(Grids.outerX/2+Grids.midX, Grids.nodeFirstY+(Grids.nodeSeparationY*i), new Rotation2d(0)));
      gridNodes.add(new Pose2d(Grids.outerX/2+Grids.highX, Grids.nodeFirstY+(Grids.nodeSeparationY*i), new Rotation2d(0)));

      gridNodes.add(new Pose2d(SimConstants.fieldLength-(Grids.outerX/2+Grids.lowX), Grids.nodeFirstY+(Grids.nodeSeparationY*i), new Rotation2d(0)));
      gridNodes.add(new Pose2d(SimConstants.fieldLength-(Grids.outerX/2+Grids.midX), Grids.nodeFirstY+(Grids.nodeSeparationY*i), new Rotation2d(0)));
      gridNodes.add(new Pose2d(SimConstants.fieldLength-(Grids.outerX/2+Grids.highX), Grids.nodeFirstY+(Grids.nodeSeparationY*i), new Rotation2d(0)));
    }

    m_swerveDrive = swerveDrive;
    m_vision = vision;
  }

  public void initSim() {}

  public Field2d getField2d() {
    return m_field2d;
  }

  public void setTrajectory(Trajectory trajectory) {
    m_field2d.getObject("trajectory").setTrajectory(trajectory);
  }

  public void resetRobotPose(Pose2d pose) {
    m_field2d.setRobotPose(pose);
  }

  private void updateRobotPoses() {
    m_field2d.setRobotPose(m_swerveDrive.getPoseMeters());
    m_field2d
        .getObject("oakAvgRobotPose")
        .setPose(m_vision.getRobotPose2d(Constants.Vision.CAMERA_POSITION.FORWARD_LOCALIZER));
    m_field2d
        .getObject("oakRobotPoses")
        .setPoses(m_vision.getRobotPoses2d(Constants.Vision.CAMERA_POSITION.FORWARD_LOCALIZER));
    m_field2d
        .getObject("oakTagPoses")
        .setPoses(m_vision.getTagPoses2d(CAMERA_POSITION.FORWARD_LOCALIZER));

    m_field2d
        .getObject("Limelight Pose")
        .setPose(m_vision.getRobotPose2d(CAMERA_POSITION.REAR_LOCALIZER));
    
    int i = 1;
    for (Pose2d node : gridNodes) {
      m_field2d
        .getObject("Node "+Integer.toString(i))
        .setPose(node);
      i++;
    }
    
    if (RobotBase.isSimulation()) {
      m_field2d
          .getObject("Swerve Modules")
          .setPoses(ModuleMap.orderedValues(m_swerveDrive.getModulePoses(), new Pose2d[0]));
    }
  }

  @Override
  public void periodic() {
    updateRobotPoses();

    if (RobotBase.isSimulation()) simulationPeriodic();

    SmartDashboard.putData("Field2d", m_field2d);
  }

  public void simulationPeriodic() {}
}
