package com.sohan.student.binlog;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;

/**
 * @Author Sachin
 * @Date 2021/6/19
 **/
public class AggregationListener implements BinaryLogClient.EventListener {
    private String databaseName;
    private String tableName;

    //private Map<String ,ILisen>
    @Override
    public void onEvent(Event event) {

        System.out.println(event);
    }
}
