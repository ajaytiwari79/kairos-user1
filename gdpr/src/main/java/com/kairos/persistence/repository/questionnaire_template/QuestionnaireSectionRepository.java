package com.kairos.persistence.repository.questionnaire_template;

import com.kairos.persistence.model.questionnaire_template.QuestionnaireSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
//@JaversSpringDataAuditable
public interface QuestionnaireSectionRepository extends JpaRepository<QuestionnaireSection, Long>  {

    @Query(value = "SELECT qs FROM QuestionnaireSection qs WHERE qs.id = ?1 and qs.deleted = false")
    QuestionnaireSection findByIdAndDeletedFalse(Long id);

    @Modifying
    @Transactional
    @Query(value = "delete from questionnaire_sectionmd_questions where questionnaire_sectionmd_id = ?1 and questions_id =?2", nativeQuery = true)
    Integer unlinkQuestionFromQuestionnaireSection(Long sectionId, Long questionId);


}
