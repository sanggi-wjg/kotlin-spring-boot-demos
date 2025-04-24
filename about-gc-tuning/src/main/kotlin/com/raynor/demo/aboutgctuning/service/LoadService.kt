package com.raynor.demo.aboutgctuning.service

import com.raynor.demo.aboutgctuning.model.ComputeResult
import com.raynor.demo.aboutgctuning.model.People
import com.raynor.demo.aboutgctuning.model.PeopleAdapter
import com.raynor.demo.aboutgctuning.model.PersonAdapter
import org.springframework.stereotype.Service

@Service
class LoadService {

    companion object {
        const val EMAIL_REGEX = "^([^@]+)@"
    }

    fun compute(): ComputeResult {
        val results = mutableListOf<Double>()
        for (i in 0 until 10_000) {
            val term = 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
            results.add(term)
        }
        val pi = results.sum()
        return ComputeResult(pi, results)
    }

    fun convert(people: People): PeopleAdapter {
        return PeopleAdapter(
            people.persons.map {
                PersonAdapter(
                    it.id,
                    it.name,
                    it.email,
                    extractEmailId(it.email)
                )
            }
        )
    }

    private fun extractEmailId(email: String): String {
        val regex = Regex(EMAIL_REGEX)
        val match = regex.find(email)
        return match?.groups?.get(1)?.value ?: email
    }
}
