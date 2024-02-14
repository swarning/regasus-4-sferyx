package de.regasus.portal.page.editor;

import static com.lambdalogic.util.CollectionsHelper.*;
import static de.regasus.LookupService.getPKGenerator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.Vigenere;
import com.lambdalogic.util.exception.ErrorMessageException;

import de.regasus.common.File;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.portal.Page;
import de.regasus.portal.ParticipantFieldIdProvider;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalFileHelper;
import de.regasus.portal.PortalFileModel;
import de.regasus.portal.PortalModel;
import de.regasus.portal.Section;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.FieldComponent;
import de.regasus.portal.component.FieldType;
import de.regasus.portal.component.FileComponent;
import de.regasus.portal.component.ProgrammeBookingComponent;
import de.regasus.portal.component.StreamComponent;
import de.regasus.programme.ProgrammePointModel;


public class PageHelper {

	public static final String COMPONENT_HTML_ID_PREFIX = "component-";
	public static final String SECTION_HTML_ID_PREFIX = "section-";


	/**
	 * Add a {@link Section} to a {@link Page}.
	 *
	 * The {@link Section} can be a new one or a copied one (copy and paste).
	 *
	 * The place of the {@link Section} is defined by the item that is currently selected.
	 *
	 * @param page
	 * @param id ID of the selected item
	 * @param section
	 * @return
	 * @throws Exception
	 */
	public static Section addSection(Page page, String id, Section section) throws Exception {
		Map<String, String> renameHtmlIdMap = MapHelper.createHashMap(1 + section.getComponentList().size());

		// replace existing ids with unique ones
		if (section.getId() == null || existsId(page, section.getId())) {
			section.setId( UUID.randomUUID().toString() );
		}

		// Always set new unique htmlId
		{
			String htmlId = section.getHtmlId();
			String newHtmlId = generateUniqueHtmlId(SECTION_HTML_ID_PREFIX);
			section.setHtmlId(newHtmlId);
			renameHtmlIdMap.put(htmlId, newHtmlId);
		}


		for (Component component : section.getComponentList()) {
			String oldHtmlId = component.getHtmlId();
			handleComponent(page, component);
			String newHtmlId = component.getHtmlId();

			if (oldHtmlId != null && ! oldHtmlId.equals(newHtmlId)) {
				renameHtmlIdMap.put(oldHtmlId, newHtmlId);
			}
		}


		/* replace old htmlIds with new ones in Component.render
		 * We are doing this to preserve references between Components of this Section.
		 */
		if ( ! renameHtmlIdMap.isEmpty() ) {
			List<String> oldHtmlIds = new ArrayList<>();
			List<String> newHtmlIds = new ArrayList<>();

			for (Map.Entry<String, String> entry : renameHtmlIdMap.entrySet()) {
				oldHtmlIds.add( entry.getKey() );
				newHtmlIds.add( entry.getValue() );
			}

    		for (Component component : section.getComponentList()) {
    			String render = component.getRender();
    			render = StringHelper.replace(render, oldHtmlIds, newHtmlIds);
    			component.setRender(render);
    		}
		}


		List<Section> sectionList = page.getSectionList();
		if (sectionList != null) {
			int sectionIndex = getSectionIndex(sectionList, id);
			sectionList.add(sectionIndex, section);
		}
		else {
			sectionList = createArrayList(section);
			page.setSectionList(sectionList);
		}

		return section;
	}


	public static void addComponent(Page page, String id, Component component) throws Exception {
		handleComponent(page, component);

		List<Section> sectionList = page.getSectionList();

		Section section = getSection(sectionList, id);

		List<Component> componentList = section.getComponentList();
		if (componentList != null) {
			int componentIndex = getComponentIndex(componentList, id);
			componentList.add(componentIndex, component);
		}
		else {
			componentList = createArrayList(component);
			section.setComponentList(componentList);
		}
	}


	private static void handleComponent(Page page, Component component) throws Exception {
		// replace existing id with unique one
		if ( existsId(page, component.getId())) {
			component.setId( UUID.randomUUID().toString() );
		}

		component.setHtmlId( generateUniqueHtmlId(COMPONENT_HTML_ID_PREFIX) );

		if (component instanceof FieldComponent) {
			handleFieldComponent(page, (FieldComponent) component);
		}
		else if (component instanceof FileComponent) {
			handleFileComponent(page, (FileComponent) component);
		}
		else if (component instanceof ProgrammeBookingComponent) {
			handleProgrammeBookingComponent(page, (ProgrammeBookingComponent) component);
		}
		else if (component instanceof StreamComponent) {
			handleStreamComponent(page, (StreamComponent) component);
		}
	}




	/**
	 * Check if the {@link FieldComponent} references a {@link ParticipantCustomField} which does not exist in
	 * the target Event. If so, replace it with a corresponding one. If no such {@link ParticipantCustomField}
	 * exists in the target Event, remove the reference.
	 *
	 * @param page
	 * @param fieldComponent
	 * @throws Exception
	 */
	private static void handleFieldComponent(Page page, FieldComponent fieldComponent) throws Exception {
		// determine target Event
		Portal targetPortal = PortalModel.getInstance().getPortal( page.getPortalId() );
		Long targetEventPK = targetPortal.getEventId();

		/* We don't know the source Event here, therefore we just go on and expect source and target
		 * Event to be different.
		 */

		FieldType fieldType = fieldComponent.getFieldType();
		if (fieldType == FieldType.CUSTOM_FIELD) {
			String fieldId = fieldComponent.getFieldId();
			if (fieldId.startsWith(ParticipantFieldIdProvider.PARTICIPANT_CUSTOM_FIELD_PREFIX)) {
				try {
					Long sourceCustomFieldPK = TypeHelper.toLong(
						fieldId.substring( ParticipantFieldIdProvider.PARTICIPANT_CUSTOM_FIELD_PREFIX.length() )
					);

					Long targetCustomFieldPK = getMatchingTargetCustomFieldPK(sourceCustomFieldPK, targetEventPK);
					if (targetCustomFieldPK != null) {
						String targetFieldId = ParticipantFieldIdProvider.PARTICIPANT_CUSTOM_FIELD(targetCustomFieldPK);
						fieldComponent.setFieldId(targetFieldId);
					}
					else {
						fieldComponent.setFieldId(null);
						fieldComponent.setFieldType(null);
					}
				}
				catch (ParseException e) {
					throw new ErrorMessageException(e);
				}
			}
			else {
				throw new ErrorMessageException("The value of FieldComponent.fieldId does not start with 'cf.'");
			}
		}
	}


	private static Long getMatchingTargetCustomFieldPK(Long sourceCustomFieldPK, Long targetEventPK) throws Exception {
		Long targetCustomFieldPK = null;

		// load source Custom Field
		ParticipantCustomFieldModel pcfModel = ParticipantCustomFieldModel.getInstance();
		ParticipantCustomField sourceCustomField = pcfModel.getParticipantCustomField(sourceCustomFieldPK);

		if (sourceCustomField.getEventPK().equals(targetEventPK)) {
			targetCustomFieldPK = sourceCustomFieldPK;
		}
		else {
    		// check if a matching Custom Field exists in target Event
    		List<ParticipantCustomField> targetCustomFields = pcfModel.getParticipantCustomFieldsByEventPK(targetEventPK);
    		for (ParticipantCustomField currentTargetCustomField : targetCustomFields) {
    			if ( isMatching(sourceCustomField, currentTargetCustomField) ) {
    				targetCustomFieldPK = currentTargetCustomField.getID();
    				break;
    			}
    		}
		}

		return targetCustomFieldPK;
	}


	/**
	 * Check if two {@link ParticipantCustomField} match.
	 * In this context two custom fields match if their names are equal.
	 * Their values for type, min, max and precision does not matter!
	 * When copying a Portal only the name matters, because {@link FieldComponent} does not rely to the type.
	 * We'd even get into a problem if we included the type. If there was a custom field with the same name but a
	 * different type, we could not create one with the same type because the names of all custom fields must be
	 * unique.
	 *
	 * @param customField0
	 * @param customField1
	 * @return
	 */
	private static boolean isMatching(ParticipantCustomField customField0, ParticipantCustomField customField1) {
		return customField0.getName().equals( customField1.getName() );
	}


	/**
	 * Remove File mnemonics that do not exist
	 * @param page
	 * @param fileComponent
	 * @throws Exception
	 */
	private static void handleFileComponent(Page page, FileComponent fileComponent) throws Exception {
		// if there is no File with fileMnemonic: remove mnemonic
		String fileMnemonic = fileComponent.getFileMnemonic();
		Long portalId = page.getPortalId();

		if ( ! existsFileMnemonic(portalId, fileMnemonic)) {
			fileComponent.setFileMnemonic(null);
		}
	}


	private static boolean existsFileMnemonic(Long portalPK, String fileMnemonic) throws Exception {
		Collection<File> portalFiles = PortalFileModel.getInstance().getPortalFiles(portalPK);
		for (File file : portalFiles) {
			String currentFileMnemonic = PortalFileHelper.extractFileMnemonic( file.getInternalPath() );
			if (currentFileMnemonic.equals(fileMnemonic)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Check if the {@link ProgrammeBookingComponent} references Programme Points which does not exist in
	 * the target Event. If so, replace them with corresponding ones. If no such Programme Points
	 * exists in the target Event, remove the references.
	 * @param page
	 * @param pbComponent
	 * @throws Exception
	 */
	private static void handleProgrammeBookingComponent(Page page, ProgrammeBookingComponent pbComponent)
	throws Exception {
		// determine target Event
		Portal targetPortal = PortalModel.getInstance().getPortal( page.getPortalId() );
		Long targetEventPK = targetPortal.getEventId();

		/* We don't know the source Event here, therefore we just go on and expect source and target
		 * Event to be different.
		 */

		List<Long> sourceProgrammePointPKs = pbComponent.getProgrammePointIdList();
		if ( notEmpty(sourceProgrammePointPKs) ) {
    		List<Long> targetProgrammePointPKs = new ArrayList<>( sourceProgrammePointPKs.size() );

    		for (Long sourceProgrammePointPK : sourceProgrammePointPKs) {
    			Long targetProgrammePointPK = getMatchingTargetProgrammePointPK(sourceProgrammePointPK, targetEventPK);
    			if (targetProgrammePointPK != null) {
    				targetProgrammePointPKs.add(targetProgrammePointPK);
    			}
    		}

    		pbComponent.setProgrammePointIdList(targetProgrammePointPKs);
		}
	}


	/**
	 * Check if the {@link StreamComponent} references Programme Points which does not exist in
	 * the target Event. If so, replace them with corresponding ones. If no such Programme Points
	 * exists in the target Event, remove the references.
	 * @param page
	 * @param streamComponent
	 * @throws Exception
	 */
	private static void handleStreamComponent(Page page, StreamComponent streamComponent)
	throws Exception {
		// determine target Event
		Portal targetPortal = PortalModel.getInstance().getPortal( page.getPortalId() );
		Long targetEventPK = targetPortal.getEventId();

		/* We don't know the source Event here, therefore we just go on and expect source and target
		 * Event to be different.
		 */

		// TODO
//		List<Long> sourceProgrammePointPKs = streamComponent.getProgrammePointIds();
//		if ( notEmpty(sourceProgrammePointPKs) ) {
//    		List<Long> targetProgrammePointPKs = new ArrayList<>( sourceProgrammePointPKs.size() );
//
//    		for (Long sourceProgrammePointPK : sourceProgrammePointPKs) {
//    			Long targetProgrammePointPK = getMatchingTargetProgrammePointPK(sourceProgrammePointPK, targetEventPK);
//    			if (targetProgrammePointPK != null) {
//    				targetProgrammePointPKs.add(targetProgrammePointPK);
//    			}
//    		}
//
//    		streamComponent.setProgrammePointIds(targetProgrammePointPKs);
//		}
	}


	private static Long getMatchingTargetProgrammePointPK(Long sourceProgrammePointPK, Long targetEventPK)
	throws Exception {
		Long targetProgrammePointPK = null;

		// load source Programme Point
		ProgrammePointModel ppModel = ProgrammePointModel.getInstance();
		ProgrammePointVO sourceProgrammePointVO = ppModel.getProgrammePointVO(sourceProgrammePointPK);

		if (sourceProgrammePointVO.getEventPK().equals(targetEventPK)) {
			targetProgrammePointPK = sourceProgrammePointPK;
		}
		else {
    		List<ProgrammePointVO> targetProgrammePoints = ppModel.getProgrammePointVOsByEventPK(targetEventPK);
    		for (ProgrammePointVO programmePointVO : targetProgrammePoints) {
    			if ( isMatching(sourceProgrammePointVO, programmePointVO) ) {
    				targetProgrammePointPK = programmePointVO.getID();
    				break;
    			}
    		}
		}

		return targetProgrammePointPK;
	}


	/**
	 * Check if two {@link ProgrammePointVO} match.
	 * In this context two Programme Points match if their names are equal.
	 *
	 * @param pp0
	 * @param pp1
	 * @return
	 */
	private static boolean isMatching(ProgrammePointVO pp0, ProgrammePointVO pp1) {
		return pp0.getName().equals( pp1.getName() );
	}


	/**
	 * Determine the Section to which the specified id belongs.
	 *
	 * @param page
	 * @param id
	 * @return
	 */
	private static Section getSection(List<Section> sectionList, String id) {
		Objects.requireNonNull(sectionList);

		// determine index of previous Section
		for (Section section : sectionList) {
			if (section.getId().equals(id)) {
				return section;
			}
			else {
				for (Component component : section.getComponentList()) {
					if (component.getId().equals(id)) {
						return section;
					}
				}
			}
		}

		return null;
	}


	/**
	 * Determine the index for a new Section which is the index of the Section with the specified id plus 1.
	 * The id can also be the id of one of the Sections Components.
	 *
	 * @param page
	 * @param id
	 * @return
	 */
	private static int getComponentIndex(List<Component> componentList, String id) {
		Objects.requireNonNull(componentList);

		// determine index of previous Component
		int componentIndex = 1;
		for (Component component : componentList) {
			if (component.getId().equals(id)) {
				return componentIndex;
			}
			componentIndex++;
		}

		return 0;
	}


	/**
	 * Determine the index for a new Section which is the index of the Section with the specified id plus 1.
	 * The id can also be the id of one of the Sections Components.
	 *
	 * @param page
	 * @param id
	 * @return
	 */
	private static int getSectionIndex(List<Section> sectionList, String id) {
		Objects.requireNonNull(sectionList);

		// determine index of previous Section
		int sectionIndex = 1;
		for (Section section : sectionList) {
			if (section.getId().equals(id)) {
				return sectionIndex;
			}
			else {
				for (Component component : section.getComponentList()) {
					if (component.getId().equals(id)) {
						return sectionIndex;
					}
				}
			}
			sectionIndex++;
		}

		return 0;
	}


	public static String generateUniqueHtmlId(String prefix) {
		Long uniqueLong = getPKGenerator().getValue();
		String uniqueString = Vigenere.toVigenereString(uniqueLong);

		StringBuilder htmlId = new StringBuilder(32);
		if (prefix != null ) {
			htmlId.append(prefix);
		}
		htmlId.append(uniqueString);
		return htmlId.toString();
	}


	/**
	 * Check if the htmlId is already used in one of the Pages Sections or Components.
	 * @param page
	 * @param htmlId
	 * @return
	 */
	private static boolean existsId(Page page, String id) {
		for (Section section : page.getSectionList()) {
			if (section.getId().equals(id)) {
				return true;
			}

			for (Component component : section.getComponentList()) {
				if (component.getId().equals(id)) {
					return true;
				}
			}
		}
		return false;
	}

}
