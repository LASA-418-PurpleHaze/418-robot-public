/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.lasarobotics.hazylib;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * IterativeRobotBase implements a specific type of robot program framework, extending the RobotBase
 * class.
 *
 * <p>The IterativeRobotBase class does not implement startCompetition(), so it should not be used
 * by teams directly.
 *
 * <p>This class provides the following functions which are called by the main loop,
 * startCompetition(), at the appropriate times:
 *
 * <p>robotInit() -- provide for initialization at robot power-on
 *
 * <p>init() functions -- each of the following functions is called once when the
 * appropriate mode is entered:
 * - disabledInit()   -- called each and every time disabled is entered from
 * another mode
 * - autonomousInit() -- called each and every time autonomous is entered from
 * another mode
 * - teleopInit()     -- called each and every time teleop is entered from
 * another mode
 * - testInit()       -- called each and every time test is entered from
 * another mode
 *
 * <p>periodic() functions -- each of these functions is called on an interval:
 * - robotPeriodic()
 * - disabledPeriodic()
 * - autonomousPeriodic()
 * - teleopPeriodic()
 * - testPeriodic()
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class HazyIterative extends HazyRobotBase {
    protected double m_period;

    private enum Mode {
        kNone,
        kDisabled,
        kAutonomous,
        kTeleop,
        kTest
    }

    private Mode m_lastMode = Mode.kNone;
    private final Watchdog m_watchdog;
    private static final double kPacketPeriod = 0.02;

    protected HazyIterative() {
        m_period = kPacketPeriod;
        m_watchdog = new Watchdog(m_period, this::printLoopOverrunMessage);
    }

    @Override
    public void startCompetition() {
        robotInit();

        // Tell the DS that the robot is ready to be enabled
        HAL.observeUserProgramStarting();

        // Loop forever, calling the appropriate mode-dependent function
        while (true) {
            // Wait for new data to arrive
            m_ds.waitForData();

            loopFunc();
        }
    }

    /* ----------- Overridable initialization code ----------------- */
    public void robotInit() {
        System.out.println("Default robotInit() method... Overload me!");
    }

    public void disabledInit() {
        System.out.println("Default disabledInit() method... Overload me!");
    }

    public void autonomousInit() {
        System.out.println("Default autonomousInit() method... Overload me!");
    }

    public void teleopInit() {
        System.out.println("Default teleopInit() method... Overload me!");
    }

    public void testInit() {
        System.out.println("Default testInit() method... Overload me!");
    }

    /* ----------- Overridable periodic code ----------------- */

    private boolean m_rpFirstRun = true;

    public void robotPeriodic() {
        if (m_rpFirstRun) {
            System.out.println("Default robotPeriodic() method... Overload me!");
            m_rpFirstRun = false;
        }
    }

    private boolean m_dpFirstRun = true;

    public void disabledPeriodic() {
        if (m_dpFirstRun) {
            System.out.println("Default disabledPeriodic() method... Overload me!");
            m_dpFirstRun = false;
        }
    }

    private boolean m_apFirstRun = true;

    public void autonomousPeriodic() {
        if (m_apFirstRun) {
            System.out.println("Default autonomousPeriodic() method... Overload me!");
            m_apFirstRun = false;
        }
    }

    private boolean m_tpFirstRun = true;

    public void teleopPeriodic() {
        if (m_tpFirstRun) {
            System.out.println("Default teleopPeriodic() method... Overload me!");
            m_tpFirstRun = false;
        }
    }

    private boolean m_tmpFirstRun = true;

    @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
    public void testPeriodic() {
        if (m_tmpFirstRun) {
            System.out.println("Default testPeriodic() method... Overload me!");
            m_tmpFirstRun = false;
        }
    }

    protected void loopFunc() {
        m_watchdog.reset();

        if (isDisabled()) {
            if (m_lastMode != Mode.kDisabled) {
                disabledInit();
                m_watchdog.addEpoch("disabledInit()");
                m_lastMode = Mode.kDisabled;
            }

            HAL.observeUserProgramDisabled();
            disabledPeriodic();
            m_watchdog.addEpoch("disablePeriodic()");
        } else if (isAutonomous()) {
            // Call AutonomousInit() if we are now just entering autonomous mode from either a different
            // mode or from power-on.
            if (m_lastMode != Mode.kAutonomous) {
                autonomousInit();
                m_watchdog.addEpoch("autonomousInit()");
                m_lastMode = Mode.kAutonomous;
            }

            HAL.observeUserProgramAutonomous();
            autonomousPeriodic();
            m_watchdog.addEpoch("autonomousPeriodic()");
        } else if (isOperatorControl()) {
            // Call TeleopInit() if we are now just entering teleop mode from either a different mode or
            // from power-on.
            if (m_lastMode != Mode.kTeleop) {
                teleopInit();
                m_watchdog.addEpoch("teleopInit()");
                m_lastMode = Mode.kTeleop;
            }

            HAL.observeUserProgramTeleop();
            teleopPeriodic();
            m_watchdog.addEpoch("teleopPeriodic()");
        } else {
            // Call TestInit() if we are now just entering test mode from either a different mode or from
            // power-on.
            if (m_lastMode != Mode.kTest) {
                testInit();
                m_watchdog.addEpoch("testInit()");
                m_lastMode = Mode.kTest;
            }

            HAL.observeUserProgramTest();
            testPeriodic();
            m_watchdog.addEpoch("testPeriodic()");
        }

        robotPeriodic();
        m_watchdog.addEpoch("robotPeriodic()");
        m_watchdog.disable();

        // Warn on loop time overruns
        if (m_watchdog.isExpired()) {
            m_watchdog.printEpochs();
        }
    }

    private void printLoopOverrunMessage() {
        DriverStation.reportWarning("Loop time of " + m_period + "s overrun\n", false);
    }
}
