package com.advocacia.repository;

import com.advocacia.entity.Screen;
import com.advocacia.entity.User;
import com.advocacia.entity.UserScreen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserScreenRepository extends JpaRepository<UserScreen, UserScreen.UserScreenId> {

    @Query("SELECT us.screen FROM UserScreen us WHERE us.user.id = :userId")
    List<Screen> findScreensByUserId(@Param("userId") Long userId);

    @Query("SELECT s.code FROM UserScreen us JOIN us.screen s WHERE us.user.id = :userId")
    List<String> findScreenCodesByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserScreen us WHERE us.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByUserAndScreen(User user, Screen screen);
}
