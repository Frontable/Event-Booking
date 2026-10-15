import { useEffect, useState } from 'react'
import { api } from './api'
import { EventForm } from './EventForm'
import { ErrorBox } from './ErrorBox'

export default function App() {
  const [venues, setVenues] = useState([])
  const [venueId, setVenueId] = useState(null)
  const [rooms, setRooms] = useState([])
  const [roomId, setRoomId] = useState(null)
  const [events, setEvents] = useState([])
  const [city, setCity] = useState('')
  const [error, setError] = useState(null)

  const venue = venues.find((v) => v.id === venueId)
  const room = rooms.find((r) => r.id === roomId)

  // run an API call, show its error if it fails; returns true on success
  async function run(action) {
    setError(null)
    try {
      await action()
      return true
    } catch (e) {
      setError(e)
      return false
    }
  }

  const loadVenues = () => run(async () => setVenues((await api.venues.list()).content))
  const loadRooms = (id) => run(async () => setRooms(id ? await api.rooms.list(id) : []))
  const loadEvents = () => run(async () => setEvents((await api.events.list(city)).content))

  // Load venues and events once, when the page opens
  useEffect(() => {
    api.venues.list().then((page) => setVenues(page.content)).catch(setError)
    api.events.list('').then((page) => setEvents(page.content)).catch(setError)
  }, [])

  // Reload rooms whenever a different venue is selected
  useEffect(() => {
    if (!venueId) return
    let ignore = false
    api.rooms.list(venueId)
      .then((list) => { if (!ignore) setRooms(list) })
      .catch((e) => { if (!ignore) setError(e) })
    return () => { ignore = true }
  }, [venueId])

  function selectVenue(id) {
    setVenueId(id)
    setRoomId(null)
    setRooms([])
  }

  return (
    <main>
      <h1>Event Booking: API tester</h1>
      {error && <ErrorBox error={error} onClose={() => setError(null)} />}

      <div className="grid">
        <section className="card">
          <h2>Venues</h2>
          <ul className="list">
            {venues.map((v) => (
              <li key={v.id} className={v.id === venueId ? 'selected' : ''} onClick={() => selectVenue(v.id)}>
                <span>{v.name} <span className="muted">· {v.city}</span></span>
                <span className="muted">{v.capacity}</span>
              </li>
            ))}
            {venues.length === 0 && <li className="muted">No venues yet</li>}
          </ul>
          <SimpleForm
            fields={[['name', 'Name'], ['address', 'Address'], ['city', 'City'], ['capacity', 'Capacity', 'number']]}
            submitLabel="Add venue"
            onSubmit={(v) => run(async () => { await api.venues.create(v); await loadVenues() })}
          />
          {venue && (
            <button className="secondary" style={{ marginTop: '.5rem' }}
              onClick={() => run(async () => { await api.venues.remove(venue.id); selectVenue(null); await loadVenues() })}>
              Delete “{venue.name}”
            </button>
          )}
        </section>

        <section className="card">
          <h2>Rooms {venue ? `in ${venue.name}` : ''}</h2>
          {!venue && <p className="muted">Select a venue.</p>}
          {venue && (
            <>
              <ul className="list">
                {rooms.map((r) => (
                  <li key={r.id} className={r.id === roomId ? 'selected' : ''} onClick={() => setRoomId(r.id)}>
                    <span>{r.name}</span>
                    <span className="muted">{r.capacity}</span>
                  </li>
                ))}
                {rooms.length === 0 && <li className="muted">No rooms yet</li>}
              </ul>
              <SimpleForm
                fields={[['name', 'Room name'], ['capacity', 'Capacity', 'number']]}
                submitLabel="Add room"
                onSubmit={(r) => run(async () => { await api.rooms.create(venue.id, r); await loadRooms(venue.id) })}
              />
              {room && (
                <button className="secondary" style={{ marginTop: '.5rem' }}
                  onClick={() => run(async () => { await api.rooms.remove(venue.id, room.id); setRoomId(null); await loadRooms(venue.id) })}>
                  Delete “{room.name}”
                </button>
              )}
            </>
          )}
        </section>

        <section className="card">
          <h2>New event {room ? `in ${room.name}` : ''}</h2>
          {!room && <p className="muted">Select a venue and a room.</p>}
          {room && (
            <EventForm
              key={room.id}
              room={room}
              onCreate={(event) => run(async () => { await api.events.create(event); await loadEvents() })}
            />
          )}
        </section>
      </div>

      <section className="card" style={{ marginTop: '1rem' }}>
        <div className="row" style={{ alignItems: 'center', marginBottom: '.75rem' }}>
          <h2 style={{ margin: 0 }}>Upcoming events</h2>
          <input placeholder="Filter by city" value={city} onChange={(e) => setCity(e.target.value)} />
          <button className="secondary" onClick={loadEvents}>Search</button>
        </div>
        <table>
          <thead>
            <tr><th>When</th><th>Title</th><th>Where</th><th>Seats</th><th>Price</th><th></th></tr>
          </thead>
          <tbody>
            {events.map((e) => (
              <tr key={e.id}>
                <td>{formatRange(e.startsAt, e.endsAt)}</td>
                <td>{e.title}</td>
                <td>{e.roomName}, {e.venueName} <span className="muted">· {e.city}</span></td>
                <td>{e.capacity}</td>
                <td>{e.price} лв</td>
                <td><button className="secondary" onClick={() => run(async () => { await api.events.cancel(e.id); await loadEvents() })}>Cancel</button></td>
              </tr>
            ))}
            {events.length === 0 && <tr><td colSpan="6" className="muted">No upcoming events</td></tr>}
          </tbody>
        </table>
      </section>
    </main>
  )
}

/** A tiny form: fields = [[name, label, type?], ...]. Numbers are sent as numbers. */
function SimpleForm({ fields, submitLabel, onSubmit }) {
  const empty = Object.fromEntries(fields.map(([name]) => [name, '']))
  const [values, setValues] = useState(empty)

  async function handleSubmit(e) {
    e.preventDefault() // stop the browser's own page reload on submit
    const body = Object.fromEntries(
      fields.map(([name, , type]) => [name, type === 'number' ? Number(values[name]) : values[name]]),
    )
    if (await onSubmit(body)) setValues(empty)
  }

  return (
    <form onSubmit={handleSubmit}>
      {fields.map(([name, label, type = 'text']) => (
        <input key={name} type={type} placeholder={label} value={values[name]}
          onChange={(e) => setValues({ ...values, [name]: e.target.value })} />
      ))}
      <button type="submit">{submitLabel}</button>
    </form>
  )
}

function formatRange(startIso, endIso) {
  const start = new Date(startIso)
  const end = new Date(endIso)
  const day = start.toLocaleDateString(undefined, { weekday: 'short', day: 'numeric', month: 'short' })
  const time = (d) => d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })
  return `${day} ${time(start)}–${time(end)}`
}
