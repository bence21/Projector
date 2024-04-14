package com.bence.projector.server.backend.service.util;

import com.bence.projector.server.utils.ApplicationProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryUtil {

    public static Statement getStatement() throws SQLException {
        ApplicationProperties properties = ApplicationProperties.getInstance();
        Connection connection = DriverManager.getConnection(properties.springDatasourceUrl(), properties.springDatasourceUsername(), properties.springDatasourcePassword());
        return connection.createStatement(
                ResultSet.TYPE_FORWARD_ONLY, //or ResultSet.TYPE_FORWARD_ONLY
                ResultSet.CONCUR_READ_ONLY);
    }
}
