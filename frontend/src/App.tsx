import { useState } from 'react'
import { AssetWorkspace } from './features/assets/AssetWorkspace'
import { api, clearSession, loadSession, saveSession, type Session } from './lib/api'

function LoginScreen({ onAuthenticated }: { onAuthenticated: (session: Session) => void }) {
  const [organizationId, setOrganizationId] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function submit(event: React.FormEvent) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const session = await api.login({ organizationId: organizationId.trim(), email: email.trim(), password })
      saveSession(session)
      onAuthenticated(session)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Không thể đăng nhập.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="login-layout">
      <section className="login-context">
        <a className="brand" href="#top" aria-label="DefenSME"><span className="brand-mark">D</span><span>DefenSME</span></a>
        <div className="login-copy"><p className="eyebrow">CYBERSECURITY OPERATIONS</p><h1>Biết rõ tài sản nào đang cần được bảo vệ.</h1><p>Kiểm kê hệ thống, xác định mức độ quan trọng và giữ dữ liệu của từng doanh nghiệp tách biệt.</p></div>
        <div className="trust-line"><span /> Phiên đăng nhập và quyền truy cập được kiểm soát bởi backend</div>
      </section>
      <section className="login-panel" aria-labelledby="login-title"><div className="login-card">
        <p className="eyebrow">ĐĂNG NHẬP HỆ THỐNG</p><h2 id="login-title">Chào mừng trở lại</h2><p className="muted">Nhập thông tin tổ chức do quản trị viên cung cấp.</p>
        <form onSubmit={submit} className="form-stack">
          <label>Mã tổ chức<input required value={organizationId} onChange={(e) => setOrganizationId(e.target.value)} placeholder="UUID của tổ chức" /></label>
          <label>Email<input required type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@company.vn" /></label>
          <label>Mật khẩu<input required type="password" value={password} onChange={(e) => setPassword(e.target.value)} /></label>
          {error && <div className="alert error" role="alert">{error}</div>}
          <button className="button primary full" disabled={submitting}>{submitting ? 'Đang xác thực…' : 'Đăng nhập'}</button>
        </form>
      </div></section>
    </main>
  )
}

export default function App() {
  const [session, setSession] = useState<Session | null>(() => loadSession())
  if (!session) return <LoginScreen onAuthenticated={setSession} />
  return <AssetWorkspace session={session} onSignOut={() => { clearSession(); setSession(null) }} />
}
