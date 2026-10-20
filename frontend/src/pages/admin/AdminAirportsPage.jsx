import { useCallback, useEffect, useState } from "react";
import { api } from "../../api.js";
import { ErrorNotice, SuccessNotice } from "../../components/Notice.jsx";

const empty = {
  code: "",
  name: "",
  city: "",
};

export default function AdminAirportsPage() {
  const [airports, setAirports] = useState([]);
  const [form, setForm] = useState(empty);
  const [editingId, setEditingId] = useState(null);
  const [confirmId, setConfirmId] = useState(null);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState("");

  const load = useCallback(async () => {
    try {
      const response = await api.airports();
      const list = response?.data;

      if (!Array.isArray(list)) {
        console.error("Unexpected airports response:", response);
        throw new Error("Invalid airports response from server.");
      }

      setAirports(list);
    } catch (err) {
      console.error("Failed to load airports:", err);
      setAirports([]);
      setError(err);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const set = (field) => (e) => {
    setForm((previous) => ({
      ...previous,
      [field]: e.target.value,
    }));
  };

  const reset = () => {
    setForm(empty);
    setEditingId(null);
  };

  const submit = async (e) => {
    e.preventDefault();
    setError(null);
    setMessage("");

    const payload = {
      code: form.code.trim().toUpperCase(),
      name: form.name.trim(),
      city: form.city.trim(),
    };

    try {
      if (editingId !== null) {
        await api.updateAirport(editingId, payload);
        setMessage(`Airport ${payload.code} updated.`);
      } else {
        await api.createAirport(payload);
        setMessage(`Airport ${payload.code} added.`);
      }

      reset();
      await load();
    } catch (err) {
      setError(err);
    }
  };

  const edit = (airport) => {
    setEditingId(airport.id);
    setForm({
      code: airport.code ?? "",
      name: airport.name ?? "",
      city: airport.city ?? "",
    });
    setMessage("");
    setError(null);
  };

  const deactivate = async (airport) => {
    setError(null);
    setMessage("");

    try {
      await api.deactivateAirport(airport.id);
      setMessage(`Airport ${airport.code} deactivated.`);
      await load();
    } catch (err) {
      setError(err);
    } finally {
      setConfirmId(null);
    }
  };

  return (
    <>
      <h1>Airports</h1>

      <SuccessNotice>{message}</SuccessNotice>
      <ErrorNotice error={error} />

      <form className="panel form inline-form" onSubmit={submit}>
        <h2>{editingId !== null ? "Edit airport" : "Add an airport"}</h2>

        <div className="grid-3">
          <label>
            Code
            <input
              required
              maxLength={3}
              placeholder="BLR"
              value={form.code}
              onChange={set("code")}
            />
          </label>

          <label>
            Name
            <input required value={form.name} onChange={set("name")} />
          </label>

          <label>
            City
            <input required value={form.city} onChange={set("city")} />
          </label>
        </div>

        <div className="form-actions">
          <button className="btn primary" type="submit">
            {editingId !== null ? "Save changes" : "Add airport"}
          </button>

          {editingId !== null && (
            <button type="button" className="btn ghost" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>City</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            {airports.map((airport) => (
              <tr key={airport.id}>
                <td>
                  <strong>{airport.code}</strong>
                </td>

                <td>{airport.name}</td>
                <td>{airport.city}</td>

                <td className="actions">
                  <button
                    type="button"
                    className="btn ghost"
                    onClick={() => edit(airport)}
                  >
                    Edit
                  </button>

                  {confirmId === airport.id ? (
                    <>
                      <button
                        type="button"
                        className="btn danger solid"
                        onClick={() => deactivate(airport)}
                      >
                        Confirm Delete
                      </button>

                      <button
                        type="button"
                        className="btn ghost"
                        onClick={() => setConfirmId(null)}
                      >
                        Keep
                      </button>
                    </>
                  ) : (
                    <button
                      type="button"
                      className="btn ghost danger"
                      onClick={() => setConfirmId(airport.id)}
                    >
                      Delete
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {airports.length === 0 && (
          <p className="empty">No active airports. Add the first one above.</p>
        )}
      </div>
    </>
  );
}