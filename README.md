## Links
**Front-end:**&nbsp;&nbsp; [repository](https://github.com/willy-it-wonka/Bookshelf-frontend)\
**Demo hosted on AWS:**&nbsp;&nbsp; RDS + Elastic Beanstalk + S3 → [CloudFront](https://d39oa1kkhfrmo.cloudfront.net)\
**Example data for the database:**&nbsp;&nbsp; [repository](https://github.com/willy-it-wonka/Bookshelf-database)
</br></br>

## Tech stack
<img src="https://user-images.githubusercontent.com/25181517/117201156-9a724800-adec-11eb-9a9d-3cd0f67da4bc.png" width="55px" height="auto" alt="java 17">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/117201470-f6d56780-adec-11eb-8f7c-e70e376cfd07.png" width="55px" height="auto" alt="spring boot">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/190229463-87fa862f-ccf0-48da-8023-940d287df610.png" width="55px" height="auto" alt="lombok">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/117207242-07d5a700-adf4-11eb-975e-be04e62b984b.png" width="55px" height="auto" alt="maven">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/183894676-137319b5-1364-4b6a-ba4f-e9fc94ddc4aa.png" width="55px" height="auto" alt="tomact">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/117207493-49665200-adf4-11eb-808e-a9c0fcc2a0a0.png" width="55px" height="auto" alt="hibernate">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/183896128-ec99105a-ec1a-4d85-b08b-1aa1620b2046.png" width="55px" height="auto" alt="mysql">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/117533873-484d4480-afef-11eb-9fad-67c8605e3592.png" width="55px" height="auto" alt="junit">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/183892181-ad32b69e-3603-418c-b8e7-99e976c2a784.png" width="55px" height="auto" alt="mocikto">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/184097317-690eea12-3a26-4f7c-8521-729ebbbb3f98.png" width="45px" height="auto" alt="testcontainers">\
<img src="https://user-images.githubusercontent.com/25181517/192107858-fe19f043-c502-4009-8c47-476fc89718ad.png" width="55px" height="auto" alt="rest api">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/192109061-e138ca71-337c-4019-8d42-4792fdaa7128.png" width="45px" height="auto" alt="postman">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/186711335-a3729606-5a78-4496-9a36-06efcc74f800.png" width="45px" height="auto" alt="swagger">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/192108372-f71d70ac-7ae6-4c0d-8395-51d8870c2ef0.png" width="55px" height="auto" alt="git">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/117207330-263ba280-adf4-11eb-9b97-0ac5b40bc3be.png" width="60px" height="auto" alt="docker">&nbsp;&nbsp;
<img src="https://user-images.githubusercontent.com/25181517/183896132-54262f2e-6d98-41e3-8888-e40ab5a17326.png" width="55px" height="auto" alt="aws">
</br></br>

## Installation
1. Clone the repository. [Instruction](https://www.jetbrains.com/help/idea/set-up-a-git-repository.html#clone-repo).
2. In the `appliction.properties` configure the database and the SMTP ([outlook](https://support.microsoft.com/en-gb/office/pop-imap-and-smtp-settings-for-outlook-com-d088b986-291d-42b8-9564-9c414e2aa040), [gmail](https://www.getmailbird.com/setup/access-gmail-com-via-imap-smtp) or google it: "local SMTP" e.g. [MailDev](https://github.com/maildev/maildev), [Mailpit](https://github.com/axllent/mailpit)).\
   For gmail: [instruction](https://itsupport.umd.edu/itsupport?id=kb_article_view&sysparm_article=KB0015112) how to create an app password.
4. Go to `BookshelfApplication` and run it.\
   Or at the command prompt, go to the root directory of the project and write:
   ```
   mvn spring-boot:run
   ```
5. Now you can send http requests using [Postman](https://www.postman.com) or run the [front-end](https://github.com/willy-it-wonka/Bookshelf-frontend) of this application.
6. REST API Documentation:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

## Launching with Docker
1. Open Docker.\
   In cmd/terminal go to the root directory of the project and execute the following command:
   ```
   docker-compose up --build
   ```
2. Now you can send http requests using [Postman](https://www.postman.com) or run the [front-end](https://github.com/willy-it-wonka/Bookshelf-frontend?tab=readme-ov-file#launching-with-docker) of this application.
</br></br>

## Description
Your virtual library.\
With this application you can manage your library. Create a reading schedule. Keep a record of what you read and when. Write down notes and conclusions about the books you have read.
</br>
* REST API documentation
* registration and login
* user account management
* sending confirmation email (SMTP)
* resetting a forgotten password via e-mail (SMTP)
* security configuration
* JWT authorization
* exception handling
* ASPECTS: exceptions logging, methods info
* CRUD operations
* unit testing
* integration testing
</br></br>

## Database schema
![database_schema](https://raw.githubusercontent.com/willy-it-wonka/Bookshelf-database/aa0c191a26ebb7c236b9ea8aa067ac5068d174fd/assets/Bookshelf%20database%20schema.png)\
_Generated by MySQL Workbench → Database → Reverse Engineer._
</br></br>

## Future features
* Reading schedule.
* Reading statistics.
* Implement generative AI. E.g., use OpenAI API to generate book summaries, to recommend new books.
* Use an external API to compare book prices at different bookstores.
* Integration testing of controllers:\
  Divide into unit and E2E tests, extend them.\
  Remove deprecated `@MockBean`.
* Consider moving pagination, sorting and search logic from the front-end to the back-end.\
  Fewer portions of data – reduces network load. Less memory used in the browser – smoother application performance on the client side.
