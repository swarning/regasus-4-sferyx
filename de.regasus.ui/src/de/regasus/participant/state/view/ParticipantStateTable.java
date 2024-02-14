/**
 * ParticipantStateTable.java
 * Created on 16.04.2012
 */
package de.regasus.participant.state.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.I18N;

/**
 * @author huuloi
 *
 */

enum ParticipantStateTableColumns {NAME, DESCRIPTION, /*REQUIRED_BY_SYSTEM,*/ BADGE_PRINT};

public class ParticipantStateTable 
extends SimpleTable<ParticipantState, ParticipantStateTableColumns>{

	public ParticipantStateTable(Table table) {
		super(table, ParticipantStateTableColumns.class);
	}

	@Override
	public String getColumnText(ParticipantState participantState, ParticipantStateTableColumns column) {
		String label = null;
		
		switch (column) {
		case NAME:
			LanguageString nameLanguageString = participantState.getName();
			if (nameLanguageString != null) {
				label = nameLanguageString.getString();
			}
			break;
		case DESCRIPTION:
			LanguageString descriptionLanguageString = participantState.getDescription();
			if (descriptionLanguageString != null) {
				label = descriptionLanguageString.getString();
			}
			break;
//		case REQUIRED_BY_SYSTEM:
//			boolean requiredBySystem = participantState.isRequiredBySystem();
//			label = requiredBySystem ? EmailI18N.YES : EmailI18N.NO;
//			break;
		case BADGE_PRINT:
			boolean badgePrint = participantState.isBadgePrint();
			label = badgePrint ? I18N.YES : I18N.NO;
			break;
		}
		if (label == null) {
			label = ""; 
		}
		return label;
	}
	
	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		ParticipantState participantState,
		ParticipantStateTableColumns column
	) {
		switch (column) {
		case NAME:
			LanguageString nameLanguageString = participantState.getName();
			if (nameLanguageString != null) {
				return nameLanguageString.getString();
			}
		case DESCRIPTION:
			LanguageString descriptionLanguageString = participantState.getDescription();
			if (descriptionLanguageString != null) {
				return descriptionLanguageString.getString();
			}
//		case REQUIRED_BY_SYSTEM:
//			boolean requiredBySystem = participantState.isRequiredBySystem();
//			return requiredBySystem ? EmailI18N.YES : EmailI18N.NO;
		case BADGE_PRINT:
			boolean badgePrint = participantState.isBadgePrint();
			return badgePrint ? I18N.YES : I18N.NO;
		default:
			return super.getColumnComparableValue(participantState, column);
		}
	}
	
	@Override
	protected ParticipantStateTableColumns getDefaultSortColumn() {
		return ParticipantStateTableColumns.NAME;
	}
}
