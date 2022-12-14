package com.kairos.service.tag;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.controller.staffing_level.StaffingLevelController;
import com.kairos.dto.TranslationInfo;
import com.kairos.dto.user.country.tag.TagDTO;
import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.MasterDataTypeEnum;
import com.kairos.persistence.model.activity.Activity;
import com.kairos.persistence.model.tag.Tag;
import com.kairos.persistence.repository.activity.ActivityMongoRepository;
import com.kairos.persistence.repository.tag.TagMongoRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.exception.ExceptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectUtils.isNull;
import static com.kairos.constants.ActivityMessagesConstants.*;

/**
 * Created by prerna on 20/11/17.
 */
@Transactional
@Service
public class TagService {

    private Logger logger= LoggerFactory.getLogger(StaffingLevelController.class);

    @Autowired
    private UserIntegrationService userIntegrationService;

    @Autowired
    TagMongoRepository tagMongoRepository;
    @Autowired
    ExceptionService exceptionService;
    @Inject
    private ActivityMongoRepository activityMongoRepository;



    public Tag addCountryTag(Long countryId, TagDTO tagDTO) {
        if ( !userIntegrationService.isCountryExists(countryId)) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID,countryId);
        }
        logger.info("tagDTO : "+tagDTO.getMasterDataType());
        if( tagMongoRepository.findTagByNameIgnoreCaseAndCountryIdAndMasterDataTypeAndDeletedAndCountryTagTrue(tagDTO.getName(), countryId, tagDTO.getMasterDataType().toString(), false)  != null){
           exceptionService.duplicateDataException(MESSAGE_TAG_NAME,tagDTO.getName() );
        }
        return tagMongoRepository.save(buildTag(tagDTO, true, countryId));
    }

    public Tag  updateCountryTag(Long countryId, BigInteger tagId, TagDTO tagDTO) {
        if ( !userIntegrationService.isCountryExists(countryId)) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID,countryId);
        }

        Tag tag = tagMongoRepository.findTagByIdAndCountryIdAndMasterDataTypeAndDeletedAndCountryTagTrue(tagId, countryId, tagDTO.getMasterDataType().toString(), false);
        if(  tag  == null){
            exceptionService.dataNotFoundByIdException(MESSAGE_TAG_ID,tagId);
        }
        if(! ( tag.getName().equalsIgnoreCase(tagDTO.getName()) ) && tagMongoRepository.findTagByNameIgnoreCaseAndCountryIdAndMasterDataTypeAndDeletedAndCountryTagTrue(tagDTO.getName(), countryId, tagDTO.getMasterDataType().toString(), false) != null ){
            exceptionService.duplicateDataException(MESSAGE_TAG_NAME,tagDTO.getName());
        }
        tag.setName(tagDTO.getName());
        tagMongoRepository.save(tag);
        return tag;
    }

    public HashMap<String,Object> getListOfCountryTags(Long countryId, String filterText, MasterDataTypeEnum masterDataType,boolean includeStaffTags){
        if (!userIntegrationService.isCountryExists(countryId)) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID,countryId);
        }
        filterText = isNull(filterText) ? "" : filterText;
        HashMap<String,Object> tagsData = new HashMap<>();
        List<Tag> tags;
        List<TagDTO> tagDTOS ;
        if(masterDataType == null){
            tagDTOS = tagMongoRepository.findAllTagByCountryIdAndNameAndDeletedAndCountryTag(countryId, filterText, false);
        } else {
            tagDTOS = tagMongoRepository.findAllTagByCountryIdAndNameAndMasterDataTypeAndDeleted(countryId, filterText, masterDataType.toString(), false);
        }
        if(includeStaffTags && MasterDataTypeEnum.ACTIVITY.equals(masterDataType)){
            tags = userIntegrationService.getAllStaffTagsByCountryIdOrOrganizationId(countryId, filterText, true);
            tagDTOS.addAll(ObjectMapperUtils.copyCollectionPropertiesByMapper(tags, com.kairos.dto.activity.tags.TagDTO.class));
        }
        tagsData.put("tags",tagDTOS);
        return tagsData;
    }

    public boolean deleteCountryTag(Long countryId, BigInteger tagId){
        if ( !userIntegrationService.isCountryExists(countryId)) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID,countryId);
        }
        Tag tag = tagMongoRepository.findTagByIdAndCountryIdAndDeletedAndCountryTagTrue(tagId, countryId, false);
        if( tag == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TAG_ID,tagId);
        }
        tag.setDeleted(true);
        tagMongoRepository.save(tag);
        return true;
    }



    public Tag addOrganizationTag(Long organizationId, TagDTO tagDTO) {
        if ( !userIntegrationService.isExistOrganization(organizationId)) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID,organizationId);
        }
        logger.info("tagDTO : "+tagDTO.getMasterDataType());
        if( tagMongoRepository.findTagByNameIgnoreCaseAndOrganizationIdAndMasterDataTypeAndDeletedAndCountryTagFalse(tagDTO.getName(), organizationId, tagDTO.getMasterDataType().toString(), false)  != null){
           exceptionService.duplicateDataException(MESSAGE_TAG_NAME,tagDTO.getName());
        }
        return tagMongoRepository.save(buildTag(tagDTO, false, organizationId));
    }

    public Tag  updateOrganizationTag(Long organizationId, BigInteger tagId, TagDTO tagDTO) {

        Tag tag = tagMongoRepository.findTagByIdAndOrganizationIdAndMasterDataTypeAndDeletedAndCountryTagFalse(tagId, organizationId, tagDTO.getMasterDataType().toString(), false);
        if(  tag  == null){
            exceptionService.duplicateDataException(MESSAGE_TAG_ID,tagId);
        }
        if(! ( tag.getName().equalsIgnoreCase(tagDTO.getName()) ) && tagMongoRepository.findTagByNameIgnoreCaseAndOrganizationIdAndMasterDataTypeAndDeletedAndCountryTagFalse(tagDTO.getName(), organizationId, tagDTO.getMasterDataType().toString(), false) != null ){
            exceptionService.duplicateDataException(MESSAGE_TAG_NAME,tagDTO.getName());
        }
        tag.setName(tagDTO.getName());
        tagMongoRepository.save(tag);
        return tag;
    }

    public HashMap<String,Object> getListOfOrganizationTags(Long organizationId, String filterText, MasterDataTypeEnum masterDataType, boolean includeStaffTags){

        if(filterText == null){
            filterText = "";
        }

        HashMap<String,Object> tagsData = new HashMap<>();
        List<Tag> tags;
        if(masterDataType == null){
            tags =  tagMongoRepository.findAllTagByOrganizationIdAndNameAndDeletedAndCountryTagFalse(organizationId, filterText, false);
        } else {
            tags = tagMongoRepository.findAllTagByOrganizationIdAndNameAndMasterDataTypeAndDeletedAndCountryTagFalse(organizationId, filterText, masterDataType.toString(), false);
        }
        if(userIntegrationService.showCountryTagForOrganization(organizationId)){
            Long countryId = UserContext.getUserDetails().getCountryId();
            if(masterDataType == null){
                tags.addAll( tagMongoRepository.findAllTagByCountryIdAndNameAndDeletedAndCountryTagTrue(countryId, filterText, false));
            } else {
                tags.addAll( tagMongoRepository.findAllTagByCountryIdAndNameAndMasterDataTypeAndDeletedAndCountryTagTrue(countryId, filterText, masterDataType.toString(), false));
            }
        }
        if(includeStaffTags && MasterDataTypeEnum.ACTIVITY.equals(masterDataType)){
            tags.addAll(userIntegrationService.getAllStaffTagsByCountryIdOrOrganizationId(organizationId, filterText, false));
        }
        List<com.kairos.dto.activity.tags.TagDTO> tagDTOS =ObjectMapperUtils.copyCollectionPropertiesByMapper(tags, com.kairos.dto.activity.tags.TagDTO.class);
        tagsData.put("tags",tagDTOS);
        return tagsData;
    }

    public boolean deleteOrganizationTag(Long organizationId, BigInteger tagId){
        if ( !userIntegrationService.isExistOrganization(organizationId)) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID,organizationId);
        }
        Tag tag = tagMongoRepository.findTagByIdAndOrganizationIdAndDeletedAndCountryTagFalse(tagId, organizationId, false);
        if( tag == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TAG_ID,tagId);
        }
        tag.setDeleted(true);
        tagMongoRepository.save(tag);
        return true;
    }

    public Tag getCountryTagByName(Long countryId, String nameOfTag, MasterDataTypeEnum masterDataTypeEnum){
        return tagMongoRepository.findTagByCountryIdAndNameAndMasterDataTypeAndDeletedAndCountryTagTrue(countryId, nameOfTag, masterDataTypeEnum.toString(), false);

    }

    public Tag getOrganizationTagByName(Long orgId, String nameOfTag, MasterDataTypeEnum masterDataTypeEnum){
        return tagMongoRepository.findTagByOrganizationIdAndNameAndMasterDataTypeAndDeletedAndCountryTagFalse(orgId, nameOfTag, masterDataTypeEnum.toString(), false);

    }

    public static Tag buildTag(TagDTO tagDTO, boolean countryTag, long countryOrOrgId){
        return new Tag(tagDTO.getName(), tagDTO.getMasterDataType(), countryTag, countryOrOrgId);
    }

    public boolean unlinkTagFromActivity(BigInteger tagId){
        List<Activity> activities = activityMongoRepository.findActivitiesByTagId(tagId);
        for (Activity activity : activities) {
            activity.getActivityGeneralSettings().setTags(activity.getActivityGeneralSettings().getTags().stream().filter(t->!t.equals(tagId)).collect(Collectors.toList()));
            activity.setTags(activity.getTags().stream().filter(t->!t.equals(tagId)).collect(Collectors.toList()));
        }
        activityMongoRepository.saveAll(activities);
        return true;
    }

    public Map<String, TranslationInfo> updateTranslation(BigInteger tagId, Map<String,TranslationInfo> translations) {
        Tag tag =tagMongoRepository.findTagByIdAndEnabled(tagId);
        if(isNull(tag)){
            exceptionService.dataNotFoundByIdException(MESSAGE_TAG_ID,tagId);
        }
        tag.setTranslations(translations);
        tagMongoRepository.save(tag);
        return tag.getTranslations();
    }
}
