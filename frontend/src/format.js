const money = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
});

export const formatMoney = (value) => money.format(Number(value));

// "2026-10-17T06:30:00" -> Date in local time
const toDate = (iso) => new Date(iso);

export const formatTime = (iso) =>
  toDate(iso).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: false });

export const formatDate = (iso) =>
  toDate(iso).toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });

export const formatDateTime = (iso) => `${formatDate(iso)}, ${formatTime(iso)}`;

export function formatDuration(fromIso, toIso) {
  const minutes = Math.round((toDate(toIso) - toDate(fromIso)) / 60000);
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return h ? `${h}h ${m ? `${m}m` : ''}`.trim() : `${m}m`;
}

// Value for <input type="date"> / <input type="datetime-local">
const pad = (n) => String(n).padStart(2, '0');
export const toDateInput = (date) =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
export const toDateTimeInput = (iso) => (iso ? iso.slice(0, 16) : '');

export const tomorrow = () => {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return toDateInput(d);
};
