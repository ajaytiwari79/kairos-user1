package com.kairos.service.skill;

import com.kairos.persistence.model.country.Country;
import com.kairos.persistence.model.user.skill.SkillCategory;
import com.kairos.persistence.model.user.skill.SkillCategoryQueryResults;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.skill.SkillCategoryGraphRepository;
import com.kairos.persistence.repository.user.skill.SkillGraphRepository;
import com.kairos.service.exception.ExceptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.kairos.constants.UserMessagesConstants.MESSAGE_SKILLCATEGORY_NAME_DUPLICATE;

/**
 * SkillCategoryService
 */
@Service
@Transactional
public class SkillCategoryService {

    @Inject
    private SkillCategoryGraphRepository skillCategoryGraphRepository;

    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private SkillGraphRepository skillGraphRepository;

    @Inject
    private ExceptionService exceptionService;

    public Object createSkillCategory(long countryId, SkillCategory skillCategory) {
        Country country = countryGraphRepository.findOne(countryId);
        if (country == null) {
            return null;
        }
        String name = "(?i)" + skillCategory.getName();
        if (!skillCategoryGraphRepository.checkDuplicateSkillCategory(countryId, name).isEmpty()) {
            exceptionService.duplicateDataException(MESSAGE_SKILLCATEGORY_NAME_DUPLICATE);

        } else {
            skillCategory.setCountry(country);
            skillCategoryGraphRepository.save(skillCategory);
            return skillCategory.retieveDetails();
        }

        return null;
    }


    public SkillCategory getSkillCategorybyId(Long id) {
        return skillCategoryGraphRepository.findOne(id);
    }


    public boolean deleteSkillCategorybyId(long skillCategory) {
        SkillCategory category = skillCategoryGraphRepository.findOne(skillCategory);
        if (category != null) {
            category.setEnabled(false);
            skillCategoryGraphRepository.save(category);
            return true;
        }
        return false;
    }


    /**
     * List all SkillCategory
     *
     * @return
     */
    public List<SkillCategory> getAllSkillCategory() {
        return skillCategoryGraphRepository.findAll();
    }

    public Map<String, Object> updateSkillCategory(SkillCategory skillCategory) {

        if (skillCategory != null) {
            SkillCategory currentCategory = skillCategoryGraphRepository.findOne(skillCategory.getId());
            if (currentCategory != null) {
                currentCategory.setName(skillCategory.getName());
                currentCategory.setDescription(skillCategory.getDescription());
                skillCategoryGraphRepository.save(currentCategory);


                Map<String, Object> response = skillCategory.retieveDetails();
                response.put("skills", skillCategoryGraphRepository.getThisCategorySkills(currentCategory.getId()));
                return response;
            }
        }
        return null;
    }

    public List<SkillCategoryQueryResults> getAllSkillCategoryOfCountryOrUnit(Long countryOrUnitId, boolean isCountry) {
        return isCountry ? skillCategoryGraphRepository.findSkillCategoryByCountryId(countryOrUnitId) : skillCategoryGraphRepository.findSkillCategoryByUnitId(countryOrUnitId);
    }
}
