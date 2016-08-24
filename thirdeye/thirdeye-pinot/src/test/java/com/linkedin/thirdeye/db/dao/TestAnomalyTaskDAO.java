package com.linkedin.thirdeye.db.dao;

import java.util.List;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedin.thirdeye.anomaly.monitor.MonitorConstants.MonitorType;
import com.linkedin.thirdeye.anomaly.monitor.MonitorTaskInfo;
import com.linkedin.thirdeye.anomaly.task.TaskConstants.TaskStatus;
import com.linkedin.thirdeye.anomaly.task.TaskConstants.TaskType;
import com.linkedin.thirdeye.db.entity.AnomalyJobSpec;
import com.linkedin.thirdeye.db.entity.AnomalyTaskSpec;

public class TestAnomalyTaskDAO extends AbstractDbTestBase {

  private Long anomalyTaskId1;
  private Long anomalyTaskId2;
  private Long anomalyJobId;

  @Test
  public void testCreate() throws JsonProcessingException {
    AnomalyJobSpec testAnomalyJobSpec = getTestJobSpec();
    anomalyJobId = anomalyJobDAO.save(testAnomalyJobSpec);
    anomalyTaskId1 = anomalyTaskDAO.save(getTestTaskSpec(testAnomalyJobSpec));
    Assert.assertNotNull(anomalyTaskId1);
    anomalyTaskId2 = anomalyTaskDAO.save(getTestTaskSpec(testAnomalyJobSpec));
    Assert.assertNotNull(anomalyTaskId2);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testFindAll() {
    List<AnomalyTaskSpec> anomalyTasks = anomalyTaskDAO.findAll();
    Assert.assertEquals(anomalyTasks.size(), 2);
  }

  @Test(dependsOnMethods = { "testFindAll" })
  public void testUpdateStatusAndWorkerId() {
    TaskStatus oldStatus = TaskStatus.WAITING;
    TaskStatus newStatus = TaskStatus.RUNNING;
    Long workerId = 1L;

    boolean status  = anomalyTaskDAO.updateStatusAndWorkerId(workerId, anomalyTaskId1, oldStatus, newStatus);
    AnomalyTaskSpec anomalyTask = anomalyTaskDAO.findById(anomalyTaskId1);
    Assert.assertTrue(status);
    Assert.assertEquals(anomalyTask.getStatus(), newStatus);
    Assert.assertEquals(anomalyTask.getWorkerId(), workerId);
  }

  @Test(dependsOnMethods = {"testUpdateStatusAndWorkerId"})
  public void testFindByStatusOrderByCreationTimeAsc() {
    List<AnomalyTaskSpec> anomalyTasks = anomalyTaskDAO.findByStatusOrderByCreateTimeAscending(TaskStatus.WAITING);
    Assert.assertEquals(anomalyTasks.size(), 1);
  }

  @Test(dependsOnMethods = {"testFindByStatusOrderByCreationTimeAsc"})
  public void testUpdateStatusAndTaskEndTime() {
    TaskStatus oldStatus = TaskStatus.RUNNING;
    TaskStatus newStatus = TaskStatus.COMPLETED;
    long taskEndTime = System.currentTimeMillis();
    anomalyTaskDAO.updateStatusAndTaskEndTime(anomalyTaskId1, oldStatus, newStatus, taskEndTime);
    AnomalyTaskSpec anomalyTask = anomalyTaskDAO.findById(anomalyTaskId1);
    Assert.assertEquals(anomalyTask.getStatus(), newStatus);
    Assert.assertEquals(anomalyTask.getTaskEndTime(), taskEndTime);
  }

  @Test(dependsOnMethods = {"testUpdateStatusAndTaskEndTime"})
  public void testFindByJobIdStatusNotIn() {
    TaskStatus status = TaskStatus.COMPLETED;
    List<AnomalyTaskSpec> anomalyTaskSpecs = anomalyTaskDAO.findByJobIdStatusNotIn(anomalyJobId, status);
    Assert.assertEquals(anomalyTaskSpecs.size(), 1);
  }

  @Test(dependsOnMethods = {"testFindByJobIdStatusNotIn"})
  public void testDeleteRecordOlderThanDaysWithStatus() {
    TaskStatus status = TaskStatus.COMPLETED;
    int numRecordsDeleted = anomalyTaskDAO.deleteRecordsOlderThanDaysWithStatus(0, status);
    Assert.assertEquals(numRecordsDeleted, 1);
  }

  AnomalyTaskSpec getTestTaskSpec(AnomalyJobSpec anomalyJobSpec) throws JsonProcessingException {
    AnomalyTaskSpec jobSpec = new AnomalyTaskSpec();
    jobSpec.setJobName("Test_Anomaly_Task");
    jobSpec.setStatus(TaskStatus.WAITING);
    jobSpec.setTaskType(TaskType.MONITOR);
    jobSpec.setTaskStartTime(new DateTime().minusDays(20).getMillis());
    jobSpec.setTaskEndTime(new DateTime().minusDays(10).getMillis());
    jobSpec.setTaskInfo(new ObjectMapper().writeValueAsString(getTestMonitorTaskInfo()));
    jobSpec.setJobId(anomalyJobSpec.getId());
    return jobSpec;
  }

  static MonitorTaskInfo getTestMonitorTaskInfo() {
    MonitorTaskInfo taskInfo = new MonitorTaskInfo();
    taskInfo.setJobExecutionId(1L);
    taskInfo.setMonitorType(MonitorType.UPDATE);
    return taskInfo;
  }
}
