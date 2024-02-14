package de.regasus.hotel.view.tree;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.hotel.HotelPhotoModel;
import de.regasus.hotel.RoomDefinitionPhotoModel;
import de.regasus.ui.Activator;

/**
 * Resize Photos of all Hotels and Room Definitions according to the values of
 * - hotel.photo.width
 * - hotel.photo.height
 * - roomDefinition.photo.width
 * - roomDefinition.photo.height
 * in the table PROPERTY.
 *
 */
public class ResizePhotosAction extends AbstractAction {

	private final IWorkbenchWindow window;

	private Integer hotelPhotoWidth;
	private Integer hotelPhotoHeight;
	private Integer roomDefinitionPhotoWidth;
	private Integer roomDefinitionPhotoHeight;


	public ResizePhotosAction(IWorkbenchWindow window) {
		this.window = window;
		setId( getClass().getName() );
		setText(I18N.ResizePhotos);
	}


	@Override
	public void dispose() {
	}


	private void initPropertyValues() throws ErrorMessageException {
		String errorMessage = I18N.ResizePhotos_ErrorMissingSettings;

		hotelPhotoWidth = HotelPhotoModel.getInstance().getWidth();
		if (hotelPhotoWidth == null) {
			errorMessage = StringHelper.replace(errorMessage, "<setting>", HotelPhotoModel.WIDTH_PROPERTY_KEY);
			throw new ErrorMessageException(errorMessage);
		}

		hotelPhotoHeight = HotelPhotoModel.getInstance().getHeight();
		if (hotelPhotoHeight == null) {
			errorMessage = StringHelper.replace(errorMessage, "<setting>", HotelPhotoModel.HEIGHT_PROPERTY_KEY);
			throw new ErrorMessageException(errorMessage);
		}

		roomDefinitionPhotoWidth = RoomDefinitionPhotoModel.getInstance().getWidth();
		if (roomDefinitionPhotoWidth == null) {
			errorMessage = StringHelper.replace(errorMessage, "<setting>", RoomDefinitionPhotoModel.WIDTH_PROPERTY_KEY);
			throw new ErrorMessageException(errorMessage);
		}

		roomDefinitionPhotoHeight = RoomDefinitionPhotoModel.getInstance().getHeight();
		if (roomDefinitionPhotoHeight == null) {
			errorMessage = StringHelper.replace(errorMessage, "<setting>", RoomDefinitionPhotoModel.HEIGHT_PROPERTY_KEY);
			throw new ErrorMessageException(errorMessage);
		}
	}


	@Override
	public void run() {
		try {
			initPropertyValues();

			Shell shell = window.getShell();
			String title = I18N.ResizePhotos;
			String message = I18N.ResizePhotos_DialogMessage;
			message = StringHelper.replace(message, "<hotel.photo.width>",           String.valueOf(hotelPhotoWidth));
			message = StringHelper.replace(message, "<hotel.photo.height>",          String.valueOf(hotelPhotoHeight));
			message = StringHelper.replace(message, "<roomDefinition.photo.width>",  String.valueOf(roomDefinitionPhotoWidth));
			message = StringHelper.replace(message, "<roomDefinition.photo.height>", String.valueOf(roomDefinitionPhotoHeight));

			boolean confirmed = MessageDialog.openConfirm(shell, title, message);
			if (confirmed) {
				ResizePhotosJob job = new ResizePhotosJob();
				job.setUser(true);
				job.schedule();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
