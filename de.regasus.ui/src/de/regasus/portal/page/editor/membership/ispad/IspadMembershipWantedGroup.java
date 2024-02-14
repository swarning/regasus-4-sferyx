package de.regasus.portal.page.editor.membership.ispad;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.i18n.I18NHtmlWidget;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.component.membership.ispad.IspadMembershipComponent;
import de.regasus.ui.Activator;

public class IspadMembershipWantedGroup extends EntityGroup<IspadMembershipComponent> {

	private final int COL_COUNT = 2;

	private static GridDataFactory htmlEditorLabelGridDataFactory;
	private static GridDataFactory htmlEditorGridDataFactory;
	private static GridDataFactory i18nCompositeGridDataFactory;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private List<Language> languageList;

	// widgets
	private I18NHtmlWidget membershipWantedQuestionWidget;
	private I18NComposite<IspadMembershipComponent> i18nComposite;
	private I18NHtmlWidget membershipWantedInfoWidget;


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public IspadMembershipWantedGroup(Composite parent, int style, List<Language> languageList)
	throws Exception {
		// super calls initialize(Object[]) and createWidgets(Composite)
		super(parent, style, languageList);

//		setText( IspadMembershipComponent.MEMBERSHIP_WANTED_QUESTION.getString() );
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

		i18nCompositeGridDataFactory = GridDataFactory.fillDefaults()
			.span(COL_COUNT, 1)
			.grab(true, false)
			.indent(SWT.DEFAULT, 10);


		buildMembershipWantedQuestionEditor(parent);
		buildI18NComposite(parent);
		buildMembershipWantedInfoEditor(parent);
	}


	private void buildMembershipWantedQuestionEditor(Composite parent) {
		Label label = new Label(parent, SWT.RIGHT);
		htmlEditorLabelGridDataFactory.applyTo(label);
		label.setText( IspadMembershipComponent.MEMBERSHIP_WANTED_QUESTION.getString() );
		SWTHelper.makeBold(label);

		membershipWantedQuestionWidget = new I18NHtmlWidget(parent, SWT.BORDER, languageList);
		htmlEditorGridDataFactory.applyTo(membershipWantedQuestionWidget);
		membershipWantedQuestionWidget.addModifyListener(modifySupport);
	}


	private void buildI18NComposite(Composite parent) {
		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new IspadMembershipWantedI18NWidgetController());
		i18nCompositeGridDataFactory.applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}


	private void buildMembershipWantedInfoEditor(Composite parent) {
		Label label = new Label(parent, SWT.RIGHT);
		htmlEditorLabelGridDataFactory.applyTo(label);
		label.setText( IspadMembershipComponent.MEMBERSHIP_WANTED_INFO.getString() );
		SWTHelper.makeBold(label);

		membershipWantedInfoWidget = new I18NHtmlWidget(parent, SWT.BORDER, languageList);
		htmlEditorGridDataFactory.applyTo(membershipWantedInfoWidget);
		membershipWantedInfoWidget.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		membershipWantedQuestionWidget.setLanguageString( entity.getMembershipWantedQuestion() );
		i18nComposite.setEntity(entity);
		membershipWantedInfoWidget.setLanguageString( entity.getMembershipWantedInfo() );
	}


	@Override
	public void syncEntityToWidgets() {
		entity.setMembershipWantedQuestion( membershipWantedQuestionWidget.getLanguageString() );
		i18nComposite.syncEntityToWidgets();
		entity.setMembershipWantedInfo( membershipWantedInfoWidget.getLanguageString() );
	}

}
