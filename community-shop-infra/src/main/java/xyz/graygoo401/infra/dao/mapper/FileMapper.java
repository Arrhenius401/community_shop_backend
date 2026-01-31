package xyz.graygoo401.infra.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import xyz.graygoo401.infra.dao.entity.File;

/**
 * 文件Mapper接口
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {

    @Update("UPDATE file SET is_delete = 1 WHERE file_path = #{filePath}")
    int deleteByFilePath(String filePath);

}
