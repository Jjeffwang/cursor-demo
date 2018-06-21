package com.cursor.demo.thread;

import com.cursor.demo.bo.LongCursor;
import com.cursor.demo.cursor.CursorManager;
import com.cursor.demo.cursor.CursorUtil;
import com.cursor.demo.pojo.NameList;
import com.cursor.demo.task.QueryTask;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyThread extends ExecuteService {

    /**
     * 名单任务游标读取路径
     */
    @Value("${push.namelist.cursor.path:#{systemProperties['java.io.tmpdir']}}")
    private String cursorPath;

    @Resource
    @Qualifier("sqlSessionFactory")
    private SqlSessionFactory nameListSqlSessionFactory;

    @Autowired
    @Qualifier("threadPool")
    private ThreadPoolExecutor executor;

    @Autowired
    private CursorUtil cursorUtil;

    public static volatile LongCursor nameListCursor;

    Logger logger = LoggerFactory.getLogger(MyThread.class);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");


    public List<NameList> queryList(LongCursor cursor) throws InterruptedException, ExecutionException {
        List<QueryTask> tasks = new ArrayList<>();
        QueryTask queryTask = new QueryTask(nameListSqlSessionFactory, cursor);
        tasks.add(queryTask);
        List<Future<List<NameList>>> completed = executor.invokeAll(tasks);
        List<NameList> nameLists = new ArrayList<>();
        for (Future<List<NameList>> f : completed) {
            Collection<NameList> sOrders = f.get();
            if (sOrders != null && !sOrders.isEmpty())
                nameLists.addAll(sOrders);
        }
        return nameLists;
    }


    public void loadNameList() {

        String fileFullName = cursorPath + "/" + "namelist.cursor";
        try {
            logger.info("开始定时加载名单数据------->{}", sdf.format(new Date()));
            nameListCursor = cursorUtil.read(fileFullName);
            nameListCursor = cursorUtil.read();
            cursorUtil.write(fileFullName, doLoader(nameListCursor));
            logger.info("名单数据加载成功------>{}", sdf.format(new Date()));
        } catch (Exception e) {
            logger.error("定时加载任务执行失败", e);
        }
    }

    private LongCursor doLoader(LongCursor cursor) {
        String fileFullName = cursorPath + "/" + "namelist.cursor";

        try {
            logger.info("开始定时加载名单数据------->{}", sdf.format(new Date()));
            nameListCursor = cursorUtil.read(fileFullName);
            nameListCursor = cursorUtil.read();

            if (cursor == null || cursor.getFrom() > cursor.getTo())
                return cursor;
            //如果开始时间和结束时间相同则认为名单读取已经结束
            if (cursor.getFrom() >= cursor.getTo()) {
                logger.info("名单数据加载完成!last:{}", sdf.format(new Date(nameListCursor.getTo())));
                return cursor;
            }
            //查询数据
            List<NameList> nameLists = queryList(cursor);
            if (nameLists == null || nameLists.isEmpty()) {
                return cursor;
            }
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }


    @Override
    protected void doExecute() {
        scheduledService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                loadNameList();
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }
}
