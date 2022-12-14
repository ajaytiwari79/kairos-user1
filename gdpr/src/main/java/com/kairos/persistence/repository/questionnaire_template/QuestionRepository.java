package com.kairos.persistence.repository.questionnaire_template;

import com.kairos.persistence.model.questionnaire_template.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "Select Q from Question Q where Q.countryId = ?1 and Q.deleted = false")
    List<Question> getAllMasterQuestion(Long countryId);

    @Query(value = "Select Q from questionmd Q INNER JOIN questionnaire_sectionmd_questions SQ ON Q.id = SQ.questions_id where Q.id = ?1 and SQ.questionnaire_sectionmd_id = ?2 and Q.deleted = false", nativeQuery = true)
    Question findQuestionByIdAndSectionId(Long id, Long sectionId);

    Question findByIdAndDeletedFalse(Long id);

}
