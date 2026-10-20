// Thin wrapper around fetch for the Spring Boot API (base path /api/v1).

const BASE = '/api/v1';
export const SESSION_KEY = 'skyway.session';

let onUnauthorized = () => { };
export function setUnauthorizedHandler(fn) {
  onUnauthorized = fn;
}

export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

function getToken() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY))?.token ?? null;
  } catch {
    return null;
  }
}

async function request(path, { method = 'GET', body, params } = {}) {
  const url = new URL(BASE + path, window.location.origin);
  Object.entries(params ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') url.searchParams.set(key, value);
  });

  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(url, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Check that the backend is running on port 8080.');
  }

  if (response.status === 204) return null;

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    // An expired or invalid token: drop the session so the user can sign in again.
    if (response.status === 401 && token) onUnauthorized();
    throw new ApiError(response.status, data?.message || `Request failed (${response.status})`);
  }
  return data;
}

export const api = {
  // auth
  register: (body) => request('/auth/register', { method: 'POST', body }),
  login: (body) => request('/auth/login', { method: 'POST', body }),

  // airports
  airports: () => request('/airports'),
  createAirport: (body) => request('/airports', { method: 'POST', body }),
  updateAirport: (id, body) => request(`/airports/${id}`, { method: 'PUT', body }),
  deactivateAirport: (id) => request(`/airports/${id}`, { method: 'DELETE' }),

  // flights
  searchFlights: (params) => request('/flights/search', { params }),
  upcomingFlights: (params) => request('/flights', { params }),
  flight: (id) => request(`/flights/${id}`),
  createFlight: (body) => request('/flights', { method: 'POST', body }),
  updateFlight: (id, body) => request(`/flights/${id}`, { method: 'PUT', body }),
  deactivateFlight: (id) => request(`/flights/${id}`, { method: 'DELETE' }),
  flightPassengers: (id) => request(`/flights/${id}/passengers`),
  flightOccupancy: (id) => request(`/flights/${id}/occupancy`),

  // bookings
  createBooking: (body) => request('/bookings', { method: 'POST', body }),
  myBookings: (params) => request('/bookings', { params }),
  booking: (id) => request(`/bookings/${id}`),
  cancelBooking: (id) => request(`/bookings/${id}/cancel`, { method: 'POST' }),
};
