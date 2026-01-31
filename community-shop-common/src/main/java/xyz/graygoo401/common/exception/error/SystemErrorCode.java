package xyz.graygoo401.common.exception.error;

import lombok.Getter;

/**
 * 系统错误码
 */
@Getter
public enum SystemErrorCode implements IErrorCode {

    SUCCESS("SYSTEM_000", 200, "操作成功"),
    FAILURE("SYSTEM_001", 500, "操作失败"),

    PARAM_ERROR("SYSTEM_002", 400, "参数错误"),
    PARAM_NULL("SYSTEM_003", 400, "参数为空"),
    NOT_FOUND("SYSTEM_004", 404, "资源不存在"),
    RELATED_DATA_MISSING("SYSTEM_005", 400, "缺少关联数据"),

    DATA_UPDATE_FAILED("SYSTEM_011", 500, "数据更新失败"),
    DATA_DELETE_FAILED("SYSTEM_012", 500, "数据删除失败"),
    DATA_INSERT_FAILED("SYSTEM_013", 500, "数据插入失败"),
    DATA_QUERY_FAILED("SYSTEM_014", 500, "数据查询失败"),

    PERMISSION_UNAUTHORIZED("SYSTEM_021", 401, "未登录"),
    PERMISSION_DENIED("SYSTEM_022", 403, "无权限"),

    USER_NOT_EXISTS("SYSTEM_031", 404, "用户不存在"),

    OPERATION_REPEAT("SYSTEM_051", 409, "操作重复");

    private final String code;
    private final int standardCode;
    private final String message;

    SystemErrorCode(String code, int standardCode, String message) {
        this.code = code;
        this.standardCode = standardCode;
        this.message = message;
    }
}
