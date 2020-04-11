[![Build status](https://ci.appveyor.com/api/projects/status/twce208g6yb18vgl/branch/development?svg=true)](https://ci.appveyor.com/project/shayaantx/botdarr/branch/development)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# Summary

Made this simple slack/discord/telegram bot so I could access radarr, sonarr, and lidarr (not implemented yet) all from a multiple slack/discord/telegram channels

<br/>

## Currently Supported API's

- [x] Radarr (v2)
- [ ] Radarr (v3)
- [ ] Sonarr (v2)
- [x] Sonarr (v3)
- [ ] Lidarr

## Currently Supported Chat Client's

- [x] Discord
- [x] Slack
- [x] Telegram

## Discord Bot Installation

See https://github.com/shayaantx/botdarr/wiki/Install-Discord-Bot

## Slack Bot Installation

See https://github.com/shayaantx/botdarr/wiki/Install-Slack-Bot

## Telegram bot installation

See https://github.com/shayaantx/botdarr/wiki/Install-Telegram-Bot

## Jar installation/Configuration

1. Get latest copy of botdarr botdarr-release.jar
1. Make sure you have openjdk 8 or oracle java 8 installed on your machine
1. Create a folder called "database" in same folder you run jar in
1. Create a file called "properties" (without double quotes) in same folder as the jar
1. Fill it with the following properties (you can omit sonarr properties if you aren't using it, same with radarr, however everything else listed below is required)
1. You can only configure discord or slack or telegram token/channels, otherwise you will get an error during startup
1. There are is an available option for url base for both radarr/sonarr. If you have a url base and use radarr WITHOUT configuring the url base here, 
I've found radarr will execute most api requests normally, but /api/movie POST requests wont (assume this is a bug but haven't had time to investigate yet). 
Radarr seems to return a 200 http code, not actually add the movie, and return json as if you are calling /api/movie as a GET request, unless you prefix 
the api url with your radarr url base.
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

# If you don't want to limit user requests, don't set these properties
# The max number of requests per user per month
#max-requests-per-user=
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
1. Create a logs folder in the botdarr folder
1. Put your properties file in botdarr folder inside a folder named "config"
1. Create a folder called "database" in the botdarr folder
1. Then run below command (replace BOTDARR_HOME variables)
```
# for latest
docker run -d --name botdarr -v /BOTDARR_HOME/database:/home/botdarr/database -v /BOTDARR_HOME/properties:/home/botdarr/config/properties -v /BOTDARR_HOME/logs:/home/botdarr/logs shayaantx/botdarr:latest &

# for stable

docker run -d --name botdarr -v /BOTDARR_HOME/database:/home/botdarr/database -v /BOTDARR_HOME/properties:/home/botdarr/config/properties -v /BOTDARR_HOME/logs:/home/botdarr/logs shayaantx/botdarr:stable &
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

* Type /help in discord to get information about commands and what is supported
* Type /movies help in discord to get information about movie commands
* Notifications will appear indicating the current downloads (based on your configuration for max downloads), their status, and their time remaining.
* (discord only for now) When you react to movie/show results with add commands in the result with a "thumbs up" the content will be added.

<br/>

## Radarr Tips

1. Just cause you add a movie successfully does not mean the movie will show up instantly or at all
   - The way radarr works is you search for a film, then add it, then radarr will start searching through all the configured indexers for a torrent
   - that matches the configure quality profiles the admin user has set. i.e., if there is only a CAM version of the film you want out there
   - but the master user of radarr has configured to disallow CAM quality, then it will not download.
   - If you use "movie find downloads TITLE" or "movie find all downloads TITLE" it can show you the downloads available through radarr for your requested/existing film.
   - Although this functionality is not complete yet, as movies with similar titles will conflict and not show you downloads.
   - I also need to somehow add functionality to let you force specific downloads as well.

2. /movie title add
   - This command will specifically try to add a movie based on title alone. Sometimes there are movies that have same titles or very similar titles
   - When the title cannot be added by title alone, multiple movies will be returned. Embedded in the results is a command to add the movie with an id
   - The command will look something "movie add John Wick: Chapter 4 603692". This command uses the movie title plus the TMDBID to add the movie

3. /movie profiles
   - and this profile is used when identifying downloads.
   - This command shows you all the profiles available in radarr, it does NOT tell you which is the default profile. The default profile is configured by the bot admin
   - and this profile is used when identifying downloads.

4. /movie find new
   - This command uses radarr search api to identify new films.
   - Embedded in the results are commands to add the films directly, like "movie add Ad Astra 570820"

5. /movie find existing
   - This command finds any existing films and gives you information about them.
   - It will tell you if the movie has been downloaded and if the radarr has the file.


## Sonarr Tips

1. Just like radarr, when you add a show with sonarr it doesn't automatically mean the show will magically appear. It really depends
  - on how many trackers your sonarr installation has and how diverse the content within said trackers is.

TODO: need to add more tips for sonarr  
  
<br/>

## Stuff I might add in the future

1. Interactive season search/download (only available in v3 sonarr)
2. Per episode search/download
3. Cancelling/blacklisting downloads (movies and tvshows)
4. When I implement lidarr support I want to search by song instead of just artist/album (since lidarr doesn't support song search)
