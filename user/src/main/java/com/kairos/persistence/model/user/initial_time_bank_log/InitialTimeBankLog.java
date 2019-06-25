package com.kairos.persistence.model.user.initial_time_bank_log;

import com.kairos.persistence.model.common.UserBaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.NodeEntity;


/**
 * Created By G.P.Ranjan on 25/6/19
 **/
@NodeEntity
@Getter
@Setter
@NoArgsConstructor
public class InitialTimeBankLog extends UserBaseEntity {
    private Long employmentId;
    private Long previousInitialBalanceInMinutes;
    private Long updateInitialBalanceInMinutes;

    public InitialTimeBankLog(Long employmentId, Long previousInitialBalanceInMinutes, Long updateInitialBalanceInMinutes){
        this.employmentId=employmentId;
        this.previousInitialBalanceInMinutes=previousInitialBalanceInMinutes;
        this.updateInitialBalanceInMinutes=updateInitialBalanceInMinutes;
    }

    @Override
    public String toString() {
        return "InitialTimeBankLog{" +
                "id=" + id +
                "employmentId=" + employmentId +
                ", previousInitialBalanceInMinutes=" + previousInitialBalanceInMinutes +
                ", updateInitialBalanceInMinutes=" + updateInitialBalanceInMinutes +
                '}';
    }
}
