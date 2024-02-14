package de.regasus.event.editor;

import static com.lambdalogic.util.StringHelper.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

public class EventOnsiteWorkflowComposite extends Composite {

	// the entity
	private EventVO eventVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private Text onsiteWorkflow;

	// *
	// * Widgets
	// **************************************************************************


	public EventOnsiteWorkflowComposite(Composite parent, int style) throws Exception {
		super(parent, style);

		setLayout(new FillLayout());
		onsiteWorkflow = new Text(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

		// set font for XML code
		Font sourceCodeFont = com.lambdalogic.util.rcp.Activator.getDefault().getFontFromRegistry(
			com.lambdalogic.util.rcp.Activator.SOURCE_CODE_FONT
		);

		if (sourceCodeFont != null) {
			onsiteWorkflow.setFont(sourceCodeFont);
		}

		onsiteWorkflow.addModifyListener(modifySupport);
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

	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					onsiteWorkflow.setText( avoidNull(eventVO.getOnsiteWorkflow()) );
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (eventVO != null) {
			eventVO.setOnsiteWorkflow( trim(onsiteWorkflow.getText()) );
		}
	}


	public void setEventVO(EventVO eventVO) {
		this.eventVO = eventVO;

		syncWidgetsToEntity();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
