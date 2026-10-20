import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api.js";
import { formatMoney } from "../format.js";
import FlightPass from "../components/FlightPass.jsx";
import Pagination from "../components/Pagination.jsx";
import { ErrorNotice } from "../components/Notice.jsx";

const emptyPage = {
  content: [],
  page: 0,
  size: 5,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
};

export default function MyBookingsPage() {
  const [pageIndex, setPageIndex] = useState(0);
  const [page, setPage] = useState(emptyPage);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;

    async function loadBookings() {
      setLoading(true);
      setError(null);

      try {
        const response = await api.myBookings({
          page: pageIndex,
          size: 5,
          sort: "createdAt,desc",
        });

        // Support the API wrapper: { success, data: {...} }
        const result = response?.data ?? response;

        // The backend returns a paginated response with content.
        const normalizedPage = Array.isArray(result?.content)
          ? result
          : Array.isArray(result)
            ? { ...emptyPage, content: result, totalElements: result.length }
            : emptyPage;

        if (!ignore) {
          setPage(normalizedPage);
        }
      } catch (err) {
        if (!ignore) {
          setError(err);
          setPage(emptyPage);
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    loadBookings();

    return () => {
      ignore = true;
    };
  }, [pageIndex]);

  const bookings = Array.isArray(page?.content) ? page.content : [];

  return (
    <>
      <h1>My bookings</h1>

      {error && <ErrorNotice error={error} />}

      {loading && <p className="muted">Loading your bookings…</p>}

      {!loading && !error && bookings.length === 0 && (
        <p className="empty">
          You have no bookings yet. <Link to="/">Find a flight</Link>
        </p>
      )}

      <div className="stack">
        {!loading &&
          bookings.map((booking) => {
            const passengers = Array.isArray(booking.passengers)
              ? booking.passengers
              : [];

            return (
              <FlightPass
                key={booking.id}
                flight={booking.flight}
                showFare={false}
                muted={booking.status === "CANCELLED"}
                stub={
                  <>
                    <span
                      className={`badge ${(
                        booking.status ?? "UNKNOWN"
                      ).toLowerCase()}`}
                    >
                      {booking.status === "CONFIRMED"
                        ? "Confirmed"
                        : booking.status === "CANCELLED"
                          ? "Cancelled"
                          : (booking.status ?? "Unknown")}
                    </span>

                    <span className="price">
                      {formatMoney(booking.totalAmount)}
                    </span>

                    <span className="muted">
                      {passengers.length}{" "}
                      {passengers.length === 1 ? "passenger" : "passengers"}
                    </span>

                    <Link className="btn ghost" to={`/bookings/${booking.id}`}>
                      View ticket
                    </Link>
                  </>
                }
              />
            );
          })}
      </div>

      {!loading && !error && bookings.length > 0 && (
        <Pagination page={page} onChange={setPageIndex} />
      )}
    </>
  );
}