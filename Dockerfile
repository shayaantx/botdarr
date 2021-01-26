FROM openjdk:8
RUN mkdir -p /home/botdarr
ADD target/botdarr-release.jar /home/botdarr

WORKDIR /home/botdarr

COPY ./docker/entrypoint.sh ./entrypoint.sh
RUN chmod +x ./entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]
CMD ["java", "-jar", "botdarr-release.jar"]