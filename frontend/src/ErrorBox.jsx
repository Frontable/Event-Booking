/** Shows an ApiError: the problem+json "detail", plus per-field messages on a 400. */
export function ErrorBox({ error, onClose }) {
  const fieldErrors = Object.entries(error.fieldErrors ?? {})
  return (
    <div className="error" style={{ marginBottom: '1rem' }}>
      <strong>{error.status ? `${error.status}: ` : ''}</strong>{error.message}
      {fieldErrors.length > 0 && (
        <ul>{fieldErrors.map(([field, msg]) => <li key={field}><b>{field}</b> {msg}</li>)}</ul>
      )}
      <button className="secondary" style={{ marginLeft: '.75rem', padding: '.1rem .5rem' }} onClick={onClose}>×</button>
    </div>
  )
}
