package com.community_shop.backend.enums.simple;

public enum ChatSessionStatusEnum {

    /** 会话已开始 */
    STARTED,

    /** 会话进行中 */
    ACTIVE,

    /** 会话已关闭 */
    CLOSED,

    /** 会话已超时 */
    TIMEOUT,

    /** 会话异常 */
    ERROR;
}
