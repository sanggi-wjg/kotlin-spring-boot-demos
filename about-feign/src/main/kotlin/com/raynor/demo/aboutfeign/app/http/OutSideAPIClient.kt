package com.raynor.demo.aboutfeign.app.http

import com.raynor.demo.aboutfeign.app.model.User
import com.raynor.demo.aboutfeign.app.model.toUser
import com.raynor.demo.aboutfeign.app.model.toUserV2
import org.springframework.stereotype.Component

@Component
class OutSideAPIClient(
    private val outSideAPI: OutSideAPI,
) {
    fun requestUsers(): Result<List<User>> {
        return runCatching {
            outSideAPI.getUsers().map { it.toUser() }
        }
    }

    fun requestUsersUnHandledEnum(): Result<List<User>> {
        return runCatching {
            outSideAPI.getUsersUnHandledEnum().map { it.toUser() }
        }
    }

    fun requestUsersUnHandledEnumButNoRaise(): Result<List<User>> {
        return runCatching {
            outSideAPI.getUsersUnHandledEnum().map { it.toUserV2() }
        }
    }
}
