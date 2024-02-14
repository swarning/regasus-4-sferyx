package com.lambdalogic.util.rcp.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.Licence;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;

public class LicenceGroup extends Group {

	private Text customerNameText;


	private NullableSpinner licenceCountSpinner;

	private DateTimeComposite beginTime;

	private DateTimeComposite endTime;

	private Text macAddressesText;


	public LicenceGroup(Composite parent, int style) {
		super(parent, style);

		boolean readOnly = (style & SWT.READ_ONLY) != 0;

		setLayout(new GridLayout(2, false));


		setText(UtilI18N.LicenceDetails);

		Label label = new Label(this, SWT.NONE);
		label.setText(UtilI18N.CustomerName);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		customerNameText = new Text(this, SWT.BORDER);
		customerNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		customerNameText.setEditable(! readOnly);

		label = new Label(this, SWT.NONE);
		label.setText(UtilI18N.LicenceCount);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		licenceCountSpinner = new NullableSpinner(this, SWT.BORDER);
		licenceCountSpinner.setEnabled(! readOnly);
		licenceCountSpinner.setMinimum(1L);

		// huge number, so that the width is always big enough
		licenceCountSpinner.setMaximum(Integer.MAX_VALUE);
		WidgetSizer.setWidth(licenceCountSpinner);

		label = new Label(this, SWT.NONE);
		label.setText(UtilI18N.BeginValid);
		label.setToolTipText(UtilI18N.IfNothingEnteredValidAtOnce);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		beginTime = new DateTimeComposite(this, SWT.BORDER);
		beginTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		beginTime.setEnabled(! readOnly);
		beginTime.setEditable(! readOnly);

		label = new Label(this, SWT.NONE);
		label.setText(UtilI18N.EndValid);
		label.setToolTipText(UtilI18N.IfNothingEnteredValidForever);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		endTime = new DateTimeComposite(this, SWT.BORDER);
		endTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		endTime.setEnabled(! readOnly);
		endTime.setEditable(! readOnly);

		label = new Label(this, SWT.NONE);
		label.setText(UtilI18N.MACAddresses);
		label.setToolTipText(UtilI18N.IfNothingEnteredValidForAllServers);
		GridData layoutData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		layoutData.verticalIndent = SWTConstants.VERTICAL_INDENT;

		label.setLayoutData(layoutData);

		macAddressesText = new MultiLineText(this, SWT.BORDER | SWT.V_SCROLL, false);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 50;
		macAddressesText.setLayoutData(layoutData);
		macAddressesText.setEditable(! readOnly);

	}

	public void setLicence(Licence licence) {
		beginTime.setDate(licence.getBeginTime());
		endTime.setDate(licence.getEndTime());
		customerNameText.setText(licence.getCustomerName());
		licenceCountSpinner.setValue(licence.getLicenceCount());
		StringBuilder sb = new StringBuilder();
		if (licence.getMacAddresses() != null) {
			for (String macAddress : licence.getMacAddresses()) {
				sb.append(macAddress);
				sb.append("\n");
			}
		}
		macAddressesText.setText(sb.toString());
	}

	public Licence getLicence() {
		// Build licence from entered data
		Licence licence = new Licence();
		licence.setBeginTime( TypeHelper.toDate(beginTime.getLocalDateTime()) );
		licence.setEndTime( TypeHelper.toDate(endTime.getLocalDateTime()) );
		licence.setCustomerName(customerNameText.getText());
		licence.setLicenceCount(licenceCountSpinner.getValueAsInteger());
		licence.setMacAddresses(getMacAddressesFromString(macAddressesText.getText()));
		return licence;
	}

	@Override
	protected void checkSubclass() {
		// Disables the inherited check that forbids subclassing
	}

	public List<String> getMacAddressesFromString(String macAddressesString) {
		List<String> addresses = new ArrayList<>();

		if (!StringHelper.isEmpty(macAddressesString)) {

			StringTokenizer st = new StringTokenizer(macAddressesString, " ,\r\n\t");
			while (st.hasMoreTokens()) {
				String addressString = st.nextToken();
				if (StringHelper.isEmpty(addressString)) {
					continue;
				}
				if (addressString.matches("(([0-9a-fA-F]){1,2}[-:]){5}([0-9a-fA-F]){1,2}")) {
					addresses.add(addressString);
				}
				else {
					throw new RuntimeException("Keine MAC-Adresse: " + addressString);
				}
			}
		}
		return addresses;
	}
}
