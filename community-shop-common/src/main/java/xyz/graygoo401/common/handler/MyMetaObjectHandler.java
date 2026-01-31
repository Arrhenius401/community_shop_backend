package xyz.graygoo401.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器
 * 通常情况下，自定义的方法不会触发自动填充
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * MyBatis-Plus 的插入填充
     * @param metaObject 元数据对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        // 这里的 fieldName 是 Java 实体类的属性名，而不是数据库字段名
        // 并且实体类没有对应属性名时，不报错
        // MyBatis-Plus 的 strictInsertFill 和 strictUpdateFill 方法在设计时就考虑到了通用性
        // (1) 检测属性是否存在：它会先检查当前正在操作的实体类中是否有 updateTime 这个属性的 Setter 方法。
        // (2) 检测类型是否匹配：检查实体类中该属性的类型是否为你指定的类型（如 LocalDateTime.class）。
        // (3) 检测字段值是否为空：默认情况下，如果实体类中该字段已经手动设置了值（非空），它就不会进行覆盖填充。
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * MyBatis-Plus 的更新填充
     * @param metaObject 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}