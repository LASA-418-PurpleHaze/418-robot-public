/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.lasarobotics.frc2019;

import org.lasarobotics.hazylib.HazyRobotBase;

public final class Main {
    private Main() {
    }

    public static void main(String... args) {
        HazyRobotBase.startRobot(org.lasarobotics.frc2019.Robot::new);
    }
}
