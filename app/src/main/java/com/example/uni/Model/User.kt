package com.example.uni.Model

data class User(
    val uid: String = "",
    val fullname: String = "",
    val userName: String = "", // Matches Firebase field name
    val bio: String = "",
    val email: String = "",
    val fcmToken: String = "", // Include this if needed
    val image: String = ""
) {
    // No-argument constructor for Firebase deserialization
    constructor() : this("", "", "", "", "", "", "")

    // Builder class for easier object creation
    class Builder {
        private var uid: String = ""
        private var fullname: String = ""
        private var userName: String = ""
        private var bio: String = ""
        private var email: String = ""
        private var fcmToken: String = ""
        private var image: String = ""

        fun uid(uid: String) = apply { this.uid = uid }
        fun fullname(fullname: String) = apply { this.fullname = fullname }
        fun userName(userName: String) = apply { this.userName = userName }
        fun bio(bio: String) = apply { this.bio = bio }
        fun email(email: String) = apply { this.email = email }
        fun fcmToken(fcmToken: String) = apply { this.fcmToken = fcmToken }
        fun image(image: String) = apply { this.image = image }

        fun build() = User(uid, fullname, userName, bio, email, fcmToken, image)
    }
}
