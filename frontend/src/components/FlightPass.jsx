import {
  formatDate,
  formatDuration,
  formatMoney,
  formatTime,
} from "../format.js";

// A flight drawn as a boarding pass: route on the left, price and action on the stub.
export default function FlightPass({
  flight,
  stub,
  muted = false,
  showFare = true,
}) {
  // Handle bookings whose flight details are missing.
  if (!flight || typeof flight !== "object") {
    return (
      <article className={`pass${muted ? " is-muted" : ""}`}>
        <div className="pass-main">
          <strong>Flight details unavailable</strong>
          <p className="muted">
            Flight information could not be loaded for this booking.
          </p>
        </div>
        <div className="pass-stub">{stub}</div>
      </article>
    );
  }

  const origin = flight.origin ?? {};
  const destination = flight.destination ?? {};

  const departureTime = flight.departureTime;
  const arrivalTime = flight.arrivalTime;

  const availableSeats = Number(flight.availableSeats);
  const hasSeatCount =
    flight.availableSeats !== null &&
    flight.availableSeats !== undefined &&
    Number.isFinite(availableSeats);

  const soldOut = hasSeatCount && availableSeats <= 0;

  const safeFormatDate = (value) =>
    value ? formatDate(value) : "Date unavailable";

  const safeFormatTime = (value) => (value ? formatTime(value) : "--:--");

  const duration =
    departureTime && arrivalTime
      ? formatDuration(departureTime, arrivalTime)
      : "--";

  return (
    <article className={`pass${muted ? " is-muted" : ""}`}>
      <div className="pass-main">
        <div className="pass-meta">
          <strong>{flight.flightNumber ?? "Flight"}</strong>
          <span className="muted">{safeFormatDate(departureTime)}</span>
        </div>

        <div className="route">
          <div className="stop">
            <span className="time">{safeFormatTime(departureTime)}</span>
            <span className="code">{origin.code ?? "---"}</span>
            <span className="city">{origin.city ?? "Unknown city"}</span>
          </div>

          <div className="leg" aria-hidden="true">
            <span className="leg-line" />
            <span className="leg-time">{duration}</span>
          </div>

          <div className="stop end">
            <span className="time">{safeFormatTime(arrivalTime)}</span>
            <span className="code">{destination.code ?? "---"}</span>
            <span className="city">{destination.city ?? "Unknown city"}</span>
          </div>
        </div>
      </div>

      <div className="pass-stub">
        {showFare && (
          <>
            <span className="price">
              {flight.price != null
                ? formatMoney(flight.price)
                : "Fare unavailable"}
            </span>

            <span
              className={`seats${
                soldOut
                  ? " none"
                  : hasSeatCount && availableSeats <= 10
                    ? " low"
                    : ""
              }`}
            >
              {soldOut
                ? "Sold out"
                : hasSeatCount
                  ? `${availableSeats} seats left`
                  : "Seat availability unknown"}
            </span>
          </>
        )}

        {stub}
      </div>
    </article>
  );
}