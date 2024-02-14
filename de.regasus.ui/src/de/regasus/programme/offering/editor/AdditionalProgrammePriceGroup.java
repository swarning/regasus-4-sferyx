package de.regasus.programme.offering.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.rcp.i18n.I18NText;

import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.finance.PriceGroup;

public abstract class AdditionalProgrammePriceGroup extends PriceGroup {
	
	protected I18NText names;
	
	private GridData namesLabelGridData;
	private GridData namesI18NTextGridData;

	
	public AdditionalProgrammePriceGroup(
		Composite parent, 
		int style, 
		Long eventPK, 
		String label
	) 
	throws Exception {
		super(parent, style, eventPK, label);
	}

	
	@Override
	protected void createAdditionalWidget() {
		/* The widgets have to be added into this Group that has a GridLayout with 5 columns.
		 * All 5 columns have to be filled! Otherwise the layout is destroyed!
		 */

		// column 1
		Label namesLabel = new Label(this, SWT.NONE);
		namesLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		namesLabel.setLayoutData(namesLabelGridData);
		namesLabel.setText(KernelLabel.Name.getString());
//		SWTHelper.makeBold(namesLabel);
		
		// column 2-5
		names = new I18NText(this, SWT.NONE, LanguageProvider.getInstance(), true);
		namesI18NTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		names.setLayoutData(namesI18NTextGridData);
	}

	
	@Override
	protected void addModifyListenerToAdditionalWidgets(
		ModifyListener modifyListener,
		SelectionListener selectionListener
	) {
		names.addModifyListener(modifyListener);
	}

	
	@Override
	/* Declared abstract, because this class may be used for either the additional price 1 or 2 
	 * this method has to be implemented in a sub class.
	 */
	protected abstract void syncAdditionalWidgetToEntity();

	
	@Override
	/* Declared abstract, because this class may be used for either the additional price 1 or 2 
	 * this method has to be implemented in a sub class.
	 */
	protected abstract void syncEntityToAdditionalWidget();


	@Override
	protected void setAdditionalWidgetHeight(int heightHint) {
		namesLabelGridData.heightHint = heightHint;
		namesI18NTextGridData.heightHint = heightHint;
	}
	
}
