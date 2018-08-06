package com.idleroom.web.entity;

import java.util.Collection;

public class Page<T> {
    public long count;      //数据总量
    public long pageSize;   //分页大小
    public long curPage;    //当前分页
    public long num;        //数据总量
    public Collection<T> content;
}
