# AZ-Chat

# Preview
<p>
<img src="https://github.com/omarzer0/AZ-Chat/blob/main/assets/image1.jpeg" width="220">
<img src="https://github.com/omarzer0/AZ-Chat/blob/main/assets/image2.jpeg" width="220">
<img src="https://github.com/omarzer0/AZ-Chat/blob/main/assets/image3.jpeg" width="220">
<img src="https://github.com/omarzer0/AZ-Chat/blob/main/assets/image4.jpeg" width="220">
<img src="https://github.com/omarzer0/AZ-Chat/blob/main/assets/image5.jpeg" width="220">
<img src="https://github.com/omarzer0/AZ-Chat/blob/main/assets/image6.jpeg" width="220">
<img src="https://github.com/omarzer0/AZ-Chat/blob/main/assets/image7.jpeg" width="220">
<img src="https://github.com/omarzer0/AZ-Chat/blob/main/assets/image8.jpeg" width="220">
<img src="https://github.com/omarzer0/AZ-Chat/blob/main/assets/image9.jpeg" width="220">
<img src="https://github.com/omarzer0/AZ-Chat/blob/main/assets/image10.jpeg" width="220">
</p>

## How to use 
  - Add your SERVER_KEY="your key" in a file in the root folder by the name of "apikey.properties".
  - Connect your project with firebase (add google-service.json to PROJECT_NAME/app/) and use firestore, storage, phone Auth and FCM.
  - Add your app sha-1 and sha-256 [Read this](https://stackoverflow.com/a/39144864/12863720) and you are ready to go.

# Features
## Ready
- Login with your phone number
- Send a Private chat
- Create a Group chat
- Send text, image, and voice messages (INSTANTLY)
- Delete and/or update messages
- Reactions to messages
- Notification for private and group chats
- Realtime messaging and updates
- Online, offline, and writing status of a private chat
- Message status (have been sent, the user saw the message, updated, deleted, sent at, and sent by )
- Offline support for all messages type
- Change your profile photo, hide your phone number, write and edit your name and bio
- Block users 
- Leave groups
- Preview images with zoom in and out
- Multi notification but only one from each chat or group

## Future plans (Will be added to [notion](https://cooperative-utensil-832.notion.site/896e68780bfe4c7cbb004d2b130474e8?v=383417e84ed0439986c743340a993751)) but here are a few:
- Offline Chat using WIFI or nearby API.
- Video and Audio calls.
- Bottom Sheet Fragment for Image Viewer instead of normal fragment
- Private messages on a password-protected screen.
- Add audio when sending a message.

## Known issues
- None yet! :)


## Tech stack & Open-source libraries
- Minimum SDK level 21
- [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) together with [Flow](https://developer.android.com/kotlin/flow) for asynchronous streams and one side viewModel to fragment communication.

- Firebase used extensively in this app: 
  - [Firestore](https://firebase.google.com/docs/firestore)
  - [Cloud Storage](https://firebase.google.com/docs/storage)
  - [FCM](https://firebase.google.com/docs/cloud-messaging)
  - [Authentication](https://firebase.google.com/products/auth)

- Dagger Hilt for dependency injection.
- [Retrofit](https://square.github.io/retrofit/) A type-safe HTTP client for Android and Java
- [Glide](https://github.com/bumptech/glide) for loading images.
- [SDP](https://github.com/intuit/sdp) for different screen sizes
- [Lottie files](https://airbnb.io/lottie/) for playing beautiful animations
- Shimmer for facebook-like loading effect

- JetPack:
  - Lifecycle - Dispose of observing data when the lifecycle state changes.
  - ViewModel - UI related data holder, lifecycle aware.
  - ViewBinding - Interact with XML views in safeway and avoid findViewById() 
  - Navigation Component - Make it easy to navigate between different screens and pass data in type-safe way

- Architecture:
  - [MVI Architecture (Model-View-Intent)](https://www.raywenderlich.com/817602-mvi-architecture-for-android-tutorial-getting-started)
  - Repository pattern

- [Material-Components](https://github.com/material-components/material-components-android) - Material design components like cardView
- SaveStateHandler to handle process death
