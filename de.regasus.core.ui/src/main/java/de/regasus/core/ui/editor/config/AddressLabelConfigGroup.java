package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigLabel;
import com.lambdalogic.messeinfo.config.parameter.AddressLabelConfigParameter;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;


public class AddressLabelConfigGroup  extends Group {

	// the entity
	private AddressLabelConfigParameter addressLabelConfigParameter;


	// Widgets
	private BooleanConfigWidgets ignoreSimpleDregreeWidgets;
	private BooleanConfigWidgets writeOutProfWidgets;
	private BooleanConfigWidgets writeOutDrWidgets;
	private BooleanConfigWidgets writeOutNobilityWidgets;
	private BooleanConfigWidgets emptyLineBeforeGermanCityWidgets;


	public AddressLabelConfigGroup(
		Composite parent,
		int style
	) {
		super(parent, style);

		final GridLayout gridLayout = new GridLayout(BooleanConfigWidgets.NUM_COLS, false);
		setLayout(gridLayout);
		setText( Address.LABEL.getString() );


		ignoreSimpleDregreeWidgets = new BooleanConfigWidgets(
			this,
			ConfigLabel.IgnoreSimpleDregreeInAddress_Label.getString(),
			ConfigLabel.IgnoreSimpleDregreeInAddress_Description.getString()
		);
		writeOutProfWidgets = new BooleanConfigWidgets(
			this,
			ConfigLabel.WriteOutProfInAddress_Label.getString(),
			ConfigLabel.WriteOutProfInAddress_Description.getString()
		);
		writeOutDrWidgets = new BooleanConfigWidgets(
			this,
			ConfigLabel.WriteOutDrInAddress_Label.getString(),
			ConfigLabel.WriteOutDrInAddress_Description.getString()
		);
		writeOutNobilityWidgets = new BooleanConfigWidgets(
			this,
			ConfigLabel.WriteOutNobilityInAddress_Label.getString(),
			ConfigLabel.WriteOutNobilityInAddress_Description.getString()
		);
		emptyLineBeforeGermanCityWidgets = new BooleanConfigWidgets(
			this,
			ConfigLabel.EmptyLineBeforeGermanCity_Label.getString(),
			ConfigLabel.EmptyLineBeforeGermanCity_Description.getString()
		);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		ignoreSimpleDregreeWidgets.addModifyListener(modifyListener);
		writeOutProfWidgets.addModifyListener(modifyListener);
		writeOutDrWidgets.addModifyListener(modifyListener);
		writeOutNobilityWidgets.addModifyListener(modifyListener);
		emptyLineBeforeGermanCityWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (addressLabelConfigParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						ignoreSimpleDregreeWidgets.setValue(addressLabelConfigParameter.getIgnoreSimpleDregree());
						writeOutProfWidgets.setValue(addressLabelConfigParameter.getWriteOutProf());
						writeOutDrWidgets.setValue(addressLabelConfigParameter.getWriteOutDr());
						writeOutNobilityWidgets.setValue(addressLabelConfigParameter.getWriteOutNobility());
						emptyLineBeforeGermanCityWidgets.setValue(addressLabelConfigParameter.getEmptyLineBeforeGermanCity());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (addressLabelConfigParameter != null) {
			addressLabelConfigParameter.setIgnoreSimpleDregree(ignoreSimpleDregreeWidgets.getValue());
			addressLabelConfigParameter.setWriteOutProf(writeOutProfWidgets.getValue());
			addressLabelConfigParameter.setWriteOutDr(writeOutDrWidgets.getValue());
			addressLabelConfigParameter.setWriteOutNobility(writeOutNobilityWidgets.getValue());
			addressLabelConfigParameter.setEmptyLineBeforeGermanCity(emptyLineBeforeGermanCityWidgets.getValue());
		}
	}


	public void setAddressLabelConfigParameter(AddressLabelConfigParameter addressLabelConfigParameter) {
		this.addressLabelConfigParameter = addressLabelConfigParameter;

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
