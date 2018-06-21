//package com.cursor.demo.controller;
//
//
//import com.cursor.demo.bo.LongCursor;
//import com.cursor.demo.cursor.CursorManager;
//import com.cursor.demo.mapper.NameListMapper;
//import com.cursor.demo.pojo.NameList;
//import com.cursor.demo.pojo.NameListExample;
//import org.apache.ibatis.session.SqlSession;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//public class TestController {
//
//    Logger logger = LoggerFactory.getLogger(TestController.class);
//
//    /**
//     * 名单任务游标读取路径
//     */
//    @Value("${push.namelist.cursor.path:#{systemProperties['java.io.tmpdir']}}")
//    private String cursorPath;
//
////    /**
////     * 扫描数据时间跨度
////     */
////    @Value("${push.namelist.duration:14400000l}")
////    private long duration;
//    /**
//     * 名单任务开关
//     */
//
//    @Value("${push.namelist.enable:false}")
//    private boolean namelistEnable;
//
//    /**
//     * 最大的连续取数为空的次数
//     */
//    @Value("${push.namelist.maxContinuousEmptyCount:3}")
//    private int maxContinuousEmptyCount;
//
//    /**
//     * 连续取数为空的次数
//     */
//    private int continuousEmptyCount;
//
//    /**
//     * 后台连接池
//     */
//    @Resource
//    @Qualifier("sqlSessionFactory")
//    private SqlSessionFactory nameListSqlSessionFactory;
//
//    @Autowired
//    @Qualifier("fileCursor")
//    protected CursorManager cursorManager;
//
//
//    public static volatile Boolean namelistLoaded = false;
//
//
//    public static volatile LongCursor nameListCursor;
//
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//
//
//    @Scheduled(fixedDelayString = "${push.namelist.fixedRate:60000}")
//    public void loadNameList() {
//        String fileFullName = cursorPath + "/" + "namelist.cursor";
//        if (!namelistEnable || namelistLoaded)
//            return;
//        try {
//            logger.info("开始定时加载名单数据------->{}", sdf.format(new Date()));
//            nameListCursor = cursorManager.read(fileFullName);
//            nameListCursor = read();
//            write(fileFullName, doLoader(nameListCursor));
//            logger.info("名单数据加载成功------>{}", sdf.format(new Date()));
//        } catch (Exception e) {
//            logger.error("定时加载任务执行失败", e);
//        }
//    }
//    private LongCursor doLoader(LongCursor cursor) {
//
//        if (cursor == null || cursor.getFrom() > cursor.getTo())
//            return cursor;
//        Date begin = new Date(cursor.getFrom() - 2000);
//        Date end = new Date(cursor.getTo());
//        //如果开始时间和结束时间相同则认为名单读取已经结束
//        if (cursor.getFrom() >= cursor.getTo()) {
//            logger.info("名单数据加载完成!last:{}", sdf.format(new Date(nameListCursor.getTo())));
//            namelistLoaded = true;
//            return cursor;
//        }
//        //查询数据
//        List<NameList> nameLists = queryNameList(begin, end, "gray");
//        if (nameLists == null || nameLists.isEmpty()) {
//            if (begin.before(end))//避免时间戳是最大更新时间
//                continuousEmptyCount++;
//            return cursor;
//        }
//        continuousEmptyCount = 0;
//        return cursor;
//    }
//
//    private void write(String filename, LongCursor cursor) throws IOException {
//        cursorManager.write(filename, cursor);
//    }
//
//
//    private LongCursor read() throws Exception {
//
//        SqlSession session = null;
//        try {
//            session = nameListSqlSessionFactory.openSession();
//            nameListCursor = initCursor(session, nameListCursor);
//            if (nameListCursor == null) {//namelist表为空
//                logger.info("namelist table is empty.");
//                //为空任务已经加载完成
//                namelistLoaded = true;
//                return nameListCursor;
//            }
//
//            Date maxTransTime = getTransTime(session, true);
//            long max = maxTransTime.getTime();
//            nameListCursor.moveFoward(14400000l, max + 1000L);
//            logger.info("scan max : " + sdf.format(new Date(max)));
//            logger.info("scan namelist from:{},to:{}",
//                    sdf.format(new Date(nameListCursor.getFrom())),
//                    sdf.format(new Date(nameListCursor.getTo())));
//            return nameListCursor;
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            throw e;
//        } finally {
//            if (session != null)
//                session.close();
//        }
//    }
//
//
//
//    private LongCursor initCursor(SqlSession session, LongCursor cursor) throws SQLException, ParseException {
//        if (cursor != null) {
//            if (continuousEmptyCount > maxContinuousEmptyCount) {
//                Date lastTransTime = getLastTransTime(session, cursor.getTo());
//                if (lastTransTime != null)
//                    cursor.setTo(lastTransTime.getTime());
//            }
//            return cursor;
//
//        }
//        Date minTransTime = getTransTime(session, false);
//        if (minTransTime == null) return null;
//        long min = minTransTime.getTime();
//
//        return new LongCursor(min, min);
//    }
//
//
//    private Date getLastTransTime(SqlSession session, Long time) throws SQLException {
//        Date date = null;
//        String method = "min";
//        String field = "UPDATE_TIME";
//        String tableName = "nl_ol_name_list";
//        StringBuffer sb = new StringBuffer();
//        sb.append("select ").append(method).append("(" + field + ")")
//                .append(" from " + tableName).append(" where UPDATE_TIME >= ?");
//        logger.info("---sql---:" + sb.toString());
//        PreparedStatement ps = session.getConnection().prepareStatement(
//                sb.toString());
//        ps.setTimestamp(1, new java.sql.Timestamp(time));
//        ResultSet rs = null;
//        try {
//            rs = ps.executeQuery();
//            // 目前默认使用时间字段作为cursor的域
//            if (rs.next()) {
//                date = rs.getTimestamp(1);
//            }
//        } finally {
//            ps.close();
//            if (rs != null) {
//                rs.close();
//            }
//        }
//        return date;
//    }
//
//    private Date getTransTime(SqlSession session, boolean last)
//            throws SQLException {
//        Date date = null;
//        String method = last ? "max" : "min";
//        String field = "UPDATE_TIME";
//        String tableName = "nl_ol_name_list";
//        StringBuffer sb = new StringBuffer();
//        // 从指定的表获取最大或最小时间
//        sb.append("select ").append(method).append("(" + field + ")")
//                .append(" from " + tableName);
//        logger.info("---sql---:" + sb.toString());
//        PreparedStatement ps = session.getConnection().prepareStatement(
//                sb.toString());
//        ResultSet rs = null;
//        try {
//            rs = ps.executeQuery();
//            // 目前默认使用时间字段作为cursor的域
//            if (rs.next()) {
//                date = rs.getTimestamp(1);
//            }
//        } finally {
//            if (ps != null) {
//                ps.close();
//            }
//            if (rs != null) {
//                rs.close();
//            }
//        }
//        return date;
//    }
//
//
//
//    private List<NameList> queryNameList(Date begin, Date end, String type) {
//        List<NameList> nameLists = null;
//        SqlSession session = nameListSqlSessionFactory.openSession();
//        if (session == null)
//            return null;
//        try {
//            NameListMapper mapper = session.getMapper(NameListMapper.class);
//            NameListExample example = new NameListExample();
//            example.createCriteria().andTypeNotEqualTo(type).andUpdateTimeGreaterThanOrEqualTo(begin)
//                    .andUpdateTimeLessThan(end);
//            logger.info("start:" + sdf.format(begin) + "   end:" + sdf.format(end));
//            nameLists = mapper.selectByExample(example);
//        } catch (Exception e) {
//            logger.error("NameList query error: {}", e);
//        } finally {
//            session.close();
//        }
//        logger.info("nameLists.size:" + nameLists.size());
//        return nameLists;
//    }
//
//
//}
