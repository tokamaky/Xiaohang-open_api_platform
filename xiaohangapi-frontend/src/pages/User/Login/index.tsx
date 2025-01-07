import Footer from '@/components/Footer';
import {userLoginUsingPost, userRegisterUsingPost} from '@/services/xiaohang-backend/userController';
import {LockOutlined, UserOutlined,} from '@ant-design/icons';
import {LoginFormPage, ProFormCheckbox, ProFormInstance, ProFormText,} from '@ant-design/pro-components';
import {useModel} from '@umijs/max';
import {message, Tabs} from 'antd';
import type {CSSProperties} from 'react';
import React, {useRef, useState} from 'react';
import pandaBackImg from '../../../../public/panda2.jpg';
import logo from '../../../../public/logo.png';

type LoginType = 'account' | 'register' | 'forgetPassword';

const iconStyles: CSSProperties = {
    color: 'rgba(0, 0, 0, 0.2)',
    fontSize: '18px',
    verticalAlign: 'middle',
    cursor: 'pointer',
};
const Login: React.FC = () => {
    const {initialState, setInitialState} = useModel('@@initialState');
    const [loginType, setLoginType] = useState<LoginType>('account');
    const formRef = useRef<ProFormInstance>();


  const handleSubmit = async (values: API.UserRegisterRequest) => {
    const { userPassword, checkPassword } = values;
    if (checkPassword) {
      // Register
      if (userPassword !== checkPassword) {
        message.error('The passwords do not match!');
        return;
      }
      const res = await userRegisterUsingPost(values);
      if (res.code === 0) {
        // Registration success
        const defaultRegisterSuccessMessage = 'Registration successful!';
        message.success(defaultRegisterSuccessMessage);
        // Switch to login
        setLoginType('account');
        // Reset form
        formRef.current?.resetFields();
      }

    } else {
      // Login
      const res = await userLoginUsingPost({
        ...values,
      });
      if (res.data) {
        const defaultLoginSuccessMessage = 'Login successful!';
        message.success(defaultLoginSuccessMessage);
        // Handle login success
        const urlParams = new URL(window.location.href).searchParams;
        // Redirect to the location specified by the redirect parameter
        location.href = urlParams.get('redirect') || '/';
        // Save login state
        setInitialState({
          loginUser: res.data,
        });
      } else {
        message.error(res.message);
      }
    }
  };

  return (
    <div>
      <div
        style={{
          backgroundColor: 'white',
          height: 'calc(100vh - 100px)',
          margin: 0,
        }}
      >
        <LoginFormPage
          backgroundImageUrl={pandaBackImg}
          logo={logo}
          title="Panda API"
          subTitle="**The Best Free API Interface Platform in History**"
          initialValues={{
            autoLogin: true,
          }}
          onFinish={async (values) => {
            await handleSubmit(values as API.UserRegisterRequest);
          }}
        >
          {
            <Tabs
              centered
              activeKey={loginType}
              onChange={(activeKey) => setLoginType(activeKey as LoginType)}
            >
              <Tabs.TabPane key={'account'} tab={'Login'} />
              <Tabs.TabPane key={'register'} tab={'Register'} />
            </Tabs>
          }
          {loginType === 'account' && (
            <>
              <ProFormText
                name="userAccount"
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined />,
                }}
                placeholder={'Please enter your username'}
                rules={[
                  {
                    required: true,
                    message: 'Username is required!',
                  },
                ]}
              />
              <ProFormText.Password
                name="userPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined />,
                }}
                placeholder={'Please enter your password'}
                rules={[
                  {
                    required: true,
                    message: 'Password is required!',
                  },
                ]}
              />
              <div
                style={{
                  marginBottom: 24,
                }}
              >
                <ProFormCheckbox noStyle name="autoLogin">
                  Auto-login
                </ProFormCheckbox>
                <a
                  style={{
                    float: 'right',
                  }}
                  onClick={() => setLoginType("forgetPassword")}
                >
                  Forgot password?
                </a>
              </div>
            </>
          )}
          {loginType === 'register' && (
            <>
              <ProFormText
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined />,
                }}
                name="userAccount"
                placeholder={'Please enter your username'}
                rules={[
                  {
                    required: true,
                    message: 'Username is required!',
                  },
                  {
                    min: 4,
                    message: 'Length cannot be less than 4 characters!',
                  },
                ]}
              />
              <ProFormText.Password
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined />,
                }}
                name="userPassword"
                placeholder={'Please enter your password'}
                rules={[
                  {
                    required: true,
                    message: 'Password is required!',
                  },
                  {
                    min: 8,
                    message: 'Length cannot be less than 8 characters!',
                  },
                ]}
              />
              <ProFormText.Password
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined />,
                }}
                name="checkPassword"
                placeholder={'Please re-enter your password'}
                rules={[
                  {
                    required: true,
                    message: 'Password is required!',
                  },
                  {
                    min: 8,
                    message: 'Length cannot be less than 8 characters!',
                  },
                ]}
              />
            </>
          )}
        </LoginFormPage>
      </div>
      <Footer />
    </div>
  );
};
export default Login;
