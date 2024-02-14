package de.regasus.programme.cancelterm.editor;

import org.eclipse.jface.resource.ImageDescriptor;

import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class ProgrammeCancelationTermEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput {

	/**
	 * The PK of the parent.
	 */
	protected  Long offeringPK;


	private ProgrammeCancelationTermEditorInput(Long cancelationTermPK, Long offeringPK) {
		key = cancelationTermPK;
		this.offeringPK = offeringPK;
	}


	public static ProgrammeCancelationTermEditorInput getEditInstance(
		Long programmeCancelationTermPK,
		Long programmeOfferingPK
	) {
		ProgrammeCancelationTermEditorInput pctEditorInput = new ProgrammeCancelationTermEditorInput(
			programmeCancelationTermPK,
			programmeOfferingPK
		);
		return pctEditorInput;
	}


	public static ProgrammeCancelationTermEditorInput getCreateInstance(
		Long programmeOfferingPK
	) {
		ProgrammeCancelationTermEditorInput pctEditorInput = new ProgrammeCancelationTermEditorInput(
			null, // programmeCancelationTermPK
			programmeOfferingPK
		);
		return pctEditorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor(IImageKeys.PROGRAMME_CANCELATION_TERM);
	}


	@Override
	public Class<?> getEntityType() {
		return ProgrammeCancelationTermVO.class;
	}


	public Long getOfferingPK() {
		return offeringPK;
	}

}
