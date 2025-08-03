package com.community_shop.backend.service.impl;

import com.community_shop.backend.component.exception.UserException;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.mapper.UserMapper;
import com.community_shop.backend.component.exception.errorcode.ErrorCode;
import com.community_shop.backend.service.base.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    //搜索相关
    //默认的偏移量和数量限制
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 0;

    //User类的role属性常量
    private static final String USER_ROLE_ADMIN = "ROLE_ADMIN";
    private static final String USER_ROLE_TEACHER = "ROLE_TEACHER";
    private static final String USER_ROLE_STUDENT = "ROLE_STUDENT";
    public static final List<String> ALLOWED_USER_ROLE = Arrays.asList(USER_ROLE_ADMIN, USER_ROLE_TEACHER, USER_ROLE_STUDENT);

    //User类的status属性常量
    private static final String USER_STATUS_NORMAL = "NORMAL";
    private static final String USER_STATUS_BANNED = "BANNED";
    public static final List<String> ALLOWED_USER_STATUS = Arrays.asList(USER_STATUS_NORMAL, USER_STATUS_BANNED);

    //SQL片段（如列名、表名、排序方向等）
    //排序方式
    private static final String SQL_DESCENDING = "DESC";
    private static final String SQL_ASCENDING = "ASC";

    //列名
    private static final String SQL_USER_USERNAME = "username";
    private static final String SQL_USER_ROLE = "role";
    private static final String SQL_USER_CREATED_AT = "created_at";
    private static final String SQL_USER_LAST_ACTIVITY_AT = "last_activity_at";
    private static final String SQL_USER_EMAIL = "email";
    private static final String SQL_USER_PHONE_NUMBER = "phone_number";
    private static final String SQL_USER_STATUS = "status";
    public static final List<String> ALLOWED_USER_COLUMN_NAME = Arrays.asList(SQL_USER_USERNAME, SQL_USER_ROLE, SQL_USER_CREATED_AT,
            SQL_USER_LAST_ACTIVITY_AT, SQL_USER_EMAIL, SQL_USER_PHONE_NUMBER, SQL_USER_STATUS);


    @Autowired
    UserMapper userMapper;


    //更新帖子状态
    @Override
    public boolean updateUserStatus(Long userID, String status){
        if(userID == null){
            return false;
        }

        User user = userMapper.getUserByID(userID);

        if(user == null || !ALLOWED_USER_STATUS.contains(status)){
            return false;
        }

        userMapper.updateUserStatus(userID, status);
        return true;
    }

    //风险方法
    //获得所有用户
    public List<User> getAllUser(){
        //生成日志
        System.out.println("正在获取所有用户");
        List<User> users = null;

        try {
            users = userMapper.getAllUsers();
        }catch (Exception e){
            System.out.println("获取用户失败: " + e.getMessage());
        }

        return users;
    }


    @Override
    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.getUserByID(id);
    }

    //根据状态获取用户
    @Override
    public List<User> getUsersByStatus(String status, boolean isDESC, String order, Integer limit, Integer offset){
        //是否采用默认的数量限制
        if(limit == null){
            limit = DEFAULT_LIMIT;
        }
        //是否采用默认的偏移量
        if(offset == null){
            offset = DEFAULT_OFFSET;
        }

        //检测比较参数和排序参数是否合法
        if(!ALLOWED_USER_STATUS.contains(status) || !ALLOWED_USER_COLUMN_NAME.contains(order)){
            return null;
        }

        if(isDESC){
            return userMapper.getUsersByAllParam(SQL_USER_STATUS, status, order, SQL_DESCENDING, limit, offset);
        }else {
            return userMapper.getUsersByAllParam(SQL_USER_STATUS, status, order, SQL_ASCENDING, limit, offset);
        }

    }

    //根据用户名获取用户
    @Override
    public List<User> getUsersByUsername(String username, boolean isDESC, String order, Integer limit, Integer offset){
        //是否采用默认的数量限制
        if(limit == null){
            limit = DEFAULT_LIMIT;
        }
        //是否采用默认的偏移量
        if(offset == null){
            offset = DEFAULT_OFFSET;
        }

        //检测比较参数和排序参数是否合法
        if(!ALLOWED_USER_COLUMN_NAME.contains(order)){
            return null;
        }

        if(isDESC){
            return userMapper.getUsersByAllParam(SQL_USER_USERNAME, username, order, SQL_DESCENDING, limit, offset);
        }else {
            return userMapper.getUsersByAllParam(SQL_USER_USERNAME, username, order, SQL_ASCENDING, limit, offset);
        }
    }

    //根据用户角色获取用户
    @Override
    public List<User> getUsersByRole(String role, boolean isDESC, String order, Integer limit, Integer offset){
        //是否采用默认的数量限制
        if(limit == null){
            limit = DEFAULT_LIMIT;
        }
        //是否采用默认的偏移量
        if(offset == null){
            offset = DEFAULT_OFFSET;
        }

        //检测比较参数和排序参数是否合法
        if(!ALLOWED_USER_ROLE.contains(role) || !ALLOWED_USER_COLUMN_NAME.contains(order)){
            return null;
        }

        if(isDESC){
            return userMapper.getUsersByAllParam(SQL_USER_ROLE, role, order, SQL_DESCENDING, limit, offset);
        }else {
            return userMapper.getUsersByAllParam(SQL_USER_ROLE, role, order, SQL_ASCENDING, limit, offset);
        }
    }

    //添加用户
    @Override
    public int addUser(User user) throws UserException {
        try {
            checkFactor(user);
            return userMapper.addUser(user);
        } catch (UserException be){
            log.error("can't add in \"users\" table: " + be.getMessage());
            throw be;
        } catch (Exception e){
            log.error("can't add in \"users\" table: " + e.getMessage());
            throw new UserException(ErrorCode.FAILURE);
        }
    }

    //修改用户
    @Override
    public int updateUser(User user) throws UserException {
        try{
            checkUserExists(user.getUserID());
            checkFactor(user);
            return userMapper.updateUser(user);
        } catch (UserException be){
            log.error("can't update in \"users\" table.BE code = " + be.getCode());
            throw be;
        } catch (Exception e){
            log.error("can't update in \"users\" table because of unknown error." + e.getMessage());
            throw new UserException(ErrorCode.FAILURE);
        }
    }

    //删除用户
    @Override
    public int deleteUser(Long id) {
        int result = userMapper.deleteUser(id);
        if(result == 0){
            log.warn("can't delete in \"users\" table");
        }
        return result;
    }

    //更新用户角色
    @Override
    public boolean updateUserRole(Long userId, String role) throws UserException{
        checkUserExists(userId);
        if(!ALLOWED_USER_ROLE.contains(role)){
            throw new UserException(ErrorCode.ROLE_ERROR);
        }
        userMapper.updateUserRole(role, userId);
        return true;
    }

    //更新用户密码
    @Override
    public boolean updateUserPassword(Long userId, String password) throws UserException{
        checkUserExists(userId);
        if(password == null){
            throw new UserException(ErrorCode.PASSWORD_NULL);
        }
        userMapper.updateUserPassword(password, userId);
        return true;
    }

    //更新用户名称
    @Override
    public boolean updateUsername(Long userId, String username) throws UserException{
        checkUserExists(userId);
        if(username == null){
            throw new UserException(ErrorCode.USERNAME_NULL);
        }
        userMapper.updateUsername(username, userId);
        return true;
    }


    //检验关键数据是否非法
    //若姓名、邮箱和电话非法，则抛出对应异常
    //针对add，update
    @Override
    public void checkFactor(User user) throws UserException{

        String role = user.getRole();
        String name = user.getUsername();
        String phoneNumber = user.getPhoneNumber();
        String email = user.getEmail();

        //检查用户角色
        if(role == null){
            throw new UserException(ErrorCode.ROLE_NULL);
        }
        if(ALLOWED_USER_ROLE.contains(role)){
            throw new UserException(ErrorCode.ROLE_ERROR);
        }

        //检查用户名
        if(name == null){
            throw new UserException(ErrorCode.USERNAME_NULL);
        }
        if(!isNameUnique(name)){
            throw new UserException(ErrorCode.USERNAME_EXISTS);
        }

        //检查电话号码
        if(phoneNumber == null){
            throw new UserException(ErrorCode.PHONE_NULL);
        }
        if(!isPhoneNumberUnique(phoneNumber)){
            throw new UserException(ErrorCode.PHONE_EXISTS);
        }

        //检查邮箱号码
        if(email == null){
            throw new UserException(ErrorCode.EMAIL_NULL);
        }
        if(!isEmailUnique(email)){
            throw new UserException(ErrorCode.EMAIL_EXISTS);
        }
    }

    //根据userId检查用户是否存在
    //针对update
    @Override
    public void checkUserExists(Long userId) throws UserException{
        if(userId == 0){
            throw new UserException(ErrorCode.USER_ID_NULL);
        }
        if(userMapper.getUserByID(userId) == null){
            throw new UserException(ErrorCode.USER_NOT_EXISTS);
        }
    }

    //检验用户名称唯一性
    public boolean isNameUnique(String name){
        //调用mapper
        Long db_id = userMapper.getIDByUsername(name);
        if(db_id==null){
            //用户不存在，凭证唯一
            return true;
        }else {
            //用户存在，凭证不唯一
            return false;
        }
    }

    //检验电话号码唯一性
    public boolean isPhoneNumberUnique(String phoneNumber){
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
