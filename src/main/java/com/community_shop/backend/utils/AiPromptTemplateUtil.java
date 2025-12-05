package com.community_shop.backend.utils;

import com.community_shop.backend.utils.constants.AiPromptTemplateConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * AI Prompt 模板工具类：集中管理所有场景的 Prompt 模板，提供参数填充方法
 * 场景覆盖：商品描述生成、帖子润色、订单问答、内容审核等
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE) // 禁止实例化（工具类无需对象）
public class AiPromptTemplateUtil {

    // -------------------------- 1. 商品交易类 Prompt 模板 --------------------------
    /**
     * 场景1：二手商品描述生成（卖家发布商品时辅助）
     * @param productTitle 商品标题（如"iPhone 13 128G"）
     * @param productCategory 商品类别（如"二手手机"）
     * @param productCondition 商品成色（如"95新，仅边缘轻微划痕"）
     * @param additionalInfo 附加信息（可选，如"附带原装充电器"）
     * @return 填充后的完整 Prompt
     */
    public static String buildProductDescPrompt(
            String productTitle,
            String productCategory,
            String productCondition,
            String additionalInfo
    ) {
        // 1. 处理可选参数（避免"附加信息"为null时显示"null"）
        String safeAdditionalInfo = StringUtils.defaultIfBlank(additionalInfo, "无");

        // 2. 填充参数（按模板中 %s 的顺序传入参数，StringUtils 确保参数安全）
        return String.format(
                AiPromptTemplateConstants.PRODUCT_DESC_TEMPLATE,
                StringUtils.trim(productTitle),    // 去除首尾空格，避免格式混乱
                StringUtils.trim(productCategory),
                StringUtils.trim(productCondition),
                safeAdditionalInfo
        );
    }

    /**
     * 场景2：商品营销短文案生成（3条，用于列表页展示）
     * @param productName 商品名称（如"24考研数学一真题"）
     * @param coreAdvantage 核心优势（如"全新未用，比官网便宜50元"）
     * @param targetUser 目标人群（如"24考研党"）
     * @return 填充后的完整 Prompt
     */
    public static String buildProductMarketingPrompt(
            String productName,
            String coreAdvantage,
            String targetUser
    ) {

        return String.format(
                AiPromptTemplateConstants.PRODUCT_MARKETING_TEMPLATE,
                StringUtils.trim(productName),
                StringUtils.trim(coreAdvantage),
                StringUtils.trim(targetUser)
        );
    }

    // -------------------------- 2. 社区互动类 Prompt 模板 --------------------------
    /**
     * 场景3：帖子内容润色（学术问答/生活分享）
     * @param originalPostContent 用户原始内容（如"高数第三章不会，求帮忙"）
     * @param postType 帖子类型（如"学术问答""生活分享""求助吐槽"）
     * @return 填充后的完整 Prompt
     */
    public static String buildPostPolishPrompt(
            String originalPostContent,
            String postType
    ) {
        // 根据帖子类型动态追加优化规则（场景化适配）
        String typeSpecificRule = switch (postType) {
            case "学术问答" -> "帮用户明确问题点（如分点列出疑问，补充\"请问XX知识点/例题？\"）；";
            case "生活分享" -> "帮用户突出重点（如分点说明\"地点/价格/体验\"，补充实用细节）；";
            case "求助吐槽" -> "帮用户梳理诉求（如\"求助：XX问题，需要XX帮助\"，避免语气过激）；";
            default -> "帮用户优化逻辑结构，修正语病，简化冗余表述；";
        };

        String template = String.format(
                AiPromptTemplateConstants.POST_POLISH_TEMPLATE,
                typeSpecificRule,
                originalPostContent,
                postType
        );

        return StringUtils.trim(template);
    }

    /**
     * 场景4：帖子标题优化（提升点击量）
     * @param postCoreContent 帖子核心内容（如"想找24考研英语二真题"）
     * @param postType 帖子类型（如"求助""分享""答疑""避雷"）
     * @return 填充后的完整 Prompt
     */
    public static String buildPostTitleOptPrompt(
            String postCoreContent,
            String postType
    ) {
        // 按帖子类型生成互动引导词（如"求助"用"求问"，"分享"用"分享"）
        String guideWord = switch (postType) {
            case "求助" -> "求问/请问/有人知道吗";
            case "分享" -> "分享/推荐/亲测";
            case "答疑" -> "答疑/解惑/教程";
            case "避雷" -> "避雷/提醒/注意";
            default -> "求问/分享/请问";
        };

        return String.format(
                AiPromptTemplateConstants.POST_TITLE_OPT_TEMPLATE,
                guideWord,
                StringUtils.trim(postCoreContent),
                StringUtils.trim(postType)
        );
    }

    // -------------------------- 3. AI问答类 Prompt 模板 --------------------------
    /**
     * 场景5：订单状态查询问答（智能客服）
     * @param userQuestion 用户问题（如"我的订单发货了吗？"）
     * @param orderInfo 订单查询结果（如"订单号2024050112345，已发货，顺丰SF123456"）
     * @return 填充后的完整 Prompt
     */
    public static String buildOrderQaPrompt(
            String userQuestion,
            String orderInfo
    ) {

        return String.format(
                AiPromptTemplateConstants.ORDER_QA_TEMPLATE,
                StringUtils.trim(userQuestion),
                StringUtils.trim(orderInfo)
        );
    }

    // -------------------------- 4. 内容审核类 Prompt 模板 --------------------------
    /**
     * 场景6：内容违规检测（帖子/跟帖）
     * @param contentToCheck 待审核内容（如"出iPhone 13，微信wx123456"）
     * @param contentType 内容类型（如"帖子正文""跟帖评论""商品描述"）
     * @return 填充后的完整 Prompt
     */
    public static String buildContentCheckPrompt(
            String contentToCheck,
            String contentType
    ) {

        return String.format(
                AiPromptTemplateConstants.CONTENT_CHECK_TEMPLATE,
                StringUtils.trim(contentToCheck),
                StringUtils.trim(contentType)
        );
    }

    // -------------------------- 扩展说明：新增场景如何加 --------------------------
    // 1. 新增方法：public static String buildXXXPrompt(参数列表) { ... }
    // 2. 定义模板：用 """ 包裹多行文本，%s 作为占位符
    // 3. 处理参数：用 StringUtils 避免 null/空格问题
    // 4. 场景适配：如需动态规则（如不同类型不同逻辑），用 switch 或 if-else 处理
}
