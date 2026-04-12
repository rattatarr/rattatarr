import { describe, it, expect } from 'vitest'
import {
  profileKeys,
  movieKeys,
  seriesKeys,
  genreKeys,
  tmdbKeys,
  settingsKeys,
  jellyfinKeys,
  logKeys,
  invalidateAll,
} from '@/queries'
import type { Pageable, ProfilesFilters, MovieFilters, SearchFilters } from '@/types'

describe('profileKeys', () => {
  it('should generate base key', () => {
    expect(profileKeys.all).toEqual(['profiles'])
  })

  it('should generate list keys', () => {
    expect(profileKeys.lists()).toEqual(['profiles', 'list'])
  })

  it('should generate specific list key with pageable and filters', () => {
    const pageable: Pageable = { page: 0, size: 10 }
    const filters: ProfilesFilters = { name: '' }

    const key = profileKeys.list(pageable, filters)

    expect(key).toEqual(['profiles', 'list', { pageable, filters }])
  })

  it('should generate detail keys', () => {
    expect(profileKeys.details()).toEqual(['profiles', 'detail'])
    expect(profileKeys.detail('123')).toEqual(['profiles', 'detail', '123'])
  })
})

describe('movieKeys', () => {
  it('should generate base key', () => {
    expect(movieKeys.all).toEqual(['movies'])
  })

  it('should generate list keys with filters', () => {
    const pageable: Pageable = { page: 0, size: 24 }
    const filters: MovieFilters = {
      title: 'Inception',
      genres: ['Action', 'Sci-Fi'],
      profileId: '1',
    }

    const key = movieKeys.list(pageable, filters)

    expect(key).toEqual(['movies', 'list', { pageable, filters }])
  })

  it('should generate broken movie keys', () => {
    expect(movieKeys.broken()).toEqual(['movies', 'broken'])

    const pageable: Pageable = { page: 0, size: 10 }
    expect(movieKeys.brokenList(pageable)).toEqual(['movies', 'broken', { pageable }])
  })
})

describe('seriesKeys', () => {
  it('should generate series list keys', () => {
    const pageable: Pageable = { page: 1, size: 24 }
    const filters = {
      title: 'Game of Thrones',
      releasedAfter: 2010,
    }

    const key = seriesKeys.list(pageable, filters)

    expect(key).toEqual(['series', 'list', { pageable, filters }])
  })
})

describe('genreKeys', () => {
  it('should generate genre keys', () => {
    expect(genreKeys.all).toEqual(['genres'])
    expect(genreKeys.lists()).toEqual(['genres', 'list'])
  })
})

describe('tmdbKeys', () => {
  it('should generate TMDB movie search keys', () => {
    const filters: SearchFilters = { query: 'Matrix' }

    expect(tmdbKeys.movieSearch(filters)).toEqual(['tmdb', 'movies', 'search', filters])
  })

  it('should generate TMDB movie detail keys', () => {
    expect(tmdbKeys.movieDetail('550')).toEqual(['tmdb', 'movies', 'detail', '550'])
  })

  it('should generate TMDB series keys', () => {
    const filters: SearchFilters = { query: 'Breaking Bad' }

    expect(tmdbKeys.seriesSearch(filters)).toEqual(['tmdb', 'series', 'search', filters])
    expect(tmdbKeys.seriesDetail('1396')).toEqual(['tmdb', 'series', 'detail', '1396'])
  })

  it('should generate general TMDB search keys', () => {
    const filters: SearchFilters = { query: 'Nolan' }

    expect(tmdbKeys.search(filters)).toEqual(['tmdb', 'search', filters])
  })
})

describe('settingsKeys', () => {
  it('should generate settings keys', () => {
    expect(settingsKeys.all).toEqual(['settings'])
    expect(settingsKeys.list()).toEqual(['settings', 'list'])
    expect(settingsKeys.detail('apiKey')).toEqual(['settings', 'detail', 'apiKey'])
  })
})

describe('jellyfinKeys', () => {
  it('should generate jellyfin keys', () => {
    expect(jellyfinKeys.all).toEqual(['jellyfin'])
    expect(jellyfinKeys.test()).toEqual(['jellyfin', 'test'])
  })
})

describe('logKeys', () => {
  const defaultPageable = { page: 0, size: 20 }

  describe('historical logs (REST)', () => {
    it('should generate log keys without filters', () => {
      expect(logKeys.all).toEqual(['logs'])
      expect(logKeys.lists()).toEqual(['logs', 'list'])
      expect(logKeys.list(defaultPageable)).toEqual([
        'logs',
        'list',
        { pageable: defaultPageable, filters: undefined },
      ])
    })

    it('should generate log keys with level filter', () => {
      expect(logKeys.list(defaultPageable, { level: 'ERROR' })).toEqual([
        'logs',
        'list',
        { pageable: defaultPageable, filters: { level: 'ERROR' } },
      ])
    })

    it('should generate log keys with logger filter', () => {
      expect(logKeys.list(defaultPageable, { logger: 'com.rattatarr' })).toEqual([
        'logs',
        'list',
        { pageable: defaultPageable, filters: { logger: 'com.rattatarr' } },
      ])
    })

    it('should generate log keys with all filters', () => {
      expect(
        logKeys.list(defaultPageable, {
          level: 'WARN',
          logger: 'com.rattatarr',
          startDate: '2024-02-14T10:00:00',
          endDate: '2024-02-14T15:00:00',
        }),
      ).toEqual([
        'logs',
        'list',
        {
          pageable: defaultPageable,
          filters: {
            level: 'WARN',
            logger: 'com.rattatarr',
            startDate: '2024-02-14T10:00:00',
            endDate: '2024-02-14T15:00:00',
          },
        },
      ])
    })
  })
})

describe('invalidateAll', () => {
  it('should provide base keys for all domains', () => {
    expect(invalidateAll.profiles).toEqual(['profiles'])
    expect(invalidateAll.movies).toEqual(['movies'])
    expect(invalidateAll.series).toEqual(['series'])
    expect(invalidateAll.genres).toEqual(['genres'])
    expect(invalidateAll.tmdb).toEqual(['tmdb'])
    expect(invalidateAll.settings).toEqual(['settings'])
    expect(invalidateAll.jellyfin).toEqual(['jellyfin'])
    expect(invalidateAll.logs).toEqual(['logs'])
  })
})

describe('query key stability', () => {
  it('should generate identical keys for identical inputs', () => {
    const pageable: Pageable = { page: 0, size: 10 }
    const filters: MovieFilters = { title: 'Test' }

    const key1 = movieKeys.list(pageable, filters)
    const key2 = movieKeys.list(pageable, filters)

    expect(key1).toEqual(key2)
  })

  it('should generate different keys for different inputs', () => {
    const key1 = movieKeys.list({ page: 0, size: 10 }, { title: 'A' })
    const key2 = movieKeys.list({ page: 1, size: 10 }, { title: 'A' })

    expect(key1).not.toEqual(key2)
  })
})
