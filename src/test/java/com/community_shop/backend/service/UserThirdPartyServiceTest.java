package com.community_shop.backend.service;
import com.community_shop.backend.dto.user.ThirdPartyBindDTO;
import com.community_shop.backend.entity.User;
import com.community_shop.backend.entity.UserThirdParty;
import com.community_shop.backend.enums.SimpleEnum.ThirdPartyTypeEnum;
import com.community_shop.backend.dao.mapper.UserMapper;
import com.community_shop.backend.dao.mapper.UserThirdPartyMapper;
import com.community_shop.backend.service.impl.UserThirdPartyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserThirdPartyServiceTest {

    @Mock
    private UserThirdPartyMapper userThirdPartyMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserThirdPartyServiceImpl thirdPartyService;

    private User testUser;
    private ThirdPartyBindDTO testBindDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);

        testBindDTO = new ThirdPartyBindDTO();
        testBindDTO.setThirdType(ThirdPartyTypeEnum.WECHAT);
        testBindDTO.setOpenid("testOpenid");
        testBindDTO.setAccessToken("testToken");
    }

    @Test
    void testBind_Success() {
        // 模拟依赖行为
        when(userMapper.selectById(anyLong())).thenReturn(testUser);
        when(userThirdPartyMapper.selectByThirdTypeAndOpenid(any(), anyString())).thenReturn(null);
        when(userThirdPartyMapper.selectValidByUserId(anyLong())).thenReturn(List.of());
        when(userThirdPartyMapper.insert(any(UserThirdParty.class))).thenReturn(1);

        // 执行测试
        Boolean result = thirdPartyService.bind(1L, testBindDTO);

        // 验证结果
        assertTrue(result);
        verify(userThirdPartyMapper, times(1)).insert(any(UserThirdParty.class));
    }

    @Test
    void testBind_DuplicateOpenid() {
        // 模拟OpenID已绑定
        when(userMapper.selectById(anyLong())).thenReturn(testUser);
        when(userThirdPartyMapper.selectByThirdTypeAndOpenid(any(), anyString())).thenReturn(new UserThirdParty());

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            thirdPartyService.bind(1L, testBindDTO);
        }, "应抛出重复绑定异常");
    }

    @Test
    void testUnbind_Success() {
        // 模拟依赖行为
        when(userThirdPartyMapper.updateInvalidById(anyLong(), anyLong())).thenReturn(1);

        // 执行测试
        Boolean result = thirdPartyService.unbind(1L, 100L);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testUnbind_Failure() {
        // 模拟解绑失败
        when(userThirdPartyMapper.updateInvalidById(anyLong(), anyLong())).thenReturn(0);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            thirdPartyService.unbind(1L, 100L);
        }, "应抛出解绑失败异常");
    }
}