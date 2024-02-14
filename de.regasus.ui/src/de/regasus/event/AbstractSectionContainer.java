package de.regasus.event;

import static com.lambdalogic.util.rcp.widget.SWTHelper.prepareLabelText;

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.lambdalogic.time.I18NTemporal;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

/**
 * Abstract class that supports the usage of the Eclipse Form Toolkit Framework.
 *
 */
public abstract class AbstractSectionContainer {

	private Composite body;
	protected FormToolkit formToolkit;

	private Section section;
	protected Composite sectionComposite;

	/**
	 * Helper for formatting values
	 */
	protected FormatHelper formatHelper;

	/**
	 * A reference to the default bold font as obtained from the FontRegistry
	 */
	private Font boldFont;

	private int sectionColumns;


	protected AbstractSectionContainer(FormToolkit formToolkit, Composite body) {
		this(formToolkit, body, 2);
	}


	protected AbstractSectionContainer(FormToolkit formToolkit, Composite body, int sectionColumns) {
		this.formToolkit = formToolkit;
		this.body = body;
		this.sectionColumns = sectionColumns;

		// initialize other stuff
		formatHelper = new FormatHelper();

		boldFont = com.lambdalogic.util.rcp.Activator.getDefault().getFontFromRegistry(
			com.lambdalogic.util.rcp.Activator.DEFAULT_FONT_BOLD
		);
	}


	/**
	 * Return the section title.
	 * @return
	 */
	protected abstract String getTitle();


	/**
	 * Create all elements in the section.
	 * If necessary control the visibility of the section by calling setVisible().
	 *
	 * @throws Exception
	 */
	protected abstract void createSectionElements() throws Exception;


	public void addDisposeListener(DisposeListener listener) {
		body.addDisposeListener(listener);
	}


	/**
	 * Rebuild the content of the section.
	 * @throws Exception
	 */
	protected void refreshSection() throws Exception {
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (section == null) {
						// When called the first time, the section is created.
						section = createSection(getTitle());
						sectionComposite = (Composite) section.getClient();
					}
					else {
						// On all further calls existing elements are removed.
						for (Control control : sectionComposite.getChildren()) {
							control.dispose();
						}
					}

					// let a sub-class create the elements
					createSectionElements();

					// refresh the layout, because the number of elements in a section could have changed
					body.layout();
					section.layout();
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	protected void setVisible(boolean visible) {
		section.setVisible(visible);

		/* Set ColumnLayoutData to make the section invisible by setting its height to 0.
		 * To make the section visible just remove the LayoutData, what is the default.
		 */
		Object layoutData = null;
		if ( ! visible) {
			layoutData = new ColumnLayoutData(0, 0);
		}
		section.setLayoutData(layoutData);
	}


	/**
	 * Creates a section which is an expandable/foldable area with a header and an optional description, and returns a
	 * composite which contains that what belongs to that section.
	 *
	 */
	protected Section createSection(String sectionTitle) {
		Section section = formToolkit.createSection(body, Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED);
		section.setText(sectionTitle);
		Composite sectionClient = formToolkit.createComposite(section);
		section.setClient(sectionClient);

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = sectionColumns;
		sectionClient.setLayout(layout);

		return section;
	}


	/**
	 * Creates a label-yes row in the section if the flag is true.
	 */
	protected void addIfYes(String key, boolean flag) {
		if (flag) {
			createEntry(key, UtilI18N.Yes);
		}
	}


	/**
	 * Creates a label-text row in the section if the given number is not null.
	 */
	protected void addIfNotEmpty(String key, Integer number) {
		if (number != null) {
			createEntry(key, formatHelper.formatInteger(number));
		}
	}


	/**
	 * Creates a label-text row in the section if the given text contains at least one character.
	 */
	protected void addIfNotEmpty(String key, String text) {
		if (text != null && text.length() > 0) {
			createEntry(key, text);
		}
	}


	/**
	 * Creates a label-text row in the section if the given text contains at least one character.
	 */
	protected void addIfNotEmpty(String key, I18NTemporal temporal) {
		if (temporal != null) {
			createEntry(key, temporal.formatDefault());
		}
	}


	/**
	 * Creates a section which is an expandable/foldable area with a header and an optional description, and returns a
	 * composite which contains that what belongs to that section.
	 *
	 */
	protected Composite createOneColSection(String sectionTitle) {
		Section section = formToolkit.createSection(body, Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED);
		section.setText(sectionTitle);
		Composite sectionClient = formToolkit.createComposite(section);
		section.setClient(sectionClient);

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 1;
		sectionClient.setLayout(layout);

		return sectionClient;
	}


	protected void createEntry(String leftText, String rightText) {
		Objects.requireNonNull(leftText);

		if (rightText == null) {
			rightText = "";
		}

		/*
		 * https://download.eclipse.org/rt/rap/doc/2.2/guide/reference/api/org/eclipse/swt/widgets/Label.html#setText(java.lang.String)
		 *
		 * This method sets the widget label. The label may include the mnemonic character and line delimiters.
		 * Mnemonics are indicated by an '&' that causes the next character to be the mnemonic.
		 * When the user presses a key sequence that matches the mnemonic, focus is assigned to the control that
		 * follows the label. On most platforms, the mnemonic appears underlined but may be emphasised in a platform
		 * specific manner. The mnemonic indicator character '&' can be escaped by doubling it in the string, causing
		 * a single '&' to be displayed.
		 */

		// duplicate &
		leftText = prepareLabelText(leftText);
		rightText = prepareLabelText(rightText);

		Label leftLabel = formToolkit.createLabel(sectionComposite, leftText + ":", SWT.RIGHT);
		leftLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		Label rightLabel = formToolkit.createLabel(sectionComposite, rightText, SWT.LEFT);
		rightLabel.setFont(boldFont);
		rightLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
	}


	protected void createOneColEntry(String text) {
		Label leftLabel = formToolkit.createLabel(sectionComposite, text);
		leftLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}

}
