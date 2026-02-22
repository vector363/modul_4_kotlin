package com.example.modul_4_pract_1_4.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException


class SocialRepository(private val context: Context) {
    private var allPosts: List<SocialPost>? = null
    private var allComments: List<Comment>? = null

    suspend fun loadPosts(): List<SocialPost> {
        return withContext(Dispatchers.IO) {
            try {
                if (allPosts == null) {
                    val jsonString = context.assets.open("social_posts.json")
                        .bufferedReader().use { it.readText() }

                    val listType = object : TypeToken<List<SocialPost>>() {}.type
                    allPosts = Gson().fromJson(jsonString, listType)
                }
                allPosts ?: emptyList()
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun loadComments(postId: Int):List<Comment>{
        return withContext(Dispatchers.IO){
            try {
                if (allComments == null) {
                    val jsonString = context.assets.open("comments.json")
                        .bufferedReader().use { it.readText() }

                    val listType = object : TypeToken<List<Comment>>() {}.type
                    allComments = Gson().fromJson(jsonString, listType)


                }

                delay((500..1500).random().toLong())

                allComments?.filter { it.postid == postId } ?: emptyList()


            } catch (e: IOException) {
                throw e
            }
        }
    }


    suspend fun loadAvatar(url: String): String {
        return withContext(Dispatchers.IO) {
            // Имитация загрузки аватарки (разное время)
            delay((300..1000).random().toLong())

            // Возвращаем цвет или URL (в реальном приложении тут была бы загрузка картинки)
            listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7").random()
        }
    }

}