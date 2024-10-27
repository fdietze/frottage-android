# https://docs.earthly.dev/basics

VERSION 0.8

CACHE_INIT:
  FUNCTION
  ARG target
  RUN mv "$target" /cache_init
  CACHE --persist --chmod 0777 "$target"
  # if cache is empty, init with backup
  RUN if [ -z "$(ls -A "$target")" ]; then \
        echo "cache empty. initializing..." && \
        bash -c 'shopt -s dotglob && mv /cache_init/* "$target"'; \
      fi \
    && rm -r /cache_init

devbox:
  FROM jetpackio/devbox:latest
  # code generated using `devbox generate dockerfile`:
  # Installing your devbox project
  WORKDIR /code
  USER root:root

  DO +CACHE_INIT --target="/nix/store"

  RUN mkdir -p /code && chown ${DEVBOX_USER}:${DEVBOX_USER} /code
  USER ${DEVBOX_USER}:${DEVBOX_USER}
  COPY --chown=${DEVBOX_USER}:${DEVBOX_USER} devbox.json devbox.json
  COPY --chown=${DEVBOX_USER}:${DEVBOX_USER} devbox.lock devbox.lock
  COPY --chown=${DEVBOX_USER}:${DEVBOX_USER} --dir flakes ./
  RUN devbox run -- echo "Installed Packages."
  RUN find / \( -type f -o -type d \) -mindepth 1 -maxdepth 1 -print0 | xargs -0 du -sh | sort -hr | head -20 \
   && find /nix/store \( -type f -o -type d \) -mindepth 1 -maxdepth 1 -print0 | xargs -0 du -sh | sort -hr | head -20 


build:
  FROM +devbox
  CACHE --chmod 0777 /home/devbox/.gradle
  COPY --dir app assets build.gradle.kts gradle.properties settings.gradle.kts ./
  RUN devbox run -- gradle assembleRelease --no-daemon
  SAVE ARTIFACT app/build/outputs/apk/debug/frottage.apk app.apk

lint:
  FROM +devbox
  RUN wget https://github.com/mrmans0n/compose-rules/releases/download/v0.4.16/ktlint-compose-0.4.16-all.jar -O ktlint-compose.jar
  COPY --dir build.gradle.kts app ./
  RUN devbox run -- ktlint -R ktlint-compose.jar

ci-test:
  # BUILD +lint
  BUILD +build
