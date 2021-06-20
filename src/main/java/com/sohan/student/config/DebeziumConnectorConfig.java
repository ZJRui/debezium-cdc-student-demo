package com.sohan.student.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class provides the configurations required to setup a Debezium connector for the Student Table.
 *
 * @author Sohan
 */
@Configuration
public class DebeziumConnectorConfig {

    /**
     * Student Database details.
     */
    @Value("${student.datasource.host}")
    private String studentDBHost;

    @Value("${student.datasource.databasename}")
    private String studentDBName;

    @Value("${student.datasource.port}")
    private String studentDBPort;

    @Value("${student.datasource.username}")
    private String studentDBUserName;

    @Value("${student.datasource.password}")
    private String studentDBPassword;

    private String STUDENT_TABLE_NAME = "public.student";

    /**
     * Student database connector.
     *
     * @return Configuration.
     */
    @Bean
    public io.debezium.config.Configuration studentConnector() {
        return io.debezium.config.Configuration.create()

                .with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
                .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", "D:\\tempFiles\\DebeziumLearn\\data\\student-offset.dat")

                .with("offset.flush.interval.ms", 60000)
                //name属性将作为nameSpace
                .with("name", "student-postgres-connector")

                /**
                 * The 'database.server.name' value is invalid: Must not have the same value as database.history.kafka.topic
                 * 2021-04-29 15:37:06.561 ERROR 25400 --- [pool-4-thread-1] i.d.connector.common.BaseSourceTask      : The 'database.server.name' value is invalid: A value is required
                 */
                .with("database.server.name", "server-cdcTopic")
                .with("database.hostname", studentDBHost)
                .with("database.port", studentDBPort)
                .with("database.user", studentDBUserName)
                .with("database.password", studentDBPassword)
                .with("database.serverTimezone", "Asia/Shanghai")
                .with("snapshot.mode", "schema_only")
                .with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
                .with("database.history.file.filename", "D:\\tempFiles\\DebeziumLearn\\data\\history.dat")
                /**
                 *    .with("database.whitelist", "mysql")
                 *    .with("table.whitelist", "mysql.customers")
                 */
                .with("database.dbname", studentDBName).build();
        //指定监听的表，这里写死监听 指定数据库下的hstudent 和person表
        //  .with("table.whitelist", studentDBName + ".hstudent" + "," + studentDBName + ".person").build();
    }
}
