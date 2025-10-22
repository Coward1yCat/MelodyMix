package com.melodymix.backend.repository;

import com.melodymix.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository 接口
 *
 * JpaRepository<User, Long>: 继承 JpaRepository，Spring Data JPA 会自动为我们提供 CRUD 操作。
 * - User: 指定这个 Repository 是用来操作哪个实体类的。
 * - Long: 指定这个实体类的主键是什么类型。
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * Spring Data JPA 的 "派生查询方法" 功能：
     * 你只需要按照特定规则命名方法，JPA 就会自动为你生成 SQL 查询。
     * findByUsername -> SELECT * FROM users WHERE username = ?
     *
     * @param username 用户名
     * @return 使用 Optional<User> 是为了更好地处理可能不存在的情况，避免空指针异常 (NullPointerException)。
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * findByEmail -> SELECT * FROM users WHERE email = ?
     *
     * @param email 邮箱
     * @return 返回一个 Optional<User>
     */
    Optional<User> findByEmail(String email);
}
