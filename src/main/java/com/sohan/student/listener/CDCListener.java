package com.sohan.student.listener;

import com.sohan.student.elasticsearch.service.StudentService;
import com.sohan.student.utils.Operation;
import io.debezium.config.Configuration;
import io.debezium.embedded.EmbeddedEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.debezium.data.Envelope.FieldName.*;
import static java.util.stream.Collectors.toMap;

/**
 * This class creates, starts and stops the EmbeddedEngine, which starts the Debezium engine. The engine also
 * loads and launches the connectors setup in the configuration.
 * <p>
 * The class uses @PostConstruct and @PreDestroy functions to perform needed operations.
 *
 * @author Sohan
 * //这里启动engine，配置如何处理cdc消息
 */
@Slf4j
@Component
public class CDCListener {

    /**
     * Single thread pool which will run the Debezium engine asynchronously.
     */
    private final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * The Debezium engine which needs to be loaded with the configurations, Started and Stopped - for the
     * CDC to work.
     */
    private  EmbeddedEngine engine=null;

    /**
     * Handle to the Service layer, which interacts with ElasticSearch.
     */
    private StudentService studentService;

    /**
     * Constructor which loads the configurations and sets a callback method 'handleEvent', which is invoked when
     * a DataBase transactional operation is performed.
     *
     * @param studentConnector
     */

    @Autowired
    public CDCListener(Configuration studentConnector) {
        //这里启动engine，配置如何处理cdc消息
        this.engine = EmbeddedEngine
                .create()
                .using(studentConnector)
                .notifying(this::handleEvent)
                .using((success,message,error)->{
                    if(error!=null) {
                        log.error(message, error);
                    }
                    if (success) {
                        //Engine 正常停止
                    }else{
                        LocalDateTime dateTime=LocalDateTime.now();
                        StringBuilder builder=new StringBuilder();
                        builder.append("Engine 异常停止运行：" + dateTime.toString());
                        log.error(builder.toString());
                        //重启Engine
                        this.executor.execute(engine);
                    }

                }).
                build();

    }

    /**
     * Constructor which loads the configurations and sets a callback method 'handleEvent', which is invoked when
     * a DataBase transactional operation is performed.
     *
     * @param studentConnector
     * @param studentService
     */
    private CDCListener(Configuration studentConnector, StudentService studentService) {
        this.engine = EmbeddedEngine
                .create()
                .using(studentConnector)
                .notifying(this::handleEvent)
                .using((success,message,error)->{
                    if(error!=null) {
                        log.error(message, error);
                    }
                    if (success) {
                        //Engine 正常停止
                    }else{

                        LocalDateTime dateTime=LocalDateTime.now();
                        StringBuilder builder=new StringBuilder();
                        builder.append("Engine 异常停止运行：" + dateTime.toString());
                        log.error(builder.toString());
                    }

                }).build();

        this.studentService = studentService;
    }

    /**
     * The method is called after the Debezium engine is initialized and started asynchronously using the Executor.
     */
    @PostConstruct
    private void start() {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {

                try{
                    engine.run();
                }catch (Exception e){
                    log.error("", e);
                }
                System.out.println("end");

                }

        });
    }

    /**
     * This method is called when the container is being destroyed. This stops the debezium, merging the Executor.
     */
    @PreDestroy
    private void stop() {
        if (this.engine != null) {
            this.engine.stop();
        }
    }

    /**
     * This method is invoked when a transactional action is performed on any of the tables that were configured.
     *
     * @param sourceRecord
     */
    private void handleEvent(SourceRecord sourceRecord) {
        Struct sourceRecordValue = (Struct) sourceRecord.value();
        System.out.println(Thread.currentThread().getName());

        try {
            if (sourceRecordValue != null) {
                Operation operation = Operation.forCode((String) sourceRecordValue.get(OPERATION));

                //Only if this is a transactional operation.
                if (operation != Operation.READ) {

                    Map<String, Object> message;
                    String record = AFTER; //For Update & Insert operations.

                    if (operation == Operation.DELETE) {
                        record = BEFORE; //For Delete operations.
                    }

                    //Build a map with all row data received.
                    Struct struct = (Struct) sourceRecordValue.get(record);
                    message = struct.schema().fields().stream()
                            .map(Field::name)
                            .filter(fieldName -> struct.get(fieldName) != null)
                            .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
                            .collect(toMap(Pair::getKey, Pair::getValue));

                    //Call the service to handle the data change.
                    //this.studentService.maintainReadModel(message, operation);
                    log.info("Data Changed: {} with Operation: {}", message, operation.name());
                }
            }
           // throw new RuntimeException();
        } catch (Exception e) {
            log.error("", e) ;
         //  throw e;
        }
        

    }
}
