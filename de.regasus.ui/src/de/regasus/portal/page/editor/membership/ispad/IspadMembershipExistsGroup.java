package de.regasus.portal.page.editor.membership.ispad;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.i18n.I18NComposite;

import de.regasus.common.Language;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.component.membership.ispad.IspadMembershipComponent;
import de.regasus.ui.Activator;

public class IspadMembershipExistsGroup extends EntityGroup<IspadMembershipComponent> {

	private final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private List<Language> languageList;

	// widgets
	private I18NComposite<IspadMembershipComponent> i18nComposite;


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public IspadMembershipExistsGroup(Composite parent, int style, List<Language> languageList)
	throws Exception {
		// super calls initialize(Object[]) and createWidgets(Composite)
		super(parent, style, languageList);

//		setText( IspadMembershipComponent.MEMBERSHIP_EXISTS_QUESTION.getString() );
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		try {
			this.languageList = (List<Language>) initValues[0];
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		GridDataFactory i18nCompositeGridDataFactory = GridDataFactory.fillDefaults()
			.span(COL_COUNT, 1)
			.grab(true, false)
			.indent(SWT.DEFAULT, 10);


		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new IspadMembershipExistsI18NWidgetController());
		i18nCompositeGridDataFactory.applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		i18nComposite.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		i18nComposite.syncEntityToWidgets();
	}

}
