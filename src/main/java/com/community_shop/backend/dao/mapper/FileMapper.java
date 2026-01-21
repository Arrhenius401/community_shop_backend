package com.community_shop.backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community_shop.backend.entity.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 文件Mapper接口
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {

    @Update("UPDATE file SET is_delete = 1 WHERE file_path = #{filePath}")
    int deleteByFilePath(String filePath);

}
