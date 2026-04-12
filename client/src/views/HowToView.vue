<script setup lang="ts">
  import Accordion from 'primevue/accordion'
  import AccordionPanel from 'primevue/accordionpanel'
  import AccordionHeader from 'primevue/accordionheader'
  import AccordionContent from 'primevue/accordioncontent'
  import Button from 'primevue/button'
  import { Icon } from '@/utils/enums'

  function openExternal(url: string) {
    window.open(url, '_blank', 'noopener,noreferrer')
  }
</script>

<template>
  <div class="howto-view">
    <div class="howto-header">
      <h1 class="page-title">How To Get Your API Keys</h1>
      <p class="page-description">
        Step-by-step guides to obtain the required API keys and exports for integrating external
        services with Rattatarr.
      </p>
    </div>

    <div class="howto-container">
      <Accordion :value="['0']" multiple>
        <!-- TMDb API Key -->
        <AccordionPanel value="0">
          <AccordionHeader>
            <i :class="[Icon.KEY, 'header-icon']"></i>
            <span>TMDb API Key</span>
          </AccordionHeader>
          <AccordionContent>
            <div class="guide-content">
              <p class="guide-intro">
                The Movie Database (TMDb) provides metadata for movies and TV series. An API key is
                required for syncing media information.
              </p>

              <div class="steps">
                <div class="step">
                  <div class="step-number">1</div>
                  <div class="step-content">
                    <h3>Create a TMDb Account</h3>
                    <p>
                      Visit
                      <a
                        href="https://www.themoviedb.org/signup"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="external-link"
                      >
                        themoviedb.org/signup
                      </a>
                      and create a free account.
                    </p>
                    <Button
                      label="Go to TMDb Signup"
                      :icon="Icon.EXTERNAL_LINK"
                      size="small"
                      outlined
                      @click="openExternal('https://www.themoviedb.org/signup')"
                    />
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">2</div>
                  <div class="step-content">
                    <h3>Navigate to API Settings</h3>
                    <p>Once logged in, go to your account settings:</p>
                    <ul>
                      <li>Click your profile icon (top right)</li>
                      <li>Select "Settings"</li>
                      <li>Click "API" in the left sidebar</li>
                    </ul>
                    <Button
                      label="Go to API Settings"
                      :icon="Icon.EXTERNAL_LINK"
                      size="small"
                      outlined
                      @click="openExternal('https://www.themoviedb.org/settings/api')"
                    />
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">3</div>
                  <div class="step-content">
                    <h3>Request an API Key</h3>
                    <p>Click "Request an API Key" and select "Developer".</p>
                    <p class="note">
                      <i :class="Icon.INFO_CIRCLE"></i>
                      You'll need to accept the terms of use and provide basic information about
                      your application.
                    </p>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">4</div>
                  <div class="step-content">
                    <h3>Copy Your API Key</h3>
                    <p>
                      Once approved, copy the <strong>API Key (v3 auth)</strong> and paste it into
                      Rattatarr's TMDb API Key setting.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </AccordionContent>
        </AccordionPanel>

        <!-- Jellyfin API Key -->
        <AccordionPanel value="1">
          <AccordionHeader>
            <i :class="[Icon.KEY, 'header-icon']"></i>
            <span>Jellyfin API Key</span>
          </AccordionHeader>
          <AccordionContent>
            <div class="guide-content">
              <p class="guide-intro">
                Jellyfin is your self-hosted media server. You'll need an API key to sync your media
                library with Rattatarr.
              </p>

              <div class="steps">
                <div class="step">
                  <div class="step-number">1</div>
                  <div class="step-content">
                    <h3>Access Jellyfin Dashboard</h3>
                    <p>Log into your Jellyfin server as an administrator.</p>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">2</div>
                  <div class="step-content">
                    <h3>Navigate to API Keys</h3>
                    <ul>
                      <li>Click the menu icon (☰) in the top left</li>
                      <li>Select "Dashboard"</li>
                      <li>Go to "Advanced" → "API Keys"</li>
                    </ul>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">3</div>
                  <div class="step-content">
                    <h3>Create New API Key</h3>
                    <p>Click the "+" button to create a new API key.</p>
                    <ul>
                      <li>App Name: <code>Rattatarr</code> (or any name you prefer)</li>
                      <li>Click "OK" to generate the key</li>
                    </ul>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">4</div>
                  <div class="step-content">
                    <h3>Copy the API Key</h3>
                    <p>Copy the generated API key and paste it into Rattatarr's settings.</p>
                    <p class="note">
                      <i :class="Icon.INFO_CIRCLE"></i>
                      Also make sure to provide your Jellyfin Base URL (e.g.,
                      <code>http://localhost:8096</code>).
                    </p>
                  </div>
                </div>
              </div>

              <div class="additional-info">
                <h3>Finding Your Jellyfin URL</h3>
                <p>Your Jellyfin Base URL is the address you use to access Jellyfin:</p>
                <ul>
                  <li>
                    Local: <code>http://localhost:8096</code> or
                    <code>http://192.168.x.x:8096</code>
                  </li>
                  <li>Remote: <code>https://jellyfin.yourdomain.com</code></li>
                </ul>
              </div>
            </div>
          </AccordionContent>
        </AccordionPanel>

        <!-- Sonarr API Key -->
        <AccordionPanel value="2">
          <AccordionHeader>
            <i :class="[Icon.KEY, 'header-icon']"></i>
            <span>Sonarr API Key</span>
          </AccordionHeader>
          <AccordionContent>
            <div class="guide-content">
              <p class="guide-intro">
                Sonarr manages your TV series library. Connect it to Rattatarr for seamless series
                tracking.
              </p>

              <div class="steps">
                <div class="step">
                  <div class="step-number">1</div>
                  <div class="step-content">
                    <h3>Access Sonarr Web Interface</h3>
                    <p>Open your Sonarr instance in a web browser.</p>
                    <p class="note">
                      <i :class="Icon.INFO_CIRCLE"></i>
                      Default URL is usually <code>http://localhost:8989</code>
                    </p>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">2</div>
                  <div class="step-content">
                    <h3>Navigate to Settings</h3>
                    <ul>
                      <li>Click "Settings" in the left sidebar</li>
                      <li>Go to the "General" tab</li>
                      <li>Click "Show Advanced" at the top if not visible</li>
                    </ul>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">3</div>
                  <div class="step-content">
                    <h3>Locate API Key</h3>
                    <p>
                      Scroll down to the <strong>Security</strong> section. Your API key will be
                      displayed there.
                    </p>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">4</div>
                  <div class="step-content">
                    <h3>Copy to Rattatarr</h3>
                    <p>Copy the API key and paste it into Rattatarr's Sonarr settings.</p>
                    <p>
                      Also provide your Sonarr Base URL (e.g., <code>http://localhost:8989</code>).
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </AccordionContent>
        </AccordionPanel>

        <!-- Radarr API Key -->
        <AccordionPanel value="3">
          <AccordionHeader>
            <i :class="[Icon.KEY, 'header-icon']"></i>
            <span>Radarr API Key</span>
          </AccordionHeader>
          <AccordionContent>
            <div class="guide-content">
              <p class="guide-intro">
                Radarr manages your movie library. Connect it to Rattatarr for enhanced movie
                tracking.
              </p>

              <div class="steps">
                <div class="step">
                  <div class="step-number">1</div>
                  <div class="step-content">
                    <h3>Access Radarr Web Interface</h3>
                    <p>Open your Radarr instance in a web browser.</p>
                    <p class="note">
                      <i :class="Icon.INFO_CIRCLE"></i>
                      Default URL is usually <code>http://localhost:7878</code>
                    </p>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">2</div>
                  <div class="step-content">
                    <h3>Navigate to Settings</h3>
                    <ul>
                      <li>Click "Settings" in the left sidebar</li>
                      <li>Go to the "General" tab</li>
                      <li>Click "Show Advanced" at the top if not visible</li>
                    </ul>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">3</div>
                  <div class="step-content">
                    <h3>Locate API Key</h3>
                    <p>
                      Scroll down to the <strong>Security</strong> section. Your API key will be
                      displayed there.
                    </p>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">4</div>
                  <div class="step-content">
                    <h3>Copy to Rattatarr</h3>
                    <p>Copy the API key and paste it into Rattatarr's Radarr settings.</p>
                    <p>
                      Also provide your Radarr Base URL (e.g., <code>http://localhost:7878</code>).
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </AccordionContent>
        </AccordionPanel>

        <!-- IMDb CSV Export -->
        <AccordionPanel value="4">
          <AccordionHeader>
            <i :class="[Icon.FILE_EXPORT, 'header-icon']"></i>
            <span>IMDb Ratings Export</span>
          </AccordionHeader>
          <AccordionContent>
            <div class="guide-content">
              <p class="guide-intro">
                Import your existing IMDb ratings into Rattatarr by exporting them as a CSV file.
              </p>

              <div class="steps">
                <div class="step">
                  <div class="step-number">1</div>
                  <div class="step-content">
                    <h3>Log into IMDb</h3>
                    <p>
                      Visit
                      <a
                        href="https://www.imdb.com"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="external-link"
                      >
                        imdb.com
                      </a>
                      and log into your account.
                    </p>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">2</div>
                  <div class="step-content">
                    <h3>Navigate to Ratings Export</h3>
                    <p>
                      Go directly to
                      <a
                        href="https://www.imdb.com/exports/"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="external-link"
                      >
                        imdb.com/exports/
                      </a>
                    </p>
                    <Button
                      label="Go to IMDb Exports"
                      :icon="Icon.EXTERNAL_LINK"
                      size="small"
                      outlined
                      @click="openExternal('https://www.imdb.com/exports/')"
                    />
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">3</div>
                  <div class="step-content">
                    <h3>Export Your Ratings</h3>
                    <p>Find the "Your Ratings" section and click the "Export" button.</p>
                    <p class="note">
                      <i :class="Icon.INFO_CIRCLE"></i>
                      The export may take a few moments if you have many ratings.
                    </p>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">4</div>
                  <div class="step-content">
                    <h3>Download CSV File</h3>
                    <p>Your browser will download a file named <code>ratings.csv</code>.</p>
                  </div>
                </div>

                <div class="step">
                  <div class="step-number">5</div>
                  <div class="step-content">
                    <h3>Import to Rattatarr</h3>
                    <p>Go to Settings → External Synchronization in Rattatarr.</p>
                    <p>Upload the CSV file using the IMDb Rating Import section.</p>
                    <p class="note">
                      <i :class="Icon.INFO_CIRCLE"></i>
                      Make sure you have your TMDb API Key configured before importing.
                    </p>
                  </div>
                </div>
              </div>

              <div class="additional-info">
                <h3>What Gets Imported?</h3>
                <ul>
                  <li>Your ratings (1-10 scale, converted to 0.5-10.0 for Rattatarr)</li>
                  <li>Movies and TV series</li>
                  <li>Watch dates (if available)</li>
                </ul>
              </div>
            </div>
          </AccordionContent>
        </AccordionPanel>
      </Accordion>

      <!-- Quick Links Section -->
      <div class="quick-links">
        <h2>Quick Links</h2>
        <div class="links-grid">
          <a
            href="https://www.themoviedb.org/settings/api"
            target="_blank"
            rel="noopener noreferrer"
            class="link-card"
          >
            <i :class="Icon.EXTERNAL_LINK"></i>
            <span>TMDb API Settings</span>
          </a>
          <a
            href="https://jellyfin.org/docs/"
            target="_blank"
            rel="noopener noreferrer"
            class="link-card"
          >
            <i :class="Icon.BOOK"></i>
            <span>Jellyfin Documentation</span>
          </a>
          <a
            href="https://wiki.servarr.com/sonarr"
            target="_blank"
            rel="noopener noreferrer"
            class="link-card"
          >
            <i :class="Icon.BOOK"></i>
            <span>Sonarr Wiki</span>
          </a>
          <a
            href="https://wiki.servarr.com/radarr"
            target="_blank"
            rel="noopener noreferrer"
            class="link-card"
          >
            <i :class="Icon.BOOK"></i>
            <span>Radarr Wiki</span>
          </a>
          <a
            href="https://www.imdb.com/exports/"
            target="_blank"
            rel="noopener noreferrer"
            class="link-card"
          >
            <i :class="Icon.DOWNLOAD"></i>
            <span>IMDb Exports</span>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .howto-view {
    max-width: 1200px;
    margin: 0 auto;
  }

  .howto-header {
    margin-bottom: 2rem;
  }

  .page-title {
    margin: 0 0 0.5rem 0;
    color: var(--p-text-color);
    font-size: 2rem;
    font-weight: 600;
  }

  .page-description {
    color: var(--p-text-secondary-color);
    margin: 0;
    font-size: 1rem;
    line-height: 1.6;
  }

  .howto-container {
    display: flex;
    flex-direction: column;
    gap: 2rem;
  }

  /* Accordion Header Styling */
  .header-icon {
    margin-right: 0.75rem;
    color: var(--p-primary-color);
  }

  /* Guide Content */
  .guide-content {
    padding: 0.5rem 0;
  }

  .guide-intro {
    font-size: 1rem;
    color: var(--p-text-secondary-color);
    line-height: 1.6;
    margin: 0 0 2rem 0;
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--p-surface-border);
  }

  /* Steps */
  .steps {
    display: flex;
    flex-direction: column;
    gap: 2rem;
  }

  .step {
    display: flex;
    gap: 1.5rem;
    align-items: flex-start;
  }

  .step-number {
    flex-shrink: 0;
    width: 2.5rem;
    height: 2.5rem;
    border-radius: 50%;
    background: var(--p-primary-color);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 1.125rem;
  }

  .step-content {
    flex: 1;
  }

  .step-content h3 {
    margin: 0 0 0.75rem 0;
    color: var(--p-text-color);
    font-size: 1.125rem;
    font-weight: 600;
  }

  .step-content p {
    margin: 0 0 0.75rem 0;
    color: var(--p-text-color);
    line-height: 1.6;
  }

  .step-content ul {
    margin: 0.75rem 0;
    padding-left: 1.5rem;
    color: var(--p-text-color);
  }

  .step-content li {
    margin: 0.5rem 0;
    line-height: 1.6;
  }

  .step-content code {
    background: var(--p-surface-700);
    padding: 0.125rem 0.375rem;
    border-radius: 0.25rem;
    font-family: 'Courier New', monospace;
    font-size: 0.875rem;
    color: var(--p-primary-color);
  }

  .external-link {
    color: var(--p-primary-color);
    text-decoration: none;
    font-weight: 500;
  }

  .external-link:hover {
    text-decoration: underline;
  }

  .note {
    display: flex;
    align-items: flex-start;
    gap: 0.5rem;
    padding: 0.75rem;
    background: var(--p-surface-700);
    border-radius: var(--p-border-radius);
    border-left: 3px solid var(--p-primary-color);
    color: var(--p-text-secondary-color);
    font-size: 0.875rem;
    line-height: 1.5;
  }

  .note i {
    color: var(--p-primary-color);
    margin-top: 0.125rem;
    flex-shrink: 0;
  }

  /* Additional Info Section */
  .additional-info {
    margin-top: 2rem;
    padding-top: 2rem;
    border-top: 1px solid var(--p-surface-border);
  }

  .additional-info h3 {
    margin: 0 0 1rem 0;
    color: var(--p-text-color);
    font-size: 1.125rem;
    font-weight: 600;
  }

  .additional-info p {
    margin: 0 0 0.75rem 0;
    color: var(--p-text-color);
    line-height: 1.6;
  }

  .additional-info ul {
    margin: 0.75rem 0;
    padding-left: 1.5rem;
    color: var(--p-text-color);
  }

  .additional-info li {
    margin: 0.5rem 0;
    line-height: 1.6;
  }

  .additional-info code {
    background: var(--p-surface-700);
    padding: 0.125rem 0.375rem;
    border-radius: 0.25rem;
    font-family: 'Courier New', monospace;
    font-size: 0.875rem;
    color: var(--p-primary-color);
  }

  /* Quick Links */
  .quick-links {
    margin-top: 2rem;
    padding: 2rem;
    background: var(--p-surface-800);
    border-radius: var(--p-border-radius);
  }

  .quick-links h2 {
    margin: 0 0 1.5rem 0;
    color: var(--p-text-color);
    font-size: 1.5rem;
    font-weight: 600;
  }

  .links-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
  }

  .link-card {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1rem;
    background: var(--p-surface-700);
    border: 1px solid var(--p-surface-border);
    border-radius: var(--p-border-radius);
    color: var(--p-text-color);
    text-decoration: none;
    transition: all 0.2s ease;
  }

  .link-card:hover {
    background: var(--p-surface-700);
    border-color: var(--p-primary-color);
    transform: translateY(-2px);
  }

  .link-card i {
    color: var(--p-primary-color);
    font-size: 1.25rem;
  }

  .link-card span {
    font-weight: 500;
    font-size: 0.9375rem;
  }

  /* Responsive */
  @media (max-width: 768px) {
    .page-title {
      font-size: 1.5rem;
    }

    .step {
      flex-direction: column;
      gap: 1rem;
    }

    .step-number {
      width: 2rem;
      height: 2rem;
      font-size: 1rem;
    }

    .links-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
