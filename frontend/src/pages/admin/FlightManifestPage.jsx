import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../../api.js";
import { ErrorNotice } from "../../components/Notice.jsx";

export default function FlightManifestPage() {
  const { id } = useParams();

  const [occupancy, setOccupancy] = useState(null);
  const [passengers, setPassengers] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function loadManifest() {
      setLoading(true);
      setError(null);

      try {
        const [occResponse, passengerResponse] = await Promise.all([
          api.flightOccupancy(id),
          api.flightPassengers(id),
        ]);

        // The backend may wrap results inside { success, data }.
        const occ = occResponse?.data ?? occResponse;
        const passengerData = passengerResponse?.data ?? passengerResponse;

        // Passenger data may be an array or a paginated response.
        let passengerList = [];

        if (Array.isArray(passengerData)) {
          passengerList = passengerData;
        } else if (Array.isArray(passengerData?.content)) {
          passengerList = passengerData.content;
        } else if (Array.isArray(passengerData?.passengers)) {
          passengerList = passengerData.passengers;
        }

        if (!cancelled) {
          setOccupancy(occ);
          setPassengers(passengerList);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadManifest();

    return () => {
      cancelled = true;
    };
  }, [id]);

  if (loading) {
    return <p className="muted">Loading flight manifest…</p>;
  }

  if (error) {
    return <ErrorNotice error={error} />;
  }

  if (!occupancy) {
    return (
      <p className="empty">Flight occupancy information is unavailable.</p>
    );
  }

  const percentage = Math.max(
    0,
    Math.min(100, Number(occupancy.occupancyPercentage) || 0),
  );

  return (
    <>
      <p>
        <Link to="/admin/flights">Back to flights</Link>
      </p>

      <h1>Flight {occupancy.flightNumber}</h1>

      <section className="panel">
        <h2>Occupancy</h2>

        <div
          className="meter"
          role="progressbar"
          aria-valuemin={0}
          aria-valuemax={100}
          aria-valuenow={percentage}
          aria-label="Seats booked"
        >
          <span style={{ width: `${percentage}%` }} />
        </div>

        <p>
          <strong>{occupancy.bookedSeats}</strong> of {occupancy.totalSeats}{" "}
          seats booked ({percentage}%), {occupancy.availableSeats} still free.
        </p>
      </section>

      <section className="panel">
        <h2>Passengers</h2>

        {passengers.length === 0 ? (
          <p className="empty">No confirmed passengers on this flight yet.</p>
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Age</th>
                  <th>Gender</th>
                  <th>Booking</th>
                  <th>Booked by</th>
                </tr>
              </thead>

              <tbody>
                {passengers.map((p, index) => (
                  <tr key={p.passengerId ?? p.bookingId ?? index}>
                    <td>{p.name ?? "—"}</td>
                    <td>{p.age ?? "—"}</td>
                    <td>
                      {p.gender
                        ? p.gender.charAt(0).toUpperCase() +
                          p.gender.slice(1).toLowerCase()
                        : "—"}
                    </td>
                    <td>{p.bookingId != null ? `#${p.bookingId}` : "—"}</td>
                    <td>{p.bookedBy ?? "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}