package com.easydatabaseexport.core;

import com.easydatabaseexport.log.LogManager;
import lombok.extern.java.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DataSource
 *
 * @author lzy
 * @date 2021/10/25 18:20
 **/
@Log
public abstract class DataSource {

    private final String driver;
    private String url;
    private String username;
    private String passwd;

    protected DataSource(String driver, String url, String username, String passwd) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.passwd = passwd;
    }

    public Connection getCreateConnection(String url, String username, String passwd) throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        this.url = url;
        this.username = username;
        this.passwd = passwd;
        return DriverManager.getConnection(url, username, passwd);
    }

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        return DriverManager.getConnection(url, username, passwd);
    }

    public void close(Connection connection, Statement statement, ResultSet resultSet) {
        if (statement != null || connection != null) {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                LogManager.writeLogFile(e, log);
            }
        }
    }

    public void close(Connection connection, Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException throwable) {
                LogManager.writeLogFile(throwable, log);
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwable) {
                LogManager.writeLogFile(throwable, log);
            }
        }

    }

    public void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwable) {
                LogManager.writeLogFile(throwable, log);
            }
        }
    }
}
