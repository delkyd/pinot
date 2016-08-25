package com.linkedin.thirdeye.db.dao;

import com.google.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.linkedin.thirdeye.db.entity.AbstractBaseEntity;
import com.linkedin.thirdeye.dbi.GenericResultSetMapper;
import com.linkedin.thirdeye.dbi.Predicate;
import com.linkedin.thirdeye.dbi.SqlQueryBuilder;

import javax.sql.DataSource;

public class AbstractBaseDAO<E extends AbstractBaseEntity> {

  final Class<E> entityClass;

  @Inject
  GenericResultSetMapper genericResultSetMapper;

  @Inject
  SqlQueryBuilder sqlQueryBuilder;

  @Inject
  DataSource dataSource;

  /**
   * Use at your own risk!!! Ensure to close the connection after using it or it can cause a leak.
   * 
   * @return
   * @throws SQLException
   */
  public Connection getConnection() throws SQLException {
    // ensure to close the connection
    return dataSource.getConnection();
  }

  AbstractBaseDAO(Class<E> entityClass) {
    this.entityClass = entityClass;
  }

  public Long save(E entity) {
    if (entity.getId() != null) {
      //either throw exception or invoke update
      throw new RuntimeException(
          "id must be null when inserting new record. If you are trying to update call update");
    }
    return runTask(new Task<Long>() {
      @Override
      public Long handle(Connection connection) throws Exception {
        PreparedStatement insertStatement =
            sqlQueryBuilder.createInsertStatement(connection, entity);
        int affectedRows = insertStatement.executeUpdate();
        if (affectedRows == 1) {
          ResultSet generatedKeys = insertStatement.getGeneratedKeys();
          if (generatedKeys.next()) {
            entity.setId(generatedKeys.getLong(1));
          }
          return entity.getId();
        }
        return null;
      }
    }, null);
  }

  @SuppressWarnings("unchecked")
  public E findById(Long id) {
    return runTask(new Task<E>() {
      @Override
      public E handle(Connection connection) throws Exception {
        PreparedStatement selectStatement =
            sqlQueryBuilder.createFindByIdStatement(connection, entityClass, id);
        ResultSet resultSet = selectStatement.executeQuery();
        return (E) genericResultSetMapper.mapSingle(resultSet, entityClass);
      }
    }, null);
  }

  @SuppressWarnings("unchecked")
  public List<E> findAll() {
    return runTask(new Task<List<E>>() {
      @Override
      public List<E> handle(Connection connection) throws Exception {
        PreparedStatement selectStatement =
            sqlQueryBuilder.createFindAllStatement(getConnection(), entityClass);
        ResultSet resultSet = selectStatement.executeQuery();
        return (List<E>) genericResultSetMapper.mapAll(resultSet, entityClass);
      }
    }, Collections.emptyList());
  }

  public int deleteById(Long id) {
    return runTask(new Task<Integer>() {
      @Override
      public Integer handle(Connection connection) throws Exception {
        Map<String, Object> filters = new HashMap<>();
        filters.put("id", id);
        PreparedStatement deleteStatement =
            sqlQueryBuilder.createDeleteByIdStatement(connection, entityClass, filters);
        return deleteStatement.executeUpdate();
      }
    }, 0);
  }

  public int deleteByParams(Map<String, Object> filters) {
    return runTask(new Task<Integer>() {
      @Override
      public Integer handle(Connection connection) throws Exception {
        PreparedStatement deleteStatement =
            sqlQueryBuilder.createDeleteByIdStatement(connection, entityClass, filters);
        return deleteStatement.executeUpdate();
      }
    }, 0);
  }

  @SuppressWarnings("unchecked")
  public List<E> executeParameterizedSQL(String parameterizedSQL,
      Map<String, Object> parameterMap) {
    return runTask(new Task<List<E>>() {
      @Override
      public List<E> handle(Connection connection) throws Exception {
        PreparedStatement selectStatement = sqlQueryBuilder.createStatementFromSQL(connection,
            parameterizedSQL, parameterMap, entityClass);
        ResultSet resultSet = selectStatement.executeQuery();
        return (List<E>) genericResultSetMapper.mapAll(resultSet, entityClass);
      }
    }, Collections.emptyList());
  }


  @SuppressWarnings("unchecked")
  public List<E> findByParams(Map<String, Object> filters) {
    return runTask(new Task<List<E>>() {
      @Override
      public List<E> handle(Connection connection) throws Exception {
        PreparedStatement selectStatement =
            sqlQueryBuilder.createFindByParamsStatement(connection, entityClass, filters);
        ResultSet resultSet = selectStatement.executeQuery();
        return (List<E>) genericResultSetMapper.mapAll(resultSet, entityClass);
      }
    }, Collections.emptyList());
  }

  @SuppressWarnings("unchecked")
  public List<E> findByParams(Predicate predicate) {
    return runTask(new Task<List<E>>() {
      @Override
      public List<E> handle(Connection connection) throws Exception {
        PreparedStatement selectStatement =
            sqlQueryBuilder.createFindByParamsStatement(connection, entityClass, predicate);
        ResultSet resultSet = selectStatement.executeQuery();
        return (List<E>) genericResultSetMapper.mapAll(resultSet, entityClass);
      }
    }, Collections.emptyList());
  }

  public int update(E entity) {
    return runTask(new Task<Integer>() {
      @Override
      public Integer handle(Connection connection) throws Exception {
        PreparedStatement updateStatement;
        updateStatement = sqlQueryBuilder.createUpdateStatement(connection, entity, null);
        return updateStatement.executeUpdate();
      }
    }, 0);
  }

  public Integer update(E entity, Set<String> fieldsToUpdate) {
    return runTask(new Task<Integer>() {
      @Override
      public Integer handle(Connection connection) throws Exception {
        try (PreparedStatement updateStatement =
            sqlQueryBuilder.createUpdateStatement(connection, entity, fieldsToUpdate)) {
          return updateStatement.executeUpdate();
        }
      }
    }, 0);
  }

  interface Task<T> {
    T handle(Connection connection) throws Exception;
  }

  <T> T runTask(Task<T> task, T defaultReturnValue) {
    try (Connection connection = getConnection()) {
      return task.handle(connection);
    } catch (Exception e) {
      e.printStackTrace();
      return defaultReturnValue;
    }
  }
}


