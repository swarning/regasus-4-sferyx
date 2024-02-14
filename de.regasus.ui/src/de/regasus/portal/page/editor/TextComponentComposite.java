package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.i18n.I18NHtmlEditor;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.TextComponent;
import de.regasus.users.CurrentUserModel;

public class TextComponentComposite extends EntityComposite<TextComponent> {

	private static final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private boolean expertMode;

	private Portal portal;

	private List<Language> languageList;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private Text renderText;
	private NullableSpinner numberOfLinesSpinner;

	private I18NHtmlEditor textEditor;
	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public TextComponentComposite(Composite parent, int style, Long portalPK)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		Long portalPK = (Long) initValues[0];

		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		// load Portal to get Event and Languages
		Objects.requireNonNull(portalPK);
		portal = PortalModel.getInstance().getPortal(portalPK);
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.TextComponent.getString() );

		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
			renderText = widgetBuilder.buildRender();
		}
		buildNumberOfLines(parent);

		Label label = new Label(parent, SWT.LEFT);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).span(COL_COUNT, 1).indent(SWT.DEFAULT, 10).applyTo(label);
		label.setText( TextComponent.TEXT.getString() );
		SWTHelper.makeBold(label);

		// variant 1: without Sash
//		{
//    		buildText(parent);
//    		GridDataFactory
//    			.fillDefaults()
//    			.grab(true, true)
//    			.span(COL_COUNT, 1)
//    			.applyTo(textEditor);
//
//
//    		buildConditionGroup(parent);
//    		GridDataFactory
//    			.fillDefaults()
//    			.grab(true, false)
//    			.span(COL_COUNT, 1)
//    			.indent(SWT.NONE, 10)
//    			.applyTo(visibleConditionGroup);
//		}

		// variant 2: using a Sash
		{
    		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
    		GridDataFactory
    			.swtDefaults()
    			.align(SWT.FILL, SWT.FILL)
    			.grab(true, true)
    			.span(COL_COUNT, 1)
    			.applyTo(sashForm);

    		buildText(sashForm);
    		buildConditionGroup(sashForm);

    		sashForm.setWeights(5, 2);
		}
	}


	private void buildNumberOfLines(Composite parent) {
		SWTHelper.createLabel(parent, TextComponent.TEXT_BLOCK_HEIGHT.getString(), false);

		numberOfLinesSpinner = new NullableSpinner(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(numberOfLinesSpinner);
		numberOfLinesSpinner.setMinimum(1);
		numberOfLinesSpinner.setMaximum(1000);
		WidgetSizer.setWidth(numberOfLinesSpinner);
		numberOfLinesSpinner.addModifyListener(modifySupport);
	}


	private void buildText(Composite parent) {
		textEditor = new I18NHtmlEditor(parent, SWT.BORDER, languageList);
		textEditor.addModifyListener(modifySupport);
	}


	private void buildConditionGroup(Composite parent) {
		visibleConditionGroup = new ConditionGroup(parent, SWT.NONE, false /*showYesIfNotNewButton*/, portal);
		visibleConditionGroup.setText(I18N.PageEditor_Visibility);
		visibleConditionGroup.addModifyListener(modifySupport);
		visibleConditionGroup.setDefaultCondition(true);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
			renderText.setText( avoidNull(entity.getRender()) );
		}

		numberOfLinesSpinner.setValue( entity.getTextBlockHight() );
		textEditor.setLanguageString( entity.getText() );

		visibleConditionGroup.setCondition( entity.getVisibleCondition() );
		visibleConditionGroup.setDescription( entity.getVisibleConditionDescription() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (expertMode) {
				entity.setHtmlId( htmlIdText.getText() );
				entity.setRender( renderText.getText() );
			}

			entity.setTextBlockHight( numberOfLinesSpinner.getValueAsInteger() );
			entity.setText( textEditor.getLanguageString() );

			entity.setVisibleCondition( visibleConditionGroup.getCondition() );
			entity.setVisibleConditionDescription( visibleConditionGroup.getDescription() );
		}
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
			htmlIdText.setEnabled(!fixedStructure);
			renderText.setEnabled(!fixedStructure);
		}
	}

}
