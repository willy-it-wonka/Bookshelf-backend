## Links
Front-end: [repository](https://github.com/willy-it-wonka/Bookshelf-frontend)\
Demo: [AWS](http://bookshelf-app.s3-website.eu-north-1.amazonaws.com)
</br></br>

## Tech stack
<img src="https://user-images.githubusercontent.com/25181517/117201156-9a724800-adec-11eb-9a9d-3cd0f67da4bc.png" width="50px" height="auto" alt="java 17">&nbsp;&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/183891303-41f257f8-6b3d-487c-aa56-c497b880d0fb.png" width="50px" height="auto" alt="spring boot">&nbsp;&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/117207242-07d5a700-adf4-11eb-975e-be04e62b984b.png" width="50px" height="auto" alt="maven">&nbsp;&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/183894676-137319b5-1364-4b6a-ba4f-e9fc94ddc4aa.png" width="50px" height="auto" alt="tomact">&nbsp;&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/117207493-49665200-adf4-11eb-808e-a9c0fcc2a0a0.png" width="50px" height="auto" alt="hibernate">&nbsp;&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/183896128-ec99105a-ec1a-4d85-b08b-1aa1620b2046.png" width="55px" height="auto" alt="mysql">&nbsp;&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/117533873-484d4480-afef-11eb-9fad-67c8605e3592.png" width="55px" height="auto" alt="junit">&nbsp;&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/183892181-ad32b69e-3603-418c-b8e7-99e976c2a784.png" width="55px" height="auto" alt="mocikto">&nbsp;&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/184097317-690eea12-3a26-4f7c-8521-729ebbbb3f98.png" width="45px" height="auto" alt="testcontainers">&nbsp;&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/192107858-fe19f043-c502-4009-8c47-476fc89718ad.png" width="50px" height="auto" alt="rest api">&nbsp;&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/186711335-a3729606-5a78-4496-9a36-06efcc74f800.png" width="45px" height="auto" alt="swagger">
</br></br>

## Installation
1. Clone the repository. [Instruction](https://www.jetbrains.com/help/idea/set-up-a-git-repository.html#clone-repo).
2. In the appliction.properties configure the database and the SMTP ([outlook](https://support.microsoft.com/en-gb/office/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040), [gmail](https://www.getmailbird.com/setup/access-gmail-com-via-imap-smtp) or google it: "local SMTP" e.g. [MailDev](https://github.com/maildev/maildev), [Mailpit](https://github.com/axllent/mailpit)).
3. Go to BookshelfApplication and run it.
4. Now you can send http requests using [Postman](https://www.postman.com) or run the [front-end](https://github.com/willy-it-wonka/Bookshelf-frontend) of this application.
5. REST API Documentation:
   ``` bash
   http://localhost:8080/swagger-ui/index.html
   ```
</br>

## Description
Your virtual library.\
With this application you can manage your library. Create a reading schedule. Keep a record of what you read and when. Write down notes and conclusions about the books you have read.
</br>
* REST API documentation
* registration and login
* sending confirmation email
* security configuration
* JWT authorization
* exception handling
* CRUD books, notes
* unit testing
* integration testing
</br></br>

## Future features
* Reading schedule.
