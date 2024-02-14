package de.regasus.event.search;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


/**
 * See: https://lambdalogic.atlassian.net/wiki/spaces/REGASUS/pages/2513076233/Quick+Search
 */
public class QuickSearchField {
	
	/**
	 * The id of the Tool Control as used in fragment.e4xmi
	 */
	public static final String ID = "de.regasus.event.search.QuickSearchField";

	
	private Text text;
	
	/**
	 * One of two possible targets for the Quick Search.
	 */
	private SearchTarget searchTarget = SearchTarget.PARTICIPANT;
	
	
	@PostConstruct
	public void createGui(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		
		parent.setLayout(layout);

		Label label = new Label(parent, SWT.NONE);
		label.setText(I18N.QuickSearch);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		text = new Text(parent, SWT.SEARCH);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.widthHint = 80;
		text.setLayoutData(layoutData);
		parent.pack();
		
		text.addListener(SWT.KeyDown, new Listener()  {
			public void handleEvent(Event event) {
				if (event.character == '\r') {
					initiateSearch(event);
				}
			}
		});
		text.addListener(SWT.FocusIn, new Listener()  {
			public void handleEvent(Event event) {
				text.selectAll();
			}
		});
	}

	
	public void setFocus() {
		try {
			if (text != null && !text.isDisposed() && text.isEnabled()) {
				text.setFocus();
				text.selectAll();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}
	
	
	public boolean hasFocus() {
		return text.isFocusControl();
	}

	
	public String getText() {
		return text.getText();
	}
	
	
	public void setText(String s) {
		text.setText(s);
	}
	
	
	public SearchTarget getSearchTarget() {
		return searchTarget;
	}

	
	public void setSearchTarget(SearchTarget searchTarget) {
		this.searchTarget = searchTarget;
	}

	
	private void initiateSearch(Event event) {
		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
		
		try {
			switch (searchTarget) {
			case PARTICIPANT:
				handlerService.executeCommand(QuickParticipantSearchCommandHandler.COMMAND_ID, event);
				break;
			case PROFILE:
				handlerService.executeCommand(QuickProfileSearchCommandHandler.COMMAND_ID, event);
				break;
				
			default:
				break;
			}
		}
		catch (Exception ex) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
		}
	}

}
