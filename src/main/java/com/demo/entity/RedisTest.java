package com.demo.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RedisTest implements Serializable {

    int id;

    String name;

    List<String> nameList;
}
