package com.example.modul_4_pract_1_4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.modul_4_pract_1_4.ui.theme.Modul_4_pract_14Theme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.example.modul_4_pract_1_4.data.Comment

import com.example.modul_4_pract_1_4.data.LoadState
import com.example.modul_4_pract_1_4.data.PostWithData
import com.example.modul_4_pract_1_4.data.SocialPost
import com.example.modul_4_pract_1_4.data.SocialRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async


class MainActivity : ComponentActivity() {

    private val repository by lazy { SocialRepository(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Modul_4_pract_14Theme {
                SocialFeedScreen(repository)
            }
        }
    }
}

// Composable функции
@Composable
fun SocialFeedScreen(repository: SocialRepository) {
    var posts by remember { mutableStateOf<List<SocialPost>>(emptyList()) }
    var postsWithData by remember { mutableStateOf<List<PostWithData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var loadJob by remember { mutableStateOf<Job?>(null) }



    fun launchPostDataLoading(
        postData: PostWithData,
        repo: SocialRepository
    ) {
        coroutineScope.launch {
            try {

                val avatarDeferred = async {
                    try {
                        val color = repo.loadAvatar(postData.post.avatarurl)

                        PostWithData(
                            post = postData.post,
                            avatarColor = color,
                            avatarState = LoadState.SUCCESS,
                            comments = postData.comments,
                            commentsState = postData.commentsState
                        )
                    } catch (e: Exception) {
                        postData.copy(avatarState = LoadState.ERROR)
                    }
                }

                val commentsDeferred = async {
                    try {
                        val comments = repo.loadComments(postData.post.id)
                        PostWithData(
                            post = postData.post,
                            avatarColor = postData.avatarColor,
                            avatarState = postData.avatarState,
                            comments = comments,
                            commentsState = LoadState.SUCCESS
                        )
                    } catch (e: Exception) {
                        postData.copy(commentsState = LoadState.ERROR)
                    }
                }

                // Ждем ОБЕ задачи и объединяем результаты
                val avatarResult = avatarDeferred.await()
                val commentsResult = commentsDeferred.await()

                val updatedPostData = PostWithData(
                    post = postData.post,
                    avatarColor = avatarResult.avatarColor,
                    avatarState = avatarResult.avatarState,
                    comments = commentsResult.comments,
                    commentsState = commentsResult.commentsState
                )


                // Обновляем UI только один раз, когда всё готово
                postsWithData = postsWithData.map {
                    if (it.post.id == updatedPostData.post.id) {
                        updatedPostData
                    } else {
                        it
                    }
                }

            } catch (e: Exception) {
                println("Ошибка загрузки данных поста: ${e.message}")
            }
        }
    }

    // Функция загрузки постов
    fun loadPosts(repo: SocialRepository) {
        loadJob?.cancel()
        loadJob = coroutineScope.launch {
            isLoading = true
            postsWithData = emptyList()

            try {
                val loadedPosts = withContext(Dispatchers.IO) {
                    repo.loadPosts()
                }
                posts = loadedPosts

                postsWithData = loadedPosts.map { post ->
                    PostWithData(
                        post = post,
                        avatarState = LoadState.LOADING,
                        commentsState = LoadState.LOADING
                    )
                }

                postsWithData.forEach { postData ->
                    launchPostDataLoading(postData, repo)
                }

            } catch (e: Exception) {
                println("Ошибка загрузки постов: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Загрузка постов при запуске
    LaunchedEffect(Unit) {
        loadPosts(repository)
    }

    fun refresh() {
        loadJob?.cancel()
        loadPosts(repository)
    }


    Column(
        modifier = Modifier.fillMaxSize()
            .padding(top = 25.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Социальная лента \nи точка",

                style = MaterialTheme.typography.headlineSmall
            )

            Button(onClick = { refresh() }) {
                Text("Обновить")
            }
        }

        if (isLoading && postsWithData.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(postsWithData) { postData ->
                    PostCard(postData)
                }
            }
        }
    }
}

@Composable
fun PostCard(postData: PostWithData) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (postData.avatarState) {
                    LoadState.LOADING -> {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    LoadState.SUCCESS -> {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(4.dp)
                                .background(
                                    color = Color(
                                        android.graphics.Color.parseColor(
                                            postData.avatarColor ?: "#CCCCCC"
                                        )
                                    )
                                )
                        )
                    }
                    LoadState.ERROR -> {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(4.dp)
                                .background(Color.Red),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("!", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(30.dp))


                Text(
                    text = postData.post.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

            }
            Text(
                text = "Пользователь ${postData.post.userid}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = postData.post.body,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (postData.commentsState) {
                LoadState.LOADING -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 1.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Загрузка комментариев...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                LoadState.SUCCESS -> {
                    if (postData.comments.isNotEmpty()) {
                        Text(
                            text = "Комментарии (${postData.comments.size}):",
                            style = MaterialTheme.typography.titleSmall
                        )

                        postData.comments.take(3).forEach { comment ->
                            CommentItem(comment)
                        }
                    } else {
                        Text(
                            text = "Нет комментариев",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                LoadState.ERROR -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.Red, shape = MaterialTheme.shapes.small),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "!",
                                color = Color.White,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ошибка загрузки комментариев",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp)
    ) {
        Text(
            text = comment.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = comment.body,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

