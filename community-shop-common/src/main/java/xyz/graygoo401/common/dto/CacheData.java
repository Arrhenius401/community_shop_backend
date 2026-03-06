package xyz.graygoo401.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 带逻辑过期时间的缓存数据封装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheData<T> {
    // 实际缓存数据
    private T data;
    // 逻辑过期时间（Redis 不依赖这个过期，仅用于业务判断）
    private LocalDateTime expireTime;
}