# Getting started

- goto: https://console.cloud.google.com/
- create new project -> img1 && img2
- result img3
- add google calendar api under library
- configure oauth consent screen
    - external
    - add information
    - add google calendar api scope
    - add yourself as testuser
- go to credentials
    - create new oauth
      ![create image](../img/img4.png)
    - Webapplication remember adding the auth redirect uris
      ![create image](../img/img5.png)
    - Dowload json -> credentials json
- update properties in application.yml
- create a config component -> GoogleConfiguration
- update the config in the application configuration to provide these data into the application

## Application Code

- the `GoogleConfiguration` just contains the env variables 
- The `StateService` just holds the current tokens 
  - this is not secure!! you should link this to a user and save it somewhere else
  - i just do it for local testing
  - this is just to demonstrate how to login and get something from the api with google auth but without the spring boot oauth
- The `RouteController` just check if you have a configured token
  - if not
    - show the login page with the redirect to google (could also be done directly)
  - if
    - load the latest 20 calendar entries and show the title
  - important is the `GoogleCalendarService`
    - it contains the fetching of the token and also the fetching of data from the calendar api
    - most important here is just the `AuthorizationCodeTokenRequest`
    
```java
public Optional<TokenResponse> authorize(String code) {
    val scopes = new ArrayList<String>();
    scopes.add("https://www.googleapis.com/auth/calendar");
        return Optional.of(new AuthorizationCodeTokenRequest(new NetHttpTransport(), new GsonFactory(),
                new GenericUrl("https://oauth2.googleapis.com/token"), code)
                .setRedirectUri(config.getRedirectUri())
                .setCode(code)
                .setScopes(scopes)
                .set("client_id",config.getClientId())
                .set("client_secret",config.getClientSecret())
                .set("project_id",config.getProjectId())
                .set("access_type", "offline")
                .set("prompt", "consent")
                .execute());
}

```

   - listing the entries is as simple as that

```java
val calendar = new Calendar.Builder(new NetHttpTransport(), new GsonFactory(), null).setApplicationName(config.getProjectId()).setHttpRequestInitializer(request -> {
request.getHeaders().setAuthorization("Bearer " + tokenResponse.getAccessToken());
request.getHeaders().setContentType("application/json");
}).build();
val events = calendar.events().list("primary").setMaxResults(20).setTimeMin(new DateTime(System.currentTimeMillis())).setSingleEvents(true).setOrderBy("startTime").execute();
return Optional.of(events.getItems());
```

- now all we have to do is using the new data or providing them to the frontend
- i used mustache for that to just showcase this super simple
- the templates are int the `resources/templates` folder