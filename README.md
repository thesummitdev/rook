# rook
Rook is a open source bookmark service built to provide a performant solution to organize,
tag and search bookmarks. Rook bundles a database (sqlite3) with a Java API server and lightweight
web application.

## Getting Started

alpha container images have been published and can be pulled from:

`docker pull ghcr.io/thesummitdev/rook:arm`
`docker pull ghcr.io/thesummitdev/rook:latest`

For sample configuration see the [docker-compose.yml](docker-compose.yml) in this repo.

To persist data between containers, make sure to bind a volume to the `$DATA` path. 
It is recommended to use `$DATA=/data/rook` and use a volume mount for `/data` inside the container.
With such a setup, when the container is stopped (i.e. if a new version of the container is pulled)
and later restart the application's configuration and data will not be lost.

## Features

* A single deployment can support multiple unique users whom can all store their own bookmarks and tagging metadata.
* A container based deployment means updating to a new version is as simple as pulling the latest container image.
  With a built in automatic database migrations managed by the rook server.

## Contributing

* If this project interests you contact me on Mastodon: [@tylersaunders@fosstodon.org](https://fosstodon.org/@tylersaunders)
* [Good first issues](https://github.com/thesummitdev/rook/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) 
  are marked in the issue tracker if you are looking to contribute.

## Acknowledgements

* Initial inspiration was drawn from sites such as [pinboard](https://pinboard.in/).
* Icons are provided courtesy of the excellent [tabler-icons.io](https://tabler-icons.io).

## Licenses

* [GNU GPL v3](http://www.gnu.org/licenses/gpl.html)
* Copyright 2021-2023 thesummit.dev
