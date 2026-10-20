export function ErrorNotice({ error }) {
  if (!error) return null;
  return (
    <p className="notice error" role="alert">
      {error.message ?? String(error)}
    </p>
  );
}

export function SuccessNotice({ children }) {
  if (!children) return null;
  return (
    <p className="notice success" role="status">
      {children}
    </p>
  );
}
