package com.itcafe.springboot;

import com.idleroom.web.App;
import com.idleroom.web.service.GoodService;
import com.idleroom.web.util.generator.IdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {App.class})
public class TestIdGenerator {

    @Resource(name = "goodId")
    IdGenerator id;

    @Test
    public void test(){
        System.out.println(id.getNextId());
    }
}
