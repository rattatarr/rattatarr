/**
 * Returns two-letter uppercase initials from a name, or '?' if the name is empty.
 * Examples: 'John Doe' → 'JD', 'Madonna' → 'M', null → '?'
 */
export function getInitials(name: string | null | undefined): string {
  if (!name) return '?'
  const parts = name.split(' ')
  if (parts.length >= 2) {
    return `${parts[0]?.[0] || ''}${parts[1]?.[0] || ''}`.toUpperCase()
  }
  return (name[0] || '?').toUpperCase()
}
