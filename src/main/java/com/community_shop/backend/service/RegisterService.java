//Controller：负责接收请求、参数校验和返回响应，不处理业务逻辑。
//Service：负责处理核心业务逻辑（如事务管理、权限校验、数据组装等）。
//Mapper/Repository：只负责与数据库交互（CRUD 操作）
package com.community_shop.backend.service;

import com.community_shop.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegisterService {
    private final UserMapper userMapper;

    // @Autowired 可省略（Spring 4.3+ 后，若只有一个构造函数）
    @Autowired
    private RegisterService(UserMapper userMapper){
        this.userMapper = userMapper;
    }

    public String insertDefaultUser(String username, String email, Long phoneNumber, String password, String initDate){
        String status = "";
        String role = "ROLE_USER";
        String userStatus = "NORMAL";

        //检查邮箱和电话号码的唯一性
        boolean isEmailOK = this.isEmailUnique(email);
        boolean isPhoneNumberOK = this.isPhoneNumberUnique(phoneNumber);
        if(!isEmailOK&&!isPhoneNumberOK){
            status = "email and phoneNumber exist";
        }else if(!isEmailOK){
            status = "email exists";
        }else if(!isPhoneNumberOK){
            status = "phoneNumber exists";
        }else {
            try{
                userMapper.insertDefaultUser(username, email, phoneNumber, password, initDate, role, userStatus);
                status = "ok";
            }catch (Exception e){
                System.out.println("数据库异常: " + e.getMessage());
                status = "database break";
            }
        }

        return status;
    }

    //检验电话号码唯一性
    public boolean isPhoneNumberUnique(Long phoneNumber){
        //调用mapper
        Long db_id = userMapper.getIDByPhoneNumber(phoneNumber);
        if(db_id==null){
            //用户不存在，凭证唯一
            return true;
        }else {
            //用户存在，凭证不唯一
            return false;
        }
    }

    //检验邮箱号码唯一性
    public boolean isEmailUnique(String email){
        //调用mapper
        Long db_id = userMapper.getIDByEmail(email);
        if(db_id==null){
            //用户不存在，凭证唯一
            return true;
        }else {
            //用户存在，凭证不唯一
            return false;
        }
    }
}
