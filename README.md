[![Build status](https://ci.appveyor.com/api/projects/status/twce208g6yb18vgl/branch/development?svg=true)](https://ci.appveyor.com/project/shayaantx/botdarr/branch/development)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# Summary

Made this simple slack/discord/telegram bot so I could access radarr, sonarr, and lidarr all from a multiple slack/discord/telegram channels without a UI/server.

<br/>

## Currently Supported API's

- [x] Radarr (v2)
- [ ] Radarr (v3)
- [ ] Sonarr (v2)
- [x] Sonarr (v3)
- [x] Lidarr (v1)

## Currently Supported Chat Client's

- [x] Discord
- [x] Slack
- [x] Telegram

## Currently Supported Feature's

- [x] Add movie by search string or tmdb id
- [x] Add show by search string or tvdb id
- [x] Add artist by search string or foreign artist id (available in search results)
- [x] Show downloads for movies, show, or artists
- [x] Configurable value for amount of downloads to show
- [x] Configurable value for amount of results to show during searches
- [x] Configurable value for max number of movies, shows, and artists per user
- [x] Configurable command prefix (i.e., /help, $help, !help)
- [x] Configurable value for url base for radarr, sonarr, and lidarr
- [x] Lookup torrents for movies and force download
- [x] (discord/slack only) Thumbs up reaction will add search results 
- [x] User requests audited to local database
- [ ] Cancel/blacklist existing downloads
- [ ] Episode/season search
- [ ] Album/song search
- [ ] Run bot in mode where all 3 chat clients work in same process (right now you would need 3 separate processes/containers)

## Discord Bot Installation

See https://github.com/shayaantx/botdarr/wiki/Install-Discord-Bot

## Slack Bot Installation

See https://github.com/shayaantx/botdarr/wiki/Install-Slack-Bot

<b>(WARNING)</b> If you are using slack "/help" has been deprecated by slack, so pick a different prefix for your commands (i.e., command-prefix=$). Otherwise help commands won't work

## Telegram bot installation

See https://github.com/shayaantx/botdarr/wiki/Install-Telegram-Bot

## Jar installation/Configuration

1. Get latest copy of botdarr botdarr-release.jar
1. Make sure you have openjdk 8 or oracle java 8 installed on your machine
1. Make sure you run botdarr on the same type of OS has radarr, sonarr, and lidarr (or paths when adding content won't match target OS)
1. Create a folder called "database" in same folder you run jar in
1. Create a file called "properties" (without double quotes) in same folder as the jar
1. Fill it with the following properties (you can omit sonarr properties if you aren't using it, same with radarr/lidarr, however everything else listed below is required)
1. You can only configure discord or slack or telegram token/channels, otherwise you will get an error during startup
1. There are is an available option for url base for both radarr/sonarr. If you have a url base and use radarr WITHOUT configuring the url base here, 
I've found radarr will execute most api requests normally, but /api/movie POST requests wont (assume this is a bug but haven't had time to investigate yet). 
Radarr seems to return a 200 http code, not actually add the movie, and return json as if you are calling /api/movie as a GET request, unless you prefix 
the api url with your radarr url base. So MAKE SURE you account for this in your config/setup.
```
# your discord bot token
discord-token=
# the discord channel(s) you want the bot installed on
discord-channels=

# Your slack bot oauth authentication token
slack-bot-token=
# Your slack user oauth authentication token
slack-user-token=
# the slack channel(s) you want the bot installed on
slack-channels=

# Your telegram bot token
telegram-token=
# Your actual telegram channels your bot can respond in
# this should be a list containing the name and id of the channel, i.e., CHANNEL_NAME:CHANNEL_ID
# to get the channel id, right click any post in private channel and copy post link
# you should see something like this, https://t.me/c/1408146664/63
# the id is between c/<id>/<postId>
# example: plex-channel1:id1,plex-channel2:id2
telegram-private-channels=

# your radarr url (i.e., http://SOME-IP:SOME-PORT)
radarr-url=
# your radarr token (go to Radarr->Settings->General->Security->Api Key)
radarr-token=
# the root path your radarr movies get added to
radarr-path=
# the default quality profile you want to use (go to Radarr->Settings->Profiles)
radarr-default-profile=
# leave empty if you never changed this in radarr
radarr-url-base=

# your radarr url (i.e., http://SOME-IP:SOME-PORT)
sonarr-url=
# your sonarr token (go to Sonarr->Settings->General->Security->Api Key)
sonarr-token=
# the root path your sonarr shows get added to
sonarr-path=
# the default quality profile you want to use (go to Sonarr->Settings->Profiles)
sonarr-default-profile=any
# leave empty if you never changed this in sonarr
sonarr-url-base=

# your lidarr url (i.e., http://SOME-IP:SOME-PORT)
lidarr-url=
# your lidarr token (go to Lidarr->Settings->General->Security->Api Key)
lidarr-token=
# the root path your lidarr artists get added to
lidarr-path=
# the default quality profile you want to use (go to Lidarr->Settings->Profiles)
lidarr-default-quality-profile=
# the default metadata profile you want to use (go to Lidarr->Settings->Profiles)
lidarr-default-metadata-profile=
# leave empty if you never changed this in lidarr
lidarr-url-base=

# If you don't want to limit user requests, don't set these properties
# The max number of movie requests per user per month, day, or week
#max-movie-requests-per-user=
# The max number of show requests per user per month, day, or week
#max-show-requests-per-user=
# The max number of artist requests per user per month, day, or week
#max-artist-requests-per-user=100
# The threshold type for max requests
# WEEK, MONTH, DAY
# WEEK is from monday to sunday
#max-requests-threshold=

# The max number of downloads to show (default is 20)
# If you set this to any value less than or equal to 0, no downloads will show
#max-downloads-to-show=20

# The max number of results to show per search command (default is 20)
# If you set this to any value less than 0, the bot won't startup
#max-results-to-show=20

# The command prefix (default is /)
# Any prefix is allowed (but I haven't tested every single prefix in every client)
command-prefix=/
```

1. Run the jar using java
```
nohup java -jar botdarr-release.jar &
```
<br/>

## Run with Docker

1. Docker images are here https://cloud.docker.com/repository/docker/shayaantx/botdarr/general
1. Create a folder on your host called "botdarr"
1. Create a logs folder in the botdarr folder (BOTDARR_HOME)
1. Put your properties file in botdarr folder (BOTDARR_HOME)
1. Create a folder called "database" in the botdarr folder (BOTDARR_HOME)
1. Then run below command (replace BOTDARR_HOME variables)
```
# for latest
docker run -d --name botdarr -v /BOTDARR_HOME/database:/home/botdarr/database -v /BOTDARR_HOME/properties:/home/botdarr/config/properties -v /BOTDARR_HOME/logs:/home/botdarr/logs shayaantx/botdarr:latest &
```

Or if you want to use docker-compose

```
version: '2.2'
botdarr:
    image: shayaantx/botdarr:latest
    container_name: botdarr
    volumes:
       - /BOTDARR_HOME/properties:/home/botdarr/config/properties
       - /BOTDARR_HOME/logs:/home/botdarr/logs
       - /BOTDARR_HOME/database:/home/botdarr/database
```


<br/>

## Usage

* Type /help in your configured chat client to get information about commands and what is supported
* Notifications will appear indicating the current downloads (based on your configuration for max downloads), their status, and their time remaining.
* When you search for content (i.e., /movie title add History of Dumb People) if too many results are returned you will be presented with multiple results. You can either use the thumbs up reaction (in discord or slack) or copy the add command (which will be embedded in the result) into the chat client.

![](https://raw.githubusercontent.com/shayaantx/botdarr/development/images/search-results.png)
* The success of the bot depends a lot on how diverse your trackers you use in radarr, sonarr, lidarr and your quality profiles. If you have a trackers with little content or very restrictive quality profiles, a lot of content will never actually get added. The bot can't do anything about this.
* Example commands:
  * /movie title add Lion Fling
  * /show title add One Piece
  * /movie find new zombies
  * /artist find new Linkin Flarp
  * /movie downloads
  * /show downloads
  * /help
  * /shows help
  * /movies help
<br/>
