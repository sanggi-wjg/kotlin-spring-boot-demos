package com.raynor.demo.app

class UserEntity(
    val id: Int,
    val email: String,
    val name: String,
    val age: Int,
) {
    override fun toString(): String {
        return "UserEntity(id=$id, email='$email', name='$name', age=$age)"
    }
}
