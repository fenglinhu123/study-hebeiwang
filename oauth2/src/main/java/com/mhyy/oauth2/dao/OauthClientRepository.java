package com.mhyy.oauth2.dao;

import com.mhyy.oauth2.model.Oauth2Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface OauthClientRepository extends JpaRepository<Oauth2Client, Integer> {

    Oauth2Client findByClientId(String clientId);
}
