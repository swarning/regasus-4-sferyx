package de.regasus.portal.pagelayout.editor;

import static com.lambdalogic.util.StringHelper.*;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.ControlFinder;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dnd.CopyPasteButtonComposite;
import de.regasus.portal.PageLayoutLink;
import de.regasus.ui.Activator;


public class PageLayoutLinkComposite extends Composite {

	// the entity
	private PageLayoutLink pageLayoutLink;

	private Long portalId;

	private List<Language> languageList;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private I18NComposite<PageLayoutLink> i18nComposite;

	private Text htmlIdText;
	private Button copyHtmlIdButton;
	private MultiLineText conditionText;
	private Button openInNewTabButton;

	// *
	// * Widgets
	// **************************************************************************


	private Object currentFocusData = null;


	private Label htmlIdLabel;
	private Color defaultColor;

	private Label conditionLabel;
	private Color focusColor;


	private FocusListener focusListener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			setBackground(defaultColor);
			currentFocusData = null;
		}

		@Override
		public void focusGained(FocusEvent e) {
			setBackground(focusColor);
			currentFocusData = e.widget.getData();
		}
	};


	public PageLayoutLinkComposite(Composite parent, int style, List<String> languageIds, Long portalId) {
		super(parent, style);

		this.portalId = portalId;

		try {
			languageList = LanguageModel.getInstance().getLanguages(languageIds);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		initColors();
		createWidgets();
	}


	private void initColors() {
		defaultColor = getBackground();

		// make focus background color 15% darker
		float[] defaultHSB = defaultColor.getRGB().getHSB();
		RGB focusRGB =   new RGB(defaultHSB[0], defaultHSB[1], defaultHSB[2] * 0.85f);
		focusColor = new Color(getDisplay(), focusRGB);
	}


	@Override
	public void setBackground(Color color) {
		super.setBackground(color);

		// on Windows these Labels do not inherit the background color of their parent when it changes
		if (htmlIdLabel != null) {
    		htmlIdLabel.setBackground(color);
    		conditionLabel.setBackground(color);
		}
	}


	private void createWidgets() {
		final int COL_COUNT = 3;
		setLayout( new GridLayout(COL_COUNT, false) );

		/****** Row 1 ******/
		i18nComposite = new I18NComposite<>(this, SWT.BORDER, languageList, new PageLayoutLinkI18NWidgetController(portalId));
		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, false).applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);


		/****** Row 2 ******/

		/*** htmlId ***/
		htmlIdLabel = SWTHelper.createLabel(this, PageLayoutLink.HTML_ID.getString(), false);
		SWTHelper.setBold(htmlIdLabel, true);

		htmlIdText = new Text(this, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(htmlIdText);
		htmlIdText.setTextLimit( PageLayoutLink.HTML_ID.getMaxLength() );
		htmlIdText.addModifyListener(modifySupport);
		htmlIdText.setData(PageLayoutLink.HTML_ID);
		
		copyHtmlIdButton = CopyPasteButtonComposite.createCopyButton(this);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.FILL).applyTo(copyHtmlIdButton);
		copyHtmlIdButton.addSelectionListener(copyHtmlListener);


		/****** Row 3 ******/

		/*** condition ***/
		conditionLabel = new Label(this, SWT.NONE);
		conditionLabel.setText( PageLayoutLink.CONDITION.getString() );
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.TOP).indent(0, SWTConstants.VERTICAL_INDENT).applyTo(conditionLabel);

		conditionText = new MultiLineText(this, SWT.BORDER);
		conditionText.setMinLineCount(2);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(conditionText);
		conditionText.setTextLimit( PageLayoutLink.CONDITION.getMaxLength() );
		conditionText.addModifyListener(modifySupport);
		conditionText.setData(PageLayoutLink.CONDITION);
		
		/*** open in new tab ***/
		new Label(this, SWT.NONE);
		openInNewTabButton = new Button(this, SWT.CHECK);
		openInNewTabButton.setText( PageLayoutLink.OPEN_IN_NEW_TAB.getLabel() );
		openInNewTabButton.addSelectionListener(modifySupport);
		GridDataFactory.fillDefaults().span(COL_COUNT - 1, 1).applyTo(openInNewTabButton);

		setBackground(defaultColor);

		addFocusListener(focusListener);
	}
	
	
	private SelectionListener copyHtmlListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
			try {
				ClipboardHelper.copyToClipboard("${" + pageLayoutLink.getHtmlId() + "}");
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		};
	};


	public PageLayoutLink getPageLayoutLink() {
		return pageLayoutLink;
	}


	public void setPageLayoutLink(PageLayoutLink pageLayoutLink) {
		this.pageLayoutLink = pageLayoutLink;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (pageLayoutLink != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						i18nComposite.setEntity(pageLayoutLink);

						htmlIdText.setText( avoidNull(pageLayoutLink.getHtmlId()) );
						conditionText.setText( avoidNull(pageLayoutLink.getCondition()) );
						openInNewTabButton.setSelection( pageLayoutLink.isOpenInNewTab() );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (pageLayoutLink != null) {
			i18nComposite.syncEntityToWidgets();

			pageLayoutLink.setHtmlId( trim(htmlIdText.getText()) );
			pageLayoutLink.setCondition( trim(conditionText.getText()) );
			pageLayoutLink.setOpenInNewTab( openInNewTabButton.getSelection() );
		}
	}


	public Object getFocusData() {
		return currentFocusData;
	}


	public void setFocus(Object focusData) {
		Control control = ControlFinder.findControl(this, focusData);
		if (control != null) {
			control.setFocus();
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		/* Instead of observing the focus of this Composite (which can never hold the focus),
		 * observe the focus of its widgets.
		 */

		i18nComposite.addFocusListener(listener);

		htmlIdText.addFocusListener(listener);
		conditionText.addFocusListener(listener);
		openInNewTabButton.addFocusListener(listener);
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


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
