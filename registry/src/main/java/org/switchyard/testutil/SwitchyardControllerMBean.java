package org.switchyard.testutil;

/**
 * MBean interface for SwitchyardController
 *
 * @author tcunning
 */
public interface SwitchyardControllerMBean {
    String getName();
    void start();
    void stop();
    boolean isRunning();
    void boot(int delay);
}
