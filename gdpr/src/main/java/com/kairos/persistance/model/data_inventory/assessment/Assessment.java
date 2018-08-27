package com.kairos.persistance.model.data_inventory.assessment;


import com.kairos.enums.AssessmentStatus;
import com.kairos.enums.AssessmentType;
import com.kairos.gdpr.Staff;
import com.kairos.persistance.model.common.MongoBaseEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.math.BigInteger;
import java.time.LocalDate;

@Document
public class Assessment extends MongoBaseEntity {

    @NotBlank
    private String name;

    private LocalDate endDate;

    private LocalDate completedDate;

    private String comment;

    private Staff assignee;

    private Staff approver;

    private AssessmentStatus  assessmentStatus=AssessmentStatus.NEW;

    private BigInteger questionnaireTemplateId;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public LocalDate getEndDate() { return endDate; }

    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getCompletedDate() { return completedDate; }

    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public Staff getAssignee() { return assignee; }

    public void setAssignee(Staff assignee) { this.assignee = assignee; }

    public Staff getApprover() { return approver; }

    public void setApprover(Staff approver) { this.approver = approver; }

    public AssessmentStatus getAssessmentStatus() { return assessmentStatus; }

    public void setAssessmentStatus(AssessmentStatus assessmentStatus) { this.assessmentStatus = assessmentStatus; }

    public BigInteger getQuestionnaireTemplateId() { return questionnaireTemplateId; }

    public void setQuestionnaireTemplateId(BigInteger questionnaireTemplateId) { this.questionnaireTemplateId = questionnaireTemplateId; }


}
