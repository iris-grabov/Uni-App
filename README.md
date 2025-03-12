# Introducing "Uni" 
Uni is a revolutionary app that combines the best elements of social networks and traditional dating apps, designed to keep you active, engaged, and in control of your connections.
How Uni Works:
Get Exposure by Being Active
In Uni, there are no followers, and no one gets special treatment.
The only way to be seen by others is to stay active—post something on the homepage!
Every post you share automatically gets featured on the app's homepage, giving you maximum exposure to everyone using the app.
Connect Through Interaction
Users can interact with your posts by liking them, creating meaningful engagement.
You cant see how likes your posts- This creates interest and curiosity.
The "I Want to Know" Button
If you're interested in someone, click the "I Want to Know" button on their profile.
But here's the twist: no one knows you clicked unless they click on you too!
Only if both users click "I Want to Know" will a chat open, letting you start your conversation on equal footing.

# Project README

## Overview

This Android application is a social media platform designed for users to interact, share posts, and chat in real-time. It integrates with Firebase for user authentication, cloud storage for images, and Firebase Cloud Messaging (FCM) for push notifications. The app features user profiles, posts with images, and real-time messaging between users.

### Key Features:
- **User Authentication**: Firebase Authentication allows users to sign up, sign in, and manage their profiles- I also add in my app a Log Out option.
- **Real-Time Messaging**: Users can send and receive messages instantly via Firebase Firestore- this is a push notification- also when the user recive a message inside or outside the app he can click on the notification and get insite the app.
- **Post Uploading**: Users can upload posts with images and captions. The app stores these images in Firebase Storage and the Url in the real time database- they can upload a picture by using a camera or choosing from a gallery.
- **Likes on Posts**: Posts can be liked or unliked, with real-time updates on like counts- and also the users appear in the LikersFragment.
- **Push Notifications**: Firebase Cloud Messaging (FCM) sends push notifications for new messages.
- **Profile Management**: Users can manage their profiles, including updating profile pictures,name and bios.
- **Crop Image**: When the user want to update their image profile he can crop the image as he want.
- **Want To Know**: When the both users clicked on the want to know button- their appear in the CuriousFragment in their pages.

### Adapters:
- **ChatAdapter**: Displays chat messages in a conversation.
- **ChatListAdapter**: Displays a list of active chats.
- **CuriousUsersAdapter**: Both users clicked on the want to know button.
- **LikersAdapter**: Displays users who liked a post.
- **PostAdapter**: Displays posts in the feed, with images and captions and like optin.
- **ProfilePostsAdapter**: Displays another user profile.
- **UserAdapter**: RecyclerView adapter that binds a list of `User` objects to a view, displaying each user's profile picture, username, and optionally a "Want to Know" button based on the fragment context.


### Fragments:
- **CuriousTogetherFragment**: Shows the users that both of you clicked on the Want To Know.
- **HomeFragment**: Main feed where users can view posts.
- **LikersFragment**: Displays users who liked your posts.
- **UploadPostFragment**: Allows users to upload posts with images and captions- using camera or gallery.
- **UserFragment**: Displays user profile details and their posts.
  

### Models:
1. **ChatMessage**: Represents a chat message sent by a user. Includes sender ID, message text, and timestamp.
   ```kotlin
   data class ChatMessage(
       val senderId: String = "",
       val text: String = "",
       val timestamp: Long = 0
   )
   ```

2. **Post**: Represents a user’s post. Contains post ID, image URL, publisher, caption, timestamp, and a map of likes. Includes methods to toggle likes and check if a user has liked a post.
   ```kotlin
   data class Post(
       val postId: String = "",
       val postImage: String = "",
       val publisher: String = "",
       val caption: String = "",
       val timestamp: Long = System.currentTimeMillis(),
       val likes: MutableMap<String, Boolean> = mutableMapOf()
   )
   ```

3. **User**: Represents a user profile. Contains user ID, name, bio, email, FCM token, and profile image.
   ```kotlin
   data class User(
       val uid: String = "",
       val fullname: String = "",
       val userName: String = "",
       val bio: String = "",
       val email: String = "",
       val fcmToken: String = "",
       val image: String = ""
   )
   ```

### Firebase Integration:
- **Authentication**: Handles user sign-in and registration with Firebase Authentication.
  ![image](https://github.com/user-attachments/assets/6c949962-6497-4333-8efb-25722489ec18)
- **RealTime DataBase**: Stores chat messages, posts, and user data
![image](https://github.com/user-attachments/assets/e0e26880-4ad8-499b-aa9f-2722cde647cb)
- **Storage**: Stores profile images and post images in Firebase Storage.
- ![image](https://github.com/user-attachments/assets/af10e77c-7947-41ef-9b49-0229dcbc0775)
- **Cloud Messaging**: Sends push notifications to users for new messages using Firebase Cloud Messaging (FCM).
![image](https://github.com/user-attachments/assets/22e30094-dd2a-4c92-9d8c-3f82a2a7c9a6)


### Firebase Function:
- **sendMessageNotification**: Sends a push notification to a user when a new message is received in the chat.
  I had to do a deploy function in my terminal: ![image](https://github.com/user-attachments/assets/d20ba143-a285-438f-af1e-f34137d3d8eb)

### Activities:
1. **ChatActivity**: Displays the chat interface where users can send and receive messages.
2. **ChatListActivity**: Displays a list of active chats.
4. **LoginActivity**: Handles user login using Firebase Authentication.
5. **MainActivity**: The main entry point of the app; contains the home screen.
6. **SignInActivity**: Handles sign-in functionality for existing users.
7. **SignUpActivity**: Handles user registration.
8. **UserProfileActivity**: Displays and allows users to edit their profile information.
9. **UserSettingActivity**: Allows users to manage app settings and preferences.

---
### Project Dependencies and Libraries

Below is a brief description of the libraries and plugins used in this Android project:

#### **Plugins:**
- **Android Application Plugin**: Used to build and package the Android application.
- **Kotlin Android Plugin**: Provides support for Kotlin in the Android project.
- **Google Services Plugin**: Integrates Firebase services, such as authentication, messaging, and analytics, into the Android project.

#### **Android Configuration:**
- **androidx.core.ktx**: Provides Kotlin extensions for Android's core functionality, enabling easier interactions with Android components.
- **androidx.appcompat**: Supports backward-compatible versions of Android's modern UI components and ensures compatibility with older Android versions.
- **material**: Implements Google's Material Design components and functionality, providing a rich set of UI elements like buttons, dialogs, and floating action buttons.
- **androidx.constraintlayout**: Offers a flexible and powerful layout manager for building complex UIs without nested layouts.
- **androidx.lifecycle-livedata-ktx**: Provides LiveData functionality with Kotlin extensions, making it easier to manage UI-related data in a lifecycle-conscious way.
- **androidx.lifecycle-viewmodel-ktx**: Provides Kotlin extensions for the ViewModel class, which is used to store and manage UI-related data in a lifecycle-conscious way.
- **androidx.navigation-fragment-ktx**: Adds Kotlin extensions for using the Navigation component with fragments.
- **androidx.navigation-ui-ktx**: Provides Kotlin extensions for integrating the Navigation component with UI elements like the app bar and bottom navigation view.
- **hdodenhof.circleimageview**: A library for displaying circular images, commonly used for user profile images.
- **androidx.activity**: Offers support for modern Android activities, helping to manage their lifecycle and interactions.
- **firebase-messaging-ktx**: Provides Kotlin extensions for Firebase Cloud Messaging, enabling push notifications in the app.

#### **Testing Libraries:**
- **JUnit**: A framework for writing and running unit tests for your Android project.
- **androidx.junit**: Provides support for running JUnit tests on Android devices.
- **androidx.espresso-core**: A UI testing library for interacting with UI components in automated tests.

#### **Firebase Libraries:**
- **firebase-storage**: Enables the storage of files (such as profile pictures and posts) in Firebase Cloud Storage.
- **firebase-analytics**: Provides integration with Firebase Analytics to track user behavior and app performance.
- **firebase-ui-auth**: A UI library that simplifies the implementation of Firebase Authentication flows (e.g., email sign-in, Google sign-in).
- **firebase-database**: Provides real-time database functionality with Firebase Realtime Database for storing and syncing data.
- **firebase-messaging**: Enables push notifications via Firebase Cloud Messaging, allowing real-time communication with users.

#### **Other Libraries:**
- **picasso**: An image loading and caching library that simplifies displaying images in Android apps.
- **glide**: An image loading and caching library that is optimized for smooth and efficient image loading in Android apps.
- **android-image-cropper**: A library that simplifies the process of cropping images in Android applications.
- **androidx.swiperefreshlayout**: Provides a swipe-to-refresh layout, allowing users to refresh content in a list or view by swiping down.

These libraries and tools provide a robust and efficient foundation for building and maintaining the application, while ensuring a smooth user experience, easy data management, and seamless Firebase integration.
