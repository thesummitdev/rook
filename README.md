# rook
Rook is a open source bookmark service built to provide a performant solution to organize,
tag and search bookmarks. Rook bundles a high throughput transactional database (postgresql)
with a Java API server and lightweight web application.

## Getting Started

**NOTE:** This project is still under active development.

0.1.0-alpha container images have been published and can be pulled from `docker pull ghcr.io/thesummitdev/rook:latest`

For sample configuration see the [docker-compose.yml](docker-compose.yml) in this repo.

To persist data between containers, make sure to bind a volume to the `$PGDATA` path. It is recommended to use `$PGDATA=/data/rook` and use a volume mount for `/data` inside the container. With such a setup, when the container is stopped (i.e. if a new version of the container is pulled) and later restart the application's configuration and data will not be lost.

## Features

* A single deployment can support multiple unique users whom can all store their own bookmarks and tagging metadata.
* New versions will remain backward compatible with automatic database migrations managed by the rook server.

## Contributing

If this project interests you contact me on Twitter: [_tylersaunders@](https://twitter.com/_tylersaunders)

## Acknowledgements

* Initial inspiration was drawn from sites such as [pinboard](https://pinboard.in/).
* Icons are provided courtesy of the excellent [tabler-icons.io](https://tabler-icons.io).

## Licenses

* [GNU GPL v3](http://www.gnu.org/licenses/gpl.html)
* Copyright 2021 thesummit.dev
