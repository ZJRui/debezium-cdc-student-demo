package com.sohan.student.binlog;

/**
 * @Author Sachin
 * @Date 2021/6/19
 **/
public interface IListener {

    /**
     *
     */
    void register();

    void onEvent(BinlogRowData eventData);
}
