
# Let's talk Chat App
This is an simple android messaging application developed in java that lets users communicate securely 
.

## Screenshots
![Screenshot_20211015_122843](https://user-images.githubusercontent.com/28120359/137472989-d6b43154-8d6f-45de-a7cc-1a8e729664d4.png)

![Screenshot_20211015_123114](https://user-images.githubusercontent.com/28120359/137473117-e3951445-0dfe-4983-b1c1-9ce9fd82fef0.png)

![Screenshot_20211015_123258](https://user-images.githubusercontent.com/28120359/137473203-fbca0c63-5b17-4de8-ad63-0e41706097be.png)

![Screenshot_20211015_123338](https://user-images.githubusercontent.com/28120359/137473228-d5ef0c9f-eb00-411b-9a9c-73b06f10b203.png)
## Features

Messaging

- Send and receive messages with other users
- Users are able to share photos, videos and audios with file size not bigger than 3Mb

Profile

- Update your profile picture
- View other users profile

Authentication

- Uses firebase phone number Authentication 

Cool aesthetics

- Users are able to change chat wallpapers
- Users are able to apply blur to their wallpapers
- Chat body backgrounds change depending on the chat wallpapers

Privacy
- Messages are encrypted before leaving the client using AES encrytpion 
- Users are able to show and hide their last seen status
- Users are able to show and hide their online status
- Users are able to prevent access from unauthorized people through fingerprint lock.


## Installation

Install my-project with git

```bash
  git clone https://github.com/NormanGadenya/ChatApp.git
  cd ChatApp
```

- Create a new Firebase Project in console

- Connect project with Firebase (Tools/Firebase) in Android Studio

- Generate, download, paste google-services.json into the project

- Go to the authentication section in your firebase console and select phone as the sign in method
