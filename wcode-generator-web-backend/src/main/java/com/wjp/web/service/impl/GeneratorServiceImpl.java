package com.wjp.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wjp.web.common.ErrorCode;
import com.wjp.web.constant.CommonConstant;
import com.wjp.web.constant.FileConstant;
import com.wjp.web.exception.BusinessException;
import com.wjp.web.mapper.GeneratorMapper;
import com.wjp.web.model.dto.generator.GeneratorQueryRequest;
import com.wjp.web.model.entity.Generator;
import com.wjp.web.model.entity.User;
import com.wjp.web.model.vo.GeneratorVO;
import com.wjp.web.model.vo.UserVO;
import com.wjp.web.service.GeneratorService;
import com.wjp.web.service.UserService;
import com.wjp.web.utils.AIgorithumUtils;
import com.wjp.web.utils.SqlUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子服务实现
 *
 * @author <a href="https://github.com/liwjp">程序员鱼皮</a>
 * @from <a href="https://wjp.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class GeneratorServiceImpl extends ServiceImpl<GeneratorMapper, Generator> implements GeneratorService {

    @Resource
    private UserService userService;


    /**
     * 检验
     *
     * @param generator
     * @param add
     */

    @Override
    public void validGenerator(Generator generator, boolean add) {
        if (generator == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = generator.getName();
        String description = generator.getDescription();
        System.out.println("description = " + description);

        String tags = generator.getTags();
        // 创建时，参数不能为空
        if(StringUtils.isBlank(name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称不能为空");
        }
        if(StringUtils.isEmpty(description)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述不能为空");
        }
        if(StringUtils.isBlank(tags)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签不能为空");
        }

        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 256) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param generatorQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Generator> getQueryWrapper(GeneratorQueryRequest generatorQueryRequest) {
        QueryWrapper<Generator> queryWrapper = new QueryWrapper<>();
        if (generatorQueryRequest == null) {
            return queryWrapper;
        }

        String sortField = generatorQueryRequest.getSortField();
        String sortOrder = generatorQueryRequest.getSortOrder();
        Long id = generatorQueryRequest.getId();

        Long notId = generatorQueryRequest.getNotId();
        String searchText = generatorQueryRequest.getSearchText();
        List<String> tags = generatorQueryRequest.getTags();
        Long userId = generatorQueryRequest.getUserId();
        String name = generatorQueryRequest.getName();
        String description = generatorQueryRequest.getDescription();
        String basePackage = generatorQueryRequest.getBasePackage();
        String version = generatorQueryRequest.getVersion();
        String author = generatorQueryRequest.getAuthor();
        String distPath = generatorQueryRequest.getDistPath();
        Integer status = generatorQueryRequest.getStatus();


        List<String> tagList = generatorQueryRequest.getTags();

        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.eq(StringUtils.isNotBlank(version), "version", version);
        queryWrapper.eq(StringUtils.isNotBlank(author), "author", author);
        queryWrapper.eq(StringUtils.isNotBlank(distPath), "distPath", distPath);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }



    /**
     * 将实体类 转为封装类
     *
     * @param generator
     * @param request
     * @return
     */
    @Override
    public GeneratorVO getGeneratorVO(Generator generator, HttpServletRequest request) {
        String picture = generator.getPicture();
        // 去掉 picture 中 https://wcoder-1308962059.cos.ap-shanghai.myqcloud.com(FileConstant.COS_HOST) 这部分
        // 防止小人知道web服务器的地址，直接暴力访问，导致我的钱包空空如也
        picture = picture.substring(picture.indexOf(FileConstant.COS_HOST) + FileConstant.COS_HOST.length());

        generator.setPicture(picture);
        GeneratorVO generatorVO = GeneratorVO.objToVo(generator);
        long generatorId = generator.getId();
        // 1. 关联查询用户信息
        Long userId = generator.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
            String userAvatar = user.getUserAvatar();
            userAvatar = userAvatar.substring(userAvatar.indexOf(FileConstant.COS_HOST) + FileConstant.COS_HOST.length());
            user.setUserAvatar(userAvatar);
        }
        UserVO userVO = userService.getUserVO(user);
        generatorVO.setUser(userVO);

        return generatorVO;
    }


    /**
     * 将实体类的分页 转为 封装类的分页
     *
     * @param generatorPage
     * @param request
     * @return
     */
    /**
     * 将实体类的分页 转为 封装类的分页
     *
     * @param generatorPage
     * @param request
     * @return
     */
    @Override
    public Page<GeneratorVO> getGeneratorVOPage(Page<Generator> generatorPage, HttpServletRequest request) {
        List<Generator> generatorList = generatorPage.getRecords();
        Page<GeneratorVO> generatorVOPage = new Page<>(generatorPage.getCurrent(), generatorPage.getSize(), generatorPage.getTotal());
        if (CollUtil.isEmpty(generatorList)) {
            return generatorVOPage;
        }
        // 1. 关联查询用户信息
        // 去除picture中的https://wcoder-1308962059.cos.ap-shanghai.myqcloud.com(FileConstant.COS_HOST)
        Set<Long> userIdSet = generatorList.stream().map(Generator::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        List<GeneratorVO> generatorVOList = generatorList.stream().map(generator -> {
            GeneratorVO generatorVO = GeneratorVO.objToVo(generator);
            // 去除 picture 中的 COS_HOST URL
            String picture = generatorVO.getPicture();
            if (picture != null && picture.contains(FileConstant.COS_HOST)) {
                picture = picture.replace(FileConstant.COS_HOST, ""); // 替换 COS_HOST
                generatorVO.setPicture(picture); // 更新 picture 字段
            }

            Long userId = generator.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }

            // 去除 userAvatar 中的 COS_HOST URL
            if (user != null && user.getUserAvatar() != null) {
                String userAvatar = user.getUserAvatar();
                if (userAvatar.contains(FileConstant.COS_HOST)) {
                    userAvatar = userAvatar.replace(FileConstant.COS_HOST, ""); // 替换 COS_HOST
                    user.setUserAvatar(userAvatar); // 更新 userAvatar 字段
                }
            }


            generatorVO.setUser(userService.getUserVO(user));
            return generatorVO;
        }).collect(Collectors.toList());
        generatorVOPage.setRecords(generatorVOList);
        return generatorVOPage;
    }





    /**
     * 匹配用户
     *
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<Generator> matchGenerators(long num, User loginUser) {
        QueryWrapper<Generator> queryWrapper = new QueryWrapper<>();
        // 只查询id 和 tags字段
        queryWrapper.select("id", "tags");
        // 匹配不为空的标签(过滤掉标签为空的用户)
        queryWrapper.isNotNull("tags");
        List<Generator> generatorList = this.list(queryWrapper);
        // 登录用户的标签
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表下标 => 相似度
//        SortedMap<Integer, Long> indexDistanceMap = new TreeMap<>();
        // 能排序的数据结构
        List<Pair<Generator, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < generatorList.size(); i++) {
            Generator generator = generatorList.get(i);
            String generatorTags = generator.getTags();
            // 无标签 或者 为当前用户自己
            if (StringUtils.isBlank(generatorTags) || Objects.equals(generator.getId(), loginUser.getId())) {
                // throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户标签不能为空");
                continue;
            }
            // 转换为 List<String> 格式
            List<String> generatorTagList = gson.fromJson(generatorTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算两两标签相似度匹配
            // tagList: 当前用户标签列表
            // generatorTagList: 其他项目的标签列表
            long distance = AIgorithumUtils.minDistanceTags(tagList, generatorTagList);
            // 相似度越大，排名越靠前
            // indexDistanceMap.put(i, distance);
            list.add(new Pair<>(generator, distance));
        }
        // 按照编辑距离由小到大排序(升序)
        List<Pair<Generator, Long>> topGeneratorPairList = list.stream().sorted((a, b) -> (int) (a.getValue() - b.getValue())).limit(num).collect(Collectors.toList());
        // 原本顺序的 generatorId列表
        List<Long> generatorIdList = topGeneratorPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<Generator> generatorQueryWrapper = new QueryWrapper<>();
        // 查询所有 id 值属于 generatorIdList 中的用户记录。
        generatorQueryWrapper.in("id", generatorIdList);
        // 1、3、2
        // Generator1、Generator2、Generator3
        // 1 => Generator1、2 => Generator2、3 => Generator3
        // 根据 generator表中的数据，查询与之对应 generatorIdList中id的用户，并进行脱敏
        Map<Long, List<Generator>> generatorIdGeneratorListMap = this.list(generatorQueryWrapper).stream().map(generator -> getSafetyGenerator(generator))
                .collect(Collectors.groupingBy(Generator::getId));
        List<Generator> finalGeneratorList = new ArrayList<>();
        // 遍历 id列表
        for (Long generatorId : generatorIdList) {
            // 获取对应 id用户列表，添加到最终结果
            System.out.println("generatorIdGeneratorListMap = " + generatorIdGeneratorListMap.get(generatorId));
            finalGeneratorList.add(generatorIdGeneratorListMap.get(generatorId).get(0));
        }
        return finalGeneratorList;
    }
    /**
     * 用户脱敏
     *
     * @param originGenerator
     * @return 返回脱敏信息
     */
    @Override
    public Generator getSafetyGenerator(Generator originGenerator) {
        if (originGenerator == null) return null;
        Generator safetyGenerator = new Generator();
        safetyGenerator.setId(originGenerator.getId());
        safetyGenerator.setName(originGenerator.getName());
        safetyGenerator.setDescription(originGenerator.getDescription());
        safetyGenerator.setAuthor(originGenerator.getAuthor());
        safetyGenerator.setTags(originGenerator.getTags());
        safetyGenerator.setPicture(originGenerator.getPicture());
        safetyGenerator.setDistPath(originGenerator.getDistPath());
        safetyGenerator.setDownLoadNum(originGenerator.getDownLoadNum());
        safetyGenerator.setStatus(originGenerator.getStatus());
        safetyGenerator.setUserId(originGenerator.getUserId());
        return safetyGenerator;
    }

}




