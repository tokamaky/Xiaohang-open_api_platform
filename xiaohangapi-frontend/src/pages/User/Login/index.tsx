import Footer from '@/components/Footer';
import { userLoginUsingPost, userRegisterUsingPost } from '@/services/xiaohang-backend/userController';
import {
  LockOutlined,
  UserOutlined,
  ApiOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  CloudServerOutlined,
  CodeOutlined,
  DatabaseOutlined,
  ArrowRightOutlined,
} from '@ant-design/icons';
import { Alert, message } from 'antd';
import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useModel } from '@umijs/max';
import './index.less';

type LoginType = 'account' | 'register';

interface FormValues {
  userAccount: string;
  userPassword: string;
  checkPassword?: string;
  autoLogin?: boolean;
}

const DATA_STREAMS = [
  'GET /api/v1/users',
  'POST /api/v1/auth',
  '200 OK | 42ms',
  'x-rate-limit: 1000',
  'Content-Type: json',
  'Authorization: Bearer',
  'REST API v2.0',
  'GraphQL Endpoint',
  'WebSocket: Connected',
  'SDK: Initialized',
  'RPC Call | 12ms',
  'gRPC Stream',
  'PUT /api/v1/config',
  'DELETE /api/v1/cache',
  'PATCH /api/v1/keys',
  'OPTIONS /api/v1/*',
];

const Login: React.FC = () => {
  const { setInitialState } = useModel('@@initialState');
  const [loginType, setLoginType] = useState<LoginType>('account');
  const [streamItems, setStreamItems] = useState<string[]>([]);
  const [panelOpen, setPanelOpen] = useState(false);
  const [panelClosing, setPanelClosing] = useState(false);
  const closeTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [formValues, setFormValues] = useState<FormValues>({
    userAccount: '',
    userPassword: '',
    checkPassword: '',
    autoLogin: false,
  });
  const [loading, setLoading] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    let idx = 0;
    const interval = setInterval(() => {
      setStreamItems((prev) => {
        const next = [...prev, DATA_STREAMS[idx % DATA_STREAMS.length]];
        if (next.length > 12) next.shift();
        return next;
      });
      idx++;
    }, 900);
    return () => clearInterval(interval);
  }, []);

  const lastMouseXRef = useRef<number | null>(null);

  const handleMouseMove = useCallback((e: MouseEvent) => {
    const threshold = window.innerWidth * 0.82;
    const prevX = lastMouseXRef.current;
    lastMouseXRef.current = e.clientX;
    if (prevX === null) return;
    if (prevX <= threshold && e.clientX > threshold && !panelOpen && !panelClosing) {
      setPanelOpen(true);
    }
  }, [panelOpen, panelClosing]);

  useEffect(() => {
    window.addEventListener('mousemove', handleMouseMove);
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, [handleMouseMove]);

  const handleSubmit = async () => {
    const { userPassword, checkPassword, userAccount } = formValues;
    if (!userAccount) { message.error('Username is required!'); return; }
    if (!userPassword) { message.error('Password is required!'); return; }
    setLoading(true);
    try {
      if (loginType === 'register') {
        if (userPassword !== checkPassword) {
          message.error('The passwords do not match!');
          return;
        }
        const res = await userRegisterUsingPost(formValues);
        if (res.code === 0) {
          message.success('Registration successful!');
          setLoginType('account');
          setFormValues({ userAccount: '', userPassword: '', checkPassword: '' });
        }
      } else {
        const res = await userLoginUsingPost({ userAccount, userPassword });
        if (res.data) {
          message.success('Login successful!');
          const urlParams = new URL(window.location.href).searchParams;
          location.href = urlParams.get('redirect') || '/';
          setInitialState({ loginUser: res.data });
        } else {
          message.error(res.message);
        }
      }
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setPanelOpen(false);
    setPanelClosing(true);
    if (closeTimerRef.current) clearTimeout(closeTimerRef.current);
    closeTimerRef.current = setTimeout(() => setPanelClosing(false), 350);
  };

  return (
    <div className={`login-page-wrapper ${mounted ? 'login-mounted' : ''}`}>

      {/* ========================================================
          FULL-SCREEN BRAND PANEL
          ======================================================== */}
      <div className={`login-brand-panel ${panelOpen ? 'brand-panel-shift' : ''}`}>
        {/* Background layer */}
        <div className="brand-bg-layer" />

        {/* Network topology SVG */}
        <svg className="network-graph" viewBox="0 0 600 460" fill="none">
          <line x1="300" y1="230" x2="160" y2="115" stroke="url(#ngGrad)" strokeWidth="0.8" opacity="0.2" />
          <line x1="300" y1="230" x2="440" y2="115" stroke="url(#ngGrad)" strokeWidth="0.8" opacity="0.2" />
          <line x1="300" y1="230" x2="120" y2="280" stroke="url(#ngGrad)" strokeWidth="0.8" opacity="0.15" />
          <line x1="300" y1="230" x2="480" y2="280" stroke="url(#ngGrad)" strokeWidth="0.8" opacity="0.15" />
          <line x1="300" y1="230" x2="200" y2="380" stroke="url(#ngGrad)" strokeWidth="0.6" opacity="0.1" />
          <line x1="300" y1="230" x2="400" y2="380" stroke="url(#ngGrad)" strokeWidth="0.6" opacity="0.1" />
          <line x1="160" y1="115" x2="70" y2="65" stroke="url(#ngGrad)" strokeWidth="0.4" opacity="0.1" />
          <line x1="160" y1="115" x2="230" y2="65" stroke="url(#ngGrad)" strokeWidth="0.4" opacity="0.1" />
          <line x1="440" y1="115" x2="370" y2="65" stroke="url(#ngGrad)" strokeWidth="0.4" opacity="0.1" />
          <line x1="440" y1="115" x2="530" y2="65" stroke="url(#ngGrad)" strokeWidth="0.4" opacity="0.1" />
          <line x1="120" y1="280" x2="55" y2="330" stroke="url(#ngGrad)" strokeWidth="0.4" opacity="0.08" />
          <line x1="120" y1="280" x2="185" y2="340" stroke="url(#ngGrad)" strokeWidth="0.4" opacity="0.08" />
          <line x1="480" y1="280" x2="415" y2="340" stroke="url(#ngGrad)" strokeWidth="0.4" opacity="0.08" />
          <line x1="480" y1="280" x2="545" y2="330" stroke="url(#ngGrad)" strokeWidth="0.4" opacity="0.08" />
          <circle r="2.5" fill="#00D4AA" opacity="0.8">
            <animateMotion dur="3.5s" repeatCount="indefinite" path="M300,230 L160,115 L70,65" />
          </circle>
          <circle r="2" fill="#00A3FF" opacity="0.7">
            <animateMotion dur="4.5s" repeatCount="indefinite" path="M300,230 L440,115 L530,65" />
          </circle>
          <circle r="2" fill="#00D4AA" opacity="0.6">
            <animateMotion dur="2.8s" repeatCount="indefinite" path="M300,230 L120,280 L55,330" />
          </circle>
          <circle r="1.5" fill="#00A3FF" opacity="0.6">
            <animateMotion dur="3.8s" repeatCount="indefinite" path="M300,230 L480,280 L545,330" />
          </circle>
          <defs>
            <linearGradient id="ngGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#00D4AA" />
              <stop offset="100%" stopColor="#00A3FF" />
            </linearGradient>
            <radialGradient id="ngHubGlow" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stopColor="#00D4AA" stopOpacity="0.25" />
              <stop offset="100%" stopColor="#00D4AA" stopOpacity="0" />
            </radialGradient>
          </defs>
          <circle cx="160" cy="115" r="7" fill="rgba(0,212,170,0.08)" stroke="#00D4AA" strokeWidth="0.8" opacity="0.35" />
          <circle cx="440" cy="115" r="7" fill="rgba(0,163,255,0.08)" stroke="#00A3FF" strokeWidth="0.8" opacity="0.35" />
          <circle cx="120" cy="280" r="5" fill="rgba(0,212,170,0.06)" stroke="#00D4AA" strokeWidth="0.6" opacity="0.25" />
          <circle cx="480" cy="280" r="5" fill="rgba(0,163,255,0.06)" stroke="#00A3FF" strokeWidth="0.6" opacity="0.25" />
          <circle cx="200" cy="380" r="4" fill="rgba(0,212,170,0.04)" stroke="#00D4AA" strokeWidth="0.5" opacity="0.2" />
          <circle cx="400" cy="380" r="4" fill="rgba(0,163,255,0.04)" stroke="#00A3FF" strokeWidth="0.5" opacity="0.2" />
          <circle cx="70" cy="65" r="2.5" fill="rgba(0,212,170,0.3)" />
          <circle cx="230" cy="65" r="2.5" fill="rgba(0,212,170,0.3)" />
          <circle cx="370" cy="65" r="2.5" fill="rgba(0,163,255,0.3)" />
          <circle cx="530" cy="65" r="2.5" fill="rgba(0,163,255,0.3)" />
          <circle cx="55" cy="330" r="2" fill="rgba(0,212,170,0.2)" />
          <circle cx="185" cy="340" r="2" fill="rgba(0,212,170,0.2)" />
          <circle cx="415" cy="340" r="2" fill="rgba(0,163,255,0.2)" />
          <circle cx="545" cy="330" r="2" fill="rgba(0,163,255,0.2)" />
          <circle cx="300" cy="230" r="45" fill="url(#ngHubGlow)" />
          <circle cx="300" cy="230" r="24" fill="rgba(0,212,170,0.08)" stroke="#00D4AA" strokeWidth="1.2" />
          <circle cx="300" cy="230" r="14" fill="rgba(0,212,170,0.12)" stroke="#00D4AA" strokeWidth="1" />
          <circle cx="300" cy="230" r="5" fill="#00D4AA" className="hub-pulse" />
        </svg>

        {/* Main brand content — centered */}
        <div className="brand-content">
          {/* Logo + Brand name */}
          <div className="brand-logo-row">
            <svg className="brand-logo-svg" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <linearGradient id="blGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stopColor="#00D4AA" />
                  <stop offset="100%" stopColor="#00A3FF" />
                </linearGradient>
              </defs>
              <path d="M24 4L40 13V29L24 38L8 29V13L24 4Z" stroke="url(#blGrad)" strokeWidth="1.5" fill="none" />
              <path d="M24 12L34 17.5V28.5L24 34L14 28.5V17.5L24 12Z" fill="url(#blGrad)" fillOpacity="0.12" stroke="url(#blGrad)" strokeWidth="1" />
              <circle cx="24" cy="23" r="4" fill="url(#blGrad)" />
              <line x1="24" y1="19" x2="24" y2="12" stroke="url(#blGrad)" strokeWidth="1.5" strokeLinecap="round" />
              <line x1="27.5" y1="21" x2="34" y2="17.5" stroke="url(#blGrad)" strokeWidth="1.5" strokeLinecap="round" />
              <line x1="27.5" y1="25" x2="34" y2="28.5" stroke="url(#blGrad)" strokeWidth="1.5" strokeLinecap="round" />
              <line x1="24" y1="27" x2="24" y2="34" stroke="url(#blGrad)" strokeWidth="1.5" strokeLinecap="round" />
              <line x1="20.5" y1="25" x2="14" y2="28.5" stroke="url(#blGrad)" strokeWidth="1.5" strokeLinecap="round" />
              <line x1="20.5" y1="21" x2="14" y2="17.5" stroke="url(#blGrad)" strokeWidth="1.5" strokeLinecap="round" />
            </svg>
            <div className="brand-text">
              <h1 className="brand-title">
                API Marketplace
                <span className="brand-title-accent">Platform</span>
              </h1>
              <p className="brand-tagline">
                The unified hub for discovering, integrating, and scaling<br />
                third-party APIs with ease and confidence.
              </p>
            </div>
          </div>

          {/* API Terminal */}
          <div className="api-stream-terminal">
            <div className="terminal-header">
              <div className="terminal-dots">
                <span /><span /><span />
              </div>
              <span className="terminal-title">api_stream.log</span>
              <span className="terminal-live-dot" />
            </div>
            <div className="terminal-body">
              {streamItems.map((line, i) => (
                <div
                  key={i}
                  className={`stream-line ${
                    line.includes('200') || line.includes('Connected') || line.includes('Initialized')
                      ? 'stream-ok'
                      : line.includes('Error') || line.includes('Failed')
                        ? 'stream-err'
                        : 'stream-req'
                  }`}
                  style={{ animationDelay: `${i * 0.04}s` }}
                >
                  <span className="stream-prefix">{'>'}</span>
                  <span className="stream-text">{line}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Feature pills */}
          <div className="login-feature-pills">
            <div className="feature-pill">
              <ThunderboltOutlined />
              <span>Low Latency</span>
            </div>
            <div className="feature-pill">
              <SafetyCertificateOutlined />
              <span>Enterprise Security</span>
            </div>
            <div className="feature-pill">
              <CloudServerOutlined />
              <span>99.9% Uptime</span>
            </div>
            <div className="feature-pill">
              <CodeOutlined />
              <span>Multi-SDK</span>
            </div>
          </div>
        </div>

        {/* Hover hint — right edge */}
        <div className="hover-hint">
          <ArrowRightOutlined className="hint-arrow" />
          <span>Slide to sign in</span>
        </div>

        {/* Trigger button — always visible */}
        <button
          className="trigger-btn"
          onClick={() => setPanelOpen(true)}
          aria-label="Open login panel"
        >
          <span>Sign In</span>
          <ArrowRightOutlined />
        </button>
      </div>

      {/* ========================================================
          SLIDE-IN LOGIN PANEL
          ======================================================== */}
      <div className={`login-slide-panel ${panelOpen ? 'slide-panel-open' : ''}`}>
        {/* Backdrop blur layer */}
        <div className="slide-backdrop" onClick={handleClose} />

        {/* Panel content */}
        <div className="slide-panel-inner">
          {/* Close button */}
          <button className="slide-close-btn" onClick={handleClose} aria-label="Close panel">
            <span>&times;</span>
          </button>

          {/* Panel header */}
          <div className="slide-panel-header">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" className="slide-logo-svg">
              <defs>
                <linearGradient id="slGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stopColor="#00D4AA" />
                  <stop offset="100%" stopColor="#00A3FF" />
                </linearGradient>
              </defs>
              <path d="M24 4L40 13V29L24 38L8 29V13L24 4Z" stroke="url(#slGrad)" strokeWidth="1.5" fill="none" />
              <path d="M24 12L34 17.5V28.5L24 34L14 28.5V17.5L24 12Z" fill="url(#slGrad)" fillOpacity="0.12" stroke="url(#slGrad)" strokeWidth="1" />
              <circle cx="24" cy="23" r="4" fill="url(#slGrad)" />
              <line x1="24" y1="19" x2="24" y2="12" stroke="url(#slGrad)" strokeWidth="1.5" strokeLinecap="round" />
              <line x1="27.5" y1="21" x2="34" y2="17.5" stroke="url(#slGrad)" strokeWidth="1.5" strokeLinecap="round" />
              <line x1="27.5" y1="25" x2="34" y2="28.5" stroke="url(#slGrad)" strokeWidth="1.5" strokeLinecap="round" />
              <line x1="24" y1="27" x2="24" y2="34" stroke="url(#slGrad)" strokeWidth="1.5" strokeLinecap="round" />
              <line x1="20.5" y1="25" x2="14" y2="28.5" stroke="url(#slGrad)" strokeWidth="1.5" strokeLinecap="round" />
              <line x1="20.5" y1="21" x2="14" y2="17.5" stroke="url(#slGrad)" strokeWidth="1.5" strokeLinecap="round" />
            </svg>
            <h2 className="slide-panel-title">Welcome back</h2>
            <p className="slide-panel-subtitle">Sign in to your developer dashboard</p>
            <div className="slide-meta-row">
              <span className="meta-tag"><ApiOutlined /> v2.0 REST API</span>
              <span className="meta-tag meta-secure"><SafetyCertificateOutlined /> SOC 2</span>
            </div>
          </div>

          {/* Tab switcher */}
          <div className="login-tabs-wrap">
            <div
              className={`tab-btn ${loginType === 'account' ? 'tab-active' : ''}`}
              onClick={() => setLoginType('account')}
            >
              Sign In
            </div>
            <div
              className={`tab-btn ${loginType === 'register' ? 'tab-active' : ''}`}
              onClick={() => setLoginType('register')}
            >
              Create Account
            </div>
            <div
              className="tab-indicator"
              style={{ left: loginType === 'account' ? '0%' : '50%' }}
            />
          </div>

          {/* Form fields */}
          <div className="form-fields">
            {loginType === 'account' && (
              <>
                <div className="form-field-group">
                  <label className="form-label">Username</label>
                  <div className="input-wrapper">
                    <UserOutlined className="input-prefix-icon" />
                    <input
                      className="form-input"
                      placeholder="Username or email"
                      value={formValues.userAccount}
                      onChange={(e) => setFormValues((v) => ({ ...v, userAccount: e.target.value }))}
                      autoFocus
                    />
                  </div>
                </div>
                <div className="form-field-group">
                  <label className="form-label">Password</label>
                  <div className="input-wrapper">
                    <LockOutlined className="input-prefix-icon" />
                    <input
                      className="form-input"
                      type="password"
                      placeholder="Password"
                      value={formValues.userPassword}
                      onChange={(e) => setFormValues((v) => ({ ...v, userPassword: e.target.value }))}
                    />
                  </div>
                </div>
                <div className="form-options-row">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      className="form-checkbox"
                      checked={formValues.autoLogin}
                      onChange={(e) => setFormValues((v) => ({ ...v, autoLogin: e.target.checked }))}
                    />
                    <span className="remember-me-text">Remember me</span>
                  </label>
                  <a
                    className="forgot-link"
                    onClick={() => message.info('Please contact the administrator to reset your password.')}
                  >
                    Forgot password?
                  </a>
                </div>
              </>
            )}
            {loginType === 'register' && (
              <>
                <div className="form-field-group">
                  <label className="form-label">Username</label>
                  <div className="input-wrapper">
                    <UserOutlined className="input-prefix-icon" />
                    <input
                      className="form-input"
                      placeholder="Username (min 4 characters)"
                      value={formValues.userAccount}
                      onChange={(e) => setFormValues((v) => ({ ...v, userAccount: e.target.value }))}
                    />
                  </div>
                </div>
                <div className="form-field-group">
                  <label className="form-label">Password</label>
                  <div className="input-wrapper">
                    <LockOutlined className="input-prefix-icon" />
                    <input
                      className="form-input"
                      type="password"
                      placeholder="Password (min 8 characters)"
                      value={formValues.userPassword}
                      onChange={(e) => setFormValues((v) => ({ ...v, userPassword: e.target.value }))}
                    />
                  </div>
                </div>
                <div className="form-field-group">
                  <label className="form-label">Confirm Password</label>
                  <div className="input-wrapper">
                    <LockOutlined className="input-prefix-icon" />
                    <input
                      className="form-input"
                      type="password"
                      placeholder="Confirm password"
                      value={formValues.checkPassword}
                      onChange={(e) => setFormValues((v) => ({ ...v, checkPassword: e.target.value }))}
                    />
                  </div>
                </div>
              </>
            )}

            <button className="form-submit-btn" onClick={handleSubmit} disabled={loading}>
              {loading ? 'Signing in...' : loginType === 'account' ? 'Sign In' : 'Create Account'}
            </button>

            <Alert
              message="Database wake-up notice"
              description="If you see a database error on first login, give it a moment to initialize — then try again."
              type="info"
              showIcon
              icon={<DatabaseOutlined />}
              className="db-notice-alert"
            />
          </div>

          <div className="login-test-hint">
            Demo: <code>xiaohang</code> / <code>12345678</code>
          </div>
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default Login;
