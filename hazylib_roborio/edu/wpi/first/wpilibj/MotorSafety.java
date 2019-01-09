/*----------------------------------------------------------------------------*/
/* Copyright (c) 2008-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This base class runs a watchdog timer and calls the subclass's StopMotor()
 * function if the timeout expires.
 *
 * <p>The subclass should call feed() whenever the motor value is updated.
 */
public abstract class MotorSafety {
  private static final double kDefaultSafetyExpiration = 0.1;

  private double m_expiration = kDefaultSafetyExpiration;
  private boolean m_enabled;
  private double m_stopTime = Timer.getFPGATimestamp();
  private final Object m_thisMutex = new Object();
  private static final Set<MotorSafety> m_instanceList = new LinkedHashSet<>();
  private static final Object m_listMutex = new Object();

  /**
   * MotorSafety constructor.
   */
  public MotorSafety() {
    synchronized (m_listMutex) {
      m_instanceList.add(this);
    }
  }

  /**
   * Feed the motor safety object.
   *
   * <p>Resets the timer on this object that is used to do the timeouts.
   */
  public void feed() {
    synchronized (m_thisMutex) {
      m_stopTime = Timer.getFPGATimestamp() + m_expiration;
    }
  }

  /**
   * Set the expiration time for the corresponding motor safety object.
   *
   * @param expirationTime The timeout value in seconds.
   */
  public void setExpiration(double expirationTime) {
    synchronized (m_thisMutex) {
      m_expiration = expirationTime;
    }
  }

  /**
   * Retrieve the timeout value for the corresponding motor safety object.
   *
   * @return the timeout value in seconds.
   */
  public double getExpiration() {
    synchronized (m_thisMutex) {
      return m_expiration;
    }
  }

  /**
   * Determine of the motor is still operating or has timed out.
   *
   * @return a true value if the motor is still operating normally and hasn't timed out.
   */
  public boolean isAlive() {
    synchronized (m_thisMutex) {
      return !m_enabled || m_stopTime > Timer.getFPGATimestamp();
    }
  }

  /**
   * Check if this motor has exceeded its timeout. This method is called periodically to determine
   * if this motor has exceeded its timeout value. If it has, the stop method is called, and the
   * motor is shut down until its value is updated again.
   */
  public void check() {
    boolean enabled;
    double stopTime;

    synchronized (m_thisMutex) {
      enabled = m_enabled;
      stopTime = m_stopTime;
    }

    if (!enabled || DriverStation.getInstance().isDisabled() || DriverStation.getInstance().isTest()) {
      return;
    }

    if (stopTime < Timer.getFPGATimestamp()) {
      DriverStation.reportError(getDescription() + "... Output not updated often enough.", false);

      stopMotor();
    }
  }

  /**
   * Enable/disable motor safety for this device.
   *
   * <p>Turn on and off the motor safety option for this PWM object.
   *
   * @param enabled True if motor safety is enforced for this object
   */
  public void setSafetyEnabled(boolean enabled) {
    synchronized (m_thisMutex) {
      m_enabled = enabled;
    }
  }

  /**
   * Return the state of the motor safety enabled flag.
   *
   * <p>Return if the motor safety is currently enabled for this device.
   *
   * @return True if motor safety is enforced for this device
   */
  public boolean isSafetyEnabled() {
    synchronized (m_thisMutex) {
      return m_enabled;
    }
  }

  /**
   * Check the motors to see if any have timed out.
   *
   * <p>This static method is called periodically to poll all the motors and stop any that have
   * timed out.
   */
  public static void checkMotors() {
    synchronized (m_listMutex) {
      for (MotorSafety elem : m_instanceList) {
        elem.check();
      }
    }
  }

  public abstract void stopMotor();

  public abstract String getDescription();
}
