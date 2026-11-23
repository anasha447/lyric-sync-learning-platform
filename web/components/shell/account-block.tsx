/**
 * Bottom of the sidebar, and on Profile for phones (DESIGN.md → Components → Account block).
 * For now everyone is a guest: the display name and level arrive with onboarding (FE-6.1),
 * and "Sign up to save words" with the auth pages (FE-5.6).
 */
export function AccountBlock() {
  return (
    <div className="flex flex-col gap-1">
      <p className="font-display text-title text-ink">Guest</p>
      <p className="text-label text-ink-muted">Listening without an account</p>
    </div>
  );
}
