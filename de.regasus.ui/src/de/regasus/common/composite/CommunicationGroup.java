package de.regasus.common.composite;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.parameterset.CommunicationConfigParameterSet;
import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.contact.Communication;
import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

//REFERENCE
public class CommunicationGroup extends EntityGroup<Communication> {

	private final int COL_COUNT = 2;


	// widgets
	private Text phone1;
	private Text phone2;
	private Text phone3;
	private Text mobile1;
	private Text mobile2;
	private Text mobile3;
	private Text fax1;
	private Text fax2;
	private Text fax3;
	private Text email1;
	private Text email2;
	private Text email3;
	private Text www;


	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// config flags
	private boolean showPhone1;
	private boolean showPhone2;
	private boolean showPhone3;
	private boolean showMobile1;
	private boolean showMobile2;
	private boolean showMobile3;
	private boolean showFax1;
	private boolean showFax2;
	private boolean showFax3;
	private boolean showEmail1;
	private boolean showEmail2;
	private boolean showEmail3;
	private boolean showWww;


	public CommunicationGroup(
		Composite parent,
		int style,
		CommunicationConfigParameterSet configParameterSet
	)
	throws Exception {
		// super calls initialize(Object[]) and createWidgets(Composite)
		super(parent, style, configParameterSet);

		setText( AbstractPerson.COMMUNICATION.getString() );
	}


	public CommunicationGroup(Composite parent, int style) throws Exception {
		this(parent, style, null);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		try {
			CommunicationConfigParameterSet configParameterSet = (CommunicationConfigParameterSet) initValues[0];
    		if (configParameterSet != null) {
    			showPhone1	= configParameterSet.getPhone1().isVisible();
    			showPhone2	= configParameterSet.getPhone2().isVisible();
    			showPhone3	= configParameterSet.getPhone3().isVisible();
    			showMobile1	= configParameterSet.getMobile1().isVisible();
    			showMobile2	= configParameterSet.getMobile2().isVisible();
    			showMobile3	= configParameterSet.getMobile3().isVisible();
    			showFax1	= configParameterSet.getFax1().isVisible();
    			showFax2	= configParameterSet.getFax2().isVisible();
    			showFax3	= configParameterSet.getFax3().isVisible();
    			showEmail1	= configParameterSet.getEmail1().isVisible();
    			showEmail2	= configParameterSet.getEmail2().isVisible();
    			showEmail3	= configParameterSet.getEmail3().isVisible();
    			showWww		= configParameterSet.getWww().isVisible();
    		}
    		else {
    			showPhone1 = true;
    			showPhone2 = true;
    			showPhone3 = true;
    			showMobile1 = true;
    			showMobile2 = true;
    			showMobile3 = true;
    			showFax1 = true;
    			showFax2 = true;
    			showFax3 = true;
    			showEmail1 = true;
    			showEmail2 = true;
    			showEmail3 = true;
    			showWww = true;
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );


		if (showPhone1) {
			phone1 = widgetBuilder.fieldMetadata(Communication.PHONE1).createTextWithLabel();
		}

		if (showMobile1) {
			mobile1 = widgetBuilder.fieldMetadata(Communication.MOBILE1).createTextWithLabel();
		}

		if (showFax1) {
			fax1 = widgetBuilder.fieldMetadata(Communication.FAX1).createTextWithLabel();
		}

		if (showEmail1) {
			email1 = widgetBuilder.fieldMetadata(Communication.EMAIL1).createTextWithLabel();
		}


		if (showPhone2 || showMobile2 || showFax2 || showEmail2) {
			widgetBuilder.verticalSpace();
		}

		if (showPhone2) {
			phone2 = widgetBuilder.fieldMetadata(Communication.PHONE2).createTextWithLabel();
		}

		if (showMobile2) {
			mobile2 = widgetBuilder.fieldMetadata(Communication.MOBILE2).createTextWithLabel();
		}

		if (showFax2) {
			fax2 = widgetBuilder.fieldMetadata(Communication.FAX2).createTextWithLabel();
		}

		if (showEmail2) {
			email2 = widgetBuilder.fieldMetadata(Communication.EMAIL2).createTextWithLabel();
		}


		if (showPhone3 || showMobile3 || showFax3 || showEmail3) {
			widgetBuilder.verticalSpace();
		}

		if (showPhone3) {
			phone3 = widgetBuilder.fieldMetadata(Communication.PHONE3).createTextWithLabel();
		}

		if (showMobile3) {
			mobile3 = widgetBuilder.fieldMetadata(Communication.MOBILE3).createTextWithLabel();
		}

		if (showFax3) {
			fax3 = widgetBuilder.fieldMetadata(Communication.FAX3).createTextWithLabel();
		}

		if (showEmail3) {
			email3 = widgetBuilder.fieldMetadata(Communication.EMAIL3).createTextWithLabel();
		}


		if (showWww) {
			widgetBuilder.verticalSpace();
			www = widgetBuilder.fieldMetadata(Communication.WWW).createTextWithLabel();
		}
	}



	/**
	 * Copy values from widgets to entity and return it.
	 * @return
	 */
	public Communication getCommunication() {
		syncEntityToWidgets();
		return entity;
	}


	/**
	 * Set entity and copy its values to widgets.
	 * @param communication
	 */
	public void setCommunication(Communication communication) {
		setEntity(communication);
	}


	@Override
	public void setEnabled (boolean enabled) {
		if (showPhone1) {
			phone1.setEnabled(enabled);
		}

		if (showMobile1) {
			mobile1.setEnabled(enabled);
		}

		if (showFax1) {
			fax1.setEnabled(enabled);
		}

		if (showEmail1) {
			email1.setEnabled(enabled);
		}

		if (showPhone2) {
			phone2.setEnabled(enabled);
		}

		if (showMobile2) {
			mobile2.setEnabled(enabled);
		}

		if (showFax2) {
			fax2.setEnabled(enabled);
		}

		if (showEmail2) {
			email2.setEnabled(enabled);
		}

		if (showPhone3) {
			phone3.setEnabled(enabled);
		}

		if (showMobile3) {
			mobile3.setEnabled(enabled);
		}

		if (showFax3) {
			fax3.setEnabled(enabled);
		}

		if (showEmail3) {
			email3.setEnabled(enabled);
		}

		if (showWww) {
			www.setEnabled(enabled);
		}
	}

}
