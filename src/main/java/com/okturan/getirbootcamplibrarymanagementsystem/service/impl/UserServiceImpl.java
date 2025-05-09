package com.okturan.getirbootcamplibrarymanagementsystem.service.impl;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserDetailsDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.UserMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.UserRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	private final UserMapper userMapper;

	private User getById(Long id) {
		return userRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
	}

	private User getByUsername(String username) {
		return userRepository.findByUsername(username)
			.orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
	}

	@Override
	public User registerUser(User user) {
		log.info("Registering user: username={}", user.getUsername());
		return userRepository.save(user);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetailsDTO findByUsername(String username) {
		User user = getByUsername(username);
		return userMapper.mapToDetailsDTO(user);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserDetailsDTO> getAllUsers(Pageable pageable) {
		Page<User> usersPage = userRepository.findAll(pageable);
		return usersPage.map(userMapper::mapToDetailsDTO);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetailsDTO findById(Long id) {
		User user = getById(id);
		return userMapper.mapToDetailsDTO(user);
	}

	@Override
	public UserDetailsDTO updateUser(Long id, AdminUserUpdateDTO adminUserUpdateDTO) {
		log.info("Admin updating user: id={}", id);

		User existingUser = getById(id);
		userMapper.updateUserFromAdminDto(adminUserUpdateDTO, existingUser);

		return userMapper.mapToDetailsDTO(userRepository.save(existingUser));
	}

	@Override
	public UserDetailsDTO updateCurrentUser(String username, UserUpdateDTO userUpdateDTO) {
		log.info("Self-updating user: username={}", username);

		User existingUser = getByUsername(username);
		userMapper.updateUserFromDto(userUpdateDTO, existingUser);

		return userMapper.mapToDetailsDTO(userRepository.save(existingUser));
	}

}
