import { useEffect, useMemo, useState, useRef } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import { api, SESSION_KEY } from './services/api.js'

const icons = {
  camera: <><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></>,
  qr: <><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/><path d="M7 17h.01M17 7h.01M7 7h.01M17 17h.01"/></>,
  overview: <><rect x="3.5" y="3.5" width="7" height="7" rx="1.5"/><rect x="13.5" y="3.5" width="7" height="7" rx="1.5"/><rect x="3.5" y="13.5" width="7" height="7" rx="1.5"/><rect x="13.5" y="13.5" width="7" height="7" rx="1.5"/></>,
  attendance: <><path d="M8 3.5H6.5A2.5 2.5 0 0 0 4 6v13.5A2.5 2.5 0 0 0 6.5 22h11a2.5 2.5 0 0 0 2.5-2.5V6a2.5 2.5 0 0 0-2.5-2.5H16"/><path d="M8 3.5A1.5 1.5 0 0 1 9.5 2h5A1.5 1.5 0 0 1 16 3.5v2H8zM8 12h8M8 16h5"/></>,
  timetable: <><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18M8 14h2M14 14h2M8 18h2"/></>,
  sessions: <><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></>,
  people: <><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></>,
  audit: <><path d="M3 12a9 9 0 1 0 3-6.7L3 8"/><path d="M3 3v5h5M12 7v5l3 2"/></>,
  book: <><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2Z"/><path d="M8 7h7M8 10h5"/></>,
  bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></>,
  sun: <><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42m11.3 11.3 1.42 1.42M2 12h2m16 0h2M4.93 19.07l1.42-1.42m11.3-11.3 1.42-1.42"/></>,
  moon: <path d="M20.9 13A9 9 0 0 1 11 3.1 9 9 0 1 0 20.9 13Z"/>,
  plus: <path d="M12 5v14M5 12h14"/>,
  arrow: <><path d="M7 17 17 7M7 7h10v10"/></>,
  chevron: <path d="m9 18 6-6-6-6"/>,
  down: <path d="m7 10 5 5 5-5"/>,
  pin: <><path d="M20 10c0 5-8 12-8 12S4 15 4 10a8 8 0 1 1 16 0Z"/><circle cx="12" cy="10" r="2.5"/></>,
  check: <path d="m5 12 4 4L19 6"/>,
  download: <><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/></>,
  logout: <><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/></>,
  close: <path d="m18 6-12 12M6 6l12 12"/>,
  refresh: <><path d="M20 7v5h-5M4 17v-5h5"/><path d="M5.6 9A7 7 0 0 1 18 6l2 2M4 16l2 2a7 7 0 0 0 12.4-3"/></>,
  search: <><circle cx="10.8" cy="10.8" r="6.8"/><path d="m16 16 4.5 4.5"/></>,
}

function Icon({ name, size = 18 }) {
  return <svg aria-hidden="true" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">{icons[name]}</svg>
}

function readSession() {
  try { return JSON.parse(window.localStorage.getItem(SESSION_KEY) || 'null') } catch { return null }
}

function normalizedRole(role = '') { return role.replace(/^ROLE_/, '').toUpperCase() }
function roleName(role) { return ({ FACULTY: 'Faculty', DIRECTOR: 'Director', STUDENT: 'Student' })[role] || role }
function shortName(value = '') { return value.trim().split(/\s+/)[0] || 'there' }
function todayName() { return new Intl.DateTimeFormat('en', { weekday: 'long' }).format(new Date()).toUpperCase() }
function isTodaySlot(slot) {
  const slotDay = String(slot.dayOfWeek || '').toUpperCase()
  return slotDay === todayName() || slotDay.slice(0, 3) === todayName().slice(0, 3)
}
function sortTimetable(rows) {
  const order = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']
  return [...rows].sort((a, b) => {
    const dayA = order.indexOf(String(a.dayOfWeek || '').toUpperCase().slice(0, 3))
    const dayB = order.indexOf(String(b.dayOfWeek || '').toUpperCase().slice(0, 3))
    return (dayA < 0 ? 99 : dayA) - (dayB < 0 ? 99 : dayB) || Number(a.periodNumber || 0) - Number(b.periodNumber || 0)
  })
}
function formatDate(value) {
  if (!value) return '—'
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? String(value) : parsed.toLocaleDateString('en', { month: 'short', day: 'numeric', year: 'numeric' })
}
function formatDateTime(value) {
  if (!value) return '—'
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? String(value).replace('T', ' ') : parsed.toLocaleString('en', { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' })
}
function formatTime(value) {
  if (!value) return '—'
  const [hourText, minuteText = '00'] = String(value).split(':')
  const hour = Number(hourText)
  if (!Number.isFinite(hour)) return value
  return `${hour % 12 || 12}:${minuteText.slice(0, 2)} ${hour >= 12 ? 'PM' : 'AM'}`
}
function initials(name = '') { return name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]).join('').toUpperCase() || 'CP' }

function PortalApp() {
  const [session, setSession] = useState(readSession)
  const [authError, setAuthError] = useState('')
  const [dark, setDark] = useState(() => window.localStorage.getItem('campusly-theme') === 'dark')

  useEffect(() => {
    document.documentElement.dataset.theme = dark ? 'dark' : 'light'
    window.localStorage.setItem('campusly-theme', dark ? 'dark' : 'light')
  }, [dark])

  useEffect(() => {
    const handleUnauthorized = () => {
      window.localStorage.removeItem(SESSION_KEY)
      setSession(null)
      setAuthError('Your session expired. Please sign in again.')
    }
    window.addEventListener('campusly:unauthorized', handleUnauthorized)
    return () => window.removeEventListener('campusly:unauthorized', handleUnauthorized)
  }, [])

  async function handleLogin(email, password) {
    setAuthError('')
    const result = await api.login(email, password)
    const next = { ...result, role: normalizedRole(result.role) }
    if (!next.token || !['STUDENT', 'FACULTY', 'DIRECTOR'].includes(next.role)) {
      throw new Error('This account does not have a supported portal role.')
    }
    window.localStorage.setItem(SESSION_KEY, JSON.stringify(next))
    setSession(next)
  }

  function handleLogout() {
    window.localStorage.removeItem(SESSION_KEY)
    setSession(null)
    setAuthError('')
  }

  if (!session) return <LoginScreen onLogin={handleLogin} initialError={authError} />
  const role = normalizedRole(session.role)
  const common = { session, dark, setDark, onLogout: handleLogout }
  if (role === 'FACULTY') return <FacultyPortal {...common} />
  if (role === 'DIRECTOR') return <DirectorPortal {...common} />
  return <StudentPortal {...common} />
}

function LoginScreen({ onLogin, initialError }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(initialError)
  const [busy, setBusy] = useState(false)
  useEffect(() => setError(initialError), [initialError])

  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try { await onLogin(email.trim(), password) } catch (cause) { setError(cause.message || 'Sign in failed. Please try again.') } finally { setBusy(false) }
  }

  return <main className="login-page">
    <section className="login-art">
      <div className="login-brand"><span className="brand-mark"><Icon name="book" size={20}/></span><strong>campusly</strong></div>
      <div className="login-art-copy"><span className="login-eyebrow">YOUR CAMPUS, CONNECTED</span><h1>Everything<br/>in its right place.</h1><p>Attendance, class schedules, and the work that keeps your campus moving.</p></div>
      <div className="login-orbit login-orbit-a"/><div className="login-orbit login-orbit-b"/><div className="login-art-stamp"><Icon name="check" size={19}/><span>Ready for<br/>today</span></div>
      <div className="login-art-foot"><span>One portal for every campus day.</span><span>01 — 03</span></div>
    </section>
    <section className="login-form-side"><form className="login-form" onSubmit={submit}>
      <span className="section-kicker">WELCOME BACK</span><h2>Sign in to Campusly</h2><p>Use the email and password connected to your college account.</p>
      <label className="form-label">College email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@college.edu" autoComplete="username" required /></label>
      <label className="form-label">Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Enter your password" autoComplete="current-password" required /></label>
      {error && <div className="error-banner" role="alert">{error}</div>}
      <button className="button button-primary login-submit" type="submit" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'} <Icon name="chevron" size={16}/></button>
      <div className="login-divider"><span/>Secure college sign-in<span/></div>
      <p className="login-note">Your role is detected from your college account. Students, faculty, and directors use the same sign-in.</p>
    </form><span className="login-copyright">Campusly · College portal</span></section>
  </main>
}

function PortalShell({ session, role, active, setActive, items, title, dark, setDark, onLogout, children }) {
  const [search, setSearch] = useState('')
  const userName = session.fullName || session.email || roleName(role)
  const titleText = items.find((item) => item.id === active)?.label || title
  return <div className="app-shell portal-app">
    <aside className="sidebar portal-sidebar">
      <div className="brand"><div className="brand-mark"><Icon name="book" size={20}/></div><div className="brand-copy"><strong>campusly</strong><span>COLLEGE PORTAL</span></div></div>
      <div className="portal-role-chip"><span className="role-dot"/>{roleName(role)} workspace</div>
      <nav className="side-nav" aria-label="Main navigation">{items.map((item) => <button key={item.id} className={`nav-item ${active === item.id ? 'active' : ''}`} onClick={() => setActive(item.id)}><Icon name={item.icon} size={18}/><span>{item.label}</span>{item.count > 0 && <span className="nav-count">{item.count}</span>}</button>)}</nav>
      <div className="sidebar-bottom">
        <div className="portal-side-note"><div className="portal-side-note-mark">✳</div><strong>Campusly keeps you in sync</strong><p>Your classes and attendance live together in one place.</p></div>
        <div className="profile-mini"><span className="avatar avatar-small">{initials(userName)}</span><span className="profile-info"><strong>{userName}</strong><small>{roleName(role)} · {session.departmentName || 'Campus account'}</small></span><button className="logout-icon" title="Sign out" aria-label="Sign out" onClick={onLogout}><Icon name="logout" size={16}/></button></div>
      </div>
    </aside>
    <main className="main-area">
      <header className="topbar"><div className="breadcrumb"><span>Workspace</span><Icon name="chevron" size={14}/><strong>{titleText}</strong></div><div className="topbar-actions"><label className="search-box"><Icon name="search" size={16}/><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search this page" aria-label="Search this page"/><kbd>⌘ K</kbd></label><button className="icon-button" onClick={() => setDark(!dark)} aria-label={`Switch to ${dark ? 'light' : 'dark'} mode`}><Icon name={dark ? 'sun' : 'moon'} size={17}/></button><span className="avatar avatar-top">{initials(userName)}</span></div></header>
      <div className="page-content">{children({ search })}</div>
    </main>
  </div>
}

function PageIntro({ eyebrow, title, description, action }) {
  return <section className="welcome-row portal-intro"><div><div className="eyebrow">{eyebrow}</div><h1>{title}</h1><p>{description}</p></div>{action}</section>
}

function Metric({ label, value, hint, tone = 'blue', icon = 'overview' }) {
  return <article className="stat-card"><div className="stat-top"><span>{label}</span><span className={`stat-icon ${tone}`}><Icon name={icon} size={17}/></span></div><div className="stat-value">{value ?? '—'}</div><div className="stat-note">{hint}</div></article>
}

function Panel({ eyebrow, title, action, children, className = '' }) {
  return <section className={`panel ${className}`}><div className="panel-heading"><div>{eyebrow && <span className="section-kicker">{eyebrow}</span>}{title && <h2>{title}</h2>}</div>{action}</div>{children}</section>
}

function LoadNotice({ loading, error, empty, emptyText = 'Nothing to show yet.' }) {
  if (loading) return <div className="portal-state"><span className="spinner"/>Loading from your college account…</div>
  if (error) return <div className="portal-error" role="alert"><strong>Couldn’t load this section</strong><span>{error}</span></div>
  if (empty) return <div className="portal-state">{emptyText}</div>
  return null
}

function EmptyRow({ columns, children }) { return <tr><td colSpan={columns} className="empty-cell">{children}</td></tr> }

function StudentScannerModal({ studentId, onClose, onSuccess }) {
  const [tokenInput, setTokenInput] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [cameraActive, setCameraActive] = useState(false)
  const [cameraError, setCameraError] = useState('')
  const videoRef = useRef(null)
  const streamRef = useRef(null)
  const intervalRef = useRef(null)

  const stopCamera = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current)
      intervalRef.current = null
    }
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop())
      streamRef.current = null
    }
    setCameraActive(false)
  }

  const startCamera = async () => {
    setCameraError('')
    setError('')
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' }
      })
      streamRef.current = stream
      setCameraActive(true)
      setTimeout(() => {
        if (videoRef.current) {
          videoRef.current.srcObject = stream
          videoRef.current.play().catch(() => {})
        }
      }, 100)

      if ('BarcodeDetector' in window) {
        const detector = new window.BarcodeDetector({ formats: ['qr_code'] })
        intervalRef.current = setInterval(async () => {
          if (videoRef.current && videoRef.current.readyState >= 2) {
            try {
              const barcodes = await detector.detect(videoRef.current)
              if (barcodes.length > 0) {
                const scanned = barcodes[0].rawValue?.trim()
                if (scanned) {
                  stopCamera()
                  submitCheckIn(scanned)
                }
              }
            } catch {
              // ignore frame read errors
            }
          }
        }, 300)
      } else {
        setCameraError('Live barcode scanning is not supported by this browser. Enter the session token below.')
      }
    } catch {
      setCameraError('Could not access camera. Please allow camera permissions or enter the code manually.')
      setCameraActive(false)
    }
  }

  useEffect(() => {
    return () => stopCamera()
  }, [])

  const submitCheckIn = async (tokenToUse) => {
    const raw = (tokenToUse || tokenInput).trim()
    if (!raw) {
      setError('Please provide a valid session code')
      return
    }
    setBusy(true)
    setError('')
    setSuccess('')
    try {
      const res = await api.checkIn({ sessionToken: raw, studentId })
      setSuccess('Attendance marked successfully! You are recorded as PRESENT.')
      stopCamera()
      if (onSuccess) onSuccess(res)
    } catch (err) {
      setError(err.message || 'Check-in failed. The session may have expired or you may have already checked in.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <section className="request-modal session-modal" role="dialog" aria-modal="true" style={{ maxWidth: '460px' }}>
        <div className="modal-top">
          <span className="modal-icon"><Icon name="camera" size={20}/></span>
          <button className="icon-button" onClick={onClose} aria-label="Close"><Icon name="close" size={17}/></button>
        </div>
        <h2>Scan attendance QR</h2>
        <p>Scan the QR code displayed on the classroom screen, or paste the session code to verify your attendance.</p>

        {error && <div className="error-banner" role="alert" style={{ marginBottom: '14px' }}>{error}</div>}
        {success && <div className="success-banner" style={{ marginBottom: '14px', background: '#dcfce7', color: '#15803d', padding: '12px 14px', borderRadius: '8px' }}>{success}</div>}

        <div style={{ background: 'var(--surface-sunken, #f8fafc)', padding: '16px', borderRadius: '10px', border: '1px solid var(--line)', marginBottom: '16px', textAlign: 'center' }}>
          {cameraActive ? (
            <div>
              <div style={{ position: 'relative', width: '100%', height: '220px', background: '#000', borderRadius: '8px', overflow: 'hidden', margin: '0 auto 12px' }}>
                <video ref={videoRef} autoPlay playsInline muted style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                <div style={{ position: 'absolute', inset: '24px', border: '2px dashed #3b82f6', borderRadius: '8px', pointerEvents: 'none' }} />
              </div>
              <button className="button button-secondary" type="button" onClick={stopCamera}>Turn off camera</button>
            </div>
          ) : (
            <div>
              <button className="button button-primary" type="button" onClick={startCamera} style={{ width: '100%', justifyContent: 'center' }}>
                <Icon name="camera" size={18}/> Open camera scanner
              </button>
              {cameraError && <p style={{ color: '#dc2626', fontSize: '13px', margin: '8px 0 0' }}>{cameraError}</p>}
            </div>
          )}
        </div>

        <form onSubmit={(e) => { e.preventDefault(); submitCheckIn(); }}>
          <label className="form-label">
            Or enter session code / token
            <input
              type="text"
              value={tokenInput}
              onChange={(e) => setTokenInput(e.target.value)}
              placeholder="e.g. 5e913a7c-8e01-4467..."
              disabled={busy}
              style={{ fontFamily: 'monospace', fontSize: '13px' }}
            />
          </label>
          <div className="modal-actions">
            <button className="button button-secondary" type="button" onClick={onClose} disabled={busy}>Close</button>
            <button className="button button-primary" type="submit" disabled={busy || !tokenInput.trim() || !!success}>
              {busy ? 'Verifying…' : 'Mark attendance'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

function StudentPortal({ session, dark, setDark, onLogout }) {
  const [active, setActive] = useState('overview')
  const [data, setData] = useState({ attendance: null, timetable: [], notifications: [] })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [refresh, setRefresh] = useState(0)
  const [showScanner, setShowScanner] = useState(false)
  const [toast, setToast] = useState('')

  useEffect(() => {
    let alive = true
    setLoading(true)
    Promise.allSettled([api.studentAttendance(session.profileId), api.studentTimetable(session.profileId), api.studentNotifications(session.profileId)]).then((results) => {
      if (!alive) return
      const [attendance, timetable, notifications] = results
      setData({ attendance: attendance.status === 'fulfilled' ? attendance.value : null, timetable: timetable.status === 'fulfilled' ? timetable.value : [], notifications: notifications.status === 'fulfilled' ? notifications.value : [] })
      const failure = results.find((item) => item.status === 'rejected')
      setError(failure?.reason?.message || '')
      setLoading(false)
    })
    return () => { alive = false }
  }, [session.profileId, refresh])

  const items = [
    { id: 'overview', label: 'Overview', icon: 'overview' },
    { id: 'scan', label: 'Scan attendance', icon: 'camera' },
    { id: 'attendance', label: 'Attendance', icon: 'attendance' },
    { id: 'timetable', label: 'Timetable', icon: 'timetable' },
    { id: 'notifications', label: 'Notifications', icon: 'bell', count: data.notifications.filter((n) => !n.isRead).length }
  ]
  const attendance = data.attendance
  const subjects = attendance?.subjectBreakdown || []
  const todaySlots = data.timetable.filter(isTodaySlot)

  return <PortalShell session={session} role="STUDENT" active={active} setActive={setActive} items={items} dark={dark} setDark={setDark} onLogout={onLogout}>{({ search }) => <>
    <PageIntro
      eyebrow={`${session.className || 'Student account'}${session.labBatch ? ` · ${session.labBatch}` : ''}`}
      title={active === 'overview' ? `Welcome back, ${shortName(session.fullName)}` : items.find((item) => item.id === active)?.label}
      description="A clear view of attendance and what’s happening in your classes."
      action={
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <button className="button button-primary" onClick={() => setShowScanner(true)}>
            <Icon name="camera" size={16}/> Scan QR code
          </button>
          <button className="button button-secondary" onClick={() => setRefresh((value) => value + 1)}>
            <Icon name="refresh" size={15}/>Refresh
          </button>
        </div>
      }
    />
    {toast && <div className="toast-message" style={{ marginBottom: '16px' }}><Icon name="check" size={16}/>{toast}<button onClick={() => setToast('')}><Icon name="close" size={14}/></button></div>}
    <LoadNotice loading={loading} error={error} />
    
    {active === 'overview' && <><section className="stats-grid"><Metric label="Overall attendance" value={attendance ? `${Number(attendance.overallPercentage).toFixed(1)}%` : '—'} hint={Number(attendance?.overallPercentage) < 75 ? 'Below the 75% requirement' : 'You’re meeting the requirement'} tone={Number(attendance?.overallPercentage) < 75 ? 'orange' : 'green'} icon="attendance"/><Metric label="Classes attended" value={`${attendance?.attendedSessions ?? 0} / ${attendance?.totalSessions ?? 0}`} hint="Recorded attendance" tone="blue" icon="check"/><Metric label="Subjects tracked" value={subjects.length} hint="Current subject breakdown" tone="purple" icon="book"/><Metric label="Unread alerts" value={data.notifications.filter((n) => !n.isRead).length} hint="From your college" tone="orange" icon="bell"/></section>
      <section className="portal-grid"><Panel eyebrow="SUBJECT BREAKDOWN" title="Attendance by subject" className="portal-subject-panel"><LoadNotice loading={loading} error={error} empty={!subjects.length} emptyText="Your subject attendance will appear here once sessions are recorded."/>{subjects.map((subject) => <SubjectProgress key={subject.subjectId} subject={subject}/>)}</Panel><Panel eyebrow="TODAY" title="Your timetable" action={<button className="text-button" onClick={() => setActive('timetable')}>Full week <Icon name="chevron" size={14}/></button>}><TimetableRows rows={todaySlots} search={search} emptyText="No classes are scheduled for today."/></Panel></section></>}
    
    {active === 'scan' && (
      <Panel eyebrow="ATTENDANCE CHECK-IN" title="Scan classroom QR code">
        <p style={{ margin: '0 0 16px', color: 'var(--muted)' }}>
          To check into your current lecture or lab, open your camera to scan the code from the classroom display, or enter the session code.
        </p>
        <button className="button button-primary" onClick={() => setShowScanner(true)} style={{ padding: '12px 20px', fontSize: '15px' }}>
          <Icon name="camera" size={18}/> Launch QR scanner modal
        </button>
      </Panel>
    )}

    {active === 'attendance' && <Panel eyebrow="ATTENDANCE RECORD" title={`${attendance?.overallPercentage?.toFixed?.(1) ?? '—'}% overall`}><LoadNotice loading={loading} error={error} empty={!subjects.length} emptyText="No attendance records have been returned yet."/><div className="data-table-wrap"><table className="data-table"><thead><tr><th>Subject</th><th>Attended</th><th>Total</th><th>Attendance</th></tr></thead><tbody>{subjects.filter((subject) => `${subject.courseName} ${subject.shortCode}`.toLowerCase().includes(search.toLowerCase())).map((subject) => <tr key={subject.subjectId}><td><strong>{subject.courseName}</strong><small>{subject.shortCode || subject.courseCode}</small></td><td>{subject.attendedClasses}</td><td>{subject.totalClasses}</td><td><span className={`status-pill ${subject.percentage >= 75 ? 'status-present' : 'status-late'}`}>{Number(subject.percentage).toFixed(1)}%</span></td></tr>)}{!subjects.length && !loading && <EmptyRow columns={4}>No subject records yet.</EmptyRow>}</tbody></table></div></Panel>}
    {active === 'timetable' && <Panel eyebrow="WEEKLY SCHEDULE" title={`${session.className || 'Your section'}${session.labBatch ? ` · Batch ${session.labBatch}` : ''}`}><TimetableRows rows={data.timetable} search={search} emptyText="Your timetable hasn’t been published yet." showDay/></Panel>}
    {active === 'notifications' && <Panel eyebrow="CAMPUS UPDATES" title="Alerts & notifications"><LoadNotice loading={loading} error={error} empty={!data.notifications.length} emptyText="You’re all caught up."/>{data.notifications.filter((notice) => `${notice.title} ${notice.message} ${notice.type}`.toLowerCase().includes(search.toLowerCase())).map((notice) => <article className="portal-notice" key={notice.id}><span className={`notice-mark ${String(notice.type).toLowerCase()}`}><Icon name={notice.type === 'LOW_ATTENDANCE' ? 'attendance' : 'bell'} size={17}/></span><div><div className="notice-meta">{String(notice.type || 'GENERAL').replaceAll('_', ' ')} · {formatDateTime(notice.createdAt)}</div><h3>{notice.title}</h3><p>{notice.message}</p></div><span className={`read-mark ${notice.isRead ? 'is-read' : ''}`}/></article>)}</Panel>}

    {showScanner && (
      <StudentScannerModal
        studentId={session.profileId}
        onClose={() => setShowScanner(false)}
        onSuccess={() => {
          setRefresh((r) => r + 1)
          setToast('Attendance marked successfully! You are recorded as PRESENT.')
          setShowScanner(false)
        }}
      />
    )}
  </>}</PortalShell>
}

function SubjectProgress({ subject }) {
  const pct = Math.max(0, Math.min(100, Number(subject.percentage) || 0))
  return <div className="subject-progress"><div className="subject-progress-head"><span><strong>{subject.courseName}</strong><small>{subject.shortCode || subject.courseCode}</small></span><b className={pct < 75 ? 'below-threshold' : ''}>{pct.toFixed(1)}%</b></div><div className="progress-track"><span className={pct < 75 ? 'orange' : 'blue'} style={{ width: `${pct}%` }}/></div><div className="subject-progress-foot">{subject.attendedClasses} attended of {subject.totalClasses} sessions<span>{pct < 75 ? 'Below 75%' : 'On track'}</span></div></div>
}

function TimetableRows({ rows = [], search = '', emptyText, showDay = false }) {
  const filtered = rows.filter((slot) => `${slot.courseName} ${slot.shortCode} ${slot.section} ${slot.roomNumber} ${slot.facultyName} ${slot.dayOfWeek}`.toLowerCase().includes(search.toLowerCase()))
  const sorted = sortTimetable(filtered)
  if (!sorted.length) return <div className="portal-state">{emptyText}</div>
  return <div className="data-table-wrap"><table className="data-table"><thead><tr>{showDay && <th>Day</th>}<th>Time</th><th>Course</th><th>Section / batch</th><th>Faculty</th><th>Room</th></tr></thead><tbody>{sorted.map((slot, index) => <tr key={slot.id || `${slot.dayOfWeek}-${slot.periodNumber}-${index}`}>{showDay && <td>{slot.dayOfWeek}</td>}<td>{formatTime(slot.startTime)} – {formatTime(slot.endTime)}</td><td><strong>{slot.courseName}</strong><small>{slot.shortCode}</small></td><td>{slot.section} · {slot.targetBatch}</td><td>{slot.facultyName}</td><td>{slot.roomNumber || '—'}</td></tr>)}</tbody></table></div>
}

function FacultyPortal({ session, dark, setDark, onLogout }) {
  const [active, setActive] = useState('overview')
  const [data, setData] = useState({ timetable: [], activeSessions: [], history: [] })
  const [loading, setLoading] = useState(true)
  const [errors, setErrors] = useState({})
  const [refresh, setRefresh] = useState(0)
  const [showStart, setShowStart] = useState(false)
  const [selectedSession, setSelectedSession] = useState(null)
  const [toast, setToast] = useState('')
  const items = [{ id: 'overview', label: 'Overview', icon: 'overview' }, { id: 'timetable', label: 'My timetable', icon: 'timetable' }, { id: 'sessions', label: 'Attendance sessions', icon: 'attendance', count: data.activeSessions.length }]

  useEffect(() => {
    let alive = true
    setLoading(true)
    Promise.allSettled([api.facultyTimetable(session.profileId), api.activeSessions(session.profileId), api.sessionHistory(session.profileId)]).then((results) => {
      if (!alive) return
      const [timetable, activeSessions, history] = results
      setData({ timetable: timetable.status === 'fulfilled' ? timetable.value : [], activeSessions: activeSessions.status === 'fulfilled' ? activeSessions.value : [], history: history.status === 'fulfilled' ? history.value : [] })
      setErrors({ timetable: timetable.status === 'rejected' ? timetable.reason.message : '', active: activeSessions.status === 'rejected' ? activeSessions.reason.message : '', history: history.status === 'rejected' ? history.reason.message : '' })
      setLoading(false)
    })
    return () => { alive = false }
  }, [session.profileId, refresh])

  async function sessionStarted(created) {
    setShowStart(false)
    setSelectedSession(created)
    setToast('Attendance session started. Share the QR code with the class.')
    setRefresh((value) => value + 1)
  }

  async function sessionClosed() {
    setSelectedSession(null)
    setToast('Session closed. Unchecked students were marked absent.')
    setRefresh((value) => value + 1)
  }

  const todaySlots = data.timetable.filter(isTodaySlot)
  const displaySlots = todaySlots.length ? todaySlots : data.timetable.slice(0, 4)
  const historyRows = data.history.slice(0, 8)
  return <PortalShell session={session} role="FACULTY" active={active} setActive={setActive} items={items} title="Faculty workspace" dark={dark} setDark={setDark} onLogout={onLogout}>{({ search }) => <>
    {toast && <div className="toast-message"><Icon name="check" size={16}/>{toast}<button onClick={() => setToast('')} aria-label="Dismiss"><Icon name="close" size={14}/></button></div>}
    <PageIntro eyebrow={`${session.departmentName || 'Faculty workspace'} · ${new Date().toLocaleDateString('en', { weekday: 'long', month: 'long', day: 'numeric' })}`} title={active === 'overview' ? `Welcome, ${shortName(session.fullName)}` : items.find((item) => item.id === active)?.label} description="Keep today’s classes and attendance sessions moving." action={<button className="button button-primary" onClick={() => setShowStart(true)}><Icon name="plus" size={16}/>Start attendance</button>}/>
    {loading && <div className="portal-state"><span className="spinner"/>Loading your faculty workspace…</div>}
    {active === 'overview' && <><section className="stats-grid"><Metric label="Classes on timetable" value={data.timetable.length} hint="Across your weekly schedule" tone="blue" icon="timetable"/><Metric label="Today’s classes" value={todaySlots.length} hint={todaySlots.length ? 'From your faculty timetable' : 'No entries for today'} tone="purple" icon="book"/><Metric label="Active sessions" value={data.activeSessions.length} hint="Live attendance sessions" tone="green" icon="sessions"/><Metric label="Sessions recorded" value={data.history.length} hint="Attendance history" tone="orange" icon="attendance"/></section>
      <section className="portal-grid"><Panel eyebrow="TODAY’S TEACHING" title={todaySlots.length ? 'Classes on your schedule' : 'Your upcoming timetable'} action={<button className="text-button" onClick={() => setActive('timetable')}>Full timetable <Icon name="chevron" size={14}/></button>}><LoadNotice loading={loading} error={errors.timetable} empty={!displaySlots.length} emptyText="No timetable entries have been assigned to your account."/><TimetableRows rows={displaySlots} search={search} emptyText="No classes found."/></Panel><Panel eyebrow="IN PROGRESS" title="Live attendance"><LoadNotice loading={loading} error={errors.active} empty={!data.activeSessions.length} emptyText="No active sessions. Start one when class begins."/>{data.activeSessions.slice(0, 3).map((live) => <ActiveSessionCard key={live.sessionId} session={live} onOpen={() => setSelectedSession(live)}/>)}</Panel></section>
      <Panel eyebrow="RECENT ACTIVITY" title="Latest sessions" action={<button className="text-button" onClick={() => setActive('sessions')}>View history <Icon name="chevron" size={14}/></button>}><SessionTable rows={historyRows} search={search}/></Panel></>}
    {active === 'timetable' && <Panel eyebrow="WEEKLY SCHEDULE" title="Your assigned classes"><LoadNotice loading={loading} error={errors.timetable} empty={!data.timetable.length} emptyText="No timetable entries have been assigned to your account."/><TimetableRows rows={data.timetable} search={search} emptyText="No classes match your search." showDay/></Panel>}
    {active === 'sessions' && <Panel eyebrow="ATTENDANCE" title="Your sessions" action={<button className="button button-primary" onClick={() => setShowStart(true)}><Icon name="plus" size={16}/>Start session</button>}><LoadNotice loading={loading} error={errors.history} empty={!data.history.length} emptyText="You haven’t recorded an attendance session yet."/><SessionTable rows={data.history.filter((row) => `${row.courseName} ${row.section} ${row.status}`.toLowerCase().includes(search.toLowerCase()))}/></Panel>}
    {selectedSession && <LiveSessionView session={selectedSession} onBack={() => setSelectedSession(null)} onClosed={sessionClosed}/>}
    {showStart && <StartSessionDialog facultyId={session.profileId} slots={data.timetable} onClose={() => setShowStart(false)} onStarted={sessionStarted}/>}
  </>}</PortalShell>
}

function ActiveSessionCard({ session, onOpen }) {
  return <button className="active-session-card" onClick={onOpen}><span className="live-indicator"/><span className="active-session-copy"><strong>{session.courseName || `Subject ${session.subjectId}`}</strong><small>{session.section} · {session.targetBatch || 'ALL'} · {session.roomNumber || 'Room pending'}</small></span><span className="active-count">{session.presentCount || 0}<small>checked in</small></span><Icon name="chevron" size={15}/></button>
}

function StartSessionDialog({ facultyId, slots, onClose, onStarted }) {
  const [selected, setSelected] = useState(slots.length ? '0' : 'manual')
  const initial = slots[0]
  const [classId, setClassId] = useState(initial?.classId || '')
  const [subjectId, setSubjectId] = useState(initial?.subjectId || '')
  const [targetBatch, setTargetBatch] = useState(initial?.targetBatch || 'ALL')
  const [roomNumber, setRoomNumber] = useState(initial?.roomNumber || '')
  const [durationMinutes, setDurationMinutes] = useState(60)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  function chooseSlot(value) {
    setSelected(value)
    const slot = slots[Number(value)]
    if (!slot) return
    setClassId(slot.classId || '')
    setSubjectId(slot.subjectId || '')
    setTargetBatch(slot.targetBatch || 'ALL')
    setRoomNumber(slot.roomNumber || '')
  }

  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const created = await api.startSession(facultyId, { classId: Number(classId), subjectId: Number(subjectId), targetBatch, durationMinutes: Number(durationMinutes), attendanceMethod: 'QR_AND_MANUAL', roomNumber: roomNumber.trim() })
      onStarted(created)
    } catch (cause) { setError(cause.message || 'Could not start the session.') } finally { setBusy(false) }
  }

  return <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && onClose()}><section className="request-modal session-modal" role="dialog" aria-modal="true" aria-labelledby="start-session-title"><div className="modal-top"><span className="modal-icon"><Icon name="attendance" size={20}/></span><button className="icon-button" onClick={onClose} aria-label="Close"><Icon name="close" size={17}/></button></div><h2 id="start-session-title">Start an attendance session</h2><p>Choose a class from your timetable. A live session and QR token will be created for students to check in.</p>
    <form onSubmit={submit}>
      {slots.length > 0 && <label className="form-label">Class from timetable<select value={selected} onChange={(event) => chooseSlot(event.target.value)}>{slots.map((slot, index) => <option key={`${slot.classId}-${slot.subjectId}-${index}`} value={String(index)}>{slot.courseName} · {slot.section} · {slot.dayOfWeek} · Period {slot.periodNumber}</option>)}<option value="manual">Enter class and subject IDs</option></select></label>}
      {(selected === 'manual' || slots.length === 0) && <div className="form-row"><label className="form-label">Class ID<input type="number" min="1" value={classId} onChange={(event) => setClassId(event.target.value)} required/></label><label className="form-label">Subject ID<input type="number" min="1" value={subjectId} onChange={(event) => setSubjectId(event.target.value)} required/></label></div>}
      <div className="form-row"><label className="form-label">Target batch<select value={targetBatch} onChange={(event) => setTargetBatch(event.target.value)}><option value="ALL">ALL · Theory</option><option value="B1">B1 · Lab</option><option value="B2">B2 · Lab</option></select></label><label className="form-label">Duration<select value={durationMinutes} onChange={(event) => setDurationMinutes(event.target.value)}><option value="30">30 minutes</option><option value="45">45 minutes</option><option value="60">60 minutes</option><option value="90">90 minutes</option></select></label></div>
      <label className="form-label">Room<input value={roomNumber} onChange={(event) => setRoomNumber(event.target.value)} placeholder="e.g. A403"/></label>
      {error && <div className="error-banner" role="alert">{error}</div>}
      <div className="modal-actions"><button className="button button-secondary" type="button" onClick={onClose}>Cancel</button><button className="button button-primary" type="submit" disabled={busy || !classId || !subjectId}>{busy ? 'Starting…' : 'Start session'}</button></div>
    </form>
  </section></div>
}

function LiveSessionView({ session: initialSession, onBack, onClosed }) {
  const [session, setSession] = useState(initialSession)
  const [roster, setRoster] = useState([])
  const [rosterLoading, setRosterLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [filter, setFilter] = useState('ALL')
  const [refreshError, setRefreshError] = useState('')
  const [actionBusy, setActionBusy] = useState(null)
  const [notice, setNotice] = useState('')
  const [closing, setClosing] = useState(false)
  const [cancelling, setCancelling] = useState(false)

  const fetchSessionAndRoster = async () => {
    try {
      const [latestSession, latestRoster] = await Promise.all([
        api.liveSession(initialSession.sessionId),
        api.sessionRoster(initialSession.sessionId).catch(() => [])
      ])
      setSession(latestSession)
      if (Array.isArray(latestRoster) && latestRoster.length > 0) {
        setRoster(latestRoster)
      }
      setRefreshError('')
    } catch (cause) {
      setRefreshError(cause.message || 'Could not refresh session')
    } finally {
      setRosterLoading(false)
    }
  }

  useEffect(() => {
    let alive = true
    fetchSessionAndRoster()
    const timer = window.setInterval(() => {
      if (alive) fetchSessionAndRoster()
    }, 5000)
    return () => { alive = false; window.clearInterval(timer) }
  }, [initialSession.sessionId])

  async function toggleStudentStatus(student, targetStatus) {
    const isProxyCheck = targetStatus === 'ABSENT'
    const confirmMsg = isProxyCheck
      ? `Mark ${student.fullName} (${student.registerNumber}) as ABSENT? This overrides their QR scan to prevent proxy.`
      : `Mark ${student.fullName} (${student.registerNumber}) as PRESENT?`
    if (!window.confirm(confirmMsg)) return

    setActionBusy(student.studentId)
    setNotice('')
    setRoster(prev => prev.map(s => s.studentId === student.studentId ? { ...s, status: targetStatus } : s))

    try {
      await api.manualAttendance({
        sessionId: session.sessionId,
        records: [{
          studentId: student.studentId,
          status: targetStatus,
          remarks: isProxyCheck ? 'Marked absent by faculty (proxy check)' : 'Manually verified present by faculty'
        }]
      })
      setNotice(`${student.fullName} marked as ${targetStatus}.`)
      const latest = await api.liveSession(session.sessionId)
      setSession(latest)
    } catch (err) {
      setRefreshError(err.message || 'Failed to update attendance status')
      fetchSessionAndRoster()
    } finally {
      setActionBusy(null)
    }
  }

  async function closeSession() {
    if (!window.confirm('Close this attendance session? Any eligible student who has not checked in will be permanently marked ABSENT.')) return
    setClosing(true)
    try {
      await api.closeSession(session.sessionId)
      onClosed()
    } catch (cause) {
      setRefreshError(cause.message)
    } finally {
      setClosing(false)
    }
  }

  async function cancelSession() {
    if (!window.confirm('Cancel this attendance session? All scanned attendance for this session will be discarded and no student will be penalized.')) return
    setCancelling(true)
    try {
      if (api.cancelSession) {
        await api.cancelSession(session.sessionId).catch(() => api.closeSession(session.sessionId))
      } else {
        await api.closeSession(session.sessionId)
      }
      onClosed()
    } catch (cause) {
      setRefreshError(cause.message)
    } finally {
      setCancelling(false)
    }
  }

  const percent = session.totalEligibleCount ? Math.min(100, Math.round((session.presentCount / session.totalEligibleCount) * 100)) : 0
  const presentCount = roster.filter(s => s.status === 'PRESENT').length
  const absentCount = roster.length - presentCount

  const filteredRoster = roster.filter(s => {
    const matchesSearch = `${s.fullName} ${s.registerNumber} ${s.rollNumber || ''} ${s.labBatch || ''}`.toLowerCase().includes(search.toLowerCase())
    if (!matchesSearch) return false
    if (filter === 'PRESENT') return s.status === 'PRESENT'
    if (filter === 'ABSENT') return s.status === 'ABSENT'
    return true
  })

  return (
    <div className="live-view-overlay">
      <div className="live-view">
        <div className="live-view-header">
          <button className="text-button" onClick={onBack}>← Back to faculty workspace</button>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <span className="live-badge"><i/> SESSION LIVE</span>
            <button className="button button-danger" onClick={cancelSession} disabled={cancelling} style={{ padding: '6px 12px', fontSize: '13px' }}>
              {cancelling ? 'Cancelling…' : '✖ Cancel session'}
            </button>
          </div>
        </div>

        <div className="live-view-grid">
          <section className="panel live-main-panel">
            <div className="live-session-heading">
              <div>
                <span className="section-kicker">{session.section} · {session.targetBatch || 'ALL'} · {session.roomNumber || 'Room pending'}</span>
                <h1>{session.courseName || `Subject ${session.subjectId}`}</h1>
                <p>{session.shortCode || 'Attendance check-in'} · Started {formatDateTime(session.startTime)}</p>
              </div>
              <button className="button button-secondary" onClick={onBack}>Minimize</button>
            </div>

            <div className="qr-stage">
              {session.sessionToken ? (
                <div className="qr-card">
                  <QRCodeSVG value={session.sessionToken} size={220} level="H" includeMargin />
                  <div style={{ marginTop: '8px', textAlign: 'center' }}>
                    <small style={{ color: 'var(--muted)', display: 'block', marginBottom: '2px' }}>Session Token</small>
                    <code style={{ fontSize: '12px' }}>{session.sessionToken}</code>
                  </div>
                </div>
              ) : (
                <div className="portal-state">The session response did not include a QR token.</div>
              )}
              <div className="qr-caption">
                <span className="qr-caption-icon"><Icon name="attendance" size={17}/></span>
                <span><strong>Project this QR on screen</strong><small>Students scan via their phone camera. Live roster updates automatically on the right.</small></span>
              </div>
            </div>

            <div className="live-count-row">
              <div>
                <span className="section-kicker">CLASS CHECK-INS</span>
                <strong>{session.presentCount || presentCount || 0}<small> / {session.totalEligibleCount || roster.length || '—'}</small></strong>
                <p>Students verified in room</p>
              </div>
              <div className="live-progress"><span style={{ width: `${percent}%` }}/></div>
              <b>{session.totalEligibleCount ? `${percent}%` : 'Waiting for roster'}</b>
            </div>

            {refreshError && <div className="inline-warning" style={{ margin: '8px 0' }}>{refreshError}</div>}
            {notice && <div className="success-banner" style={{ margin: '8px 0', background: '#dcfce7', color: '#15803d', padding: '10px 14px', borderRadius: '6px' }}>{notice}</div>}

            <div style={{ display: 'flex', gap: '10px', marginTop: '16px' }}>
              <button className="button button-danger full-button" onClick={closeSession} disabled={closing} style={{ flex: 1, padding: '12px' }}>
                {closing ? 'Closing session…' : '✓ Close session & mark absentees'}
              </button>
              <button className="button button-secondary" onClick={cancelSession} disabled={cancelling} style={{ padding: '12px 18px' }}>
                {cancelling ? 'Cancelling…' : 'Cancel session'}
              </button>
            </div>
          </section>

          <aside className="panel manual-panel" style={{ display: 'flex', flexDirection: 'column', maxHeight: '720px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
              <span className="section-kicker">CLASS ROSTER · ANTI-PROXY</span>
              <span style={{ fontSize: '12px', color: 'var(--muted)' }}>Live updates</span>
            </div>
            <h2>Students in {session.section}</h2>
            <p style={{ margin: '0 0 12px', fontSize: '13px', color: 'var(--muted)' }}>
              Spot students absent in person who scanned via proxy, and toggle their status immediately.
            </p>

            <div style={{ marginBottom: '10px' }}>
              <input
                type="text"
                placeholder="Search student or SRN..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', fontSize: '13px', borderRadius: '6px', border: '1px solid var(--line)', background: 'var(--surface)' }}
              />
            </div>

            <div style={{ display: 'flex', gap: '6px', marginBottom: '12px' }}>
              <button
                type="button"
                className={`button ${filter === 'ALL' ? 'button-primary' : 'button-secondary'}`}
                style={{ padding: '4px 10px', fontSize: '12px' }}
                onClick={() => setFilter('ALL')}
              >
                All ({roster.length})
              </button>
              <button
                type="button"
                className={`button ${filter === 'PRESENT' ? 'button-primary' : 'button-secondary'}`}
                style={{ padding: '4px 10px', fontSize: '12px', background: filter === 'PRESENT' ? '#16a34a' : undefined }}
                onClick={() => setFilter('PRESENT')}
              >
                Present ({presentCount})
              </button>
              <button
                type="button"
                className={`button ${filter === 'ABSENT' ? 'button-primary' : 'button-secondary'}`}
                style={{ padding: '4px 10px', fontSize: '12px', background: filter === 'ABSENT' ? '#dc2626' : undefined }}
                onClick={() => setFilter('ABSENT')}
              >
                Absent ({absentCount})
              </button>
            </div>

            <div style={{ flex: 1, overflowY: 'auto', border: '1px solid var(--line)', borderRadius: '8px', padding: '6px', background: 'var(--canvas)' }}>
              {rosterLoading && <div style={{ padding: '20px', textAlign: 'center', color: 'var(--muted)', fontSize: '13px' }}>Loading student roster…</div>}
              {!rosterLoading && filteredRoster.length === 0 && (
                <div style={{ padding: '20px', textAlign: 'center', color: 'var(--muted)', fontSize: '13px' }}>
                  {roster.length === 0 ? 'No students found in this section.' : 'No students match your search.'}
                </div>
              )}
              {filteredRoster.map((st) => {
                const isPresent = st.status === 'PRESENT'
                const isUpdating = actionBusy === st.studentId
                return (
                  <div
                    key={st.studentId}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      padding: '8px 10px',
                      marginBottom: '4px',
                      borderRadius: '6px',
                      background: 'var(--surface)',
                      border: isPresent ? '1px solid #bbf7d0' : '1px solid var(--line)',
                      gap: '8px'
                    }}
                  >
                    <div style={{ minWidth: 0, flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <strong style={{ fontSize: '13px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{st.fullName}</strong>
                        {st.labBatch && <span style={{ fontSize: '10px', background: '#e2e8f0', padding: '1px 5px', borderRadius: '4px' }}>{st.labBatch}</span>}
                      </div>
                      <div style={{ fontSize: '11px', color: 'var(--muted)', fontFamily: 'monospace' }}>
                        {st.registerNumber} {st.rollNumber ? `· ${st.rollNumber}` : ''}
                      </div>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span className={`status-pill ${isPresent ? 'status-present' : 'status-late'}`} style={{ fontSize: '11px', padding: '3px 7px' }}>
                        {isPresent ? 'PRESENT' : 'ABSENT'}
                      </span>
                      <button
                        type="button"
                        className="button"
                        disabled={isUpdating}
                        onClick={() => toggleStudentStatus(st, isPresent ? 'ABSENT' : 'PRESENT')}
                        style={{
                          padding: '4px 8px',
                          fontSize: '11px',
                          background: isPresent ? '#fee2e2' : '#dcfce7',
                          color: isPresent ? '#b91c1c' : '#15803d',
                          border: isPresent ? '1px solid #fca5a5' : '1px solid #86efac',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          fontWeight: 600
                        }}
                        title={isPresent ? 'Anti-proxy: Mark absent if student is not physically in class' : 'Mark present manually'}
                      >
                        {isUpdating ? '…' : isPresent ? 'Mark Absent' : 'Mark Present'}
                      </button>
                    </div>
                  </div>
                )
              })}
            </div>
          </aside>
        </div>
      </div>
    </div>
  )
}

function SessionTable({ rows = [], search = '' }) {
  const filtered = rows.filter((row) => `${row.courseName} ${row.section} ${row.status} ${row.targetBatch}`.toLowerCase().includes(search.toLowerCase()))
  return <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Class</th><th>Section</th><th>Batch</th><th>Date</th><th>Check-ins</th><th>Status</th></tr></thead><tbody>{filtered.map((row) => <tr key={row.sessionId}><td><strong>{row.courseName || `Subject ${row.subjectId}`}</strong><small>{row.shortCode}</small></td><td>{row.section}</td><td>{row.targetBatch || 'ALL'}</td><td>{formatDate(row.sessionDate || row.startTime)}</td><td>{row.presentCount ?? 0}{row.totalEligibleCount ? ` / ${row.totalEligibleCount}` : ''}</td><td><span className={`status-pill ${row.status === 'ACTIVE' ? 'status-present' : 'status-neutral'}`}>{row.status || '—'}</span></td></tr>)}{!filtered.length && <EmptyRow columns={6}>No sessions match this search.</EmptyRow>}</tbody></table></div>
}

function DirectorPortal({ session, dark, setDark, onLogout }) {
  const [active, setActive] = useState('overview')
  const [stats, setStats] = useState(null)
  const [defaulters, setDefaulters] = useState([])
  const [auditLogs, setAuditLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [errors, setErrors] = useState({})
  const [thresholdInput, setThresholdInput] = useState('75')
  const [threshold, setThreshold] = useState(75)
  const [refresh, setRefresh] = useState(0)
  const items = [{ id: 'overview', label: 'Overview', icon: 'overview' }, { id: 'defaulters', label: 'Low attendance', icon: 'people', count: defaulters.length }, { id: 'audit', label: 'Audit trail', icon: 'audit' }]

  useEffect(() => {
    let alive = true
    setLoading(true)
    Promise.allSettled([api.directorDashboard(), api.lowAttendance(threshold), api.auditLogs()]).then((results) => {
      if (!alive) return
      const [dashboard, low, audit] = results
      setStats(dashboard.status === 'fulfilled' ? dashboard.value : null)
      setDefaulters(low.status === 'fulfilled' ? low.value : [])
      setAuditLogs(audit.status === 'fulfilled' ? audit.value : [])
      setErrors({ stats: dashboard.status === 'rejected' ? dashboard.reason.message : '', low: low.status === 'rejected' ? low.reason.message : '', audit: audit.status === 'rejected' ? audit.reason.message : '' })
      setLoading(false)
    })
    return () => { alive = false }
  }, [threshold, refresh])

  const metrics = stats || {}
  function applyThreshold(event) { event.preventDefault(); const parsed = Number(thresholdInput); if (parsed >= 0 && parsed <= 100) setThreshold(parsed) }
  function exportReport() { downloadCsv(`low-attendance-under-${threshold}.csv`, ['Student ID', 'Student', 'Roll number', 'Attendance %', 'Attended', 'Total sessions'], defaulters.map((student) => [student.studentId, student.fullName, student.rollNumber, student.overallPercentage, student.attendedSessions, student.totalSessions])) }
  function render({ search }) {
    const filtered = defaulters.filter((student) => `${student.fullName} ${student.rollNumber} ${student.studentId}`.toLowerCase().includes(search.toLowerCase()))
    const filteredAudit = auditLogs.filter((item) => `${item.action} ${item.entityName} ${item.details} ${item.user?.fullName}`.toLowerCase().includes(search.toLowerCase()))
    return <>
      <PageIntro eyebrow={`${session.departmentName || 'Institution-wide view'} · DIRECTOR`} title={active === 'overview' ? `Good day, ${shortName(session.fullName)}` : items.find((item) => item.id === active)?.label} description="An institution-wide view of attendance activity and follow-up." action={<button className="button button-secondary" onClick={() => setRefresh((value) => value + 1)}><Icon name="refresh" size={15}/>Refresh data</button>}/>
      {loading && <div className="portal-state"><span className="spinner"/>Loading institution analytics…</div>}
      {active === 'overview' && <><section className="stats-grid director-stats"><Metric label="Total students" value={metrics.totalStudents} hint="Registered student profiles" tone="blue" icon="people"/><Metric label="Faculty" value={metrics.totalFaculty} hint="Registered faculty profiles" tone="purple" icon="book"/><Metric label="Classes" value={metrics.totalClasses} hint="College sections" tone="green" icon="timetable"/><Metric label="Attendance sessions" value={metrics.totalAttendanceSessions} hint={`${metrics.totalAttendanceRecords ?? 0} attendance records`} tone="orange" icon="attendance"/></section>
        {errors.stats && <div className="portal-error" role="alert">{errors.stats}</div>}
        <section className="portal-grid"><Panel eyebrow={`BELOW ${threshold}%`} title="Attendance follow-up" action={<button className="text-button" onClick={() => setActive('defaulters')}>View all <Icon name="chevron" size={14}/></button>}><LoadNotice loading={loading} error={errors.low} empty={!defaulters.length} emptyText={`No students are below ${threshold}% in the current report.`}/><DefaulterRows rows={defaulters.slice(0, 5)} search={search}/></Panel><Panel eyebrow="ACCOUNTABILITY" title="Recent audit activity" action={<button className="text-button" onClick={() => setActive('audit')}>Audit trail <Icon name="chevron" size={14}/></button>}><LoadNotice loading={loading} error={errors.audit} empty={!auditLogs.length} emptyText="No audit entries have been returned."/>{auditLogs.slice(0, 5).map((item) => <AuditRow key={item.id} item={item}/>)}</Panel></section></>}
      {active === 'defaulters' && <Panel eyebrow={`THRESHOLD · ${threshold}%`} title="Students needing follow-up" action={<button className="button button-secondary" onClick={exportReport}><Icon name="download" size={15}/>Export CSV</button>}><form className="threshold-form" onSubmit={applyThreshold}><label>Show students below <input type="number" min="0" max="100" step="0.5" value={thresholdInput} onChange={(event) => setThresholdInput(event.target.value)}/> %</label><button className="button button-primary" type="submit">Apply</button><span>{filtered.length} students</span></form><LoadNotice loading={loading} error={errors.low} empty={!filtered.length} emptyText={`No students are below ${threshold}% in the current report.`}/><DefaulterRows rows={filtered} search=""/></Panel>}
      {active === 'audit' && <Panel eyebrow="RECORD OF MANUAL CHANGES" title="Attendance audit trail"><LoadNotice loading={loading} error={errors.audit} empty={!filteredAudit.length} emptyText="No audit entries have been returned."/><div className="data-table-wrap"><table className="data-table"><thead><tr><th>Action</th><th>Changed by</th><th>Record</th><th>Details</th><th>Time</th></tr></thead><tbody>{filteredAudit.map((item) => <tr key={item.id}><td><span className="status-pill status-neutral">{item.action}</span></td><td>{item.user?.fullName || item.user?.email || '—'}</td><td>{item.entityName}{item.entityId ? ` #${item.entityId}` : ''}</td><td className="audit-details">{item.details || '—'}</td><td>{formatDateTime(item.createdAt)}</td></tr>)}{!filteredAudit.length && !loading && <EmptyRow columns={5}>No audit records match this search.</EmptyRow>}</tbody></table></div></Panel>}
    </>
  }

  return <PortalShell session={session} role="DIRECTOR" active={active} setActive={setActive} items={items} dark={dark} setDark={setDark} onLogout={onLogout}>{render}</PortalShell>
}

function DefaulterRows({ rows, search = '' }) {
  const filtered = rows.filter((student) => `${student.fullName} ${student.rollNumber} ${student.studentId}`.toLowerCase().includes(search.toLowerCase()))
  return <div className="data-table-wrap"><table className="data-table"><thead><tr><th>Student</th><th>Roll number</th><th>Attended</th><th>Attendance</th></tr></thead><tbody>{filtered.map((student) => <tr key={student.studentId}><td><strong>{student.fullName}</strong><small>Student ID {student.studentId}</small></td><td>{student.rollNumber || '—'}</td><td>{student.attendedSessions} / {student.totalSessions}</td><td><span className="status-pill status-late">{Number(student.overallPercentage).toFixed(1)}%</span></td></tr>)}{!filtered.length && <EmptyRow columns={4}>No students match this search.</EmptyRow>}</tbody></table><p className="api-limit-note">The current low-attendance API does not include student phone numbers or section names.</p></div>
}

function AuditRow({ item }) {
  return <article className="audit-row"><span className="audit-icon"><Icon name="audit" size={15}/></span><span><strong>{item.action} · {item.entityName}</strong><small>{item.user?.fullName || item.user?.email || 'Account'} · {formatDateTime(item.createdAt)}</small></span></article>
}

function downloadCsv(filename, headers, rows) {
  const escapeCell = (value) => {
    let cell = String(value ?? '')
    if (/^[=+\-@]/.test(cell)) cell = `'${cell}`
    return `"${cell.replaceAll('"', '""')}"`
  }
  const content = [headers, ...rows].map((row) => row.map(escapeCell).join(',')).join('\r\n')
  const url = URL.createObjectURL(new Blob([content], { type: 'text/csv;charset=utf-8' }))
  const anchor = document.createElement('a')
  anchor.href = url; anchor.download = filename; anchor.click()
  URL.revokeObjectURL(url)
}

export default PortalApp
