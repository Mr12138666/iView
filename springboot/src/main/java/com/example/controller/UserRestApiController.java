package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.Employ;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.EmployService;
import com.example.service.UserService;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserRestApiController {

    @Resource
    private AdminService adminService;
    @Resource
    private EmployService employService;
    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        return Result.success(loginByRole(account));
    }

    @PostMapping("/register")
    public Result register(@RequestBody Account account) {
        if (account == null || account.getRole() == null) {
            throw new CustomException("400", "role不能为空");
        }
        if (RoleEnum.EMPLOY.name().equals(account.getRole())) {
            employService.register(account);
            return Result.success();
        }
        if (RoleEnum.USER.name().equals(account.getRole())) {
            userService.register(account);
            return Result.success();
        }
        throw new CustomException("400", "不支持的注册角色");
    }

    @GetMapping("/current")
    public Result current() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("401", "用户未登录");
        }
        return Result.success(currentUser);
    }

    @PostMapping("/logout")
    public Result logout() {
        return Result.success();
    }

    @PutMapping("/profile")
    public Result updateProfile(@RequestBody Map<String, Object> profile) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("401", "用户未登录");
        }
        if (RoleEnum.ADMIN.name().equals(currentUser.getRole())) {
            Admin admin = new Admin();
            admin.setId(currentUser.getId());
            admin.setRole(RoleEnum.ADMIN.name());
            admin.setName(stringValue(profile, "name"));
            admin.setAvatar(stringValue(profile, "avatar"));
            admin.setPhone(stringValue(profile, "phone"));
            admin.setEmail(stringValue(profile, "email"));
            adminService.updateById(admin);
            return Result.success();
        }
        if (RoleEnum.EMPLOY.name().equals(currentUser.getRole())) {
            Employ employ = new Employ();
            employ.setId(currentUser.getId());
            employ.setRole(RoleEnum.EMPLOY.name());
            employ.setName(stringValue(profile, "name"));
            employ.setAvatar(stringValue(profile, "avatar"));
            employ.setCity(stringValue(profile, "city"));
            employ.setAddress(stringValue(profile, "address"));
            employ.setScale(stringValue(profile, "scale"));
            employ.setStage(stringValue(profile, "stage"));
            employ.setIndustryId(integerValue(profile, "industryId"));
            employService.updateById(employ);
            return Result.success();
        }
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            User user = new User();
            user.setId(currentUser.getId());
            user.setRole(RoleEnum.USER.name());
            user.setName(stringValue(profile, "name"));
            user.setAvatar(stringValue(profile, "avatar"));
            user.setPhone(stringValue(profile, "phone"));
            user.setEmail(stringValue(profile, "email"));
            userService.updateById(user);
            return Result.success();
        }
        throw new CustomException("403", "不支持的用户角色");
    }

    private Account loginByRole(Account account) {
        if (account == null || account.getRole() == null) {
            throw new CustomException("400", "role不能为空");
        }
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {
            return adminService.login(account);
        }
        if (RoleEnum.EMPLOY.name().equals(account.getRole())) {
            return employService.login(account);
        }
        if (RoleEnum.USER.name().equals(account.getRole())) {
            return userService.login(account);
        }
        throw new CustomException("400", "不支持的登录角色");
    }

    private String stringValue(Map<String, Object> source, String key) {
        if (source == null || source.get(key) == null) {
            return null;
        }
        return String.valueOf(source.get(key));
    }

    private Integer integerValue(Map<String, Object> source, String key) {
        if (source == null || source.get(key) == null) {
            return null;
        }
        Object value = source.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }
}
