package com.kairos.persistance.repository.account_type;

import com.kairos.persistance.model.account_type.AccountType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@Repository
public interface AccountTypeMongoRepository extends  MongoRepository<AccountType,BigInteger> {

   @Query("{deleted:false,countryId:?0,_id:?1}")
   AccountType findByIdAndNonDeleted(Long countryId,BigInteger id);

   @Query("{deleted:false,countryId:?0}")
   List<AccountType> getAllAccountType(Long countryId);

   @Query("{deleted:false,countryId:?0,_id:{$in:?1}}")
   List<AccountType> getAccountTypeList(Long countryId,Set<BigInteger> ids);

   AccountType findByid(BigInteger id);

   @Query("{name:?1,countryId:?0,deleted:false}")
   AccountType findByTypeOfAccount(Long countryId,String typeOfAccount);

   @Query("{deleted:false,countryId:?0,name:?1}")
  AccountType findByNameAndNonDeleted(Long countryId,String name);



}
