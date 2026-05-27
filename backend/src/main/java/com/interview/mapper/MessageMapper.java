package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试消息数据访问层
 * 继承MyBatis-Plus BaseMapper，自动获得CRUD能力
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
