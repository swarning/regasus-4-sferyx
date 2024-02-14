package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.SummaryComponent;
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;

public class SummaryComponentComposite extends Composite {

	private static final int COL_COUNT = 2;

	private final boolean expertMode;

	// the entity
	private SummaryComponent component;

	protected ModifySupport modifySupport = new ModifySupport(this);

	private Portal portal;
	private List<Language> languageList;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private Text renderText;
	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public SummaryComponentComposite(Composite parent, int style, Long portalPK) throws Exception {
		super(parent, style);

		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		// load Portal to get Event and Languages
		Objects.requireNonNull(portalPK);
		this.portal = PortalModel.getInstance().getPortal(portalPK);
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);

		createWidgets();
	}


	private void createWidgets() {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(this, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.SummaryComponent.getString() );
		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
			renderText = widgetBuilder.buildRender();
		}

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
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

    						visibleConditionGroup.setCondition( component.getVisibleCondition() );
    						visibleConditionGroup.setDescription( component.getVisibleConditionDescription() );
						}
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

			component.setVisibleCondition( visibleConditionGroup.getCondition() );
			component.setVisibleConditionDescription( visibleConditionGroup.getDescription() );
		}
	}


	public SummaryComponent getComponent() {
		return component;
	}


	public void setComponent(SummaryComponent summaryComponent) {
		this.component = summaryComponent;
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
