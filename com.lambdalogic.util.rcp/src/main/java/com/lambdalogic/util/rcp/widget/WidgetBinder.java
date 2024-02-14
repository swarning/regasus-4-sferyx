package com.lambdalogic.util.rcp.widget;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import de.regasus.core.validation.FieldMetadata;

public class WidgetBinder<Entity, ValueType> {

	private Supplier<ValueType> widgetGetFunctionalInterface;
	private Consumer<ValueType> widgetSetFunctionalInterface;

	private Function<Entity, ValueType> entityGetFunctionalInterface;
	private BiConsumer<Entity, ValueType> entitySetFunctionalInterface;

	private Method entityGetMethod;
	private Method entitySetMethod;


	private Class<?> valueTypeClass;


	public static <Entity> WidgetBinder<Entity, String> build(Text text, FieldMetadata fieldMetadata) {
		WidgetBinder<Entity, String> connector = new WidgetBinder<>();

		connector.widgetGetFunctionalInterface = () -> text.getText();
		connector.widgetSetFunctionalInterface = value -> text.setText(value);

		connector.entityGetMethod = fieldMetadata.getGetterMethod();
		connector.entitySetMethod = fieldMetadata.getSetterMethod();

		connector.valueTypeClass = String.class;

		return connector;
	}


	public static <Entity> WidgetBinder<Entity, String> build(
		Text text,
		Function<Entity, String> entityGetter,
		BiConsumer<Entity, String> entitySetter
	) {
		WidgetBinder<Entity, String> connector = new WidgetBinder<>();

		connector.widgetGetFunctionalInterface = () -> text.getText();
		connector.widgetSetFunctionalInterface = value -> text.setText(value);

		connector.entityGetFunctionalInterface = entityGetter;
		connector.entitySetFunctionalInterface = entitySetter;

		connector.valueTypeClass = String.class;

		return connector;
	}


	public static <Entity> WidgetBinder<Entity, Boolean> build(Button button, FieldMetadata fieldMetadata) {
		WidgetBinder<Entity, Boolean> connector = new WidgetBinder<>();

		connector.widgetGetFunctionalInterface = () -> button.getSelection();
		connector.widgetSetFunctionalInterface = value -> button.setSelection(value);

		connector.entityGetMethod = fieldMetadata.getGetterMethod();
		connector.entitySetMethod = fieldMetadata.getSetterMethod();

		connector.valueTypeClass = Boolean.class;

		return connector;
	}


	public static <Entity> WidgetBinder<Entity, Boolean> build(
		Button button,
		Function<Entity, Boolean> entityGetter,
		BiConsumer<Entity, Boolean> entitySetter
	) {
		WidgetBinder<Entity, Boolean> connector = new WidgetBinder<>();

		connector.widgetGetFunctionalInterface = () -> button.getSelection();
		connector.widgetSetFunctionalInterface = value -> button.setSelection(value);

		connector.entityGetFunctionalInterface = entityGetter;
		connector.entitySetFunctionalInterface = entitySetter;

		connector.valueTypeClass = Boolean.class;

		return connector;
	}


	public WidgetBinder() {
	}


	public void syncWidgetToEntity(Entity entity) {
		ValueType value = getValueFromEntity(entity);

		if (valueTypeClass == String.class && value == null) {
			value = (ValueType) "";
		}
		widgetSetFunctionalInterface.accept(value);
	}


	public void syncEntityToWidget(Entity entity) {
		ValueType value = widgetGetFunctionalInterface.get();

		setValueToEntity(value, entity);
	}


	private ValueType getValueFromEntity(Entity entity) {
		ValueType value = null;
		if (entityGetFunctionalInterface != null) {
			value = entityGetFunctionalInterface.apply(entity);
		}
		else if (entityGetMethod != null) {
			try {
				value = (ValueType) entityGetMethod.invoke(entity);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return value;
	}


	private void setValueToEntity(ValueType value, Entity entity) {
		if (entitySetFunctionalInterface != null) {
			entitySetFunctionalInterface.accept(entity, value);
		}
		else if (entitySetMethod != null) {
			try {
				entitySetMethod.invoke(entity, value);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
