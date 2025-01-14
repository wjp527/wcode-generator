package com.wjp.web;

import com.wjp.web.utils.AIgorithumUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class AIgorithumUtilsTest {
    @Test
    void test() {
        String str1 = "鱼皮是狗";
        String str2 = "狗是鱼皮";
        String str3 = "鱼皮不是狗";
        int score1 = AIgorithumUtils.minDistance(str1, str2);
        int score2 = AIgorithumUtils.minDistance(str1, str3);
        // 4
        System.out.println(score1);
        // 1
        System.out.println(score2);
    }

    @Test
    void testCompareTags() {
        // minDistanceTags
        List<String> tagList1 = Arrays.asList("java", "大一", "男");
        List<String> tagList2 = Arrays.asList("java", "大一", "女");
        List<String> tagList3 = Arrays.asList("python", "大二", "女");

        // 1
        int distance1 = AIgorithumUtils.minDistanceTags(tagList1, tagList2);
        // 3
        int distance2 = AIgorithumUtils.minDistanceTags(tagList1, tagList3);
        System.out.println(distance1);
        System.out.println(distance2);
    }
}
