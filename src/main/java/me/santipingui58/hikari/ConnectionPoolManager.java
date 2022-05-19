package me.santipingui58.hikari;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPoolManager {

	 private final Main plugin;
	 
	    private String hostname;
	    private String port;
	    private String database;
	    private String username;
	    private String password;
	    
	    private int minimumConnections;
	    private int maximumConnections;
	    private long connectionTimeout;
	    private String testQuery;
	    private HikariDataSource dataSource;
	    
	    public ConnectionPoolManager(Main plugin) {
	        this.plugin = plugin;
	        init();
	        setupPool();
	    }
	
	
	
	
	private void init() {
		hostname = plugin.getConfig().getString("database.hostname");
        port = plugin.getConfig().getString("database.port");
        database = plugin.getConfig().getString("database.database");
        username = plugin.getConfig().getString("database.username");
        password = plugin.getConfig().getString("database.password");

        minimumConnections = plugin.getConfig().getInt("database.min-connections");
        maximumConnections = plugin.getConfig().getInt("database.max-connections");
        connectionTimeout = plugin.getConfig().getLong("database.timeout");
        testQuery = "";
        
    }
	
	
	private void setupPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://" +
                        hostname +
                        ":" +
                        port +
                        "/" +
                        database+"?useSSL=false&allowMultiQueries=true"
        );
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(minimumConnections);
        config.setMaximumPoolSize(maximumConnections);
        config.setConnectionTimeout(connectionTimeout);
        config.setConnectionTestQuery(testQuery);
        dataSource = new HikariDataSource(config);
    }
	
	
	
	 public Connection getConnection() throws SQLException {
	        return dataSource.getConnection();
	    }
	
	
	 public void close(Connection conn, PreparedStatement ps, ResultSet res) {
	        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
	        if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
	        if (res != null) try { res.close(); } catch (SQLException ignored) {}
	    }
	 
	 
	 public void closePool() {
	        if (dataSource != null && !dataSource.isClosed()) {
	            dataSource.close();
	        }
	    }
	
}
