package com.example.wx.repository;

import com.example.wx.entiy.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 16:19
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByRequestIp(String userIp);

}
