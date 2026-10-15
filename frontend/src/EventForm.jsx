import { useEffect, useState } from 'react'
import { api } from './api'

const pad = (n) => String(n).padStart(2, '0')
const today = () => {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** "2026-11-01" + "20:00" in the browser's local time zone -> Date */
const toDate = (date, time) => new Date(`${date}T${time}`)

/** Same rule as TimeSlot.overlaps in Java: half-open [start, end) */
const overlaps = (aStart, aEnd, bStart, bEnd) => aStart < bEnd && bStart < aEnd

export function EventForm({ room, onCreate }) {
  const [form, setForm] = useState({
    title: '', description: '', date: today(), start: '19:00', end: '21:00',
    capacity: room.capacity, price: '20.00',
  })
  const [busy, setBusy] = useState([])
  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  // Load the room's busy slots for the chosen day (local midnight to midnight)
  useEffect(() => {
    let ignore = false
    const dayStart = toDate(form.date, '00:00')
    const dayEnd = new Date(dayStart)
    dayEnd.setDate(dayEnd.getDate() + 1)
    api.rooms
      .busy(room.id, dayStart.toISOString(), dayEnd.toISOString())
      .then((slots) => { if (!ignore) setBusy(slots) })
      .catch(() => { if (!ignore) setBusy([]) })
    return () => { ignore = true }
  }, [room.id, form.date])

  const start = toDate(form.date, form.start)
  const end = toDate(form.date, form.end)
  const validRange = end > start
  const clash = busy.find((s) => overlaps(start, end, new Date(s.startsAt), new Date(s.endsAt)))

  // position on a 24h bar, in % of the day
  const dayStart = toDate(form.date, '00:00').getTime()
  const pct = (d) => Math.min(100, Math.max(0, ((new Date(d).getTime() - dayStart) / 864e5) * 100))
  const bar = (from, to) => ({ left: `${pct(from)}%`, width: `${Math.max(0.5, pct(to) - pct(from))}%` })

  async function handleSubmit(e) {
    e.preventDefault()
    const ok = await onCreate({
      roomId: room.id,
      title: form.title,
      description: form.description,
      startsAt: start.toISOString(), // local time -> UTC instant, e.g. 2026-11-01T17:00:00.000Z
      endsAt: end.toISOString(),
      capacity: Number(form.capacity),
      price: form.price,
    })
    if (ok) {
      setForm({ ...form, title: '', description: '' })
      const s = new Date(start)
      s.setHours(0, 0, 0, 0)
      const next = new Date(s)
      next.setDate(next.getDate() + 1)
      setBusy(await api.rooms.busy(room.id, s.toISOString(), next.toISOString()))
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <input placeholder="Title" value={form.title} onChange={set('title')} />
      <textarea placeholder="Description (optional)" rows="2" value={form.description} onChange={set('description')} />
      <label>Date <input type="date" value={form.date} onChange={set('date')} /></label>
      <div className="row">
        <label>From <input type="time" value={form.start} onChange={set('start')} /></label>
        <label>To <input type="time" value={form.end} onChange={set('end')} /></label>
      </div>

      <div>
        <div className="timeline" title="Orange = booked, blue = your pick">
          {busy.map((s) => <div key={s.eventId} className="slot" style={bar(s.startsAt, s.endsAt)} title={s.title} />)}
          {validRange && <div className="pick" style={bar(start, end)} />}
        </div>
        <div className="hours"><span>00</span><span>06</span><span>12</span><span>18</span><span>24</span></div>
      </div>

      <div>
        {!validRange && <span className="badge busy">End must be after start</span>}
        {validRange && clash && (
          <span className="badge busy">
            Overlaps “{clash.title}” {new Date(clash.startsAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}–
            {new Date(clash.endsAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
          </span>
        )}
        {validRange && !clash && <span className="badge free">Room is free</span>}
      </div>

      <div className="row">
        <label>Seats <input type="number" value={form.capacity} onChange={set('capacity')} /></label>
        <label>Price <input type="number" step="0.01" value={form.price} onChange={set('price')} /></label>
      </div>
      <button type="submit">Create event</button>
      <p className="muted" style={{ margin: 0 }}>
        The badge is only a hint: the server re-checks on save (409 if someone took the slot meanwhile).
      </p>
    </form>
  )
}
