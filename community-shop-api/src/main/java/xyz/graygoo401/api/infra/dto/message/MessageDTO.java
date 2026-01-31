package xyz.graygoo401.api.infra.dto.message;

import lombok.Data;
import xyz.graygoo401.api.infra.enums.MessageTypeEnum;
import xyz.graygoo401.api.user.dto.user.UserDTO;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long msgId;
    private UserDTO sender;   // 装配：系统消息则为 null 或虚拟对象
    private UserDTO receiver;
    private String title;
    private String content;
    private MessageTypeEnum type;
    private Boolean isRead;
    private LocalDateTime createTime;
}