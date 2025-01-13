package com.wjp.web.service;

import com.wjp.web.model.entity.Generator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GeneratorServiceTest {


    @Resource
    private GeneratorService generatorService;

    @Test
    public void testInsert() {
        Generator generator = generatorService.getById(39L);
        for (long i = 40; i < 100041; i++) {
            generator.setId(i);
            generator.setUserId(1877360642009055234L);
            generatorService.save(generator);
        }
    }

}