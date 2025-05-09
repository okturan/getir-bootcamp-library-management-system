package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.AuthResultDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.LoginDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO;

public interface AuthService {

	AuthResultDTO registerPatron(UserRegistrationDTO registrationDTO);

	AuthResultDTO registerWithRole(AdminUserRegistrationDTO registrationDTO);

	AuthResultDTO login(LoginDTO loginDTO);

}
