package com.sohan.student.binlog;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.*;

/**
 * @Author Sachin
 * @Date 2021/6/19
 **/
public class BinlogClient {

    private BinaryLogClient binaryLogClient;


    private final BinlogConfig config;

    private final AggregationListener listener;

    public BinlogClient(BinlogConfig config, AggregationListener listener) {
        this.config = config;
        this.listener = listener;
    }

    public void connect() {
        int corePoolSize = 1;
        int maximumPoolSize = 1;
        long keepAliveTime = 0L;

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(1);
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("binlog-thread-%d").build();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                workQueue,
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );
        executor.submit(() -> {
            binaryLogClient = new BinaryLogClient(
                    config.getHost(),
                    config.getPort(),
                    config.getUsername(),
                    config.getPassword()
            );
            if (StringUtils.isNotBlank(config.getBinlogName())
                    && !config.getPosition().equals(-1L)) {
                binaryLogClient.setBinlogFilename(config.getBinlogName());
                binaryLogClient.setBinlogPosition(config.getPosition());
            }
            binaryLogClient.registerEventListener(listener);
            try {
                //  log.info("connecting to mysql...");
                binaryLogClient.connect();
                //log.info("connected to mysql");
            } catch (Exception e) {
                e.printStackTrace();
                //log.error("mysql binlog connect error");
            }
        });
        executor.shutdown();
    }
    public void close() {
        try {
            binaryLogClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            //log.error("mysql binlog disconnect error");
        }
    }

}
