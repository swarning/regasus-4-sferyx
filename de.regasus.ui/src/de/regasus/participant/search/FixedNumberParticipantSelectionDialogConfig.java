package de.regasus.participant.search;

import java.util.Collection;
import java.util.Objects;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ISelectionDialogConfig;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import com.lambdalogic.util.rcp.UtilI18N;

public class FixedNumberParticipantSelectionDialogConfig implements ISelectionDialogConfig {

	private Collection<Integer> allowedNumbers;


	public FixedNumberParticipantSelectionDialogConfig(Collection<Integer> allowedNumbers) {
		Objects.requireNonNull(allowedNumbers);

		for (Integer number : allowedNumbers) {
			if (number == null) {
				throw new IllegalArgumentException("Parameter 'allowedNmbers' contains null value.");
			}
			else if (number < 1) {
				throw new IllegalArgumentException("Parameter 'allowedNmbers' contains the illegal value " + number +
					". All values have to be greater than 0.");
			}
		}

		this.allowedNumbers = allowedNumbers;
	}


	@Override
	public SelectionMode getSelectionMode() {
		SelectionMode selectionMode = SelectionMode.MULTI_SELECTION;
		if (allowedNumbers.size() == 1 && allowedNumbers.contains(1)) {
			selectionMode = SelectionMode.SINGLE_SELECTION;
		}

		return selectionMode;
	}


	@Override
	public boolean canFinish(Collection<?> selectedItems) {
		return selectedItems != null && allowedNumbers.contains( selectedItems.size() );
	}


	@Override
	public String getMessage() {
		String message = I18N.ParticipantSelectionDialog_Description_FixedNumber;
		String numbers = StringHelper.createEnumeration(
			"",
			UtilI18N.Or,
			allowedNumbers
		);
		message = message.replace("<numbers>", numbers);
		return message;
	}

}
