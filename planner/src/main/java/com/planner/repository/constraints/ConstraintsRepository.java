package com.planner.repository.constraints;

import com.planner.domain.constarints.Constraint;
import com.planner.repository.common.MongoBaseRepository;

import java.math.BigInteger;

public interface ConstraintsRepository extends MongoBaseRepository<Constraint,BigInteger>{
}
