import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { api } from "../api.js";
import { formatDateTime, formatMoney } from "../format.js";
import FlightPass from "../components/FlightPass.jsx";
import { ErrorNotice, SuccessNotice } from "../components/Notice.jsx";

function unwrap(response) {
  return response?.data ?? response;
}

function formatLabel(value) {
  if (typeof value !== "string" || !value) return "—";
  return value.charAt(0) + value.slice(1).toLowerCase();
}

export default function BookingDetailPage() {
  const { id } = useParams();
  const location = useLocation();

  const [booking, setBooking] = useState(null);
  const [error, setError] = useState(null);
  const [confirming, setConfirming] = useState(false);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState(
    location.state?.created ? "Booking confirmed. Have a good flight." : "",
  );

  useEffect(() => {
    let ignore = false;

    async function loadBooking() {
      try {
        setError(null);

        const response = await api.booking(id);
        const data = unwrap(response);

        if (!data || data.id == null) {
          throw new Error(
            "Booking details were missing from the API response.",
          );
        }

        if (!ignore) {
          setBooking(data);
        }
      } catch (err) {
        if (!ignore) {
          setError(err);
        }
      }
    }

    loadBooking();

    return () => {
      ignore = true;
    };
  }, [id]);

  const cancel = async () => {
    setBusy(true);
    setError(null);

    try {
      const response = await api.cancelBooking(id);
      const data = unwrap(response);

      if (!data || data.id == null) {
        throw new Error(
          "Updated booking details were missing from the API response.",
        );
      }

      setBooking(data);
      setMessage("Booking cancelled. Your seats were released.");
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
      setConfirming(false);
    }
  };

  if (!booking) {
    return error ? (
      <ErrorNotice error={error} />
    ) : (
      <p className="muted">Loading booking…</p>
    );
  }

  const status = booking.status ?? booking.bookingStatus ?? "";
  const normalizedStatus =
    typeof status === "string" ? status.toUpperCase() : "";

  const confirmed = normalizedStatus === "CONFIRMED";

  const passengers = Array.isArray(booking.passengers)
    ? booking.passengers
    : [];

  const flight = booking.flight ?? booking.flightDetails ?? null;

  return (
    <>
      <p>
        <Link to="/bookings">Back to my bookings</Link>
      </p>

      <h1>
        Booking #{booking.id}{" "}
        <span
          className={`badge ${
            normalizedStatus ? normalizedStatus.toLowerCase() : "unknown"
          }`}
        >
          {status ? formatLabel(status) : "Status unavailable"}
        </span>
      </h1>

      {message && <SuccessNotice>{message}</SuccessNotice>}
      <ErrorNotice error={error} />

      {flight ? (
        <FlightPass
          flight={flight}
          showFare={false}
          muted={!confirmed}
          stub={
            <>
              <span className="muted">Total paid</span>
              <span className="price">
                {booking.totalAmount != null
                  ? formatMoney(booking.totalAmount)
                  : "Fare unavailable"}
              </span>
            </>
          }
        />
      ) : (
        <section className="panel">
          <p>Flight details are unavailable for this booking.</p>
        </section>
      )}

      <section className="panel">
        <h2>Passengers</h2>

        <table className="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Age</th>
              <th>Gender</th>
            </tr>
          </thead>

          <tbody>
            {passengers.map((passenger, index) => (
              <tr key={passenger.id ?? index}>
                <td>{passenger.name ?? "—"}</td>
                <td>{passenger.age ?? "—"}</td>
                <td>{formatLabel(passenger.gender)}</td>
              </tr>
            ))}

            {passengers.length === 0 && (
              <tr>
                <td colSpan={3}>No passenger details available.</td>
              </tr>
            )}
          </tbody>
        </table>

        <p className="muted">
          Booked on{" "}
          {booking.createdAt ? formatDateTime(booking.createdAt) : "—"}
        </p>
      </section>

      {confirmed && (
        <section className="panel">
          <h2>Need to change plans?</h2>

          {!confirming ? (
            <button
              type="button"
              className="btn danger"
              onClick={() => setConfirming(true)}
            >
              Cancel booking
            </button>
          ) : (
            <div className="form-actions">
              <span>
                Cancel this booking and release {passengers.length} seat(s)?
              </span>

              <button
                type="button"
                className="btn danger solid"
                onClick={cancel}
                disabled={busy}
              >
                {busy ? "Cancelling…" : "Yes, cancel booking"}
              </button>

              <button
                type="button"
                className="btn ghost"
                onClick={() => setConfirming(false)}
                disabled={busy}
              >
                Keep booking
              </button>
            </div>
          )}
        </section>
      )}
    </>
  );
}