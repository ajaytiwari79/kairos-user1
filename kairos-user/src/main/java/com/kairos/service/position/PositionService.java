package com.kairos.service.position;

import com.kairos.custom_exception.DataNotFoundByIdException;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.user.agreement.wta.WTAWithCountryAndOrganizationTypeDTO;
import com.kairos.persistence.model.user.agreement.wta.WorkingTimeAgreement;
import com.kairos.persistence.model.user.agreement.wta.templates.WTABaseRuleTemplate;
import com.kairos.persistence.model.user.agreement.wta.templates.WTAWithCategoryDTO;
import com.kairos.persistence.model.user.country.EmploymentType;
import com.kairos.persistence.model.user.expertise.Expertise;
import com.kairos.persistence.model.user.position.Position;
import com.kairos.persistence.model.user.position.PositionName;
import com.kairos.persistence.model.user.position.PositionQueryResult;
import com.kairos.persistence.model.user.staff.Staff;
import com.kairos.persistence.model.user.staff.UnitEmployment;
import com.kairos.persistence.repository.organization.OrganizationGraphRepository;
import com.kairos.persistence.repository.user.agreement.cta.CollectiveTimeAgreementGraphRepository;
import com.kairos.persistence.repository.user.agreement.wta.WorkingTimeAgreementGraphRepository;
import com.kairos.persistence.repository.user.country.EmploymentTypeGraphRepository;
import com.kairos.persistence.repository.user.expertise.ExpertiseGraphRepository;
import com.kairos.persistence.repository.user.position.PositionGraphRepository;
import com.kairos.persistence.repository.user.position.PositionNameGraphRepository;
import com.kairos.persistence.repository.user.staff.StaffGraphRepository;
import com.kairos.persistence.repository.user.staff.UnitEmploymentGraphRepository;
import com.kairos.response.dto.web.PositionDTO;
import com.kairos.response.dto.web.WtaDTO;
import com.kairos.service.UserBaseService;
import com.kairos.service.agreement.wta.WTAService;
import com.kairos.service.organization.OrganizationService;
import com.kairos.service.staff.StaffService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by pawanmandhan on 26/7/17.
 */


@Transactional
@Service
public class PositionService extends UserBaseService {

    private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    @Inject
    private StaffGraphRepository staffGraphRepository;
    @Inject
    private PositionGraphRepository positionGraphRepository;
    @Inject
    private PositionNameGraphRepository positionNameGraphRepository;
    @Inject
    private ExpertiseGraphRepository expertiseGraphRepository;
    @Inject
    private UnitEmploymentGraphRepository unitEmploymentGraphRepository;
    @Inject
    private WorkingTimeAgreementGraphRepository workingTimeAgreementGraphRepository;
    @Inject
    private CollectiveTimeAgreementGraphRepository costTimeAgreementGraphRepository;
    @Inject
    private OrganizationGraphRepository organizationGraphRepository;
    @Inject
    private StaffService staffService;
    @Inject
    private EmploymentTypeGraphRepository employmentTypeGraphRepository;
    @Inject
    private OrganizationService organizationService;
    @Inject
    private PositionNameService positionNameService;

    @Inject
    private WTAService wtaService;

    public Position createPosition(Long id, long unitEmploymentId, PositionDTO positionDTO, String type) {
        UnitEmployment unitEmployment = unitEmploymentGraphRepository.findOne(unitEmploymentId);
        if (!Optional.ofNullable(unitEmployment).isPresent()) {
            throw new DataNotFoundByIdException("Invalid UnitEmployment id" + unitEmploymentId);
        }

        Organization organization = organizationService.getOrganizationDetail(id, type);
        if (!organization.isParentOrganization()) {
            organization = organizationService.getParentOfOrganization(organization.getId());

        }
        Position position = preparePosition(positionDTO, organization, id);
        List<Position> positions = unitEmployment.getPositions();

        positions.add(position);
        unitEmployment.setPositions(positions);
       // save(unitEmployment);
        position.setStaff(null);
        return position;
    }


    public Position updatePosition(long positionId, PositionDTO positionDTO) {

        //Position position=preparePosition(positionDTO);
        Position oldPosition = positionGraphRepository.findOne(positionId);

        if (oldPosition == null) {
            return null;
        }
/*
        //check if new Name already exist
        if (!(oldPosition.getName().equalsIgnoreCase(position.getName())) &&
                (positionNameGraphRepository.checkDuplicatePositionName(unitId, positionName.getName()) != null)) {
            throw new DuplicateDataException("PositionName can't be updated");
        }*/
        preparePosition(oldPosition, positionDTO);


        save(oldPosition);

        return oldPosition;

    }


    public boolean removePosition(long positionId) {

        Position position = positionGraphRepository.findOne(positionId);
        if (position == null) {
            return false;
        }
        position.setEnabled(false);
        save(position);
        if (positionGraphRepository.findOne(positionId).isEnabled()) {
            return false;
        }
        return true;
    }


    public Position getPosition(long positionId) {
        return positionGraphRepository.findOne(positionId);
    }
    /*
    * Created by vipul
    * 4-august-17
    * used to get all positions based on unitEmployment
    * */

    public List<PositionQueryResult> getAllPositions(long unitEmploymentId) {
        UnitEmployment unitEmployment = unitEmploymentGraphRepository.findOne(unitEmploymentId);

        if (unitEmployment == null) {
            throw new DataNotFoundByIdException("Invalid UnitEmployment id" + unitEmploymentId);
        }
        return positionGraphRepository.findAllPositions(unitEmploymentId);

    }

    private Position preparePosition(PositionDTO positionDTO, Organization organization, Long unitId) {
        Position position = new Position();

        //String name, String description, Expertise expertise, CostTimeAgreement cta, WorkingTimeAgreement wta

        WorkingTimeAgreement wtatt = workingTimeAgreementGraphRepository.findByExpertiseId(positionDTO.getExpertiseId(), 53L);
        WTAWithCountryAndOrganizationTypeDTO wtaWithCountryAndOrganizationTypeDTO = workingTimeAgreementGraphRepository.getWTAByExpertiseAndCountry(positionDTO.getExpertiseId(), 53L);

        if (!Optional.ofNullable(wtaWithCountryAndOrganizationTypeDTO.getExpertise()).isPresent()) {
            throw new DataNotFoundByIdException("Invalid Expertize" + positionDTO.getExpertiseId());
        }
        position.setExpertise(wtaWithCountryAndOrganizationTypeDTO.getExpertise());

        EmploymentType employmentType = organizationGraphRepository.getEmploymentTypeByOrganizationAndEmploymentId(organization.getId(), positionDTO.getEmploymentTypeId(), false);
        if (employmentType == null) {
            throw new DataNotFoundByIdException("Employment Type does not exist in unit " + employmentType.getId() + " AND " + positionDTO.getEmploymentTypeId());
        }
        position.setEmploymentType(employmentType);

        PositionName positionName = positionNameService.getPositionNameByUnitIdAndId(organization.getId(), positionDTO.getPositionNameId());
        if (!Optional.ofNullable(positionName).isPresent()) {
            throw new DataNotFoundByIdException("position Name does not exist in unit " + positionDTO.getPositionNameId());
        }
        position.setPositionName(positionName);

        /*CostTimeAgreement cta = costTimeAgreementGraphRepository.findOne(positionDTO.getCtaId());
         if (cta == null) {
            throw new DataNotFoundByIdException("Invalid CTA");
        }
        position.setCta(cta);<String, Object>

        */

        List<WTAWithCategoryDTO> wtaRuleTemplateQueryResponseArrayList = new ArrayList<WTAWithCategoryDTO>();
        WorkingTimeAgreement wta = getWta(wtaWithCountryAndOrganizationTypeDTO);

        WorkingTimeAgreement newWtaObject = new WorkingTimeAgreement();
        WorkingTimeAgreement.copyProperties(wta, newWtaObject);
        if (Optional.ofNullable(wta.getRuleTemplates()).isPresent() && wta.getRuleTemplates().size() > 0) {
            WtaDTO wtaDTO = wta.buildwtaDTO();
            List<Long> ruleTemplateIds = new ArrayList<>();
            for (WTABaseRuleTemplate wtaBRT : wta.getRuleTemplates()) {
                ruleTemplateIds.add(wtaBRT.getId());
            }
            wtaDTO.setRuleTemplates(ruleTemplateIds);

            wtaService.copyRuleTemplates(wtaDTO, wta.getRuleTemplates(), wtaRuleTemplateQueryResponseArrayList);
        }
        newWtaObject.setId(null);
        save(newWtaObject);

        Staff staff = staffGraphRepository.findOne(positionDTO.getStaffId());
        if (!Optional.ofNullable(staff).isPresent()) {
            throw new DataNotFoundByIdException("Invalid Staff Id" + positionDTO.getStaffId());
        }
        position.setStaff(staff);
        position.setStartDate(positionDTO.getStartDate());
        position.setEndDate(positionDTO.getEndDate());
        position.setWta(newWtaObject);
        position.setTotalWeeklyHours(positionDTO.getTotalWeeklyHours());
        position.setAvgDailyWorkingHours(positionDTO.getAvgDailyWorkingHours());
        position.setHourlyWages(positionDTO.getHourlyWages());
        position.setSalary(positionDTO.getSalary());
        position.setWorkingDaysInWeek(positionDTO.getWorkingDaysInWeek());

        return position;
    }

    private WorkingTimeAgreement getWta(WTAWithCountryAndOrganizationTypeDTO wtaWithCountryAndOrganizationTypeDTO) {
        WorkingTimeAgreement wta = new WorkingTimeAgreement();
        wta.setName(wtaWithCountryAndOrganizationTypeDTO.getName());
        wta.setDescription(wtaWithCountryAndOrganizationTypeDTO.getDescription());
        wta.setEnabled(true);
        wta.setEndDateMillis(wtaWithCountryAndOrganizationTypeDTO.getEndDateMillis());
        wta.setStartDateMillis(wtaWithCountryAndOrganizationTypeDTO.getStartDateMillis());
        return wta;
    }

    //!Optional.ofNullable().isPresent()
    private void preparePosition(Position oldPosition, PositionDTO positionDTO) {

        if (oldPosition.getExpertise().getId() != positionDTO.getExpertiseId()) {

            WTAWithCountryAndOrganizationTypeDTO wtaWithCountryAndOrganizationTypeDTO = workingTimeAgreementGraphRepository.getWTAByExpertiseAndCountry(positionDTO.getExpertiseId(), 53L);

            if (!Optional.ofNullable(wtaWithCountryAndOrganizationTypeDTO.getExpertise()).isPresent()) {
                throw new DataNotFoundByIdException("Invalid Expertize" + positionDTO.getExpertiseId());
            }
            oldPosition.setExpertise(wtaWithCountryAndOrganizationTypeDTO.getExpertise());



            Expertise expertise = expertiseGraphRepository.findOne(positionDTO.getExpertiseId());
            if (!Optional.ofNullable(expertise).isPresent()) {
                throw new DataNotFoundByIdException("Expertize Cannot be null" + positionDTO.getExpertiseId());
            }
            oldPosition.setExpertise(expertise);

        }


        if (oldPosition.getPositionName().getId() != positionDTO.getPositionNameId()) {
            PositionName positionName = positionNameGraphRepository.findOne(positionDTO.getPositionNameId());
            if (!Optional.ofNullable(positionName).isPresent()) {
                throw new DataNotFoundByIdException("PositionName Cannot be null" + positionDTO.getPositionNameId());
            }
            oldPosition.setPositionName(positionName);
        }

        if (oldPosition.getEmploymentType().getId() != positionDTO.getEmploymentTypeId()) {
            EmploymentType employmentType = employmentTypeGraphRepository.findOne(positionDTO.getEmploymentTypeId());
            if (!Optional.ofNullable(employmentType).isPresent()) {
                throw new DataNotFoundByIdException("employmentType Cannot be null" + positionDTO.getEmploymentTypeId());
            }
            oldPosition.setEmploymentType(employmentType);
        }

        /*if (oldPosition.getCta() != null && (oldPosition.getCta().getId() != positionDTO.getCtaId())) {
            CostTimeAgreement cta = costTimeAgreementGraphRepository.findOne(positionDTO.getCtaId());
         if (cta == null) {
            throw new NullPointerException("CTA Cannot be null");
        }
            oldPosition.setCta(cta);
        }

        if (oldPosition.getWta() != null && (oldPosition.getWta().getId() != positionDTO.getWtaId())) {
            WorkingTimeAgreement wta = workingTimeAgreementGraphRepository.findOne(positionDTO.getWtaId());
         if (wta == null) {
            throw new NullPointerException("WTA Cannot be null");
        }
            oldPosition.setWta(wta);*/
        //}

        oldPosition.setStartDate(positionDTO.getStartDate());
        oldPosition.setEndDate(positionDTO.getEndDate());
        oldPosition.setWorkingDaysInWeek(positionDTO.getWorkingDaysInWeek());
        oldPosition.setTotalWeeklyHours(positionDTO.getTotalWeeklyHours());
        oldPosition.setAvgDailyWorkingHours(positionDTO.getAvgDailyWorkingHours());
        oldPosition.setHourlyWages(positionDTO.getHourlyWages());
        oldPosition.setSalary(positionDTO.getSalary());

    }

    /*
     * @auth vipul
     * used to get all positions of organization n buy organization and staff Id
     * */
    public List<PositionQueryResult> getAllPositionByStaff(long id, long unitEmploymentId, long staffId, String type) {

        Long unitId = organizationService.getOrganization(id, type);

        Staff staff = staffGraphRepository.findOne(staffId);
        if (!Optional.ofNullable(staff).isPresent()) {
            throw new DataNotFoundByIdException("Invalid Staff Id" + staffId);
        }

        return positionGraphRepository.getAllPositionByStaff(unitId, unitEmploymentId, staffId);
    }

}
