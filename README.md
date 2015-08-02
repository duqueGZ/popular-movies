# popular-movies
Udacity Android Developer Nanodegree. Project 1

In order to make the app works correctly is needed to configure and use a themoviedb.org API_KEY.
To get one, you must to sign up for an account (https://www.themoviedb.org/account/signup) and then ask for your API_KEY
through your profile page.
Since it is not allowed to publicly share your personal API KEY, the code in this repository does not contain mine. So, 
once you have the API KEY, it would be needed to replace 'PLACE_API_KEY_HERE' placeholder in strings.xml resource file
('themoviedb_api_key' string value) by your real and valid API KEY value:

app/src/main/res/values/strings.xml:
```xml
<resources>
    <string name="app_name">Popular Movies</string>
    <string name="action_settings">Settings</string>
    <string name="themoviedb_api_key" translatable="false">PLACE_API_KEY_HERE</string> <!--put here your API KEY -->
</resources>
```
