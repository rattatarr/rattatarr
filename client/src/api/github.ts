export interface GitHubRelease {
  tag_name: string
  html_url: string
  name: string
}

export async function getLatestRelease(): Promise<GitHubRelease> {
  const response = await fetch('https://api.github.com/repos/rattatarr/rattatarr/releases/latest', {
    headers: { Accept: 'application/vnd.github+json' },
  })
  if (!response.ok) {
    throw new Error(`GitHub API error: ${response.status}`)
  }
  return (await response.json()) as GitHubRelease
}
