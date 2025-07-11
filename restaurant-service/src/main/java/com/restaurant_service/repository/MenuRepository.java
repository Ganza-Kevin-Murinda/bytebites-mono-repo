package com.restaurant_service.repository;

import com.restaurant_service.model.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu,Long> {
    Optional<Menu> findByName(String name);
    Page<Menu> findByRestaurantId(Long id, Pageable pageable);
}
