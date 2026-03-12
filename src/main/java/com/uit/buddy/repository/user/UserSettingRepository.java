package com.uit.buddy.repository.user;

import com.uit.buddy.entity.user.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting, String> {
}