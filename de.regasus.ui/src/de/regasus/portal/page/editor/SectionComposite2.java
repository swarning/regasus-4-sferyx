package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NHtmlEditor;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Page;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.Section;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.FieldComponent;
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;


public class SectionComposite2 extends Composite {

	private static final int COL_COUNT = 2;

	// the entity
	private Section section;

	protected ModifySupport modifySupport = new ModifySupport(this);

	private Page page;
	private List<Language> languageList;

	private final boolean expertMode;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private Button companionContextButton;
	private I18NHtmlEditor headerEditor;
	private MultiLineText visibleConditionText;
	private MultiLineText visibleConditionDescriptionText;

	// *
	// * Widgets
	// **************************************************************************


	public SectionComposite2(Composite parent, int style, Page page) throws Exception {
		super(parent, style);

		this.page = Objects.requireNonNull(page);

		// load Portal to get Event and Languages
		Portal portal = PortalModel.getInstance().getPortal( page.getPortalId() );
		List<String> languageIds = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageIds);

		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		createWidgets();
	}


	private void createWidgets() {
		setLayout( new GridLayout(COL_COUNT, false) );

		buildTypeLabel();

		if (expertMode) {
			buildHtmlId();
		}

		if (page.isCompanionContext()) {
			buildCompanionContext();
		}

		buildHeader();

		if (expertMode) {
			buildVisibleConditionGroupForExperts();
		}
		else {
			buildVisibleConditionGroupForNormalUsers();
		}
	}


	private void buildTypeLabel() {

		GridDataFactory gridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.LEFT, SWT.CENTER)
			.grab(true, false)
			.span(COL_COUNT, 1);

		Font font = com.lambdalogic.util.rcp.Activator.getDefault().getFontFromRegistry(com.lambdalogic.util.rcp.Activator.BIG_FONT);

		Label typeLabel = new Label(this, SWT.NONE);
		gridDataFactory.applyTo(typeLabel);
		typeLabel.setFont(font);
		typeLabel.setText( PortalI18N.Section.getString() );

		Label distanceLabel = new Label(this, SWT.NONE);
		gridDataFactory.applyTo(distanceLabel);
	}


	private void buildHtmlId() {
		SWTHelper.createLabel(this, Component.HTML_ID.getString(), true);

		htmlIdText = new Text(this, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(htmlIdText);
		SWTHelper.makeBold(htmlIdText);
		htmlIdText.setTextLimit( Component.HTML_ID.getMaxLength() );
		htmlIdText.addModifyListener(modifySupport);
	}


	private void buildCompanionContext() {
		new Label(this, SWT.NONE); // placeholder

		companionContextButton = new Button(this, SWT.CHECK);
		GridDataFactory.swtDefaults().applyTo(companionContextButton);
		companionContextButton.setText( Section.COMPANION_CONTEXT.getString() );
		companionContextButton.setToolTipText( Section.COMPANION_CONTEXT.getDescription() );
		companionContextButton.addSelectionListener(modifySupport);
	}


	private void buildHeader() {
		// vertical gap only if header is not the first widget
		int verticalIndent = 0;
		if (page.isCompanionContext() || expertMode) {
			verticalIndent = 10;
		}

		Label label = new Label(this, SWT.NONE);
		GridDataFactory
			.swtDefaults()
			.align(SWT.LEFT, SWT.CENTER)
			.span(COL_COUNT, 1)
			.indent(0, verticalIndent)
			.applyTo(label);

		label.setText(Page.ARTICLE_HEADER.getString());

		headerEditor = new I18NHtmlEditor(this, SWT.BORDER, languageList);
		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, true).applyTo(headerEditor);
		headerEditor.addModifyListener(modifySupport);
	}


	private void buildVisibleConditionGroupForExperts() {
		// GridDataFactory for conditions
		GridDataFactory conditionGroupGridDataFactory = GridDataFactory
			.fillDefaults()
			.grab(true, false)
			.span(COL_COUNT, 1)
			.indent(SWT.NONE, 20);

		GridDataFactory conditionLabelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.TOP)
			.indent(0, SWTConstants.VERTICAL_INDENT);

		GridDataFactory conditionGridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

		GridLayoutFactory conditionGroupLayoutFactory = GridLayoutFactory.swtDefaults().numColumns(2);


		Group group = new Group(this, SWT.NONE);
		conditionGroupGridDataFactory.applyTo(group);
		conditionGroupLayoutFactory.applyTo(group);
		group.setText(I18N.PageEditor_Visibility);

		Label descriptionLabel = new Label(group, SWT.NONE);
		descriptionLabel.setText(UtilI18N.Description);
		conditionLabelGridDataFactory.applyTo(descriptionLabel);

		visibleConditionDescriptionText = new MultiLineText(group, SWT.BORDER);
		conditionGridDataFactory.applyTo(visibleConditionDescriptionText);
		visibleConditionDescriptionText.setMinLineCount(2);
		visibleConditionDescriptionText.setTextLimit( FieldComponent.VISIBLE_CONDITION_DESCRIPTION.getMaxLength() );
		visibleConditionDescriptionText.addModifyListener(modifySupport);


		Label conditionLabel = new Label(group, SWT.NONE);
		conditionLabel.setText(I18N.PageEditor_Condition);
		conditionLabelGridDataFactory.applyTo(conditionLabel);

		visibleConditionText = new MultiLineText(group, SWT.BORDER);
		conditionGridDataFactory.applyTo(visibleConditionText);
		visibleConditionText.setMinLineCount(2);
		visibleConditionText.setTextLimit( FieldComponent.VISIBLE_CONDITION.getMaxLength() );
		visibleConditionText.addModifyListener(modifySupport);
	}


	private void buildVisibleConditionGroupForNormalUsers() {
		// GridDataFactory for conditions
		GridDataFactory conditionGroupGridDataFactory = GridDataFactory
			.fillDefaults()
			.grab(true, false)
			.span(COL_COUNT, 1)
			.indent(SWT.NONE, 20);

		GridDataFactory conditionLabelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.TOP)
			.indent(0, SWTConstants.VERTICAL_INDENT);

		GridDataFactory conditionGridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

		GridLayoutFactory conditionGroupLayoutFactory = GridLayoutFactory.swtDefaults().numColumns(2);


		Group group = new Group(this, SWT.NONE);
		conditionGroupGridDataFactory.applyTo(group);
		conditionGroupLayoutFactory.applyTo(group);
		group.setText(I18N.PageEditor_Visibility);

		Label descriptionLabel = new Label(group, SWT.NONE);
		descriptionLabel.setText(UtilI18N.Description);
		conditionLabelGridDataFactory.applyTo(descriptionLabel);

		visibleConditionDescriptionText = new MultiLineText(group, SWT.BORDER);
		conditionGridDataFactory.applyTo(visibleConditionDescriptionText);
		visibleConditionDescriptionText.setMinLineCount(2);

		SWTHelper.disableTextWidget(visibleConditionDescriptionText);
	}


	private void syncWidgetsToEntity() {
		if (section != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					modifySupport.setEnabled(false);
					try {
						if (expertMode) {
							htmlIdText.setText( avoidNull(section.getHtmlId()) );
						}

						if (page.isCompanionContext()) {
							companionContextButton.setSelection( section.isCompanionContext() );
						}

						headerEditor.setLanguageString( section.getHeader() );

						if (expertMode) {
    						visibleConditionText.setText( avoidNull(section.getVisibleCondition()) );
						}
						visibleConditionDescriptionText.setText( avoidNull(section.getVisibleConditionDescription()) );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	public Section getSection() {
		return section;
	}


	public void setSection(Section section) {
		this.section = section;
		syncWidgetsToEntity();
	}


	public void syncEntityToWidgets() {
		if (section != null) {
			if (expertMode) {
				section.setHtmlId( htmlIdText.getText() );
			}

			if (page.isCompanionContext()) {
				section.setCompanionContext( companionContextButton.getSelection() );
			}

			section.setHeader( headerEditor.getLanguageString() );

			if (expertMode) {
				section.setVisibleCondition( visibleConditionText.getText() );
				section.setVisibleConditionDescription( visibleConditionDescriptionText.getText() );
			}
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

}
