package xyz.graygoo401.ai.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import xyz.graygoo401.common.vo.ResultVO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class DateTimeTool {

    /**
     * 获取当前时间
     * @return 当前时间
     */
    @Tool(description = "返回用户当前时区的当前时间")
    public ResultVO<String> getCurrentTime() {
        String res = LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        return ResultVO.success(res);
    }

    /**
     * 设置一个闹钟
     * @param time 闹钟时间
     * @return 设置结果
     */
    @Tool(description = "设置一个指定时间的闹钟，格式为 ISO-8601")
    public ResultVO<?> setAlarm(String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        log.info("设置闹钟时间为：{}", alarmTime);
        return ResultVO.success();
    }
}