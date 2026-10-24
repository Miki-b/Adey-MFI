# Same Java 21 dev container image, minus one broken apt source.
# The image ships an apt repository for Yarn whose signing key has expired (NO_PUBKEY 62D54FD4003F6525);
# "apt-get update" then fails and the docker-in-docker feature cannot install. We don't use Yarn.
FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm
RUN rm -f /etc/apt/sources.list.d/yarn.list
