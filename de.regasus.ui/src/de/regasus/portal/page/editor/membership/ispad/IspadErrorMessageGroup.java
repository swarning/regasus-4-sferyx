package de.regasus.portal.page.editor.membership.ispad;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.i18n.I18NHtmlWidget;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.component.membership.ispad.IspadMembershipComponent;
import de.regasus.ui.Activator;

public class IspadErrorMessageGroup extends EntityGroup<IspadMembershipComponent> {

	private final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private List<Language> languageList;

	private static GridDataFactory htmlEditorLabelGridDataFactory;
	private static GridDataFactory htmlEditorGridDataFactory;

	// widgets
	private I18NHtmlWidget invalidMembershipMessageWidget;
	private I18NHtmlWidget invalidUserCredentialsMessageWidget;



	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public IspadErrorMessageGroup(Composite parent, int style, List<Language> languageList)
	throws Exception {
		// super calls initialize(Object[]) and createWidgets(Composite)
		super(parent, style, languageList);

//		setText( IspadMembershipComponent.USER_NAME_PASSWORD_PROMPT.getString() );
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


		htmlEditorLabelGridDataFactory = GridDataFactory.swtDefaults()
	    	.align(SWT.LEFT, SWT.CENTER)
	    	.span(COL_COUNT, 1)
	    	.indent(SWT.DEFAULT, 10);

		htmlEditorGridDataFactory = GridDataFactory.fillDefaults()
			.span(COL_COUNT, 1)
			.grab(true, true)
			.hint(SWT.DEFAULT, 300);


		buildInvalidMembershipMessageEditor(parent);
		buildInvalidUserCredentialsMessageEditor(parent);
	}


	private void buildInvalidMembershipMessageEditor(Composite parent) {
		Label label = new Label(parent, SWT.RIGHT);
		htmlEditorLabelGridDataFactory.applyTo(label);
		label.setText( IspadMembershipComponent.INVALID_MEMBERSHIP_MESSAGE.getString() );
		SWTHelper.makeBold(label);

		invalidMembershipMessageWidget = new I18NHtmlWidget(parent, SWT.BORDER, languageList);
		htmlEditorGridDataFactory.applyTo(invalidMembershipMessageWidget);
		invalidMembershipMessageWidget.addModifyListener(modifySupport);
	}


	private void buildInvalidUserCredentialsMessageEditor(Composite parent) {
		Label label = new Label(parent, SWT.RIGHT);
		htmlEditorLabelGridDataFactory.applyTo(label);
		label.setText( IspadMembershipComponent.INVALID_USER_CREDENTIALS_MESSAGE.getString() );
		SWTHelper.makeBold(label);

		invalidUserCredentialsMessageWidget = new I18NHtmlWidget(parent, SWT.BORDER, languageList);
		htmlEditorGridDataFactory.applyTo(invalidUserCredentialsMessageWidget);
		invalidUserCredentialsMessageWidget.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		invalidMembershipMessageWidget.setLanguageString( entity.getInvalidMembershipMessage() );
		invalidUserCredentialsMessageWidget.setLanguageString( entity.getInvalidUserCredentialsMessage() );
	}


	@Override
	public void syncEntityToWidgets() {
		entity.setInvalidMembershipMessage( invalidMembershipMessageWidget.getLanguageString() );
		entity.setInvalidUserCredentialsMessage( invalidUserCredentialsMessageWidget.getLanguageString() );
	}

}
