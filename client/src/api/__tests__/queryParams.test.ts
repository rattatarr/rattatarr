import { describe, it, expect } from 'vitest'
import {
  buildMoviesQueryParams,
  buildSeriesQueryParams,
  buildGenresQueryParams,
  buildProfilesQueryParams,
  buildPageableQueryParams,
  buildSearchQueryParams,
  buildJobsQueryParams,
} from '../queryParams'
import type {
  Pageable,
  MovieFilters,
  SeriesFilters,
  GenresFilters,
  ProfilesFilters,
  SearchFilters,
} from '@/types'

describe('queryParams', () => {
  describe('buildMoviesQueryParams', () => {
    const defaultPageable: Pageable = {
      page: 0,
      size: 20,
      sort: ['rating,desc'],
    }

    it('builds query params with all pageable fields', () => {
      const filters: MovieFilters = { title: '' }
      const result = buildMoviesQueryParams(defaultPageable, filters)

      expect(result.page).toBe(0)
      expect(result.size).toBe(20)
      expect(result.sort).toEqual(['rating,desc'])
    })

    it('includes all filter fields when provided', () => {
      const filters: MovieFilters = {
        id: '123',
        title: 'Inception',
        releasedAfter: 2010,
        releasedBefore: 2020,
        genres: ['Action', 'Sci-Fi'],
        posterSize: 'w500',
        backdropSize: 'w1280',
        profileSize: 'h632',
        profileId: 'profile-1',
        ratingMin: 7,
        ratingMax: 10,
      }

      const result = buildMoviesQueryParams(defaultPageable, filters)

      expect(result.id).toBe('123')
      expect(result.title).toBe('Inception')
      expect(result.releasedAfter).toBe(2010)
      expect(result.releasedBefore).toBe(2020)
      expect(result.genres).toBe('Action,Sci-Fi') // Joined with comma
      expect(result.posterSize).toBe('w500')
      expect(result.backdropSize).toBe('w1280')
      expect(result.profileSize).toBe('h632')
      expect(result.profileId).toBe('profile-1')
      expect(result.ratingMin).toBe(7)
      expect(result.ratingMax).toBe(10)
    })

    it('excludes empty title strings', () => {
      const filters: MovieFilters = { title: '' }
      const result = buildMoviesQueryParams(defaultPageable, filters)

      expect(result.title).toBeUndefined()
    })

    it('excludes empty genres arrays', () => {
      const filters: MovieFilters = { title: '', genres: [] }
      const result = buildMoviesQueryParams(defaultPageable, filters)

      expect(result.genres).toBeUndefined()
    })

    it('joins multiple genres with commas', () => {
      const filters: MovieFilters = {
        title: '',
        genres: ['Action', 'Drama', 'Thriller'],
      }
      const result = buildMoviesQueryParams(defaultPageable, filters)

      expect(result.genres).toBe('Action,Drama,Thriller')
    })

    it('handles single genre', () => {
      const filters: MovieFilters = { title: '', genres: ['Comedy'] }
      const result = buildMoviesQueryParams(defaultPageable, filters)

      expect(result.genres).toBe('Comedy')
    })

    it('handles undefined optional fields', () => {
      const filters: MovieFilters = {
        title: '',
        id: undefined,
        releasedAfter: undefined,
        releasedBefore: undefined,
        genres: undefined,
      }
      const result = buildMoviesQueryParams(defaultPageable, filters)

      // Should not include undefined fields
      expect(result.id).toBeUndefined()
      expect(result.title).toBeUndefined()
      expect(result.releasedAfter).toBeUndefined()
      expect(result.releasedBefore).toBeUndefined()
      expect(result.genres).toBeUndefined()
    })
  })

  describe('buildSeriesQueryParams', () => {
    const defaultPageable: Pageable = {
      page: 0,
      size: 20,
      sort: ['rating,desc'],
    }

    it('includes all filter fields when provided', () => {
      const filters: SeriesFilters = {
        id: '456',
        title: 'Breaking Bad',
        releasedAfter: 2008,
        releasedBefore: 2013,
        genres: ['Drama', 'Crime'],
        withSeasons: true,
        withEpisodes: false,
        posterSize: 'w500',
        backdropSize: 'w1280',
        profileSize: 'h632',
        profileId: 'profile-2',
        ratingMin: 8,
        ratingMax: 10,
      }

      const result = buildSeriesQueryParams(defaultPageable, filters)

      expect(result.id).toBe('456')
      expect(result.title).toBe('Breaking Bad')
      expect(result.releasedAfter).toBe(2008)
      expect(result.releasedBefore).toBe(2013)
      expect(result.genres).toBe('Drama,Crime')
      expect(result.withSeasons).toBe(true)
      expect(result.withEpisodes).toBe(false)
      expect(result.posterSize).toBe('w500')
      expect(result.backdropSize).toBe('w1280')
      expect(result.profileSize).toBe('h632')
      expect(result.profileId).toBe('profile-2')
      expect(result.ratingMin).toBe(8)
      expect(result.ratingMax).toBe(10)
    })

    it('excludes empty title strings', () => {
      const filters: SeriesFilters = { title: '' }
      const result = buildSeriesQueryParams(defaultPageable, filters)

      expect(result.title).toBeUndefined()
    })

    it('excludes empty genres arrays', () => {
      const filters: SeriesFilters = { title: '', genres: [] }
      const result = buildSeriesQueryParams(defaultPageable, filters)

      expect(result.genres).toBeUndefined()
    })

    it('handles withSeasons and withEpisodes booleans', () => {
      const filters1: SeriesFilters = { title: '', withSeasons: true, withEpisodes: true }
      const result1 = buildSeriesQueryParams(defaultPageable, filters1)
      expect(result1.withSeasons).toBe(true)
      expect(result1.withEpisodes).toBe(true)

      const filters2: SeriesFilters = { title: '', withSeasons: false, withEpisodes: false }
      const result2 = buildSeriesQueryParams(defaultPageable, filters2)
      expect(result2.withSeasons).toBe(false)
      expect(result2.withEpisodes).toBe(false)
    })
  })

  describe('buildGenresQueryParams', () => {
    const defaultPageable: Pageable = {
      page: 0,
      size: 50,
      sort: ['name,asc'],
    }

    it('includes all fields when provided', () => {
      const filters: GenresFilters = { name: 'Action' }
      const result = buildGenresQueryParams(defaultPageable, filters)

      expect(result.page).toBe(0)
      expect(result.size).toBe(50)
      expect(result.sort).toEqual(['name,asc'])
      expect(result.name).toBe('Action')
    })

    it('excludes empty name strings', () => {
      const filters: GenresFilters = { name: '' }
      const result = buildGenresQueryParams(defaultPageable, filters)

      expect(result.name).toBeUndefined()
    })

    it('handles undefined name', () => {
      const filters: GenresFilters = { name: undefined as unknown as string }
      const result = buildGenresQueryParams(defaultPageable, filters)

      expect(result.name).toBeUndefined()
    })
  })

  describe('buildProfilesQueryParams', () => {
    const defaultPageable: Pageable = {
      page: 0,
      size: 10,
      sort: ['name,asc'],
    }

    it('includes all filter fields when provided', () => {
      const filters: ProfilesFilters = {
        name: 'John',
        createdAtAfter: '2024-01-01',
        createdAtBefore: '2024-12-31',
        updatedAtAfter: '2024-06-01',
        updatedAtBefore: '2024-06-30',
      }

      const result = buildProfilesQueryParams(defaultPageable, filters)

      expect(result.page).toBe(0)
      expect(result.size).toBe(10)
      expect(result.sort).toEqual(['name,asc'])
      expect(result.name).toBe('John')
      expect(result.createdAtAfter).toBe('2024-01-01')
      expect(result.createdAtBefore).toBe('2024-12-31')
      expect(result.updatedAtAfter).toBe('2024-06-01')
      expect(result.updatedAtBefore).toBe('2024-06-30')
    })

    it('handles undefined date filters', () => {
      const filters: ProfilesFilters = {
        name: '',
        createdAtAfter: undefined,
        createdAtBefore: undefined,
        updatedAtAfter: undefined,
        updatedAtBefore: undefined,
      }

      const result = buildProfilesQueryParams(defaultPageable, filters)

      expect(result.name).toBe('')
      expect(result.createdAtAfter).toBeUndefined()
      expect(result.createdAtBefore).toBeUndefined()
      expect(result.updatedAtAfter).toBeUndefined()
      expect(result.updatedAtBefore).toBeUndefined()
    })
  })

  describe('buildPageableQueryParams', () => {
    it('includes only pageable fields', () => {
      const pageable: Pageable = {
        page: 5,
        size: 100,
        sort: ['id,asc', 'name,desc'],
      }

      const result = buildPageableQueryParams(pageable)

      expect(result.page).toBe(5)
      expect(result.size).toBe(100)
      expect(result.sort).toEqual(['id,asc', 'name,desc'])
      // Should not have any other fields
      expect(Object.keys(result)).toHaveLength(3)
    })

    it('handles multiple sort parameters', () => {
      const pageable: Pageable = {
        page: 0,
        size: 20,
        sort: ['rating,desc', 'releaseDate,desc', 'title,asc'],
      }

      const result = buildPageableQueryParams(pageable)

      expect(result.sort).toEqual(['rating,desc', 'releaseDate,desc', 'title,asc'])
    })

    it('handles empty sort array', () => {
      const pageable: Pageable = {
        page: 0,
        size: 20,
        sort: [],
      }

      const result = buildPageableQueryParams(pageable)

      expect(result.sort).toEqual([])
    })
  })

  describe('buildSearchQueryParams', () => {
    it('includes all search filter fields', () => {
      const filters: SearchFilters = {
        query: 'Inception',
        posterSize: 'w500',
        page: 1,
      }

      const result = buildSearchQueryParams(filters)

      expect(result.query).toBe('Inception')
      expect(result.posterSize).toBe('w500')
      expect(result.page).toBe(1)
    })

    it('handles undefined optional fields', () => {
      const filters: SearchFilters = {
        query: 'Matrix',
        posterSize: undefined,
        page: undefined,
      }

      const result = buildSearchQueryParams(filters)

      expect(result.query).toBe('Matrix')
      expect(result.posterSize).toBeUndefined()
      expect(result.page).toBeUndefined()
    })

    it('handles page 0', () => {
      const filters: SearchFilters = {
        query: 'Avatar',
        page: 0,
      }

      const result = buildSearchQueryParams(filters)

      expect(result.page).toBe(0)
    })
  })

  describe('edge cases', () => {
    const defaultPageable: Pageable = {
      page: 0,
      size: 20,
      sort: [],
    }

    it('handles genres with special characters', () => {
      const filters: MovieFilters = {
        title: '',
        genres: ['Sci-Fi', 'Action & Adventure', 'Drama/Thriller'],
      }

      const result = buildMoviesQueryParams(defaultPageable, filters)

      expect(result.genres).toBe('Sci-Fi,Action & Adventure,Drama/Thriller')
    })

    it('handles very long genre lists', () => {
      const filters: MovieFilters = {
        title: '',
        genres: ['Genre1', 'Genre2', 'Genre3', 'Genre4', 'Genre5', 'Genre6'],
      }

      const result = buildMoviesQueryParams(defaultPageable, filters)

      expect(result.genres).toBe('Genre1,Genre2,Genre3,Genre4,Genre5,Genre6')
    })

    it('handles zero values for ratings', () => {
      const filters: MovieFilters = {
        title: '',
        ratingMin: 0,
        ratingMax: 0,
      }

      const result = buildMoviesQueryParams(defaultPageable, filters)

      // Zero is a valid rating
      expect(result.ratingMin).toBe(0)
      expect(result.ratingMax).toBe(0)
    })

    it('handles negative page numbers', () => {
      const pageable: Pageable = {
        page: -1,
        size: 20,
        sort: [],
      }

      const result = buildPageableQueryParams(pageable)

      // Should pass through as-is (backend will validate)
      expect(result.page).toBe(-1)
    })
  })

  describe('buildJobsQueryParams', () => {
    const pageable: Pageable = { page: 0, size: 20 }

    it('flattens pageable fields to top level', () => {
      const result = buildJobsQueryParams(pageable)

      expect(result.page).toBe(0)
      expect(result.size).toBe(20)
    })

    it('includes undefined filter values when no filters passed', () => {
      const result = buildJobsQueryParams(pageable)

      expect(result.type).toBeUndefined()
      expect(result.status).toBeUndefined()
      expect(result.startDate).toBeUndefined()
      expect(result.endDate).toBeUndefined()
    })

    it('passes type filter', () => {
      const result = buildJobsQueryParams(pageable, { type: 'JELLYFIN_SYNC' })

      expect(result.type).toBe('JELLYFIN_SYNC')
    })

    it('passes status filter', () => {
      const result = buildJobsQueryParams(pageable, { status: 'FAILED' })

      expect(result.status).toBe('FAILED')
    })

    it('passes date range filters', () => {
      const result = buildJobsQueryParams(pageable, {
        startDate: '2026-04-01T00:00:00',
        endDate: '2026-04-18T23:59:59',
      })

      expect(result.startDate).toBe('2026-04-01T00:00:00')
      expect(result.endDate).toBe('2026-04-18T23:59:59')
    })

    it('passes all filters together', () => {
      const result = buildJobsQueryParams(
        { page: 1, size: 50 },
        {
          type: 'CSV_IMPORT',
          status: 'COMPLETED',
          startDate: '2026-04-01T00:00:00',
          endDate: '2026-04-18T23:59:59',
        },
      )

      expect(result.page).toBe(1)
      expect(result.size).toBe(50)
      expect(result.type).toBe('CSV_IMPORT')
      expect(result.status).toBe('COMPLETED')
      expect(result.startDate).toBe('2026-04-01T00:00:00')
      expect(result.endDate).toBe('2026-04-18T23:59:59')
    })
  })
})
