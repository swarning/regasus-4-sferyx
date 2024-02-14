package de.regasus.core.ui.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.FilteredList;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

/**
 * A variant of the JFace dialog to select one element from a list, where you can enter a filter in a text that makes
 * the list shrink.
 * <p>
 * In contrast to the JFace behaviour: When the focus in the text widget, it's content serves as entry; when the focus
 * is in the list, the single selected element serves as entry. And when you enter a filter so that nothing gets
 * selected, the contents of the filter also serves as entry.
 * 
 * 
 * @author manfred
 * 
 */
public class EntryOrSelectionDialog extends ElementListSelectionDialog {

	/**
	 * Must be kept in an attribute, otherwise you cannot ask for the selection after the dialog is closed
	 */
	protected String entryOrSelection = "";

	/**
	 * An additional reference to the text widget that is actually created in a JFace superclass, put declared
	 * as private.
	 */
	private Text text;


	public EntryOrSelectionDialog(Shell parent) {
		super(parent, new LabelProvider());
		setMultipleSelection(false);
	}


	/**
	 * Called by some superclass methods
	 */
	@Override
	protected void updateButtonsEnableState(IStatus status) {
		setSelectionOrEntry();
	}


	/**
	 * You can press OK when there is a selection or an entry, which is found out by {@link #setSelectionOrEntry()}
	 */
	@Override
	protected void updateOkState() {
		Button okButton = getOkButton();
		if (okButton != null) {
			okButton.setEnabled(entryOrSelection.trim().length() > 0);
		}
	}


	/**
	 * After the list is created in a superclass, a listener is added to handle list selections
	 */
	@Override
	protected FilteredList createFilteredList(Composite parent) {

		final FilteredList list = super.createFilteredList(parent);

		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setSelectionOrEntry();
			}
		});

		return list;
	}


	/**
	 * After the text is created in a superclass, a listener is added to update the selection when the text gains or
	 * looses focus.
	 */
	@Override
	protected Text createFilterText(Composite parent) {
		text = super.createFilterText(parent);

		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				setSelectionOrEntry();
			}


			@Override
			public void focusLost(FocusEvent e) {
				setSelectionOrEntry();
			}
		});
		return text;
	}


	public Object getSelectionOrEntry() {
		return entryOrSelection;
	}


	/**
	 * Use as entry what is contained in the filter text widget, if that is focussed, otherwise use what is selected in
	 * the list entry.
	 */
	protected void setSelectionOrEntry() {
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			public void run() {
				Object[] selection = fFilteredList.getSelection();
				if (selection != null && selection.length > 0 && !text.isFocusControl()) {
					entryOrSelection = StringHelper.avoidNull(selection[0]);
				}
				else {
					entryOrSelection = fFilteredList.getFilter();
				}
				updateOkState();
			}
		});
	}
}
