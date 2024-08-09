package com.raynor.demo.aboutfeign.app

import com.raynor.demo.aboutfeign.app.http.OutSideAPIClient
import com.raynor.demo.aboutfeign.app.model.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SeekService(
    private val outSideAPIClient: OutSideAPIClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getUsers(): List<User> {
        return outSideAPIClient.requestUsers()
            .onSuccess { logger.info("Success to getUsers: $it") }
            .onFailure { logger.error("Failed to getUsers", it) }
            .getOrDefault(emptyList())
    }

    fun getUsersUnHandledEnum(): List<User> {
        return outSideAPIClient.requestUsersUnHandledEnum()
            .onSuccess { logger.info("Success to getUsersUnHandledEnum: $it") }
            .onFailure { logger.error("Failed to getUsersUnHandledEnum", it) }
            .getOrThrow() // 실패 했으니 에러 발생 시켜 버림
//            .getOrDefault(emptyList()) // 에러는 에러고 default 값으로 리턴함
    }

    fun getUsersUnHandledEnumButNoRaise(): List<User> {
        return outSideAPIClient.requestUsersUnHandledEnumButNoRaise()
            .onSuccess { logger.info("Success to getUsersUnHandledEnumButNoRaise: $it") }
            .onFailure { logger.error("Failed to getUsersUnHandledEnumButNoRaise", it) }
            .getOrThrow()
    }
}
