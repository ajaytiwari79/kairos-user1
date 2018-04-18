package com.planning.repository.skillRepository;


import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.planning.domain.skill.PlanningSkill;
import com.planning.enums.SkillLevel;
import com.planning.repository.customRepository.BaseRepository;
import com.planning.domain.skill.SkillWithLevel;
import org.drools.compiler.rule.builder.util.PackageBuilderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SkillRepository extends BaseRepository {

    private static Logger logger = LoggerFactory.getLogger(SkillRepository.class);

  /*  public PlanningSkill findById(long id){
        Select select = QueryBuilder.select().from("PlanningSkill");
        select.where(QueryBuilder.eq("id",id));
        return (PlanningSkill) findByField(select,PlanningSkill.class);
    }*/

   /* public List<PlanningSkill> findAllByIds(List ids) {
        Select select = QueryBuilder.select().from("PlanningSkill");
        select.where(QueryBuilder.in("id",ids));
        return (List<PlanningSkill>)findAllByQuery(select,PlanningSkill.class);
    }*/

   public List<SkillWithLevel> getAllSKillWithLevelByUnitId(Long unitId){
       Select select = QueryBuilder.select().from("skillwithlevel").allowFiltering();
       select.where(QueryBuilder.eq("unitid",unitId));
       return (List<SkillWithLevel>)findAllByQuery(select,SkillWithLevel.class);
   }

    public PlanningSkill findOneByExternalId(Long externalId){
        Select select = QueryBuilder.select().from("planningskill").allowFiltering();
        select.where(QueryBuilder.eq("externalid",externalId));
        return ((PlanningSkill) findByField(select,PlanningSkill.class));
    }

    public boolean deleteByExternalId(Long externalId){
        PlanningSkill planningSkill = findOneByExternalId(externalId);
        deleteById(planningSkill.getId(),PlanningSkill.class);
        return true;
    }

   public List<PlanningSkill> getAllByUnitId(long unitId){
       Select select = QueryBuilder.select().from("planningskill").allowFiltering();
       select.where(QueryBuilder.eq("unitid",unitId));
       return (List<PlanningSkill>)findAllByQuery(select,PlanningSkill.class);
   }

    public SkillWithLevel getOneSkillWithLevel(String optaSkillId, String skillLevel, long unitId){
        Select select = QueryBuilder.select().from("skillwithlevel").allowFiltering();
        select.where(QueryBuilder.eq("unitId",unitId));
        select.where(QueryBuilder.eq("skillId",optaSkillId));
        select.where(QueryBuilder.eq("skillLevel",skillLevel));
        return (SkillWithLevel) findOne(select,SkillWithLevel.class);
    }

}
