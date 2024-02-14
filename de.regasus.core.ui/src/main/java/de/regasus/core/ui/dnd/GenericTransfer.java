package de.regasus.core.ui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;


/**
 * Base class for transfer objects that use serialization.
 *
 * @param <TransferType>
 */
public abstract class GenericTransfer<TransferType> extends ByteArrayTransfer {

	private Class<TransferType> typeClass = calulateTransferTypeClass();

	private final String MYTYPENAME = typeClass.getName();

	private final int MYTYPEID = registerType(MYTYPENAME);


	public GenericTransfer() {
		super();
	}


	private Class<TransferType> calulateTransferTypeClass() {
		ParameterizedType parameterizedType = null;

		Class<?> clazz = getClass();
		while (clazz != null) {
			Type type = clazz.getGenericSuperclass();
			if (type instanceof ParameterizedType) {
				parameterizedType = (ParameterizedType) type;
				break;
			}
			clazz = clazz.getSuperclass();
		}

		if (parameterizedType == null) {
			System.err.println(
				"Couldn't determine concrete EntityType of this (subclass of) GenericDAOBean. " +
				"A NullPointerException will happen shortly."
			);
		}

		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		return (Class<TransferType>) actualTypeArguments[0];
	}


	@Override
	public void javaToNative(Object object, TransferData transferData) {
		if (!validate(object) || !isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			out.writeObject(object);
			out.close();
			byte[] buffer = baos.toByteArray();
			super.javaToNative(buffer, transferData);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	@Override
	public Object nativeToJava(TransferData transferData) {
		if (isSupportedType(transferData)) {
			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null) {
				return null;
			}

			try {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer));
				Object object = ois.readObject();
				ois.close();
				return object;
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		return null;
	}


	@Override
	protected String[] getTypeNames() {
		return new String[] { MYTYPENAME };
	}


	@Override
	protected int[] getTypeIds() {
		return new int[] { MYTYPEID };
	}


	@Override
	protected boolean validate(Object object) {
		return object != null && typeClass.isAssignableFrom( object.getClass() );
	}

}
