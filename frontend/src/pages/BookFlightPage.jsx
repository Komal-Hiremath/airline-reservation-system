import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api.js";
import { formatMoney } from "../format.js";
import FlightPass from "../components/FlightPass.jsx";
import { ErrorNotice } from "../components/Notice.jsx";

const blank = () => ({ name: "", age: "", gender: "MALE" });

function unwrap(response) {
  return response?.data ?? response;
}

export default function BookFlightPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [flight, setFlight] = useState(null);
  const [passengers, setPassengers] = useState([blank()]);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;

    async function loadFlight() {
      setLoading(true);
      setError(null);

      try {
        const response = await api.flight(id);
        const flightData = unwrap(response);

        if (!flightData || flightData.id == null) {
          throw new Error("Flight details were not returned by the server.");
        }

        if (!ignore) {
          setFlight(flightData);
        }
      } catch (err) {
        if (!ignore) setError(err);
      } finally {
        if (!ignore) setLoading(false);
      }
    }

    loadFlight();

    return () => {
      ignore = true;
    };
  }, [id]);

  if (loading) {
    return <p className="muted">Loading flight details…</p>;
  }

  if (error && !flight) {
    return <ErrorNotice error={error} />;
  }

  if (!flight) {
    return <p className="empty">Flight details are unavailable.</p>;
  }

  const price = Number(flight.price);
  const seats = Number(flight.availableSeats);

  const validPrice = Number.isFinite(price) && price >= 0;
  const validSeats =
    flight.availableSeats !== null &&
    flight.availableSeats !== undefined &&
    Number.isInteger(seats) &&
    seats >= 0;

  const maxPassengers = validSeats ? Math.min(9, seats) : 0;
  const total = validPrice ? price * passengers.length : null;

  const change = (index, field, value) => {
    setPassengers((current) =>
      current.map((passenger, i) =>
        i === index ? { ...passenger, [field]: value } : passenger,
      ),
    );
  };

  const submit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!validPrice || !validSeats) {
      setError(new Error("The flight price or seat availability is invalid."));
      return;
    }

    if (passengers.length > maxPassengers || passengers.length === 0) {
      setError(new Error("The passenger count exceeds available seats."));
      return;
    }

    if (
      passengers.some(
        (p) =>
          !p.name.trim() ||
          p.age === "" ||
          !Number.isInteger(Number(p.age)) ||
          Number(p.age) < 0 ||
          Number(p.age) > 120,
      )
    ) {
      setError(new Error("Enter a valid name and age for every passenger."));
      return;
    }

    setBusy(true);

    try {
      const response = await api.createBooking({
        flightId: flight.id,
        passengers: passengers.map((p) => ({
          name: p.name.trim(),
          age: Number(p.age),
          gender: p.gender,
        })),
      });

      const booking = unwrap(response);

      if (!booking?.id) {
        throw new Error(
          "The booking was created, but no booking ID was returned.",
        );
      }

      navigate(`/bookings/${booking.id}`, {
        state: { created: true },
      });
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <h1>Book your seats</h1>

      <FlightPass flight={flight} />

      <form className="panel form" onSubmit={submit}>
        <h2>Passengers</h2>

        <ErrorNotice error={error} />

        {!validPrice && (
          <p className="notice error">
            The flight price is unavailable. Please reload or check the flight
            API.
          </p>
        )}

        {!validSeats && (
          <p className="notice error">
            Seat availability is unavailable. Booking is disabled until it can
            be verified.
          </p>
        )}

        {passengers.map((p, index) => (
          <fieldset className="passenger" key={index}>
            <legend>Passenger {index + 1}</legend>

            <label>
              Full name
              <input
                required
                value={p.name}
                onChange={(e) => change(index, "name", e.target.value)}
              />
            </label>

            <label>
              Age
              <input
                type="number"
                min="0"
                max="120"
                required
                value={p.age}
                onChange={(e) => change(index, "age", e.target.value)}
              />
            </label>

            <label>
              Gender
              <select
                value={p.gender}
                onChange={(e) => change(index, "gender", e.target.value)}
              >
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </label>

            {passengers.length > 1 && (
              <button
                type="button"
                className="btn ghost danger"
                onClick={() =>
                  setPassengers((current) =>
                    current.filter((_, i) => i !== index),
                  )
                }
              >
                Remove
              </button>
            )}
          </fieldset>
        ))}

        <div className="form-actions">
          <button
            type="button"
            className="btn ghost"
            disabled={!validSeats || passengers.length >= maxPassengers || busy}
            onClick={() => setPassengers((current) => [...current, blank()])}
          >
            Add passenger
          </button>

          <span className="muted">
            {passengers.length} of {validSeats ? maxPassengers : "—"} seats
          </span>
        </div>

        <div className="total">
          <span>Total</span>
          <strong>
            {total !== null ? formatMoney(total) : "Price unavailable"}
          </strong>
        </div>

        <button
          className="btn primary"
          type="submit"
          disabled={busy || !validPrice || !validSeats || maxPassengers === 0}
        >
          {busy ? "Booking…" : "Confirm booking"}
        </button>
      </form>
    </>
  );
}