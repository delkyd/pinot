package com.linkedin.thirdeye.db.dao;

import com.linkedin.thirdeye.db.entity.EmailConfiguration;
import java.util.List;

public class EmailConfigurationDAO extends AbstractBaseDAO<EmailConfiguration> {
  private static final String FIND_BY_FUNCTION_ID =
      "select ec from EmailConfiguration ec, AnomalyFunctionSpec fn where fn.id=:id "
          + "and fn in elements(ec.functions)";

  public EmailConfigurationDAO() {
    super(EmailConfiguration.class);
  }

  public List<EmailConfiguration> findByFunctionId(Long id) {
    return null;
//    return getEntityManager().createQuery(FIND_BY_FUNCTION_ID, entityClass)
//        .setParameter("id", id)
//        .getResultList();
  }
}
