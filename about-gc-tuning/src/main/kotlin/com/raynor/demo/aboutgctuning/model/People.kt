package com.raynor.demo.aboutgctuning.model

interface IPerson {
    val id: Int
    val name: String
    val email: String
}

data class People(
    val persons: List<Person>,
)

data class Person(
    override val id: Int,
    override val name: String,
    override val email: String,
) : IPerson

data class PeopleAdapter(
    val persons: List<PersonAdapter>,
)

data class PersonAdapter(
    override val id: Int,
    override val name: String,
    override val email: String,
    val emailId: String,
) : IPerson
