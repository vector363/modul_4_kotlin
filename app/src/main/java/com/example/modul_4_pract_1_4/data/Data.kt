package com.example.modul_4_pract_1_4.data


data class GithubRepo(
    val id: Int,
    val full_name: String,
    val description: String?,
    val stargazers_count: Int,
    val language: String?
)