package com.restaurant_service.service;

import com.restaurant_service.dto.request.MenuRequestDTO;
import com.restaurant_service.dto.response.MenuResponseDTO;
import com.restaurant_service.dto.response.MenuValidationResponseDTO;
import com.restaurant_service.model.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public interface MenuService {

    MenuResponseDTO createMenu(MenuRequestDTO menuRequestDto);

    Optional<Menu> getMenuById(Long id);

    Page<MenuResponseDTO> getRestaurantMenus(Long id, Pageable pageable);

    MenuResponseDTO updateMenu(MenuRequestDTO menuRequestDto, Long menuId);

    void deleteMenu(Long id);

    List<MenuValidationResponseDTO> validateMenuItems(Long restaurantId, List<Long> menuItemIds);
}
