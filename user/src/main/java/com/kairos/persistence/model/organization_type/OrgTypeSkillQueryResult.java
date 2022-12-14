package com.kairos.persistence.model.organization_type;

import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * Created by prabjot on 12/4/17.
 */
@QueryResult
public class OrgTypeSkillQueryResult {

    private List<Map<String,Object>> skillList;
    private Long id;
    private String name ;
    private String description;

    public OrgTypeSkillQueryResult() {
        // DC
    }

    public void setSkillList(List<Map<String, Object>> skillList) {
        this.skillList = skillList;
    }

    public List<Map<String, Object>> getSkillList() {
        return skillList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
