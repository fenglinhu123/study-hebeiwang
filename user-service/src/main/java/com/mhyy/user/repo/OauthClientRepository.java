package com.mhyy.user.repo;

import com.mhyy.user.pojo.Oauth2Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface OauthClientRepository extends JpaRepository<Oauth2Client, Integer> {

    Oauth2Client findByClientId(String clientId);

    @Query(value = "update oauth_client_details set client_secret = ?1 where client_id = ?2",nativeQuery = true)
    @Modifying
    @Transactional
    void updateSecretByClientId(String secret,String clientId);
}
