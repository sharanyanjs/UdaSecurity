package com.udacity.catpoint.service;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 *
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService {

    private ImageService imageService;
    private SecurityRepository securityRepository;
    private Set<StatusListener> statusListeners = new HashSet<>();
    private boolean catDetected = false; // Track if cat was detected

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     * @param armingStatus
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        if(armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
            catDetected = false; // Reset cat detection when disarmed
        } else {
            // REQUIREMENT 10: If the system is armed, reset all sensors to inactive
            Set<Sensor> sensors = getSensors();
            sensors.forEach(sensor -> {
                if(sensor.getActive()) {
                    sensor.setActive(false);
                    securityRepository.updateSensor(sensor);
                }
            });

            // REQUIREMENT 11: If the system is armed-home while the camera shows a cat, set the alarm status to alarm
            if(armingStatus == ArmingStatus.ARMED_HOME && catDetected) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }
        securityRepository.setArmingStatus(armingStatus);
    }

    /**
     * Internal method that handles alarm status changes based on whether
     * the camera currently shows a cat.
     * @param cat True if a cat is detected, otherwise false.
     */
    private void catDetected(Boolean cat) {
        this.catDetected = cat; // Store cat detection state
        ArmingStatus armingStatus = getArmingStatus();

        if(cat && armingStatus == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if (!cat) {
            // REQUIREMENT 8: If no cat detected and sensors are not active, set to NO_ALARM
            boolean anySensorActive = getSensors().stream()
                    .anyMatch(Sensor::getActive);

            if(!anySensorActive) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }
        // If cat detected but system is not armed-home, do nothing

        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    /**
     * Register the StatusListener for alarm system updates from within the SecurityService.
     * @param statusListener
     */
    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    /**
     * Change the alarm status of the system and notify all listeners.
     * @param status
     */
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    /**
     * Internal method for updating the alarm status when a sensor has been activated.
     */
    private void handleSensorActivated() {
        if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return; //no problem if the system is disarmed
        }
        switch(securityRepository.getAlarmStatus()) {
            case NO_ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
            default -> {
                // Do nothing for ALARM or other states
            }
        }
    }

    /**
     * Internal method for updating the alarm status when a sensor has been deactivated
     */
    private void handleSensorDeactivated() {
        AlarmStatus currentAlarmStatus = securityRepository.getAlarmStatus();

        switch(currentAlarmStatus) {
            case PENDING_ALARM -> {
                // REQUIREMENT 3: If pending alarm and all sensors are inactive, return to no alarm state
                boolean allSensorsInactive = getSensors().stream()
                        .allMatch(sensor -> !sensor.getActive());

                if(allSensorsInactive) {
                    setAlarmStatus(AlarmStatus.NO_ALARM);
                }
            }
            case ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            default -> {
                // Do nothing for NO_ALARM and other states
            }
        }
    }

    /**
     * Change the activation status for the specified sensor and update alarm status if necessary.
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        AlarmStatus currentAlarmStatus = securityRepository.getAlarmStatus();
        boolean sensorWasActive = sensor.getActive();

        if(!sensorWasActive && active) {
            // Sensor becoming active
            handleSensorActivated();
        } else if (sensorWasActive && !active) {
            // Sensor becoming inactive
            handleSensorDeactivated();
        } else if (sensorWasActive && active) {
            // REQUIREMENT 5: If a sensor is activated while already active and the system is in pending state, change it to alarm state
            if(currentAlarmStatus == AlarmStatus.PENDING_ALARM) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }

        // Update sensor state
        sensor.setActive(active);
        securityRepository.updateSensor(sensor);

        // Additional check for Requirement 3: If pending alarm and all sensors become inactive
        // This handles the case even when deactivating an already inactive sensor
        if(currentAlarmStatus == AlarmStatus.PENDING_ALARM && !active) {
            boolean allSensorsInactive = getSensors().stream()
                    .allMatch(s -> !s.getActive());

            if(allSensorsInactive) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }
    }

    /**
     * Send an image to the SecurityService for processing. The securityService will use its provided
     * ImageService to analyze the image for cats and update the alarm status accordingly.
     * @param currentCameraImage
     */
    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}