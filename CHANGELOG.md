# Changelog

## [1.3.0](https://github.com/rattatarr/rattatarr/compare/v1.2.0...v1.3.0) (2026-06-28)


### Features

* dismiss watched and show all watched movies/series button ([#48](https://github.com/rattatarr/rattatarr/issues/48)) ([fd90735](https://github.com/rattatarr/rattatarr/commit/fd90735f9fcb202fae89d164ba1759df99ffcfe6))
* rich text review ([#50](https://github.com/rattatarr/rattatarr/issues/50)) ([400a299](https://github.com/rattatarr/rattatarr/commit/400a2991220c47f5c4713f6247d3402c389d994e))
* track manual mark watch events in jellyfin ([#49](https://github.com/rattatarr/rattatarr/issues/49)) ([b603c3a](https://github.com/rattatarr/rattatarr/commit/b603c3aac88368f7ea23baa63605ba55b8f51a82))

## [1.2.0](https://github.com/rattatarr/rattatarr/compare/v1.1.1...v1.2.0) (2026-05-17)


### Features

* add filter by last watched in movies/series ([c27468a](https://github.com/rattatarr/rattatarr/commit/c27468ab53b0e19435cb4ffd33520ba5c9209753))
* sonarr/radarr anime instances ([#16](https://github.com/rattatarr/rattatarr/issues/16)) ([ca74345](https://github.com/rattatarr/rattatarr/commit/ca74345aaaa3f0c42566c5ce6e789e019e82e1e3))
* update to newer version chip alert ([87281b7](https://github.com/rattatarr/rattatarr/commit/87281b7d4cb480b0f5669614e9eccb894d2178c3))
* year rewind ([#15](https://github.com/rattatarr/rattatarr/issues/15)) ([44df312](https://github.com/rattatarr/rattatarr/commit/44df312c74f93fe884c5ef9b03c1e1c40279dd89))

## [1.1.1](https://github.com/rattatarr/rattatarr/compare/v1.1.0...v1.1.1) (2026-05-01)


### Bug Fixes

* jellyfin runtime media tracks only watched/equivalent complete ([30df7ba](https://github.com/rattatarr/rattatarr/commit/30df7ba8addc6a954e6fb8e0bec2eead18dbc487))
* on rated item fill the star ([90f6df9](https://github.com/rattatarr/rattatarr/commit/90f6df98ff00b1ce3e1b79952bac2d20c2dfaac0))
* remove dead view from recent trends & default to jellyfin view if data for 365 is 0 ([e585529](https://github.com/rattatarr/rattatarr/commit/e585529aae0b43f1653f9f8fe6059a443d73eba0))
* scroll restoration breaking query param people search ([be32719](https://github.com/rattatarr/rattatarr/commit/be32719f31c7eee8d5f84ebff30f7ae990db35a4))
* scroll restoration for movies/series views ([8735915](https://github.com/rattatarr/rattatarr/commit/8735915d03bef482e24f015aee2d69ac06e525df))

## [1.1.0](https://github.com/rattatarr/rattatarr/compare/v1.0.0...v1.1.0) (2026-04-26)


### Features

* radarr integration & flyway migrations ([#9](https://github.com/rattatarr/rattatarr/issues/9)) ([a857111](https://github.com/rattatarr/rattatarr/commit/a857111ca42ba9c6e662291e6b1fc8d92b41c123))
* rate individual seasons ([#7](https://github.com/rattatarr/rattatarr/issues/7)) ([80eccfe](https://github.com/rattatarr/rattatarr/commit/80eccfed64604abbe8c3b48c7966907623bb61e2))
* sonarr integration & arr check connection ([#10](https://github.com/rattatarr/rattatarr/issues/10)) ([f21c61e](https://github.com/rattatarr/rattatarr/commit/f21c61e63b97e2102ef5df9f88d9d3237a84cccc))
* visualize watch activity, filter & deduplicate event for same session ([#8](https://github.com/rattatarr/rattatarr/issues/8)) ([5e6b41e](https://github.com/rattatarr/rattatarr/commit/5e6b41ee52b4ab3d3cba115dc1914217d8bbd950))


### Bug Fixes

* caddy config for ws ([c0b3cb5](https://github.com/rattatarr/rattatarr/commit/c0b3cb52cdd2ef4754f062e316efee7ebbe5a987))
* settings ollama section & handle error in ai chat ([e237fa1](https://github.com/rattatarr/rattatarr/commit/e237fa18d58935a7f7d4340c27acfca6d7d2e2b4))

## 1.0.0 (2026-04-18)


### Features

* ai recommandations ([#2](https://github.com/rattatarr/rattatarr/issues/2)) ([a1f9539](https://github.com/rattatarr/rattatarr/commit/a1f9539b26bea64986c77da38d5a9e119ab31a78))
* export profile ratings as csv ([ae9e166](https://github.com/rattatarr/rattatarr/commit/ae9e166861fee01ddf8a981a558323bc2976ff75))
* jellyfin activity track & dashboard new data ([#1](https://github.com/rattatarr/rattatarr/issues/1)) ([e78592e](https://github.com/rattatarr/rattatarr/commit/e78592ec15373746c26f6e47c413d7196068b904))
* job tracking for background tasks & websockets notifications ([#3](https://github.com/rattatarr/rattatarr/issues/3)) ([49a52f6](https://github.com/rattatarr/rattatarr/commit/49a52f6a1684e5b6df31bf5100f4c1217afaeed2))


### Bug Fixes

* redirect to settings if setup is not done ([a7134af](https://github.com/rattatarr/rattatarr/commit/a7134af012a683248d5d6fb3a6a5e0ed31296cbf))
