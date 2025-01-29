package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.util.JwtUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.micrometer.common.util.StringUtils;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String login(LoginVo loginVo) {
        // 检验手机号和验证码是否为空
        String code = loginVo.getCode();
        String phone = loginVo.getPhone();
        if (StringUtils.isBlank(phone)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }

        if (StringUtils.isBlank(code)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }

        // 检验验证码是否过期
        String key = RedisConstant.APP_LOGIN_PREFIX + phone;
        String verifyCode = redisTemplate.opsForValue().get(key);
        if (verifyCode == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }
        // 检验验证码是否正确
        if (!verifyCode.equals(code)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        // 检验账号是否存在
        LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInfoLambdaQueryWrapper.eq(UserInfo::getPhone, phone);
        UserInfo user = userInfoMapper.selectOne(userInfoLambdaQueryWrapper);
        // 若不存在, 则创建新账号
        if (user == null) {
            user = new UserInfo();
            user.setPhone(phone);
            user.setStatus(BaseStatus.ENABLE);
            user.setNickname("用户-"+loginVo.getPhone().substring(6));
            userInfoMapper.insert(user);
        } else {
            // 账号存在的情况下, 若账号被禁用则抛出异常
            if (user.getStatus().equals(BaseStatus.DISABLE)) {
                throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
            }
        }

        // 校验通过, 生成token, 返回给前端
        return JwtUtil.createToken(user.getId(), user.getPhone());
    }

    @Override
    public UserInfoVo info() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        UserInfo userInfo = userInfoMapper.selectById(loginUser.getUserId());
        return new UserInfoVo(userInfo.getNickname(), userInfo.getAvatarUrl());
    }
}
