package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NHtmlEditor;
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
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;


public class SectionComposite extends Composite {

	private static final int COL_COUNT = 2;

	private final boolean expertMode;

	// the entity
	private Section section;

	private ModifySupport modifySupport = new ModifySupport(this);

	private Page page;
	private Portal portal;
	private List<Language> languageList;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private Button companionContextButton;
	private Button showFrameButton;
	private I18NHtmlEditor headerEditor;
	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public SectionComposite(Composite parent, int style, Page page) throws Exception {
		super(parent, style);

		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		this.page = Objects.requireNonNull(page);

		// load Portal to get Event and Languages
		this.portal = PortalModel.getInstance().getPortal( page.getPortalId() );
		List<String> languageIds = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageIds);

		createWidgets();
	}


	private void createWidgets() {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(this, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.Section.getString() );
		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
		}

		if (page.isCompanionContext()) {
			buildCompanionContext();
		}

		buildShowFrame();

		buildHeader();

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
	}


	private void buildCompanionContext() {
		new Label(this, SWT.NONE); // placeholder

		companionContextButton = new Button(this, SWT.CHECK);
		GridDataFactory.swtDefaults().applyTo(companionContextButton);
		companionContextButton.setText( Section.COMPANION_CONTEXT.getString() );
		companionContextButton.setToolTipText( Section.COMPANION_CONTEXT.getDescription() );
		companionContextButton.addSelectionListener(modifySupport);
	}


	private void buildShowFrame() {
		new Label(this, SWT.NONE); // placeholder

		showFrameButton = new Button(this, SWT.CHECK);
		GridDataFactory.swtDefaults().applyTo(showFrameButton);
		showFrameButton.setText( Section.SHOW_FRAME.getString() );
		showFrameButton.setToolTipText( Section.SHOW_FRAME.getDescription() );
		showFrameButton.addSelectionListener(modifySupport);
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

						showFrameButton.setSelection( section.isShowFrame() );

						headerEditor.setLanguageString( section.getHeader() );

						visibleConditionGroup.setCondition( section.getVisibleCondition() );
						visibleConditionGroup.setDescription( section.getVisibleConditionDescription() );
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


	public void syncEntityToWidgets() {
		if (section != null) {
			if (expertMode) {
				section.setHtmlId( htmlIdText.getText() );
			}

			if (page.isCompanionContext()) {
				section.setCompanionContext( companionContextButton.getSelection() );
			}

			section.setShowFrame( showFrameButton.getSelection() );

			section.setHeader( headerEditor.getLanguageString() );

			section.setVisibleCondition( visibleConditionGroup.getCondition() );
			section.setVisibleConditionDescription( visibleConditionGroup.getDescription() );
		}
	}


	public Section getSection() {
		return section;
	}


	public void setSection(Section section) {
		this.section = section;
		syncWidgetsToEntity();
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
			htmlIdText.setEnabled(!fixedStructure);
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
