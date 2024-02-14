package com.lambdalogic.util.rcp.html;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;

public class HtmlWidget extends Composite {

	private ModifySupport modifySupport = new ModifySupport(this);
	private String html;

	private int style;
	private Browser browser;
	private Composite buttonComposite;
	private Button editButton;



	public HtmlWidget(Composite parent, int style) {
		super(parent, style);

		this.style = style;

		GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		createWidgets();
	}


	private void createWidgets() {
		createBrowser();
		createEditButton();
	}


	private void createBrowser() {
		browser = BrowserFactory.createBrowser(this, style);
		browser.setLayoutData( new GridData(GridData.FILL_BOTH) );
		browser.setText("<p/>");
	}


	private void createEditButton() {
		buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );
		GridLayout buttonLayout = new GridLayout(1, false);
		buttonLayout.horizontalSpacing = 0;
		buttonLayout.verticalSpacing = 0;
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonLayout);

		editButton = new Button(buttonComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(editButton);
		editButton.setText(UtilI18N.Edit);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HtmlEditorDialog htmlEditorDialog = new HtmlEditorDialog( getShell() );
				htmlEditorDialog.setHtml(html);
				int result = htmlEditorDialog.open();
				if (result == 0) {
					String editedHtml = htmlEditorDialog.getHtml();

					if ( ! editedHtml.equals(html)) {
						setHtml(editedHtml);
						modifySupport.fire();
					}
				}
			}
		});
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		editButton.setEnabled(enabled);
	}


	public String getHtml() {
		return html;
	}


	public void setHtml(String html) {
		this.html = html;
		browser.setText( avoidNull(html) );
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}

}
