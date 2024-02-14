package de.regasus.hotel.offering.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.rcp.i18n.I18NText;

import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.finance.PriceGroup;

/**
 * PriceGroup for the additional prices of a hotel offering.
 */
public abstract class AdditionalHotelPriceGroup extends PriceGroup {
	
	protected I18NText names;
	
	protected Button calcAmountWithGuestCount;
	protected Button calcAmountWithNightCount;
	
	private GridData namesLabelGridData;
	private GridData namesI18NTextGridData;
	private GridData calcAmountCompositeGridData;
	private GridData calcAmountWithGuestCountGridData;
	private GridData calcAmountWithNightCountGridData;	

	public AdditionalHotelPriceGroup(
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
		
		// column 2-3
		names = new I18NText(this, SWT.NONE, LanguageProvider.getInstance(), true);
		namesI18NTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		names.setLayoutData(namesI18NTextGridData);
		
		// column 4
		new Label(this, SWT.NONE);
		
		// column 5
		Composite calcAmountComposite = new Composite(this, SWT.NONE);
		calcAmountComposite.setLayout(new GridLayout(1, true));
		calcAmountCompositeGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		calcAmountComposite.setLayoutData(calcAmountCompositeGridData);
		
		calcAmountWithGuestCount = new Button(calcAmountComposite, SWT.CHECK);
		calcAmountWithGuestCountGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		calcAmountWithGuestCount.setLayoutData(calcAmountWithGuestCountGridData);
		calcAmountWithGuestCount.setText(HotelLabel.CalcAmountWithGuestCount.getString());
		
		calcAmountWithNightCount = new Button(calcAmountComposite, SWT.CHECK);
		calcAmountWithNightCountGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		calcAmountWithNightCount.setLayoutData(calcAmountWithNightCountGridData);
		calcAmountWithNightCount.setText(HotelLabel.CalcAmountWithNightCount.getString());
	}

	
	@Override
	protected void addModifyListenerToAdditionalWidgets(
		ModifyListener modifyListener,
		SelectionListener selectionListener
	) {
		names.addModifyListener(modifyListener);
		calcAmountWithGuestCount.addSelectionListener(selectionListener);
		calcAmountWithNightCount.addSelectionListener(selectionListener);
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
		calcAmountCompositeGridData.heightHint = heightHint;
		calcAmountWithGuestCountGridData.heightHint = heightHint;
		calcAmountWithNightCountGridData.heightHint = heightHint;
	}
	
}
