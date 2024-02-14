package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NHtmlEditor;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.FileComponent;
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;

public class FileComponentComposite extends Composite {

	private final int COL_COUNT = 2;

	private final boolean expertMode;

	// the entity
	private FileComponent component;

	protected ModifySupport modifySupport = new ModifySupport(this);

	private Portal portal;
	private List<Language> languageList;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private Text renderText;
	private PortalFileMnemonicCombo fileMnemonicCombo;
	private I18NHtmlEditor labelEditor;
	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public FileComponentComposite(
		Composite parent,
		int style,
		Long portalPK
	)
	throws Exception {
		super(parent, style);

		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		// load Portal to get Languages
		Objects.requireNonNull(portalPK);
		this.portal = PortalModel.getInstance().getPortal(portalPK);
		List<String> languageIds = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageIds);

		createWidgets();
	}


	private void createWidgets() throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(this, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.FileComponent.getString());
		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
			renderText = widgetBuilder.buildRender();
		}

		buildFileMnemonic();
		buildLabel();

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
	}


	private void buildFileMnemonic() throws Exception {
		SWTHelper.createLabel(this, UtilI18N.File);

		fileMnemonicCombo = new PortalFileMnemonicCombo(this, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fileMnemonicCombo);
		fileMnemonicCombo.setPortalId( portal.getId() );
		fileMnemonicCombo.addModifyListener(modifySupport);
	}


	private void buildLabel() {
		Label label = new Label(this, SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).span(COL_COUNT, 1).indent(SWT.DEFAULT, 10).applyTo(label);
		SWTHelper.makeBold(label);
		label.setText( FileComponent.LABEL.getString() );

		labelEditor = new I18NHtmlEditor(this, SWT.BORDER, languageList);
		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, true).applyTo(labelEditor);
		labelEditor.addModifyListener(modifySupport);
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					modifySupport.setEnabled(false);
					try {
						if (expertMode) {
    						htmlIdText.setText( avoidNull(component.getHtmlId()) );
    						renderText.setText( avoidNull(component.getRender()) );
						}

						fileMnemonicCombo.setMnemonic( component.getFileMnemonic() );
						labelEditor.setLanguageString( component.getLabel() );

						visibleConditionGroup.setCondition( component.getVisibleCondition() );
						visibleConditionGroup.setDescription( component.getVisibleConditionDescription() );
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
		if (component != null) {
			if (expertMode) {
    			component.setHtmlId( htmlIdText.getText() );
    			component.setRender( renderText.getText() );
			}

			component.setFileMnemonic( fileMnemonicCombo.getMnemonic() );
			component.setLabel( labelEditor.getLanguageString() );

			component.setVisibleCondition( visibleConditionGroup.getCondition() );
			component.setVisibleConditionDescription( visibleConditionGroup.getDescription() );
		}
	}


	public FileComponent getComponent() {
		return component;
	}


	public void setComponent(FileComponent fileComponent) {
		this.component = fileComponent;
		syncWidgetsToEntity();
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
			htmlIdText.setEnabled(!fixedStructure);
			renderText.setEnabled(!fixedStructure);
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
