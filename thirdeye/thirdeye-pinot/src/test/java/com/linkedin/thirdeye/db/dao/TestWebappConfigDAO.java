package com.linkedin.thirdeye.db.dao;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.linkedin.thirdeye.dashboard.configs.AbstractConfig;
import com.linkedin.thirdeye.dashboard.configs.CollectionConfig;
import com.linkedin.thirdeye.dashboard.configs.WebappConfigFactory.WebappConfigType;
import com.linkedin.thirdeye.db.entity.WebappConfig;

public class TestWebappConfigDAO extends AbstractDbTestBase {

  private static final Logger LOG = LoggerFactory.getLogger(TestWebappConfigDAO.class);

  private static final String collection = "testCollection";
  private static final WebappConfigType configType = WebappConfigType.COLLECTION_CONFIG;
  private Long webappConfigId;


  @Test
  public void testCreate() {
    WebappConfig webappConfig = getWebappConfig();
    webappConfigId = webappConfigDAO.save(webappConfig);
    Assert.assertNotNull(webappConfigId);
    Assert.assertEquals(webappConfigDAO.findAll().size(), 1);
    WebappConfig readWebappConfig = webappConfigDAO.findById(webappConfigId);
    System.out.println(readWebappConfig);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testDuplicteCreate() {
    WebappConfig webappConfig = getWebappConfig();
    Long duplicateId = null;
    boolean insertSuccess = false;
    try {
      duplicateId = webappConfigDAO.save(webappConfig);
      insertSuccess = true;
    } catch (Exception e) {
      Assert.assertFalse(insertSuccess);
    }
    Assert.assertNull(duplicateId);
  }

  @Test(dependsOnMethods = {"testDuplicteCreate"})
  public void testFind() {
    Assert.assertEquals(webappConfigDAO.findByCollection(collection).size(), 1);
    Assert.assertEquals(webappConfigDAO.findByConfigType(WebappConfigType.COLLECTION_SCHEMA).size(), 0);
    Assert.assertEquals(webappConfigDAO.findByConfigType(configType).size(), 1);
    Assert.assertEquals(webappConfigDAO.findByCollectionAndConfigType(collection, configType).size(), 1);
  }

  @Test(dependsOnMethods = {"testFind"})
  public void testUpdate() throws Exception {
    WebappConfig webappConfig = webappConfigDAO.findById(webappConfigId);
    CollectionConfig collectionConfig = AbstractConfig.fromJSON(webappConfig.getConfig(), CollectionConfig.class);
    collectionConfig.setCollectionAlias("testAlias");
    webappConfig.setConfig(collectionConfig.toJSON());
    webappConfigDAO.update(webappConfig);

    webappConfig = webappConfigDAO.findById(webappConfigId);
    collectionConfig = AbstractConfig.fromJSON(webappConfig.getConfig(), CollectionConfig.class);
    Assert.assertEquals(collectionConfig.getCollectionAlias(), "testAlias");
  }

  @Test(dependsOnMethods = {"testUpdate"})
  public void testDelete() {
    webappConfigDAO.deleteById(webappConfigId);
    Assert.assertNull(webappConfigDAO.findById(webappConfigId));
  }

  private static WebappConfig getWebappConfig() {

    CollectionConfig collectionConfig = new CollectionConfig();
    collectionConfig.setCollectionName(collection);
    Map<String, String> derivedMetrics = new HashMap<>();
    derivedMetrics.put("dm1", "m1/m2");
    collectionConfig.setDerivedMetrics(derivedMetrics);

    WebappConfig webappConfig = new WebappConfig();
    webappConfig.setConfigId(collectionConfig.getConfigId());
    webappConfig.setCollection(collection);
    webappConfig.setConfigType(configType);
    try {
      webappConfig.setConfig(collectionConfig.toJSON());
    } catch (Exception e) {
      LOG.error("Exception in converting config to json", e);
    }
    return webappConfig;
  }
}
