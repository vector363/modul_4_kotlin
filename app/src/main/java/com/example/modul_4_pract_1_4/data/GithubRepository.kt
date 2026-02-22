package com.example.modul_4_pract_1_4.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class GithubRepository(private val context: Context) {

    private var allRepos: List<GithubRepo>? = null

    suspend fun loadReposFromAssets(): List<GithubRepo> {
        return withContext(Dispatchers.IO) {
            try {
                if (allRepos == null) {
                    val jsonString = context.assets.open("github_repos.json")
                        .bufferedReader().use { it.readText() }

                    val listType = object : TypeToken<List<GithubRepo>>() {}.type
                    allRepos = Gson().fromJson(jsonString, listType)
                }
                allRepos ?: emptyList()
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun searchRepos(query: String): List<GithubRepo> {
        if (query.isBlank()) return emptyList()

        return withContext(Dispatchers.Default) {
            val repos = loadReposFromAssets()

            kotlinx.coroutines.delay(300)

            repos.filter { repo ->
                repo.full_name.contains(query, ignoreCase = true) ||
                        (repo.description?.contains(query, ignoreCase = true) == true) ||
                        (repo.language?.contains(query, ignoreCase = true) == true)
            }.sortedByDescending { it.stargazers_count }
        }
    }
}