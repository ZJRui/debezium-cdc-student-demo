package com.sohan.student.binlog;

/**
 * @Author Sachin
 * @Date 2021/6/20
 **/
public class TestMain {

    public static void main(String[] args) {


        BinlogConfig binlogConfig = new BinlogConfig();
        binlogConfig.setBinlogName("mysql-bin.000012");
        binlogConfig.setHost("localhost");
        binlogConfig.setPassword("root");
        binlogConfig.setPort(3306);
        binlogConfig.setUsername("root");
        binlogConfig.setPosition(-1L);

        BinlogClient client = new BinlogClient(binlogConfig, new AggregationListener());
        client.connect();

        try {
            Thread.sleep(1000 * 60);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        client.close();

    }
}
