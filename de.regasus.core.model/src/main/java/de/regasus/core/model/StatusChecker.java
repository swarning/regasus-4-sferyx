package de.regasus.core.model;

/**
 * An interface to be implemented by classes that are to be called periodically by the {@link StatusCheckerNotifier}.
 * Those classes must be configured in the extension point de.regasus.core.model.statusChecker.
 */
public interface StatusChecker {

	public void checkStatus();
}
