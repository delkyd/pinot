package com.linkedin.thirdeye.db.dao;

import com.linkedin.thirdeye.api.dto.GroupByKey;
import com.linkedin.thirdeye.api.dto.GroupByRow;
import com.linkedin.thirdeye.constant.AnomalyFeedbackType;
import com.linkedin.thirdeye.constant.FeedbackStatus;
import com.linkedin.thirdeye.db.entity.AnomalyFeedback;
import com.linkedin.thirdeye.db.entity.AnomalyFunctionSpec;
import com.linkedin.thirdeye.db.entity.AnomalyResult;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAnomalyResultDAO extends AbstractDbTestBase {

  AnomalyResult anomalyResult;
  AnomalyFunctionSpec spec = getTestFunctionSpec("metric", "dataset");

  @Test
  public void testAnomalyResultCRUD() {
    anomalyFunctionDAO.save(spec);
    Assert.assertNotNull(spec);

    // create anomaly result
    anomalyResult = getAnomalyResult();

    anomalyResult.setFunction(spec);
    anomalyResultDAO.save(anomalyResult);

    AnomalyResult resultRet = anomalyResultDAO.findById(anomalyResult.getId());
    Assert.assertEquals(resultRet.getFunction(), spec);
  }

  @Test(dependsOnMethods = {"testAnomalyResultCRUD"})
  public void testGetCountByFunction() {
//    List<GroupByRow<GroupByKey, Long>> groupByRows =
//        anomalyResultDAO.getCountByFunction(0l, System.currentTimeMillis());
//    Assert.assertEquals(groupByRows.size(), 1);
//    Assert.assertEquals(groupByRows.get(0).getGroupBy().getFunctionId(), spec.getId());
//    Assert.assertEquals(groupByRows.get(0).getValue().longValue(), 1);
  }

  @Test(dependsOnMethods = {"testGetCountByFunction"})
  public void testFindUnmergedByCollectionMetricAndDimensions() {
    List<AnomalyResult> unmergedResults = anomalyResultDAO
        .findUnmergedByCollectionMetricAndDimensions(spec.getCollection(), spec.getMetric(),
            anomalyResult.getDimensions() );
    Assert.assertEquals(unmergedResults.size(), 1);
  }

  @Test(dependsOnMethods = {"testFindUnmergedByCollectionMetricAndDimensions"})
  public void testResultFeedback() {
    AnomalyResult result = anomalyResultDAO.findById(anomalyResult.getId());
    Assert.assertNotNull(result);
    Assert.assertNull(result.getFeedback());

    AnomalyFeedback feedback = new AnomalyFeedback();
    feedback.setComment("this is a good find");
    feedback.setFeedbackType(AnomalyFeedbackType.ANOMALY);
    feedback.setStatus(FeedbackStatus.NEW);
    result.setFeedback(feedback);
    anomalyResultDAO.save(result);

    AnomalyResult resultRet = anomalyResultDAO.findById(anomalyResult.getId());
    Assert.assertEquals(resultRet.getId(), result.getId());
    Assert.assertNotNull(resultRet.getFeedback());

    AnomalyFunctionSpec functionSpec = result.getFunction();

    anomalyResultDAO.deleteById(anomalyResult.getId());
    anomalyFunctionDAO.deleteById(functionSpec.getId());
  }

}
