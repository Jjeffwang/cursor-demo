package com.cursor.demo.task;

import com.cursor.demo.bo.LongCursor;
import com.cursor.demo.mapper.NameListMapper;
import com.cursor.demo.pojo.NameList;
import com.cursor.demo.pojo.NameListExample;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class QueryTask implements Callable<List<NameList>> {

    Logger logger = LoggerFactory.getLogger(QueryTask.class);

    private SqlSessionFactory nameListSqlSessionFactory;

    private LongCursor cursor;

    public QueryTask(SqlSessionFactory nameListSqlSessionFactory, LongCursor cursor) {
        this.nameListSqlSessionFactory = nameListSqlSessionFactory;
        this.cursor = cursor;

    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");

    @Override
    public List<NameList> call() {

        return queryNameList("white");
    }

    private List<NameList> queryNameList(String type) {
        if (cursor == null || cursor.getFrom() > cursor.getTo())
            return null;
        Date begin = new Date(cursor.getFrom() - 2000);
        Date end = new Date(cursor.getTo());
        //如果开始时间和结束时间相同则认为名单读取已经结束
        if (cursor.getFrom() >= cursor.getTo()) {
            logger.info("名单数据加载完成!last:{}", sdf.format(new Date(cursor.getTo())));
            return null;
        }
        List<NameList> nameLists = null;
        SqlSession session = nameListSqlSessionFactory.openSession();
        if (session == null)
            return null;
        try {
            NameListMapper mapper = session.getMapper(NameListMapper.class);
            NameListExample example = new NameListExample();
            example.createCriteria().andTypeNotEqualTo(type).andUpdateTimeGreaterThanOrEqualTo(begin)
                    .andUpdateTimeLessThan(end);
            logger.info("start:" + sdf.format(begin) + "   end:" + sdf.format(end));
            nameLists = mapper.selectByExample(example);
        } catch (Exception e) {
            logger.error("NameList query error: {}", e);
        } finally {
            session.close();
        }
        logger.info("nameLists.size:" + nameLists.size());
        return nameLists;
    }


}
