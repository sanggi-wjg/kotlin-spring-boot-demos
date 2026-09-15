package com.raynor.demo.boiler.support.fixture

import com.raynor.demo.boiler.domain.user.User

object UserFixture {
    fun general(name: String = "general") =
        User(
            name = name,
            isAdmin = false,
        )
}
