
package com.sohan.student.elasticsearch.entity;

import lombok.Data;


/**
 * ElasticSearch Student entity
 */

@Data
//@Document(indexName = "student", shards = 1, replicas = 0, refreshInterval = "-1")
public class Student {
    private Integer id;

    private String name;

    private String address;

    private String email;
}

