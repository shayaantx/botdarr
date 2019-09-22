# Summary

Made this discord bot so I could access radarr, sonarr, and lidarr all from a single discord channel

<br/>

## Currently Supported

- [x] Radarr (v2)
- [ ] Radarr (v3)
- [ ] Sonarr (v2)
- [ ] Sonarr (v3)
- [ ] Lidarr

<br/>

## Discord Bot Installation

https://discordpy.readthedocs.io/en/latest/discord.html

<br/>

## Manual Installation

1. Get latest copy of botdar botdar-release.jar
1. Make sure you have openjdk 8 or oracle java 8 installed on your machine
1. Create a file called "properties" (without double quotes) in same folder as the jar
1. Fill it with the following properties
```
# your discord bot token
token=

# your radarr url (i.e., http://SOME-IP:SOME-PORT
radarr-url=
# your radarr token (go to Radarr->Settings->General->Security->Api Key)
radarr-token=
# the root path your radarr movies get added to
radarr-path=
# the default quality profile you want to use (go to Radarr->Settings->Profiles)
radarr-default-profile=
```
1. Run the jar using java
```
nohup java -jar botdar-release.jar &
```
<br/>

## Docker installation

1. Docker images are here https://cloud.docker.com/repository/docker/rudeyoshi/botdar/general
1. Create a folder on your host called botdar
1. Create a logs folder in the botdar folder
1. Put your properties file in this folder
1. Then run below command
```
docker run --name botdar -v /FULL_PATH_TO_PROPS/properties:/home/botdar/config/properties -v logs:/PATH_TO_BOTDAR_FOLDER/botdar/logs rudeyoshi/botdar:latest &
```

<br/>

## Usage

* Type help in discord to get information about commands and what is supported
* Type movies help in discord to get information about movie commands
* Every minute notifications will appear indicating the current downloads, their status, and their time remaining.

<br/>

## Tips

1. Just cause you add a movie successfully does not mean the movie will show up instantly or at all
  * The way radarr works is you search for a film, then add it, then radarr will start searching through all the configured indexers for a torrent
  * that matches the configure quality profiles the admin user has set. i.e., if there is only a CAM version of the film you want out there
  * but the master user of radarr has configured to disallow CAM quality, then it will not download.
  * If you use "movie find downloads TITLE" or "movie find all downloads TITLE" it can show you the downloads available through radarr for your requested/existing film.
  * Although this functionality is not complete yet, as movies with similar titles will conflict and not show you downloads.
  * I also need to somehow add functionality to let you force specific downloads as well.

2. movie title add
  * This command will specifically try to add a movie based on title alone. Sometimes there are movies that have same titles or very similar titles
  * When the title cannot be added by title alone, multiple movies will be returned. Embedded in the results is a command to add the movie with an id
  * The command will look something "movie add John Wick: Chapter 4 603692". This command uses the movie title plus the TMDBID to add the movie

3. movie profiles
  * This command shows you all the profiles available in radarr, it does NOT tell you which is the default profile. The default profile is configured by the bot admin
  * and this profile is used when identifying downloads.

4. movie find new
  * This command uses radarr search api to identify new films.
  * Embedded in the results are commands to add the films directly, like "movie add Ad Astra 570820"

5. movie find existing
  * This command finds any existing films and gives you information about them.
  * It will tell you if the movie has been downloaded and if the radarr has the file.