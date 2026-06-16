package com.example.loudhotel.repository;

import com.example.loudhotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    Optional<User> findByPhoneAndIsDeletedFalse(String phone);

    boolean existsByEmailAndIsDeletedFalse(String email);

    boolean existsByUsernameAndIsDeletedFalse(String username);

    boolean existsByPhoneAndIsDeletedFalse(String phone);

    Page<User> findByRoleNotAndIsDeletedFalse(User.Role role, Pageable pageable);

    @Query("""
    SELECT u FROM User u
    WHERE u.role <> :role
    AND (
        LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR CAST(u.createdAt AS string) LIKE CONCAT('%', :keyword, '%')
        OR CAST(u.updatedAt AS string) LIKE CONCAT('%', :keyword, '%')
    )
""")
    Page<User> searchUsers(
            @Param("role") User.Role role,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<User> findByRoleAndIsDeletedFalse(User.Role role);
}
