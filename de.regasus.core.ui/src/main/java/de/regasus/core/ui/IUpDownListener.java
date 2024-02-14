package de.regasus.core.ui;


/**
 * An interface to be implemented by views or composites which make use of an {@link UpDownComposite}, to be notified when
 * one of the buttons contained in there is pressed.
 * 
 * @author manfred
 * 
 */

public interface IUpDownListener {

	public void upPressed();
	public void downPressed();
	public void topPressed();
	public void bottomPressed();
}
