package com.sohan.student.binlog;

import lombok.Data;

/**
 * @Author Sachin
 * @Date 2021/6/19
 **/
@Data
public class BinlogConfig {
    private String host;
    private Integer port;
    private String username;
    private String password;


    private String binlogName;
    private Long position;
}
