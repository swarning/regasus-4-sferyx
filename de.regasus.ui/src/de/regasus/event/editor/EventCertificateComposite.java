package de.regasus.event.editor;

import static com.lambdalogic.util.StringHelper.isNotEmpty;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.jdom2.JDOMException;

import com.lambdalogic.messeinfo.config.parameterset.CertificateConfigParameterSet;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class EventCertificateComposite extends Composite {

	// the entity
	private EventVO eventVO;
	private Long eventPK;

	private ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private EventCertificateDispatchGroup certificateDispatchGroup;
	private Text certificatePolicy;

	// *
	// * Widgets
	// **************************************************************************


	public EventCertificateComposite(Composite parent, int style, CertificateConfigParameterSet configParameterSet)
	throws Exception {
		super(parent, style);

		setLayout(new GridLayout());

		if ( configParameterSet.getEmail().isVisible() ) {
    		// EventCertificateDispatchGroup
    		certificateDispatchGroup = new EventCertificateDispatchGroup(this, SWT.NONE);
    		GridDataFactory.fillDefaults().applyTo(certificateDispatchGroup);
    		certificateDispatchGroup.addModifyListener(modifySupport);
		}

		// Certificate Policy
		certificatePolicy = new Text(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(certificatePolicy);

		// set font for XML code
		Font sourceCodeFont = com.lambdalogic.util.rcp.Activator.getDefault().getFontFromRegistry(
			com.lambdalogic.util.rcp.Activator.SOURCE_CODE_FONT
		);

		if (sourceCodeFont != null) {
			certificatePolicy.setFont(sourceCodeFont);
		}

		certificatePolicy.addModifyListener(modifySupport);
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


	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (certificateDispatchGroup != null) {
							certificateDispatchGroup.setEntity(eventVO);
						}

						XMLContainer certificatePolicyXMLConteiner = eventVO.getCertificatePolicyXML();
						String certificatePolicySource = null;
						if (certificatePolicyXMLConteiner != null) {
							try {
								certificatePolicySource = certificatePolicyXMLConteiner.getPrettySource();
							}
							catch (JDOMException e) {
								certificatePolicySource = certificatePolicyXMLConteiner.getRawSource();
							}
						}
						certificatePolicy.setText(StringHelper.avoidNull(certificatePolicySource));
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (eventVO != null) {
			if (certificateDispatchGroup != null) {
				certificateDispatchGroup.syncEntityToWidgets();
			}

			String certificatePolicySource = certificatePolicy.getText();
			if ( isNotEmpty(certificatePolicySource) ) {
				XMLContainer certificatePolicyXMLContainer = new XMLContainer(certificatePolicySource);
				eventVO.setCertificatePolicyXML(certificatePolicyXMLContainer);
			}
			else {
				eventVO.setCertificatePolicyXML(null);
			}
		}
	}


	public void setEventVO(EventVO eventVO) {
		this.eventVO = eventVO;

		if (eventPK == null && eventVO.getID() != null) {
			eventPK = eventVO.getID();
		}

		syncWidgetsToEntity();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
