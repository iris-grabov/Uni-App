package com.example.uni.Model

data class Post(
    val postId: String = "",
    val postImage: String = "",
    val publisher: String = "",
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: MutableMap<String, Boolean> = mutableMapOf()
) {
    val likesCount: Int
        get() = likes.size

    fun toggleLike(userId: String) {
        if (likes.containsKey(userId)) {
            likes.remove(userId) // Unlike
        } else {
            likes[userId] = true // Like
        }
    }

    fun isUserLiked(userId: String): Boolean {
        return likes.containsKey(userId)
    }

    // Builder class
    class Builder {
        private var postId: String = ""
        private var postImage: String = ""
        private var publisher: String = ""
        private var caption: String = ""
        private var timestamp: Long = System.currentTimeMillis()
        private var likes: MutableMap<String, Boolean> = mutableMapOf()

        fun postId(postId: String) = apply { this.postId = postId }
        fun postImage(postImage: String) = apply { this.postImage = postImage }
        fun publisher(publisher: String) = apply { this.publisher = publisher }
        fun caption(caption: String) = apply { this.caption = caption }
        fun timestamp(timestamp: Long) = apply { this.timestamp = timestamp }
        fun likes(likes: MutableMap<String, Boolean>) = apply { this.likes = likes }

        fun build() = Post(
            postId, postImage, publisher, caption, timestamp, likes
        )
    }
}