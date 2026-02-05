package com.advocacia.repository;

import com.advocacia.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    Optional<Screen> findByCode(String code);

    List<Screen> findAllByOrderByDisplayOrderAsc();
}
