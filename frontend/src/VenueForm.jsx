import { useState } from 'react';
import { ApiError, venueApi } from './api';

const EMPTY = { name: '', address: '', city: '', capacity: '' };

export default function VenueForm({ venue, onSaved, onCancel }) {
    const [values, setValues] = useState(() =>
        venue
            ? { name: venue.name, address: venue.address, city: venue.city, capacity: String(venue.capacity) }
            : EMPTY,
    );
    const [fieldErrors, setFieldErrors] = useState({});
    const [formError, setFormError] = useState(null);
    const [saving, setSaving] = useState(false);

    function handleChange(event) {
        const { name, value } = event.target;
        setValues((prev) => ({ ...prev, [name]: value }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setSaving(true);
        setFieldErrors({});
        setFormError(null);

        const payload = { ...values, capacity: Number(values.capacity) };

        try {
            const saved = venue
                ? await venueApi.update(venue.id, payload)
                : await venueApi.create(payload);
            onSaved(saved);
        } catch (err) {
            if (err instanceof ApiError && err.status === 400) {
                setFieldErrors(err.fieldErrors);
            } else {
                setFormError(err.message);
            }
        } finally {
            setSaving(false);
        }
    }

    return (
        <form className="card" onSubmit={handleSubmit} noValidate>
            <h2>{venue ? `Edit "${venue.name}"` : 'New venue'}</h2>

            {formError && <p className="error">{formError}</p>}

            <Field label="Name" name="name" value={values.name} error={fieldErrors.name} onChange={handleChange} />
            <Field label="Address" name="address" value={values.address} error={fieldErrors.address} onChange={handleChange} />
            <Field label="City" name="city" value={values.city} error={fieldErrors.city} onChange={handleChange} />
            <Field label="Capacity" name="capacity" type="number" value={values.capacity}
                   error={fieldErrors.capacity} onChange={handleChange} />

            <div className="actions">
                <button type="submit" disabled={saving}>
                    {saving ? 'Saving…' : venue ? 'Save changes' : 'Create venue'}
                </button>
                <button type="button" onClick={onCancel} disabled={saving}>
                    Cancel
                </button>
            </div>
        </form>
    );
}

function Field({ label, name, type = 'text', value, error, onChange }) {
    return (
        <label className="field">
            <span>{label}</span>
            <input name={name} type={type} value={value} onChange={onChange} aria-invalid={Boolean(error)} />
            {error && <small className="field-error">{error}</small>}
        </label>
    );
}