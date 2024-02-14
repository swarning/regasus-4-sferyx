package de.regasus.onlineform.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;

import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.onlineform.RegistrationFormConfigModel;


public class PreviewTabComposite extends Composite implements SelectionListener {

	private static final String PAGE = "page";

	// the entity
	private RegistrationFormConfig registrationFormConfig;

	@SuppressWarnings("unused")
	private Button beforePeriodPageButton;

	@SuppressWarnings("unused")
	private Button startPageButton;

	@SuppressWarnings("unused")
	private Button personalPageButton;

	private Button bookingPageButton;

	private Button companionPageButton;

	private Button travelPageButton;

	private Button hotelPageButton;

	@SuppressWarnings("unused")
	private Button summaryPageButton;

	private Button paymentPageButton;

	@SuppressWarnings("unused")
	private Button endPageButton;

	@SuppressWarnings("unused")
	private Button afterPeriodPageButton;

	private Browser browser;

	private String currentPage = "begin";

	private Button refreshButton;

	public PreviewTabComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(2, false));

		// ==================================================

		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttonComposite.setLayout(new GridLayout(1, false));


		beforePeriodPageButton = createButton(buttonComposite, OnlineFormI18N.beforeRegistrationPeriod, "beforeRegistrationPeriod");
		startPageButton = createButton(buttonComposite, OnlineFormI18N.startPage, "begin");
		personalPageButton = createButton(buttonComposite, OnlineFormI18N.personalPage, "personal");
		bookingPageButton  = createButton(buttonComposite, OnlineFormI18N.bookingPage, "bookings");
		companionPageButton = createButton(buttonComposite, OnlineFormI18N.companionPage, "companion");

		travelPageButton = createButton(buttonComposite, OnlineFormI18N.travelPage, "travel");
		hotelPageButton = createButton(buttonComposite, OnlineFormI18N.hotelPage, "hotel");
		summaryPageButton = createButton(buttonComposite, OnlineFormI18N.summaryPage, "summary");
		paymentPageButton = createButton(buttonComposite, OnlineFormI18N.paymentPage, "payment");
		endPageButton = createButton(buttonComposite, OnlineFormI18N.endPage, "end");
		afterPeriodPageButton = createButton(buttonComposite, OnlineFormI18N.afterRegistrationPeriod, "afterRegistrationPeriod");

		refreshButton = new Button(buttonComposite, SWT.PUSH);
		refreshButton.setText(OnlineFormI18N.Refresh);
		refreshButton.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				previewCurrentPage();
			}
		});

	    // MIRCP-2998 - Use default browser for preview of online forms
		browser = new Browser(this, SWT.BORDER);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	}


	private Button createButton(Composite buttonComposite, String text, String path) {
		Button button = new Button(buttonComposite, SWT.PUSH);
		button.setText(text);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		button.addSelectionListener(this);
		button.setData(PAGE, path);
		return button;
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		Button pressedButton = (Button) e.widget;
		currentPage = (String) pressedButton.getData(PAGE);
		previewCurrentPage();
	}


	public void setRegistrationFormConfig(RegistrationFormConfig registrationFormConfig) {
		this.registrationFormConfig = registrationFormConfig;

		updateButtonStates(registrationFormConfig);
	}



	public void isMadeVisible() {
		updateButtonStates(registrationFormConfig);

		previewCurrentPage();
	}

	private void updateButtonStates(final RegistrationFormConfig registrationFormConfig) {

		Display.getDefault().syncExec(
			new Runnable() {
				@Override
				public void run() {
					companionPageButton.setEnabled(registrationFormConfig.isCompanionEnabled());
					travelPageButton.setEnabled(registrationFormConfig.isTravelEnabled());
					hotelPageButton.setEnabled(registrationFormConfig.isHotelEnabled());
					bookingPageButton.setEnabled(registrationFormConfig.isBookingEnabled());
					paymentPageButton.setEnabled(registrationFormConfig.isPaymentTypePageEnabled());
				}
			}
		);
	}



	protected void previewCurrentPage() {

		String webId = registrationFormConfig.getWebId();
		String url = RegistrationFormConfigModel.getInstance().getOnlineWebappUrl(webId);

		// two similar named variables ahead, do not confuse
		String previewUrl = url + "&preview=" + currentPage;

		String previousUrl = browser.getUrl();

		if (isUnconfiguredCurrentPage()) {
			browser.setUrl("about:blank");
		}
		else if (previousUrl != null && previousUrl.equals(previewUrl)) {
			browser.execute("window.location.reload()");
		} else {
			browser.setUrl(previewUrl);
		}
	}


	private boolean isUnconfiguredCurrentPage() {

		if ("hotel".equals(currentPage) && ! registrationFormConfig.isHotelEnabled()) {
			return true;
		}
		if ("travel".equals(currentPage) && ! registrationFormConfig.isTravelEnabled()) {
			return true;
		}
		if ("payment".equals(currentPage) && ! registrationFormConfig.isPaymentTypePageEnabled()) {
			return true;
		}
		if ("bookings".equals(currentPage) && ! registrationFormConfig.isBookingEnabled()) {
			return true;
		}

		return false;
	}

}
