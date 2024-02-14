package de.regasus.common.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameterset.AddressConfigParameterSet;
import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.participant.editor.ParticipantEditor;

/**
 * Composite with several {@link AddressGroup}.
 * It implements {@link ModifyListener} even if it is not adding itself as one. Instead it is added by
 * other classes, e.g. {@link ParticipantEditor} tp observe {@link PersonGroup}.
 * Received {@link ModifyEvent}s are then delegated to the internal {@link AddressGroup}s.
 */
public class AddressGroupsComposite extends LazyComposite implements ModifyListener {

	private AddressGroup[] addressGroupsList = new AddressGroup[AbstractPerson.ADDRESS_COUNT];

	private ModifySupport modifySupport = new ModifySupport(this);


	// parameters that can be set from outside before this LazyComposite has been initialized
	private AddressConfigParameterSet addressConfigParameterSet;
	private String homeCountryPK;
	private AbstractPerson abstractPerson;
	private int mainAddressNumber;
	private int invoiceAddressNumber;

	/**
	 * Control if country fields have to be bold if this is the main address.
	 */
	private boolean mainCountryBold;

	/**
	 * Control if city fields have to be bold if this is the main address.
	 */
	private boolean mainCityBold;


	public AddressGroupsComposite(
		Composite parent,
		int style,
		AddressConfigParameterSet addressConfigParameterSet
	)
	throws Exception {
		super(parent, style);
		this.addressConfigParameterSet = addressConfigParameterSet;
	}


	@Override
	protected void createPartControl() throws Exception {
		setLayout(new GridLayout(2, true));

		for (int i = 0; i < addressGroupsList.length; i++) {
			int addressNumber = i + 1;

			boolean visible = true;
			if (addressConfigParameterSet != null) {
				visible = addressConfigParameterSet.getAddress(addressNumber).isVisible();
			}

			if (visible) {
    			addressGroupsList[i] = new AddressGroup(
    				this,
    				SWT.NONE,
    				addressConfigParameterSet,
    				addressNumber,
    				this,
    				(Address) null	// groupManagerAddress, only relevant when AddressGroup used in wizard
    			);

    			addressGroupsList[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    			addressGroupsList[i].addModifyListener(modifySupport);
			}
			else {
				// create label as placeholder
				new Label(this, SWT.NONE);
			}
		}

		setHomeCountryPK(homeCountryPK);
		setAbstractPerson(abstractPerson);
		setMainAddressGroup(mainAddressNumber);
		setInvoiceAddressGroup(invoiceAddressNumber);

		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				layout();
			}
		});
	}


	public void setAbstractPerson(final AbstractPerson abstractPerson) {
		this.abstractPerson = abstractPerson;
		
		if (abstractPerson != null) {
			for (AddressGroup addressGroup : addressGroupsList) {
				if (addressGroup != null) {
					addressGroup.setAbstractPerson(abstractPerson);
				}
			}
			
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					setMainAddressGroup( abstractPerson.getMainAddressNumber() );
					setInvoiceAddressGroup( abstractPerson.getInvoiceAddressNumber() );
				}
			});
		}

	}


	public void setHomeCountryPK(String homeCountryPK) {
		this.homeCountryPK = homeCountryPK;

		for (AddressGroup addressGroup : addressGroupsList) {
			if (addressGroup != null) {
				addressGroup.setHomeCountryPK(homeCountryPK);
			}
		}
	}


	@Override
	public void modifyText(ModifyEvent event) {
		// delegate ModifyEvent to internal AddressGroups
		for (AddressGroup addressGroup : addressGroupsList) {
			if (addressGroup != null) {
				addressGroup.modifyText(event);
			}
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	public void syncEntityToWidgets() {
		for (AddressGroup addressGroup : addressGroupsList) {
			if (addressGroup != null) {
				addressGroup.syncEntityToWidgets() ;
			}
		}
	}


	public void autoCorrection() {
		/* Auto correction is done in the widgets!
		 * Therefore this LazyComposite has to be initialized before.
		 * Otherwise auto correction would not affect address data.
		 */
		init();

		for (AddressGroup addressGroup : addressGroupsList) {
			if (addressGroup != null) {
				addressGroup.autoCorrection();
			}
		}
	}


	public void setMainAddressGroup(int mainAddressNumber) {
		this.mainAddressNumber = mainAddressNumber;

		for (int i = 0; i < addressGroupsList.length; i++) {
			AddressGroup addressGroup = addressGroupsList[i];
			if (addressGroup != null) {
    			int groupAddressNumber = i + 1;
    			boolean isMainAddressGroup = (mainAddressNumber == groupAddressNumber);
    			addressGroup.setMainAddress(isMainAddressGroup);
    			if (isMainAddressGroup) {
    				addressGroup.setCountryBold(mainCountryBold);
    				addressGroup.setCityBold(mainCityBold);
    			}
    			else {
    				addressGroup.setCountryBold(false);
    				addressGroup.setCityBold(false);
    			}
			}
		}
	}


	public void setInvoiceAddressGroup(int invoiceAddressNumber) {
		this.invoiceAddressNumber = invoiceAddressNumber;

		for(int i = 0; i < addressGroupsList.length; i++) {
			AddressGroup addressGroup = addressGroupsList[i];
			if (addressGroup != null) {
    			int groupAddressNumber = i + 1;
    			boolean isInvoiceAddressGroup = (invoiceAddressNumber == groupAddressNumber);
    			addressGroup.setInvoiceAddress(isInvoiceAddressGroup);
			}
		}
	}


	public void refreshDefaultAddressLabel() {
		for (AddressGroup addressGroup : addressGroupsList) {
			if (addressGroup != null) {
				addressGroup.refreshDefaultAddressLabel();
			}
		}
	}


	/**
	 * Control if country fields have to be bold if this is the main address.
	 * @param b
	 */
	public void setMainCountryBold(boolean b) {
		this.mainCountryBold = b;
	}


	/**
	 * Control if city fields have to be bold if this is the main address.
	 * @param b
	 */
	public void setMainCityBold(boolean b) {
		this.mainCityBold = b;
	}

}
