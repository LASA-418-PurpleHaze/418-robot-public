/*----------------------------------------------------------------------------*/
/* Copyright (c) 2008-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.lasarobotics.hazylib;

import edu.wpi.first.hal.FRCNetComm.tInstances;
import edu.wpi.first.hal.FRCNetComm.tResourceType;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import org.lasarobotics.frc2019.Robot;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Supplier;

public abstract class HazyRobotBase implements AutoCloseable {
    // This is usually 1, but it is best to make sure
    public static final long MAIN_THREAD_ID = Thread.currentThread().getId();


    protected final DriverStation m_ds = DriverStation.getInstance();

    protected HazyRobotBase() {
        //see if we actually need this or not...
        /*
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        inst.setNetworkIdentity("Robot");
        inst.startServer("/home/lvuser/networktables.ini");
        m_ds = DriverStation.getInstance();
        */
    }

    @Deprecated
    public void free() {
    }

    @Override
    public void close() {
    }

    public boolean isDisabled() {
        return m_ds.isDisabled();
    }

    public boolean isEnabled() {
        return m_ds.isEnabled();
    }

    public boolean isAutonomous() {
        return m_ds.isAutonomous();
    }

    public boolean isTest() {
        return m_ds.isTest();
    }

    public boolean isOperatorControl() {
        return m_ds.isOperatorControl();
    }

    public boolean isNewDataAvailable() {
        return m_ds.isNewControlData();
    }

    /**
     * Provide an alternate "main loop" via startCompetition().
     */
    public abstract void startCompetition();

    @SuppressWarnings("JavadocMethod")
    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        String propVal = System.getProperty(name);
        if (propVal == null) {
            return defaultValue;
        }
        if ("false".equalsIgnoreCase(propVal)) {
            return false;
        } else if ("true".equalsIgnoreCase(propVal)) {
            return true;
        } else {
            throw new IllegalStateException(propVal);
        }
    }

    /**
     * Starting point for the applications.
     */
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.AvoidCatchingThrowable",
            "PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public static <T extends HazyRobotBase> void startRobot(Supplier<T> robotSupplier) {
        if (!HAL.initialize(500, 0)) {
            throw new IllegalStateException("Failed to initialize. Terminating");
        }

        HAL.report(tResourceType.kResourceType_Language, tInstances.kLanguage_Java);

        System.out.println("********** Robot program starting **********");

        T robot;
        try {
            robot = robotSupplier.get();
        } catch (Throwable throwable) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                throwable = cause;
            }
            String robotName = "Unknown";
            StackTraceElement[] elements = throwable.getStackTrace();
            if (elements.length > 0) {
                robotName = elements[0].getClassName();
            }
            DriverStation.reportError("Unhandled exception instantiating robot " + robotName + " "
                    + throwable.toString(), elements);
            DriverStation.reportWarning("Robots should not quit, but yours did!", false);
            DriverStation.reportError("Could not instantiate robot " + robotName + "!", false);
            System.exit(1);
            return;
        }

        try {
            final File file = new File("/tmp/frc_versions/FRC_Lib_Version.ini");

            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            try (OutputStream output = Files.newOutputStream(file.toPath())) {
                output.write("Java ".getBytes(StandardCharsets.UTF_8));
                output.write("2019.418".getBytes(StandardCharsets.UTF_8));
            }

        } catch (IOException ex) {
            DriverStation.reportError("Could not write FRC_Lib_Version.ini: " + ex.toString(),
                    ex.getStackTrace());
        }

        boolean errorOnExit = false;
        try {
            robot.startCompetition();
        } catch (Throwable throwable) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                throwable = cause;
            }
            DriverStation.reportError("Unhandled exception: " + throwable.toString(),
                    throwable.getStackTrace());
            errorOnExit = true;
        } finally {
            // startCompetition never returns unless exception occurs....
            DriverStation.reportWarning("Robots should not quit, but yours did!", false);
            if (errorOnExit) {
                DriverStation.reportError(
                        "The startCompetition() method (or methods called by it) should have "
                                + "handled the exception above.", false);
            } else {
                DriverStation.reportError("Unexpected return from startCompetition() method.", false);
            }
        }
        System.exit(1);
    }
}
