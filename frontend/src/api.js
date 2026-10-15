export class ApiError extends Error {
  constructor(status, problem) {
    super(problem?.detail ?? `Request failed with status ${status}`)
    this.status = status
    this.fieldErrors = problem?.errors ?? {} // from GlobalExceptionHandler on 400
  }
}

async function request(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    ...options,
    headers: options.body ? { 'Content-Type': 'application/json' } : {},
  })
  if (response.status === 204) return null
  const body = await response.json().catch(() => null)
  if (!response.ok) throw new ApiError(response.status, body)
  return body
}

const json = (method, body) => ({ method, body: JSON.stringify(body) })
const query = (params) =>
  new URLSearchParams(Object.entries(params).filter(([, v]) => v !== undefined && v !== '')).toString()

export const api = {
  venues: {
    list: () => request(`/venues?${query({ size: 100, sort: 'name' })}`),
    create: (venue) => request('/venues', json('POST', venue)),
    remove: (id) => request(`/venues/${id}`, { method: 'DELETE' }),
  },
  rooms: {
    list: (venueId) => request(`/venues/${venueId}/rooms`),
    create: (venueId, room) => request(`/venues/${venueId}/rooms`, json('POST', room)),
    remove: (venueId, roomId) => request(`/venues/${venueId}/rooms/${roomId}`, { method: 'DELETE' }),
    busy: (roomId, from, to) => request(`/rooms/${roomId}/busy?${query({ from, to })}`),
  },
  events: {
    list: (city) => request(`/events?${query({ city, size: 50 })}`),
    create: (event) => request('/events', json('POST', event)),
    cancel: (id) => request(`/events/${id}/cancel`, { method: 'POST' }),
  },
}
