package com.linkedin.thirdeye.dbi.dao;

import com.linkedin.thirdeye.common.persistence.PersistenceApp;
import com.linkedin.thirdeye.constant.AnomalyFeedbackType;
import com.linkedin.thirdeye.constant.FeedbackStatus;
import com.linkedin.thirdeye.db.dao.AnomalyFeedbackDAO;
import com.linkedin.thirdeye.dbi.DaoProviderUtil;
import com.linkedin.thirdeye.db.entity.AnomalyFeedback;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public class AnomalyFeedbackDAOTest {


  public static void main(String[] args) throws Exception {
    URL url = AnomalyFeedbackDAOTest.class.getResource("/persistence.yml");
    PersistenceApp.main(new String[]{"db" ,"migrate",new File(url.getFile()).getParent()});
    File configFile = new File(url.toURI());
    DaoProviderUtil.init(configFile);
    DataSource dataSource = DaoProviderUtil.getInstance(DataSource.class);
    AnomalyFeedbackDAO dao = DaoProviderUtil.getInstance(AnomalyFeedbackDAO.class);

    Connection conn = dao.getConnection();

    //INSERT 3 rows
    for (int i = 0; i < 3; i++) {
      AnomalyFeedback feedback = new AnomalyFeedback();
      feedback.setComment("asdsad-" + i);
      feedback.setStatus(FeedbackStatus.NEW);
      feedback.setFeedbackType(AnomalyFeedbackType.ANOMALY);
      Long feedbackId = dao.save(feedback);
      System.out.println("Saved Feedback ID:" + feedbackId);
    }
    //READ ALL ROWS
    ResultSet selectionResultSet =
        conn.createStatement().executeQuery("select * from anomaly_feedback");
    int count = 0;
    while (selectionResultSet.next()) {
      count++;
      System.out.println(selectionResultSet.getString(2));
    }
    System.out.println("Results found:" + count);
    //FIND BY ID
    AnomalyFeedback anomalyFeedback = dao.findById(1L);
    System.out.println("Retreived " + anomalyFeedback);

    //FIND BY PARAMS
    Map<String, Object> filters = new HashMap<>();
    filters.put("status", "NEW");
    List<AnomalyFeedback> results = dao.findByParams(filters);
    for (AnomalyFeedback result : results) {
      System.out.println("Retreived result: " + result);
    }

    //UPDATE TEST
    AnomalyFeedback updateFeedback = new AnomalyFeedback();
    updateFeedback.setId(1L);
    updateFeedback.setStatus(FeedbackStatus.RESOLVED);
    updateFeedback.setFeedbackType(AnomalyFeedbackType.NOT_ANOMALY);
    int updatedRows = dao.update(updateFeedback);
    System.out.println("Num rows Updated " + updatedRows);

    //READ THE UPDATED ROW
    AnomalyFeedback updatedFeedback = dao.findById(1L);
    System.out.println("Retreived updatedFeedback: " + updatedFeedback);

    //parameterized sql
    String parameterizedSQL = "select * from AnomalyFeedback where status = :status and feedbackType = :feedbackType";
    Map<String, Object> parameterMap = new HashMap<>();
    parameterMap.put("status", FeedbackStatus.RESOLVED);
    parameterMap.put("feedbackType", AnomalyFeedbackType.NOT_ANOMALY);
    
    List<AnomalyFeedback> feedbacks = dao.executeParameterizedSQL(parameterizedSQL , parameterMap);
    System.out.println("result executing parameterized sql:"+ feedbacks);

    //DELETE TEST
    int numRowsDeleted = dao.deleteById(1L);
    System.out.println("Num rows Deleted " + numRowsDeleted);
    
    //READ THE DELETED ROW
    AnomalyFeedback deletedFeedback = dao.findById(1L);
    System.out.println("Retreived deletedFeedback must be null: " + deletedFeedback);


  }
}
