package com.example.controller;

import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.service.AdminService;
import com.example.service.EmployService;
import com.example.service.UserService;
import com.example.utils.TokenUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserRestApiControllerTest {

    @Test
    void loginDelegatesByRoleAndReturnsTokenAccount() throws Exception {
        UserService userService = mock(UserService.class);
        UserRestApiController controller = controller(mock(AdminService.class), mock(EmployService.class), userService);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        User loginUser = new User();
        loginUser.setId(9);
        loginUser.setUsername("alice");
        loginUser.setRole(RoleEnum.USER.name());
        loginUser.setToken("token-9");
        when(userService.login(any(Account.class))).thenReturn(loginUser);

        mvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"123456\",\"role\":\"USER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.token").value("token-9"));

        verify(userService).login(any(Account.class));
    }

    @Test
    void registerDelegatesToExistingRegisterService() throws Exception {
        UserService userService = mock(UserService.class);
        UserRestApiController controller = controller(mock(AdminService.class), mock(EmployService.class), userService);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"123456\",\"role\":\"USER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(userService).register(any(Account.class));
    }

    @Test
    void currentReturnsTokenUser() throws Exception {
        UserRestApiController controller = controller(mock(AdminService.class), mock(EmployService.class), mock(UserService.class));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        User current = currentUser();

        try (MockedStatic<TokenUtils> tokenUtils = mockStatic(TokenUtils.class)) {
            tokenUtils.when(TokenUtils::getCurrentUser).thenReturn(current);

            mvc.perform(get("/api/user/current"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(9))
                    .andExpect(jsonPath("$.data.role").value("USER"));
        }
    }

    @Test
    void logoutIsStatelessSuccess() throws Exception {
        UserRestApiController controller = controller(mock(AdminService.class), mock(EmployService.class), mock(UserService.class));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/api/user/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    void profileUpdatesOnlyCurrentUserIdentity() throws Exception {
        UserService userService = mock(UserService.class);
        UserRestApiController controller = controller(mock(AdminService.class), mock(EmployService.class), userService);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        User current = currentUser();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        try (MockedStatic<TokenUtils> tokenUtils = mockStatic(TokenUtils.class)) {
            tokenUtils.when(TokenUtils::getCurrentUser).thenReturn(current);

            mvc.perform(put("/api/user/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\":999,\"role\":\"ADMIN\",\"name\":\"Alice New\",\"phone\":\"13800000000\",\"email\":\"a@example.com\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        verify(userService).updateById(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(9);
        assertThat(userCaptor.getValue().getRole()).isEqualTo(RoleEnum.USER.name());
        assertThat(userCaptor.getValue().getName()).isEqualTo("Alice New");
        assertThat(userCaptor.getValue().getPhone()).isEqualTo("13800000000");
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("a@example.com");
    }

    private UserRestApiController controller(AdminService adminService,
                                             EmployService employService,
                                             UserService userService) {
        UserRestApiController controller = new UserRestApiController();
        ReflectionTestUtils.setField(controller, "adminService", adminService);
        ReflectionTestUtils.setField(controller, "employService", employService);
        ReflectionTestUtils.setField(controller, "userService", userService);
        return controller;
    }

    private User currentUser() {
        User current = new User();
        current.setId(9);
        current.setUsername("alice");
        current.setRole(RoleEnum.USER.name());
        current.setName("Alice");
        return current;
    }
}
