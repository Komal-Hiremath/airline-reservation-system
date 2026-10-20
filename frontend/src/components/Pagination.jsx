export default function Pagination({ page, onChange }) {
  if (!page || typeof page.totalPages !== "number") {
    return null;
  }

  const currentPage = Number(page?.page ?? 0);
  const totalPages = Number(page?.totalPages ?? 0);

  if (
    !Number.isFinite(currentPage) ||
    !Number.isFinite(totalPages) ||
    totalPages <= 1
  ) {
    return null;
  }

  return (
    <div className="pagination">
      <button
        className="btn ghost"
        disabled={currentPage <= 0}
        onClick={() => onChange(currentPage - 1)}
      >
        Previous
      </button>

      <span>
        Page {currentPage + 1} of {totalPages}
      </span>

      <button
        className="btn ghost"
        disabled={currentPage >= totalPages - 1}
        onClick={() => onChange(currentPage + 1)}
      >
        Next
      </button>
    </div>
  );
}
