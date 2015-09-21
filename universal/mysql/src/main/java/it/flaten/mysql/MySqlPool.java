package it.flaten.mysql;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

public class MySqlPool {
    private final ComboPooledDataSource dataSource;

    public MySqlPool(String name) {
        this.dataSource = new ComboPooledDataSource(name);
    }

    public void setUrl(String url) throws PropertyVetoException {
        this.dataSource.setDriverClass("com.mysql.jdbc.Driver");
        this.dataSource.setJdbcUrl(url);
    }

    public void setUser(String user) {
        this.dataSource.setUser(user);
    }

    public void setPassword(String password) {
        this.dataSource.setPassword(password);
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public void close() {
        this.dataSource.close();
    }
}
