package com.kairos.persistence.repository.cta;

import com.kairos.dto.activity.cta.CTAResponseDTO;
import com.kairos.dto.activity.cta.CTARuleTemplateDTO;
import com.kairos.persistence.model.cta.CostTimeAgreement;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author pradeep
 * @date - 3/8/18
 */

public interface CustomCostTimeAgreementRepository {

    List<CTAResponseDTO> findCTAByCountryId(Long countryId);

    CTAResponseDTO getOneCtaById(BigInteger ctaId);

    CostTimeAgreement getCTAByIdAndOrganizationSubTypeAndCountryId(Long organizationSubTypeId, Long countryId, BigInteger ctaId);

    List<CTAResponseDTO> getAllCTAByOrganizationSubType(Long countryId, Long organizationSubTypeId);

    List<CTAResponseDTO> findCTAByUnitId(Long unitId);

    Boolean isCTAExistWithSameNameInUnit(Long unitId, String name, BigInteger ctaId);

    List<CTAResponseDTO> getDefaultCTA(Long unitId, Long expertiseId);
    List<CTAResponseDTO> getDefaultCTAOfExpertiseAndDate(Long unitId, Long expertiseId,LocalDate selectedDate);

    List<CTAResponseDTO> getParentCTAByUpIds(List<Long> employmentIds);

    List<CTAResponseDTO> getCTAByUpIds(Set<Long> employmentIds);

    CTAResponseDTO getCTAByEmploymentIdAndDate(Long employmentId, Date date);

    List<CTAResponseDTO> getCTAByEmploymentIds(Collection<Long> employmentIds, Date date);

    List<CTAResponseDTO> getCTAByEmploymentIdsAndDate(List<Long> employmentIds, Date startDate, Date endDate);

    List<CostTimeAgreement> getCTAByEmployment(Long employmentId);

    void disableOldCta(BigInteger oldctaId, LocalDate endDate);
    void setEndDateToCTAOfEmployment(Long employmentId, LocalDate endDate);

    List<CTAResponseDTO> getCTAByEmploymentIdBetweenDate(Long employmentId, Date startDate, Date endDate);

    List<CTARuleTemplateDTO> getCTARultemplateByEmploymentId(Long employmentId);

    boolean isEmploymentCTAExistsOnDate(Long employmentId,LocalDate localDate,BigInteger ctaId);
    boolean isGapExistsInEmploymentCTA(Long employmentId,LocalDate localDate,BigInteger ctaId);
    CTAResponseDTO findCTAById(BigInteger ctaId);
    boolean existsOngoingCTAByEmployment(Long employmentId,Date endDate);
    List<CTAResponseDTO> getEmploymentIdsByAccountTypes(Set<Long> employmentIds,Set<String> accountTypes);
}
