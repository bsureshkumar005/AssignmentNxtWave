package com.example.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@SpringBootApplication
@RestController
public class UserManagementAPI {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementAPI.class, args);
    }

    private final Map<String, User> users = new HashMap<>();
    private final Set<String> managerIds = new HashSet<>(Arrays.asList("manager1", "manager2", "manager3"));

    @PostMapping("/create_user")
    public Map<String, String> createUser(@RequestBody Map<String, String> payload) {
        String fullName = payload.get("full_name");
        String mobNum = payload.get("mob_num");
        String panNum = payload.get("pan_num");
        String managerId = payload.get("manager_id");

        if (fullName == null || fullName.trim().isEmpty())
            return error("Invalid full name");
        if (!isValidMobile(mobNum))
            return error("Invalid mobile number");
        if (!isValidPAN(panNum))
            return error("Invalid PAN number");
        if (managerId != null && !managerIds.contains(managerId))
            return error("Invalid manager ID");

        mobNum = formatMobile(mobNum);
        panNum = panNum.toUpperCase();
        String userId = UUID.randomUUID().toString();

        User user = new User(userId, fullName, mobNum, panNum, managerId, LocalDateTime.now(), null, true);
        users.put(userId, user);
        return success("User created successfully");
    }

    @PostMapping("/get_users")
    public Map<String, Object> getUsers(@RequestBody(required = false) Map<String, String> payload) {
        String mobNum = payload != null ? payload.get("mob_num") : null;
        String userId = payload != null ? payload.get("user_id") : null;
        String managerId = payload != null ? payload.get("manager_id") : null;

        List<User> result = new ArrayList<>(users.values());

        if (mobNum != null)
            result.removeIf(user -> !user.getMobNum().equals(mobNum));
        if (userId != null)
            result.removeIf(user -> !user.getUserId().equals(userId));
        if (managerId != null)
            result.removeIf(user -> !Objects.equals(user.getManagerId(), managerId));

        return Collections.singletonMap("users", result);
    }

    @PostMapping("/delete_user")
    public Map<String, String> deleteUser(@RequestBody Map<String, String> payload) {
        String userId = payload.get("user_id");
        String mobNum = payload.get("mob_num");

        if (userId != null && users.remove(userId) != null)
            return success("User deleted successfully");
        if (mobNum != null)
            users.values().removeIf(user -> user.getMobNum().equals(mobNum));

        return success("User deleted successfully");
    }

    @PostMapping("/update_user")
    public Map<String, String> updateUser(@RequestBody Map<String, Object> payload) {
        List<String> userIds = (List<String>) payload.get("user_ids");
        Map<String, String> updateData = (Map<String, String>) payload.get("update_data");

        for (String userId : userIds) {
            User user = users.get(userId);
            if (user == null)
                continue;

            updateData.forEach((key, value) -> {
                switch (key) {
                    case "full_name" -> user.setFullName(value);
                    case "mob_num" -> user.setMobNum(formatMobile(value));
                    case "pan_num" -> user.setPanNum(value.toUpperCase());
                    case "manager_id" -> {
                        if (managerIds.contains(value))
                            user.setManagerId(value);
                    }
                }
            });
            user.setUpdatedAt(LocalDateTime.now());
        }
        return success("Users updated successfully");
    }

    private boolean isValidMobile(String mobNum) {
        return Pattern.compile("^(\\+91|0)?[6-9][0-9]{9}$").matcher(mobNum).matches();
    }

    private String formatMobile(String mobNum) {
        return mobNum.replaceAll("^(\\+91|0)", "");
    }

    private boolean isValidPAN(String panNum) {
        return Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}").matcher(panNum.toUpperCase()).matches();
    }

    private Map<String, String> error(String message) {
        return Collections.singletonMap("error", message);
    }

    private Map<String, String> success(String message) {
        return Collections.singletonMap("success", message);
    }
}

class User {
    private final String userId;
    private String fullName;
    private String mobNum;
    private String panNum;
    private String managerId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;

    public User(String userId, String fullName, String mobNum, String panNum, String managerId, LocalDateTime createdAt,
            LocalDateTime updatedAt, boolean isActive) {
        this.userId = userId;
        this.fullName = fullName;
        this.mobNum = mobNum;
        this.panNum = panNum;
        this.managerId = managerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
    }

    // Getters and setters omitted for brevity
}
