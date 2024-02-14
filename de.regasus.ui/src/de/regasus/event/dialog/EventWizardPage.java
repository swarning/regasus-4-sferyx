package de.regasus.event.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventTableComposite;
import de.regasus.ui.Activator;

/**
 * Universal WizardPage to select one Event.
 */
public class EventWizardPage extends WizardPage {

	public static final String ID = "de.regasus.event.wizard.EventWizardPage";

	private ModifySupport modifySupport = new ModifySupport();

	private EventTableComposite eventTableComposite;

	boolean multiSelection = false;
	boolean allowEmptySelection = false;
	boolean selectAll = false;

	private Collection<Long> initiallySelectedEventPKs;
	private Collection<Long> hiddenEventPKs = null;


	public EventWizardPage() {
		super(ID);

		setTitle(I18N.EventPage_Title);
		setDescription(I18N.EventPage_Desc_One);
	}


	public void setMultiSelection(boolean multiSelection) {
		this.multiSelection = multiSelection;

		if (multiSelection && I18N.EventPage_Desc_One.equals(getDescription()) ) {
			setDescription(I18N.EventPage_Desc_OneOrMore);
		}

		if (!multiSelection && I18N.EventPage_Desc_OneOrMore.equals(getDescription()) ) {
			setDescription(I18N.EventPage_Desc_One);
		}
	}


	public void setAllowEmptySelection(boolean allowEmptySelection) {
		this.allowEmptySelection = allowEmptySelection;
	}


	public void setInitiallySelectedEventPKs(Collection<Long> initiallySelectedEventPKs) {
		this.initiallySelectedEventPKs = initiallySelectedEventPKs;
	}


	public void setInitiallySelectedEventPK(Long eventPK) {
		this.initiallySelectedEventPKs = new ArrayList<>();
		if (eventPK != null) {
			initiallySelectedEventPKs.add(eventPK);
		}
	}


	public void selectAll() {
		this.selectAll = true;
		if (eventTableComposite != null) {
			eventTableComposite.selectAll();
		}
	}


	public void setHiddenEventPKs(Collection<Long> eventPKs) {
		this.hiddenEventPKs = eventPKs;
		if (eventTableComposite != null) {
			eventTableComposite.setHiddenEventPKs(eventPKs);
		}
	}


	@Override
	public void createControl(Composite parent) {
		try {
			Composite container = new Composite(parent, SWT.NULL);
			container.setLayout(new FillLayout());

			setControl(container);

			eventTableComposite = new EventTableComposite(
				container,
				hiddenEventPKs,
				initiallySelectedEventPKs,
				multiSelection,
				SWT.NONE
			);

			if (selectAll) {
				eventTableComposite.selectAll();
			}

			modifySupport.setWidget(eventTableComposite);

			eventTableComposite.addModifyListener(modifySupport);
			eventTableComposite.addModifyListener(tableListener);

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private ModifyListener tableListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			setPageComplete( isPageComplete() );
		}
	};


	@Override
	public boolean isPageComplete() {
		boolean pageComplete;

		if (allowEmptySelection) {
			pageComplete = true;
		}
		else {
			pageComplete = !eventTableComposite.getSelectedEvents().isEmpty();
		}

		return pageComplete;

	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}


	// *****************************************************************************************************************
	// * Get-Event-Methods
	// *

	public List<Long> getEventIds() {
		return eventTableComposite.getSelectedEventIds();
	}


	public List<EventVO> getEvents() {
		return eventTableComposite.getSelectedEvents();
	}


	public Long getEventId() {
		if (multiSelection) {
			throw new RuntimeException("The method getEventPK() is not supported, because multiselection is true.");
		}

		return eventTableComposite.getSelectedEventId();
	}


	public EventVO getEvent() {
		if (multiSelection) {
			throw new RuntimeException("The method getEvent() is not supported, because multiselection is true.");
		}

		return eventTableComposite.getSelectedEvent();
	}

	// *
	// * * Get-Event-Methods
	// *****************************************************************************************************************

}
