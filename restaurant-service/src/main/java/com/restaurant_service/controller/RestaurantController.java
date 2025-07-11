package com.restaurant_service.controller;

import com.restaurant_service.dto.request.MenuRequestDTO;
import com.restaurant_service.dto.request.RestaurantRequestDTO;
import com.restaurant_service.dto.response.ApiResponseDTO;
import com.restaurant_service.dto.response.MenuResponseDTO;
import com.restaurant_service.dto.response.RestaurantResponseDTO;
import com.restaurant_service.exception.RestaurantNotFoundException;
import com.restaurant_service.exception.UnauthorizedException;
import com.restaurant_service.mapper.RestaurantMapper;
import com.restaurant_service.model.Restaurant;
import com.restaurant_service.service.MenuService;
import com.restaurant_service.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant Management", description = "APIs for managing restaurants and menus")
@Slf4j
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final RestaurantMapper restaurantMapper;

    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping("/public")
    @Operation(summary = "Get all restaurants", description = "Retrieve all restaurants with pagination (Public)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved restaurants"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<ApiResponseDTO<Page<RestaurantResponseDTO>>> getAllRestaurants(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        Page<RestaurantResponseDTO> restaurants = restaurantService.getAllRestaurants(pageable);
        return ResponseEntity.ok(ApiResponseDTO.success("Restaurants retrieved successfully", restaurants));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Get restaurant by ID", description = "Retrieve a specific restaurant by ID (Public)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurant found"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public ResponseEntity<ApiResponseDTO<RestaurantResponseDTO>> getRestaurantById(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {

        Restaurant restaurant = restaurantService.getRestaurantById(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + id));

        RestaurantResponseDTO response = restaurantMapper.toResponseDTO(restaurant);
        return ResponseEntity.ok(ApiResponseDTO.success("Restaurant retrieved successfully", response));
    }

    @GetMapping("/public/{restaurantId}/menus")
    @Operation(summary = "Get restaurant menus", description = "Retrieve all menus for a specific restaurant (Public)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menus retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public ResponseEntity<ApiResponseDTO<Page<MenuResponseDTO>>> getRestaurantMenus(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        // Verify restaurant exists
        restaurantService.getRestaurantById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + restaurantId));

        Page<MenuResponseDTO> menus = menuService.getRestaurantMenus(restaurantId, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success("Menus retrieved successfully", menus));
    }

    // ==================== PROTECTED ENDPOINTS ====================

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Create restaurant", description = "Create a new restaurant (Restaurant Owner only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Restaurant created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Restaurant Owner role required")
    })
    public ResponseEntity<ApiResponseDTO<RestaurantResponseDTO>> createRestaurant(
            @Valid @RequestBody RestaurantRequestDTO request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String userEmail) throws Exception {

        log.info("Creating restaurant for user: {} ({})", userId, userEmail);
        request.setOwnerId(userId);

        RestaurantResponseDTO restaurant = restaurantService.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(restaurant));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update restaurant", description = "Update restaurant details (Owner only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurant updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the restaurant owner"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public ResponseEntity<ApiResponseDTO<RestaurantResponseDTO>> updateRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Valid @RequestBody RestaurantRequestDTO request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {

        // Verify ownership
        Restaurant restaurant = restaurantService.getRestaurantById(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + id));

        if (!restaurant.getOwnerId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own restaurant");
        }

        RestaurantResponseDTO updated = restaurantService.updateRestaurant(request, id);
        return ResponseEntity.ok(ApiResponseDTO.success("Restaurant updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Delete restaurant", description = "Delete a restaurant (Owner or Admin only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Restaurant deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the restaurant owner"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public ResponseEntity<ApiResponseDTO<Void>> deleteRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId,
            @Parameter(hidden = true) @RequestHeader("X-User-Roles") String roles) {

        // Admin can delete any restaurant, owner can only delete their own
        if (!roles.contains("ROLE_ADMIN")) {
            Restaurant restaurant = restaurantService.getRestaurantById(id)
                    .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + id));

            if (!restaurant.getOwnerId().equals(userId)) {
                throw new UnauthorizedException("You can only delete your own restaurant");
            }
        }

        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Restaurant deleted successfully", null));
    }

    // ==================== MENU MANAGEMENT ====================

    @PostMapping("/{restaurantId}/menus")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Add menu item", description = "Add a new menu item to restaurant (Owner only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Menu item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the restaurant owner"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public ResponseEntity<ApiResponseDTO<MenuResponseDTO>> addMenuItem(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Valid @RequestBody MenuRequestDTO request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {

        // Verify ownership
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + restaurantId));

        if (!restaurant.getOwnerId().equals(userId)) {
            throw new UnauthorizedException("You can only add menu items to your own restaurant");
        }

        request.setRestaurantId(restaurantId);
        MenuResponseDTO menu = menuService.createMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(menu));
    }

    @PutMapping("/{restaurantId}/menus/{menuId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update menu item", description = "Update a menu item (Owner only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the restaurant owner"),
            @ApiResponse(responseCode = "404", description = "Restaurant or menu not found")
    })
    public ResponseEntity<ApiResponseDTO<MenuResponseDTO>> updateMenuItem(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Menu ID") @PathVariable Long menuId,
            @Valid @RequestBody MenuRequestDTO request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {

        // Verify ownership
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + restaurantId));

        if (!restaurant.getOwnerId().equals(userId)) {
            throw new UnauthorizedException("You can only update menu items in your own restaurant");
        }

        MenuResponseDTO updated = menuService.updateMenu(request, menuId);
        return ResponseEntity.ok(ApiResponseDTO.success("Menu item updated successfully", updated));
    }

    @DeleteMapping("/{restaurantId}/menus/{menuId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Delete menu item", description = "Delete a menu item (Owner only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Menu item deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the restaurant owner"),
            @ApiResponse(responseCode = "404", description = "Restaurant or menu not found")
    })
    public ResponseEntity<ApiResponseDTO<Void>> deleteMenuItem(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Menu ID") @PathVariable Long menuId,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {

        // Verify ownership
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + restaurantId));

        if (!restaurant.getOwnerId().equals(userId)) {
            throw new UnauthorizedException("You can only delete menu items from your own restaurant");
        }

        menuService.deleteMenu(menuId);
        return ResponseEntity.ok(ApiResponseDTO.success("Menu item deleted successfully", null));
    }

}
