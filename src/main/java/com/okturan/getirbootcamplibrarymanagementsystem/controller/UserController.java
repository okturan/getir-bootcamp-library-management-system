package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.controller.api.UserApi;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.PageDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserDetailsDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Override
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDetailsDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDetailsDTO userDetailsDTO = userService.findByUsername(username);
        return ResponseEntity.ok(userDetailsDTO);
    }

    @Override
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDetailsDTO> updateCurrentUser(
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDetailsDTO updatedUser = userService.updateCurrentUser(username, userUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Override
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<PageDTO<UserDetailsDTO>> getAllUsers(
            @ParameterObject @PageableDefault(page = 0, size = 20, sort = "id,asc") Pageable pageable) {
        Page<UserDetailsDTO> userDTOs = userService.getAllUsers(pageable);
        PageDTO<UserDetailsDTO> pageDTO = PageDTO.from(userDTOs);
        return ResponseEntity.ok(pageDTO);
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<UserDetailsDTO> getUserById(@PathVariable Long id) {
        UserDetailsDTO userDetailsDTO = userService.findById(id);
        return ResponseEntity.ok(userDetailsDTO);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<UserDetailsDTO> updateUser(@PathVariable Long id,
                                                     @Valid @RequestBody AdminUserUpdateDTO adminUserUpdateDTO) {
        UserDetailsDTO updatedUser = userService.updateUser(id, adminUserUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

}
