import Footer from '@/components/Footer';
import { userLoginUsingPost, userRegisterUsingPost } from '@/services/xiaohang-backend/userController';
import { LockOutlined, UserOutlined, ApiOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { LoginFormPage, ProFormCheckbox, ProFormInstance, ProFormText } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { Alert, message, Tabs } from 'antd';
import type { CSSProperties } from 'react';
import React, { useRef, useState } from 'react';
import './index.less';

type LoginType = 'account' | 'register';

const iconStyles: CSSProperties = {
  color: 'rgba(99, 102, 241, 0.4)',
  fontSize: '16px',
  verticalAlign: 'middle',
  cursor: 'pointer',
};

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
        const defaultRegisterSuccessMessage = 'Registration successful!';
        message.success(defaultRegisterSuccessMessage);
        setLoginType('account');
        formRef.current?.resetFields();
      }
    } else {
      const res = await userLoginUsingPost({ ...values });
      if (res.data) {
        const defaultLoginSuccessMessage = 'Login successful!';
        message.success(defaultLoginSuccessMessage);
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
      {/* Animated background grid */}
      <div className="login-bg-grid" />
      {/* Floating particles */}
      <div className="login-particles">
        {Array.from({ length: 12 }).map((_, i) => (
          <div key={i} className={`particle particle-${i + 1}`} />
        ))}
      </div>
      {/* Glow orbs */}
      <div className="glow-orb glow-orb-1" />
      <div className="glow-orb glow-orb-2" />

      <div className="login-content-area">
        <LoginFormPage
          logo={<img src="/api-logo.svg" alt="API Marketplace" className="login-logo-img" />}
          title={<span className="login-title-text">API Marketplace Platform</span>}
          subTitle={
            <div className="login-subtitle-wrapper">
              <p className="login-subtitle-main">Discover, Integrate, and Scale Your APIs</p>
              <div className="login-features">
                <span className="feature-tag">
                  <ApiOutlined /> Unified Access
                </span>
                <span className="feature-tag">
                  <SafetyCertificateOutlined /> Secure by Default
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
                description="If you encounter a database error on first login, don't worry — the database needs a moment to wake up. Simply try logging in again."
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
                <a className="forgot-link" onClick={() => message.info('Please contact the administrator to reset your password.')}>
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

        <div className="login-footer-info">
          <p className="test-account-hint">
            <span className="hint-label">Demo Account:</span>
            <code>xiaohang</code> / <code>12345678</code>
          </p>
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default Login;
