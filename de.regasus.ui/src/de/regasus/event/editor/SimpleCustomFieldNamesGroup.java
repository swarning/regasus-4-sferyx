package de.regasus.event.editor;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


/**
 * Container for textComposite (with Text widgets and the moreButton).
 */
public class SimpleCustomFieldNamesGroup extends Group {

	/**
	 * The entity
	 */
	private EventVO eventVO;
	
	/**
	 * Modify support
	 */
	private ModifySupport modifySupport = new ModifySupport(this);

	/*** Widgets ***/
	private Composite textComposite; 
	private ArrayList<Text> textList;
	private int customFieldCount = 20;
	private Button moreButton; 

	
	public SimpleCustomFieldNamesGroup(Composite parent, int style) {
		super(parent, style);
		
		setText(ParticipantLabel.Event_CustonFieldNames.getString());
		setLayout(new GridLayout(1, false));

		// Container for the Text widgets
		textComposite = new Composite(this, SWT.NONE);
		textComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		textComposite.setLayout(new GridLayout(4, false));

		// Anzahl der benötigten Freifelder ermitteln
		customFieldCount = getRequiredCustomFieldCount(eventVO);

		textList = new ArrayList<Text>(customFieldCount);

		for (int i = 0; i < customFieldCount; i++) {
			Label label = new Label(textComposite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			label.setText(String.valueOf(i + 1));

			Text text = new Text(textComposite, SWT.BORDER);
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			text.addModifyListener(modifySupport);
			textList.add(text);
		}

		if (customFieldCount < EventVO.CUSTOM_FIELD_NAME_COUNT) {
			moreButton = new Button(this, SWT.PUSH);
			moreButton.setText(KernelLabel.More.getString());
			moreButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			moreButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					more();
				}
			});
		}

		syncWidgetsToEntity();
	}

	
	private boolean widgetsInitialized() {
		return textList != null;
	}
	
	
	private int getRequiredCustomFieldCount(EventVO eventVO) {
		int count = 20;
		// Anzahl der benötigten Freifelder ermitteln
		if (eventVO != null) {
			for (int i = 21; i <= EventVO.CUSTOM_FIELD_NAME_COUNT; i++) {
				String customFieldName = eventVO.getCustomFieldName(i);
				if (customFieldName != null) {
					count = i;
				}
			}
			
			// auf 10 aufrunden
			if (count % 10 != 0) {
				count = (count / 10) * 10 + 10;
			}
		}
		return count;
	}
	
	
	private void more() {
		if (widgetsInitialized() && customFieldCount < EventVO.CUSTOM_FIELD_NAME_COUNT) {
			// add 10 more custom fields
			customFieldCount += 10;
			
			
			for (int i = textList.size(); i < customFieldCount; i++) {
				// add a label
				Label label = new Label(textComposite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				label.setText(String.valueOf(i + 1));

				// add a text widget
				Text text = new Text(textComposite, SWT.BORDER);
				text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				text.addModifyListener(modifySupport);
				textList.add(text);
			}
			
			// remove the moreButton if no more custom fields are possible
			if (customFieldCount >= EventVO.CUSTOM_FIELD_NAME_COUNT) {
				moreButton.dispose();
				moreButton = null;
			}
			
			// layout this Group, compute its new dimension and set is to the scrolledComposite
			layout();
		}
	}
	
	
	public Control getLastControl() {
		Control lastControl = moreButton;
		if (lastControl == null) {
			lastControl = textList.get(textList.size() - 1);
		}
		return lastControl;
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

	
	public void addMoreButtonSelectionListener(SelectionListener listener) {
		if (moreButton != null) {
			moreButton.addSelectionListener(listener);
		}
	}

	// *
	// * Modifying
	// **************************************************************************

	private void syncWidgetsToEntity() {
		if (eventVO != null && widgetsInitialized()) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				public void run() {
					try {
						modifySupport.setEnabled(false);
						
						int i = 1;
						for (Text text : textList) {
							text.setText(StringHelper.avoidNull(eventVO.getCustomFieldName(i++)));	
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
		if (eventVO != null && widgetsInitialized()) {
			int i = 1;
			for (Text text : textList) {
				eventVO.setCustomFieldName(i++, text.getText());
			}
		}
	}
	
	
	public void setEventVO(EventVO eventVO) {
		this.eventVO = eventVO;
		
		if (widgetsInitialized()) {
			int requiredCustomFieldCount = getRequiredCustomFieldCount(eventVO);
			while (customFieldCount < requiredCustomFieldCount) {
				more();
			}
		}
		
		syncWidgetsToEntity();
	}


	/**
	 * I don't register myself with models, so nothing to do here
	 */
	public void widgetDisposed(DisposeEvent e) {
	}

	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
}
