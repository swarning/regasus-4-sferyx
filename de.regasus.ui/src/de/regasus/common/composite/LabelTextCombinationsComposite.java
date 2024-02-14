package de.regasus.common.composite;

import java.util.List;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.ILabelTextCombinations;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class LabelTextCombinationsComposite extends Composite {

	// entity
	private ILabelTextCombinations labelTextSupport;

	protected final ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private I18NComposite<ILabelTextCombinations> i18nComposite;


	public LabelTextCombinationsComposite(
		Composite parent,
		int style,
		ILabelTextCombinations labelTextCombinations,
		List<Language> languageList
	) {
		super(parent, style);

		this.labelTextSupport = labelTextCombinations;

		try {
			setLayout(new GridLayout(1, false));

			if (labelTextCombinations != null) {
				i18nComposite = new I18NComposite<>(
					this,
					SWT.BORDER,
					languageList,
					new LabelTextCombinationsI18NWidgetController(labelTextCombinations.getSize())
				);
				GridDataFactory.fillDefaults().span(4, 1).grab(true, false).applyTo(i18nComposite);
				i18nComposite.addModifyListener(modifySupport);
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new RuntimeException(e);
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

	public void setEntity(ILabelTextCombinations labelTextSupport) {
		this.labelTextSupport = labelTextSupport;

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (labelTextSupport != null) {

    		SWTHelper.syncExecDisplayThread(new Runnable() {
    			@Override
				public void run() {
    				try {
    					i18nComposite.setEntity(labelTextSupport);
    				}
    				catch (Exception e) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    				}
    			}
    		});

		}
	}


	public void syncEntityToWidgets() {
		if (labelTextSupport != null) {
			i18nComposite.syncEntityToWidgets();
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
