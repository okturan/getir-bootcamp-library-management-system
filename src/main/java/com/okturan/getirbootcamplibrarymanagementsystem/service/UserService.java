package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserDetailsDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    User registerUser(User user);

    UserDetailsDTO findByUsername(String username);

    UserDetailsDTO updateUser(Long id, AdminUserUpdateDTO adminUserUpdateDTO);

    UserDetailsDTO updateCurrentUser(String username, UserUpdateDTO userUpdateDTO);

    Page<UserDetailsDTO> findAllUsers(Pageable pageable);

    UserDetailsDTO findById(Long id);

}
