package xyz.graygoo401.infra.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EmailSendUtil {

    // 从配置文件注入发送邮箱、邮件主题、模板路径
    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${email.code.subject}")
    private String emailSubject;
    @Value("${email.code.template}")
    private String emailTemplatePath;

    @Autowired
    private JavaMailSender mailSender;  // Spring提供的邮件发送核心类

    @Autowired
    private TemplateEngine templateEngine;  // Thymeleaf模板引擎（用于渲染HTML邮件）

    /**
     * 发送HTML格式的邮箱验证码
     * @param toEmail 接收邮箱地址（如 user@example.com）
     * @param code 生成的6位验证码
     * @throws MessagingException 邮件发送失败时抛出异常
     */
    public void sendCodeEmail(String toEmail, String code) throws MessagingException {
        // 1. 创建MIME邮件对象（支持HTML、附件等复杂内容）
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        // 2. 设置邮件基础信息
        helper.setFrom(fromEmail);          // 发送方邮箱
        helper.setTo(toEmail);              // 接收方邮箱
        helper.setSubject(emailSubject);    // 邮件主题

        // 3. 渲染HTML模板（传递验证码、有效期等变量到模板）
        Context context = new Context();
        context.setVariable("code", code);  // 验证码
        context.setVariable("expireMin", 5); // 有效期（分钟）
        context.setVariable("systemName", "graygoo401 的验证码服务"); // 系统名称
        // 使用Thymeleaf渲染模板，生成HTML内容
        String htmlContent = templateEngine.process(emailTemplatePath, context);

        // 4. 设置邮件内容为HTML格式
        helper.setText(htmlContent, true);  // 第二个参数true表示内容为HTML

        // 5. 发送邮件
        mailSender.send(mimeMessage);
    }

    /**
     * 发送简单文本格式的邮箱验证码（备用，推荐用HTML格式）
     * @param toEmail 接收邮箱
     * @param code 验证码
     */
    public void sendSimpleCodeEmail(String toEmail, String code) {
        // 构建文本内容
        String content = String.format(
                "您正在执行敏感操作，您的邮箱验证码为：%s\n" +
                        "验证码有效期为5分钟，请尽快完成验证，请勿泄露给他人。",
                code
        );
        // 发送简单邮件（无需MimeMessageHelper）
        org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(emailSubject);
        message.setText(content);
        mailSender.send(message);
    }
}
