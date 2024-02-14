package de.regasus.event.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.LazyComposite;


public class EventTemplatesComposite extends LazyComposite {

	private Long eventId;

	private EventBadgeTemplateGroup badgeTemplateGroup;
	private EventNoteTemplateGroup noteTemplateGroup;


	public EventTemplatesComposite(Composite parent, int style) {
		super(parent, style);
	}


	@Override
	protected void createPartControl() throws Exception {
		setLayout( new GridLayout(1, false) );

		badgeTemplateGroup = new EventBadgeTemplateGroup(this);
		noteTemplateGroup = new EventNoteTemplateGroup(this);

		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, true);
		gridDataFactory.applyTo(badgeTemplateGroup);
		gridDataFactory.applyTo(noteTemplateGroup);

		badgeTemplateGroup.setEvent(eventId);
		noteTemplateGroup.setEvent(eventId);
	}


	public void setEvent(Long eventId) {
		this.eventId = eventId;

		if ( isInitialized() ) {
    		badgeTemplateGroup.setEvent(eventId);
    		noteTemplateGroup.setEvent(eventId);
		}
	}

}
