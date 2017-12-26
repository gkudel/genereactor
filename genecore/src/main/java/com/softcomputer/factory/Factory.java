package com.softcomputer.factory;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Factory<TEntity> {
    TEntity create(ResultSet resultSet) throws SQLException;
}
