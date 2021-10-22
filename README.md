
# Let's talk Chat App
This is an simple android messaging application developed in java that lets users communicate securely 
.

## Screenshots
<table>
  <tr>
    <td>Signin Page</td>
     <td>Message List Page</td>
     <td>Settings Page</td>
    <td> Chat Page</td>
  </tr>
  <tr>
    <td><img src="https://user-images.githubusercontent.com/28120359/137472989-d6b43154-8d6f-45de-a7cc-1a8e729664d4.png" width=100% height=50%></td>
    <td><img src="https://user-images.githubusercontent.com/28120359/137605550-62145589-806e-45e4-ab29-ce26fb1530fa.png" width=100% height=50%></td>
    <td><img src="https://user-images.githubusercontent.com/28120359/138475247-baaf458b-da9c-4e53-bc41-666285245a26.png" width=100% height=50%></td>
    <td><img src="https://user-images.githubusercontent.com/28120359/138475344-806e02fa-d73e-4a88-97f2-c6356c8bfe60.png" width=100% height=50%></td>
    


  </tr>
 </table>






## Features

Messaging

- Send and receive messages with other users
- Users are able to share photos, videos and audios with file size not bigger than 2Mb
- Messages arrive in the inbox in users preferred language enabling seamless conversations
- The available language translations are French, English, Spanish, Swahili and German 

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
