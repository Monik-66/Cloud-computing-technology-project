const API_ROOT = (import.meta.env.VITE_API_BASE_URL || 'https://cloud-computing-technology-project.onrender.com/api/v1').replace(/\/+$/, '')
const SESSION_KEY = 'campusly-auth'

export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

function savedToken() {
  try {
    return JSON.parse(window.localStorage.getItem(SESSION_KEY) || 'null')?.token || null
  } catch {
    return null
  }
}

async function request(path, { body, publicRequest = false, ...options } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (!publicRequest) {
    const token = savedToken()
    if (token) headers.Authorization = `Bearer ${token}`
  }

  let response
  try {
    response = await fetch(`${API_ROOT}${path}`, {
      ...options,
      headers: { ...headers, ...options.headers },
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError('Could not reach the attendance service. Check your connection and try again.')
  }

  const raw = await response.text()
  let data = null
  if (raw) {
    try { data = JSON.parse(raw) } catch { data = raw }
  }

  if (response.status === 401 && !publicRequest) {
    window.dispatchEvent(new CustomEvent('campusly:unauthorized'))
  }
  if (!response.ok) {
    const message = typeof data === 'string'
      ? data
      : data?.message || data?.error || `Request failed (${response.status})`
    throw new ApiError(message, response.status)
  }
  return data
}

export const api = {
  login: (email, password) => request('/auth/login', { method: 'POST', body: { email, password }, publicRequest: true }),
  studentAttendance: (id) => request(`/student/${id}/attendance`),
  studentTimetable: (id) => request(`/student/${id}/timetable`),
  studentNotifications: (id) => request(`/student/${id}/notifications`),
  facultyTimetable: (id) => request(`/faculty/${id}/timetable`),
  activeSessions: (id) => request(`/faculty/${id}/sessions/active`),
  sessionHistory: (id) => request(`/faculty/${id}/sessions/history`),
  startSession: (facultyId, payload) => request(`/attendance/session/start?facultyId=${encodeURIComponent(facultyId)}`, { method: 'POST', body: payload }),
  liveSession: (sessionId) => request(`/attendance/session/${sessionId}/live`),
  closeSession: (sessionId) => request(`/attendance/session/${sessionId}/close`, { method: 'POST' }),
  manualAttendance: (payload) => request('/attendance/manual', { method: 'POST', body: payload }),
  directorDashboard: () => request('/director/dashboard'),
  lowAttendance: (threshold = 75) => request(`/director/low-attendance?threshold=${encodeURIComponent(threshold)}`),
  auditLogs: () => request('/director/audit-logs'),
}

export { SESSION_KEY }
