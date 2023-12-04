package com.mhyy.user.repo;

import com.mhyy.user.pojo.Oauth2ClientRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OauthClientRegisterRepository extends JpaRepository<Oauth2ClientRegister, Integer> {
    List<Oauth2ClientRegister> findByUserId(Integer userId);

    Oauth2ClientRegister findByAppName(String appName);

    @Query(value = "update oauth_ext_app_register set approve = 1 where app_name = ?1", nativeQuery = true)
    @Modifying
    @Transactional
    void updateRegisterClientByAppName(String appName);
}
