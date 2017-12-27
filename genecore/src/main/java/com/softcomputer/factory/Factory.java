package com.softcomputer.factory;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Factory<TEntity> {
    TEntity create();
    TEntity createAndFill(ResultSet resultSet) throws SQLException;
    void fill(TEntity object, ResultSet resultSet) throws SQLException;
}
