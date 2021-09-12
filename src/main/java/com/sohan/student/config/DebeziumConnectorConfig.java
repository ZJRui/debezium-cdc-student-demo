package com.sohan.student.config;

import io.debezium.config.Field;
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
                //.with("snapshot.mode", "schema_only")
                .with("snapshot.mode", "initial")
                //.with("snapshot.mode", "when_needed")
                /**
                 * FileDatabaseHistory:A DatabaseHistory implementation that stores the schema history in a local file.
                 * 这个文件是用来存储Schema的，里面并没有表的数据，全部都是create table drop table等语句
                 *
                 */
                .with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
                .with("database.history.file.filename", "D:\\tempFiles\\DebeziumLearn\\data\\history.dat")
                /**
                 *
                 *     指定监听的数据库以及数据表
                 *    .with("database.whitelist", "mysql")
                 *    .with("table.whitelist", "mysql.customers")
                 *
                 *    注意： database.dbname属性对Mysql类型的数据库是无效的配置属性：
                 *    因为在io.debezium.testing.testcontainers.ConnectorConfiguration中有注释说明
                 *    // This property is valid for all databases except MySQL
                 *      * 此属性对于除My SQL之外的所有数据库都有效.
                 *        如果不是mysql类型的数据库，则获取配置的 database.dbname属性
                 * if (!isMySQL(driverClassName)) {
                 *configuration.with("databasse.dbname", jdbcDatabaseContainer.getDatabaseName());
                 *}
                 *
                 *
                 */
                //.with("database.dbname", studentDBName).build();
                .with("database.whitelist",studentDBName)//指定数据库
               // .with("table.whitelist",studentDBName+"."+"person")//指定表,table.whitelist 属性在新版版中被舍弃，使用
                .with("table.whitelist","studentdb.person,studentdb.student")//指定表,table.whitelist 属性在新版版中被舍弃，使用

                .build();
             //指定监听的表，这里写死监听 指定数据库下的hstudent 和person表
            //  .with("table.whitelist", studentDBName + ".hstudent" + "," + studentDBName + ".person").build();


        /**
         * 备注：
         * 1，对于不同的数据库连接 配置参数实际上是有不同的区别
         *
         * 比如对MySQL的一个简单配置：documentation/modules/ROOT/pages/connectors/mysql.adoc ,在这个配置中没有database.dbname属性
         * {
         *   "name": "inventory-connector", // <1>
         *   "config": {
         *     "connector.class": "io.debezium.connector.mysql.MySqlConnector", // <2>
         *     "database.hostname": "192.168.99.100", // <3>
         *     "database.port": "3306", // <4>
         *     "database.user": "debezium-user", // <5>
         *     "database.password": "debezium-user-pw", // <6>
         *     "database.server.id": "184054", <7>
         *     "database.server.name": "fullfillment", // <8>
         *     "database.whitelist": "inventory", // <9>
         *     "database.history.kafka.bootstrap.servers": "kafka:9092", // <10>
         *     "database.history.kafka.topic": "dbhistory.fullfillment", // <11>
         *     "include.schema.changes": "true" // <12>
         *   }
         * }
         *
         *
         * 在oracle的配置中 D:\codeProjects\debezium\documentation\modules\ROOT\pages\connectors\oracle.adoc 中就可以使用database.dbname 属性
         *
         * [source,json,indent=0]
         * ----
         * {
         *     "name": "inventory-connector",
         *     "config": {
         *         "connector.class" : "io.debezium.connector.oracle.OracleConnector",
         *         "tasks.max" : "1",
         *         "database.server.name" : "server1",
         *         "database.hostname" : "<oracle ip>",
         *         "database.port" : "1521",
         *         "database.user" : "c##xstrm",
         *         "database.password" : "xs",
         *         "database.dbname" : "ORCLCDB",
         *         "database.pdb.name" : "ORCLPDB1",
         *         "database.out.server.name" : "dbzxout",
         *         "database.history.kafka.bootstrap.servers" : "kafka:9092",
         *         "database.history.kafka.topic": "schema-changes.inventory"
         *     }
         * }
         *
         *------------------------------------------------
         *,2，table.whitelist 属性被舍弃，使用table.include.list 属性替代
         *    protected static final String TABLE_WHITELIST_NAME = "table.whitelist";
         *     protected static final String TABLE_INCLUDE_LIST_NAME = "table.include.list";
         * -----------
         *   Old, backwards-compatible "whitelist" property.
         *      *
         *@Deprecated
         *public static final Field TABLE_WHITELIST = Field.create(TABLE_WHITELIST_NAME)
                *             .withDisplayName("Deprecated: Include Tables")
                *             .withType(Type.LIST)
                *             .withWidth(Width.LONG)
                *             .withImportance(Importance.LOW)
                *             .withValidation(Field::isListOfRegex)
                *             .withInvisibleRecommender()
                *             .
        withDescription("The tables for which changes are to be captured (deprecated, use \"" + TABLE_INCLUDE_LIST.name() + "\" instead)");
         *
         *
         * =====================
         * 3，对于不再白名单之内的数据的修改BinlogReader仍然能够接收到 cdc数据，只不过是不推送给业务逻辑， Skipping update row event
         *
         * 2021-09-12 13:47:42.454 DEBUG 24672 --- [-localhost:3306] i.debezium.connector.mysql.BinlogReader  : Skipping update row event: Event{header=EventHeaderV4{timestamp=1631425662000, eventType=EXT_UPDATE_ROWS, serverId=1, headerLength=19, dataLength=489, nextPosition=5773, flags=0}, data=UpdateRowsEventData{tableId=1936, includedColumnsBeforeUpdate={0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35}, includedColumns={0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35}, rows=[
         *     {before=[133193, 133057, 2021-04-13T01:05:38.378, 2021-09-12T01:05:38, 39287, 39287, [B@11266150, [B@77881a30, [B@413afb34, [B@3b5d2c85, [B@1953e65f, null, null, null, null, null, null, [B@2ebd26df, [B@38c88b67, [B@6a23b1ff, [B@6cc4d3fa, [B@2b088b63, [B@57e7aed6, [B@20bc419c, [B@6aad0d72, null, null, [B@591d0d7, [B@6f6a4fad, [B@2d98437d, [B@2fdc0d21, 1, [B@24c07765, [B@196a4377, null, null], after=[133193, 133057, 2021-04-13T01:05:38.378, 2021-10-01T01:05:38, 39287, 39287, [B@7686788c, [B@39fcd678, [B@1ea0f57d, [B@551a7c25, [B@109d3c12, null, null, null, null, null, null, [B@4f0d676f, [B@561f68ee, [B@50e2dd5c, [B@296616fe, [B@df43aa2, [B@20816bea, [B@664f4a9b, [B@7f87714a, null, null, [B@538a7532, [B@78ff9a92, [B@64f80c84, [B@20b13c70, 1, [B@2092eeb1, [B@24bddb3b, null, null]}
         * ]}} for non-monitored table null
         *
         */
    }
}

