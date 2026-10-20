import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../../api.js";
import { formatDateTime, formatMoney, toDateTimeInput } from "../../format.js";
import Pagination from "../../components/Pagination.jsx";
import { ErrorNotice, SuccessNotice } from "../../components/Notice.jsx";

const empty = {
  flightNumber: "",
  originCode: "",
  destinationCode: "",
  departureTime: "",
  arrivalTime: "",
  price: "",
  totalSeats: "",
};

const emptyPage = {
  content: [],
  page: 0,
  size: 8,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
};

export default function AdminFlightsPage() {
  const [airports, setAirports] = useState([]);
  const [pageIndex, setPageIndex] = useState(0);
  const [page, setPage] = useState(emptyPage);
  const [form, setForm] = useState(empty);
  const [editingId, setEditingId] = useState(null);
  const [confirmId, setConfirmId] = useState(null);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState("");

  const loadAirports = useCallback(async () => {
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

  const load = useCallback(async () => {
    try {
      const response = await api.upcomingFlights({
        page: pageIndex,
        size: 8,
        sort: "departureTime,asc",
      });

      const pageData = response?.data;

      if (!pageData || !Array.isArray(pageData.content)) {
        console.error("Unexpected flights response:", response);
        throw new Error("Invalid flights response from server.");
      }

      setPage(pageData);
    } catch (err) {
      console.error("Failed to load flights:", err);
      setPage(emptyPage);
      setError(err);
    }
  }, [pageIndex]);

  useEffect(() => {
    loadAirports();
  }, [loadAirports]);

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

  const toPayload = () => ({
    ...form,
    flightNumber: form.flightNumber.trim().toUpperCase(),
    originCode: form.originCode,
    destinationCode: form.destinationCode,
    departureTime: form.departureTime.replace("T", " "),
    arrivalTime: form.arrivalTime.replace("T", " "),
    price: Number(form.price),
    totalSeats: Number(form.totalSeats),
  });

  const submit = async (e) => {
    e.preventDefault();
    setError(null);
    setMessage("");

    if (form.originCode === form.destinationCode) {
      setError(new Error("Origin and destination must be different."));
      return;
    }

    if (new Date(form.arrivalTime) <= new Date(form.departureTime)) {
      setError(new Error("Arrival must be after departure."));
      return;
    }

    try {
      const payload = toPayload();

      if (editingId !== null) {
        await api.updateFlight(editingId, payload);
        setMessage(`Flight ${payload.flightNumber} updated.`);
      } else {
        await api.createFlight(payload);
        setMessage(`Flight ${payload.flightNumber} added.`);
      }

      reset();

      if (pageIndex !== 0) {
        setPageIndex(0);
      } else {
        await load();
      }
    } catch (err) {
      setError(err);
    }
  };

  const edit = (flight) => {
    setEditingId(flight.id);
    setForm({
      flightNumber: flight.flightNumber ?? "",
      originCode: flight.origin?.code ?? "",
      destinationCode: flight.destination?.code ?? "",
      departureTime: toDateTimeInput(flight.departureTime),
      arrivalTime: toDateTimeInput(flight.arrivalTime),
      price: String(flight.price ?? ""),
      totalSeats: String(flight.totalSeats ?? ""),
    });

    setMessage("");
    setError(null);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const deactivate = async (flight) => {
    setError(null);
    setMessage("");

    try {
      await api.deactivateFlight(flight.id);
      setMessage(
        `Flight ${flight.flightNumber} deactivated. Existing bookings are kept.`,
      );
      await load();
    } catch (err) {
      setError(err);
    } finally {
      setConfirmId(null);
    }
  };

  const options = airports.map((airport) => (
    <option key={airport.id} value={airport.code}>
      {airport.city} ({airport.code})
    </option>
  ));

  return (
    <>
      <h1>Flights</h1>

      <SuccessNotice>{message}</SuccessNotice>
      <ErrorNotice error={error} />

      <form className="panel form" onSubmit={submit}>
        <h2>{editingId !== null ? "Edit flight" : "Add a flight"}</h2>

        <div className="grid-3">
          <label>
            Flight number
            <input
              required
              maxLength={10}
              placeholder="AI501"
              value={form.flightNumber}
              onChange={set("flightNumber")}
            />
          </label>

          <label>
            From
            <select
              required
              value={form.originCode}
              onChange={set("originCode")}
            >
              <option value="" disabled>
                Select airport
              </option>
              {options}
            </select>
          </label>

          <label>
            To
            <select
              required
              value={form.destinationCode}
              onChange={set("destinationCode")}
            >
              <option value="" disabled>
                Select airport
              </option>
              {options}
            </select>
          </label>

          <label>
            Departure
            <input
              type="datetime-local"
              required
              value={form.departureTime}
              onChange={set("departureTime")}
            />
          </label>

          <label>
            Arrival
            <input
              type="datetime-local"
              required
              value={form.arrivalTime}
              onChange={set("arrivalTime")}
            />
          </label>

          <label>
            Price per seat (INR)
            <input
              type="number"
              min="1"
              step="0.01"
              required
              value={form.price}
              onChange={set("price")}
            />
          </label>

          <label>
            Total seats
            <input
              type="number"
              min="1"
              required
              value={form.totalSeats}
              onChange={set("totalSeats")}
            />
          </label>
        </div>

        <div className="form-actions">
          <button className="btn primary" type="submit">
            {editingId !== null ? "Save changes" : "Add flight"}
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
              <th>Flight</th>
              <th>Route</th>
              <th>Departure</th>
              <th>Price</th>
              <th>Seats free</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            {(page?.content ?? []).map((flight) => (
              <tr key={flight.id}>
                <td>
                  <strong>{flight.flightNumber}</strong>
                </td>

                <td>
                  {flight.origin?.code ?? "—"} to{" "}
                  {flight.destination?.code ?? "—"}
                </td>

                <td>{formatDateTime(flight.departureTime)}</td>
                <td>{formatMoney(flight.price)}</td>

                <td>
                  {flight.availableSeats} / {flight.totalSeats}
                </td>

                <td className="actions">
                  <Link
                    className="btn ghost"
                    to={`/admin/flights/${flight.id}/manifest`}
                  >
                    Passengers
                  </Link>

                  <button
                    type="button"
                    className="btn ghost"
                    onClick={() => edit(flight)}
                  >
                    Edit
                  </button>

                  {confirmId === flight.id ? (
                    <>
                      <button
                        type="button"
                        className="btn danger solid"
                        onClick={() => deactivate(flight)}
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
                      onClick={() => setConfirmId(flight.id)}
                    >
                      Delete
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {page.content.length === 0 && (
          <p className="empty">No upcoming flights. Add one above.</p>
        )}
      </div>

      <Pagination
        page={page}
        onChange={(nextPage) => {
          const value = Number(nextPage);

          if (
            Number.isInteger(value) &&
            value >= 0 &&
            value < page.totalPages
          ) {
            setPageIndex(value);
          }
        }}
      />
    </>
  );
}