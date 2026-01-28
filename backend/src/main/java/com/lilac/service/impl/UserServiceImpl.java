package com.lilac.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.constant.UserConstant;
import com.lilac.domain.dto.user.UserQueryRequest;
import com.lilac.domain.entity.User;
import com.lilac.domain.vo.LoginUserVO;
import com.lilac.domain.vo.UserVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.enums.UserRoleEnum;
import com.lilac.exception.BusinessException;
import com.lilac.service.UserService;
import com.lilac.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author lilac
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)){
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "参数不能为空");
        }
        if (userPassword.length() < 6 || checkPassword.length() < 6){
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 检查用户账号是否和数据库中已有的重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0){
            throw new BusinessException(HttpsCodeEnum.USER_EXIST);
        }
        // 密码加密
        String encodedPassword = getEncodedPassword(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encodedPassword);
        user.setUserName("user");
        user.setUserRole(UserRoleEnum.USER.getValue());
        // 插入数据库
        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(HttpsCodeEnum.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword)){
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() >= 256){
            throw new BusinessException(HttpsCodeEnum.USER_OR_PASSWORD_ERROR);
        }
        if (userPassword.length() < 6 || userPassword.length() > 18){
            throw new BusinessException(HttpsCodeEnum.USER_OR_PASSWORD_ERROR);
        }
        // 密码加密
        String encodedPassword = getEncodedPassword(userPassword);
        // 查询数据库中是否存在用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount).eq("userPassword", encodedPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null){
            log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(HttpsCodeEnum.USER_OR_PASSWORD_ERROR);
        }
        // 保存用户状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断用户是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null){
            throw new BusinessException(HttpsCodeEnum.NEED_LOGIN);
        }
        // 从数据库中查询用户信息
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null){
            throw new BusinessException(HttpsCodeEnum.NEED_LOGIN);
        }
        return currentUser;
    }

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 注销结果
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 判断用户是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(HttpsCodeEnum.NEED_LOGIN);
        }
        // 移除用户
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取登录用户信息
     *
     * @param user 用户
     * @return 登录用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户
     * @return 脱敏后的用户信息
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户列表
     *
     * @param userList 用户列表
     * @return 脱敏后的用户列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if(CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询条件
     * @return 查询条件
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if(userQueryRequest == null){
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncodedPassword(String userPassword) {
        final String salt = "lilac";
        return DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
    }
}