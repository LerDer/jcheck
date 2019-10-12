package com.ler.jcheck.service;

import com.ler.jcheck.domain.User;

/**
 * @author lww
 */
public interface UserService {

	void addUser(User user);

	void deleteUser(Long id);
}
