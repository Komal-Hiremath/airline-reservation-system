import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api.js";
import { useAuth } from "../auth.jsx";
import { tomorrow } from "../format.js";
import FlightPass from "../components/FlightPass.jsx";
import Pagination from "../components/Pagination.jsx";
import { ErrorNotice } from "../components/Notice.jsx";

const SORTS = [
  { value: "departureTime,asc", label: "Earliest departure" },
  { value: "price,asc", label: "Lowest price" },
  { value: "price,desc", label: "Highest price" },
  { value: "arrivalTime,asc", label: "Earliest arrival" },
];

export default function SearchPage() {
  const { isAdmin } = useAuth();
  const [airports, setAirports] = useState([]);
  const [form, setForm] = useState({ from: "", to: "", date: tomorrow() });
  const [query, setQuery] = useState({
    mode: "upcoming",
    page: 0,
    sort: "departureTime,asc",
  });
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api
      .airports()
      .then((response) => {
        setAirports(Array.isArray(response?.data) ? response.data : []);
      })
      .catch((err) => {
        console.error("Failed to load airports:", err);
        setAirports([]);
        setError(err);
      });
  }, []);

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    setError(null);

    const paging = { page: query.page, size: 6, sort: query.sort };
    const request =
      query.mode === "search"
        ? api.searchFlights({
            from: query.from,
            to: query.to,
            date: query.date,
            ...paging,
          })
        : api.upcomingFlights(paging);
    request
      .then((response) => {
        if (ignore) return;

        const pageData = response?.data;

        if (pageData && Array.isArray(pageData.content)) {
          setResults(pageData);
        } else {
          console.error("Unexpected flights response:", response);
          setResults(null);
          setError(new Error("Invalid flights response from server."));
        }
      })
      .catch((e) => !ignore && setError(e))
      .finally(() => !ignore && setLoading(false));

    return () => {
      ignore = true;
    };
  }, [query]);

  const update = (field) => (e) =>
    setForm({ ...form, [field]: e.target.value });

  const submit = (e) => {
    e.preventDefault();
    if (form.from === form.to) {
      setError(new Error("Choose two different airports."));
      return;
    }
    setQuery({ mode: "search", ...form, page: 0, sort: query.sort });
  };

  const showAll = () =>
    setQuery({ mode: "upcoming", page: 0, sort: query.sort });

  return (
    <>
      <h1 className="hero-title">Where are you flying?</h1>

      <form className="panel search" onSubmit={submit}>
        <label>
          From
          <select value={form.from} onChange={update("from")} required>
            <option value="" disabled>
              Select airport
            </option>
            {airports.map((a) => (
              <option key={a.id} value={a.code}>
                {a.city} ({a.code})
              </option>
            ))}
          </select>
        </label>
        <label>
          To
          <select value={form.to} onChange={update("to")} required>
            <option value="" disabled>
              Select airport
            </option>
            {airports.map((a) => (
              <option key={a.id} value={a.code}>
                {a.city} ({a.code})
              </option>
            ))}
          </select>
        </label>
        <label>
          Date
          <input
            type="date"
            value={form.date}
            onChange={update("date")}
            required
          />
        </label>
        <button className="btn primary" type="submit">
          Search flights
        </button>
      </form>

      <div className="results-head">
        <h2>
          {query.mode === "search"
            ? `${query.from} to ${query.to}`
            : "Upcoming flights"}
        </h2>
        <div className="results-tools">
          {query.mode === "search" && (
            <button className="btn ghost" onClick={showAll}>
              Show all flights
            </button>
          )}
          <label className="inline">
            Sort by
            <select
              value={query.sort}
              onChange={(e) =>
                setQuery({ ...query, sort: e.target.value, page: 0 })
              }
            >
              {SORTS.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </label>
        </div>
      </div>

      <ErrorNotice error={error} />
      {loading && <p className="muted">Loading flights…</p>}

      {!loading && results && (results.content ?? []).length === 0 && (
        <p className="empty">
          No flights found
          {query.mode === "search"
            ? " for that route and date. Try another day."
            : " right now."}
        </p>
      )}

      <div className="stack">
        {(results?.content ?? []).map((flight) => (
          <FlightPass
            key={flight.id}
            flight={flight}
            stub={
              !isAdmin &&
              (flight.availableSeats > 0 ? (
                <Link className="btn primary" to={`/flights/${flight.id}/book`}>
                  Book
                </Link>
              ) : (
                <span className="btn disabled" aria-disabled="true">
                  Unavailable
                </span>
              ))
            }
          />
        ))}
      </div>

      <Pagination
        page={results}
        onChange={(p) => setQuery({ ...query, page: p })}
      />
    </>
  );
}
