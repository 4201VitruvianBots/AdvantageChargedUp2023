package frc.robot.commands.auto;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.pathplanner.lib.PathConstraints;
import com.pathplanner.lib.auto.SwerveAutoBuilder;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.swerve.SetSwerveNeutralMode;
import frc.robot.commands.swerve.SetSwerveOdometry;
import frc.robot.simulation.FieldSim;
import frc.robot.subsystems.SwerveDrive;
import frc.robot.utils.TrajectoryUtils;

public class BlueMiddleTwoConeBottomBalance extends SequentialCommandGroup {
  public BlueMiddleTwoConeBottomBalance(SwerveAutoBuilder autoBuilder, SwerveDrive swerveDrive, FieldSim fieldSim) {
    var trajectory =
        TrajectoryUtils.readTrajectory(
            "BlueMiddleTwoConeBalance",
            new PathConstraints(Units.feetToMeters(13), Units.feetToMeters(52)));

    var autoPath = autoBuilder.fullAuto(trajectory);
    addCommands(
        new SetSwerveOdometry(swerveDrive, trajectory.get(0).getInitialHolonomicPose(), fieldSim),
        autoPath,
        new SetSwerveNeutralMode(swerveDrive, NeutralMode.Brake)
            .andThen(() -> swerveDrive.drive(0, 0, 0, false, false)));
  }
}
