<script setup lang="ts">
  import { useToast } from '@/composables/useToast'
  import { useCreateProfile, useRateMediaItem } from '@/queries'
  import { APIError } from '@/api/client'

  const toast = useToast()
  const createProfile = useCreateProfile()
  const rateMovie = useRateMediaItem()

  // Example 1: Simple success toast
  function showSuccess() {
    toast.success('Operation completed successfully!')
  }

  // Example 2: Success with description
  function showSuccessWithDescription() {
    toast.success('Profile created', 'Your new profile is ready to use')
  }

  // Example 3: Error toast with APIError
  async function triggerApiError() {
    try {
      // This will fail and trigger APIError
      throw new APIError({
        status: 404,
        statusText: 'Not Found',
        message: 'Movie not found',
        body: {
          message: 'The requested movie does not exist in our database',
          detail: 'Please check the movie ID and try again',
        },
        url: '/api/v1/movies/123',
      })
    } catch (error) {
      toast.error(error)
    }
  }

  // Example 4: Error with validation errors
  async function triggerValidationError() {
    try {
      throw new APIError({
        status: 400,
        statusText: 'Bad Request',
        message: 'Validation failed',
        body: {
          message: 'Invalid input',
          fieldErrors: {
            email: 'Must be a valid email address',
            password: 'Must be at least 8 characters',
            age: 'Must be 18 or older',
          },
        },
      })
    } catch (error) {
      toast.error(error)
    }
  }

  // Example 5: Using with TanStack Query mutation
  async function createNewProfile() {
    try {
      await createProfile.mutateAsync({ name: 'Test Profile' })
      toast.success('Profile created successfully!')
    } catch {
      // Error is already handled by global mutation error handler
      // But you can add custom logic here if needed
      console.log('Custom error handling')
    }
  }

  // Example 6: Promise toast (automatic loading → success/error)
  async function createProfileWithPromiseToast() {
    toast.promise(createProfile.mutateAsync({ name: 'Test Profile' }), {
      loading: 'Creating profile...',
      success: (data) => {
        return `Profile "${data.profile?.name}" created successfully!`
      },
      error: (err) => {
        if (err instanceof APIError) {
          return err.getUserMessage()
        }
        return 'Failed to create profile'
      },
    })
  }

  // Example 7: Loading toast with auto-dismiss
  async function saveWithLoadingToast() {
    const loadingToast = toast.loading('Saving changes...')

    try {
      await new Promise((resolve) => setTimeout(resolve, 2000))
      // Auto-dismiss loading toast when showing success
      toast.success('Changes saved successfully!', { dismissLoadingToast: loadingToast })
    } catch (error) {
      // Auto-dismiss loading toast when showing error
      toast.error(error, { dismissLoadingToast: loadingToast })
    }
  }

  // Example 8: Info and warning toasts
  function showInfo() {
    toast.info('New feature available', 'Check out our latest movie recommendations!')
  }

  function showWarning() {
    toast.warning('Storage almost full', 'Consider deleting some cached data')
  }

  // Example 9: Error with fallback message
  async function errorWithFallback() {
    try {
      throw new Error('Network error')
    } catch (error) {
      toast.error(error, 'Failed to connect to server')
    }
  }

  // Example 10: Rate movie with custom error handling
  async function handleRateMovie() {
    try {
      await rateMovie.mutateAsync({
        entityId: 'movie-123',
        ratingMediaType: 'MEDIA_ITEM',
        rating: 8.5,
        profileId: 'profile-1',
      })
      toast.success('Rating saved!', 'Your rating has been recorded')
    } catch (error) {
      if (error instanceof APIError) {
        if (error.isStatus(409)) {
          // Custom handling for conflict
          toast.warning('Rating already exists', 'Your previous rating has been updated')
        } else {
          // Use default error handling
          toast.error(error)
        }
      } else {
        toast.error(error)
      }
    }
  }
</script>

<template>
  <div class="toast-examples">
    <h1>Toast Examples</h1>

    <section>
      <h2>Basic Toasts</h2>
      <button @click="showSuccess">Show Success</button>
      <button @click="showSuccessWithDescription">Success with Description</button>
      <button @click="showInfo">Show Info</button>
      <button @click="showWarning">Show Warning</button>
    </section>

    <section>
      <h2>Error Handling</h2>
      <button @click="triggerApiError">Trigger API Error</button>
      <button @click="triggerValidationError">Trigger Validation Error</button>
      <button @click="errorWithFallback">Error with Fallback</button>
    </section>

    <section>
      <h2>With TanStack Query</h2>
      <button @click="createNewProfile" :disabled="createProfile.isPending.value">
        {{ createProfile.isPending.value ? 'Creating...' : 'Create Profile (Auto Toast)' }}
      </button>
      <button @click="createProfileWithPromiseToast">Create Profile (Promise Toast)</button>
    </section>

    <section>
      <h2>Loading Toasts</h2>
      <button @click="saveWithLoadingToast">Save with Loading Toast</button>
    </section>

    <section>
      <h2>Advanced Examples</h2>
      <button @click="handleRateMovie" :disabled="rateMovie.isPending.value">
        {{ rateMovie.isPending.value ? 'Rating...' : 'Rate Movie (Custom Handling)' }}
      </button>
    </section>
  </div>
</template>

<style scoped>
  .toast-examples {
    padding: 2rem;
    max-width: 800px;
    margin: 0 auto;
  }

  h1 {
    margin-bottom: 2rem;
  }

  section {
    margin-bottom: 2rem;
    padding: 1rem;
    border: 1px solid var(--color-border);
    border-radius: 8px;
  }

  h2 {
    margin-top: 0;
    margin-bottom: 1rem;
    font-size: 1.25rem;
  }

  button {
    margin: 0.5rem 0.5rem 0.5rem 0;
    padding: 0.5rem 1rem;
    background: var(--color-background-soft);
    border: 1px solid var(--color-border);
    border-radius: 4px;
    cursor: pointer;
  }

  button:hover:not(:disabled) {
    background: var(--color-background-mute);
  }

  button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
</style>
