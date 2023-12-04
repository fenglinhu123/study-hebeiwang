package com.mhyy.user.repo;

import com.mhyy.user.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUserName(String userName);

    User findByUserPhone(String userPhone);

    @Query(value = "update user set user_phone = ?1 where id = ?2",nativeQuery = true)
    @Modifying
    @Transactional
    void updateUserPhoneById(String phoneNumber,Integer clientId);
}
