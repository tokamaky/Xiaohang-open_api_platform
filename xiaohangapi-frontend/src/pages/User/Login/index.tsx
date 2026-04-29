import Footer from '@/components/Footer';
import { userLoginUsingPost, userRegisterUsingPost } from '@/services/xiaohang-backend/userController';
import {
  LockOutlined,
  UserOutlined,
  ApiOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  CloudOutlined,
  BarChartOutlined,
} from '@ant-design/icons';
import { LoginFormPage, ProFormCheckbox, ProFormInstance, ProFormText } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { Alert, message, Tabs } from 'antd';
import React, { useRef, useState } from 'react';
import './index.less';

type LoginType = 'account' | 'register';

const Login: React.FC = () => {
  const { setInitialState } = useModel('@@initialState');
  const [loginType, setLoginType] = useState<LoginType>('account');
  const formRef = useRef<ProFormInstance>();

  const handleSubmit = async (values: API.UserRegisterRequest) => {
    const { userPassword, checkPassword } = values;
    if (checkPassword) {
      if (userPassword !== checkPassword) {
        message.error('The passwords do not match!');
        return;
      }
      const res = await userRegisterUsingPost(values);
      if (res.code === 0) {
        message.success('Registration successful!');
        setLoginType('account');
        formRef.current?.resetFields();
      }
    } else {
      const res = await userLoginUsingPost({ ...values });
      if (res.data) {
        message.success('Login successful!');
        const urlParams = new URL(window.location.href).searchParams;
        location.href = urlParams.get('redirect') || '/';
        setInitialState({ loginUser: res.data });
      } else {
        message.error(res.message);
      }
    }
  };

  return (
    <div className="login-page-wrapper">
      {/* ======================== LEFT PANEL ======================== */}
      <div className="login-left-panel">
        {/* Ambient glows */}
        <div className="panel-glow panel-glow-1" />
        <div className="panel-glow panel-glow-2" />

        {/* Floating particles */}
        <div className="login-left-particles">
          {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
            <div key={i} className={`particle particle-${i}`} />
          ))}
        </div>

        {/* Decorative hexagons */}
        <svg className="login-hex-decor login-hex-1" viewBox="0 0 100 100" fill="none">
          <path d="M50 5L90 27.5V72.5L50 95L10 72.5V27.5L50 5Z" stroke="white" strokeWidth="1" fill="none" />
          <path d="M50 20L75 35V65L50 80L25 65V35L50 20Z" stroke="white" strokeWidth="0.5" fill="none" />
          <circle cx="50" cy="50" r="6" stroke="white" strokeWidth="0.5" fill="none" />
        </svg>
        <svg className="login-hex-decor login-hex-2" viewBox="0 0 100 100" fill="none">
          <path d="M50 5L90 27.5V72.5L50 95L10 72.5V27.5L50 5Z" stroke="white" strokeWidth="1" fill="none" />
          <path d="M50 20L75 35V65L50 80L25 65V35L50 20Z" stroke="white" strokeWidth="0.5" fill="none" />
        </svg>
        <svg className="login-hex-decor login-hex-3" viewBox="0 0 100 100" fill="none">
          <path d="M50 5L90 27.5V72.5L50 95L10 72.5V27.5L50 5Z" stroke="white" strokeWidth="1" fill="none" />
        </svg>

        {/* Left panel content */}
        <div className="login-left-content">
          <img src="/api-logo.svg" alt="API Marketplace" className="login-left-logo" />
          <h1 className="login-left-brand">
            API Marketplace
            <br />
            <span>Platform</span>
          </h1>
          <p className="login-left-tagline">
            The unified hub for discovering, integrating, and scaling
            third-party APIs with ease and confidence.
          </p>

          {/* Feature grid */}
          <div className="login-left-features">
            <div className="login-left-feature">
              <div className="feature-icon">
                <ThunderboltOutlined />
              </div>
              <div className="feature-title">Lightning Fast</div>
              <div className="feature-desc">Real-time API responses with minimal latency</div>
            </div>
            <div className="login-left-feature">
              <div className="feature-icon">
                <SafetyCertificateOutlined />
              </div>
              <div className="feature-title">Secure by Default</div>
              <div className="feature-desc">End-to-end encryption and access control</div>
            </div>
            <div className="login-left-feature">
              <div className="feature-icon">
                <CloudOutlined />
              </div>
              <div className="feature-title">Cloud Native</div>
              <div className="feature-desc">Built for scalability and reliability</div>
            </div>
            <div className="login-left-feature">
              <div className="feature-icon">
                <BarChartOutlined />
              </div>
              <div className="feature-title">Usage Analytics</div>
              <div className="feature-desc">Track and monitor every API call</div>
            </div>
          </div>

          {/* Stats row */}
          <div className="login-left-stats">
            <div className="login-stat">
              <div className="stat-number">50+</div>
              <div className="stat-label">APIs</div>
            </div>
            <div className="login-stat">
              <div className="stat-number">99.9%</div>
              <div className="stat-label">Uptime</div>
            </div>
            <div className="login-stat">
              <div className="stat-number">24/7</div>
              <div className="stat-label">Support</div>
            </div>
          </div>
        </div>
      </div>

      {/* ======================== RIGHT PANEL ======================== */}
      <div className="login-right-panel">
        <div className="panel-glow" />

        {/* Login Form */}
        <LoginFormPage
          logo={<img src="/api-logo.svg" alt="API Marketplace" className="login-logo-img" />}
          title={<span className="login-title-text">API Marketplace Platform</span>}
          subTitle={
            <div className="login-subtitle-wrapper">
              <p className="login-subtitle-main">Sign in to access your API dashboard</p>
              <div className="login-features">
                <span className="feature-tag">
                  <ApiOutlined /> Unified Access
                </span>
                <span className="feature-tag">
                  <SafetyCertificateOutlined /> Secure
                </span>
              </div>
            </div>
          }
          initialValues={{ autoLogin: false }}
          onFinish={async (values) => {
            await handleSubmit(values as API.UserRegisterRequest);
          }}
          actions={
            <div className="login-bottom-notice">
              <Alert
                message="First-time login notice"
                description="If you encounter a database error on first login, do not worry — the database needs a moment to wake up. Simply try logging in again."
                type="info"
                showIcon
                icon={<SafetyCertificateOutlined />}
                className="db-notice-alert"
              />
            </div>
          }
          className="custom-login-form"
        >
          <Tabs
            centered
            activeKey={loginType}
            onChange={(activeKey) => setLoginType(activeKey as LoginType)}
            className="login-tabs"
            items={[
              { key: 'account', label: 'Sign In' },
              { key: 'register', label: 'Create Account' },
            ]}
          />
          {loginType === 'account' && (
            <>
              <ProFormText
                name="userAccount"
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined />,
                }}
                placeholder={'Username'}
                rules={[{ required: true, message: 'Username is required!' }]}
              />
              <ProFormText.Password
                name="userPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined />,
                }}
                placeholder={'Password'}
                rules={[{ required: true, message: 'Password is required!' }]}
              />
              <div className="login-form-options">
                <ProFormCheckbox noStyle name="autoLogin">
                  <span className="remember-me-text">Remember me</span>
                </ProFormCheckbox>
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
              <ProFormText
                fieldProps={{ size: 'large', prefix: <UserOutlined /> }}
                name="userAccount"
                placeholder={'Username (min 4 characters)'}
                rules={[
                  { required: true, message: 'Username is required!' },
                  { min: 4, message: 'Length cannot be less than 4 characters!' },
                ]}
              />
              <ProFormText.Password
                fieldProps={{ size: 'large', prefix: <LockOutlined /> }}
                name="userPassword"
                placeholder={'Password (min 8 characters)'}
                rules={[
                  { required: true, message: 'Password is required!' },
                  { min: 8, message: 'Length cannot be less than 8 characters!' },
                ]}
              />
              <ProFormText.Password
                fieldProps={{ size: 'large', prefix: <LockOutlined /> }}
                name="checkPassword"
                placeholder={'Confirm password'}
                rules={[
                  { required: true, message: 'Please confirm your password!' },
                  { min: 8, message: 'Length cannot be less than 8 characters!' },
                ]}
              />
            </>
          )}
        </LoginFormPage>

        {/* Demo account hint */}
        <div className="login-test-hint">
          Demo: <code>xiaohang</code> / <code>12345678</code>
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default Login;
