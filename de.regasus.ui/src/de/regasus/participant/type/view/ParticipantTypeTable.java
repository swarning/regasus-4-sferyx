package de.regasus.participant.type.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.event.ParticipantType;

enum ParticipantTypeTableColumns {NAME, CATEGORY, PROOF_REQUIRED};

public class ParticipantTypeTable extends SimpleTable<ParticipantType, ParticipantTypeTableColumns> {

	public ParticipantTypeTable(Table table) {
		super(table, ParticipantTypeTableColumns.class);
	}

	@Override
	public String getColumnText(
		ParticipantType participantType,
		ParticipantTypeTableColumns column
	) {
		String label = null;

		switch (column) {
			case NAME:
    			LanguageString languageString = participantType.getName();

    			// Eigentlich liefert participantTypeVO.getName() nie null zurück,
    			// aber aus Sauberkeitsgründen wird hier trotzdem geprüft.
    			if (languageString != null) {
    				label = languageString.getString();
    			}
    			break;

			case CATEGORY:
				label = participantType.getCategory();
				break;

			case PROOF_REQUIRED:
				label = participantType.isProofRequired() ? KernelLabel.Yes.getString() : KernelLabel.No.getString();
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


//	@Override
//	protected Comparable<?> getColumnComparableValue(
//		ParticipantType participantType,
//		ParticipantTypeTableColumns column
//	) {
//		switch (column) {
//    		case NAME:
//    			LanguageString languageString = participantType.getName();
//
//    			// Eigentlich liefert participantTypeVO.getName() nie null zurück,
//    			// aber aus Sauberkeitsgründen wird hier trotzdem geprüft.
//    			if (languageString != null) {
//    				return languageString.getString();
//    			}
//
//    		case CATEGORY:
//    			return participantType.getCategory();
//
//    		default:
//    			return super.getColumnComparableValue(participantType, column);
//		}
//	}


	@Override
	protected ParticipantTypeTableColumns getDefaultSortColumn() {
		return ParticipantTypeTableColumns.NAME;
	}

}
