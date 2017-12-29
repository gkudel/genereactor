package com.softcomputer.annotationprocessor.orm;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Mapper<TEntity> {
    TEntity create();
    TEntity createAndFill(ResultSet resultSet) throws SQLException;
    void fill(TEntity object, ResultSet resultSet) throws SQLException;
}
