package com.eayun.common.tools;

/**
 * <p>Title: jdbc工具类</p>
 * <p>Description:  </p>
 * <p>Copyright: Copyright (c) 2012 02</p>
 * <p>Company: telgenius</p>
 * @author 陈玉良
 * @version 1.0
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcDbExecutor {
    private static final Log log = LogFactory.getLog(JdbcDbExecutor.class);

    private JdbcDbExecutor() {
    }

    /**
     * 根据查询语句得到查询结果  
     * @param sql        sql语句
     * @return           CachedRowSet结果集
     */
    @SuppressWarnings("restriction")
    public static CachedRowSet querySql(String sql) {
        log.info("DBAccess::querySql()" + sql);
        ResultSet rs = null;
        Statement stmt = null;
        Connection conn = null;
        JdbcTemplate jdbcTemplate = (JdbcTemplate) SpringBeanUtil.getBean("jdbcTemplate");
        try {
            CachedRowSet crs = new com.sun.rowset.CachedRowSetImpl();
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            crs.populate(rs);
            rs.close();
            rs = null;
            return crs;
        } catch (Exception ex) {
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            } catch (Exception e) {
                log.error("关闭连接异常！  ", e);
            }
            rs = null;
            log.info("查询sql语句失败！ " + ex.getMessage());
            log.error(ex.getMessage(), ex);
            return null;
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            } catch (Exception e) {
                log.error("关闭连接异常！  ", e);
            }
            rs = null;

        }
    }

    /**
     * 批量执行sql语句； 
     * @param sql存放标准sql语句；
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean executeSql(Vector sqlV) {
        String sql = "";
        Statement stmt = null;
        Connection conn = null;
        JdbcTemplate jdbcTemplate = (JdbcTemplate) SpringBeanUtil.getBean("jdbcTemplate");
        try {

            // 开始事务
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            for (int i = 0; i < sqlV.size(); i++) {
                sql = sqlV.elementAt(i).toString().trim();
                log.debug("jdbc 批量执行sql语句 ===》" + sql);
                stmt.executeUpdate(sqlV.elementAt(i).toString().trim());
            }
            // 提交事务
            conn.commit();
            return true;
        } catch (Exception ex) {
            log.error("jdbc 批量执行sql语句 失败" + sql, ex);
            DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error(e, e);
                }
                stmt = null;
            }
            return false;
        } finally {
            try {
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }

            } catch (Exception ex) {
                log.info("关闭Session失败！ DBAccess" + ex.getMessage());
            }
        }
    }

    /**
     * 执行标准sql语句； 
     * @param sql存放标准sql语句；
     * @return
     */
    public static boolean executeSql(String sql) {
        Statement stmt = null;
        Connection conn = null;
        JdbcTemplate jdbcTemplate = (JdbcTemplate) SpringBeanUtil.getBean("jdbcTemplate");
        try {
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception ex) {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error(e, e);
                }
                stmt = null;
            }
            log.info("执行sql语句失败！ " + ex.getMessage());
            DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            return false;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            } catch (Exception ex) {
                log.info("关闭Session失败！ DBAccess" + ex.getMessage());
            }
        }
    }

    public static boolean executeSql(String sql, Object[] params) {
        JdbcTemplate jdbcTemplate = (JdbcTemplate) SpringBeanUtil.getBean("jdbcTemplate");
        jdbcTemplate.update(sql, params);
        return false;
    }

    public static int[] batchExecuteSql(String sql, final List<Object[]> params) {
        JdbcTemplate jdbcTemplate = (JdbcTemplate) SpringBeanUtil.getBean("jdbcTemplate");
        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public int getBatchSize() {
                return params.size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] o = params.get(i);
                if (o != null) {
                    for (int j = 0; j < o.length; j++) {
                        ps.setObject(j + 1, o[j]);
                    }
                }
            }
        });
    }

    @SuppressWarnings("restriction")
    public static CachedRowSet querySql(String sql, Object[] params) {
        log.info("DBAccess::querySql()" + sql);
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        JdbcTemplate jdbcTemplate = (JdbcTemplate) SpringBeanUtil.getBean("jdbcTemplate");
        try {
            CachedRowSet crs = new com.sun.rowset.CachedRowSetImpl();
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            pstmt = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
            }
            rs = pstmt.executeQuery();
            crs.populate(rs);
            rs.close();
            rs = null;
            return crs;
        } catch (Exception ex) {
            log.error(ex, ex);
            return null;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (Exception e) {
                log.error(e, e);
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (Exception e) {
                log.info(e, e);
            }
            DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
        }
    }

    public static int[] batchExecuteSql(String[] sql) {
        JdbcTemplate jdbcTemplate = (JdbcTemplate) SpringBeanUtil.getBean("jdbcTemplate");
        return jdbcTemplate.batchUpdate(sql);
    }

    public static int[] batchExecuteSql(String sql, final Object[] params) {
        JdbcTemplate jdbcTemplate = (JdbcTemplate) SpringBeanUtil.getBean("jdbcTemplate");
        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public int getBatchSize() {
                return params.length;
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setObject(1, params[i]);
            }
        });
    }
}
