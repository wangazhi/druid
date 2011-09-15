package com.alibaba.druid.bvt.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.alibaba.druid.filter.trace.TraceFilter;
import com.alibaba.druid.mock.MockDriver;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.DruidDataSourceStatManager;
import com.alibaba.druid.stat.JdbcStatContext;
import com.alibaba.druid.stat.JdbcStatManager;
import com.alibaba.druid.stat.JdbcTraceManager;

public class TraceFilterTest extends TestCase {

    private MockDriver      driver;
    private DruidDataSource dataSource;
    private TraceFilter filter;

    protected void setUp() throws Exception {
        Assert.assertEquals(0, DruidDataSourceStatManager.getInstance().getDataSourceList().size());

        driver = new MockDriver();

        dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mock:xxx");
        dataSource.setDriver(driver);
        dataSource.setInitialSize(1);
        dataSource.setMaxActive(2);
        dataSource.setMaxIdle(2);
        dataSource.setMinIdle(1);
        dataSource.setMinEvictableIdleTimeMillis(300 * 1000); // 300 / 10
        dataSource.setTimeBetweenEvictionRunsMillis(180 * 1000); // 180 / 10
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setFilters("trace");
        
        filter = (TraceFilter) dataSource.getProxyFilters().get(0);
        JdbcStatContext statContext = new JdbcStatContext();
        statContext.setTraceEnable(true);
        JdbcStatManager.getInstance().setStatContext(statContext);
    }

    protected void tearDown() throws Exception {
        dataSource.close();
        Assert.assertEquals(0, DruidDataSourceStatManager.getInstance().getDataSourceList().size());
        JdbcStatManager.getInstance().setStatContext(null);
    }

    public void test_exuecute() throws Exception {
        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("SELECT 1");
        stmt.close();
        conn.close();
    }
    
    public void test_exuecuteQuery() throws Exception {
        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT 1");
        rs.next();
        rs.close();
        stmt.close();
        conn.close();
    }
    
    public void test_preExuecuteQuery() throws Exception {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT ?");
        stmt.setInt(1, 123);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        rs.close();
        stmt.close();
        conn.close();
    }
}