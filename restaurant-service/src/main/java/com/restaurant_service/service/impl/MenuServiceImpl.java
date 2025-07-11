package com.restaurant_service.service.impl;

import com.restaurant_service.dto.request.MenuRequestDTO;
import com.restaurant_service.dto.response.MenuResponseDTO;
import com.restaurant_service.exception.MenuExistsException;
import com.restaurant_service.exception.MenuNotFoundException;
import com.restaurant_service.exception.RestaurantNotFoundException;
import com.restaurant_service.mapper.MenuMapper;
import com.restaurant_service.model.Menu;
import com.restaurant_service.model.Restaurant;
import com.restaurant_service.repository.MenuRepository;
import com.restaurant_service.repository.RestaurantRepository;
import com.restaurant_service.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public MenuResponseDTO createMenu(MenuRequestDTO menuDto) {
        // Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findById(menuDto.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(
                        String.format("A restaurant with the id '%s' doesn't exist", menuDto.getRestaurantId())
                ));

        // Check if menu name already exists
        if (menuRepository.findByName(menuDto.getName()).isPresent()) {
            throw new MenuExistsException(
                    String.format("A menu with the name '%s' already exists", menuDto.getName())
            );
        }

        // Map DTO to entity and set restaurant
        Menu menu = Menu.builder()
                .name(menuDto.getName())
                .price(menuDto.getPrice())
                .restaurant(restaurant)
                .build();

        Menu savedMenu = menuRepository.save(menu);
        return menuMapper.toResponseDTO(savedMenu);
    }

    @Override
    public Optional<Menu> getMenuByName(String name) {
        return menuRepository.findByName(name);
    }

    @Override
    public Optional<Menu> getMenuById(Long id) {
        return menuRepository.findById(id);
    }

    @Override
    public Page<MenuResponseDTO> getAllMenus(Pageable pageable) {
        return menuRepository.findAll(pageable)
                .map(menuMapper::toResponseDTO);
    }

    @Override
    public Page<MenuResponseDTO> getRestaurantMenus(Long id, Pageable pageable) {
        return menuRepository.findByRestaurantId(id, pageable)
                .map(menuMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public MenuResponseDTO updateMenu(MenuRequestDTO menuRequestDto, Long menuId) {
        Menu menu = getMenuById(menuId)
                .orElseThrow(() -> new MenuNotFoundException(
                        String.format("A menu with the Id '%d' doesn't exist", menuId)));

        if (menuRequestDto.getName() != null) {
            menu.setName(menuRequestDto.getName());
        }
        if (menuRequestDto.getPrice() != 0) {
            menu.setPrice(menuRequestDto.getPrice());
        }

        Menu updatedMenu = menuRepository.save(menu);
        return menuMapper.toResponseDTO(updatedMenu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        if (!menuRepository.existsById(id)) {
            throw new MenuNotFoundException(
                    String.format("A menu with the Id '%d' doesn't exist", id));
        }
        menuRepository.deleteById(id);
    }
}
