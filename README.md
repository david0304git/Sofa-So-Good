# Sofa So Good

## Screens / Pages
1. Login Page
2. Main Menu
3. Setting Page
4. Credit Page
5. Level Select Page
6. Leaderboard Page
7. Pasue Overlay
8. Level Result Page

## Project Structure
- LoginActivity.java / activity_login.xml  
Handles user account and password input and verification
- MainActivity.java / activity_main.xml  
Displays the game title and main menu (Start Game, Settings, Leaderboard)
- SettingsActivity.java / activity_settings.xml  
Game settings (sound effects, language, control methods)
- CreditsActivity.java / activity_credits.xml  
Show developer list
- LeaderboardActivity.java / activity_leaderboard.xml  
Displays player score or completion time

## Archive Structure
SofaSoGood/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/speed/sofasogood/
│   │   │   │   ├── game/
│   │   │   │   │   ├── levels/
│   │   │   │   │   │   ├── Level1Activity.java
│   │   │   │   │   │   └── Level2Activity.java
│   │   │   │   │   ├── model/
│   │   │   │   │   ├── GameView.java
│   │   │   │   │   ├── LevelResultActivity.java
│   │   │   │   │   └── Pause.java
│   │   │   │   ├── CreditsActivity.java
│   │   │   │   ├── LeaderboardActivity.java
│   │   │   │   ├── LevelSelectActivity.java
│   │   │   │   ├── LoginActivity.java
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── OutlinedTextButton.java
│   │   │   │   └── SettingsActivity.java
│   │   │   ├── res/
│   │   │   │   ├── anim/
│   │   │   │   │   ├── button_press.xml
│   │   │   │   │   └── button_release.xml
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   │   ├── ic_launcher_foreground.xml
│   │   │   │   │   ├── main_background.png
│   │   │   │   │   ├── main_button.xml
│   │   │   │   │   └── menu_title.png
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_credits.xml
│   │   │   │   │   ├── activity_leaderboard.xml
│   │   │   │   │   ├── activity_level_result.xml
│   │   │   │   │   ├── activity_level_select.xml
│   │   │   │   │   ├── activity_login.xml
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_settings.xml
│   │   │   │   │   ├── level1.xml
│   │   │   │   │   └── level2.xml
│   │   │   │   ├── raw/
│   │   │   │   │   └── button_click.mp3
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── styles.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── values-night/
│   │   │   │   │   └── themes.xml
│   │   │   │   └── xml/
│   │   │   │       ├── backup_rules.xml
│   │   │   │       └── data_extraction_rules.xml
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/.../ExampleInstrumentedTest.java
│   │   └── test/.../ExampleUnitTest.java
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml
│   └── gradle-daemon-jvm.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md
├── gradlew
└── gradlew.bat
