package com.cursor.demo.cursor;

import com.cursor.demo.bo.LongCursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class CursorUtil {

    /**
     * 名单任务开关
     */

    @Value("${push.namelist.enable:false}")
    private boolean namelistEnable;

    /**
     * 后台连接池
     */
    @Resource
    @Qualifier("sqlSessionFactory")
    private SqlSessionFactory nameListSqlSessionFactory;

    @Autowired
    @Qualifier("fileCursor")
    protected CursorManager cursorManager;


    public static volatile Boolean namelistLoaded = false;


    public static volatile LongCursor nameListCursor;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");

    Logger logger = LoggerFactory.getLogger(CursorUtil.class);


    public LongCursor read(String filePath){
        return cursorManager.read(filePath);
    }

    public void write(String filename, LongCursor cursor) throws IOException {
        cursorManager.write(filename, cursor);
    }


    public LongCursor read() throws Exception {

        SqlSession session = null;
        try {
            session = nameListSqlSessionFactory.openSession();
            nameListCursor = initCursor(session, nameListCursor);
            if (nameListCursor == null) {//namelist表为空
                logger.info("namelist table is empty.");
                //为空任务已经加载完成
                namelistLoaded = true;
                return nameListCursor;
            }

            Date maxTransTime = getTransTime(session, true);
            long max = maxTransTime.getTime();
            nameListCursor.moveFoward(14400000l, max + 1000L);
            logger.info("scan max : " + sdf.format(new Date(max)));
            logger.info("scan namelist from:{},to:{}",
                    sdf.format(new Date(nameListCursor.getFrom())),
                    sdf.format(new Date(nameListCursor.getTo())));
            return nameListCursor;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (session != null)
                session.close();
        }
    }



    public LongCursor initCursor(SqlSession session, LongCursor cursor) throws SQLException, ParseException {

        if(cursor!=null){
            return cursor;
        }
        Date minTransTime = getTransTime(session, false);
        if (minTransTime == null) return null;
        long min = minTransTime.getTime();

        return new LongCursor(min, min);
    }


    public Date getLastTransTime(SqlSession session, Long time) throws SQLException {
        Date date = null;
        String method = "min";
        String field = "UPDATE_TIME";
        String tableName = "nl_ol_name_list";
        StringBuffer sb = new StringBuffer();
        sb.append("select ").append(method).append("(" + field + ")")
                .append(" from " + tableName).append(" where UPDATE_TIME >= ?");
        logger.info("---sql---:" + sb.toString());
        PreparedStatement ps = session.getConnection().prepareStatement(
                sb.toString());
        ps.setTimestamp(1, new java.sql.Timestamp(time));
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            // 目前默认使用时间字段作为cursor的域
            if (rs.next()) {
                date = rs.getTimestamp(1);
            }
        } finally {
            ps.close();
            if (rs != null) {
                rs.close();
            }
        }
        return date;
    }

    private Date getTransTime(SqlSession session, boolean last)
            throws SQLException {
        Date date = null;
        String method = last ? "max" : "min";
        String field = "UPDATE_TIME";
        String tableName = "nl_ol_name_list";
        StringBuffer sb = new StringBuffer();
        // 从指定的表获取最大或最小时间
        sb.append("select ").append(method).append("(" + field + ")")
                .append(" from " + tableName);
        logger.info("---sql---:" + sb.toString());
        PreparedStatement ps = session.getConnection().prepareStatement(
                sb.toString());
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            // 目前默认使用时间字段作为cursor的域
            if (rs.next()) {
                date = rs.getTimestamp(1);
            }
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
        return date;
    }

}
