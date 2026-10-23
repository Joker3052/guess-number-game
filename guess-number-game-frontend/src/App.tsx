import { useEffect, useRef, useState, type FormEvent } from 'react'
import { ArrowRight, Crown, Gamepad2, LoaderCircle, LogOut, RotateCcw, Sparkles, UserRound, Zap } from 'lucide-react'
import { api, getErrorMessage, setAccessToken, type GameResult, type LeaderboardEntry, type LoginResponse, type PaymentResult, type TurnsPackage, type UserInfo } from './api'
import './App.css'

type AuthMode = 'login' | 'register'

function App() {
  const [token, setToken] = useState(() => localStorage.getItem('guess-number-token'))
  const [user, setUser] = useState<UserInfo | null>(null)
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([])
  const [turnsPackage, setTurnsPackage] = useState<TurnsPackage | null>(null)
  const [authMode, setAuthMode] = useState<AuthMode>('login')
  const [authForm, setAuthForm] = useState({ username: '', email: '', password: '' })
  const [selectedNumber, setSelectedNumber] = useState<number | null>(null)
  const [result, setResult] = useState<GameResult | null>(null)
  const [loading, setLoading] = useState<'auth' | 'profile' | 'guess' | 'payment' | 'leaderboard' | null>(null)
  const [message, setMessage] = useState(() => new URLSearchParams(window.location.search).get('payment') === 'cancel' ? 'Bạn đã hủy thanh toán. Chưa có lượt nào được cộng.' : '')
  const [error, setError] = useState('')
  const captureAttempt = useRef<string | null>(null)

  useEffect(() => {
    if (!token) return
    setAccessToken(token)
    void refreshUser(token)
    void loadLeaderboard()
    void api.get<TurnsPackage>('/payments/paypal/packages').then((response) => setTurnsPackage(response.data)).catch((requestError: unknown) => setError(getErrorMessage(requestError)))
    const query = new URLSearchParams(window.location.search)
    const paypalOrderId = query.get('token')
    if (query.get('payment') === 'success' && paypalOrderId && captureAttempt.current !== paypalOrderId) {
      captureAttempt.current = paypalOrderId
      void capturePayment(paypalOrderId)
    } else if (query.get('payment') === 'cancel') {
      window.history.replaceState({}, '', window.location.pathname)
    }
  }, [token])

  async function capturePayment(paypalOrderId: string) {
    setError('')
    setMessage('Đang xác nhận thanh toán với PayPal...')
    setLoading('payment')
    try {
      const response = await api.post<PaymentResult>('/payments/paypal/capture', { paypalOrderId })
      setUser((current) => current ? { ...current, turns: response.data.remainingTurns } : current)
      setMessage(`Thanh toán thành công! ${response.data.turnsAdded} lượt đã được cộng.`)
      window.history.replaceState({}, '', window.location.pathname)
      void loadLeaderboard()
    } catch (requestError) {
      captureAttempt.current = null
      setError(getErrorMessage(requestError))
      setMessage('Không thể xác nhận thanh toán. Bạn có thể thử tải lại sau.')
    } finally {
      setLoading(null)
      void refreshUser()
    }
  }

  async function refreshUser(currentToken = token) {
    if (!currentToken) return
    setLoading('profile')
    try {
      const response = await api.get<UserInfo>('/users/me')
      setUser(response.data)
    } catch (requestError) {
      setAccessToken(null)
      setToken(null)
      setUser(null)
      setError(getErrorMessage(requestError))
    } finally {
      setLoading(null)
    }
  }

  async function loadLeaderboard() {
    setLoading('leaderboard')
    try {
      const response = await api.get<LeaderboardEntry[]>('/game/leaderboard')
      setLeaderboard(response.data)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setLoading(null)
    }
  }

  async function handleAuth(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setMessage('')
    setLoading('auth')
    try {
      if (authMode === 'register') {
        const response = await api.post<string>('/auth/register', {
          username: authForm.username,
          email: authForm.email,
          password: authForm.password,
        })
        setMessage(typeof response.data === 'string' ? response.data : 'Tạo tài khoản thành công. Hãy đăng nhập.')
        setAuthMode('login')
      } else {
        const response = await api.post<LoginResponse>('/auth/login', {
          username: authForm.username,
          password: authForm.password,
        })
        setAccessToken(response.data.accessToken)
        setToken(response.data.accessToken)
        setMessage('Đăng nhập thành công. Chúc bạn may mắn!')
      }
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setLoading(null)
    }
  }

  async function handleGuess() {
    if (selectedNumber === null || !user) return
    setError('')
    setLoading('guess')
    try {
      const response = await api.post<GameResult>('/game/guess', { guessedNumber: selectedNumber })
      setResult(response.data)
      setUser((current) => current ? { ...current, score: response.data.currentScore, turns: response.data.remainingTurns } : current)
      void loadLeaderboard()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
      if (getErrorMessage(requestError).includes('No turns left')) void refreshUser()
    } finally {
      setLoading(null)
    }
  }

  async function handleBuyTurns() {
    setError('')
    setLoading('payment')
    try {
      if (!turnsPackage) throw new Error('Không tải được gói lượt chơi. Hãy thử làm mới trang.')
      const response = await api.post<{ approvalUrl: string }>('/payments/paypal/orders', { packageId: turnsPackage.packageId })
      window.location.assign(response.data.approvalUrl)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      if (window.location.pathname === '/') setLoading(null)
    }
  }

  function logout() {
    setAccessToken(null)
    setToken(null)
    setUser(null)
    setResult(null)
    setSelectedNumber(null)
    setError('')
    setMessage('')
  }

  const busy = loading !== null

  if (!token) {
    return (
      <main className="auth-layout">
        <section className="auth-intro">
          <a className="brand" href="#top" aria-label="Lucky Five trang chủ"><span className="brand-mark"><Sparkles size={19} /></span> lucky five</a>
          <div className="intro-copy">
            <div className="number-orbit" aria-hidden="true"><span>1</span><span>2</span><span>3</span><span>4</span><strong>5</strong></div>
            <p className="eyebrow">MỘT LẦN ĐOÁN, MỘT CƠ HỘI</p>
            <h1>Vận may<br />đang gọi tên bạn.</h1>
            <p className="intro-description">Chọn một con số từ 1 đến 5. Đoán đúng, nhận ngay 10 điểm và leo lên bảng xếp hạng.</p>
          </div>
          <div className="intro-foot"><span className="online-dot" /> Sẵn sàng cho một ván mới?</div>
        </section>

        <section className="auth-panel" id="top">
          <div className="auth-box">
            <p className="eyebrow">CHÀO MỪNG BẠN</p>
            <h2>{authMode === 'login' ? 'Đăng nhập' : 'Tạo tài khoản'}</h2>
            <p className="auth-subtitle">{authMode === 'login' ? 'Tiếp tục hành trình chinh phục điểm số.' : 'Tạo tài khoản để lưu lại điểm số của bạn.'}</p>
            <div className="auth-tabs" role="tablist" aria-label="Chọn hình thức xác thực">
              <button className={authMode === 'login' ? 'active' : ''} onClick={() => { setAuthMode('login'); setError(''); setMessage('') }} type="button">Đăng nhập</button>
              <button className={authMode === 'register' ? 'active' : ''} onClick={() => { setAuthMode('register'); setError(''); setMessage('') }} type="button">Đăng ký</button>
            </div>
            <form className="auth-form" onSubmit={handleAuth}>
              <label>Tên đăng nhập<input autoComplete="username" placeholder="Ví dụ: luckyplayer" required value={authForm.username} onChange={(event) => setAuthForm({ ...authForm, username: event.target.value })} /></label>
              {authMode === 'register' && <label>Email<input autoComplete="email" placeholder="ban@email.com" required type="email" value={authForm.email} onChange={(event) => setAuthForm({ ...authForm, email: event.target.value })} /></label>}
              <label>Mật khẩu<input autoComplete={authMode === 'login' ? 'current-password' : 'new-password'} minLength={6} placeholder="Ít nhất 6 ký tự" required type="password" value={authForm.password} onChange={(event) => setAuthForm({ ...authForm, password: event.target.value })} /></label>
              {error && <p className="notice error" role="alert">{error}</p>}
              {message && <p className="notice success" role="status">{message}</p>}
              <button className="button primary auth-submit" disabled={busy} type="submit">{loading === 'auth' ? <><LoaderCircle className="spinner" size={18} /> Đang xử lý...</> : <>{authMode === 'login' ? 'Vào chơi ngay' : 'Tạo tài khoản'} <ArrowRight size={18} /></>}</button>
            </form>
            <p className="auth-note"><Zap size={14} /> Mỗi lượt chơi đều có cơ hội thắng 5%.</p>
          </div>
          <p className="copyright">© 2026 Lucky Five · Trò chơi đoán số</p>
        </section>
      </main>
    )
  }

  return (
    <main className="game-shell">
      <header className="topbar">
        <a className="brand" href="#top"><span className="brand-mark"><Sparkles size={19} /></span> lucky five</a>
        <div className="topbar-right">
          <div className="user-chip"><span className="avatar"><UserRound size={17} /></span><span>{user?.username ?? 'Đang tải...'}</span></div>
          <button className="icon-button" aria-label="Đăng xuất" onClick={logout}><LogOut size={18} /></button>
        </div>
      </header>

      <div className="dashboard">
        <section className="welcome-row">
          <div><p className="eyebrow">KHU VỰC TRÒ CHƠI</p><h1>Xin chào, {user?.username ?? 'bạn'} <span className="wave">✦</span></h1><p>Chọn số của bạn và thử vận may hôm nay.</p></div>
          <button className="button secondary" onClick={() => { setResult(null); setSelectedNumber(null); void refreshUser(); void loadLeaderboard() }} disabled={busy}><RotateCcw size={16} /> Làm mới</button>
        </section>

        <section className="stats-row" aria-label="Thông tin người chơi">
          <article className="stat-card"><span className="stat-icon score-icon"><Crown size={19} /></span><div><span className="stat-label">TỔNG ĐIỂM</span><strong>{loading === 'profile' && !user ? '—' : user?.score ?? 0}</strong></div><span className="stat-caption">điểm</span></article>
          <article className="stat-card"><span className="stat-icon turns-icon"><Gamepad2 size={19} /></span><div><span className="stat-label">LƯỢT CÒN LẠI</span><strong>{loading === 'profile' && !user ? '—' : user?.turns ?? 0}</strong></div><span className="stat-caption">lượt</span></article>
          <article className="stat-card profile-card"><span className="stat-icon profile-icon"><UserRound size={19} /></span><div><span className="stat-label">TÀI KHOẢN</span><strong className="profile-email">{user?.email ?? 'Đang tải...'}</strong></div></article>
        </section>

        <div className="content-grid">
          <section className="game-card">
            <div className="game-heading"><div><p className="eyebrow">TRÒ CHƠI HÔM NAY</p><h2>Đoán con số bí mật</h2><p>Chọn một số từ 1 đến 5. Bạn có 5% cơ hội thắng.</p></div><div className="chance-mark"><span>1</span><span>/</span><strong>20</strong></div></div>
            <div className="number-picker" aria-label="Chọn số để đoán">
              {[1, 2, 3, 4, 5].map((number) => <button key={number} className={`number-button ${selectedNumber === number ? 'selected' : ''}`} onClick={() => { setSelectedNumber(number); setResult(null); setError('') }} aria-pressed={selectedNumber === number}>{number}</button>)}
            </div>
            {result ? <div className={`result-panel ${result.won ? 'win' : 'lose'}`} role="status"><span className="result-emoji">{result.won ? '🎉' : '💫'}</span><div><strong>{result.won ? 'Bạn đã thắng!' : 'Chưa trúng lần này'}</strong><span>{result.won ? `Số bí mật là ${result.secretNumber}. Bạn nhận được 10 điểm!` : `Số bí mật là ${result.secretNumber}. Thử vận may lần nữa nhé.`}</span></div></div> : <div className="game-hint">{selectedNumber ? <>Bạn chọn số <strong>{selectedNumber}</strong>. Sẵn sàng chưa?</> : 'Chọn một con số bạn cảm thấy may mắn.'}</div>}
            {error && <p className="notice error" role="alert">{error}</p>}
            {message && <p className="notice success" role="status">{message}</p>}
            <div className="game-actions">
              <button className="button primary play-button" disabled={busy || !selectedNumber || !user?.turns} onClick={() => void handleGuess()}>{loading === 'guess' ? <><LoaderCircle className="spinner" size={18} /> Đang đoán...</> : <>Thử vận may <ArrowRight size={18} /></>}</button>
              <button className="button secondary buy-button" disabled={busy || !turnsPackage} onClick={() => void handleBuyTurns()}>{loading === 'payment' ? <><LoaderCircle className="spinner" size={17} /> Đang mở PayPal...</> : <><Zap size={17} /> Mua {turnsPackage?.turns ?? 5} lượt · {turnsPackage ? `${turnsPackage.amount.toFixed(2)} ${turnsPackage.currency}` : '...'}</>}</button>
            </div>
            {user?.turns === 0 && <p className="empty-turns">Bạn đã dùng hết lượt. Nhận thêm lượt để tiếp tục chơi.</p>}
          </section>

          <aside className="leaderboard-card">
            <div className="leaderboard-head"><div><p className="eyebrow">CỘNG ĐỒNG</p><h2><Crown size={19} /> Bảng xếp hạng</h2></div><span className="top-ten">TOP 10</span></div>
            {loading === 'leaderboard' && leaderboard.length === 0 ? <div className="leaderboard-loading"><LoaderCircle className="spinner" size={19} /> Đang tải bảng xếp hạng...</div> : leaderboard.length === 0 ? <div className="leaderboard-empty">Chưa có người chơi nào. Hãy là người đầu tiên!</div> : <ol className="leaderboard-list">{leaderboard.slice(0, 10).map((entry, index) => <li className={entry.username === user?.username ? 'current-player' : ''} key={`${entry.username}-${index}`}><span className={`rank rank-${index + 1}`}>{String(index + 1).padStart(2, '0')}</span><span className="leader-name">{entry.username}{entry.username === user?.username && <small>BẠN</small>}</span><strong>{entry.score}<span> đ</span></strong></li>)}</ol>}
            <button className="leader-refresh" onClick={() => void loadLeaderboard()} disabled={busy}><RotateCcw size={14} /> Cập nhật bảng</button>
          </aside>
        </div>
        <footer className="dashboard-footer"><span>Đoán số · Nhận điểm · Lên hạng</span><span>Chúc bạn may mắn <Sparkles size={14} /></span></footer>
      </div>
    </main>
  )
}

export default App
